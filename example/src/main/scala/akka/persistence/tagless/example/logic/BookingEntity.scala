package akka.persistence.tagless.example.logic

import akka.persistence.tagless.\/
import akka.persistence.tagless.core.typeclass.Entity
import akka.persistence.tagless.example.algebra.BookingAlg
import akka.persistence.tagless.example.algebra.BookingAlg.BookingAlreadyExists
import akka.persistence.tagless.example.data.Booking._
import akka.persistence.tagless.example.data.{Booking, BookingEvent}
import cats.Monad
import cats.data.NonEmptyList
import cats.syntax.applicative._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._

class BookingEntity[F[_]: Monad](implicit entity: Entity[F, Option[Booking], BookingEvent])
    extends BookingAlg[F] {
  import entity._

  def place(
      clientId: ClientId,
      concertId: ConcertId,
      seats: NonEmptyList[Seat]
  ): F[BookingAlreadyExists \/ Unit] =
    read >>= {
      case Some(_) =>
        write(BookingEvent.BookingPlaced(clientId, concertId, seats)).map(_.asRight)
      case None => BookingAlreadyExists(clientId, concertId).asLeft.pure
    }

  def status: F[BookingStatus] = read >>= {
    case Some(booking) => booking.status.pure
    case None          => BookingStatus.None.pure[F]
  }
}
