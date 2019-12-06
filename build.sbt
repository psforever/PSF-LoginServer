lazy val commonSettings = Seq(
  organization := "net.psforever",
  version := "1.0.2-SNAPSHOT",
  scalaVersion := "2.11.8",
  scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8", "-language:postfixOps"),
  // scaladoc flags: https://github.com/scala/scala/blob/2.11.x/src/scaladoc/scala/tools/nsc/doc/Settings.scala
  // Trick taken from https://groups.google.com/d/msg/scala-user/mxV9ok7J_Eg/kt-LnsrD0bkJ
  scalacOptions in (Compile,doc) <<= baseDirectory map {
    bd => Seq(
    "-groups",
    "-implicits",
    "-doc-title", "PSF-LoginServer - ",
    "-doc-version", "master",
    "-doc-footer", "Copyright PSForever",
    // For non unidoc builds, you may need bd.getName before the template parameter
    "-doc-source-url", "https://github.com/psforever/PSF-LoginServer/blob/master/€{FILE_PATH}.scala",
    "-sourcepath", bd.getAbsolutePath // needed for scaladoc relative source paths
    )
  },
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  libraryDependencies ++= Seq(
    "com.typesafe.akka"          %% "akka-actor"    % "2.4.4",
    "com.typesafe.akka"          %% "akka-testkit"  % "2.4.8" % "test",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "org.specs2"                 %% "specs2-core"   % "3.8.3" % "test",
    "org.scalatest"              %% "scalatest"     % "3.0.1" % "test",
    "org.scodec"                 %% "scodec-core"   % "1.10.0",
    "org.scodec"                 %% "scodec-akka"   % "0.2.0",
    "net.java.dev.jna"            % "jna"           % "4.2.1",
    "com.typesafe.akka"          %% "akka-slf4j"    % "2.4.4",
    "ch.qos.logback"              % "logback-classic" % "1.1.7",
    "org.log4s"                  %% "log4s"         % "1.3.0",
    "org.fusesource.jansi"        % "jansi"         % "1.12",
    "org.scoverage"              %% "scalac-scoverage-plugin" % "1.1.1",
    "com.github.nscala-time"     %% "nscala-time"   % "2.12.0",
    "com.github.mauricio"        %% "mysql-async"   % "0.2.21",
    "org.ini4j"                  % "ini4j"         % "0.5.4",
    "org.scala-graph"            %% "graph-core"    % "1.12.5"
  )
)

lazy val pscryptoSettings = Seq(
  unmanagedClasspath in Test += (baseDirectory in ThisBuild).value / "pscrypto-lib",
  unmanagedClasspath in Runtime += (baseDirectory in ThisBuild).value / "pscrypto-lib",
  unmanagedClasspath in Compile += (baseDirectory in ThisBuild).value / "pscrypto-lib"
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
  //enablePlugins(ScalaUnidocPlugin).
  settings(psloginPackSettings: _*).
  aggregate(pslogin, common)

lazy val pslogin = (project in file("pslogin")).
  settings(commonSettings: _*).
  settings(
    name := "pslogin",
    // ActorTests have specific timing requirements and will be flaky if run in parallel
    parallelExecution in Test := false
  ).
  settings(pscryptoSettings: _*).
  dependsOn(common)

lazy val common = (project in file("common")).
  settings(commonSettings: _*).
  settings(
    name := "common"
  ).
  settings(pscryptoSettings: _*)
