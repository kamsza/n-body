package simulation

import `object`.Cluster
import utils.CSVUtil

import java.io.File

object Simulator extends App {
  val inputFileName = "2_bodies.txt"
  val outputFileName = "2_bodies.csv"

  new File("results/" + outputFileName).delete()

  val cluster = Cluster(CSVUtil.loadBodies(inputFileName))
  cluster.saveData(outputFileName)
//  for(i <- 1 to 40) {
//    for(_ <- 1 to 10000) cluster.makeSimulationStep()
//    cluster.saveData(outputFile)
//    if(i % 10 == 0) print("X")
//    if(i % 100 == 0) print("\n")
//  }
  val stepsCount = 1000
  for(step <- 1 to stepsCount) {
    for(_ <- 1 to 6000) {
      cluster.makeSimulationStep()
    }
    cluster.saveData(outputFileName)
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
