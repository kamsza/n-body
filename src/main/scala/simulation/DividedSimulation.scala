package simulation

import akka.actor.{ActorSystem, Props}
import divided.DividedSimulatorActor
import message.SimulationStart
import utils.CSVUtil
import divided.ClusterActor

object DividedSimulation extends App {
  val inputDir = "/solar_systems"
  val outputDir = "results/solar_systems2"

  val system = ActorSystem("N-BodySystem")

  val clusters = CSVUtil.loadClusters(inputDir, outputDir, system, classOf[ClusterActor])

  val simulatorActor = system.actorOf(Props(classOf[DividedSimulatorActor]))

  simulatorActor ! SimulationStart(clusters)
}
