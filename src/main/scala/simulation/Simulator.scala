package simulation

import akka.actor.{ActorSystem, Props}
import message.SimulationStart
import single.SimulatorActor
import utils.CSVUtil

object Simulator extends App {
//  val inputFileName = "solar_system.txt"
//  val outputDir = "results/out"
//
//  val system = ActorSystem("N-BodySystem")

  // CLUSTERED
//
//
//
//
//  val cluster1 = system.actorOf(
//    Props(
//      classOf[ClusterActor],
//      "1",
//      CSVUtil.loadBodies(inputFileName, "1", Vec2(BigDecimal("7.0e11"), 0)),
//      Paths.get(outputDir, "cluster_1.csv")
//    ),
//    name = "cluster1")
//
//  val cluster2 = system.actorOf(
//    Props(
//      classOf[ClusterActor],
//      "2",
//      CSVUtil.loadBodies(inputFileName, "2", Vec2(BigDecimal("-7.0e11"), 0)),
//      Paths.get(outputDir, "cluster_2.csv")
//    ),
//    name = "cluster2")
//
//  val simulatorActor = system.actorOf(Props(classOf[SimulatorActor]))
//
//  simulatorActor ! SimulationStart(List(cluster1, cluster2))

//  val bodies = CSVUtil.loadBodies(inputFileName, outputDir, system)
//  val simulatorActor = system.actorOf(Props(classOf[SimulatorActor]))
//
//  simulatorActor ! SimulationStart(bodies)
//
  // BODY
//  cluster1 ! SaveData("2_bodies_1.csv")
//  cluster2 ! SaveData("2_bodies_2.csv")
//
//  val stepsCount = 500
//  for(step <- 1 to stepsCount) {
//    cluster1 ! MakeSimulation(5000)
//    cluster2 ! MakeSimulation(5000)
//
//    cluster1 ! SaveData("2_bodies_1.csv")
//    cluster2 ! SaveData("2_bodies_2.csv")
//
//    ProgressMarker.updateProgress(step, stepsCount)
//  }
}

