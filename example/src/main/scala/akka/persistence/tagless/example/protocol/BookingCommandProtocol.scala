package akka.persistence.tagless.example.protocol

import akka.persistence.tagless.\/
import akka.persistence.tagless.circe.{CirceCommandProtocol, CirceDecoder}
import akka.persistence.tagless.core.typeclass.protocol.{Decoder, IncomingCommand, OutgoingCommand}
import akka.persistence.tagless.example.algebra.BookingAlg
import akka.persistence.tagless.example.algebra.BookingAlg.BookingAlreadyExists
import akka.persistence.tagless.example.data.Booking.{BookingStatus, LatLon, BookingID}
import io.circe.Json
import io.circe.generic.auto._

class BookingCommandProtocol extends CirceCommandProtocol[BookingAlg] {
  override def client: BookingAlg[OutgoingCommand[Json, *]] =
    new BookingAlg[OutgoingCommand[Json, *]] {
      def place(
          bookingID: BookingID,
          passengerCount: Int,
          origin: LatLon,
          destination: LatLon
      ): OutgoingCommand[Json, BookingAlreadyExists \/ Unit] =
        outgoingCommand[BookingCommand, BookingAlreadyExists \/ Unit](
          BookingCommand.PlaceBooking(bookingID, passengerCount, origin, destination)
        )

      def status: OutgoingCommand[Json, BookingStatus] =
        outgoingCommand[BookingCommand.Status.type, BookingStatus](BookingCommand.Status)
    }

  override def server[F[_]]: Decoder[Json, IncomingCommand[F, BookingAlg, Json]] =
    CirceDecoder(io.circe.Decoder[BookingCommand].map {
      case BookingCommand.PlaceBooking(
            rideID: BookingID,
            passengerCount: Int,
            origin: LatLon,
            destination: LatLon
          ) =>
        incomingCommand[F, BookingAlreadyExists \/ Unit](
          _.place(rideID, passengerCount, origin, destination)
        )
      case BookingCommand.Status => incomingCommand[F, BookingStatus](_.status)
    })
}
