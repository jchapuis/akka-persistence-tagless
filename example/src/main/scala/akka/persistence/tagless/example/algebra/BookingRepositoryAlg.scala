package akka.persistence.tagless.example.algebra

import akka.persistence.tagless.example.data.Booking.BookingID

trait BookingRepositoryAlg[F[_]] {
  def bookingFor(clientId: BookingID): BookingAlg[F]
}
