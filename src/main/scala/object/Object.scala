package `object`

import utils.Vec2

class Object (
  val id: String,
  val mass: BigDecimal,
  var position: Vec2
) {}

object Object {
  def apply(id: String, mass: BigDecimal, position: Vec2): Object = new Object(id, mass, position)
}