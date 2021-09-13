package akka.persistence.tagless.runtime.data

final case class Reply[Code](payload: Code)
