package divided

import akka.actor.{ActorRef, Props}
import clustered_common.ClusterSimulationHandler
import common.ActorDescriptor
import constant.{Constants, SimulationConstants}
import math.Vec2
import message._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class DividedSimulatorActor() extends ClusterSimulationHandler {

  val connectionManager: ActorRef = context.actorOf(
    Props(classOf[ConnectionManagerActor]),
    "connection_manager"
  )

  val clusterObjects: mutable.Set[ClusterActorDescriptor] = mutable.Set()

  val activeInitCluster: mutable.Set[(String, String)] = mutable.Set()

  var initializedClustersCounter = 0

  override def receive: Receive = {
    case SimulationStart(clusters) => handleSimulationStart(clusters)
    case ClusterInitialized(id, position) =>
      handleClusterInitialized(id, position, sender())
    case ClusterReady()     => handleClusterReady()
    case ActorInitActive(id,  ids, oldMessageId, newMessageId) => handleActive(id, ids, oldMessageId, newMessageId)
    case ActorInitInactive(id, messageId) =>         handleInactive(id, messageId)
    case SimulationFinish() => handleSimulationFinish()
  }

  def handleClusterReady(): Unit = {
    readyActorsCounter += 1
    if (readyActorsCounter.equals(actorsCount)) {
      startSimulation()
//      actors.foreach(body => body ! SendDataInit())
//      println("data initialization")
    }
  }

  def handleActive(id: String, ids: Set[String], oldMessageId: String, newMessageId: String): Unit = {
    val activeClusterId = (id, oldMessageId)
    activeInitCluster -= activeClusterId
    activeInitCluster.addAll(ids.map(id => (id, newMessageId)))
  }

  def handleInactive(id: String, messageId: String): Unit = {
    val activeClusterId = (id, messageId)
      activeInitCluster -= activeClusterId
    if(activeInitCluster.isEmpty) {
      startSimulation()
    }
  }

  def handleClusterInitialized(
      id: String,
      position: Vec2,
      senderRef: ActorRef
  ): Unit = {
    clusterObjects.add(ClusterActorDescriptor(id, position, senderRef))
    initializedClustersCounter += 1
    if (initializedClustersCounter == clusters.size) {
      afterClustersInitialize()
      setNeighbours()
    }
  }

  def afterClustersInitialize(): Unit = {
    progressMonitor ! ProgressMonitorInitialize(
      clusterObjects.map(c => c.id).toSet
    )
    connectionManager ! ConnectionManagerInitialize(clusterObjects.toSet)
  }

  def setNeighbours(): Unit = {
    println("setting neighbours")
    val neighboursCount = new ListBuffer[Int]()
    clusterObjects.foreach(cluster => {
      var neighbourClusters = getNeighboursInRange(cluster)

      if(neighbourClusters.size < SimulationConstants.minNeighboursCount) {
        val closestMissingNeighbours = getNClosestNonNeighbours(cluster, neighbourClusters)
        closestMissingNeighbours.foreach(neighbourActor =>
          neighbourActor.actorRef ! AddNeighbourCluster(ActorDescriptor(cluster.id, cluster.actorRef)))
        neighbourClusters ++= closestMissingNeighbours
      }

      neighboursCount += neighbourClusters.size

      cluster.actorRef ! AddNeighbourClusters(neighbourClusters)
    })
    printNeighboursCountInfo(neighboursCount.toList)
    println("setting neighbours done")
  }

  def getNeighboursInRange(cluster: ClusterActorDescriptor): Set[ActorDescriptor] =
    clusterObjects
      .filterNot(c => cluster.equals(c))
      .filter(c => cluster.position.distance(c.position) < SimulationConstants.neighbourDistance)
      .map(c => ActorDescriptor(c.id, c.actorRef))
      .toSet

  def getNClosestNonNeighbours(cluster: ClusterActorDescriptor, neighbours: Set[ActorDescriptor]): Set[ActorDescriptor] =
    clusterObjects
      .filterNot(c => cluster.equals(c))
      .filter(c => !neighbours.exists(x => x.id == c.id))
      .toList
      .sortWith(_.position.distance(cluster.position) < _.position.distance(cluster.position))
      .take(SimulationConstants.minNeighboursCount - neighbours.size)
      .map(c => ActorDescriptor(c.id, c.actorRef))
      .toSet

  def printNeighboursCountInfo(neighboursCount: List[Int]): Unit = {
    println("------------- NEIGH COUNT -------------")
    neighboursCount
      .groupBy(e => e)
      .map(t => (t._1, t._2.length))
      .toSeq
      .sortBy(_._1)
      .foreach(x => println(s"> neighbours count: ${x._1}     clusters count:  ${x._2}"))
  }

  override def handleSimulationFinish(): Unit = {
    finishedActorsCounter += 1
    if (finishedActorsCounter.equals(actorsCount)) {
      connectionManager ! SimulationFinish()
      super.endSimulation()
    }
  }

  override def initializeClusters(): Unit = {
    clusters.foreach(cluster =>
      cluster.actorRef ! DividedInitialize(
        context.self,
        progressMonitor,
        connectionManager
      )
    )
  }
}
