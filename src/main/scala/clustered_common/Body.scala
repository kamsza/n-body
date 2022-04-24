package clustered_common

import `object`.{AbstractBody, Object}
import math.Vec2

class Body(
            id: String,
            mass: BigDecimal,
            startPosition: Vec2,
            startVelocity: Vec2
          ) extends AbstractBody(id, mass, startPosition, startVelocity) {

  def changePosition(changeVec: Vec2): Unit = this.position = this.position + changeVec

  def findNewCluster(currDist: BigDecimal, neighbours: Set[Object]): Option[Object] = {
    val neighbourDescriptors = neighbours.map(neighbour => (neighbour, neighbour.position.distance(this.position)))
      .filter(neighbourDescriptor => neighbourDescriptor._2 < currDist)
      .toList
      .sortBy(_._2)

    if (neighbourDescriptors.isEmpty) {
      return Option.empty
    }
    Option(neighbourDescriptors.head._1)
  }
}
