package divided

import `object`.Object
import clustered_common.{AbstractClusterActor, Body}
import math.Vec2
import message.{ActivateProgressMonitor, AddNeighbourClusters, DividedDataUpdate, Initialize, MakeSimulation}

import java.io.BufferedWriter
import scala.collection.mutable

class ClusterActor(
                    id: String,
                    bodies: mutable.Set[Body],
                    resultsFileWriter: BufferedWriter)
  extends AbstractClusterActor(id, bodies, resultsFileWriter) {

  val neighbourObjects: mutable.Map[String, ClusterDescriptor] = mutable.Map()

  var timestamp: Int = 0

  override def receive: Receive = {
    case Initialize(simulationController, progressMonitor) => handleInitialize(simulationController, progressMonitor)
    case AddNeighbourClusters(clusters) => handleAddNeighbourClusters(clusters)
    case ActivateProgressMonitor(progressMonitor) => setProgressMonitor(progressMonitor)
    case MakeSimulation() => handleMakeSimulation()
    case DividedDataUpdate(id, mass, position, timestamp, neighbours) => handleClusterDataUpdate(id, mass, position, timestamp, neighbours)
  }

  override def sendUpdate(): Unit = neighbourClusters.foreach(_ ! DividedDataUpdate(id, mass, position, timestamp, Set.empty))

  def handleClusterDataUpdate(id: String, mass: BigDecimal, position: Vec2, timestamp: Int, neighbours: Set[ClusterDescriptor]): Unit = {
    receivedMessagesCounter += 1

    neighbourObjects.get(id) match {
      case Some(cD: ClusterDescriptor) if cD.timestamp < timestamp =>
        cD.position = position
        cD.timestamp = timestamp
      case Some(_: ClusterDescriptor) => // nothing to do
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

  override def neighbours: Set[Object] = neighbourObjects.values.toSet
}

