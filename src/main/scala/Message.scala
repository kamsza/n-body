import akka.actor.ActorRef

case class Data(id: Int, msgId: Int, mass: Double, position: Vector, velocity: Vector) {
  override def equals(that: Any): Boolean = {
    that match {
      case d: Data => id.equals(d.id)
      case _ => false
    }
  }
}

case class StartSimulation(replyTo: ActorRef = ActorRef.noSender) {
  override def equals(that: Any): Boolean = false;
}
