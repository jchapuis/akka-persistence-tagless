package akka.persistence.tagless.runtime

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityTypeKey}
import akka.persistence.tagless.core.typeclass.{
  CommandRouter,
  Encoded,
  EntityIDEncoder,
  EntityNameProvider
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
  def routerForID[Code](id: ID): Encoded[Code, *] ~> F = new (Encoded[Code, *] ~> F) {
    def apply[A](fa: Encoded[Code, A]): F[A] = {
      F.fromFuture {
        F.delay {
          sharding.entityRefFor(
            EntityTypeKey[Command[Code]](nameProvider.name),
            idEncoder(id)
          ) ? Command(
            idEncoder(id),
            fa.payload
          )
        }
      } >>= { case Reply(payload) => fa.decoder.decode(payload).pure[F] }
    }
  }
}

object ShardingCommandRouter {
  def apply[F[_], ID](implicit
      sharding: ClusterSharding,
      actorSystem: ActorSystem[_],
      askTimeout: Timeout,
      idEncoder: EntityIDEncoder[ID],
      nameProvider: EntityNameProvider[ID],
      F: Async[F]
  ): ShardingCommandRouter[F, ID] =
    new ShardingCommandRouter
}
