package akka.persistence.tagless.example.data

import akka.persistence.tagless.example.data.Booking.{BookingStatus, ClientId, ConcertId, Seat}
import cats.data.NonEmptyList

case class Booking(
    clientId: ClientId,
    concertId: ConcertId,
    seats: NonEmptyList[Seat],
    status: BookingStatus
)

object Booking {
  case class ClientId(id: String)
  case class ConcertId(id: String)
  case class Seat(number: Int)
  sealed trait BookingStatus
  object BookingStatus {
    object Pending extends BookingStatus
    object Paid extends BookingStatus
    object None extends BookingStatus
  }
}
