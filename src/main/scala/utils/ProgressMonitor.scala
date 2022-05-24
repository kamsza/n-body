package utils

import akka.actor.Actor
import constant.SimulationConstants
import message.{OneTenthDone, ProgressMonitorInitialize}

import scala.collection.mutable

/**
 * Actor printing info messages to keep track of simulation progress.
 */
case class ProgressMonitor() extends Actor {

  val markersCount = 10

  var receivedCounter = 0

  var partsDone = 0

  var actorIds: Set[String] = Set.empty

  var receivedIds: mutable.SortedSet[String] = mutable.SortedSet()

  override def receive: Receive = {
    case ProgressMonitorInitialize(actorIds) => handleProgressMonitorInitialize(actorIds)
    case OneTenthDone(id) => handleTenthDone(id)
  }

  def handleProgressMonitorInitialize(actorIds: Set[String]): Unit = {
    this.actorIds = actorIds
  }

  def handleTenthDone(id: String): Unit = {
    if (receivedIds.contains(id)) printWarningMessage(id)
    receivedIds += id
    if (receivedIds.equals(actorIds)) {
      receivedIds.clear()
      receivedCounter = 0
      updateProgress()
      checkFinish()
    }
  }

  def printWarningMessage(id: String): Unit = println(
    s"""----------------------- WARNING -----------------------
       | Actor with id ${id} is way ahead others
       | Received info from: ${receivedIds.mkString(", ")}""")

  def updateProgress(): Unit = {
    partsDone += 1
    printUpdateMessage()
  }

  def printUpdateMessage(): Unit = println(s"[${"X" * partsDone}${"-" * partsToDo}] ${partsDone}/${markersCount}")

  def partsToDo: Int = markersCount - partsDone

  def checkFinish(): Unit = {
    if (partsDone == markersCount) {
      context.stop(self)
    }
  }
}
