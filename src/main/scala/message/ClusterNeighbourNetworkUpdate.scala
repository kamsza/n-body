package message

import math.Vec2

case class ClusterNeighbourNetworkUpdate(id: String, position: Vec2, neighbours: Set[String])