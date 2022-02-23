package simulation

import akka.actor.{ActorSystem, Props}
import message.SimulationStart
import single.SimulatorActor
import utils.CSVUtil

object SingleSimulator extends App {
  val inputFileName = "solar_system.txt"
  val outputDir = "results/out"

  val system = ActorSystem("N-BodySystem")

  val bodies = CSVUtil.loadBodies(inputFileName, outputDir, system)

  val simulatorActor = system.actorOf(Props(classOf[SimulatorActor]))

  simulatorActor ! SimulationStart(bodies)
}
