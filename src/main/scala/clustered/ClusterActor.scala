package clustered

import `object`.Object
import akka.actor.{Actor, ActorRef}
import message._
import utils.CSVUtil.DELIMITER
import utils.{CSVUtil, SimulationConstants, Vec2}

import java.io.BufferedWriter
import java.nio.file.Path
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ClusterActor(
               id: String,
               mass: BigDecimal,
               startPosition: Vec2,
               resultsFileWriter: BufferedWriter,
               bodies: ArrayBuffer[Body] = ArrayBuffer[Body](),
               neighbourClusters: ArrayBuffer[ActorRef] = ArrayBuffer[ActorRef](),
               neighbourObjects: mutable.Map[String, Object] = mutable.Map())
  extends Cluster(id, mass, startPosition) with Actor {

  var stepsCounter: Int = SimulationConstants.simulationStepsCount

  var managingActor: ActorRef = ActorRef.noSender

  var receivedMessagesCounter: Int = 0

  def this(id: String, bodies: ArrayBuffer[Body], resultsFilePath: Path) = {
    this(id, Cluster.countSummaryMass(bodies), Cluster.countCenterOfMass(bodies), CSVUtil.initCsvFile(resultsFilePath))
    this.bodies.addAll(bodies)
  }

  override def receive: Receive = {
    case AddNeighbourClusters(clusters, simulationController) =>
      managingActor = simulationController
      neighbourClusters.addAll(clusters)
      managingActor ! ClusterReady
    case MakeSimulation() =>
      makeSimulationStep()
      sendUpdate()
    case ClusterDataUpdate(id, mass, position) =>
      receivedMessagesCounter += 1
      neighbourObjects += (id -> Cluster(id, mass, position))                     // TODO: additionally check message id
      if(receivedMessagesCounter == neighbourClusters.size) {                     // TODO: additionally check timestamp between last msg and current, if is big, update
        receivedMessagesCounter = 0
        makeSimulationStep()
        doOnSimulationStepAction(stepsCounter)
        sendUpdate()
      }
  }

  def makeSimulationStep(): Unit = {
    stepsCounter -= 1
    updateBodiesPosition()
    position = countCenterOfMass()
  }

  def updateBodiesPosition(): Unit = {
    bodies.foreach(body => bodies.foreach(body.applyForce))
    bodies.foreach(body => neighbourObjects.values.foreach(body.applyForce))
    bodies.foreach(_.move())
  }

  def doOnSimulationStepAction(stepsCounter: Int): Unit = stepsCounter match {
    case 0 => finish()
    case _ if stepsCounter % SimulationConstants.communicationStep == 0 => writeDataToFile()
    case _ => // DO NOTHING
  }

  def writeDataToFile(): Unit = {
    val dataString = bodies
      .map(body => body.toTuple)
      .map(tuple => tuple.productIterator.mkString(DELIMITER))
      .mkString("\n")
    resultsFileWriter.write(s"\n${dataString}")
  }

  def sendUpdate(): Unit = neighbourClusters.foreach(_ ! ClusterDataUpdate(id, mass, position))

  def countCenterOfMass(): Vec2 = Cluster.countCenterOfMass(bodies)

  def countSummaryMass(): BigDecimal = Cluster.countSummaryMass(bodies)

  def moveSystemMassCenter(vector: Vec2): Unit = bodies.foreach(_.changePosition(vector))

  @Override
  def toList: List[(String, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal)] = {
    bodies.sortBy(body => body.id).map(body => body.toTuple).toList
  }

  override def toString: String = bodies.map(body => body.toString).mkString("\n")

  def finish(): Unit = {
    resultsFileWriter.close()
    managingActor ! SimulationFinish
    context.stop(self)
  }
}

