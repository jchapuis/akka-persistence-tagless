package akka.persistence.tagless.core.typeclass.protocol

trait IncomingCommand[F[_], Alg[_[_]], Code] {
  type Reply
  def runWith(alg: Alg[F]): F[Reply]
  def replyEncoder: Encoder[Reply, Code]
}
