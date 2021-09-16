package akka.persistence.tagless.example

import akka.persistence.tagless.\/
import akka.persistence.tagless.circe.{CirceCommandProtocol, CirceEncoder, CirceIncomingCommand}
import akka.persistence.tagless.core.typeclass.protocol.{Command, OutgoingCommand}
import akka.persistence.tagless.example.BookingCommandProtocol._
import akka.persistence.tagless.example.algebra.BookingAlg
import akka.persistence.tagless.example.algebra.BookingAlg.BookingAlreadyExists
import akka.persistence.tagless.example.data.Booking.{BookingStatus, ClientId, ConcertId, Seat}
import cats.data.NonEmptyList
import io.circe
import io.circe.Json
import io.circe.generic.auto._

class BookingCommandProtocol[F[_]] extends CirceCommandProtocol[BookingAlg] {
  implicit def incomingCommandDecoder: circe.Decoder[CirceIncomingCommand[BookingAlg]] = ???

  private def placeIncomingCommand(
      clientId: ClientId,
      concertId: ConcertId,
      seats: NonEmptyList[Seat]
  ) = CirceIncomingCommand[BookingAlg, BookingAlreadyExists \/ Unit](
    new Command[BookingAlg, BookingAlreadyExists \/ Unit] {
      def run[G[_]](alg: BookingAlg[G]): G[BookingAlreadyExists \/ Unit] =
        alg.place(clientId, concertId, seats)
    }
  )

  override def client: BookingAlg[OutgoingCommand[Json, *]] =
    new BookingAlg[OutgoingCommand[Json, *]] {
      def place(
          clientId: ClientId,
          concertId: ConcertId,
          seats: NonEmptyList[Seat]
      ): OutgoingCommand[Json, BookingAlreadyExists \/ Unit] =
        outgoingCommand((clientId, concertId, seats))

      def status: OutgoingCommand[Json, BookingStatus] = outgoingCommand(())
    }
}
