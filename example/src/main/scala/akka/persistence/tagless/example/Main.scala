package akka.persistence.tagless.example

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.persistence.tagless.core.interpret.{EntityLift, EntityT, RepositoryT}
import akka.persistence.tagless.core.typeclass.entity.EntityNameProvider
import akka.persistence.tagless.core.typeclass.protocol.EntityIDEncoder
import akka.persistence.tagless.example.data.Booking.{BookingID, LatLon}
import akka.persistence.tagless.example.logic.{
  BookingEntity,
  BookingEventApplier,
  BookingRepository
}
import akka.persistence.tagless.example.protocol.BookingCommandProtocol
import akka.util.Timeout
import cats.effect._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import akka.persistence.tagless.runtime.ShardingCommandRouter._
import akka.persistence.tagless.core.interpret.RepositoryT._
import akka.persistence.tagless.core.interpret.EntityT._
import akka.persistence.tagless.example.algebra.BookingAlg
import akka.persistence.tagless.example.data.{Booking, BookingEvent}
import io.circe.Json

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main extends IOApp {
  implicit val actorSystem: ActorSystem[Nothing] =
    ActorSystem.wrap(akka.actor.ActorSystem("bookings-as"))
  implicit val clusterSharding: ClusterSharding = ClusterSharding(actorSystem)
  implicit val commandProtocol: BookingCommandProtocol = new BookingCommandProtocol
  implicit val eventApplier: BookingEventApplier = new BookingEventApplier
  implicit val foo: EntityLift[EntityT[IO, Option[Booking], BookingEvent, *], IO, Option[
    Booking
  ], BookingEvent] = EntityT.instance[IO, Option[Booking], BookingEvent]
  implicit val bookingEntity: BookingEntity[IO] =
    new BookingEntity[IO]
  implicit val bookingEntityNameProvider: EntityNameProvider[BookingID] = () => "booking"
  implicit val idEncoder: EntityIDEncoder[BookingID] = _.id.toString
  implicit val askTimeout: Timeout = Timeout(10.seconds)
  val repositoryT =
    RepositoryT.instance[IO, Option[Booking], BookingEvent, BookingID, Json, BookingAlg]
  val bookingRepository = new BookingRepository[IO]()
  val httpService = HttpRoutes
    .of[IO] {
      case req @ POST -> Root / "booking" =>
        for {
          bookingRequest <- req.as[BookingRequest]
          bookingID <- IO(UUID.randomUUID()).map(BookingID)
          result <- bookingRepository
            .bookingFor(bookingID)
            .place(
              bookingID,
              bookingRequest.passengerCount,
              bookingRequest.origin,
              bookingRequest.destination
            )
            .flatMap {
              case Left(alreadyExists) => BadRequest(alreadyExists)
              case Right(_)            => Accepted()
            }
        } yield result
      case GET -> Root / "booking" / UUIDVar(bookingID) / "status" =>
        bookingRepository.bookingFor(BookingID(bookingID)).status.flatMap(Ok(_))
    }
    .orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(httpService)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)

  final case class BookingRequest(passengerCount: Int, origin: LatLon, destination: LatLon)
}
