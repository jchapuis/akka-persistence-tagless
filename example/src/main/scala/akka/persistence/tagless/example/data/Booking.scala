package akka.persistence.tagless.example.data

import akka.persistence.tagless.example.data.Booking._

import java.util.UUID

final case class Booking(
    id: BookingID,
    origin: LatLon,
    destination: LatLon,
    passengerCount: Int,
    status: BookingStatus
)

object Booking {
  final case class BookingID(id: UUID) extends AnyVal
  final case class LatLon(lat: Double, lon: Double)
  sealed trait BookingStatus
  object BookingStatus {
    object Pending extends BookingStatus
    object Scheduled extends BookingStatus
    object Canceled extends BookingStatus
    object Unknown extends BookingStatus
  }
}
