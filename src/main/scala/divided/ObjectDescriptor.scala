package divided

import akka.actor.ActorRef
import utils.Vec2

case class ObjectDescriptor(id: String,
                           position: Vec2,
                           actorRef: ActorRef,
                           ) {

  override def equals(obj: Any): Boolean = {
    obj match {
      case other: ObjectDescriptor => id == other.id
      case _ => false
    }
  }
}
