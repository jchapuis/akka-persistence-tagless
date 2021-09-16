package akka.persistence.tagless.core.typeclass.protocol

trait IncomingCommand[Alg[_[_]], Code] {
  type Reply
  def command: Command[Alg, Reply]
  def replyEncoder: Encoder[Reply, Code]
}
