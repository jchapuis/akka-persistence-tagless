package akka.persistence.tagless.runtime.data

import akka.actor.typed.ActorRef

final case class Command[Code](id: String, payload: Code)(val replyTo: ActorRef[Reply[Code]])
