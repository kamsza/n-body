package clustered

import `object`.Object
import akka.actor.{Actor, ActorRef}
import message._
import utils.{CSVUtil, Vec2}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ClusterActor(
               id: String,
               mass: BigDecimal,
               startPosition: Vec2)
  extends Cluster(id, mass, startPosition) with Actor {

  val bodies: ArrayBuffer[Body]

  val neighbourClusters: ArrayBuffer[ActorRef] = ArrayBuffer[ActorRef]()

  val neighbourObjects: mutable.Map[String, Object] = mutable.Map()

  def this(id: String, bodies: ArrayBuffer[Body]) = {
    this(id, Cluster.countSummaryMass(bodies), Cluster.countCenterOfMass(bodies), bodies)
  }

  //def addBody(body: Body): Unit = bodies.append(body)

  //def addNeighbourCluster(cluster: Cluster): Unit = neighbourClusters.append(cluster)

  override def receive: Receive = {
    case MoveCluster(vector) => moveSystemMassCenter(vector)
    case SaveData(outputFile) => saveData(outputFile)
    case AddNeighbourClusters(clusters, simulationController) =>
      neighbourClusters.addAll(clusters)
      neighbourClusters.foreach(_ ! ClusterDataUpdate(id, mass, position))
      simulationController ! ClusterReady
    case MakeSimulation(count) => for(_ <- 0 to count) {
      makeSimulationStep()
      position = countCenterOfMass()
      neighbourClusters.foreach(_ ! ClusterDataUpdate(id, mass, position))
    }
    case ClusterDataUpdate(id, mass, position) =>
      neighbourObjects += (id -> Cluster(id, mass, position))
  }

  def makeSimulationStep(): Unit = {
    bodies.foreach(body => bodies.foreach(body.applyForce))
    bodies.foreach(body => neighbourObjects.values.foreach(body.applyForce))
    bodies.foreach(_.move())
  }

  def countCenterOfMass(): Vec2 = Cluster.countCenterOfMass(bodies)

  def countSummaryMass(): BigDecimal = Cluster.countSummaryMass(bodies)

  def moveSystemMassCenter(vector: Vec2): Unit = bodies.foreach(_.changePosition(vector))

  @Override
  def toList: List[(String, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal)] = {
    bodies.sortBy(body => body.id).map(body => body.toTuple).toList
  }

  def saveData(csvFileName: String): Unit = CSVUtil.saveBodiesDataToFile(csvFileName, this.toList)

  override def toString: String = bodies.map(body => body.toString).mkString("\n")
}

