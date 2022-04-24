package divided

import `object`.Object
import akka.actor.ActorRef
import clustered_common.{AbstractClusterActor, Body}
import common.ActorDescriptor
import message._

import java.io.BufferedWriter
import scala.collection.mutable

class ClusterActor(
                    id: String,
                    _bodies: Set[Body],
                    resultsFileWriter: BufferedWriter)
  extends AbstractClusterActor(id, _bodies, resultsFileWriter) {

  val clusters: mutable.Map[String, ClusterDescriptor] = mutable.Map(id -> ClusterDescriptor(id, mass, position, timestamp))
  var timestamp: Int = 0
  var connectionManager: ActorRef = ActorRef.noSender

  override def receive: Receive = {
    case DividedInitialize(simulationController, progressMonitor, connectionManager) => handleInitialize(simulationController, progressMonitor, connectionManager)
    case AddNeighbourClusters(clusters) => handleAddNeighbourClusters(clusters)
    case ActivateProgressMonitor(progressMonitor) => setProgressMonitor(progressMonitor)
    case MakeSimulation() => handleMakeSimulation()
    case DividedDataUpdate(clusters) => handleClusterDataUpdate(clusters)
    case UpdateNeighbourList(newNeighbours, farNeighbours) => handleUpdateNeighbourList(newNeighbours, farNeighbours)
    case UpdateBodiesList(newBodies) => handleUpdateBodiesList(newBodies)
  }

  def handleInitialize(simulationController: ActorRef, progressMonitor: ActorRef, connectionManager: ActorRef): Unit = {
    super.handleInitialize(simulationController, progressMonitor)
    this.connectionManager = connectionManager
    connectionManager ! SayHello()
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

    if (receivedMessagesCounter == neighbourClusters.size) {
      receivedMessagesCounter = 0
      makeSimulationStep()
      updateDescriptor()
      doOnSimulationStepAction(stepsCounter)
      sendUpdate()
    }
  }

  override def sendUpdate(): Unit = {
    neighbourClusters.foreach(_.actorRef ! DividedDataUpdate(clusters.values.toSet))
  }

  override def makeSimulationStep(): Unit = {
    super.makeSimulationStep()
    timestamp += 1
  }

  def updateDescriptor(): Unit = {
    clusters.update(id, ClusterDescriptor(id, mass, position, timestamp))
  }

  override def doOnSimulationStepAction(stepsCounter: Int): Unit = {
    super.doOnSimulationStepAction(stepsCounter)
    if (stepsCounter % 10 == 0) checkBodiesAffiliation()
    if (stepsCounter % 10 == 0) checkClusterAffiliation()
  }

  def checkBodiesAffiliation(): Unit = {
    bodies.map(body => (body, this.position.distance(body.position)))
      .map(bodyDescriptor => (bodyDescriptor._1, bodyDescriptor._1.findNewCluster(bodyDescriptor._2, neighbours)))
      .filter(bodyDescriptor => bodyDescriptor._2.isDefined)
      .map(bodyDescriptor => (bodyDescriptor._1, bodyDescriptor._2.get))
      .groupBy(_._2)
      .foreach(group => {
        neighbourClusters.find(nc => nc.id == group._1.id) match {
          case Some(actorDescriptor: ActorDescriptor) =>
            actorDescriptor.actorRef ! UpdateBodiesList(group._2.map(x => x._1))
            this.bodies.removedAll(group._2.map(x => x._1))
          case None => // nothing to do?
        }
      })
  }

  override def neighbours: Set[Object] = clusters.values.toSet

  def checkClusterAffiliation(): Unit = connectionManager ! ClusterNeighbourNetworkUpdate(this.id, this.position, this.neighbours.map(n => n.id))

  def handleUpdateNeighbourList(newNeighbours: Set[ActorDescriptor], farNeighbours: Set[ActorDescriptor]): Unit = {
    println(s"handleUpdateNeighbourList  newNeighbours: ${newNeighbours.size}  farNeighbours: ${farNeighbours.size}")
    //??
  }

  def handleUpdateBodiesList(newBodies: Set[Body]): Unit = {
    println(s"handleUpdateBodiesList  newBodies: ${newBodies.size}")
    //this.bodies ++= newBodies
  }
}

