package utils

import akka.actor.{ActorRef, ActorSystem, Props}
import clustered_common.Body
import common.ActorDescriptor
import math.Vec2
import single.BodyActor

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Path, Paths}
import scala.collection.mutable
import scala.io.Source


object CSVUtil {
  val DELIMITER = ";"

  val outputDir = "results/"

  def bodyDataDescription: String = List(
    "id",
    "\"mass [kg]\"",
    "\"position x [m]\"",
    "\"position y [m]\"",
    "\"velocity x [m/s]\"",
    "\"velocity y [m/s]\""
  ).mkString(DELIMITER)

  def loadBodiesActors(resourceName: String, outputDir: String, system: ActorSystem): Set[ActorDescriptor] = {
    var bodyIdx = 0
    val bodies = mutable.Set[ActorDescriptor]()
    val bufferedSource = createBufferedSource(getResourcePath(resourceName))
    for (line <- bufferedSource.getLines.drop(1)) {
      val csvCols = line
        .split(DELIMITER)
        .map(_.trim)
      bodies += createBodyActor(system, bodyIdx, outputDir, csvCols)
      bodyIdx += 1
    }
    bufferedSource.close
    bodies.toSet
  }

  def createBodyActor(system: ActorSystem, bodyIdx: Int, outputDir: String, csvData: Array[String]): ActorDescriptor = {
    val bodyId = s"body_${bodyIdx}"
    val actor = system.actorOf(
      Props(classOf[BodyActor],
        bodyId,
        BigDecimal(csvData(0)),
        Vec2(BigDecimal(csvData(1)), BigDecimal(csvData(2))),
        Vec2(BigDecimal(csvData(3)), BigDecimal(csvData(4))),
        initCsvFile(Paths.get(outputDir), s"${bodyId}.csv")
      ), name = bodyId)
    ActorDescriptor(bodyId, actor)
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

  def loadClusterBodies(resourceName: String, clusterId: String, shiftVector: Vec2): Set[Body] = {
    val bodies = loadClusterBodies(resourceName, clusterId)
    bodies.foreach(_.changePosition(shiftVector))
    bodies
  }

  def loadClusters(resourceDir: String, outputDir: String, system: ActorSystem, T: Class[_]): Set[ActorDescriptor] = {
    val dir = new File(getResourcePath(resourceDir))
    if (!dir.exists || !dir.isDirectory) throw new IllegalArgumentException(s"Resource directory '${resourceDir}' not found")
    dir.listFiles
      .filter(_.isFile)
      .map(file => createClusterActor(system, outputDir, file, T))
      .toSet
  }

  def createClusterActor(system: ActorSystem, outputDir: String, resource: File, T: Class[_]): ActorDescriptor = {
    val resourcePath = resource.getPath
    val clusterId = resource.getName
      .replace(".txt", "")
      .replace(".csv", "")
    val actor = system.actorOf(
      Props(
        T,
        clusterId,
        CSVUtil.loadClusterBodies(resourcePath, clusterId),
        initCsvFile(Paths.get(outputDir), s"${clusterId}.csv")
      ),
      name = clusterId)
    ActorDescriptor(clusterId, actor)
  }

  def createBufferedSource(resourcePath: String): Source = {
    try {
      Source.fromFile(resourcePath)
    } catch {
      case _: NullPointerException => throw new IllegalArgumentException(s"Cannot find resource: ${resourcePath}")
    }
  }

  def initCsvFile(dirPath: Path, csvFileName: String): BufferedWriter = {
    val path = Paths.get(dirPath.toString, csvFileName)
    if(path.toFile.exists()) {
      path.toFile.delete()
    } else if(!dirPath.toFile.exists()) {
      dirPath.toFile.mkdir()
    }
    val fileWriter = new BufferedWriter(new FileWriter(path.toFile))
    val dataHeader = s"${bodyDataDescription}"
    fileWriter.write(dataHeader)
    fileWriter
  }

  def getResourcePath(resourceName: String) :String = {
    val resourceRelativePath = if (resourceName.startsWith("/")) resourceName else "/" + resourceName
    getClass.getResource(resourceRelativePath).getPath
  }
}
