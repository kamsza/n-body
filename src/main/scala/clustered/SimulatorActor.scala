package clustered

import akka.actor.{Actor, ActorRef, Props}
import message.{ActivateProgressMonitor, AddNeighbourClusters, ClusterReady, MakeSimulation, SayHello, SimulationFinish, SimulationStart}
import utils.ProgressMonitor

case class SimulatorActor() extends Actor {

  var clusters: List[ActorRef] = List()

  var readyClustersCounter = 0

  var finishedActorsCounter = 0

  override def receive: Receive = {
    case SimulationStart(clusters) =>
      this.clusters = clusters

      val progressMonitor = context.actorOf(Props(classOf[ProgressMonitor], clusters.size), "progress_monitor")
      progressMonitor ! SayHello()

      clusters.foreach(cluster => {
        val neighbourClusters = clusters.filterNot(_ == cluster)
        cluster ! AddNeighbourClusters(neighbourClusters, context.self)
        cluster ! ActivateProgressMonitor(progressMonitor)
      })
    case ClusterReady =>
      readyClustersCounter += 1
      if(readyClustersCounter == clusters.size){
        clusters.foreach(cluster => cluster ! MakeSimulation())
        println("SIMULATION STARTED")
      }
    case SimulationFinish =>
      finishedActorsCounter += 1
      if(finishedActorsCounter == clusters.size) {
        context.stop(self)
        context.system.terminate()
      }
  }
}
