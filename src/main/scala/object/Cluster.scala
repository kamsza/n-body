package `object`

import akka.actor.{Actor, ActorRef}
import message.{AddNeighbourClusters, ClusterDataUpdate, ClusterReady, MakeSimulation, MoveCluster, SaveData, SimulationStart}
import utils.{CSVUtil, Vec2}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Cluster(
                val id: String,
                val mass: BigDecimal,
               val bodies: ArrayBuffer[Body] = ArrayBuffer[Body](),
               val neighbourClusters: ArrayBuffer[ActorRef] = ArrayBuffer[ActorRef](),
               val neighbourObjects: mutable.Map[String, Object] = mutable.Map())
  extends Actor {

   var position: Vec2 = _

  def this(id: String, bodies: ArrayBuffer[Body]) = {
    this(id, Cluster.countSummaryMass(bodies), bodies)
    this.position = Cluster.countCenterOfMass(bodies)
  }

  def addBody(body: Body): Unit = bodies.append(body)

  // def addNeighbourCluster(cluster: Cluster): Unit = neighbourClusters.append(cluster)

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
      neighbourObjects += (id -> Object(id, mass, position))
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

  override def equals(obj: Any): Boolean = {
    obj match {
      case c: Cluster => id == c.id
      case _ => false
    }
  }
}

object Cluster {
  def countCenterOfMass(bodies: ArrayBuffer[Body]): Vec2 = {
    val (massesSum, massesAndPositionsProduct) = bodies
      .map(b => (b.mass, b.position))
      .fold((BigDecimal(0), Vec2(BigDecimal(0), BigDecimal(0))))((b1, b2) => (b1._1 + b2._1, b1._2 + b2._2 * b2._1))
    massesAndPositionsProduct / massesSum
  }

  def countSummaryMass(bodies: ArrayBuffer[Body]): BigDecimal = bodies.map(b => b.mass).sum
}
