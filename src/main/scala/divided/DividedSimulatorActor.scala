package divided

import akka.actor.{ActorRef, Props}
import clustered_common.ClusterSimulationHandler
import common.ActorDescriptor
import math.Vec2
import message._

import scala.collection.mutable

case class DividedSimulatorActor() extends ClusterSimulationHandler {

  val connectionManager: ActorRef = context.actorOf(Props(classOf[ConnectionManagerActor]), "connection_manager")

  val clusterObjects: mutable.Set[ClusterActorDescriptor] = mutable.Set()

  var initializedClustersCounter = 0

  override def receive: Receive = {
    case SimulationStart(clusters) => handleSimulationStart(clusters)
    case ClusterInitialized(id, position) => handleClusterInitialized(id, position, sender())
    case ClusterReady() => handleActorReady()
    case SimulationFinish() => handleSimulationFinish()
  }

  def handleClusterInitialized(id: String, position: Vec2, senderRef: ActorRef): Unit = {
    clusterObjects.add(ClusterActorDescriptor(id, position, senderRef))
    initializedClustersCounter += 1
    if (initializedClustersCounter == clusters.size) {
      afterClustersInitialize()
      setNeighbours()
    }
  }

  def afterClustersInitialize(): Unit = {
    progressMonitor ! ProgressMonitorInitialize(clusterObjects.map(c => c.id).toSet)
    connectionManager ! ConnectionManagerInitialize(clusterObjects.toSet)
  }

  def setNeighbours(): Unit = {
    clusterObjects.foreach(cluster => {
      //      val neighbourClusters = clusterObjects
      //        .filterNot(c => cluster.equals(c))
      //        .filter(c => cluster.position.distance(c.position) < Constants.neighbourDistance)
      //        .map(c => ActorDescriptor(c.id, c.actorRef))
      //        .toSet
      val neighbourClusters = clusterObjects
        .filterNot(c => cluster.equals(c))
        .filter(c => isGoodId(cluster.id, c.id))
        .map(c => ActorDescriptor(c.id, c.actorRef))
        .toSet
      cluster.actorRef ! AddNeighbourClusters(neighbourClusters)
    })
  }

  def isGoodId(clusterId: String, neighbourId: String): Boolean = {
    clusterId match {
      case "solar_system_1" => neighbourId == "solar_system_2" || neighbourId == "solar_system_5"
      case "solar_system_2" => neighbourId == "solar_system_1" || neighbourId == "solar_system_3"
      case "solar_system_3" => neighbourId == "solar_system_2" || neighbourId == "solar_system_4"
      case "solar_system_4" => neighbourId == "solar_system_3" || neighbourId == "solar_system_5"
      case "solar_system_5" => neighbourId == "solar_system_4" || neighbourId == "solar_system_1"
    }
  }

  override def handleSimulationFinish(): Unit = {
    connectionManager ! SimulationFinish()
    super.handleSimulationFinish()
  }

  override def initializeClusters(): Unit = {
    clusters.foreach(cluster => cluster.actorRef ! DividedInitialize(context.self, progressMonitor, connectionManager))
  }
}

