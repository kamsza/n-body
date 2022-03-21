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
}

object Cluster {
  def countCenterOfMass(bodies: ArrayBuffer[Body]): Vec2 = {
    val (massesSum, massesAndPositionsProduct) = bodies
      .map(b => (b.mass, b.position))
      .reduce((acc, curr) => (acc._1 + curr._1, acc._2 + curr._2 * curr._1))
    massesAndPositionsProduct / massesSum
  }

  def countSummaryMass(bodies: ArrayBuffer[Body]): BigDecimal = bodies.map(b => b.mass).sum
}
