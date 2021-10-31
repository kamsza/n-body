package `object`

import utils.{CSVUtil, Vec2}

import scala.collection.mutable.ArrayBuffer

class Cluster(val bodies: ArrayBuffer[Body]) {
  def addBody(body: Body):Unit = bodies.append(body)

  def makeSimulationStep(): Unit = {
    bodies.foreach(body => bodies.foreach(body.applyForce))
    bodies.foreach(_.move())
  }

  def countCenterOfMass(): Vec2 = {
    val (massesSum, massesAndPositionsProduct) = bodies
      .map(b => (b.mass, b.position))
      .fold((BigDecimal(0), Vec2(BigDecimal(0), BigDecimal(0))))((b1, b2) => (b1._1 + b2._1, b1._2 + b2._2 * b2._1))
    massesAndPositionsProduct / massesSum
  }

  def moveSystemMassCenter(destination: Vec2): Unit = {
    bodies.foreach(_.changePosition(destination))
  }

  @Override
  def toList: List[(Int, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal)] = {
    bodies.sortBy(body => body.id).map(body => body.toTuple).toList
  }

  def saveData(csvFileName: String): Unit = CSVUtil.saveBodiesDataToFile(csvFileName, this.toList)

  override def toString: String = bodies.map(body => body.toString).mkString("\n")
}

object Cluster {
  def apply(): Cluster = new Cluster(ArrayBuffer[Body]())
  def apply(bodies: ArrayBuffer[Body]): Cluster = new Cluster(bodies)
}
