package akka.persistence.tagless.core.typeclass.protocol

trait CommandEncoder[C, Code] {
  def encode(a: C): OutgoingCommand[Code, C]
}
