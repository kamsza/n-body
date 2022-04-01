package message

import clustered_common.ActorDescriptor

case class AddNeighbourClusters(clusters: Set[ActorDescriptor])
