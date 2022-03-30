package utils

import akka.actor.{ActorRef, ActorSystem, Props}
import clustered_common.Body
import math.Vec2
import single.BodyActor

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Path, Paths}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source


object CSVUtil {
  val DELIMITER = ";"

  val outputDir = "results/"

  def bodyDataDescription: String = List("id", "\"mass [kg]\"", "\"position x [m]\"", "\"position y [m]\"", "velocity x [m/s]", "\"velocity y[m/s]\"")
    .mkString(DELIMITER)

  def loadBodiesActors(resourceName: String, outputDir: String, system: ActorSystem): List[ActorRef] = {
    var bodyId = 0
    val bodies = ArrayBuffer[ActorRef]()
    val bufferedSource = createBufferedSource(resourceName)
    for (line <- bufferedSource.getLines) {
      val cols = line.split(DELIMITER).map(_.trim)
      bodies += system.actorOf(
        Props(classOf[BodyActor],
          bodyId.toString,
          BigDecimal(cols(0)),
          Vec2(BigDecimal(cols(1)), BigDecimal(cols(2))),
          Vec2(BigDecimal(cols(3)), BigDecimal(cols(4))),
          initCsvFile(Paths.get(outputDir), s"body_${bodyId}.csv")
        ), name = s"body_${bodyId}")
      bodyId += 1
    }
    bufferedSource.close
    bodies.toList
  }

  def loadClusterBodies(resourceName: String, clusterId: String): mutable.Set[Body] = {
    var bodyId = 0
    val bodies = mutable.Set[Body]()
    val bufferedSource = createBufferedSource(resourceName)
    for (line <- bufferedSource.getLines.drop(1)) {
      val cols = line.split(DELIMITER).map(_.trim)
      bodies += new Body(
        s"${clusterId}_${bodyId}",
        BigDecimal(cols(0)),
        Vec2(BigDecimal(cols(1)), BigDecimal(cols(2))),
        Vec2(BigDecimal(cols(3)), BigDecimal(cols(4)))
      )
      bodyId += 1
    }
    bufferedSource.close
    bodies
  }

  def loadClusterBodies(resourceName: String, clusterId: String, shiftVector: Vec2): mutable.Set[Body] = {
    val bodies = loadClusterBodies(resourceName, clusterId)
    bodies.foreach(_.changePosition(shiftVector))
    bodies
  }

  def loadClusters(resourceDir: String, outputDir: String, system: ActorSystem, T: Class[_]): List[ActorRef] = {
    val clusters = ArrayBuffer[ActorRef]()
    val dir = new File(getClass.getResource(resourceDir).getPath)
    if (!dir.exists || !dir.isDirectory) throw new IllegalArgumentException(s"Resource directory '${resourceDir}' not found")
    val resourceNames = dir.listFiles.filter(_.isFile).toList
    for (resourceName <- resourceNames) {
      val clusterName = resourceName.getName.replace(".txt", "")
      val resourcePath = new File(resourceDir, resourceName.getName).toString
      clusters += system.actorOf(
        Props(
          T,
          clusterName,
          CSVUtil.loadClusterBodies(resourcePath, clusterName),
          initCsvFile(Paths.get(outputDir), s"${clusterName}.csv")
        ),
        name = clusterName)
    }
    clusters.toList
  }

  def createBufferedSource(resourceName: String): Source = {
    val resourceFullName = if (resourceName.startsWith("/")) resourceName else "/" + resourceName
    val fileUri = getClass.getResource(resourceFullName)
    Source.fromURL(fileUri)
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
}
