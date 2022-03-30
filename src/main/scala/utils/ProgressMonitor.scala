package utils

import akka.actor.Actor
import constant.SimulationConstants
import message.{OneTenthDone, SayHello}

case class ProgressMonitor(actorsCount: Int) extends Actor {

  val markersCount = 10

  var receivedCounter = 0

  var partsDone = 0

  def partsToDo: Int = markersCount - partsDone

  override def receive: Receive = {
    case SayHello() => handleWelcomeMessage()
    case OneTenthDone() => handleTenthDone()
  }

  def handleWelcomeMessage(): Unit = {
    println(s"""simulation configuration
      - steps count: ${SimulationConstants.simulationStepsCount}
      - dt: ${SimulationConstants.dt}
      - save data step: ${SimulationConstants.communicationStep}""")
  }

  def handleTenthDone(): Unit = {
    receivedCounter += 1
    if(receivedCounter == actorsCount) {
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
