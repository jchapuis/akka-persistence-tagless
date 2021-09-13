package akka.persistence.tagless.example.algebra

import akka.persistence.tagless.\/
import akka.persistence.tagless.example.data.Booking.{BookingStatus, ClientId, ConcertId, Seat}
import akka.persistence.tagless.example.algebra.BookingAlg.BookingAlreadyExists
import cats.data.NonEmptyList

trait BookingAlg[F[_]] {
  def place(
      clientId: ClientId,
      concertId: ConcertId,
      seats: NonEmptyList[Seat]
  ): F[BookingAlreadyExists \/ Unit]
  def status: F[BookingStatus]
}

object BookingAlg {
  case class BookingAlreadyExists(clientId: ClientId, concertId: ConcertId)
}
