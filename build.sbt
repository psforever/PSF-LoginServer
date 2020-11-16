import xerial.sbt.pack.PackPlugin._

lazy val psforeverSettings = Seq(
  organization := "net.psforever",
  version := "1.0.2-SNAPSHOT",
  scalaVersion := "2.13.3",
  Global / cancelable := false,
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  scalacOptions := Seq(
    "-unchecked",
    "-feature",
    "-deprecation",
    "-encoding",
    "utf8",
    "-language:postfixOps",
    "-Wunused:imports",
    "-Xmacro-settings:materialize-derivations"
  ),
  // Quiet test options
  // https://github.com/etorreborre/specs2/blob/8305db76c5084e4b3ce5827ce23117f6fb6beee4/common/shared/src/main/scala/org/specs2/main/Report.scala#L94
  // https://etorreborre.github.io/specs2/guide/SPECS2-2.4.17/org.specs2.guide.Runners.html
  testOptions in QuietTest += Tests.Argument(TestFrameworks.Specs2, "showOnly", "x!"),
  // http://www.scalatest.org/user_guide/using_the_runner
  testOptions in QuietTest += Tests.Argument(TestFrameworks.ScalaTest, "-oCEHILMNOPQRX"),
  // Trick taken from https://groups.google.com/d/msg/scala-user/mxV9ok7J_Eg/kt-LnsrD0bkJ
  // scaladoc flags: https://github.com/scala/scala/blob/2.11.x/src/scaladoc/scala/tools/nsc/doc/Settings.scala
  scalacOptions in (Compile, doc) ++= Seq(
    "-groups",
    "-doc-title",
    "PSF-LoginServer - ",
    "-doc-version",
    "master",
    // For non unidoc builds, you may need bd.getName before the template parameter
    "-doc-source-url",
    "https://github.com/psforever/PSF-LoginServer/blob/master/€{FILE_PATH}.scala",
    "-sourcepath",
    baseDirectory.value.getAbsolutePath // needed for scaladoc relative source paths
  ),
  classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  libraryDependencies ++= Seq(
    "com.typesafe.akka"          %% "akka-actor"                 % "2.6.10",
    "com.typesafe.akka"          %% "akka-slf4j"                 % "2.6.10",
    "com.typesafe.akka"          %% "akka-protobuf-v3"           % "2.6.10",
    "com.typesafe.akka"          %% "akka-stream"                % "2.6.10",
    "com.typesafe.akka"          %% "akka-testkit"               % "2.6.10"  % "test",
    "com.typesafe.akka"          %% "akka-actor-typed"           % "2.6.10",
    "com.typesafe.akka"          %% "akka-cluster-typed"         % "2.6.10",
    "com.typesafe.akka"          %% "akka-coordination"          % "2.6.10",
    "com.typesafe.akka"          %% "akka-cluster-tools"         % "2.6.10",
    "com.typesafe.akka"          %% "akka-slf4j"                 % "2.6.10",
    "com.typesafe.scala-logging" %% "scala-logging"              % "3.9.2",
    "org.specs2"                 %% "specs2-core"                % "4.10.5" % "test",
    "org.scalatest"              %% "scalatest"                  % "3.2.2"  % "test",
    "org.scodec"                 %% "scodec-core"                % "1.11.7",
    "ch.qos.logback"              % "logback-classic"            % "1.2.3",
    "org.log4s"                  %% "log4s"                      % "1.9.0",
    "org.fusesource.jansi"        % "jansi"                      % "1.18",
    "org.scoverage"              %% "scalac-scoverage-plugin"    % "1.4.2",
    "com.github.nscala-time"     %% "nscala-time"                % "2.26.0",
    "com.github.t3hnar"          %% "scala-bcrypt"               % "4.3.0",
    "org.scala-graph"            %% "graph-core"                 % "1.13.2",
    "io.kamon"                   %% "kamon-bundle"               % "2.1.8",
    "io.kamon"                   %% "kamon-apm-reporter"         % "2.1.8",
    "org.json4s"                 %% "json4s-native"              % "3.6.10",
    "io.getquill"                %% "quill-jasync-postgres"      % "3.5.3",
    "org.flywaydb"                % "flyway-core"                % "7.2.0",
    "org.postgresql"              % "postgresql"                 % "42.2.18",
    "com.typesafe"                % "config"                     % "1.4.1",
    "com.github.pureconfig"      %% "pureconfig"                 % "0.14.0",
    "com.beachape"               %% "enumeratum"                 % "1.6.1",
    "joda-time"                   % "joda-time"                  % "2.10.8",
    "commons-io"                  % "commons-io"                 % "2.8.0",
    "com.github.scopt"           %% "scopt"                      % "4.0.0-RC2",
    "io.sentry"                   % "sentry-logback"             % "1.7.30",
    "io.circe"                   %% "circe-core"                 % "0.13.0",
    "io.circe"                   %% "circe-generic"              % "0.13.0",
    "io.circe"                   %% "circe-parser"               % "0.13.0",
    "org.scala-lang.modules"     %% "scala-parallel-collections" % "0.2.0",
    "org.bouncycastle"            % "bcprov-jdk15on"             % "1.67"
  ),
  // TODO(chord): remove exclusion when SessionActor is refactored: https://github.com/psforever/PSF-LoginServer/issues/279
  coverageExcludedPackages := "net\\.psforever\\.actors\\.session\\.SessionActor.*"
)

lazy val psforever = (project in file("."))
  .configs(QuietTest)
  .settings(psforeverSettings: _*)
  .settings(
    name := "psforever",
    // Copy all tests from Test -> QuietTest (we're only changing the run options)
    inConfig(QuietTest)(Defaults.testTasks)
  )

lazy val server = (project in file("server"))
  .configs(QuietTest)
  .enablePlugins(PackPlugin)
  .settings(psforeverSettings: _*)
  .settings(
    name := "server",
    // ActorTests have specific timing requirements and will be flaky if run in parallel
    parallelExecution in Test := false,
    // Copy all tests from Test -> QuietTest (we're only changing the run options)
    inConfig(QuietTest)(Defaults.testTasks),
    packMain := Map("psforever-server" -> "net.psforever.server.Server"),
    packArchivePrefix := "psforever-server",
    packJvmOpts := Map("psforever-server" -> Seq("-Dstacktrace.app.packages=net.psforever")),
    packExtraClasspath := Map("psforever-server" -> Seq("${PROG_HOME}/config")),
    packResourceDir += (baseDirectory.in(psforever).value / "config" -> "config")
  )
  .dependsOn(psforever)

lazy val decodePackets = (project in file("tools/decode-packets"))
  .enablePlugins(PackPlugin)
  .settings(psforeverSettings: _*)
  .settings(
    packMain := Map("psforever-decode-packets" -> "net.psforever.tools.decodePackets.DecodePackets")
  )
  .dependsOn(psforever)

lazy val client = (project in file("tools/client"))
  .enablePlugins(PackPlugin)
  .settings(psforeverSettings: _*)
  .dependsOn(psforever)

// Special test configuration for really quiet tests (used in CI)
lazy val QuietTest = config("quiet") extend Test

lazy val docs = (project in file("docs"))
  .settings(psforeverSettings: _*)
  .enablePlugins(ScalaUnidocPlugin)
  .settings(
    name := "psforever"
  )
  .aggregate(psforever, server, decodePackets)
