package divided

import akka.actor.{ActorRef, Props}
import clustered.ClusteredSimulatorActor
import message.{AddNeighbourClusters, ClusterInitialized, ClusterReady, Initialize, SayHello, SimulationFinish, SimulationStart}
import utils.{Constants, ProgressMonitor, Vec2}

import scala.collection.mutable

case class DividedSimulationActor() {
  //extends ClusteredSimulatorActor() {

//  var initializedClustersCounter = 0
//
//  val clusterObjects :mutable.Set[ObjectDescriptor] = mutable.Set()
//
//  override def receive: Receive = {
//    case SimulationStart(clusters) => handleSimulationStart(clusters)
//    case ClusterInitialized(id, position) => handleClusterInitialized(id, position, sender())
//    case ClusterReady => handleClusterReady()
//    case SimulationFinish => handleSimulationFinished()
//  }
//
//  override def handleSimulationStart(clusters: List[ActorRef]): Unit = {
//    this.clusters = clusters
//    val progressMonitor = createProgressMonitor()
//    clusters.foreach(cluster => cluster ! Initialize(context.self, progressMonitor))
//  }
//
//  def createProgressMonitor(): ActorRef = {
//    val progressMonitor = context.actorOf(Props(classOf[ProgressMonitor], clusters.size), "progress_monitor")
//    progressMonitor ! SayHello()
//    progressMonitor
//  }
//
//  def handleClusterInitialized(id: String, position: Vec2, senderRef: ActorRef): Unit = {
//    clusterObjects.add(ObjectDescriptor(id, position, senderRef))
//    initializedClustersCounter += 1
//    if(initializedClustersCounter == clusters.size) setNeighbours()
//  }
//
//  def setNeighbours(): Unit = {
//    clusterObjects.foreach(cluster => {
//      val neighbourClusters = clusterObjects
//        .filterNot(c => cluster.equals(c))
//        .filter(c => cluster.position.distance(c.position) < Constants.neighbourDistance)
//        .map(c => c.actorRef)
//      cluster.actorRef ! AddNeighbourClusters(neighbourClusters.toSet)
//    })
//  }
}

