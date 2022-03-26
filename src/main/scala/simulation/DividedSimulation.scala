package simulation

import akka.actor.{ActorSystem, Props}
import divided.DividedSimulatorActor
import message.SimulationStart
import utils.CSVUtil

object DividedSimulation extends App {
  val inputDir = "/cls-12"
  val outputDir = "results/cls-12"

  val system = ActorSystem("N-BodySystem")

  val clusters = CSVUtil.loadClusters(inputDir, outputDir, system)

  val simulatorActor = system.actorOf(Props(classOf[DividedSimulatorActor]))

  simulatorActor ! SimulationStart(clusters)
}
