package akka.persistence.tagless.example.logic

import akka.persistence.tagless.core.typeclass.entity.Repository
import akka.persistence.tagless.example.algebra.{BookingAlg, BookingRepositoryAlg}
import akka.persistence.tagless.example.data.Booking.BookingID
import cats.Monad

case class BookingRepository[F[_]: Monad](repository: Repository[F, BookingID, BookingAlg])
    extends BookingRepositoryAlg[F] {
  import repository._
  def bookingFor(bookingID: BookingID): BookingAlg[F] = entityFor(bookingID)
}
