package akka.persistence.tagless.example.logic

import akka.persistence.tagless.\/
import akka.persistence.tagless.core.typeclass.event.EventApplier
import akka.persistence.tagless.example.data.Booking.BookingStatus
import akka.persistence.tagless.example.data.{Booking, BookingEvent}
import cats.syntax.either._

class BookingEventApplier extends EventApplier[Option[Booking], BookingEvent] {
  def apply(state: Option[Booking], event: BookingEvent): String \/ Option[Booking] =
    event match {
      case BookingEvent.BookingPlaced(bookingID, origin, destination, passengerCount) =>
        Option(
          Booking(bookingID, origin, destination, passengerCount, BookingStatus.Pending)
        ).asRight
      case BookingEvent.BookingCancelled =>
        state
          .toRight("Attempt to cancel unknown booking")
          .map(_.copy(status = BookingStatus.Canceled))
          .map(Some(_))
      case BookingEvent.OriginChanged(newOrigin) =>
        state
          .toRight("Attempt to change unknown booking")
          .map(_.copy(origin = newOrigin, status = BookingStatus.Pending))
          .map(Some(_))
      case BookingEvent.DestinationChanged(newDestination) =>
        state
          .toRight("Attempt to change unknown booking")
          .map(_.copy(origin = newDestination, status = BookingStatus.Pending))
          .map(Some(_))
    }
}
