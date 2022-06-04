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
                                     override val mass: Double,
                                     var position: Vec2,
                                     var bodies: Set[Body],
                                     resultsFileWriter: Option[BufferedWriter])
  extends Object with Actor {

  val progressMarker: Int = Math.max(1, (SimulationConstants.simulationStepsCount / 10).floor.toInt)

  val neighbourActorDescriptors: mutable.Set[ActorDescriptor] = mutable.Set[ActorDescriptor]()

  var managingActor: ActorRef = ActorRef.noSender
  var progressMonitor: ActorRef = ActorRef.noSender

  var receivedMessagesCounter: Int = 0
  var stepNumber: Int = 0

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
    neighbourActorDescriptors.addAll(clusters)
    managingActor ! ClusterReady()
  }

  def handleMakeSimulation(): Unit = {
    applyForce()
    bodies.foreach(_.initMove())
    sendUpdate()
  }

  def makeSimulationStep(): Unit = {
    stepNumber += 1

    moveBodies()
    updateClusterPosition()

    doOnSimulationStepAction(stepNumber)
    sendUpdate()
  }

  def moveBodies(): Unit = {
    applyForce()
    bodies.foreach(_.move())
  }

  def updateClusterPosition(): Unit =
    this.position = PhysicsUtil.countCenterOfMass(bodies)

  def applyForce(): Unit = {
    val clusterDescriptorsValues = systemClusterDescriptors.filter(n => n.id != this.id)
    bodies.foreach(body => {
      bodies.foreach(body.applyForce)
      clusterDescriptorsValues.foreach(body.applyForce)
    })
  }

  def doOnSimulationStepAction(stepsCounter: Int): Unit = {
    if (stepNumber == 0) {
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
      val dataString = bodies
        .map(body => body.toTuple)
        .map(tuple => tuple.productIterator.mkString(CsvUtil.DELIMITER))
        .map(str => str + CsvUtil.DELIMITER + stepNumber)
        .mkString("\n")
      resultsFileWriter.get.write(s"\n${dataString}")
    }
  }

  def toList: List[(String, Double, Double, Double, Double, Double)] = {
    bodies.toList
      .sortBy(body => body.id)
      .map(body => body.toTuple)
  }

  override def toString: String = bodies.map(body => body.toString).mkString("\n")

  def finish(): Unit = {
    if(resultsFileWriter.isDefined) resultsFileWriter.get.close()
    managingActor ! SimulationFinish()
    context.stop(self)
  }

  def sendUpdate(): Unit

  def systemClusterDescriptors: Set[Object]
}
