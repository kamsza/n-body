package simulation

import akka.actor.{ActorSystem, Props}
import clustered.{ClusterActor, SimulatorActor}
import message.SimulationStart
import utils.{CSVUtil, Vec2}

import java.nio.file.Paths

object Simulator extends App {
  val inputFileName = "1_body.txt"
  val outputDir = "results/out"

  val system = ActorSystem("N-BodySystem")

  val cluster1 = system.actorOf(
    Props(
      classOf[ClusterActor],
      "1",
      CSVUtil.loadBodies(inputFileName, "1", Vec2(BigDecimal("7.0e11"), 0)),
      Paths.get(outputDir, "cluster_1.csv")
    ),
    name = "cluster1")

  val cluster2 = system.actorOf(
    Props(
      classOf[ClusterActor],
      "2",
      CSVUtil.loadBodies(inputFileName, "2", Vec2(BigDecimal("-7.0e11"), 0)),
      Paths.get(outputDir, "cluster_2.csv")
    ),
    name = "cluster2")

  val simulatorActor = system.actorOf(Props(classOf[SimulatorActor]))

  simulatorActor ! SimulationStart(List(cluster1, cluster2))



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

object ProgressMarker {
  val markersCount = 10

  def updateProgress(step: Int, stepsCount: Int): Unit = {
    val stepsPerUpdate = stepsCount / markersCount
    if(stepsPerUpdate != 0 && step % stepsPerUpdate == 0) {
      val partDone = step / stepsPerUpdate
      val partToDo = markersCount - partDone
      println(s"[${"X" * partDone}${"-" * partToDo}] ${partDone}/${markersCount}")
    }
  }
}
