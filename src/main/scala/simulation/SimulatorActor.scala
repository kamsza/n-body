package simulation

import akka.actor.Actor
import message.{BodyDataSave, SimulationFinish, SimulationInit, SimulationStart}
import utils.{CSVUtil, SimulationConstants, Vec2}

import scala.collection.mutable.ListBuffer

case class SimulatorActor(fileName: String) extends Actor {

  var bodiesData = new ListBuffer[(String, BigDecimal, Vec2, Vec2, Int)]()

  var bodiesCount = 0
  var finishedActorsCounter = 0

  val progressMarker: ProgressMarker = ProgressMarker()

  override def receive: Receive = {
    case SimulationInit() =>
      val bodies = CSVUtil.loadBodies(fileName, context.system)
      bodiesCount = bodies.length
      bodies.foreach(body => body ! SimulationStart(context.self))
    case BodyDataSave(id, mass, position, velocity, messageId) =>
      bodiesData += Tuple5(id, mass, position, velocity, messageId)
      progressMarker.updateProgress(id)
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


case class ProgressMarker(
  simulationConstants: SimulationConstants = SimulationConstants()
){
  var step = 0
  val stepsCount: Int = simulationConstants.simulationStepsCount / simulationConstants.communicationStep
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
