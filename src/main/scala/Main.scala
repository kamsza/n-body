import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Main extends App {
  val system = ActorSystem("N-BodySystem")

  var bodies: Map[Int, ActorRef] = Map()
  var bodiesCount: Int = 5
  val rand = new scala.util.Random

  bodies += 0 ->
      system.actorOf(
        Props(classOf[Body],
          bodiesCount,
          0,
          1.9890e+30,
          Vector(0.0, 0.0),
          Vector(0.0, 0.0)
        ), name = "sun")

  bodies += 1 ->
    system.actorOf(
      Props(classOf[Body],
        bodiesCount,
        1,
        5.9740e+24,
        Vector(1.4960e+11, 0.0000e+00),
        Vector(0.0000e+00, 2.9800e+04)
      ), name = "earth")

  bodies += 2 ->
    system.actorOf(
      Props(classOf[Body],
        bodiesCount,
        2,
        6.4190e+23,
        Vector(2.2790e+11, 0.0000e+00),
        Vector(0.0000e+00, 2.4100e+04)
      ), name = "mars")

  bodies += 3 ->
    system.actorOf(
      Props(classOf[Body],
        bodiesCount,
        3,
        3.3020e+23,
        Vector(5.7900e+10, 0.0000e+00),
        Vector(0.0000e+00, 4.7900e+04)
      ), name = "mercury")

  bodies += 4 ->
    system.actorOf(
      Props(classOf[Body],
        bodiesCount,
        4,
        4.8690e+24,
        Vector(1.0820e+11, 0.0000e+00),
        Vector(0.0000e+00, 3.5000e+04)
      ), name = "venus")

  val simulationActor = system.actorOf(Props(classOf[Simulation], bodiesCount), name = "simulationactor")
  simulationActor ! StartSimulation
}