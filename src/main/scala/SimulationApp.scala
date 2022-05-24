import SimulationApp.inputPath
import akka.actor.{ActorRef, ActorSystem, Props}
import clustered.ClusteredSimulatorActor
import common.ActorDescriptor
import constant.SimulationConstants
import divided.DividedSimulatorActor
import message.SimulationStart
import single.SingleSimulatorActor
import utils.{ClusterFactory, CsvObjectFactory, JsonObjectFactory}

/** SBT application expecting 2 or 3 arguments: <simulation_type> <input_path> <output_path (optional)>
  * simulation_type  one of three options: s - single, c - clustered, d - divided
  * input_path       path to files with bodies data, for single simulation one file is expected,
  *                  for clustered simulation path to directory with files, each file representing one cluster
  * output_path      path to directory in which output data should be stored, optional parameter, if not present
  *                   no data will be saved
  * e.g. sbt "run s src/main/resources/test/test.csv results/test"
  *      sbt "run c src/main/resources/test results/test"
  */
object SimulationApp extends App {

  def loadObjects(simulationType: String, inputPath: String, outputPath: Option[String]): Set[ActorDescriptor] = {
    if(inputPath.endsWith(".json"))
      loadObjectsFormJson(simulationType, inputPath, outputPath)
    else
      loadObjectsFormCsv(simulationType, inputPath, outputPath)
  }

  def loadObjectsFormCsv(simulationType: String, inputPath: String, outputPath: Option[String]): Set[ActorDescriptor] = simulationType match {
    case "s" => CsvObjectFactory.loadBodiesActors(inputPath, outputPath, system)
    case "c" => CsvObjectFactory.loadClusters(inputPath, outputPath, system, classOf[clustered.ClusterActor])
    case "d" => CsvObjectFactory.loadClusters(inputPath, outputPath, system, classOf[divided.ClusterActor])
  }

  def loadObjectsFormJson(simulationType: String, inputPath: String, outputPath: Option[String]): Set[ActorDescriptor] = simulationType match {
    case "s" => JsonObjectFactory.parseJsonWithBodies(inputPath, outputPath, system)
    case "c" => JsonObjectFactory.parseJsonWithClusters(inputPath, outputPath, system, classOf[clustered.ClusterActor])
    case "d" => JsonObjectFactory.parseJsonWithClusters(inputPath, outputPath, system, classOf[divided.ClusterActor])
  }

  def getSimulationManagingActor(simulationType: String): ActorRef = simulationType match {
    case "s" => system.actorOf(Props(classOf[SingleSimulatorActor]))
    case "c" => system.actorOf(Props(classOf[ClusteredSimulatorActor]))
    case "d" => system.actorOf(Props(classOf[DividedSimulatorActor]))
  }

  def printSimulationData(simulationType: String): Unit = println(
    s"""===================== Simulation Data =====================
      |simulation type: ${simulationType}
      |
      |dt: ${SimulationConstants.dt}
      |simulation steps count: ${SimulationConstants.simulationStepsCount}
      |softening parameter: ${SimulationConstants.softeningParameter}
      |
      |communication step: ${SimulationConstants.communicationStep}
      |bodies affiliation check: ${SimulationConstants.bodiesAffiliationCheck}
      |cluster neighbours check: ${SimulationConstants.clusterNeighboursCheck}
      |
      |min neighbours count: ${SimulationConstants.minNeighboursCount}
      |simulating actors count: ${SimulationConstants.simulatingActorsCount}
      |bodies per cluster: ${SimulationConstants.bodiesPerClusterCount}
      |===========================================================
      |""".stripMargin
  )

  val simulationType = args(0) // "d"

  val inputPath = args(1) // solar_systems"

  val outputPath = if (args.length > 2) Some(args(2)) else None  // Some("F:\\magisterka\\n-body\\results\\divided_s")

  val system = ActorSystem("N-BodySystem")

  val simulatingActors = loadObjects(simulationType, inputPath, outputPath)

  val simulationManagingActor = getSimulationManagingActor(simulationType)

  printSimulationData(simulationType)

  simulationManagingActor ! SimulationStart(simulatingActors)
}
