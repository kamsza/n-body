name := "n-body"

version := "0.1"

scalaVersion := "2.13.5"

val akkaVersion     = "2.6.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.github.tototoshi" %% "scala-csv" % "1.3.8",
)
