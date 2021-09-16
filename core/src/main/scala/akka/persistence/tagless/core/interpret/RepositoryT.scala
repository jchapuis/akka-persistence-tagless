package akka.persistence.tagless.core.interpret
import akka.persistence.tagless.core.data.{EventsFolder, Folded}
import akka.persistence.tagless.core.typeclass._
import akka.persistence.tagless.core.typeclass.entity.Repository
import akka.persistence.tagless.core.typeclass.event.EventApplier
import akka.persistence.tagless.core.typeclass.protocol.{Command, CommandProtocol, CommandRouter}
import cats.tagless.FunctorK
import cats.tagless.implicits._

final class RepositoryT[F[_], S, E, ID, Code, Alg[_[_]]: FunctorK](implicit
    commandProcessor: Alg[EntityT[F, S, E, *]],
    commandProtocol: CommandProtocol[Alg, Code],
    commandRouter: CommandRouter[F, ID],
    eventApplier: EventApplier[S, E]
) extends Repository[F, ID, Alg] {
  def entityFor(id: ID): Alg[F] = commandProtocol.client.mapK(commandRouter.routerForID(id))

  def runCommand[A](state: S, command: Command[Alg, A]): F[Folded[E, A]] =
    command.run(commandProcessor).run(EventsFolder(state, eventApplier))
}

object RepositoryT {
  implicit def instance[F[_], S, E, ID, Code, Alg[_[_]]: FunctorK](implicit
      commandProcessor: Alg[EntityT[F, S, E, *]],
      commandProtocol: CommandProtocol[Alg, Code],
      commandRouter: CommandRouter[F, ID],
      applier: EventApplier[S, E]
  ): RepositoryT[F, S, E, ID, Code, Alg] = new RepositoryT
}
