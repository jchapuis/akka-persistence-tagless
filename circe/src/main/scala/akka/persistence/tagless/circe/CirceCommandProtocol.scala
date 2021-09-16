package akka.persistence.tagless.circe

import akka.persistence.tagless.core.typeclass.protocol.{
  CommandProtocol,
  Decoder,
  IncomingCommand,
  OutgoingCommand
}
import io.circe.Json

trait CirceCommandProtocol[Alg[_[_]]]
    extends CommandProtocol[Alg, Json]
    with Decoders
    with Encoders {
  override def server: Decoder[Json, IncomingCommand[Alg, Json]] =
    CirceDecoder[CirceIncomingCommand[Alg]]

  override def client: Alg[OutgoingCommand[Json, *]]

  implicit def incomingCommandDecoder: io.circe.Decoder[CirceIncomingCommand[Alg]]

  protected def outgoingCommand[C: io.circe.Encoder, R: io.circe.Decoder](
      command: C
  ): OutgoingCommand[Json, R] = CirceOutgoingCommand(command)

}
