package utils

import clustered_common.Body
import math.Vec2

import scala.collection.mutable.ArrayBuffer

object PhysicsUtil {
  def countCenterOfMass(bodies: Iterable[Body]): Vec2 = {
    val (massesSum, massesAndPositionsProduct) = bodies
      .map(b => (b.mass, b.position))
      .reduce((acc, curr) => (acc._1 + curr._1, acc._2 + curr._2 * curr._1))
    massesAndPositionsProduct / massesSum
  }

  def countSummaryMass(bodies: Iterable[Body]): BigDecimal = bodies.map(b => b.mass).sum
}
