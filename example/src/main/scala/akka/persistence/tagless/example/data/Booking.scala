package akka.persistence.tagless.example.data

import akka.persistence.tagless.example.data.Booking._
import cats.Eq

import java.util.UUID

final case class Booking(
    id: BookingID,
    origin: LatLon,
    destination: LatLon,
    passengerCount: Int
)

object Booking {
  final case class BookingID(id: UUID) extends AnyVal
  final case class LatLon(lat: Double, lon: Double)
  object LatLon {
    implicit val eq: Eq[LatLon] = Eq.fromUniversalEquals
  }
}
