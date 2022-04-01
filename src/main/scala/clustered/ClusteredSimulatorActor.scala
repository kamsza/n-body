package clustered

import akka.actor.ActorRef
import clustered_common.{ActorDescriptor, ClusterSimulationHandler}
import message.{AddNeighbourClusters, ClusterInitialized, ClusterReady, SimulationFinish, SimulationStart}

import scala.collection.mutable

case class ClusteredSimulatorActor() extends ClusterSimulationHandler {

  var initializedClustersCounter = 0

  val clusterObjects :mutable.Set[ActorDescriptor] = mutable.Set()

  override def receive: Receive = {
    case SimulationStart(clusters) => handleSimulationStart(clusters)
    case ClusterInitialized(id, _) => handleClusterInitialized(id, sender())
    case ClusterReady() => handleActorReady()
    case SimulationFinish() => handleSimulationFinish()
  }

  def handleClusterInitialized(id: String,  senderRef: ActorRef): Unit = {
    clusterObjects.add(ActorDescriptor(id, senderRef))
    initializedClustersCounter += 1
    if(initializedClustersCounter == clusters.size) setNeighbours()
  }

  def setNeighbours(): Unit = {
    clusterObjects.foreach(cluster => {
      val neighbourClusters = clusterObjects.filterNot(_ == cluster).toSet
      cluster.actorRef ! AddNeighbourClusters(neighbourClusters)
    })
  }
}
