package divided

import akka.actor.ActorRef
import math.Vec2

case class ClusterActorDescriptor(id: String,
                                  position: Vec2,
                                  actorRef: ActorRef,
                                 ) {

  override def equals(obj: Any): Boolean = {
    obj match {
      case other: ClusterActorDescriptor => id == other.id
      case _ => false
    }
  }
}
