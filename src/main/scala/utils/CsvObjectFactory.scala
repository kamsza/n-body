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

/** Factory creating simulating actors.
  */
object CsvObjectFactory {
  //** BODY SIMULATION **//
  /** Function loads data about bodies from file and returns set with actor descriptors
    *
    * @param resourceFilePath path to CSV file with bodies data, written in the order of: mass, position x, position y, velocity x, velocity y
    *                         with DELIMITER as delimiter. The first line is omitted as a header.
    * @param outputFileDir path to the directory, in which bodies positions will be stored, in separate file for each body
    * @param system ActorSystem object, for which actors will be created
    */
  def loadBodiesActors(
      resourceFilePath: String,
      outputFileDir: Option[String],
      system: ActorSystem
  ): Set[ActorDescriptor] = {
    var bodyIdx = 0
    val bodies = mutable.Set[ActorDescriptor]()
    val bufferedSource = CsvUtil.createBufferedSource(resourceFilePath)
    for (line <- bufferedSource.getLines.drop(1)) {
      val csvCols = line
        .split(CsvUtil.DELIMITER)
        .map(_.trim)
      bodies += createBodyActor(system, bodyIdx, outputFileDir, csvCols)
      bodyIdx += 1
    }
    bufferedSource.close
    bodies.toSet
  }

  def createBodyActor(
      system: ActorSystem,
      bodyIdx: Int,
      outputDir: Option[String],
      csvData: Array[String]
  ): ActorDescriptor = {
    val bodyId = s"body_${bodyIdx}"
    val actor = system.actorOf(
      Props(
        classOf[BodyActor],
        bodyId,
        csvData(0).toDouble,
        Vec2(csvData(1).toDouble, csvData(2).toDouble),
        Vec2(csvData(3).toDouble, csvData(4).toDouble),
        CsvUtil.initCsvFile(outputDir, bodyId)
      ),
      name = bodyId
    )
    ActorDescriptor(bodyId, actor)
  }

  //** CLUSTERED SIMULATION **//
  /** Function loads data about clusters from directories and returns set with actor descriptors
    *
    * @param resourceDirPath path to directories with files describing clusters. Each cluster is described by one file, if format
    *                        same as in bodies simulation.
    * @param outputDir path to the directory, in which bodies positions will be stored, in separate file for each cluster
    * @param system ActorSystem object, for which actors will be created
    */
  def loadClusters(
      resourceDirPath: String,
      outputDir: Option[String],
      system: ActorSystem,
      T: Class[_]
  ): Set[ActorDescriptor] = {
    val dir = new File(resourceDirPath)
    if (!dir.exists || !dir.isDirectory)
      throw new IllegalArgumentException(
        s"Resource directory '${resourceDirPath}' not found"
      )
    dir.listFiles
      .filter(_.isFile)
      .map(file => createClusterActor(system, outputDir, file, T))
      .toSet
  }

  def createClusterActor(
      system: ActorSystem,
      outputDir: Option[String],
      resource: File,
      T: Class[_]
  ): ActorDescriptor = {
    val resourcePath = resource.getPath
    val clusterId = resource.getName
      .replace(".txt", "")
      .replace(".csv", "")
    val actor = system.actorOf(
      Props(
        T,
        clusterId,
        CsvObjectFactory.loadClusterBodies(resourcePath, clusterId),
        CsvUtil.initCsvFile(outputDir, clusterId)
      ),
      name = clusterId
    )
    ActorDescriptor(clusterId, actor)
  }

  def loadClusterBodies(resourceName: String, clusterId: String): Set[Body] = {
    var bodyIdx = 0
    val bodies = mutable.Set[Body]()
    val bufferedSource = CsvUtil.createBufferedSource(resourceName)
    for (line <- bufferedSource.getLines.drop(1)) {
      val cols = line
        .split(CsvUtil.DELIMITER)
        .map(_.trim)
      bodies += createBody(clusterId, bodyIdx, cols)
      bodyIdx += 1
    }
    bufferedSource.close
    bodies.toSet
  }

  def createBody(
      clusterId: String,
      bodyIdx: Int,
      csvData: Array[String]
  ): Body = {
    new Body(
      s"${clusterId}_${bodyIdx}",
      csvData(0).toDouble,
      Vec2(csvData(1).toDouble, csvData(2).toDouble),
      Vec2(csvData(3).toDouble, csvData(4).toDouble)
    )
  }

  def bodyDataDescription: String = List(
    "id",
    "\"mass [kg]\"",
    "\"position x [m]\"",
    "\"position y [m]\"",
    "\"velocity x [m/s]\"",
    "\"velocity y [m/s]\"",
    "timestamp"
  ).mkString(CsvUtil.DELIMITER)

  def loadClusterBodies(
      resourceName: String,
      clusterId: String,
      shiftVector: Vec2
  ): Set[Body] = {
    val bodies = loadClusterBodies(resourceName, clusterId)
    bodies.foreach(_.changePosition(shiftVector))
    bodies
  }
}
