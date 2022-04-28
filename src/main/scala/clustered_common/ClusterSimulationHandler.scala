package clustered_common

import akka.actor.{ActorRef, Props}
import common.{ActorDescriptor, SimulationHandler}
import message.Initialize
import utils.ProgressMonitor

abstract class ClusterSimulationHandler extends SimulationHandler {

  var clusters: Set[ActorDescriptor] = Set()

  var progressMonitor: ActorRef = createProgressMonitor()

  override def actorsCount: Int = clusters.size

  override def actors: Set[ActorRef] = clusters.map(c => c.actorRef)

  def handleSimulationStart(clusters: Set[ActorDescriptor]): Unit = {
    this.clusters = clusters
    initializeClusters()
  }

  def initializeClusters(): Unit = {
    clusters.foreach(cluster =>
      cluster.actorRef ! Initialize(context.self, progressMonitor)
    )
  }

  def createProgressMonitor(): ActorRef = {
    val progressMonitor =
      context.actorOf(Props(classOf[ProgressMonitor]), "progress_monitor")
    progressMonitor
  }
}
