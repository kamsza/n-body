package `object`

import utils.{Constants, Vec2}

class Body(
    val id: Int,
    val mass: BigDecimal,
    var position: Vec2,
    var velocity: Vec2
) {

  var acceleration: Vec2 = Vec2(BigDecimal("0"), BigDecimal("0"))

  def applyForce(body: Body): Unit = {
    if(id != body.id)
      acceleration += countAcceleration(body)
  }

  def countAcceleration(otherBody: Body): Vec2 = {
    val distance = this.position.distance(otherBody.position)
    val a = (Constants.G * otherBody.mass) / (distance * distance)
    val unitDirection = (otherBody.position - this.position) / distance

    unitDirection * a
  }

  def move(): Unit = {
    this.position += this.velocity * Constants.dt + acceleration * math.pow(Constants.dt, 2) / 2
    this.velocity += acceleration * Constants.dt
    acceleration = Vec2(BigDecimal("0"), BigDecimal("0"))
  }

  def changePosition(changeVec: Vec2): Unit = this.position += changeVec

  override def toString: String = f"$mass%30.2f  |   $position   |   $velocity"

  def toTuple: (Int, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal) =
    Tuple6(id, mass, position.x, position.y, velocity.x, velocity.y)
}
