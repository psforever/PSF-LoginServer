lazy val commonSettings = Seq(
  organization := "net.psforever",
  version := "1.0",
  scalaVersion := "2.11.7",
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  libraryDependencies := Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.11",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
    "org.scodec" %% "scodec-core" % "1.8.3",
    "org.scodec" %% "scodec-akka" % "0.1.0-SNAPSHOT",
    "net.java.dev.jna" % "jna" % "4.2.1"
  )
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  aggregate(pslogin, common)

lazy val pslogin = (project in file("pslogin")).
  settings(commonSettings: _*).
  settings(
    name := "pslogin"
  ).settings(packAutoSettings: _*).dependsOn(common)

lazy val common = (project in file("common")).
  settings(commonSettings: _*).
  settings(
    name := "common"
  )