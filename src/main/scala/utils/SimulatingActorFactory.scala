package utils

import akka.actor.{ActorSystem, Props}
import clustered_common.Body
import common.ActorDescriptor
import math.Vec2
import single.BodyActor

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Path, Paths}
import scala.collection.mutable
import scala.io.Source


object SimulatingActorFactory {
  val DELIMITER = ";"

  //** BODY SIMULATION **//
  /**
   * Function loads data about bodies from file and returns set with actor descriptors
   *
   * @param resourceFilePath path to CSV file with bodies data, written in the order of: mass, position x, position y, velocity x, velocity y
   *                         with DELIMITER as delimiter. The first line is omitted as a header.
   * @param outputFileDir path to the directory, in which bodies positions will be stored, in separate file for each body
   * @param system ActorSystem object, for which actors will be created
  */
  def loadBodiesActors(resourceFilePath: String, outputFileDir: Option[String], system: ActorSystem): Set[ActorDescriptor] = {
    var bodyIdx = 0
    val bodies = mutable.Set[ActorDescriptor]()
    val bufferedSource = createBufferedSource(resourceFilePath)
    for (line <- bufferedSource.getLines.drop(1)) {
      val csvCols = line
        .split(DELIMITER)
        .map(_.trim)
      bodies += createBodyActor(system, bodyIdx, outputFileDir, csvCols)
      bodyIdx += 1
    }
    bufferedSource.close
    bodies.toSet
  }

  def createBodyActor(system: ActorSystem, bodyIdx: Int, outputDir: Option[String], csvData: Array[String]): ActorDescriptor = {
    val bodyId = s"body_${bodyIdx}"
    val actor = system.actorOf(
      Props(classOf[BodyActor],
        bodyId,
        BigDecimal(csvData(0)),
        Vec2(BigDecimal(csvData(1)), BigDecimal(csvData(2))),
        Vec2(BigDecimal(csvData(3)), BigDecimal(csvData(4))),
        initCsvFile(outputDir, s"${bodyId}.csv")
      ), name = bodyId)
    ActorDescriptor(bodyId, actor)
  }

  //** CLUSTERED SIMULATION **//
  /**
   * Function loads data about clusters from directories and returns set with actor descriptors
   *
   * @param resourceDirPath path to directories with files describing clusters. Each cluster is described by one file, if format
   *                        same as in bodies simulation.
   * @param outputDir path to the directory, in which bodies positions will be stored, in separate file for each cluster
   * @param system ActorSystem object, for which actors will be created
   */
  def loadClusters(resourceDirPath: String, outputDir: Option[String], system: ActorSystem, T: Class[_]): Set[ActorDescriptor] = {
    val dir = new File(resourceDirPath)
    if (!dir.exists || !dir.isDirectory) throw new IllegalArgumentException(s"Resource directory '${resourceDirPath}' not found")
    dir.listFiles
      .filter(_.isFile)
      .map(file => createClusterActor(system, outputDir, file, T))
      .toSet
  }

  def createClusterActor(system: ActorSystem, outputDir: Option[String], resource: File, T: Class[_]): ActorDescriptor = {
    val resourcePath = resource.getPath
    val clusterId = resource.getName
      .replace(".txt", "")
      .replace(".csv", "")
    val actor = system.actorOf(
      Props(
        T,
        clusterId,
        SimulatingActorFactory.loadClusterBodies(resourcePath, clusterId),
        initCsvFile(outputDir, s"${clusterId}.csv")
      ),
      name = clusterId)
    ActorDescriptor(clusterId, actor)
  }

  def loadClusterBodies(resourceName: String, clusterId: String, shiftVector: Vec2): Set[Body] = {
    val bodies = loadClusterBodies(resourceName, clusterId)
    bodies.foreach(_.changePosition(shiftVector))
    bodies
  }

  def loadClusterBodies(resourceName: String, clusterId: String): Set[Body] = {
    var bodyIdx = 0
    val bodies = mutable.Set[Body]()
    val bufferedSource = createBufferedSource(resourceName)
    for (line <- bufferedSource.getLines.drop(1)) {
      val cols = line.split(DELIMITER)
        .map(_.trim)
      bodies += createBody(clusterId, bodyIdx, cols)
      bodyIdx += 1
    }
    bufferedSource.close
    bodies.toSet
  }

  def createBody(clusterId: String, bodyIdx: Int, csvData: Array[String]): Body = {
    new Body(
      s"${clusterId}_${bodyIdx}",
      BigDecimal(csvData(0)),
      Vec2(BigDecimal(csvData(1)), BigDecimal(csvData(2))),
      Vec2(BigDecimal(csvData(3)), BigDecimal(csvData(4)))
    )
  }

  def createBufferedSource(resourcePath: String): Source = {
    try {
      Source.fromFile(resourcePath)
    } catch {
      case _: NullPointerException => throw new IllegalArgumentException(s"Cannot find resource: ${resourcePath}")
    }
  }

  def initCsvFile(outputPath: Option[String], csvFileName: String): Option[BufferedWriter] = {
    outputPath match {
      case Some(dirPath) => Some(initCsvFile(Paths.get(dirPath), s"${csvFileName}.csv"))
      case None => None
    }
  }

  def initCsvFile(dirPath: Path, csvFileName: String): BufferedWriter = {
    val path = Paths.get(dirPath.toString, csvFileName)
    if (path.toFile.exists()) {
      path.toFile.delete()
    } else if (!dirPath.toFile.exists()) {
      dirPath.toFile.mkdir()
    }
    val fileWriter = new BufferedWriter(new FileWriter(path.toFile))
    val dataHeader = s"${bodyDataDescription}"
    fileWriter.write(dataHeader)
    fileWriter
  }

  def bodyDataDescription: String = List(
    "id",
    "\"mass [kg]\"",
    "\"position x [m]\"",
    "\"position y [m]\"",
    "\"velocity x [m/s]\"",
    "\"velocity y [m/s]\""
  ).mkString(DELIMITER)
}
