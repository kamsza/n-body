package message

import akka.actor.ActorRef

case class DividedInitialize(simulationController: ActorRef, progressMonitor: ActorRef, connectionManager: ActorRef)
