import xerial.sbt.pack.PackPlugin._

lazy val commonSettings = Seq(
  organization := "net.psforever",
  version := "1.0.2-SNAPSHOT",
  scalaVersion := "2.13.2",
  scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8", "-language:postfixOps"),

  // Quiet test options
  // https://github.com/etorreborre/specs2/blob/8305db76c5084e4b3ce5827ce23117f6fb6beee4/common/shared/src/main/scala/org/specs2/main/Report.scala#L94
  // https://etorreborre.github.io/specs2/guide/SPECS2-2.4.17/org.specs2.guide.Runners.html
  testOptions in QuietTest += Tests.Argument(TestFrameworks.Specs2, "showOnly", "x!"),
  // http://www.scalatest.org/user_guide/using_the_runner
  testOptions in QuietTest += Tests.Argument(TestFrameworks.ScalaTest, "-oCEHILMNOPQRX"),
  // Trick taken from https://groups.google.com/d/msg/scala-user/mxV9ok7J_Eg/kt-LnsrD0bkJ
  // scaladoc flags: https://github.com/scala/scala/blob/2.11.x/src/scaladoc/scala/tools/nsc/doc/Settings.scala
  scalacOptions in (Compile,doc) := { Seq(
    "-groups",
    "-implicits",
    "-doc-title", "PSF-LoginServer - ",
    "-doc-version", "master",
    "-doc-footer", "Copyright PSForever",
    // For non unidoc builds, you may need bd.getName before the template parameter
    "-doc-source-url", "https://github.com/psforever/PSF-LoginServer/blob/master/€{FILE_PATH}.scala",
    "-sourcepath", baseDirectory.value.getAbsolutePath // needed for scaladoc relative source paths
    )
  },
  classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  libraryDependencies ++= Seq(
    "com.typesafe.akka"          %% "akka-actor"    % "2.6.5",
    "com.typesafe.akka"          %% "akka-testkit"  % "2.6.5" % "test",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "org.specs2"                 %% "specs2-core"   % "4.9.4" % "test",
    "org.scalatest"              %% "scalatest"     % "3.1.2" % "test",
    "org.scodec"                 %% "scodec-core"   % "1.11.7",
    "net.java.dev.jna"            % "jna"           % "5.5.0",
    "com.typesafe.akka"          %% "akka-slf4j"    % "2.6.5",
    "ch.qos.logback"              % "logback-classic" % "1.2.3",
    "org.log4s"                  %% "log4s"         % "1.8.2",
    "org.fusesource.jansi"        % "jansi"         % "1.12",
    "org.scoverage"              %% "scalac-scoverage-plugin" % "1.4.1",
    "com.github.nscala-time"     %% "nscala-time"   % "2.24.0",
    "com.github.postgresql-async" %% "postgresql-async" % "0.3.0",
    "com.github.t3hnar"          %% "scala-bcrypt"  % "4.1",
    "org.ini4j"                   % "ini4j"          % "0.5.4",
    "org.scala-graph"            %% "graph-core"    % "1.13.1",
    "io.kamon"                   %% "kamon-bundle"  % "2.1.0",
    "io.kamon"                   %% "kamon-apm-reporter" % "2.1.0",
    "org.json4s"                 %% "json4s-native" % "3.6.8",
    "com.typesafe.akka"          %% "akka-stream"   % "2.6.5",
  ),
)

lazy val pscryptoSettings = Seq(
  unmanagedClasspath in Test += (baseDirectory in ThisBuild).value / "pscrypto-lib",
  unmanagedClasspath in Runtime += (baseDirectory in ThisBuild).value / "pscrypto-lib",
  unmanagedClasspath in Compile += (baseDirectory in ThisBuild).value / "pscrypto-lib"
)

lazy val psloginPackSettings = Seq(
  packMain := Map("ps-login" -> "PsLogin"),
  packArchivePrefix := "pslogin",
  packExtraClasspath := Map("ps-login" -> Seq("${PROG_HOME}/pscrypto-lib",
    "${PROG_HOME}/config")),
  packResourceDir += (baseDirectory.value / "pscrypto-lib" -> "pscrypto-lib"),
  packResourceDir += (baseDirectory.value / "config" -> "config"),
  packResourceDir += (baseDirectory.value / "pslogin/src/main/resources" -> "config")
)

lazy val root = (project in file(".")).
  configs(QuietTest).
  enablePlugins(PackPlugin).
  settings(commonSettings: _*).
  settings(psloginPackSettings: _*).
  //enablePlugins(ScalaUnidocPlugin).
  aggregate(pslogin, common).
  dependsOn(pslogin, common)

lazy val pslogin = (project in file("pslogin")).
  configs(QuietTest).
  settings(commonSettings: _*).
  settings(
    name := "pslogin",
    // ActorTests have specific timing requirements and will be flaky if run in parallel
    parallelExecution in Test := false,
    // TODO(chord): remove exclusion when WorldSessionActor is refactored: https://github.com/psforever/PSF-LoginServer/issues/279
    coverageExcludedPackages :=  "WorldSessionActor.*;zonemaps.*",
    // Copy all tests from Test -> QuietTest (we're only changing the run options)
    inConfig(QuietTest)(Defaults.testTasks)
  ).
  settings(pscryptoSettings: _*).
  dependsOn(common)

lazy val common = (project in file("common")).
  configs(QuietTest).
  settings(commonSettings: _*).
  settings(
    name := "common",
    // Copy all tests from Test -> QuietTest (we're only changing the run options)
    inConfig(QuietTest)(Defaults.testTasks)
  ).
  settings(pscryptoSettings: _*)

// Special test configuration for really quiet tests (used in CI)
lazy val QuietTest = config("quiet") extend(Test)
