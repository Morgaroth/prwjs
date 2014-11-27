import spray.revolver.RevolverPlugin._

name := "prwjs"

scalaVersion := "2.11.2"

val akkaV = "2.3.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "io.github.morgaroth" %% "morgaroth-utils" % "1.2.3",
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "net.ceedubs" %% "ficus" % "1.0.1",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.2"
)

Revolver.settings

