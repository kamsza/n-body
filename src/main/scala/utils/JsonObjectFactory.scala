package utils

import akka.actor.ActorSystem
import common.ActorDescriptor

object JsonObjectFactory {
  def generateBodies(
                      jsonPath: String,
                      outputPath: Option[String],
                      system: ActorSystem
                    ): Set[ActorDescriptor] = {
    val json_content = CsvUtil.createBufferedSource(jsonPath).getLines().mkString
    val json_data = ujson.read(json_content)

    BodyFactory.createBodyActors(
      system,
      outputPath,
      json_data("bodies_count").num.toInt,
      json_data("bodies_radius").num
    )
  }

  def generateClusters(
      jsonPath: String,
      outputPath: Option[String],
      system: ActorSystem,
      T: Class[_]
  ): Set[ActorDescriptor] = {
    val json_content = CsvUtil.createBufferedSource(jsonPath).getLines().mkString
    val json_data = ujson.read(json_content)
    ClusterFactory.createClusterActors(
       system,
       outputPath,
       T,
       json_data("clusters_count").num.toInt,
       json_data("clusters_spacing").num,
       json_data("bodies_per_cluster").num.toInt,
       json_data("clusters_radius").num
    )
  }

}
