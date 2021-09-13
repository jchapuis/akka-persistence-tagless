package akka.persistence.tagless.core.typeclass

trait CommandProtocol[Alg[_[_]], Code] {
  def server: Decoder[Code, CommandMessage[Alg, Code]]
  def client: Alg[Encoder[Code, *]]
}
