package divided

import akka.actor.{Actor, ActorRef}
import common.ActorDescriptor
import constant.{Constants, SimulationConstants}
import math.Vec2
import message.{ClusterNeighbourNetworkUpdate, ConnectionManagerInitialize, SimulationFinish, UpdateNeighbourList}

case class ConnectionManagerActor() extends Actor {

  var actors: Map[String, ActorRef] = null

  var positions: Map[String, Vec2] = null

  override def receive: Receive = {
    case ConnectionManagerInitialize(clusters) => handleClusterNeighbourNetworkInitialize(clusters)
    case ClusterNeighbourNetworkUpdate(id, position, neighbours) => handleClusterNeighbourNetworkUpdate(id, position, neighbours)
    case SimulationFinish() => finish()
  }

  def handleClusterNeighbourNetworkInitialize(clusters: Set[ClusterActorDescriptor]): Unit = {
    actors = clusters
      .map(clusterDescriptor => clusterDescriptor.id -> clusterDescriptor.actorRef)
      .toMap
    positions = clusters
      .map(clusterDescriptor => clusterDescriptor.id -> clusterDescriptor.position)
      .toMap
  }

  def handleClusterNeighbourNetworkUpdate(
      id: String,
      position: Vec2,
      neighbours: Set[String]
  ): Unit = {
    val newNeighbours = positions
      .filterNot(v => v._1 == id)
      .filterNot(v => neighbours.contains(v._1))
      .filter(v => position.distance(v._2) < SimulationConstants.neighbourDistance)
      .map(v =>
        common.ActorDescriptor(
          v._1,
          actors.getOrElse(
            v._1,
            throw new RuntimeException(
              "ConnectionManagerActor: actor not found in actors map"
            )
          )
        )
      )
      .toSet

    var farNeighbours = Set[ActorDescriptor]()
    if(neighbours.size + newNeighbours.size > SimulationConstants.minNeighboursCount) {
      val farNeighbourDist = 1.1 * SimulationConstants.neighbourDistance
      farNeighbours = positions
        .filterNot(v => v._1 == id)
        .filter(v => neighbours.contains(v._1))
        .filter(v => position.distance(v._2) > farNeighbourDist)
        .map(v =>
          common.ActorDescriptor(
            v._1,
            actors.getOrElse(
              v._1,
              throw new RuntimeException(
                "ConnectionManagerActor: actor not found in actors map"
              )
            )
          )
        )
        .toSet
    }


    if (newNeighbours.nonEmpty || farNeighbours.nonEmpty) {
      actors
        .get(id)
        .collectFirst(a =>
          a ! UpdateNeighbourList(newNeighbours, farNeighbours)
        )
    }
  }

  def finish(): Unit = {
    context.stop(self)
  }
}
