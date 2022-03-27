package message

import divided.ClusterDescriptor
import math.Vec2

case class DividedDataUpdate(id: String,
                             mass: BigDecimal,
                             var position: Vec2,
                             var timestamp: Int,
                             neighbours: Set[ClusterDescriptor])  // TODO: wywalić masę
