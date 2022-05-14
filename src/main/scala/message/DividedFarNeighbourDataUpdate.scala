package message

import common.ActorDescriptor
import divided.ClusterDescriptor

case class DividedFarNeighbourDataUpdate(sender:  ActorDescriptor, clusters: Set[ClusterDescriptor])
