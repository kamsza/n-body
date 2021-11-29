package simulation

import akka.actor.{ActorSystem, Props}
import message.SimulationInit

object Simulator extends App {
//  val timeSteps = Seq.iterate(1000, 10)(_ + 1000)
//  val simulationStepsCount = 100000
  val system = ActorSystem("N-BodySystem")
  val simulationActor = system.actorOf(Props(classOf[SimulatorActor], "output.txt"), name = "simulationactor")
//  val simulationActor = system.actorOf(
//    Props(
//      classOf[TimestampCheckActor],
//      "solar_system.txt",
//      timeSteps,
//      simulationStepsCount
//    ), name = "simulationactor")
  simulationActor ! SimulationInit()
}
