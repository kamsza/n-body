package message

import common.ActorDescriptor

case class UpdateNeighbourList(newNeighbours: Set[ActorDescriptor], farNeighbours: Set[ActorDescriptor])
