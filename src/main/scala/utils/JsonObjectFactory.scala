package utils

import akka.actor.ActorSystem
import common.ActorDescriptor
import constant.SimulationConstants
import ujson.Value

object JsonObjectFactory {
  def parseJsonWithBodies(
                      jsonPath: String,
                      outputPath: Option[String],
                      system: ActorSystem
                    ): Set[ActorDescriptor] = {
    val json_content = CsvUtil.createBufferedSource(jsonPath).getLines().mkString
    val json_data = ujson.read(json_content)

    updateSimulationConstants(json_data)

    BodyFactory.createBodyActors(
      system,
      outputPath,
      json_data("bodies_count").num.toInt,
      json_data("bodies_radius").num
    )
  }

  def parseJsonWithClusters(
      jsonPath: String,
      outputPath: Option[String],
      system: ActorSystem,
      T: Class[_]
  ): Set[ActorDescriptor] = {
    val json_content = CsvUtil.createBufferedSource(jsonPath).getLines().mkString
    val json_data = ujson.read(json_content)

    updateSimulationConstants(json_data)

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

  def updateSimulationConstants(json_data: Value.Value): Unit = {
    getValueIfExists(json_data, "dt").foreach(v => SimulationConstants.dt = v)
    getValueIfExists(json_data, "simulation_steps_count").foreach(v => SimulationConstants.simulatingActorsCount = v)
    getValueNumIfExists(json_data, "softening_parameter").foreach(v => SimulationConstants.softeningParameter = v)

    getValueIfExists(json_data, "communication_step").foreach(v => SimulationConstants.communicationStep = v)
    getValueIfExists(json_data, "bodies_affiliation_check").foreach(v => SimulationConstants.bodiesAffiliationCheck = v)
    getValueIfExists(json_data, "cluster_neighbours_check").foreach(v => SimulationConstants.clusterNeighboursCheck = v)

    getValueIfExists(json_data, "min_neighbours_count").foreach(v => SimulationConstants.minNeighboursCount = v)
    getValueIfExists(json_data, "bodies_count").foreach(v => SimulationConstants.simulatingActorsCount = v)
    getValueIfExists(json_data, "clusters_count").foreach(v => SimulationConstants.simulatingActorsCount = v)
    getValueIfExists(json_data, "bodies_per_cluster").foreach(v => SimulationConstants.bodiesPerClusterCount = v)

    getValueNumIfExists(json_data, "neighbours_distance").foreach(v => SimulationConstants.neighbourDistance = v)
  }

  def getValueIfExists(json_data: Value.Value, key: String): Option[Int] = json_data.obj.value.get(key).map(_.num.toInt)

  def getValueNumIfExists(json_data: Value.Value, key: String): Option[Double] = json_data.obj.value.get(key).map(_.num)
}
