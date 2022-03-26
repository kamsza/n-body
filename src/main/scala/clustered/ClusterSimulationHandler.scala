package clustered

import akka.actor.{Actor, ActorRef, Props}
import message.{AddNeighbourClusters, Initialize, MakeSimulation, SayHello}
import utils.ProgressMonitor

abstract class ClusterSimulationHandler extends Actor {

  var clusters: List[ActorRef] = List()

  var readyClustersCounter = 0

  var finishedActorsCounter = 0

  var startTime: Long = 0

  var endTime: Long = 0

  def handleSimulationStart(clusters: List[ActorRef]): Unit = {
    this.clusters = clusters

    val progressMonitor = context.actorOf(Props(classOf[ProgressMonitor], clusters.size), "progress_monitor")
    progressMonitor ! SayHello()

    clusters.foreach(cluster => {
      val neighbourClusters = clusters.filterNot(_ == cluster)
      cluster ! Initialize(context.self, progressMonitor)
      cluster ! AddNeighbourClusters(neighbourClusters.toSet)
    })
  }

  def handleClusterReady(): Unit = {
    readyClustersCounter += 1
    if(readyClustersCounter == clusters.size){
      clusters.foreach(cluster => cluster ! MakeSimulation())
      println("SIMULATION STARTED")
      startTime = System.nanoTime()
    }
  }

  def handleSimulationFinished(): Unit = {
    finishedActorsCounter += 1
    if(finishedActorsCounter == clusters.size) {
      endTime = System.nanoTime()
      println("Simulation time: " + (endTime - startTime) + "ns")
      context.stop(self)
      context.system.terminate()
    }
  }
}
