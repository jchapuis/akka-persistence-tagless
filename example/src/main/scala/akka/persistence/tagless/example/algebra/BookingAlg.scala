package akka.persistence.tagless.example.algebra

import akka.persistence.tagless.\/
import akka.persistence.tagless.example.algebra.BookingAlg.BookingAlreadyExists
import akka.persistence.tagless.example.data.Booking.{BookingID, BookingStatus, LatLon}
import cats.tagless.{Derive, FunctorK}

trait BookingAlg[F[_]] {
  def place(
      bookingID: BookingID,
      passengerCount: Int,
      origin: LatLon,
      destination: LatLon
  ): F[BookingAlreadyExists \/ Unit]
  def status: F[BookingStatus]
}

object BookingAlg {
  final case class BookingAlreadyExists(rideID: BookingID)

  implicit def functorKInstance: FunctorK[BookingAlg] =
    Derive.functorK[BookingAlg]
}
