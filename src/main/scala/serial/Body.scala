package serial

import `object`.AbstractBody
import math.Vec2

class Body(
            id: String,
            mass: Double,
            startPosition: Vec2,
            startVelocity: Vec2
          ) extends AbstractBody(id, mass, startPosition, startVelocity) {}
