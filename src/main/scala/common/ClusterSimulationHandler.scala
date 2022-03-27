package common

import akka.actor.{Actor, ActorRef, Props}
import message.{AddNeighbourClusters, Initialize, MakeSimulation, SayHello}
import utils.ProgressMonitor

abstract class ClusterSimulationHandler extends SimulationHandler {

  var clusters: List[ActorRef] = List()

  override def actorsCount: Int = clusters.size

  override def actors: List[ActorRef] = clusters

  def handleSimulationStart(clusters: List[ActorRef]): Unit = {
    this.clusters = clusters
    val progressMonitor = createProgressMonitor()
    clusters.foreach(cluster => cluster ! Initialize(context.self, progressMonitor))
  }

  def createProgressMonitor(): ActorRef = {
    val progressMonitor = context.actorOf(Props(classOf[ProgressMonitor], clusters.size), "progress_monitor")
    progressMonitor ! SayHello()
    progressMonitor
  }
}
