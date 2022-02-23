package `object`

import utils.Vec2

trait Object {
  def id: String
  def mass: BigDecimal
  def position: Vec2

  override def equals(obj: Any): Boolean = {
    obj match {
      case other: Object => id == other.id
      case _ => false
    }
  }
}