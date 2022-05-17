package message

case class ActorInitActive(id: String, neighCount: Int, neighId: Set[String], messageId: String)
