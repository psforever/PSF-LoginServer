lazy val commonSettings = Seq(
  organization := "net.psforever",
  version := "1.0",
  scalaVersion := "2.11.7",
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  libraryDependencies ++= Seq(
    "com.typesafe.akka"          %% "akka-actor"    % "2.4.4",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "org.specs2"                 %% "specs2-core"   % "3.8.3" % "test",
    "org.scodec"                 %% "scodec-core"   % "1.10.0",
    "org.scodec"                 %% "scodec-akka"   % "0.2.0",
    "net.java.dev.jna"            % "jna"           % "4.2.1",
    "com.typesafe.akka"          %% "akka-slf4j"    % "2.4.4",
    "ch.qos.logback"              % "logback-classic" % "1.1.7",
    "org.log4s"                  %% "log4s"         % "1.3.0",
    "org.fusesource.jansi"        % "jansi"         % "1.12",
    "org.scoverage"              %% "scalac-scoverage-plugin" % "1.1.1"
  )
)

lazy val pscryptoSettings = Seq(
  unmanagedClasspath in Test += (baseDirectory in ThisBuild).value / "pscrypto-lib",
  unmanagedClasspath in Runtime += (baseDirectory in ThisBuild).value / "pscrypto-lib"
)

lazy val psloginPackSettings = packAutoSettings ++ Seq(
  packArchivePrefix := "pslogin",
  packExtraClasspath := Map("ps-login" -> Seq("${PROG_HOME}/pscrypto-lib",
    "${PROG_HOME}/config")),
  packResourceDir += (baseDirectory.value / "pscrypto-lib" -> "pscrypto-lib"),
  packResourceDir += (baseDirectory.value / "config" -> "config")
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(psloginPackSettings: _*).
  aggregate(pslogin, common)

lazy val pslogin = (project in file("pslogin")).
  settings(commonSettings: _*).
  settings(
    name := "pslogin"
  ).
  settings(pscryptoSettings: _*).
  dependsOn(common)

lazy val common = (project in file("common")).
  settings(commonSettings: _*).
  settings(
    name := "common"
  ).
  settings(pscryptoSettings: _*)
