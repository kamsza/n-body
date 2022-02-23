package message

import akka.actor.ActorRef

case class AddNeighbourBodies(bodies: List[ActorRef], simulationController: ActorRef)
