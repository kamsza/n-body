package message

import akka.actor.ActorRef

case class ActivateProgressMonitor(progressMonitor: ActorRef)
