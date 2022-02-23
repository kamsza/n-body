package clustered

import `object`.{Object, AbstractBody}
import utils.{Constants, Vec2}

class Body(
            id: String,
            mass: BigDecimal,
            startPosition: Vec2,
            startVelocity: Vec2
) extends AbstractBody(id, mass, startPosition, startVelocity) {

  def changePosition(changeVec: Vec2): Unit = this.position = this.position + changeVec
}
