package simulation

import `object`.Cluster
import akka.actor.{ActorSystem, Props}
import message.{MakeSimulation, MoveCluster, SaveData, SimulationStart}
import utils.{CSVUtil, Vec2}

object Simulator extends App {
  val inputFileName = "2_bodies.txt"
  val outputFileName = "2_bodies.csv"

  CSVUtil.initCsvFile("2_bodies_1.csv")
  CSVUtil.initCsvFile("2_bodies_2.csv")

  val system = ActorSystem("N-BodySystem")

  val cluster1 = system.actorOf(
    Props(
      classOf[Cluster],
      "1",
      CSVUtil.loadBodies(inputFileName, "1")
    ),
    name = "cluster1")

  val cluster2 = system.actorOf(
    Props(
      classOf[Cluster],
      "2",
      CSVUtil.loadBodies(inputFileName, "2")
    ),
    name = "cluster2")

  cluster1 ! MoveCluster(Vec2(BigDecimal("5.0e10"), 0))
  cluster2 ! MoveCluster(Vec2(BigDecimal("-5.0e10"), 0))

//  val simulatorActor = system.actorOf(Props(classOf[ActorSystem]))
//
//  simulatorActor ! SimulationStart(List(cluster1, cluster2))



  cluster1 ! SaveData("2_bodies_1.csv")
  cluster2 ! SaveData("2_bodies_2.csv")

  val stepsCount = 500
  for(step <- 1 to stepsCount) {
    cluster1 ! MakeSimulation(5000)
    cluster2 ! MakeSimulation(5000)

    cluster1 ! SaveData("2_bodies_1.csv")
    cluster2 ! SaveData("2_bodies_2.csv")

    ProgressMarker.updateProgress(step, stepsCount)
  }
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
