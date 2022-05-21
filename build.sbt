name := "n-body"

version := "0.1"

scalaVersion := "2.13.6"

val AkkaVersion = "2.6.16"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "org.apache.commons" % "commons-math3" % "3.6.1",
  "com.lihaoyi" %% "upickle" % "0.9.5"
)
