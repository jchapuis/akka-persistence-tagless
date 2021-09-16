package akka.persistence.tagless.core.typeclass.entity

import akka.persistence.tagless.core.typeclass.event.EventWriter
import cats.Monad

trait Entity[F[_], S, E] extends StateReader[F, S] with EventWriter[F, E] with Monad[F]
