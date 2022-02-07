package clustered

import akka.actor.{Actor, ActorRef}
import message.{AddNeighbourClusters, ClusterReady, MakeSimulation, SimulationFinish, SimulationStart}

case class SimulatorActor() extends Actor {

  var clusters: List[ActorRef] = List()

  var readyClustersCounter = 0

  var finishedActorsCounter = 0

  override def receive: Receive = {
    case SimulationStart(clusters) =>
      println(clusters)
      this.clusters = clusters
      clusters.foreach(cluster => {
        val neighbourClusters = clusters.filterNot(_ == cluster)
        cluster ! AddNeighbourClusters(neighbourClusters, context.self)
      })
    case ClusterReady =>
      readyClustersCounter += 1
      println("READY")
      if(readyClustersCounter == clusters.size){
        println("ALL READY")
        clusters.foreach(cluster => cluster ! MakeSimulation(10))
      }
    case SimulationFinish =>
      println("FINISHED")
      finishedActorsCounter += 1
      if(finishedActorsCounter == clusters.size) {
        context.stop(self)
        context.system.terminate()
        println("SimulatorActor FINISHED")
      }
  }
}
