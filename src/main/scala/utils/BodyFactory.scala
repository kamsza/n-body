package utils

import akka.actor.{ActorSystem, Props}
import common.ActorDescriptor
import math.Vec2
import org.apache.commons.math3.distribution.{NormalDistribution, UniformRealDistribution}
import single.BodyActor

import scala.collection.mutable

object BodyFactory {
  def createBodyActors(
                        system: ActorSystem,
                        outputDir: Option[String],
                        bodiesCount: Int,
                        bodiesRadius: Double,
                        massMinV: Double = 1e20,
                        massMaxV: Double = 1e35
                      ): Set[ActorDescriptor] = {
    val positionXDistribution = new NormalDistribution(0.0, bodiesRadius / 4)
    val positionYDistribution = new NormalDistribution(0.0, bodiesRadius / 4)
    val massDistribution = new UniformRealDistribution(massMinV, massMaxV)

    println("creating body actors")

    val bodies = mutable.Set[ActorDescriptor]()
    for (bodyIdx <- 0 until bodiesCount) {
      bodies += createBodyActor(
        system,
        outputDir,
        s"body_${bodyIdx}",
        massDistribution.sample(),
        positionXDistribution.sample(),
        positionYDistribution.sample()
      )
    }

    println("creating body actors done")

    bodies.toSet
  }


  def createBodyActor(
                       system: ActorSystem,
                       outputDir: Option[String],
                       bodyId: String,
                       mass: Double,
                       positionX: Double,
                       positionY: Double,
                       velocityX: Double = 0.0,
                       velocityY: Double = 0.0
                     ): ActorDescriptor = {
    val actor = system.actorOf(
      Props(
        classOf[BodyActor],
        bodyId,
        mass,
        Vec2(positionX, positionY),
        Vec2(velocityX,velocityY),
        CsvUtil.initCsvFile(outputDir, bodyId)
      ),
      name = bodyId
    )
    ActorDescriptor(bodyId, actor)
  }
}
