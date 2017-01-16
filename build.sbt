
import java.time.ZonedDateTime

name := "WebRemoteControl"
organization := "com.webremotecontrol"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.1"
scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-encoding", "UTF-8")

assemblyJarName in assembly := "webremotecontrol.jar"

resolvers ++= Seq(
)
libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.slf4j" % "slf4j-simple" % "1.7.22",
  "org.java-websocket" % "Java-WebSocket" % "1.3.0",
  "org.webjars" % "hammerjs" % "2.0.6"
)

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion,
      BuildInfoKey.action("buildTime") { ZonedDateTime.now.toString }
    ),
    buildInfoPackage := "buildinfo",
    buildInfoUsePackageAsPath := true
  )
