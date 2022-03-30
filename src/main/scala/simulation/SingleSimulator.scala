package simulation

import akka.actor.{ActorSystem, Props}
import message.SimulationStart
import single.SingleSimulatorActor
import utils.CSVUtil

object SingleSimulator extends App {
  val inputFileName = "solar_system.txt"
  val outputDir = "results/out"

  val system = ActorSystem("N-BodySystem")

  val bodies = CSVUtil.loadBodiesActors(inputFileName, outputDir, system)

  val simulatorActor = system.actorOf(Props(classOf[SingleSimulatorActor]))

  simulatorActor ! SimulationStart(bodies)
}
