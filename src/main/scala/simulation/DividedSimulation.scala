package simulation

import akka.actor.{ActorSystem, Props}
import divided.DividedSimulatorActor
import message.SimulationStart
import utils.CSVUtil

object DividedSimulation extends App {
  val inputDir = "/solar_systems"
  val outputDir = "results/solar_systems"

  val system = ActorSystem("N-BodySystem")

  val clusters = CSVUtil.loadClusters(inputDir, outputDir, system)

  val simulatorActor = system.actorOf(Props(classOf[DividedSimulatorActor]))

  simulatorActor ! SimulationStart(clusters)
}
