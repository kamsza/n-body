package divided

import `object`.Object
import math.Vec2

case class ClusterDescriptor(id: String,
                             mass: BigDecimal,
                             var position: Vec2,
                             var timestamp: Int) extends Object
