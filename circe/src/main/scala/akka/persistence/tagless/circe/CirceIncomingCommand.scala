package akka.persistence.tagless.circe

import akka.persistence.tagless.circe.CirceEncoder._
import akka.persistence.tagless.core.typeclass.protocol.IncomingCommand
import io.circe.Json

abstract class CirceIncomingCommand[Alg[_[_]], R: io.circe.Encoder]
    extends IncomingCommand[Alg, Json] {
  type Reply = R
  override def replyEncoder: CirceEncoder[Reply] = implicitly
}
