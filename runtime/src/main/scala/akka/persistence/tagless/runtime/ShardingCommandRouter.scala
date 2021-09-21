package akka.persistence.tagless.runtime

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityTypeKey}
import akka.persistence.tagless.core.typeclass.entity.EntityNameProvider
import akka.persistence.tagless.core.typeclass.protocol.{
  CommandRouter,
  EntityIDEncoder,
  OutgoingCommand
}
import akka.persistence.tagless.runtime.data.{Command, Reply}
import akka.util.Timeout
import cats.effect.kernel.Async
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.~>

final class ShardingCommandRouter[F[_], ID](implicit
    sharding: ClusterSharding,
    actorSystem: ActorSystem[_],
    askTimeout: Timeout,
    idEncoder: EntityIDEncoder[ID],
    nameProvider: EntityNameProvider[ID],
    F: Async[F]
) extends CommandRouter[F, ID] {
  def routerForID[Code](id: ID): OutgoingCommand[Code, *] ~> F =
    new (OutgoingCommand[Code, *] ~> F) {
      def apply[A](fa: OutgoingCommand[Code, A]): F[A] = {
        F.fromFuture {
          F.delay {
            sharding.entityRefFor(
              EntityTypeKey[Command[Code]](nameProvider.name),
              idEncoder(id)
            ) ? Command(idEncoder(id), fa.payload)
          }
        } >>= { case Reply(payload) => fa.replyDecoder.decode(payload).pure[F] }
      }
    }
}

object ShardingCommandRouter {
  implicit def apply[F[_], ID](implicit
      sharding: ClusterSharding,
      actorSystem: ActorSystem[_],
      askTimeout: Timeout,
      idEncoder: EntityIDEncoder[ID],
      nameProvider: EntityNameProvider[ID],
      F: Async[F]
  ): CommandRouter[F, ID] = new ShardingCommandRouter
}
