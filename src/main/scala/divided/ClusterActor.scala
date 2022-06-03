package divided

import `object`.Object
import akka.actor.ActorRef
import clustered_common.{AbstractClusterActor, Body}
import common.ActorDescriptor
import constant.SimulationConstants
import message._

import java.io.BufferedWriter
import scala.collection.mutable

class ClusterActor(
    id: String,
    _bodies: Set[Body],
    resultsFileWriter: Option[BufferedWriter]
) extends AbstractClusterActor(id, _bodies, resultsFileWriter) {

  val clusters: mutable.Map[String, ClusterDescriptor] = mutable.Map(id -> ClusterDescriptor(id, mass, position, timestamp))
  var connectionManager: ActorRef = ActorRef.noSender

  override def receive: Receive = {
    case DividedInitialize(simulationController, progressMonitor, connectionManager) => handleInitialize(simulationController, progressMonitor, connectionManager)
    case AddNeighbourClusters(clusters) => handleAddNeighbourClusters(clusters)
    case AddNeighbourCluster(cluster) => handleAddNeighbourClusters(cluster)
    case ActivateProgressMonitor(progressMonitor) => setProgressMonitor(progressMonitor)
    case SendDataInit() => handleSendDataInit()
    case DividedDataInit(clusters, id) => handleDataInit(clusters, id)
    case MakeSimulation() => handleMakeSimulation()
    case DividedDataUpdate(clusters) => handleClusterDataUpdate(clusters)
    case DividedNewNeighbourDataUpdate(sender, clusters) => handleNewNeighbourClusterDataUpdate(sender, clusters)
    case DividedFarNeighbourDataUpdate(sender, clusters) => handleFarNeighbourClusterDataUpdate(sender, clusters)
    case UpdateNeighbourList(newNeighbours, farNeighbours) => handleUpdateNeighbourList(newNeighbours, farNeighbours)
    case UpdateBodiesList(newBodies) => handleUpdateBodiesList(newBodies)
  }

  def handleInitialize(simulationController: ActorRef, progressMonitor: ActorRef, connectionManager: ActorRef): Unit = {
    super.handleInitialize(simulationController, progressMonitor)
    this.connectionManager = connectionManager
  }

  //override def handleMakeSimulation(): Unit = {
    //if(clusters.size != SimulationConstants.simulatingActorsCount) {
      //println(s"WARNING: cluster ${id} has info from ${clusters.size} clusters and should have from ${SimulationConstants.simulatingActorsCount}")
    //}
    //super.handleMakeSimulation()
  //}

  def handleAddNeighbourClusters(cluster: ActorDescriptor):Unit = {
    neighbourClusters.add(cluster)
  }

  def handleSendDataInit(): Unit = {
    neighbourClusters.foreach(_.actorRef ! DividedDataInit(clusters.values.toSet, java.util.UUID.randomUUID.toString))
  }

  def handleDataInit(clusters: Set[ClusterDescriptor], messageId: String): Unit = {
    val unknownClusters =  clusters.diff(this.clusters.values.toSet)
    if(unknownClusters.nonEmpty) {
      unknownClusters.foreach(c => this.clusters += (c.id -> c))
      val newMessageId = java.util.UUID.randomUUID.toString
      val neighbourIds = neighbourClusters.map(x => x.id).toSet
      managingActor ! ActorInitActive(this.id, neighbourIds, messageId, newMessageId)
      neighbourClusters.foreach(_.actorRef ! DividedDataInit(this.clusters.values.toSet, newMessageId))
    } else {
      managingActor ! ActorInitInactive(this.id, messageId)
    }
  }

  def handleClusterDataUpdate(clustersUpdate: Set[ClusterDescriptor]): Unit = {
    receivedMessagesCounter += 1

    clustersUpdate.foreach(cluster => {
      clusters.get(cluster.id) match {
        case Some(cD: ClusterDescriptor) if cD.timestamp < timestamp =>
          cD.position = cluster.position
          cD.timestamp = cluster.timestamp
        case Some(_: ClusterDescriptor) => // nothing to do
        case None =>
          clusters += (cluster.id -> ClusterDescriptor(cluster.id, cluster.mass, cluster.position, cluster.timestamp))
      }
    })

    if (receivedMessagesCounter == neighbourClusters.size) {
      receivedMessagesCounter = 0
      makeSimulationStep()
      updateDescriptor()
      doOnSimulationStepAction(stepsCounter)
      sendUpdate()
    }
  }

  def handleNewNeighbourClusterDataUpdate(sender:  ActorDescriptor, clustersUpdate: Set[ClusterDescriptor]): Unit = {
    this.neighbourClusters.add(sender)
    handleClusterDataUpdate(clustersUpdate)
  }

  def handleFarNeighbourClusterDataUpdate(sender:  ActorDescriptor, clustersUpdate: Set[ClusterDescriptor]): Unit = {
    this.neighbourClusters -= sender
    handleClusterDataUpdate(clustersUpdate)
  }


  override def sendUpdate(): Unit = {
    val currentClustersInfo = clusters.values.toSet
    neighbourClusters.foreach(_.actorRef ! DividedDataUpdate(currentClustersInfo))
  }

  override def makeSimulationStep(): Unit = {
    super.makeSimulationStep()
    timestamp += 1
  }

  def updateDescriptor(): Unit =
    clusters.update(id, ClusterDescriptor(id, mass, position, timestamp))

  override def doOnSimulationStepAction(stepsCounter: Int): Unit = {
    super.doOnSimulationStepAction(stepsCounter)
    if (stepsCounter != 0 && stepsCounter % SimulationConstants.bodiesAffiliationCheck == 0) checkBodiesAffiliation()
    if (stepsCounter != 0 && stepsCounter % SimulationConstants.clusterNeighboursCheck == 0) checkClusterAffiliation()
  }

  def checkBodiesAffiliation(): Unit = {
    bodies
      .map(body => (body, this.position.distance(body.position)))
      .map(bodyDescriptor =>
        (
          bodyDescriptor._1,
          bodyDescriptor._1.findNewCluster(bodyDescriptor._2, neighbours)
        )
      )
      .filter(bodyDescriptor => bodyDescriptor._2.isDefined)
      .map(bodyDescriptor => (bodyDescriptor._1, bodyDescriptor._2.get))
      .groupBy(_._2)
      .foreach(group => {
        neighbourClusters.find(nc => nc.id == group._1.id) match {
          case Some(actorDescriptor: ActorDescriptor) =>
            actorDescriptor.actorRef ! UpdateBodiesList(group._2.map(x => x._1))
            this.bodies --= group._2.map(x => x._1)
          case None => // nothing to do?
        }
      })
  }

  def checkClusterAffiliation(): Unit = {
    connectionManager ! ClusterNeighbourNetworkUpdate(
      this.id,
      this.position,
      this.neighbours.map(n => n.id)
    )
  }

  override def neighbours: Set[Object] = clusters.values.toSet

  def handleUpdateNeighbourList(
      newNeighbours: Set[ActorDescriptor],
      farNeighbours: Set[ActorDescriptor]
  ): Unit = {
    if(newNeighbours.nonEmpty || farNeighbours.nonEmpty) {
      newNeighbours.foreach(n => n.actorRef ! DividedNewNeighbourDataUpdate(ActorDescriptor(this.id, self), clusters.values.toSet))
      farNeighbours.foreach(n => n.actorRef ! DividedFarNeighbourDataUpdate(ActorDescriptor(this.id, self), clusters.values.toSet))
      this.neighbourClusters.addAll(newNeighbours)
      this.neighbourClusters --= farNeighbours
    }
  }

  def handleUpdateBodiesList(newBodies: Set[Body]): Unit = {
    if(newBodies.nonEmpty) {
      this.bodies ++= newBodies
    }
  }
}
