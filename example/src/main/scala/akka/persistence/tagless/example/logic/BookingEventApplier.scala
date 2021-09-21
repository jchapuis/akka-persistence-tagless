package akka.persistence.tagless.example.logic

import akka.persistence.tagless.\/
import akka.persistence.tagless.core.typeclass.event.EventApplier
import akka.persistence.tagless.example.data.{Booking, BookingEvent}
import cats.syntax.either._

class BookingEventApplier extends EventApplier[Option[Booking], BookingEvent] {
  def apply(state: Option[Booking], event: BookingEvent): String \/ Option[Booking] =
    event match {
      case BookingEvent.BookingPlaced(rideID, origin, destination, passengerCount) =>
        Option(
          Booking(rideID, origin, destination, passengerCount, Booking.BookingStatus.Pending)
        ).asRight
    }
}
