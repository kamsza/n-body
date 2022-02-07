package utils

import clustered.Body

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable.ArrayBuffer
import scala.io.Source


object CSVUtil {
  val DELIMITER = " "

  val outputDir = "results/"

  def bodyDataDescription: String =
    "id \"mass [kg]\" \"position x [m]\" \"position y [m]\" \"velocity x [m/s]\" \"velocity y[m/s]\""

  /**
   *  expected CSV file in which each line contains body data in the format:
   *  [mass, position X, position Y, velocity X, velocity Y]
   * */
  def loadBodies(resourceName: String, clusterId: String): ArrayBuffer[Body] = {
    val resourceFullName = if (resourceName.startsWith("/")) resourceName else "/" + resourceName
    val fileUri = getClass.getResource(resourceFullName)
    var bodyId = 0
    val bodies = ArrayBuffer[Body]()
    val bufferedSource = Source.fromURL(fileUri)
    for (line <- bufferedSource.getLines) {
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

  def loadBodies(resourceName: String, clusterId: String, shiftVector: Vec2): ArrayBuffer[Body] = {
    val bodies = loadBodies(resourceName, clusterId)
    bodies.foreach(_.changePosition(shiftVector))
    bodies
  }

  def initCsvFile(csvFilePath: Path): BufferedWriter = {
    csvFilePath.toFile.delete()
    val fileWriter = new BufferedWriter(new FileWriter(csvFilePath.toFile))
    val dataHeader = s"${bodyDataDescription}"
    fileWriter.write(dataHeader)
    fileWriter
  }
}
