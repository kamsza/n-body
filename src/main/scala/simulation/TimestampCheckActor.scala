package simulation

import akka.actor.{Actor, ActorRef}
import message.{BodyDataSave, SimulationFinish, SimulationInit, SimulationStart}
import utils.{CSVUtil, SimulationConstants, Vec2}

import scala.collection.mutable.ListBuffer

case class TimestampCheckActor(fileName: String, timeSteps: List[Int], simulationStepsCount: Int) extends Actor {
  var bodiesData = new ListBuffer[(String, BigDecimal, Vec2, Vec2, Int, Int)]()

  var bodiesCount = 0
  var finishedActorsCounter = 0

  var counter = 0
  var timeStep: Int = timeSteps(counter)

  CSVUtil.deleteFile("test_" + fileName.replaceAll("\\.[^.]*$", ".csv"))

  override def receive: Receive = {
    case SimulationInit() =>
      val bodies = CSVUtil.loadBodies(fileName, context.system)
      bodiesCount = bodies.length
      bodies.foreach(body => body ! SimulationStart(context.self, SimulationConstants(timeStep, simulationStepsCount, simulationStepsCount + 1)))

    case BodyDataSave(id, mass, position, velocity, messageId) =>
      bodiesData += Tuple6(id, mass, position, velocity, messageId, timeStep)

    case SimulationFinish() =>
      finishedActorsCounter += 1
      if(finishedActorsCounter == bodiesCount) {
        bodiesData = bodiesData.sortBy(data => (data._5, data._1))
        val outputFileName = "test_" + fileName.replaceAll("\\.[^.]*$", ".csv")
        CSVUtil.initCsvFile(outputFileName, false)
        CSVUtil.saveTestDataToFile(outputFileName, bodiesData.toList)

        counter += 1
        if(counter == timeSteps.length) {
          println(s"Finished: ${timeStep}")
          context.stop(self)
          context.system.terminate()
        } else {
          bodiesData.clear()
          println(s"Finished: ${timeStep}")
          finishedActorsCounter = 0
          timeStep = timeSteps(counter)
          context.self ! SimulationInit()
        }
      }
  }
}
