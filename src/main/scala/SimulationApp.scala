import akka.actor.{ActorSystem, Props}
import clustered.ClusteredSimulatorActor
import divided.DividedSimulatorActor
import message.SimulationStart
import single.SingleSimulatorActor
import utils.SimulatingActorFactory

/**
 * SBT application expecting 2 or 3 arguments: simulation_type input_path output_path
 * simulation_type  one of three options: s - single, c - clustered, d - divided
 * input_path       path to files with bodies data, for single simulation one file is expected,
 *                  for clustered simulation path to directory with files, each file representing one cluster
 * output_path      path to directory in which output data should be stored, optional parameter
 * e.g. sbt "run s src/main/resources/test/test.csv results/test"
 */
object SimulationApp extends App {
  val simulationType = args(0)

  val inputPath = args(1)

  val outputPath = if (args.length > 2) Some(args(2)) else None

  val system = ActorSystem("N-BodySystem")

  val simulatingActors = simulationType match {
    case "s" => SimulatingActorFactory.loadBodiesActors(inputPath, outputPath, system)
    case "c" => SimulatingActorFactory.loadClusters(inputPath, outputPath, system, classOf[clustered.ClusterActor])
    case "d" => SimulatingActorFactory.loadClusters(inputPath, outputPath, system, classOf[divided.ClusterActor])
  }

  val simulationManagingActor = simulationType match {
    case "s" => system.actorOf(Props(classOf[SingleSimulatorActor]))
    case "c" => system.actorOf(Props(classOf[ClusteredSimulatorActor]))
    case "d" => system.actorOf(Props(classOf[DividedSimulatorActor]))
  }

  simulationManagingActor ! SimulationStart(simulatingActors)
}
