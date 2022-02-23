package simulation

import akka.actor.{ActorSystem, Props}
import clustered.SimulatorActor
import message.SimulationStart
import utils.CSVUtil


object ClusteredSimulator extends App {
  val inputFileName = "1_body.txt"
  val outputDir = "results/out"

  val system = ActorSystem("N-BodySystem")

  val clusters = CSVUtil.loadClusters("/solar_systems", "results/solar_systems", system)

  val simulatorActor = system.actorOf(Props(classOf[SimulatorActor]))

  simulatorActor ! SimulationStart(clusters)
}
