package `object`

import constant.{Constants, SimulationConstants}
import math.Vec2

abstract class AbstractBody(
                             val id: String,
                             val mass: BigDecimal,
                             val startPosition: Vec2,
                             val startVelocity: Vec2,
                           ) extends Object {

  var position: Vec2 = startPosition

  var velocity: Vec2 = startVelocity

  var acceleration: Vec2 = Vec2(BigDecimal("0"), BigDecimal("0"))

  var bodiesMassDistanceDiffSum: Vec2 = Vec2(BigDecimal("0"), BigDecimal("0"))

  def applyForce(otherObject: Object): Unit = {
    if (id != otherObject.id)
      applyForce(otherObject.mass, otherObject.position)
  }

  def applyForce(mass: BigDecimal, position: Vec2): Unit = {
      bodiesMassDistanceDiffSum += countMassDistanceDiffVec(mass, position)
  }

  def countMassDistanceDiffVec(mass: BigDecimal, position: Vec2): Vec2 = {
    val distance = this.position.distance(position)
    val massDistanceDiff = mass / (distance * distance + Constants.e)
    val unitDirection = (position - this.position) / distance

    unitDirection * massDistanceDiff
  }

  def countAcceleration(): Vec2 = bodiesMassDistanceDiffSum * Constants.G

  def resetAcceleration(): Unit = this.bodiesMassDistanceDiffSum = Vec2(BigDecimal("0"), BigDecimal("0"))

  def initMove(): Unit = {
    val acceleration = countAcceleration()
    this.position += this.velocity * SimulationConstants.dt + acceleration * Math.pow(SimulationConstants.dt, 2) * 0.5
    this.velocity += acceleration * SimulationConstants.dt * 0.5
    resetAcceleration()
  }

  def move(): Unit = {
    val acceleration = countAcceleration()
    this.position += this.velocity * SimulationConstants.dt + acceleration * Math.pow(SimulationConstants.dt, 2) * 0.5
    this.velocity += acceleration * SimulationConstants.dt
    resetAcceleration()
  }

  override def toString: String = f"$mass%30.2f  |   $position   |   $velocity"

  def toTuple: (String, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal) =
    Tuple6(id, mass, position.x, position.y, velocity.x, velocity.y)
}
