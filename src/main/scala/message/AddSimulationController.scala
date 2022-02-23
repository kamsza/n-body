package message

import akka.actor.ActorRef

case class AddSimulationController(simulationController: ActorRef)
