package simulation

import akka.actor.{ActorSystem, Props}
import utils.CSVUtil
import message.SimulationInit

object Simulator extends App {
  val system = ActorSystem("N-BodySystem")
  val bodies = CSVUtil.loadBodies("2_bodies.txt", system)
  val simulationActor = system.actorOf(Props(classOf[SimulatorActor]), name = "simulationactor")
  simulationActor ! SimulationInit(bodies)
}
