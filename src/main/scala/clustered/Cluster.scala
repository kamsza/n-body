package clustered

import `object`.Object
import utils.Vec2

import scala.collection.mutable.ArrayBuffer

case class Cluster(
                    id: String,
                    mass: BigDecimal,
                    startPosition: Vec2)
  extends Object {

  var position: Vec2 = startPosition

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
