package akka.persistence.tagless.core.typeclass.protocol

trait Encoder[-A, Code] {
  def encode(a: A): Code
}
