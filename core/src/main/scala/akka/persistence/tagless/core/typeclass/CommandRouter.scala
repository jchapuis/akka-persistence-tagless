package akka.persistence.tagless.core.typeclass

import cats.~>

trait CommandRouter[F[_], ID] {
  def routerForID[Code](id: ID): Encoded[Code, *] ~> F
}
