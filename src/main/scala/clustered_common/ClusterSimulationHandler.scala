package clustered_common

import akka.actor.{ActorRef, Props}
import common.SimulationHandler
import message.{Initialize, ProgressMonitorInitialize, SayHello}
import utils.ProgressMonitor

abstract class ClusterSimulationHandler extends SimulationHandler {

  var clusters: List[ActorRef] = List()

  var progressMonitor: ActorRef = ActorRef.noSender

  override def actorsCount: Int = clusters.size

  override def actors: List[ActorRef] = clusters

  def handleSimulationStart(clusters: List[ActorRef]): Unit = {
    this.clusters = clusters
    this.progressMonitor = createProgressMonitor()
    clusters.foreach(cluster => cluster ! Initialize(context.self, progressMonitor))
  }

  def createProgressMonitor(): ActorRef = {
    val progressMonitor = context.actorOf(Props(classOf[ProgressMonitor], clusters.size), "progress_monitor")
    progressMonitor
  }
}
