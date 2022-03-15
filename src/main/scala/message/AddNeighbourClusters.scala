package message

import akka.actor.ActorRef

case class AddNeighbourClusters(clusters: Set[ActorRef])
