package divided

import `object`.Object
import akka.actor.{Actor, ActorRef}
import clustered.Body
import constant.SimulationConstants
import math.Vec2
import message.{ActivateProgressMonitor, AddNeighbourClusters, ClusterDataUpdate, ClusterInitialized, ClusterReady, DividedDataUpdate, Initialize, MakeSimulation, OneTenthDone, SimulationFinish}
import utils.CSVUtil.DELIMITER
import utils.PhysicsUtil

import java.io.BufferedWriter
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ClusterActor(
                    override val id: String,
                    override val mass: BigDecimal,
                    var position: Vec2,
                    resultsFileWriter: BufferedWriter,
                    bodies: ArrayBuffer[Body] = ArrayBuffer[Body](),
                    neighbourClusters: ArrayBuffer[ActorRef] = ArrayBuffer[ActorRef](),
                    neighbourObjects: mutable.Map[String, ClusterDescriptor] = mutable.Map())
  extends Object with Actor {

  var stepsCounter: Int = SimulationConstants.simulationStepsCount

  var managingActor: ActorRef = ActorRef.noSender

  var receivedMessagesCounter: Int = 0

  val progressMarker: Int = Math.max(1, (SimulationConstants.simulationStepsCount / 10).floor.toInt)

  var progressMonitor: ActorRef = ActorRef.noSender

  var timestamp: Int = 0

  def this(id: String, bodies: ArrayBuffer[Body], resultsFilePath: BufferedWriter) = {
    this(id, PhysicsUtil.countSummaryMass(bodies), PhysicsUtil.countCenterOfMass(bodies), resultsFilePath)
    this.bodies.addAll(bodies)
  }

  def setProgressMonitor(progressMonitor: ActorRef) :Unit = {
    this.progressMonitor = progressMonitor
  }

  override def receive: Receive = {
    case Initialize(simulationController, progressMonitor) => handleInitialize(simulationController, progressMonitor)
    case AddNeighbourClusters(clusters) => handleAddNeighbourClusters(clusters)
    case ActivateProgressMonitor(progressMonitor) => setProgressMonitor(progressMonitor)
    case MakeSimulation() => handleMakeSimulation()
    case DividedDataUpdate(id, mass, position, timestamp, neighbours) => handleClusterDataUpdate(id, mass, position, timestamp, neighbours)
  }

  def handleInitialize(simulationController: ActorRef, progressMonitor: ActorRef): Unit = {
    managingActor = simulationController
    this.progressMonitor = progressMonitor
    managingActor ! ClusterInitialized(id, position)
  }

  def handleAddNeighbourClusters(clusters: Set[ActorRef]): Unit = {
    neighbourClusters.addAll(clusters)
    managingActor ! ClusterReady()
  }

  def handleMakeSimulation(): Unit = {
    makeSimulationStep()
    sendUpdate()
  }

  def makeSimulationStep(): Unit = {
    stepsCounter -= 1
    updateBodiesPosition()
    position = countCenterOfMass()
  }


  def sendUpdate(): Unit = neighbourClusters.foreach(_ ! ClusterDataUpdate(id, mass, position))

  def handleClusterDataUpdate(id: String, mass: BigDecimal, position: Vec2, timestamp: Int, neighbours: Set[ClusterDescriptor]): Unit = {
    receivedMessagesCounter += 1
    neighbourObjects.get(id) match {
      case Some(clusterDescriptor: ClusterDescriptor) if clusterDescriptor.timestamp < timestamp =>
        clusterDescriptor.position = position
        clusterDescriptor.timestamp = timestamp
      case None => neighbourObjects += (id -> ClusterDescriptor(id, mass, position, timestamp))
    }

    neighbours.foreach(clusterDescriptor => neighbourObjects.get(clusterDescriptor.id) match {
      case Some(cD: ClusterDescriptor) if cD.timestamp < timestamp =>
        cD.position = position
        cD.timestamp = timestamp
      case None => neighbourObjects += (id -> ClusterDescriptor(id, mass, position, timestamp))
    }
    )

    if(receivedMessagesCounter == neighbourClusters.size) {                       // TODO: additionally check timestamp between last msg and current, if is big, update
      receivedMessagesCounter = 0
      makeSimulationStep()
      doOnSimulationStepAction(stepsCounter)
      sendUpdate()
    }
  }

  def updateBodiesPosition(): Unit = {
    bodies.foreach(body => bodies.foreach(body.applyForce))
    bodies.foreach(body => neighbourObjects.values.foreach(body.applyForce))
    bodies.foreach(_.move())
  }

  def doOnSimulationStepAction(stepsCounter: Int): Unit = {
    if(stepsCounter % SimulationConstants.communicationStep == 0) writeDataToFile()
    if(stepsCounter % progressMarker == 0) progressMonitor ! OneTenthDone()
    if(stepsCounter == 0) finish()
  }

  def writeDataToFile(): Unit = {
    val dataString = bodies
      .map(body => body.toTuple)
      .map(tuple => tuple.productIterator.mkString(DELIMITER))
      .mkString("\n")
    resultsFileWriter.write(s"\n${dataString}")
  }


  def countCenterOfMass(): Vec2 = PhysicsUtil.countCenterOfMass(bodies)

  def countSummaryMass(): BigDecimal = PhysicsUtil.countSummaryMass(bodies)

  def moveSystemMassCenter(vector: Vec2): Unit = bodies.foreach(_.changePosition(vector))

  @Override
  def toList: List[(String, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal)] = {
    bodies.sortBy(body => body.id).map(body => body.toTuple).toList
  }

  override def toString: String = bodies.map(body => body.toString).mkString("\n")

  def finish(): Unit = {
    resultsFileWriter.close()
    managingActor ! SimulationFinish()
    context.stop(self)
  }
}

