package divided

import akka.actor.ActorRef
import clustered_common.{ActorDescriptor, ClusterSimulationHandler}
import constant.Constants
import math.Vec2
import message.{AddNeighbourClusters, ClusterInitialized, ClusterReady, ProgressMonitorInitialize, SimulationFinish, SimulationStart}

import scala.collection.mutable

case class DividedSimulatorActor() extends ClusterSimulationHandler {

  var initializedClustersCounter = 0

  val clusterObjects :mutable.Set[ClusterActorDescriptor] = mutable.Set()

  override def receive: Receive = {
    case SimulationStart(clusters) => handleSimulationStart(clusters)
    case ClusterInitialized(id, position) => handleClusterInitialized(id, position, sender())
    case ClusterReady() => handleActorReady()
    case SimulationFinish() => handleSimulationFinish()
  }

  def handleClusterInitialized(id: String, position: Vec2, senderRef: ActorRef): Unit = {
    clusterObjects.add(ClusterActorDescriptor(id, position, senderRef))
    initializedClustersCounter += 1
    if(initializedClustersCounter == clusters.size) {
      afterClustersInitialize()
      setNeighbours()
    }
  }

  def afterClustersInitialize(): Unit = {
    progressMonitor ! ProgressMonitorInitialize(clusterObjects.map(c => c.id).toSet)
  }


  def setNeighbours(): Unit = {
    clusterObjects.foreach(cluster => {
      val neighbourClusters = clusterObjects
        .filterNot(c => cluster.equals(c))
        .filter(c => cluster.position.distance(c.position) < Constants.neighbourDistance)
        .map(c => ActorDescriptor(c.id, c.actorRef))
        .toSet
      cluster.actorRef ! AddNeighbourClusters(neighbourClusters)
    })
  }
}

