package message

import akka.actor.ActorRef

case class SimulationStart(managingActor: ActorRef)
