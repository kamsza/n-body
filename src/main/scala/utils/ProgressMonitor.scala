package utils

import akka.actor.Actor
import constant.SimulationConstants
import message.{OneTenthDone, ProgressMonitorInitialize, SayHello}

import scala.collection.mutable

case class ProgressMonitor(actorsCount: Int) extends Actor {

  val markersCount = 10

  var receivedCounter = 0

  var partsDone = 0

  var actorIds: Set[String] = null

  var receivedIds: mutable.SortedSet[String] = mutable.SortedSet()

  def partsToDo: Int = markersCount - partsDone

  override def receive: Receive = {
    case ProgressMonitorInitialize(actorIds) => handleProgressMonitorInitialize(actorIds)
    case OneTenthDone(id) => handleTenthDone(id)
  }

  def handleProgressMonitorInitialize(actorIds: Set[String]): Unit = {
    this.actorIds = actorIds
    println(s"""simulation configuration
      - steps count: ${SimulationConstants.simulationStepsCount}
      - dt: ${SimulationConstants.dt}
      - save data step: ${SimulationConstants.communicationStep}""")
  }

  def handleTenthDone(id: String): Unit = {
    if(receivedIds.contains(id)) {
      println("----------------------- WARNING -----------------------")
      println(s"| Actor with id ${id} is way ahead others")
      println(s"| Received info from: ${receivedIds.mkString(", ")}")
    }

    receivedIds += id
    if(receivedIds.equals(actorIds)) {
      receivedIds.clear()
      receivedCounter = 0
      updateProgress()
      checkFinish()
    }
  }

  def updateProgress(): Unit = {
    partsDone += 1
    println(s"[${"X" * partsDone}${"-" * partsToDo}] ${partsDone}/${markersCount}")
  }

  def checkFinish():Unit = {
    if(partsDone == markersCount) {
      context.stop(self)
    }
  }
}
