package akka.persistence.tagless.runtime
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.persistence.tagless.core.interpret.{EntityT, RepositoryT}
import akka.persistence.tagless.core.typeclass.entity.EntityNameProvider
import akka.persistence.tagless.core.typeclass.event.EventApplier
import akka.persistence.tagless.core.typeclass.protocol.{
  CommandProtocol,
  CommandRouter,
  EntityIDEncoder
}
import akka.persistence.tagless.runtime.data.{Command, Reply}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import cats.Functor
import cats.effect.kernel.{Async, Resource}
import cats.effect.std.Dispatcher
import cats.syntax.functor._
import cats.tagless.FunctorK
import ShardingCommandRouter._
import akka.util.Timeout

trait Deployer {
  def deployEntity[F[_]: Functor, S, E, ID, Code, Alg[_[_]]: FunctorK](emptyState: S)(implicit
      sharding: ClusterSharding,
      actorSystem: ActorSystem[_],
      nameProvider: EntityNameProvider[ID],
      entityIDEncoder: EntityIDEncoder[ID],
      commandProcessor: Alg[EntityT[F, S, E, *]],
      commandProtocol: CommandProtocol[Alg, Code],
      applier: EventApplier[S, E],
      askTimeout: Timeout,
      F: Async[F]
  ): Resource[F, ActorRef[ShardingEnvelope[Command[Code]]]] = Dispatcher[F].map { dispatcher =>
    val entityTypeKey = EntityTypeKey[Command[Code]](nameProvider())
    val entity = Entity(
      EntityTypeKey[Command[Code]](nameProvider())
    ) { context =>
      EventSourcedBehavior.withEnforcedReplies[Command[Code], E, S](
        PersistenceId(entityTypeKey.name, context.entityId),
        emptyState,
        commandHandler = (state, command) => {
          val incomingCommand = commandProtocol.server[EntityT[F, S, E, *]].decode(command.payload)
          val effect = RepositoryT.instance
            .runCommand(state, incomingCommand)
            .map {
              case Left(error) => throw new RunCommandException(error)
              case Right((events, reply)) =>
                Effect.persist(events.toList).thenReply(command.replyTo) { _: S =>
                  Reply(incomingCommand.replyEncoder.encode(reply))
                }
            }
          dispatcher.unsafeRunSync(effect)
        },
        eventHandler = applier.apply(_, _) match {
          case Left(error)     => throw new EventApplierException(error)
          case Right(newState) => newState
        }
      )
    }
    sharding.init(entity)
  }

  final class RunCommandException(error: String) extends RuntimeException(error)
  final class EventApplierException(error: String) extends RuntimeException(error)
}
