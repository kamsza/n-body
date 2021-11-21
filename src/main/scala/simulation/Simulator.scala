package simulation

import akka.actor.{ActorSystem, Props}
import message.SimulationInit

object Simulator extends App {
  val system = ActorSystem("N-BodySystem")
  val simulationActor = system.actorOf(Props(classOf[SimulatorActor], "solar_system.txt"), name = "simulationactor")
  simulationActor ! SimulationInit()
}
