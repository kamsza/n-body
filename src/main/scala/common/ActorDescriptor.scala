package common

import akka.actor.ActorRef

case class ActorDescriptor(id: String, actorRef: ActorRef) {

  override def equals(obj: Any): Boolean = {
    obj match {
      case other: ActorDescriptor => id == other.id
      case _ => false
    }
  }
}
