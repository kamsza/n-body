import akka.actor.{Actor, ActorRef}

import scala.collection.mutable
import scala.math.pow

class Body(bodiesCount: Int,
           id: Int,
           mass: Double,
           var position: Vector,
           var velocity: Vector) extends Actor {

  var bodies: mutable.Map[Int, Vector] = mutable.Map()
  var replyTo: ActorRef = self
  var msgId: Int = 0

  def receive: Receive = {
    case StartSimulation(replyTo) =>
      this.replyTo = replyTo
      context.system.actorSelection("/user/*") ! Data(this.id, this.msgId, this.mass, this.position, this.velocity)
    case Data(id, msgId, mass, position, _) =>
      if (id != this.id && msgId <= Constants.simulationStepsCount) {
        bodies += (id -> forceBetween(mass, position))
        if (bodies.size == bodiesCount - 1) {
          this.msgId += 1
          if(msgId == Constants.simulationStepsCount) {
            this.replyTo ! "finished"
            context.system.actorSelection("/user/*") ! Data(this.id, this.msgId, this.mass, this.position, this.velocity)
          } else {
            val F = bodies.foldLeft(Vector(0, 0))(_ + _._2)
            this.move(F)
            this.bodies.clear()
            context.system.actorSelection("/user/*") ! Data(this.id, this.msgId, this.mass, this.position, this.velocity)
          }
        }
      }
    case _ => println("Body got unknown message")
  }

  def forceBetween(mass: Double, position: Vector): Vector = {
    val distance = this.position.distance(position)
    val force = (Constants.G * mass * this.mass) / pow(distance, 2)
    val unitDirection = (position - this.position) / distance

    unitDirection * force
  }

  def move(force: Vector): Unit = {
    val a = force / this.mass
    this.velocity += a * Constants.dt
    this.position += this.velocity * Constants.dt
  }
}