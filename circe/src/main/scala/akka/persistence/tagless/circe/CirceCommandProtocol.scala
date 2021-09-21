package akka.persistence.tagless.circe

import akka.persistence.tagless.core.typeclass.protocol._
import io.circe.Json

trait CirceCommandProtocol[Alg[_[_]]] extends CommandProtocol[Alg, Json] {
  override def server[F[_]]: Decoder[Json, IncomingCommand[F, Alg, Json]]

  override def client: Alg[OutgoingCommand[Json, *]]

  protected def outgoingCommand[C: io.circe.Encoder, R: io.circe.Decoder](
      command: C
  ): OutgoingCommand[Json, R] = CirceOutgoingCommand(command)

  protected def incomingCommand[F[_], R: io.circe.Encoder](
      run: Alg[F] => F[R]
  ): IncomingCommand[F, Alg, Json] = CirceIncomingCommand(run)
}
