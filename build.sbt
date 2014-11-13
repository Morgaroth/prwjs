import spray.revolver.RevolverPlugin._

name := "prwjs_lab2"

scalaVersion := "2.10.4"

val akkaV = "2.3.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "pl.morgaroth" %% "morgaroth-utils" % "1.1.1",
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "net.ceedubs" %% "ficus" % "1.0.1",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.2"
)

Revolver.settings

