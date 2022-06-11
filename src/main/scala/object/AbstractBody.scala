package `object`

import constant.{Constants, SimulationConstants}
import math.Vec2

abstract class AbstractBody(
                             val id: String,
                             val mass: Double,
                             val startPosition: Vec2,
                             val startVelocity: Vec2,
                           ) extends Object {

  var position: Vec2 = startPosition

  var velocity: Vec2 = startVelocity

  var acceleration: Vec2 = Vec2(0.0, 0.0)

  var bodiesMassDistanceDiffSum: Vec2 = Vec2(0.0, 0.0)

  def applyForce(otherObject: Object): Unit = {
    if (id != otherObject.id)
      applyForce(otherObject.id, otherObject.mass, otherObject.position)
  }

  def applyForce(id:String, mass: Double, position: Vec2): Unit = {
    val diffVec = countMassDistanceDiffVec(mass, position)
    if(diffVec.x.isNaN || diffVec.y.isNaN) {
      println(s"WARN: NaN between ${this.id} (${this.mass}) and ${id} (${mass})")
    } else {
      bodiesMassDistanceDiffSum += diffVec
    }
  }

  def countMassDistanceDiffVec(mass: Double, position: Vec2): Vec2 = {
    val distance = this.position.distance(position)
    val massDistanceDiff = mass / (distance * distance + SimulationConstants.softeningParameter)
    val unitDirection = (position - this.position) / distance

    unitDirection * massDistanceDiff
  }

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

  def countAcceleration(): Vec2 = bodiesMassDistanceDiffSum * Constants.G

  def resetAcceleration(): Unit = this.bodiesMassDistanceDiffSum = Vec2(0.0, 0.0)

  override def toString: String = f"$mass%30.2f  |   $position   |   $velocity"

  def toTuple: (String, Double, Double, Double, Double, Double) =
    Tuple6(id, mass, position.x, position.y, velocity.x, velocity.y)
}
