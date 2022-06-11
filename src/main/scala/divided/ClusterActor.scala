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

  val clusterDescriptors: mutable.Map[String, ClusterDescriptor] = mutable.Map(id -> ClusterDescriptor(id, mass, position, stepNumber))

  var connectionManager: ActorRef = ActorRef.noSender

  var expectedMessagesCount = -1

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

  override def handleMakeSimulation(): Unit = {
    if(clusterDescriptors.size != SimulationConstants.simulatingActorsCount) {
      println(s"WARNING: cluster ${id} has info from ${clusterDescriptors.size} clusters and should have from ${SimulationConstants.simulatingActorsCount}")
    }
    super.handleMakeSimulation()
  }

  def handleAddNeighbourClusters(cluster: ActorDescriptor):Unit = {
    neighbourActorDescriptors.add(cluster)
  }

  def handleSendDataInit(): Unit = {
    val clusterDescriptorValues = clusterDescriptors.values.toSet
    neighbourActorDescriptors.foreach(_.actorRef ! DividedDataInit(clusterDescriptorValues, java.util.UUID.randomUUID.toString))
  }

  def handleDataInit(clusters: Set[ClusterDescriptor], messageId: String): Unit = {
    val unknownClusters = clusters.diff(this.clusterDescriptors.values.toSet)
    if(unknownClusters.nonEmpty) {
      unknownClusters.foreach(c => this.clusterDescriptors += (c.id -> c))
      val newMessageId = java.util.UUID.randomUUID.toString
      val neighbourIds = neighbourActorDescriptors.map(x => x.id).toSet
      managingActor ! ActorInitActive(this.id, neighbourIds, messageId, newMessageId)
      val clusterDescriptorValues = this.clusterDescriptors.values.toSet
      neighbourActorDescriptors.foreach(_.actorRef ! DividedDataInit(clusterDescriptorValues, newMessageId))
    } else {
      managingActor ! ActorInitInactive(this.id, messageId)
    }
  }

  def handleClusterDataUpdate(clustersUpdate: Set[ClusterDescriptor]): Unit = {
    receivedMessagesCounter += 1

    clustersUpdate.foreach(cluster => {
      clusterDescriptors.get(cluster.id) match {
        case Some(cD: ClusterDescriptor) if cD.timestamp < stepNumber =>
          cD.position = cluster.position
          cD.timestamp = cluster.timestamp
        case Some(_: ClusterDescriptor) => // nothing to do
        case None =>
          clusterDescriptors += (cluster.id -> ClusterDescriptor(cluster.id, cluster.mass, cluster.position, cluster.timestamp))
      }
    })

    if (receivedMessagesCounter == expectedMessagesCount) {
      receivedMessagesCounter = 0
      makeSimulationStep()
    }
  }

  def handleNewNeighbourClusterDataUpdate(sender:  ActorDescriptor, clustersUpdate: Set[ClusterDescriptor]): Unit = {
    this.neighbourActorDescriptors.add(sender)
    handleClusterDataUpdate(clustersUpdate)
  }

  def handleFarNeighbourClusterDataUpdate(sender:  ActorDescriptor, clustersUpdate: Set[ClusterDescriptor]): Unit = {
    this.neighbourActorDescriptors -= sender
    handleClusterDataUpdate(clustersUpdate)
  }


  override def sendUpdate(): Unit = {
    clusterDescriptors.update(id, ClusterDescriptor(id, mass, position, stepNumber))
    val currentClustersInfo = clusterDescriptors.values.toSet
    neighbourActorDescriptors.foreach(_.actorRef ! DividedDataUpdate(currentClustersInfo))
    expectedMessagesCount = neighbourActorDescriptors.size
  }

  override def doExtraSimulationStepAction(stepsCounter: Int): Unit = {
    super.doExtraSimulationStepAction(stepsCounter)
    if (stepsCounter % SimulationConstants.bodiesAffiliationCheck == 0) checkBodiesAffiliation()
    if (stepsCounter % SimulationConstants.clusterNeighboursCheck == 0) checkClusterAffiliation()
  }

  def checkBodiesAffiliation(): Unit = {
    bodies
      .map(body => (body, this.position.distance(body.position)))
      .map(bodyDescriptor =>
        (
          bodyDescriptor._1,
          bodyDescriptor._1.findNewCluster(bodyDescriptor._2, systemClusterDescriptors)
        )
      )
      .filter(bodyDescriptor => bodyDescriptor._2.isDefined)
      .map(bodyDescriptor => (bodyDescriptor._1, bodyDescriptor._2.get))
      .groupBy(_._2)
      .foreach(group => {
        neighbourActorDescriptors.find(nc => nc.id == group._1.id) match {
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
      this.systemClusterDescriptors.map(n => n.id)
    )
  }

  override def systemClusterDescriptors: Set[Object] = clusterDescriptors.values.toSet

  def handleUpdateNeighbourList(
      newNeighbours: Set[ActorDescriptor],
      farNeighbours: Set[ActorDescriptor]
  ): Unit = {
    if(newNeighbours.nonEmpty || farNeighbours.nonEmpty) {
      val clusterDescriptorValues = clusterDescriptors.values.toSet
      newNeighbours.foreach(n => n.actorRef ! DividedNewNeighbourDataUpdate(ActorDescriptor(this.id, self), clusterDescriptorValues))
      farNeighbours.foreach(n => n.actorRef ! DividedFarNeighbourDataUpdate(ActorDescriptor(this.id, self), clusterDescriptorValues))
      this.neighbourActorDescriptors.addAll(newNeighbours)
      this.neighbourActorDescriptors --= farNeighbours
    }
  }

  def handleUpdateBodiesList(newBodies: Set[Body]): Unit = {
    if(newBodies.nonEmpty) {
      this.bodies ++= newBodies
    }
  }
}
