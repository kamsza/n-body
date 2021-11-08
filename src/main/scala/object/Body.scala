package `object`

import akka.actor.{Actor, ActorRef}
import message.{BodyDataSave, BodyDataUpdate, SimulationFinish, SimulationStart}
import utils.{Constants, Vec2}

import scala.collection.mutable

case class Body(
            id: String,
            mass: BigDecimal,
            var position: Vec2,
            var velocity: Vec2,
            neighbourBodiesCount: Int
) extends Actor {

  var acceleration: Vec2 = Vec2(BigDecimal("0"), BigDecimal("0"))
  var managingActor: ActorRef = ActorRef.noSender
  var bodies: mutable.Set[String] = mutable.Set()
  var msgId = 0
  var counter = 0

  override def receive: Receive = {
    case SimulationStart(managingActor) =>
      this.managingActor = managingActor
      managingActor ! BodyDataSave(this.id, this.mass, this.position, this.velocity, this.msgId)
      // TODO: check for better way to realise broadcast
      context.system.actorSelection("/user/*") ! BodyDataUpdate(id, mass, position)
    case BodyDataUpdate(id, mass, position) =>
      if(id != this.id && msgId < Constants.simulationStepsCount) {
        bodies += id
        acceleration += countAcceleration(mass, position)
        if (bodies.size == neighbourBodiesCount) {
          this.msgId += 1
          move()
          bodies.clear()
          context.system.actorSelection("/user/*") ! BodyDataUpdate(this.id, this.mass, this.position)
          counter += 1
        }
        if(counter == Constants.communicationStep) {
          counter = 0
          managingActor ! BodyDataSave(this.id, this.mass, this.position, this.velocity, this.msgId)
        }
      } else if (msgId == Constants.simulationStepsCount) {
        managingActor ! SimulationFinish()
        context.stop(self)
      }
  }

    def countAcceleration(mass: BigDecimal, position: Vec2): Vec2 = {
      val distance = this.position.distance(position)
      val a = (Constants.G * mass) / (distance * distance)
      val unitDirection = (position - this.position) / distance

      unitDirection * a
    }

    def move(): Unit = {
      this.position = this.position + this.velocity * Constants.dt + acceleration * math.pow(Constants.dt, 2) / 2
      this.velocity = this.velocity + acceleration * Constants.dt
      acceleration = Vec2(BigDecimal("0"), BigDecimal("0"))
    }


//  def changePosition(changeVec: Vec2): Unit = this.position = this.position + changeVec
//
//  override def toString: String = f"$mass%30.2f  |   $position   |   $velocity"
//
//  def toTuple: (String, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal) =
//    Tuple6(id, mass, position.x, position.y, velocity.x, velocity.y)
}
