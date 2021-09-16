package akka.persistence.tagless.circe

import akka.persistence.tagless.core.typeclass.protocol.{Decoder, OutgoingCommand}
import io.circe
import io.circe.{Encoder, Json}

final class CirceOutgoingCommand[C, +R](command: C)(implicit
    commandEncoder: io.circe.Encoder[C],
    replyDecoder: io.circe.Decoder[R]
) extends OutgoingCommand[Json, R] {
  def payload: Json = commandEncoder.apply(command)
  def replyDecoder: Decoder[Json, R] = CirceDecoder[R]
}

object CirceOutgoingCommand {
  def apply[C, R](command: C)(implicit
      commandEncoder: Encoder[C],
      replyDecoder: circe.Decoder[R]
  ): CirceOutgoingCommand[C, R] = new CirceOutgoingCommand(command)
}
