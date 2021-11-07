package simulation

import akka.actor.{Actor, ActorRef}
import message.{AddNeighbourClusters, ClusterReady, SimulationStart}

case class SimulatorActor() extends Actor {

  var clusters: List[ActorRef] = List()

  var readyClustersCounter = 0

  override def receive: Receive = {
    case SimulationStart(clusters) =>
      this.clusters = clusters
      clusters.foreach(cluster => {
        val neighbourClusters = clusters.filterNot(_ == cluster)
        cluster ! AddNeighbourClusters(neighbourClusters, context.self)
      })
    case ClusterReady =>
      readyClustersCounter += 1
  }
}
