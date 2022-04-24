package clustered

import `object`.Object
import math.Vec2

case class ClusterDescriptor(id: String,
                             mass: BigDecimal,
                             _position: Vec2) extends Object {
  override var position: Vec2 = _position
}
