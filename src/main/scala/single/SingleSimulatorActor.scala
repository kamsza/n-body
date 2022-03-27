package single

import akka.actor.{Actor, ActorRef, Props}
import common.SimulationHandler
import message.{ActivateProgressMonitor, ActorReady, AddNeighbourBodies, AddSimulationController, MakeSimulation, SayHello, SimulationFinish, SimulationStart}
import utils.ProgressMonitor

case class SingleSimulatorActor() extends SimulationHandler {

  var bodies: List[ActorRef] = List()

  override def actorsCount: Int = bodies.size

  override def actors: List[ActorRef] = bodies

  override def receive: Receive = {
    case SimulationStart(bodies) => handleSimulationStart(bodies)
    case ActorReady() => handleActorReady()
    case SimulationFinish() => handleSimulationFinish()
  }

  def handleSimulationStart(bodies: List[ActorRef]): Unit = {
    this.bodies = bodies

    val progressMonitor = context.actorOf(Props(classOf[ProgressMonitor], bodies.size), "progress_monitor")
    progressMonitor ! SayHello()

    bodies.foreach(body => {
      val neighbourBodies = bodies.filterNot(_ == body)
      body ! AddNeighbourBodies(neighbourBodies, context.self)
      body ! ActivateProgressMonitor(progressMonitor)
    })
  }
}
