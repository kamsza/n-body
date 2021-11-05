package `object`

import akka.actor.Actor
import akka.actor.Actor.Receive
import message.{ClusterDataUpdate, SimulationStart}
import utils.{CSVUtil, Vec2}

import scala.collection.mutable.ArrayBuffer

case class Cluster(
                  id: String,
                  mass: BigDecimal,
                  bodies: ArrayBuffer[Body] = ArrayBuffer[Body]())
  extends Object {
 //extends Actor

  override var position: Vec2 = _

  def this(id: String, bodies: ArrayBuffer[Body]) = {
    this(id, Cluster.countSummaryMass(bodies), bodies)
    this.position = Cluster.countCenterOfMass(bodies)
  }

  def addBody(body: Body): Unit = bodies.append(body)

  // def addNeighbourCluster(cluster: Cluster): Unit = neighbourClusters.append(cluster)

  def moveCluster(vector: Vec2): Unit = bodies.foreach(_.changePosition(vector))

//  override def receive: Receive = {
//    case SimulationStart =>
//      for(_ <- 1 to 6000) {
//        makeSimulationStep()
//      }
//    case ClusterDataUpdate(id, mass, position) =>
//    // TODO: zupdatuj
//  }

  def makeSimulationStep(): Unit = {
    bodies.foreach(body => bodies.foreach(body.applyForce))
    // bodies.foreach(body => neighbourClusters.foreach(body.applyForce))
    bodies.foreach(_.move())
  }

  def countCenterOfMass(): Vec2 = Cluster.countCenterOfMass(bodies)

  def countSummaryMass(): BigDecimal = Cluster.countSummaryMass(bodies)

  def moveSystemMassCenter(destination: Vec2): Unit = {
    bodies.foreach(_.changePosition(destination))
  }

  @Override
  def toList: List[(String, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal)] = {
    bodies.sortBy(body => body.id).map(body => body.toTuple).toList
  }

  def saveData(csvFileName: String): Unit = CSVUtil.saveBodiesDataToFile(csvFileName, this.toList)

  override def toString: String = bodies.map(body => body.toString).mkString("\n")
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
