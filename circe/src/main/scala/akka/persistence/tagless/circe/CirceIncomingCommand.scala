package akka.persistence.tagless.circe

import akka.persistence.tagless.core.typeclass.protocol.{Command, IncomingCommand}
import io.circe.Json

final case class CirceIncomingCommand[Alg[_[_]], R](command: Command[Alg, R])(implicit
    replyEncoder: io.circe.Encoder[R]
) extends IncomingCommand[Alg, Json] {
  type Reply = R
  override def replyEncoder: CirceEncoder[Reply] = CirceEncoder(replyEncoder)
}
