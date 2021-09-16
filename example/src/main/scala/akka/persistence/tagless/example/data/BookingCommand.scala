package akka.persistence.tagless.example.data

import akka.persistence.tagless.example.data.Booking.{ClientId, ConcertId, Seat}
import cats.data.NonEmptyList

sealed trait BookingCommand

object BookingCommand {
  final case class PlaceBooking(clientId: ClientId, concertId: ConcertId, seats: NonEmptyList[Seat])
      extends BookingCommand
  final case object Status extends BookingCommand
}
