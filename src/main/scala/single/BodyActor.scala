package single

import `object`.AbstractBody
import akka.actor.{Actor, ActorRef}
import constant.SimulationConstants
import math.Vec2
import message._
import utils.SimulatingActorFactory.DELIMITER

import java.io.BufferedWriter
import scala.collection.mutable

class BodyActor(
                 id: String,
                 mass: BigDecimal,
                 startPosition: Vec2,
                 startVelocity: Vec2,
                 resultsFileWriter: Option[BufferedWriter])
  extends AbstractBody(id, mass, startPosition, startVelocity) with Actor {

  val progressMarker: Int = Math.max(1, (SimulationConstants.simulationStepsCount / 10).floor.toInt)
  var neighbourBodies: Set[ActorRef] = null
  var bodies: mutable.Set[String] = mutable.Set()
  var stepsCounter: Int = SimulationConstants.simulationStepsCount
  var managingActor: ActorRef = ActorRef.noSender
  var receivedMessagesCounter: Int = 0
  var neighbourBodiesCount: Int = 0
  var progressMonitor: ActorRef = ActorRef.noSender

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
    neighbourBodiesCount = bodies.size
    neighbourBodies = bodies
    managingActor ! ActorReady()
  }

  def handleMakeSimulation(): Unit = {
    initMove()
    sendUpdate()
  }

  def handleBodyDataUpdate(id: String, mass: BigDecimal, position: Vec2): Unit = {
    receivedMessagesCounter += 1
    bodies += id
    applyForce(mass, position)
    if (receivedMessagesCounter == neighbourBodiesCount) {
      receivedMessagesCounter = 0
      makeSimulationStep()
      doOnSimulationStepAction(stepsCounter)
      sendUpdate()
    }
  }

  def makeSimulationStep(): Unit = {
    stepsCounter -= 1
    move()
  }

  def doOnSimulationStepAction(stepsCounter: Int): Unit = {
    if (resultsFileWriter.isDefined && stepsCounter % SimulationConstants.communicationStep == 0) writeDataToFile()
    if (stepsCounter % progressMarker == 0) progressMonitor ! OneTenthDone(id)
    if (stepsCounter == 0) finish()
  }

  def writeDataToFile(): Unit = {
    val dataString = this.toTuple
      .productIterator
      .mkString(DELIMITER)

    resultsFileWriter.get.write(s"\n${dataString}")
  }

  def finish(): Unit = {
    if(resultsFileWriter.isDefined) resultsFileWriter.get.close()
    managingActor ! SimulationFinish()
    context.stop(self)
  }

  def sendUpdate(): Unit = neighbourBodies.foreach(_ ! BodyDataUpdate(this.id, this.mass, this.position))
}
