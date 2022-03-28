package divided

import akka.actor.ActorRef
import clustered_common.ClusterSimulationHandler
import constant.Constants
import math.Vec2
import message.{AddNeighbourClusters, ClusterInitialized, ClusterReady, SimulationFinish, SimulationStart}

import scala.collection.mutable

case class DividedSimulatorActor() extends ClusterSimulationHandler {

  var initializedClustersCounter = 0

  val clusterObjects :mutable.Set[ObjectDescriptor] = mutable.Set()

  override def receive: Receive = {
    case SimulationStart(clusters) => handleSimulationStart(clusters)
    case ClusterInitialized(id, position) => handleClusterInitialized(id, position, sender())
    case ClusterReady() => handleActorReady()
    case SimulationFinish() => handleSimulationFinish()
  }

  def handleClusterInitialized(id: String, position: Vec2, senderRef: ActorRef): Unit = {
    clusterObjects.add(ObjectDescriptor(id, position, senderRef))
    initializedClustersCounter += 1
    if(initializedClustersCounter == clusters.size) setNeighbours()
  }

  def setNeighbours(): Unit = {
    clusterObjects.foreach(cluster => {
      val neighbourClusters = clusterObjects
        .filterNot(c => cluster.equals(c))
        .filter(c => cluster.position.distance(c.position) < Constants.neighbourDistance)
        .map(c => c.actorRef)
      cluster.actorRef ! AddNeighbourClusters(neighbourClusters.toSet)
    })
  }
}

