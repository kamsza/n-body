package utils

import utils.CsvObjectFactory.bodyDataDescription

import java.io.{BufferedWriter, FileWriter}
import java.nio.file.{Path, Paths}
import scala.io.Source

object CsvUtil {
  val DELIMITER = ";"

  def createBufferedSource(resourcePath: String): Source = {
    try {
      Source.fromFile(resourcePath)
    } catch {
      case _: NullPointerException =>
        throw new IllegalArgumentException(
          s"Cannot find resource: ${resourcePath}"
        )
    }
  }

  def initCsvFile(
                   outputPath: Option[String],
                   csvFileName: String
                 ): Option[BufferedWriter] = {
    outputPath match {
      case Some(dirPath) =>
        Some(initCsvFile(Paths.get(dirPath), s"${csvFileName}.csv"))
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
}
