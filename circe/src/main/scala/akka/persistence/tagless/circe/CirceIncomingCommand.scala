package akka.persistence.tagless.circe

import akka.persistence.tagless.circe.CirceEncoder._
import akka.persistence.tagless.core.typeclass.protocol.IncomingCommand
import io.circe.{Encoder, Json}

abstract class CirceIncomingCommand[F[_], Alg[_[_]], R: io.circe.Encoder]
    extends IncomingCommand[F, Alg, Json] {
  type Reply = R
  override def replyEncoder: CirceEncoder[Reply] = implicitly
}

object CirceIncomingCommand {
  def apply[F[_], Alg[_[_]], R: Encoder](run: Alg[F] => F[R]): CirceIncomingCommand[F, Alg, R] =
    new CirceIncomingCommand[F, Alg, R] {
      def runWith(alg: Alg[F]): F[R] = run(alg)
    }
}
