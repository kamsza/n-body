package message

import akka.actor.ActorRef

case class SimulationStart(clusters: List[ActorRef])
