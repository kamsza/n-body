package utils

case class SimulationConstants(
                                dt: Int = 1000,
                                communicationStep: Int = 50000,
                                simulationStepsCount: Int = 10000000
                              )