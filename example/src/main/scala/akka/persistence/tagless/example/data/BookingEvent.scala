package akka.persistence.tagless.example.data

import akka.persistence.tagless.example.data.Booking.{LatLon, BookingID}

sealed trait BookingEvent

object BookingEvent {
  final case class BookingPlaced(
      bookingID: BookingID,
      origin: LatLon,
      destination: LatLon,
      passengerCount: Int
  ) extends BookingEvent
  final object BookingCancelled extends BookingEvent
  final case class OriginChanged(newOrigin: LatLon) extends BookingEvent
  final case class DestinationChanged(newDestination: LatLon) extends BookingEvent
}
