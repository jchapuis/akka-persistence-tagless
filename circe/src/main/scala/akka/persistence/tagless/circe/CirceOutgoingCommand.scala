package akka.persistence.tagless.circe

import akka.persistence.tagless.core.typeclass.protocol.{Decoder, OutgoingCommand}
import io.circe.Json

final class CirceOutgoingCommand[C, +R: io.circe.Decoder](command: C)(implicit
    commandEncoder: io.circe.Encoder[C]
) extends OutgoingCommand[Json, R] {
  def payload: Json = commandEncoder.apply(command)
  def replyDecoder: Decoder[Json, R] = CirceDecoder[R]
}

object CirceOutgoingCommand {
  def apply[C: io.circe.Encoder, R: io.circe.Decoder](command: C): CirceOutgoingCommand[C, R] =
    new CirceOutgoingCommand(command)
}
