package constant

object SimulationConstants {
  var dt = 100
  var simulationStepsCount: Int = 100
  var softeningParameter: Double = 10000000

  var communicationStep: Int = Int.MaxValue
  var bodiesAffiliationCheck: Int = Int.MaxValue
  var clusterNeighboursCheck: Int = Int.MaxValue

  var minNeighboursCount: Int = 1
  var simulatingActorsCount: Int = -1
  var bodiesPerClusterCount: Int = -1
  var objectsCount: Int = 0

  var neighbourDistance: Double = 0
}
