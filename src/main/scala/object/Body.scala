package `object`

import akka.actor.{Actor, ActorRef}
import message.{BodyDataInit, BodyDataSave, BodyDataUpdate, SimulationFinish, SimulationStart}
import utils.{Constants, SimulationConstants, Vec2}

import scala.collection.mutable

case class Body(
            id: String,
            mass: BigDecimal,
            var position: Vec2,
            var velocity: Vec2,
            neighbourBodiesCount: Int
) extends Actor {

  var simulationConstants: SimulationConstants = SimulationConstants()
  var acceleration: Vec2 = Vec2(BigDecimal("0"), BigDecimal("0"))
  var managingActor: ActorRef = ActorRef.noSender
  var bodies: mutable.Set[String] = mutable.Set()
  var msgId = 0
  var counter = 0

  override def receive: Receive = {
    case SimulationStart(managingActor, simulationConstants) =>
      this.managingActor = managingActor
      this.simulationConstants = simulationConstants
      managingActor ! BodyDataSave(this.id, this.mass, this.position, this.velocity, this.msgId)
      // TODO: check for better way to realise broadcast
      context.system.actorSelection("/user/*") ! BodyDataInit(id, mass, position)

    case BodyDataInit(id, mass, position) if id != this.id =>
      bodies += id
      acceleration += countAcceleration(mass, position)
      if (bodies.size == neighbourBodiesCount) update(initMove)

    case BodyDataUpdate(id, mass, position) if id != this.id =>
      bodies += id
      acceleration += countAcceleration(mass, position)
      if (bodies.size == neighbourBodiesCount) update(move)
      if (counter == simulationConstants.communicationStep) saveState()
      if (msgId == simulationConstants.simulationStepsCount) finish()
  }

  def update(move: () => Unit): Unit = {
    move()
    msgId += 1
    counter += 1
    bodies.clear()
    acceleration = Vec2(BigDecimal("0"), BigDecimal("0"))
    context.system.actorSelection("/user/*") ! BodyDataUpdate(this.id, this.mass, this.position)
  }

  def saveState(): Unit = {
    counter = 0
    managingActor ! BodyDataSave(this.id, this.mass, this.position, this.velocity, this.msgId)
  }

  def initMove(): Unit = {
    this.position += this.velocity * simulationConstants.dt + this.acceleration * Math.pow(simulationConstants.dt, 2) * 0.5
    this.velocity += this.acceleration * simulationConstants.dt * 0.5
  }

  def move(): Unit = {
    this.position = this.position + this.velocity * simulationConstants.dt + acceleration * math.pow(simulationConstants.dt, 2) / 2
    this.velocity = this.velocity + acceleration * simulationConstants.dt
  }

//  def initMove(): Unit = {
//    this.position = this.position + this.velocity * simulationConstants.dt + acceleration * math.pow(simulationConstants.dt, 2) / 2
//    this.velocity = this.velocity + acceleration * simulationConstants.dt
//    acceleration = Vec2(BigDecimal("0"), BigDecimal("0"))
//  }
//
//  def move(): Unit = {
//    this.position = this.position + this.velocity * simulationConstants.dt + acceleration * math.pow(simulationConstants.dt, 2) / 2
//    this.velocity = this.velocity + acceleration * simulationConstants.dt
//    acceleration = Vec2(BigDecimal("0"), BigDecimal("0"))
//  }


  def countAcceleration(mass: BigDecimal, position: Vec2): Vec2 = {
    val distance = this.position.distance(position)
    val a = (Constants.G * mass) / (distance * distance + Constants.e)
    val unitDirection = (position - this.position) / distance

    unitDirection * a
  }



  def finish(): Unit = {
    managingActor ! SimulationFinish()
    context.stop(self)
  }
}
