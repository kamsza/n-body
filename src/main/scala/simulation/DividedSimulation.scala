package simulation

import akka.actor.{ActorSystem, Props}
import divided.{ClusterActor, DividedSimulatorActor}
import message.SimulationStart
import utils.CSVUtil

object DividedSimulation extends App {
  val inputDir = "/solar_systems"
  val outputDir = "results/divided_ss"

  val system = ActorSystem("N-BodySystem")

  val clusters = CSVUtil.loadClusters(inputDir, outputDir, system, classOf[ClusterActor])

  val simulatorActor = system.actorOf(Props(classOf[DividedSimulatorActor]))

  simulatorActor ! SimulationStart(clusters)
}
