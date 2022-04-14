package clustered_common

import `object`.Object
import akka.actor.{Actor, ActorRef}
import common.ActorDescriptor
import constant.SimulationConstants
import math.Vec2
import message.{ClusterInitialized, ClusterReady, OneTenthDone, SimulationFinish}
import utils.CSVUtil.DELIMITER
import utils.PhysicsUtil

import java.io.BufferedWriter
import scala.collection.mutable

abstract class AbstractClusterActor(
                                     override val id: String,
                                     override val mass: BigDecimal,
                                     var position: Vec2,
                                     bodies: Set[Body],
                                     resultsFileWriter: BufferedWriter)
  extends Object with Actor {

  protected val neighbourClusters: mutable.Set[ActorDescriptor] = mutable.Set[ActorDescriptor]()

  var stepsCounter: Int = SimulationConstants.simulationStepsCount

  var managingActor: ActorRef = ActorRef.noSender

  var receivedMessagesCounter: Int = 0

  val progressMarker: Int = Math.max(1, (SimulationConstants.simulationStepsCount / 10).floor.toInt)

  var progressMonitor: ActorRef = ActorRef.noSender

  def this(id: String, bodies: Set[Body], resultsFileWriter: BufferedWriter) = {
    this(id, PhysicsUtil.countSummaryMass(bodies), PhysicsUtil.countCenterOfMass(bodies), bodies, resultsFileWriter)
  }

  def setProgressMonitor(progressMonitor: ActorRef) :Unit = {
    this.progressMonitor = progressMonitor
  }

  def handleInitialize(simulationController: ActorRef, progressMonitor: ActorRef): Unit = {
    managingActor = simulationController
    this.progressMonitor = progressMonitor
    managingActor ! ClusterInitialized(id, position)
  }

  def handleAddNeighbourClusters(clusters: Set[ActorDescriptor]): Unit = {
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

  def sendUpdate(): Unit

  def neighbours: Set[Object]

  def updateBodiesPosition(): Unit = {
    bodies.foreach(body => bodies.filter(b => b.id != body.id).foreach(body.applyForce))
    bodies.foreach(body => neighbours.filter(n => n.id != this.id).foreach(body.applyForce))
    bodies.foreach(_.move())
  }

  def doOnSimulationStepAction(stepsCounter: Int): Unit = {
    if(stepsCounter % SimulationConstants.communicationStep == 0) writeDataToFile()
    if(stepsCounter % progressMarker == 0) progressMonitor ! OneTenthDone(id)
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
    bodies.toList
      .sortBy(body => body.id)
      .map(body => body.toTuple)
  }

  override def toString: String = bodies.map(body => body.toString).mkString("\n")

  def finish(): Unit = {
    resultsFileWriter.close()
    managingActor ! SimulationFinish()
    context.stop(self)
  }
}
