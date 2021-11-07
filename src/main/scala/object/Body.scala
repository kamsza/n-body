package `object`

import utils.{Constants, Vec2}

class Body(
            id: String,
            mass: BigDecimal,
            position: Vec2,
            var velocity: Vec2
) extends Object(id, mass, position) {

  var acceleration: Vec2 = Vec2(BigDecimal("0"), BigDecimal("0"))

  def applyForce(otherObject: Object): Unit = {
    if(id != otherObject.id)
      acceleration += countAcceleration(otherObject)
  }

  def countAcceleration(otherObject: Object): Vec2 = {
    val distance = this.position.distance(otherObject.position)
    val a = (Constants.G * otherObject.mass) / (distance * distance)
    val unitDirection = (otherObject.position - this.position) / distance

    unitDirection * a
  }

  def move(): Unit = {
    this.position = this.position + this.velocity * Constants.dt + acceleration * math.pow(Constants.dt, 2) / 2
    this.velocity = this.velocity + acceleration * Constants.dt
    acceleration = Vec2(BigDecimal("0"), BigDecimal("0"))
  }

  def changePosition(changeVec: Vec2): Unit = this.position = this.position + changeVec

  override def toString: String = f"$mass%30.2f  |   $position   |   $velocity"

  def toTuple: (String, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal) =
    Tuple6(id, mass, position.x, position.y, velocity.x, velocity.y)
}
