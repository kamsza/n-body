package message

case class ActorInitActive(id: String, neighboursIds: Set[String], oldMessageId: String, newMessageId: String)
