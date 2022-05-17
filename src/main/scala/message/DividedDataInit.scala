package message

import divided.ClusterDescriptor

case class DividedDataInit(clusters: Set[ClusterDescriptor], messageId: String)
