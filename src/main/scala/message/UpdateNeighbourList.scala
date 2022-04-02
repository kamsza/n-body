package message

import clustered_common.ActorDescriptor

case class UpdateNeighbourList(newNeighbours: Set[ActorDescriptor], farNeighbours: Set[ActorDescriptor])
