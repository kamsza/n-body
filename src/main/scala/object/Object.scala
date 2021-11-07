package `object`

import utils.Vec2

trait Object {
  def id: String
  def mass: BigDecimal
  def position: Vec2
}

//object Object {
//  def apply(id: String, mass: BigDecimal, position: Vec2): Object = new Object(id, mass, position)
//
//  def apply(id: String): Object = new Object(id, BigDecimal("0"), Vec2(BigDecimal("0"), BigDecimal("0")))
//}