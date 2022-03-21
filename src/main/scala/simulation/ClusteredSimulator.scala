package simulation

import akka.actor.{ActorSystem, Props}
import clustered.ClusteredSimulatorActor
import message.SimulationStart
import utils.CSVUtil


object ClusteredSimulator extends App {
  val inputDir = "/cls-12"
  val outputDir = "results/cls-12"

  val system = ActorSystem("N-BodySystem")

  val clusters = CSVUtil.loadClusters(inputDir, outputDir, system)

  val simulatorActor = system.actorOf(Props(classOf[ClusteredSimulatorActor]))

  simulatorActor ! SimulationStart(clusters)
}
