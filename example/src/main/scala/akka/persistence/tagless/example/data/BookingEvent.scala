package akka.persistence.tagless.example.data

import akka.persistence.tagless.example.data.Booking.{ClientId, ConcertId, Seat}
import cats.data.NonEmptyList

sealed trait BookingEvent

object BookingEvent {
  case class BookingPlaced(
      clientID: ClientId,
      concertId: ConcertId,
      seats: NonEmptyList[Seat]
  ) extends BookingEvent
}
