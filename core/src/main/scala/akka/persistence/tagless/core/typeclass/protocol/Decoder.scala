package akka.persistence.tagless.core.typeclass.protocol

trait Decoder[Code, +A] {
  def decode(payload: Code): A
}
