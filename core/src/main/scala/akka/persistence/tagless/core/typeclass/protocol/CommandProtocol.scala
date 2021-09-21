package akka.persistence.tagless.core.typeclass.protocol

trait CommandProtocol[Alg[_[_]], Code] {
  def server[F[_]]: Decoder[Code, IncomingCommand[F, Alg, Code]]
  def client: Alg[OutgoingCommand[Code, *]]
}
