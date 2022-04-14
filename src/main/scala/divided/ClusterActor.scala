package divided

import `object`.Object
import clustered_common.{AbstractClusterActor, Body}
import message.{ActivateProgressMonitor, AddNeighbourClusters, DividedDataUpdate, Initialize, MakeSimulation}

import java.io.BufferedWriter
import scala.collection.mutable

class ClusterActor(
                    id: String,
                    bodies: Set[Body],
                    resultsFileWriter: BufferedWriter)
  extends AbstractClusterActor(id, bodies, resultsFileWriter) {

  var timestamp: Int = 0

  val clusters: mutable.Map[String, ClusterDescriptor] = mutable.Map(id -> ClusterDescriptor(id, mass, position, timestamp))

  override def receive: Receive = {
    case Initialize(simulationController, progressMonitor) => handleInitialize(simulationController, progressMonitor)
    case AddNeighbourClusters(clusters) => handleAddNeighbourClusters(clusters)
    case ActivateProgressMonitor(progressMonitor) => setProgressMonitor(progressMonitor)
    case MakeSimulation() => handleMakeSimulation()
    case DividedDataUpdate(clusters) => handleClusterDataUpdate(clusters)
  }

  override def sendUpdate(): Unit = {
    neighbourClusters.foreach(_.actorRef ! DividedDataUpdate(clusters.values.toSet))
  }

  def handleClusterDataUpdate(clustersUpdate: Set[ClusterDescriptor]): Unit = {
    receivedMessagesCounter += 1

    clustersUpdate.foreach(cluster => {
      clusters.get(cluster.id) match {
        case Some(cD: ClusterDescriptor) if cD.timestamp < timestamp =>
          cD.position = cluster.position
          cD.timestamp = cluster.timestamp
        case Some(_: ClusterDescriptor) => // nothing to do
        case None => clusters += (cluster.id -> ClusterDescriptor(cluster.id, cluster.mass, cluster.position, cluster.timestamp))
      }
    })

    if(receivedMessagesCounter == neighbourClusters.size) {
      receivedMessagesCounter = 0
      makeSimulationStep()
      updateDescriptor()
      doOnSimulationStepAction(stepsCounter)
      sendUpdate()
    }
  }

  override def makeSimulationStep(): Unit = {
    super.makeSimulationStep()
    timestamp += 1
  }

  def updateDescriptor(): Unit = {
    clusters.update(id, ClusterDescriptor(id, mass, position, timestamp))
  }

  override def neighbours: Set[Object] = clusters.values.toSet
}

