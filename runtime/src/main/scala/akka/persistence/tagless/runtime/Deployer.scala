package akka.persistence.tagless.runtime
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityTypeKey}
import akka.persistence.tagless.core.interpret.{EntityT, RepositoryT}
import akka.persistence.tagless.core.typeclass.entity.{Entity, EntityNameProvider, Repository}
import akka.persistence.tagless.core.typeclass.event.EventApplier
import akka.persistence.tagless.core.typeclass.protocol.{CommandProtocol, EntityIDEncoder}
import akka.persistence.tagless.runtime.ShardingCommandRouter._
import akka.persistence.tagless.runtime.data.{Command, Reply}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.util.Timeout
import cats.Functor
import cats.effect.kernel.{Async, Resource}
import cats.effect.std.Dispatcher
import cats.syntax.functor._
import cats.tagless.FunctorK

trait Deployer {
  def deployEntity[F[_]: Functor, S, E, ID, Code, Alg[_[_]]: FunctorK, RepositoryAlg[_[_]]](
      createEntity: Entity[EntityT[F, S, E, *], S, E] => Alg[EntityT[F, S, E, *]],
      createRepository: Repository[F, ID, Alg] => RepositoryAlg[F],
      emptyState: S
  )(implicit
      sharding: ClusterSharding,
      actorSystem: ActorSystem[_],
      nameProvider: EntityNameProvider[ID],
      idEncoder: EntityIDEncoder[ID],
      commandProtocol: CommandProtocol[Alg, Code],
      eventApplier: EventApplier[S, E],
      askTimeout: Timeout,
      F: Async[F]
  ): Resource[F, (RepositoryAlg[F], ActorRef[ShardingEnvelope[Command[Code]]])] =
    Dispatcher[F].map { dispatcher =>
      implicit val interpretedEntityAlg: Alg[EntityT[F, S, E, *]] = createEntity(
        EntityT.instance
      )
      val entityTypeKey = EntityTypeKey[Command[Code]](nameProvider())
      val akkaEntity = akka.cluster.sharding.typed.scaladsl.Entity(
        EntityTypeKey[Command[Code]](nameProvider())
      ) { context =>
        EventSourcedBehavior.withEnforcedReplies[Command[Code], E, S](
          PersistenceId(entityTypeKey.name, context.entityId),
          emptyState,
          commandHandler = (state, command) => {
            val incomingCommand =
              commandProtocol.server[EntityT[F, S, E, *]].decode(command.payload)
            val effect = RepositoryT.apply
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
          eventHandler = eventApplier.apply(_, _) match {
            case Left(error)     => throw new EventApplierException(error)
            case Right(newState) => newState
          }
        )
      }
      (createRepository(RepositoryT.apply[F, S, E, ID, Code, Alg]), sharding.init(akkaEntity))
    }

  final class RunCommandException(error: String) extends RuntimeException(error)
  final class EventApplierException(error: String) extends RuntimeException(error)
}
