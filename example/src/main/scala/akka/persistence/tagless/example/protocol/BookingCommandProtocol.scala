package akka.persistence.tagless.example.protocol

import akka.persistence.tagless.\/
import akka.persistence.tagless.circe.{CirceCommandProtocol, CirceDecoder}
import akka.persistence.tagless.core.typeclass.protocol.{Decoder, IncomingCommand, OutgoingCommand}
import akka.persistence.tagless.example.algebra.BookingAlg
import akka.persistence.tagless.example.algebra.BookingAlg.{BookingAlreadyExists, BookingUnknown}
import akka.persistence.tagless.example.data.Booking.{BookingID, BookingStatus, LatLon}
import BookingCommand._
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
          PlaceBooking(bookingID, passengerCount, origin, destination)
        )

      def status: OutgoingCommand[Json, BookingStatus] =
        outgoingCommand[BookingCommand, BookingStatus](BookingCommand.Status)

      def cancel: OutgoingCommand[Json, BookingUnknown.type \/ Unit] =
        outgoingCommand[BookingCommand, BookingUnknown.type \/ Unit](
          Cancel
        )

      def changeOrigin(
          newOrigin: LatLon
      ): OutgoingCommand[Json, BookingUnknown.type \/ Unit] =
        outgoingCommand[BookingCommand, BookingUnknown.type \/ Unit](
          ChangeOrigin(newOrigin)
        )

      def changeDestination(
          newDestination: LatLon
      ): OutgoingCommand[Json, BookingUnknown.type \/ Unit] =
        outgoingCommand[BookingCommand, BookingUnknown.type \/ Unit](
          ChangeDestination(newDestination)
        )

      def changeOriginAndDestination(
          newOrigin: LatLon,
          newDestination: LatLon
      ): OutgoingCommand[Json, BookingUnknown.type \/ Unit] =
        outgoingCommand[BookingCommand, BookingUnknown.type \/ Unit](
          ChangeOriginAndDestination(newOrigin, newDestination)
        )
    }

  override def server[F[_]]: Decoder[Json, IncomingCommand[F, BookingAlg, Json]] =
    CirceDecoder(io.circe.Decoder[BookingCommand].map {
      case PlaceBooking(
            rideID: BookingID,
            passengerCount: Int,
            origin: LatLon,
            destination: LatLon
          ) =>
        incomingCommand[F, BookingAlreadyExists \/ Unit](
          _.place(rideID, passengerCount, origin, destination)
        )
      case Status => incomingCommand[F, BookingStatus](_.status)
      case Cancel => incomingCommand[F, BookingUnknown.type \/ Unit](_.cancel)
      case ChangeOrigin(newOrigin) =>
        incomingCommand[F, BookingUnknown.type \/ Unit](_.changeOrigin(newOrigin))
      case ChangeDestination(newDestination) =>
        incomingCommand[F, BookingUnknown.type \/ Unit](_.changeDestination(newDestination))
      case ChangeOriginAndDestination(newOrigin, newDestination) =>
        incomingCommand[F, BookingUnknown.type \/ Unit](
          _.changeOriginAndDestination(newOrigin, newDestination)
        )
    })
}
