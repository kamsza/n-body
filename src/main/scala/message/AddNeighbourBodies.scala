package message

import akka.actor.ActorRef

case class AddNeighbourBodies(bodies: Set[ActorRef], simulationController: ActorRef)
