package message

import utils.Vec2

case class BodyDataSave(id: String, mass: BigDecimal, position: Vec2, velocity: Vec2, msgId: Int)