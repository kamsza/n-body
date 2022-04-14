package single

import `object`.AbstractBody
import akka.actor.{Actor, ActorRef}
import constant.SimulationConstants
import math.Vec2
import message.{ActivateProgressMonitor, ActorReady, AddNeighbourBodies, BodyDataUpdate, MakeSimulation, OneTenthDone, SimulationFinish}
import utils.CSVUtil.DELIMITER

import java.io.BufferedWriter
import scala.collection.mutable

class BodyActor(
                 id: String,
                 mass: BigDecimal,
                 startPosition: Vec2,
                 startVelocity: Vec2,
                 resultsFileWriter: BufferedWriter)
  extends AbstractBody(id, mass, startPosition, startVelocity) with Actor  {

  var neighbourBodies: Set[ActorRef] = null

  var bodies: mutable.Set[String] = mutable.Set()

  var stepsCounter: Int = SimulationConstants.simulationStepsCount

  var managingActor: ActorRef = ActorRef.noSender

  var receivedMessagesCounter: Int = 0

  var neighbourBodiesCount: Int = 0

  val progressMarker: Int = Math.max(1, (SimulationConstants.simulationStepsCount / 10).floor.toInt)

  var progressMonitor: ActorRef = ActorRef.noSender

  def setProgressMonitor(progressMonitor: ActorRef) :Unit = {
    this.progressMonitor = progressMonitor
  }

  override def receive: Receive = {
    case AddNeighbourBodies(bodies, simulationController) => handleAddNeighbourBodies(bodies, simulationController)
    case ActivateProgressMonitor(progressMonitor) => setProgressMonitor(progressMonitor)
    case MakeSimulation() => handleMakeSimulation()
    case BodyDataUpdate(id, mass, position) if id != this.id => handleBodyDataUpdate(id, mass, position)
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
    if(receivedMessagesCounter == neighbourBodiesCount) {
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
    if(stepsCounter % SimulationConstants.communicationStep == 0) writeDataToFile()
    if(stepsCounter % progressMarker == 0) progressMonitor ! OneTenthDone(id)
    if(stepsCounter == 0) finish()
  }

  def sendUpdate(): Unit = neighbourBodies.foreach(_ ! BodyDataUpdate(this.id, this.mass, this.position))

  def writeDataToFile(): Unit = {
    val dataString = this.toTuple
      .productIterator
      .mkString(DELIMITER)

    resultsFileWriter.write(s"\n${dataString}")
  }

  def finish(): Unit = {
    resultsFileWriter.close()
    managingActor ! SimulationFinish()
    context.stop(self)
  }
}
