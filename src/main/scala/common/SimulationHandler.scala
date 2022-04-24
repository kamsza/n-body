package common

import akka.actor.{Actor, ActorRef}
import message.MakeSimulation

abstract class SimulationHandler extends Actor {

  var readyActorsCounter = 0

  var finishedActorsCounter = 0

  var startTime: Long = 0

  var endTime: Long = 0

  def actorsCount: Int

  def actors: Iterable[ActorRef]

  def handleActorReady(): Unit = {
    readyActorsCounter += 1
    if (readyActorsCounter.equals(actorsCount)) {
      actors.foreach(body => body ! MakeSimulation())
      println("simulation started")
      startTime = System.currentTimeMillis()
    }
  }

  def handleSimulationFinish(): Unit = {
    finishedActorsCounter += 1
    if (finishedActorsCounter.equals(actorsCount)) {
      endTime = System.currentTimeMillis()
      println(s"simulation finished in ${(endTime - startTime) / 1000.0}s")
      context.stop(self)
      context.system.terminate()
    }
  }
}
