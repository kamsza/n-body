package single

import akka.actor.{Actor, ActorRef, Props}
import common.{ActorDescriptor, SimulationHandler}
import message.{ActivateProgressMonitor, ActorReady, AddNeighbourBodies, AddSimulationController, MakeSimulation, ProgressMonitorInitialize, SayHello, SimulationFinish, SimulationStart}
import utils.ProgressMonitor

case class SingleSimulatorActor() extends SimulationHandler {

  var bodies: Set[ActorDescriptor] = Set()

  override def actorsCount: Int = bodies.size

  override def actors: Set[ActorRef] = bodies.map(b => b.actorRef)

  override def receive: Receive = {
    case SimulationStart(bodies) => handleSimulationStart(bodies)
    case ActorReady() => handleActorReady()
    case SimulationFinish() => handleSimulationFinish()
  }

  def handleSimulationStart(bodies: Set[ActorDescriptor]): Unit = {
    this.bodies = bodies

    val progressMonitor = context.actorOf(Props(classOf[ProgressMonitor], bodies.size), "progress_monitor")
    progressMonitor ! ProgressMonitorInitialize(bodies.map(b => b.id))

    bodies.foreach(body => {
      val neighbourBodies = bodies
        .filterNot(_.id == body.id)
        .map(b => b.actorRef)
      body.actorRef ! AddNeighbourBodies(neighbourBodies, context.self)
      body.actorRef ! ActivateProgressMonitor(progressMonitor)
    })
  }
}
