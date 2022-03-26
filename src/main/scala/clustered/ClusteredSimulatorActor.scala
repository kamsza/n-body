package clustered

import message.{ClusterReady, SimulationFinish, SimulationStart}

case class ClusteredSimulatorActor() extends ClusterSimulationHandler {

  override def receive: Receive = {
    case SimulationStart(clusters) => handleSimulationStart(clusters)
    case ClusterReady => handleClusterReady()
    case SimulationFinish => handleSimulationFinished()
  }
}
