package `object`

import math.Vec2

trait Object {
  val id: String
  val mass: BigDecimal
  var position: Vec2

  override def equals(obj: Any): Boolean = {
    obj match {
      case other: Object => id == other.id
      case _ => false
    }
  }
}