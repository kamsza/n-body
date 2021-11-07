package message

import akka.actor.ActorRef

case class AddNeighbourClusters(clusters: List[ActorRef], simulationController: ActorRef)
