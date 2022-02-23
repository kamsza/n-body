package simulation

import akka.actor.{ActorSystem, Props}
import clustered.SimulatorActor
import message.SimulationStart
import utils.CSVUtil


object ClusteredSimulator extends App {
  val inputDir = "/solar_systems"
  val outputDir = "results/solar_systems"

  val system = ActorSystem("N-BodySystem")

  val clusters = CSVUtil.loadClusters(inputDir, outputDir, system)

  val simulatorActor = system.actorOf(Props(classOf[SimulatorActor]))

  simulatorActor ! SimulationStart(clusters)
}
