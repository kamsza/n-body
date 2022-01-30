package utils

import `object`.Body
import akka.actor.{ActorRef, ActorSystem, Props}

import java.io.{File, FileWriter}
import scala.collection.mutable.ArrayBuffer
import scala.io.Source


object CSVUtil {
  val DELIMITER = " "

  val outputDir = "results/"

  def bodyDataDescription: String =
    "id \"mass [kg]\" \"position x [m]\" \"position y [m]\" \"velocity x [m/s]\" \"velocity y[m/s]\" timestep"

  /**
  expected CSV file in which each line contains body data in the format:
    [mass, position X, position Y, velocity X, velocity Y]
   **/
  def loadBodies(resourceName: String, system: ActorSystem): List[ActorRef] = {
    val resourceFullName = if(resourceName.startsWith("/")) resourceName else "/" + resourceName
    val fileUri = getClass.getResource(resourceFullName)
    val bodies = ArrayBuffer[ActorRef]()
    val bufferedSource = Source.fromURL(fileUri)
    val fileLines = bufferedSource.getLines.toList
    val bodiesCount = fileLines.size
    var bodyId = 0
    for (line <- fileLines) {
      val cols = line.split(DELIMITER).map(_.trim)
      bodies += system.actorOf(
        Props(classOf[Body],
          bodyId.toString,
          BigDecimal(cols(0)),
          Vec2(BigDecimal(cols(1)), BigDecimal(cols(2))),
          Vec2(BigDecimal(cols(3)), BigDecimal(cols(4))),
          bodiesCount - 1
        ), name = s"body_${bodyId}")
      bodyId += 1
    }
    bufferedSource.close
    bodies.toList
  }

  def initCsvFile(csvFileName: String, overwrite: Boolean = true): Unit = {
    if(overwrite) new File(outputDir + csvFileName).delete()
    val outputPath = outputDir + csvFileName
    val dataHeader = s"${bodyDataDescription}"
    if(!new File(outputDir + csvFileName).exists()) {
      val fileWriter = new FileWriter(outputPath, true)
      try {
        fileWriter.write(dataHeader)
      } finally {
        fileWriter.close()
      }
    }
  }

  def saveBodiesDataToFile(
     csvFileName: String,
     bodiesList: List[(String, BigDecimal, Vec2, Vec2, Int)]
  ): Unit = {
    val outputPath = outputDir + csvFileName
    val flatBodiesList = bodiesList.map(t => Tuple5(t._1, t._2, t._3.productIterator.mkString(DELIMITER), t._4.productIterator.mkString(DELIMITER), t._5))
    val data = "\n" + flatBodiesList.map(tuple => tuple.productIterator.mkString(DELIMITER)).mkString("\n")
    val fileWriter = new FileWriter(outputPath, true)
    try {
      fileWriter.write(data)
    } finally {
      fileWriter.close()
    }
  }

  def saveTestDataToFile(
    csvFileName: String,
    bodiesList: List[(String, BigDecimal, Vec2, Vec2, Int, Int)]
  ): Unit = {
    val outputPath = outputDir + csvFileName
    val flatBodiesList = bodiesList.map(t => Tuple5(t._1, t._2, t._3.productIterator.mkString(DELIMITER), t._4.productIterator.mkString(DELIMITER), t._6))
    val data = "\n" + flatBodiesList.map(tuple => tuple.productIterator.mkString(DELIMITER)).mkString("\n")
    val fileWriter = new FileWriter(outputPath, true)
    try {
      fileWriter.write(data)
    } finally {
      fileWriter.close()
    }
  }

  def deleteFile(fileName: String): Unit = {
    new File(outputDir + fileName).delete()
  }
}
