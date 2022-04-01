package message

import divided.ClusterDescriptor

case class DividedDataUpdate(clusters: Set[ClusterDescriptor])
