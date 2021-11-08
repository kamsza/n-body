package `object`

import akka.actor.Actor

trait Object extends Actor { // TODO

  def subscribedClasses: Seq[Class[_]]

  override def preStart() {
    super.preStart()
    subscribedClasses.foreach(this.context.system.eventStream.subscribe(this.self, _))
  }

  override def postStop() {
    subscribedClasses.foreach(this.context.system.eventStream.unsubscribe(this.self, _))
    super.postStop()
  }
}
