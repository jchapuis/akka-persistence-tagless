package akka.persistence.tagless.core.typeclass.protocol

trait IncomingCommand[Alg[_[_]], Code] {
  type Reply
  def run[F[_]](alg: Alg[F]): F[Reply]
  def replyEncoder: Encoder[Reply, Code]
}
