package clustered_common

import `object`.Object
import akka.actor.{Actor, ActorRef}
import common.ActorDescriptor
import constant.SimulationConstants
import math.Vec2
import message.{ClusterInitialized, ClusterReady, OneTenthDone, SimulationFinish}
import utils.{CsvUtil, PhysicsUtil}

import java.io.BufferedWriter
import scala.collection.mutable

abstract class AbstractClusterActor(
                                     override val id: String,
                                     override val mass: BigDecimal,
                                     var position: Vec2,
                                     var bodies: Set[Body],
                                     resultsFileWriter: Option[BufferedWriter])
  extends Object with Actor {

  val neighbourClusters: mutable.Set[ActorDescriptor] = mutable.Set[ActorDescriptor]()

  var stepsCounter: Int = SimulationConstants.simulationStepsCount
  val progressMarker: Int = Math.max(1, (SimulationConstants.simulationStepsCount / 10).floor.toInt)
  var receivedMessagesCounter: Int = 0

  var managingActor: ActorRef = ActorRef.noSender
  var progressMonitor: ActorRef = ActorRef.noSender

  var timestamp: Int = 0

  def this(id: String, bodies: Set[Body], resultsFileWriter: Option[BufferedWriter]) = {
    this(id, PhysicsUtil.countSummaryMass(bodies), PhysicsUtil.countCenterOfMass(bodies), bodies, resultsFileWriter)
  }

  def setProgressMonitor(progressMonitor: ActorRef): Unit = {
    this.progressMonitor = progressMonitor
  }

  def handleInitialize(managingActor: ActorRef, progressMonitor: ActorRef): Unit = {
    this.managingActor = managingActor
    this.progressMonitor = progressMonitor
    managingActor ! ClusterInitialized(id, position)
  }

  def handleAddNeighbourClusters(clusters: Set[ActorDescriptor]): Unit = {
    neighbourClusters.addAll(clusters)
    managingActor ! ClusterReady()
  }

  def handleMakeSimulation(): Unit = {
    applyForce()
    bodies.foreach(_.initMove())
    sendUpdate()
  }

  def applyForce(): Unit = {
    bodies.foreach(body => bodies.filter(b => b.id != body.id).foreach(body.applyForce))
    bodies.foreach(body => neighbours.filter(n => n.id != this.id).foreach(body.applyForce))
  }

  def makeSimulationStep(): Unit = {
    stepsCounter -= 1
    updateBodiesPosition()
    position = countCenterOfMass()
  }

  def updateBodiesPosition(): Unit = {
    applyForce()
    bodies.foreach(_.move())
  }

  def countCenterOfMass(): Vec2 = PhysicsUtil.countCenterOfMass(bodies)

  def sendUpdate(): Unit

  def neighbours: Set[Object]

  def doOnSimulationStepAction(stepsCounter: Int): Unit = {
    if (resultsFileWriter.isDefined && stepsCounter % SimulationConstants.communicationStep == 0) writeDataToFile()
    if (stepsCounter % progressMarker == 0) progressMonitor ! OneTenthDone(id)
    if (stepsCounter == 0) finish()
  }

  def writeDataToFile(): Unit = {
    val dataString = bodies
      .map(body => body.toTuple)
      .map(tuple => tuple.productIterator.mkString(CsvUtil.DELIMITER))
      .map(str => str + CsvUtil.DELIMITER + timestamp)
      .mkString("\n")
    resultsFileWriter.get.write(s"\n${dataString}")
  }

  def finish(): Unit = {
    if(resultsFileWriter.isDefined) resultsFileWriter.get.close()
    managingActor ! SimulationFinish()
    context.stop(self)
  }

  def countSummaryMass(): BigDecimal = PhysicsUtil.countSummaryMass(bodies)

  def moveSystemMassCenter(vector: Vec2): Unit = bodies.foreach(_.changePosition(vector))

  @Override
  def toList: List[(String, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal)] = {
    bodies.toList
      .sortBy(body => body.id)
      .map(body => body.toTuple)
  }

  override def toString: String = bodies.map(body => body.toString).mkString("\n")
}
