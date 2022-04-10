package simulation

import akka.actor.{ActorSystem, Props}
import message.SimulationStart
import single.SingleSimulatorActor
import utils.CSVUtil

object SingleSimulator extends App {
  val inputFileName = "solar_systems.txt"
  val outputDir = "results/single_ss"

  val system = ActorSystem("N-BodySystem")

  val bodies = CSVUtil.loadBodiesActors(inputFileName, outputDir, system)

  val simulatorActor = system.actorOf(Props(classOf[SingleSimulatorActor]))

  simulatorActor ! SimulationStart(bodies)
}
