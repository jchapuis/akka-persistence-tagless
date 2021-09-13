package akka.persistence.tagless.core.typeclass

trait EntityNameProvider[ID] {
  def name: String
}
