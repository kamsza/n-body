package message

import divided.ClusterActorDescriptor

case class ConnectionManagerInitialize(clusters: Set[ClusterActorDescriptor])
