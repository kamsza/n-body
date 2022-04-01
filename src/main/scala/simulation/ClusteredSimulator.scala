package simulation

import akka.actor.{ActorSystem, Props}
import clustered.ClusteredSimulatorActor
import message.SimulationStart
import utils.CSVUtil
import clustered.ClusterActor

object ClusteredSimulator extends App {
  val inputDir = "/ss"
  val outputDir = "results/ss"

  val system = ActorSystem("N-BodySystem")

  val clusters = CSVUtil.loadClusters(inputDir, outputDir, system, classOf[ClusterActor])

  val simulatorActor = system.actorOf(Props(classOf[ClusteredSimulatorActor]))

  simulatorActor ! SimulationStart(clusters)
}
