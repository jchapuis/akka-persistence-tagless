package akka.persistence.tagless.core.interpret

import akka.persistence.tagless.core.typeclass.entity.Repository

trait RepositoryLift[G[_], F[_], ID, Alg[_[_]]] extends Repository[G, ID, Alg]
