package akka.persistence.tagless.core.typeclass.event

trait EventWriter[F[_], E] {
  def write(event: E, other: E*): F[Unit]
}
