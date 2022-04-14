package message

import common.ActorDescriptor

case class SimulationStart(clusters: Set[ActorDescriptor])
