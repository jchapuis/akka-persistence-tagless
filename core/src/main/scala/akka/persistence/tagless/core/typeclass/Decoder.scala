package akka.persistence.tagless.core.typeclass

trait Decoder[Code, A] {
  def decode(payload: Code): A
}
