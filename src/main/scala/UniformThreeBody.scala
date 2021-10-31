import akka.actor.{ActorRef, ActorSystem, Props}

object UniformThreeBody extends App {
  val system = ActorSystem("N-BodySystem")

  var bodies: Map[Int, ActorRef] = Map()
  var bodiesCount: Int = 3
  val rand = new scala.util.Random

  bodies += 0 ->
    system.actorOf(
      Props(classOf[Body],
        bodiesCount,
        0,
        8.00e+23,
        Vector(5.000000e+08, 0.0),
        Vector(0.0, -2.482233e+02)
      ), name = "body1")

  bodies += 1 ->
    system.actorOf(
      Props(classOf[Body],
        bodiesCount,
        1,
        8.00e+23,
        Vector(-2.500000e+08, 4.330127e+08),
        Vector(2.149677e+02, 1.241117e+02)
      ), name = "body2")

  bodies += 2 ->
    system.actorOf(
      Props(classOf[Body],
        bodiesCount,
        2,
        8.00e+23,
        Vector(-2.500000e+08, -4.330127e+08),
        Vector(-2.149677e+02, 1.241117e+02)
      ), name = "body3")

  val simulationActor = system.actorOf(Props(classOf[Simulation], bodiesCount), name = "simulationactor")
  simulationActor ! StartSimulation
}
