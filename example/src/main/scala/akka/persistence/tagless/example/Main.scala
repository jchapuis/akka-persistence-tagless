package akka.persistence.tagless.example

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.persistence.tagless.core.interpret.EntityT._
import akka.persistence.tagless.core.typeclass.entity.EntityNameProvider
import akka.persistence.tagless.core.typeclass.protocol.EntityIDEncoder
import akka.persistence.tagless.example.algebra.{BookingAlg, BookingRepositoryAlg}
import akka.persistence.tagless.example.data.Booking.{BookingID, LatLon}
import akka.persistence.tagless.example.data.{Booking, BookingEvent}
import akka.persistence.tagless.example.logic.{
  BookingEntity,
  BookingEventApplier,
  BookingRepository
}
import akka.persistence.tagless.example.protocol.BookingCommandProtocol
import akka.persistence.tagless.runtime.syntax.deploy._
import akka.persistence.testkit.PersistenceTestKitPlugin
import akka.util.Timeout
import cats.effect._
import com.typesafe.config.ConfigFactory
import io.circe.Json
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import cats.syntax.show._
import cats.syntax.applicative._
import cats.syntax.either._
import java.util.UUID
import scala.concurrent.duration._

object Main extends IOApp {
  private final case class BookingRequest(passengerCount: Int, origin: LatLon, destination: LatLon)
  private final case class BookingPatch(origin: Option[LatLon], destination: Option[LatLon])

  private def httpService(bookingRepository: BookingRepositoryAlg[IO]) = HttpRoutes
    .of[IO] {
      case req @ POST -> Root / "booking" =>
        for {
          bookingRequest <- req.as[BookingRequest]
          bookingID <- IO(UUID.randomUUID()).map(BookingID)
          reply <- bookingRepository
            .bookingFor(bookingID)
            .place(
              bookingID,
              bookingRequest.passengerCount,
              bookingRequest.origin,
              bookingRequest.destination
            )
          result <- reply match {
            case Left(alreadyExists) =>
              BadRequest(show"Booking with ${alreadyExists.bookingID.id} already exists")
            case Right(_) => Accepted(bookingID)
          }
        } yield result
      case GET -> Root / "booking" / UUIDVar(id) / "status" =>
        bookingRepository.bookingFor(BookingID(id)).status.flatMap(Ok(_))
      case POST -> Root / "booking" / UUIDVar(id) / "cancel" =>
        bookingRepository.bookingFor(BookingID(id)).cancel.flatMap(_ => Ok())
      case req @ PATCH -> Root / "booking" / UUIDVar(id) =>
        for {
          bookingPatch <- req.as[BookingPatch]
          bookingID = BookingID(id)
          reply <- (bookingPatch.origin, bookingPatch.destination) match {
            case (Some(newOrigin), Some(newDestination)) =>
              bookingRepository
                .bookingFor(bookingID)
                .changeOriginAndDestination(newOrigin, newDestination)
            case (Some(newOrigin), None) =>
              bookingRepository
                .bookingFor(bookingID)
                .changeOrigin(newOrigin)
            case (None, Some(newDestination)) =>
              bookingRepository.bookingFor(bookingID).changeDestination(newDestination)
            case (None, None) => ().asRight.pure[IO]
          }
          result <- reply match {
            case Left(_) =>
              BadRequest(show"Booking with $id is unknown")
            case Right(_) => Ok()
          }
        } yield result
    }
    .orNotFound

  def run(args: List[String]): IO[ExitCode] = {
    implicit val actorSystem: ActorSystem[Nothing] =
      ActorSystem.wrap(
        akka.actor.ActorSystem(
          "bookings-as",
          PersistenceTestKitPlugin.config.withFallback(ConfigFactory.defaultApplication).resolve()
        )
      )
    implicit val clusterSharding: ClusterSharding = ClusterSharding(actorSystem)
    implicit val commandProtocol: BookingCommandProtocol = new BookingCommandProtocol
    implicit val eventApplier: BookingEventApplier = new BookingEventApplier
    implicit val bookingEntityNameProvider: EntityNameProvider[BookingID] = () => "booking"
    implicit val idEncoder: EntityIDEncoder[BookingID] = _.id.toString
    implicit val askTimeout: Timeout = Timeout(10.seconds)
    deployEntity[IO, Option[
      Booking
    ], BookingEvent, BookingID, Json, BookingAlg, BookingRepositoryAlg](
      BookingEntity(_),
      BookingRepository(_),
      Option.empty[Booking]
    ).map { case (bookingRepository, _) =>
      httpService(bookingRepository)
    }.flatMap(service =>
      BlazeServerBuilder[IO]
        .bindHttp(8080, "localhost")
        .withHttpApp(service)
        .resource
    ).use(_ => IO.fromFuture(IO(actorSystem.whenTerminated)))
      .as(ExitCode.Success)
  }

}
