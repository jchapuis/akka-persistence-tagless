package akka.persistence.tagless.core.typeclass.protocol

trait CommandProtocol[Alg[_[_]], Code] {
  def server: Decoder[Code, IncomingCommand[Alg, Code]]
  def client: Alg[OutgoingCommand[Code, *]]
}
