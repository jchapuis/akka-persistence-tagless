package akka.persistence.tagless.example.data

import akka.persistence.tagless.example.data.Booking.{LatLon, BookingID}

sealed trait BookingEvent

object BookingEvent {
  final case class BookingPlaced(
      rideID: BookingID,
      origin: LatLon,
      destination: LatLon,
      passengerCount: Int
  ) extends BookingEvent
}
