package divided

import `object`.Object
import math.Vec2

case class ClusterDescriptor(id: String,
                             mass: Double,
                             var position: Vec2,
                             var timestamp: Int) extends Object
