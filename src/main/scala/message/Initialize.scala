package message

import akka.actor.ActorRef

case class Initialize(simulationController: ActorRef, progressMonitor: ActorRef)
