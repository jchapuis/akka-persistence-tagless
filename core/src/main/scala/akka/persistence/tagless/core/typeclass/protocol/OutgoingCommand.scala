package akka.persistence.tagless.core.typeclass.protocol

trait OutgoingCommand[Code, +R] {
  def payload: Code
  def replyDecoder: Decoder[Code, R]
}
