package message

import common.ActorDescriptor
import divided.ClusterDescriptor

case class DividedNewNeighbourDataUpdate(sender:  ActorDescriptor, clusters: Set[ClusterDescriptor])
