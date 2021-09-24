package akka.persistence.tagless.example.logic

import akka.persistence.tagless.\/
import akka.persistence.tagless.core.typeclass.entity.Entity
import akka.persistence.tagless.example.algebra.BookingAlg
import akka.persistence.tagless.example.algebra.BookingAlg.BookingAlreadyExists
import akka.persistence.tagless.example.data.Booking._
import akka.persistence.tagless.example.data.{Booking, BookingEvent}
import cats.Monad
import cats.syntax.applicative._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.conversions.all._

final case class BookingEntity[F[_]: Monad](entity: Entity[F, Option[Booking], BookingEvent])
    extends BookingAlg[F] {
  import entity._

  def place(
      bookingID: BookingID,
      passengerCount: Int,
      origin: LatLon,
      destination: LatLon
  ): F[BookingAlreadyExists \/ Unit] =
    read >>= {
      case Some(_) => BookingAlreadyExists(bookingID).asLeft.pure
      case None =>
        write(BookingEvent.BookingPlaced(bookingID, origin, destination, passengerCount))
          .map(_.asRight)
    }

  def status: F[BookingStatus] = read >>= {
    case Some(booking) => booking.status.pure
    case None          => BookingStatus.Unknown.pure[F]
  }
}
