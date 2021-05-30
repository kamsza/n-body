import akka.actor.{ActorRef, ActorSystem, Props}

object ThreeBody extends App {
  val system = ActorSystem("N-BodySystem")

  var bodies: Map[Int, ActorRef] = Map()
  var bodiesCount: Int = 3
  val rand = new scala.util.Random

  bodies += 0 ->
    system.actorOf(
      Props(classOf[Body],
        bodiesCount,
        0,
        5.974e24,
        Vector(0.0, 0.0),
        Vector(0.05e04, 0.0)
      ), name = "body1")

  bodies += 1 ->
    system.actorOf(
      Props(classOf[Body],
        bodiesCount,
        1,
        1.989e30,
        Vector(0.0, 4.50e10),
        Vector(3.00e04, 0.0)
      ), name = "body2")

  bodies += 2 ->
    system.actorOf(
      Props(classOf[Body],
        bodiesCount,
        2,
        1.989e30,
        Vector(0.0, -4.50e10),
        Vector(-3.00e04, 0.0)
      ), name = "body3")

  val simulationActor = system.actorOf(Props(classOf[Simulation], bodiesCount), name = "simulationactor")
  simulationActor ! StartSimulation
}
