
import java.time.ZonedDateTime

name := "WebRemoteControl"
organization := "com.webremotecontrol"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.1"
scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-encoding", "UTF-8")

assemblyJarName in assembly := "webremotecontrol.jar"

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.22"
libraryDependencies += "org.java-websocket" % "Java-WebSocket" % "1.3.0"
libraryDependencies += "org.webjars" % "hammerjs" % "2.0.6"
libraryDependencies += "com.github.kenglxn.QRGen" % "javase" % "2.2.0"

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion,
      BuildInfoKey.action("buildTime") { ZonedDateTime.now.toString }
    ),
    buildInfoPackage := "buildinfo",
    buildInfoUsePackageAsPath := true
  )

lazy val tdist = TaskKey[Unit]("dist")
tdist := {
  val af = assembly.value
  IO.copyFile(af, target.value / af.name)
  println("Created jar in target/ !")
}
