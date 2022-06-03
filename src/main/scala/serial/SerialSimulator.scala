package serial

import math.Vec2
import org.apache.commons.math3.distribution.{NormalDistribution, UniformRealDistribution}

import scala.collection.mutable

case class SerialSimulator (bodiesCount: Int,
                            stepsCount: Int) {

  val bodies: Set[Body] = createBodies(bodiesCount)

  val markersCount = 10

  val updateStep: Int = stepsCount / markersCount

  var partsDone = 0

  var startTime = 0L

  def run(): Unit = {
    startTime = System.currentTimeMillis()

    makeInitMove()

    for(i <- 1 until stepsCount) {
      makeSimulationStep()
      if(i % updateStep == 0) {
        partsDone += 1
        printUpdateMessage()
      }
    }

    val endTime = System.currentTimeMillis()

    println(s"simulation finished in ${(endTime - startTime) / 1000.0}s")
  }

  def makeInitMove(): Unit = {
    for(body1 <- bodies) {
      for(body2 <- bodies) {
        if(!body1.equals(body2)) {
          body1.applyForce(body2)
        }
      }
    }

    for(body <- bodies) {
      body.initMove()
    }
  }

  def partsToDo: Int = markersCount - partsDone

  def printUpdateMessage(): Unit = {
    val currTime = System.currentTimeMillis()
    println(s"[${"X" * partsDone}${"-" * partsToDo}] ${partsDone}/${markersCount}    in    ${(currTime - startTime) / 1000.0}s")
  }

  def makeSimulationStep(): Unit = {
    for(body1 <- bodies) {
      for(body2 <- bodies) {
        if(!body1.equals(body2)) {
          body1.applyForce(body2)
        }
      }
    }

    for(body <- bodies) {
      body.initMove()
    }
  }


  def createBodies(bodiesCount: Int): Set[Body] = {
    val positionXDistribution = new NormalDistribution(0, 2.5e30)
    val positionYDistribution = new NormalDistribution(0, 2.5e30)
    val massDistribution = new UniformRealDistribution(1e20, 1e40)

    val bodies = mutable.Set[Body]()

    for (bodyIdx <- 0 until bodiesCount) {
      bodies += createBody(
        bodyIdx,
        massDistribution.sample(),
        positionXDistribution.sample(),
        positionYDistribution.sample()
      )
    }

    bodies.toSet
  }

  def createBody(
                  bodyIdx: Int,
                  mass: Double,
                  positionX: Double,
                  positionY: Double
                ): Body = {
    new Body(
      bodyIdx.toString,
      mass,
      Vec2(positionX, positionY),
      Vec2(0.0, 0.0)
    )
  }
}
