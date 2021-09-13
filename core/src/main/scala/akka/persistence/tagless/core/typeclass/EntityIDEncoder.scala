package akka.persistence.tagless.core.typeclass

trait EntityIDEncoder[-ID] extends (ID => String)
