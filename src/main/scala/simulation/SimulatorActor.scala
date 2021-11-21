package simulation

import akka.actor.Actor
import message.{BodyDataSave, BodyDataUpdate, SimulationFinish, SimulationInit, SimulationStart}
import simulation.Simulator.system
import utils.{CSVUtil, Constants, Vec2}

import scala.collection.mutable.ListBuffer

case class SimulatorActor(fileName: String) extends Actor {

  var bodiesData = new ListBuffer[(String, BigDecimal, Vec2, Vec2, Int)]()

  var bodiesCount = 0
  var finishedActorsCounter = 0

  override def receive: Receive = {
    case SimulationInit() =>
      val bodies = CSVUtil.loadBodies(fileName, context.system)
      bodiesCount = bodies.length
      bodies.foreach(body => body ! SimulationStart(context.self))
    case BodyDataSave(id, mass, position, velocity, messageId) =>
      bodiesData += Tuple5(id, mass, position, velocity, messageId)
      ProgressMarker.updateProgress(id)
    case SimulationFinish() =>
      finishedActorsCounter += 1
      if(finishedActorsCounter == bodiesCount) {
        bodiesData = bodiesData.sortBy(data => (data._5, data._1))
        val outputFileName = fileName.replaceAll("\\.[^.]*$", ".csv")
        CSVUtil.initCsvFile(outputFileName)
        CSVUtil.saveBodiesDataToFile(outputFileName, bodiesData.toList)
        context.stop(self)
        context.system.terminate()
        println("FINISHED")
      }
  }
}


object ProgressMarker {
  var step = 0
  val stepsCount: Int = Constants.simulationStepsCount / Constants.communicationStep
  val markersCount = 10

  def updateProgress(bodyId: String): Unit = {
    if(bodyId == "0") {
      val stepsPerUpdate = stepsCount / markersCount
      if(stepsPerUpdate != 0 && step % stepsPerUpdate == 0) {
        val partDone = step / stepsPerUpdate
        val partToDo = markersCount - partDone
        println(s"[${"X" * partDone}${"-" * partToDo}] ${partDone}/${markersCount}")
      }
      step += 1
    }


  }
}
