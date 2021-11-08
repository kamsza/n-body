package message

import akka.actor.ActorRef

case class SimulationInit(bodies: List[ActorRef])
