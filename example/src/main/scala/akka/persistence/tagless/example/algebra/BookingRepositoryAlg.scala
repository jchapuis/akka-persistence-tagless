package akka.persistence.tagless.example.algebra

import akka.persistence.tagless.example.data.Booking.ClientId

trait BookingRepositoryAlg[F[_]] {
  def bookingFor(clientId: ClientId): BookingAlg[F]
}
