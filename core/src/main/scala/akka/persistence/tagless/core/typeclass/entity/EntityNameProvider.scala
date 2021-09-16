package akka.persistence.tagless.core.typeclass.entity

trait EntityNameProvider[ID] {
  def name: String
}
