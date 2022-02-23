package single

import akka.actor.{Actor, ActorRef, Props}
import message.{ActivateProgressMonitor, ActorReady, AddNeighbourBodies, AddSimulationController, MakeSimulation, SayHello, SimulationFinish, SimulationStart}
import utils.ProgressMonitor

case class SimulatorActor() extends Actor {

  var bodies: List[ActorRef] = List()

  var readyBodiesCounter = 0

  var finishedActorsCounter = 0

  override def receive: Receive = {
    case SimulationStart(bodies) =>
      this.bodies = bodies

      val progressMonitor = context.actorOf(Props(classOf[ProgressMonitor], bodies.size), "progress_monitor")
      progressMonitor ! SayHello()

      bodies.foreach(body => {
        val neighbourBodies = bodies.filterNot(_ == body)
        body ! AddNeighbourBodies(neighbourBodies, context.self)
        body ! ActivateProgressMonitor(progressMonitor)
      })
    case ActorReady() =>
      readyBodiesCounter += 1
      if (readyBodiesCounter == bodies.size) {
        bodies.foreach(body => body ! MakeSimulation())
        println("SIMULATION STARTED")
      }
    case SimulationFinish =>
      finishedActorsCounter += 1
      if(finishedActorsCounter == bodies.size) {
        context.stop(self)
        context.system.terminate()
      }
  }
}
