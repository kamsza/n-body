package utils

import akka.actor.{ActorSystem, Props}
import clustered_common.Body
import common.ActorDescriptor
import math.Vec2
import org.apache.commons.math3.distribution.{
  NormalDistribution,
  UniformRealDistribution
}
import org.apache.commons.math3.util.FastMath.sqrt

import scala.collection.mutable

object ClusterFactory {
  def createClusterActors(
                           system: ActorSystem,
                           outputDir: Option[String],
                           T: Class[_],
                           clustersCount: Int,
                           clustersSpacing: Double,
                           bodiesPerClusterCount: Int,
                           clusterRadius: Double,
                           massMinV: Double = 1e20,
                           massMaxV: Double = 1e35
                         ): Set[ActorDescriptor] = {
    val clusters = mutable.Set[ActorDescriptor]()
    val clustersInRow = sqrt(clustersCount).toInt

    println("creating cluster actors")

    for (y <- 0 to clustersInRow) {
      for(x <- 0 until clustersInRow if y * clustersInRow + x < clustersCount) {
        val cluster = createClusterActor(
          system,
          outputDir,
          T,
          s"cluster_${x}_${y}",
          bodiesPerClusterCount,
          clusterRadius,
          x * clustersSpacing,
          y * clustersSpacing,
          massMinV,
          massMaxV
        )
        clusters += cluster
      }
    }

    println("creating cluster actors done")

    clusters.toSet
  }

  def createClusterActor(
      system: ActorSystem,
      outputDir: Option[String],
      T: Class[_],
      clusterId: String,
      bodiesPerClusterCount: Int,
      clusterRadius: Double,
      clusterCenterX: Double,
      clusterCenterY: Double,
      massMinV: Double = 1e20,
      massMaxV: Double = 1e35
  ): ActorDescriptor = {
    val actor = system.actorOf(
      Props(
        T,
        clusterId,
        createBodies(
          clusterId,
          bodiesPerClusterCount,
          clusterRadius,
          clusterCenterX,
          clusterCenterY,
          massMinV,
          massMaxV
        ),
        CsvUtil.initCsvFile(outputDir, clusterId)
      ),
      name = clusterId
    )
    ActorDescriptor(clusterId, actor)
  }

  def createBodies(
      clusterId: String,
      bodiesPerClusterCount: Int,
      clusterRadius: Double,
      clusterCenterX: Double,
      clusterCenterY: Double,
      massMinV: Double = 1e20,
      massMaxV: Double = 1e35
  ): Set[Body] = {
    val positionXDistribution = new NormalDistribution(clusterCenterX, clusterRadius / 4)
    val positionYDistribution = new NormalDistribution(clusterCenterY, clusterRadius / 4)
    val massDistribution = new UniformRealDistribution(massMinV, massMaxV)

    val bodies = mutable.Set[Body]()

    for (bodyIdx <- 0 until bodiesPerClusterCount) {
      bodies += createBody(
        clusterId,
        bodyIdx,
        massDistribution.sample(),
        positionXDistribution.sample(),
        positionYDistribution.sample()
      )
    }

    bodies.toSet
  }

  def createBody(
      clusterId: String,
      bodyIdx: Int,
      mass: Double,
      positionX: Double,
      positionY: Double
  ): Body = {
    new Body(
      s"${clusterId}_${bodyIdx}",
      BigDecimal(mass),
      Vec2(positionX, BigDecimal(positionY)),
      Vec2(BigDecimal(0), BigDecimal(0))
    )
  }
}
