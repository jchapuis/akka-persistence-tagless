package akka.persistence.tagless.core.typeclass.protocol

import cats.~>

trait CommandRouter[F[_], ID] {
  def routerForID[Code](id: ID): OutgoingCommand[Code, *] ~> F
}
