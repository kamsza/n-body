package clustered

import akka.actor.ActorRef
import clustered_common.ClusterSimulationHandler
import common.ActorDescriptor
import message._

import scala.collection.mutable

case class ClusteredSimulatorActor() extends ClusterSimulationHandler {

  val clusterObjects: mutable.Set[ActorDescriptor] = mutable.Set()
  var initializedClustersCounter = 0

  override def receive: Receive = {
    case SimulationStart(clusters) => handleSimulationStart(clusters)
    case ClusterInitialized(id, _) => handleClusterInitialized(id, sender())
    case ClusterReady() => handleClusterReady()
    case ClusterDataInitialized() => handleClusterDataInitialized()
    case SimulationFinish() => handleSimulationFinish()
  }

  def handleClusterReady(): Unit = {
    readyActorsCounter += 1
    if (readyActorsCounter.equals(actorsCount)) {
      readyActorsCounter = 0
      actors.foreach(body => body ! SendDataInit())
    }
  }

  def handleClusterInitialized(id: String, senderRef: ActorRef): Unit = {
    clusterObjects.add(common.ActorDescriptor(id, senderRef))
    initializedClustersCounter += 1
    if (initializedClustersCounter == clusters.size) {
      afterClustersInitialize()
      setNeighbours()
    }
  }

  def handleClusterDataInitialized(): Unit = {
    readyActorsCounter += 1
    if (readyActorsCounter.equals(actorsCount)) {
      readyActorsCounter = 0
      startSimulation()
    }
  }

  def afterClustersInitialize(): Unit = {
    progressMonitor ! ProgressMonitorInitialize(clusterObjects.map(c => c.id).toSet)
  }

  def setNeighbours(): Unit = {
    clusterObjects.foreach(cluster => {
      val neighbourClusters = clusterObjects.filterNot(_ == cluster).toSet
      cluster.actorRef ! AddNeighbourClusters(neighbourClusters)
    })
  }
}
