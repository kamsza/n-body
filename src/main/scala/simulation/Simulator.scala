package simulation

import `object`.Cluster
import utils.{CSVUtil, Vec2}

import java.io.File

object Simulator extends App {
  val inputFileName = "2_bodies.txt"
  val outputFileName = "2_bodies.csv"

  new File("results/" + outputFileName).delete()

  val cluster1 = new Cluster("1", CSVUtil.loadBodies(inputFileName, "1"))
  val cluster2 = new Cluster("2", CSVUtil.loadBodies(inputFileName, "2"))
  cluster1.moveCluster(new Vec2(BigDecimal("5.0e10"), 0));
  cluster2.moveCluster(new Vec2(BigDecimal("-5.0e10"), 0));
  cluster1.saveData(outputFileName)
  cluster2.saveData(outputFileName)

  val stepsCount = 1000
  for(step <- 1 to stepsCount) {
    for(_ <- 1 to 2000) {
      cluster1.makeSimulationStep()
      cluster2.makeSimulationStep()
    }
    cluster1.saveData(outputFileName)
    cluster2.saveData(outputFileName)
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
