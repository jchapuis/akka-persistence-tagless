package akka.persistence.tagless.example

import akka.persistence.tagless.\/
import akka.persistence.tagless.core.typeclass._
import akka.persistence.tagless.example.algebra.BookingAlg
import akka.persistence.tagless.example.algebra.BookingAlg.BookingAlreadyExists
import akka.persistence.tagless.example.data.Booking.{BookingStatus, ClientId, ConcertId, Seat}
import cats.data.NonEmptyList

class BookingCommandProtocol[F[_]] extends CommandProtocol[BookingAlg, Json] {
  def server: Decoder[Json, CommandMessage[BookingAlg, Json]] = {
    case "place" => new CommandMessage[BookingAlg, Json] {
      type Reply = BookingAlreadyExists \/ Unit

      def command: Command[BookingAlg, BookingAlreadyExists \/ Unit] = new Command[BookingAlg, BookingAlreadyExists \/ Unit] {
        def run[G[_]](alg: BookingAlg[G]): G[BookingAlreadyExists \/ Unit] = alg.place( //decoded params here)
      }

      def replyEncoder: Encoder[BookingAlreadyExists \/ Unit, Json] = ???
    }
  }

  def client: BookingAlg[Encoder[Json, *]] = new BookingAlg[Encoder[Json, *]] {
    def place(
        clientId: ClientId,
        concertId: ConcertId,
        seats: NonEmptyList[Seat]
    ): Encoder[Json, BookingAlreadyExists \/ Unit] =
      ???

    def status: Encoder[Json, BookingStatus] = ???
  }
}
// a la endpoints ?
//trait BookingRepository:
//  def place : Command[(ClientId, ConcertId, NonEmptyList[Seat]), Either[PlaceError, Unit]] = command()
//  def status: Command[Unit, Either[StatusError, Status]] = command()
//

// wrap response types into some [Wire[A]]
// form tuples from repository algebra
// entity is a traverse operation on commands, each generating events

//trait class EntityT[F[_], S, E, A] private (
//                                             val unsafeRun: (S, (S, E) => Folded[S], Chain[E]) => F[Folded[(Chain[E], A)]]
//                                           ) extends AnyVal {
//
//  def run(current: S, update: (S, E) => Folded[S]): F[Folded[(Chain[E], A)]] =
//    unsafeRun(current, update, Chain.empty)
