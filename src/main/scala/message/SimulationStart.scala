package message

import akka.actor.ActorRef
import utils.SimulationConstants

case class SimulationStart(
                            managingActor: ActorRef,
                            simulationConstants: SimulationConstants = SimulationConstants()
                          )
