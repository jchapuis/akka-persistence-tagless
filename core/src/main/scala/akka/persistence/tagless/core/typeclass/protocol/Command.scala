package akka.persistence.tagless.core.typeclass.protocol

trait Command[Alg[_[_]], Reply] {
  def run[F[_]](alg: Alg[F]): F[Reply]
}