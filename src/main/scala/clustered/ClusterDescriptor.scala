package clustered

import `object`.Object
import math.Vec2

case class ClusterDescriptor(id: String,
                             mass: Double,
                             var position: Vec2) extends Object
