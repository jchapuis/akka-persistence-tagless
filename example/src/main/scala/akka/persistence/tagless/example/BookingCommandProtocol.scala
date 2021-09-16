package akka.persistence.tagless.example

import akka.persistence.tagless.\/
import akka.persistence.tagless.circe.{CirceCommandProtocol, CirceIncomingCommand}
import akka.persistence.tagless.core.typeclass.protocol.{
  Command,
  Decoder,
  IncomingCommand,
  OutgoingCommand
}
import akka.persistence.tagless.example.algebra.BookingAlg
import akka.persistence.tagless.example.algebra.BookingAlg.BookingAlreadyExists
import akka.persistence.tagless.example.data.Booking.{BookingStatus, ClientId, ConcertId, Seat}
import akka.persistence.tagless.example.data.BookingCommand
import cats.data.NonEmptyList
import io.circe.generic.auto._
import io.circe.{Decoder, Json}

class BookingCommandProtocol[F[_]] extends CirceCommandProtocol[BookingAlg] {
  override def client: BookingAlg[OutgoingCommand[Json, *]] =
    new BookingAlg[OutgoingCommand[Json, *]] {
      def place(
          clientId: ClientId,
          concertId: ConcertId,
          seats: NonEmptyList[Seat]
      ): OutgoingCommand[Json, BookingAlreadyExists \/ Unit] =
        outgoingCommand(BookingCommand.PlaceBooking(clientId, concertId, seats))

      def status: OutgoingCommand[Json, BookingStatus] = outgoingCommand(())
    }

  override def server: Decoder[Json, IncomingCommand[BookingAlg, Json]] =
    implicitly[io.circe.Decoder[BookingCommand]].map {
      case BookingCommand.PlaceBooking(clientId, concertId, seats) =>
        CirceIncomingCommand[BookingAlg, BookingAlreadyExists \/ Unit](
          new Command[BookingAlg, BookingAlreadyExists \/ Unit] {
            def run[G[_]](alg: BookingAlg[G]): G[BookingAlreadyExists \/ Unit] =
              alg.place(clientId, concertId, seats)
          }
        )
      case BookingCommand.Status =>
        CirceIncomingCommand[BookingAlg, BookingStatus](new Command[BookingAlg, BookingStatus] {
          def run[G[_]](alg: BookingAlg[G]): G[BookingStatus] = alg.status
        })
    }

}
