package clustered

import `object`.Object
import clustered_common.{AbstractClusterActor, Body}
import math.Vec2
import message._

import java.io.BufferedWriter
import scala.collection.mutable

class ClusterActor(
                    id: String,
                    bodies: Set[Body],
                    resultsFileWriter: Option[BufferedWriter])
  extends AbstractClusterActor(id, bodies, resultsFileWriter) {

  val neighbourObjects: mutable.Map[String, ClusterDescriptor] = mutable.Map()

  override def receive: Receive = {
    case Initialize(simulationController, progressMonitor) => handleInitialize(simulationController, progressMonitor)
    case AddNeighbourClusters(clusters) => handleAddNeighbourClusters(clusters)
    case SendDataInit() => handleSendDataInit()
    case ClusteredDataInit(id, mass, position) => handleClusteredDataInit(id, mass, position)
    case ActivateProgressMonitor(progressMonitor) => setProgressMonitor(progressMonitor)
    case MakeSimulation() => handleMakeSimulation()
    case ClusterDataUpdate(id, mass, position) => handleClusterDataUpdate(id, mass, position)
  }

  def handleSendDataInit(): Unit = {
    neighbourClusters.foreach(
      _.actorRef ! ClusteredDataInit(id, mass, position)
    )
  }

  def handleClusteredDataInit(id: String, mass: BigDecimal, position: Vec2): Unit = {
    receivedMessagesCounter += 1
    neighbourObjects += (id -> ClusterDescriptor(id, mass, position))
    if (receivedMessagesCounter == neighbourClusters.size) {
      receivedMessagesCounter = 0
      managingActor ! ClusterDataInitialized()
    }
  }

  def handleClusterDataUpdate(id: String, mass: BigDecimal, position: Vec2): Unit = {
    receivedMessagesCounter += 1
    neighbourObjects += (id -> ClusterDescriptor(id, mass, position))
    if (receivedMessagesCounter == neighbourClusters.size) {
      receivedMessagesCounter = 0
      makeSimulationStep()
      doOnSimulationStepAction(stepsCounter)
      sendUpdate()
    }
  }

  def sendUpdate(): Unit = neighbourClusters.foreach(_.actorRef ! ClusterDataUpdate(id, mass, position))

  override def neighbours: Set[Object] = neighbourObjects.values.toSet
}

