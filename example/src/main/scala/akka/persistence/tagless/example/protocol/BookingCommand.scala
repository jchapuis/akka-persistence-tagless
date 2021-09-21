package akka.persistence.tagless.example.protocol

import akka.persistence.tagless.example.data.Booking.{LatLon, BookingID}

sealed trait BookingCommand

object BookingCommand {
  final case class PlaceBooking(
      bookingID: BookingID,
      passengerCount: Int,
      origin: LatLon,
      destination: LatLon
  ) extends BookingCommand
  final case object Status extends BookingCommand
}
