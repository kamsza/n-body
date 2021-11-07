import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

class HelloActor extends Actor {
  def receive = {
    case "hello" => println("hello back at you")
    case _       => println("huh?")
  }
}


class A (val a: Int, var startB: Int) {
  var b: Int = startB
  override def toString: String = s"($a , $b)"
}

class B (a: Int, startB: Int) extends A(a, startB) {
  def x(): Unit = {
    b = b + 2
  }


}

object Main extends App {
  val b = new B(1, 2)
  println(b)
  b.x()
  println(b)
  b.x()
  println(b)
  b.x()
  println(b)
  b.x()
  println(b)
//  val system = ActorSystem("HelloSystem")
//  // default Actor constructor
//  val helloActor = system.actorOf(Props[HelloActor], name = "helloactor")
//  helloActor ! "hello"
//  helloActor ! "buenos dias"
}