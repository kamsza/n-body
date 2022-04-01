package utils

import clustered_common.Body
import math.Vec2

import scala.collection.mutable.ArrayBuffer

object PhysicsUtil {
  def countCenterOfMass(bodies: Iterable[Body]): Vec2 = {
    val startElement = (BigDecimal("0"), Vec2(BigDecimal("0"), BigDecimal("0")))
    val (massesSum, massesAndPositionsProduct) = bodies
      .map(b => (b.mass, b.position))
      .fold(startElement)((acc, curr) => (acc._1 + curr._1, acc._2 + curr._2 * curr._1))
    massesAndPositionsProduct / massesSum
  }

  def countSummaryMass(bodies: Iterable[Body]): BigDecimal = bodies.map(b => b.mass).sum
}
