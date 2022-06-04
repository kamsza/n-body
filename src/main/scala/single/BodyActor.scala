package single

import `object`.AbstractBody
import akka.actor.{Actor, ActorRef}
import constant.SimulationConstants
import math.Vec2
import message._
import utils.CsvUtil

import java.io.BufferedWriter

class BodyActor(
                 id: String,
                 mass: Double,
                 startPosition: Vec2,
                 startVelocity: Vec2,
                 resultsFileWriter: Option[BufferedWriter])
  extends AbstractBody(id, mass, startPosition, startVelocity) with Actor {

  val progressMarker: Int = Math.max(1, (SimulationConstants.simulationStepsCount / 10).floor.toInt)

  var bodyActors: Set[ActorRef] = Set.empty

  var managingActor: ActorRef = ActorRef.noSender
  var progressMonitor: ActorRef = ActorRef.noSender

  var stepNumber: Int = 0
  var receivedMessagesCounter: Int = 0

  override def receive: Receive = {
    case AddNeighbourBodies(bodies, simulationController) => handleAddNeighbourBodies(bodies, simulationController)
    case ActivateProgressMonitor(progressMonitor) => setProgressMonitor(progressMonitor)
    case MakeSimulation() => handleMakeSimulation()
    case BodyDataUpdate(id, mass, position) if id != this.id => handleBodyDataUpdate(id, mass, position)
  }

  def setProgressMonitor(progressMonitor: ActorRef): Unit = {
    this.progressMonitor = progressMonitor
  }

  def handleAddNeighbourBodies(bodies: Set[ActorRef], simulationController: ActorRef): Unit = {
    managingActor = simulationController
    bodyActors = bodies
    managingActor ! ActorReady()
  }

  def handleMakeSimulation(): Unit = {
    initMove()
    sendUpdate()
  }

  def handleBodyDataUpdate(id: String, mass: Double, position: Vec2): Unit = {
    receivedMessagesCounter += 1

    applyForce(id, mass, position)
    if (receivedMessagesCounter == bodyActors.size) {
      receivedMessagesCounter = 0
      makeSimulationStep()
    }
  }

  def makeSimulationStep(): Unit = {
    stepNumber += 1
    move()
    doOnSimulationStepAction(stepNumber)
    sendUpdate()
  }

  def doOnSimulationStepAction(stepsCounter: Int): Unit = {
    if(stepNumber == 0) {
      // nothing to do
    } else if (stepNumber == SimulationConstants.simulationStepsCount) {
      writeDataToFile()
      finish()
    } else {
      doExtraSimulationStepAction(stepsCounter)
    }
  }

  def doExtraSimulationStepAction(stepsCounter: Int): Unit = {
    if (stepsCounter % SimulationConstants.communicationStep == 0) writeDataToFile()
    if (stepsCounter % progressMarker == 0) progressMonitor ! OneTenthDone(id)
  }

  def writeDataToFile(): Unit = {
    if(resultsFileWriter.isDefined) {
      val dataString = this.toTuple
        .productIterator
        .mkString(CsvUtil.DELIMITER)

      resultsFileWriter.get.write(s"\n${dataString}${CsvUtil.DELIMITER}${stepNumber}")
    }
  }

  def finish(): Unit = {
    if(resultsFileWriter.isDefined) resultsFileWriter.get.close()
    managingActor ! SimulationFinish()
    context.stop(self)
  }

  def sendUpdate(): Unit = bodyActors.foreach(_ ! BodyDataUpdate(this.id, this.mass, this.position))
}
