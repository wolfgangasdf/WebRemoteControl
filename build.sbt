
import java.time.ZonedDateTime

name := "WebRemoteControl"
organization := "com.webremotecontrol"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.1"
scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-encoding", "UTF-8")

//unmanagedResourceDirectories in Compile += { baseDirectory.value / "public/" }

assemblyJarName in assembly := "webremotecontrol.jar"

resolvers ++= Seq(
)
libraryDependencies ++= Seq(
  "org.java-websocket" % "Java-WebSocket" % "1.3.0"
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
