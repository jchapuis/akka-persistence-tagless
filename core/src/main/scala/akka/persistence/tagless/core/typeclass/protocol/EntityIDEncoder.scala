package akka.persistence.tagless.core.typeclass.protocol

trait EntityIDEncoder[-ID] extends (ID => String)
