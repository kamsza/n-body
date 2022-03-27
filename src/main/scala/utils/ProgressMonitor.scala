package utils

import akka.actor.Actor
import constant.SimulationConstants
import message.{OneTenthDone, SayHello}

case class ProgressMonitor(actorsCount: Int) extends Actor {

  val markersCount = 10

  var partsDone = 0

  var receivedCounter = 0

  override def receive: Receive = {
    case SayHello() =>
      println(s"""simulation configuration
      - steps count: ${SimulationConstants.simulationStepsCount}
      - dt: ${SimulationConstants.dt}
      - save data step: ${SimulationConstants.communicationStep}""")
    case OneTenthDone() =>
      receivedCounter += 1
      if(receivedCounter == actorsCount) {
        updateProgress()
        receivedCounter = 0
        checkFinish()
      }
  }

  def updateProgress(): Unit = {
    partsDone += 1
    val partsToDo = markersCount - partsDone
    println(s"[${"X" * partsDone}${"-" * partsToDo}] ${partsDone}/${markersCount}")
  }

  def checkFinish():Unit = {
    if(partsDone == markersCount) {
      context.stop(self)
    }
  }
}
