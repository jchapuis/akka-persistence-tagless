package akka.persistence.tagless.core.interpret
import akka.persistence.tagless.core.data.{EventsFolder, Folded}
import akka.persistence.tagless.core.typeclass.entity.Repository
import akka.persistence.tagless.core.typeclass.event.EventApplier
import akka.persistence.tagless.core.typeclass.protocol.{
  CommandProtocol,
  CommandRouter,
  IncomingCommand
}
import cats.tagless.FunctorK
import cats.tagless.implicits._

final class RepositoryT[F[_], S, E, ID, Code, Alg[_[_]]: FunctorK](implicit
    entity: Alg[EntityT[F, S, E, *]],
    commandProtocol: CommandProtocol[Alg, Code],
    commandRouter: CommandRouter[F, ID],
    eventApplier: EventApplier[S, E]
) extends Repository[F, ID, Alg] {
  def entityFor(id: ID): Alg[F] = commandProtocol.client.mapK(commandRouter.routerForID(id))

  def runCommand(
      state: S,
      command: IncomingCommand[EntityT[F, S, E, *], Alg, _]
  ): F[Folded[E, command.Reply]] =
    command.runWith(entity).run(EventsFolder(state, eventApplier))
}

object RepositoryT {
  implicit def apply[F[_], S, E, ID, Code, Alg[_[_]]: FunctorK](implicit
      entity: Alg[EntityT[F, S, E, *]],
      commandProtocol: CommandProtocol[Alg, Code],
      commandRouter: CommandRouter[F, ID],
      applier: EventApplier[S, E]
  ): RepositoryT[F, S, E, ID, Code, Alg] = new RepositoryT
}
