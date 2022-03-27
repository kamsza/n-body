package clustered

import akka.actor.ActorRef
import common.ClusterSimulationHandler
import message.{AddNeighbourClusters, ClusterReady, SimulationFinish, SimulationStart}

case class ClusteredSimulatorActor() extends ClusterSimulationHandler {

  override def receive: Receive = {
    case SimulationStart(clusters) => handleSimulationStart(clusters)
    case ClusterReady() => handleActorReady()
    case SimulationFinish() => handleSimulationFinish()
  }

  override def handleSimulationStart(clusters: List[ActorRef]): Unit = {
    super.handleSimulationStart(clusters)
    setNeighbours()
  }

  def setNeighbours(): Unit = {
    clusters.foreach(cluster => {
      val neighbourClusters = clusters.filterNot(_ == cluster)
      cluster ! AddNeighbourClusters(neighbourClusters.toSet)
    })
  }
}
