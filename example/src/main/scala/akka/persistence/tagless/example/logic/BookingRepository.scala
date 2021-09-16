package akka.persistence.tagless.example.logic

import akka.persistence.tagless.core.typeclass.entity.Repository
import akka.persistence.tagless.example.algebra.{BookingAlg, BookingRepositoryAlg}
import akka.persistence.tagless.example.data.Booking.ClientId
import cats.Monad

class BookingRepository[F[_]: Monad](implicit repository: Repository[F, ClientId, BookingAlg])
    extends BookingRepositoryAlg[F] {
  import repository._
  def bookingFor(clientId: ClientId): BookingAlg[F] = entityFor(clientId)
}
