package akka.persistence.tagless.example.algebra

import akka.persistence.tagless.\/
import akka.persistence.tagless.example.algebra.BookingAlg.{BookingAlreadyExists, BookingUnknown}
import akka.persistence.tagless.example.data.Booking
import akka.persistence.tagless.example.data.Booking.{BookingID, LatLon}
import cats.tagless.{Derive, FunctorK}

trait BookingAlg[F[_]] {
  def place(
      bookingID: BookingID,
      passengerCount: Int,
      origin: LatLon,
      destination: LatLon
  ): F[BookingAlreadyExists \/ Unit]
  def get: F[BookingUnknown.type \/ Booking]
  def changeOrigin(newOrigin: LatLon): F[BookingUnknown.type \/ Unit]
  def changeDestination(newDestination: LatLon): F[BookingUnknown.type \/ Unit]
  def changeOriginAndDestination(
      newOrigin: LatLon,
      newDestination: LatLon
  ): F[BookingUnknown.type \/ Unit]
}

object BookingAlg {
  final case class BookingAlreadyExists(bookingID: BookingID)
  case object BookingUnknown

  implicit def functorKInstance: FunctorK[BookingAlg] =
    Derive.functorK[BookingAlg]
}
