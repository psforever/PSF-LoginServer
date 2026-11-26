//import xerial.sbt.pack.PackPlugin._

lazy val psforeverSettings = Seq(
  organization := "net.psforever",
  version := "1.0.2-SNAPSHOT",
  scalaVersion := "2.13.10",
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
  QuietTest / testOptions += Tests.Argument(TestFrameworks.Specs2, "showOnly", "x!"),
  // http://www.scalatest.org/user_guide/using_the_runner
  QuietTest / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oCEHILMNOPQRX"),
  // Write JUnit XML reports so CI can preserve per-test results even when the job fails.
  Test / testOptions += Tests.Argument(
    TestFrameworks.ScalaTest,
    "-u",
    (Test / target).value.getAbsolutePath + "/test-reports"
  ),
  QuietTest / testOptions += Tests.Argument(
    TestFrameworks.ScalaTest,
    "-u",
    (QuietTest / target).value.getAbsolutePath + "/test-reports"
  ),
  // Trick taken from https://groups.google.com/d/msg/scala-user/mxV9ok7J_Eg/kt-LnsrD0bkJ
  // scaladoc flags: https://github.com/scala/scala/blob/2.11.x/src/scaladoc/scala/tools/nsc/doc/Settings.scala
  Compile / doc / scalacOptions ++= Seq(
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
  // Actor tests share global state (Default, ServiceManager, number pools) and rely on
  // message ordering, so they must not run in parallel. This applies to both the normal
  // Test configuration and the QuietTest configuration used by CI.
  Test / parallelExecution := false,
  QuietTest / parallelExecution := false,
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  libraryDependencies ++= Seq(
    // Akka 2.8+ is BSL licensed, do not upgrade
    "com.typesafe.akka"          %% "akka-actor"                 % "2.6.20",
    "com.typesafe.akka"          %% "akka-slf4j"                 % "2.6.20",
    "com.typesafe.akka"          %% "akka-protobuf-v3"           % "2.6.20",
    "com.typesafe.akka"          %% "akka-stream"                % "2.6.20",
    "com.typesafe.akka"          %% "akka-testkit"               % "2.6.20" % "test",
    "com.typesafe.akka"          %% "akka-actor-typed"           % "2.6.20",
    "com.typesafe.akka"          %% "akka-actor-testkit-typed"   % "2.6.20" % "test",
    "com.typesafe.akka"          %% "akka-slf4j"                 % "2.6.20",
    "com.typesafe.akka"          %% "akka-cluster-typed"         % "2.6.20",
    "com.typesafe.akka"          %% "akka-coordination"          % "2.6.20",
    "com.typesafe.akka"          %% "akka-cluster-tools"         % "2.6.20",
    "com.typesafe.akka"          %% "akka-http"                  % "10.2.10",
    "com.typesafe.scala-logging" %% "scala-logging"              % "3.9.5",
    "org.specs2"                 %% "specs2-core"                % "4.20.0" % "test",
    "org.scalatest"              %% "scalatest"                  % "3.2.15" % "test",
    "org.scodec"                 %% "scodec-core"                % "1.11.11",
    "org.scodec"                 %% "scodec-bits"                % "1.2.5",
    "ch.qos.logback"              % "logback-classic"            % "1.2.12",
    "org.log4s"                  %% "log4s"                      % "1.10.0",
    "org.fusesource.jansi"        % "jansi"                      % "2.4.0",
    "com.github.nscala-time"     %% "nscala-time"                % "2.32.0",
    "com.github.t3hnar"          %% "scala-bcrypt"               % "4.3.0",
    "org.scala-graph"            %% "graph-core"                 % "1.13.5",
    "io.kamon"                   %% "kamon-bundle"               % "2.6.1",
    "io.kamon"                   %% "kamon-apm-reporter"         % "2.6.0",
    "org.json4s"                 %% "json4s-native"              % "4.0.6",
    "io.getquill"                %% "quill-jasync-postgres"      % "3.18.0",
    "org.flywaydb"                % "flyway-core"                % "9.16.0",
    "org.postgresql"              % "postgresql"                 % "42.5.4",
    "com.typesafe"                % "config"                     % "1.4.2",
    "com.github.pureconfig"      %% "pureconfig"                 % "0.17.4",
    "com.beachape"               %% "enumeratum"                 % "1.7.2",
    "commons-io"                  % "commons-io"                 % "2.13.0",
    "com.github.scopt"           %% "scopt"                      % "4.1.0",
    "io.sentry"                   % "sentry-logback"             % "6.19.0",
    "io.circe"                   %% "circe-core"                 % "0.14.5",
    "io.circe"                   %% "circe-generic"              % "0.14.5",
    "io.circe"                   %% "circe-parser"               % "0.14.5",
    "org.scala-lang.modules"     %% "scala-parallel-collections" % "1.0.4",
    "org.bouncycastle"            % "bcprov-jdk15on"             % "1.70"
  ),
  dependencyOverrides ++= Seq(
    "com.github.jasync-sql"   % "jasync-postgresql"  % "1.2.3",
    "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2"
  )
  // TODO(chord): remove exclusion when SessionActor is refactored: https://github.com/psforever/PSF-LoginServer/issues/279
  // coverageExcludedPackages := "net\\.psforever\\.actors\\.session\\.SessionActor.*"
)

lazy val psforever = (project in file("."))
  .configs(QuietTest)
  .settings(psforeverSettings: _*)
  .settings(
    name := "psforever",
    // Copy all tests from Test -> QuietTest (we're only changing the run options)
    inConfig(QuietTest)(Defaults.testTasks),
    // Root `clean` cleans every project
    clean := clean
      .dependsOn(Seq("server", "decodePackets", "client", "docs").map(p => LocalProject(p) / clean): _*)
      .value
  )

lazy val server = (project in file("server"))
  .configs(QuietTest)
  .enablePlugins(PackPlugin)
  .settings(psforeverSettings: _*)
  .settings(
    name := "server",
    // Copy all tests from Test -> QuietTest (we're only changing the run options)
    inConfig(QuietTest)(Defaults.testTasks),
    packMain := Map("psforever-server" -> "net.psforever.server.Server"),
    packArchivePrefix := "psforever-server",
    // Memory and collector settings for the packaged launcher. Without them the server takes
    // the JVM defaults, which in a container is a fraction of host RAM. Note the -Xmx in
    // .jvmopts governs sbt, not this script. G1 with an explicit pause target keeps
    // collections short, which matters because a stop-the-world pause stalls every connected
    // session simultaneously.
    packJvmOpts := Map("psforever-server" -> Seq(
      "-Dstacktrace.app.packages=net.psforever",
      "-Dio.netty.eventLoopThreads=4", // Limit DB threads
      "-Dio.netty.allocator.numDirectArenas=4", // Limit allocator arenas
      "-Dio.netty.allocator.maxOrder=9", // 4MB chunks
      "-Xms512m",
      "-Xmx4g",
    )),
    packEnvVars := Map("psforever-server" -> Map("MALLOC_ARENA_MAX" -> "2")), // fewer glibc malloc arenas
    // Collector flags depend on the JDK the launcher finds, so they are chosen at launch: G1 before 21
    // (G1PeriodicGCInterval needs 12+: a concurrent cycle after 60 s idle can uncommit heap, JEP 346;
    // the free ratios default to 40 / 70), generational ZGC from 21 (the default ZGC mode from 23).
    // TrimNativeHeapInterval releases freed native memory back to the system (21+).
    // ZGC never uncommits below -Xms, so -Xms is the floor the heap shrinks to.
    Compile / pack := {
      val dir      = (Compile / pack).value
      val launcher = dir / "bin" / "psforever-server"
      val gcSelect =
        """JAVA_MAJOR=`"$JAVACMD" -version 2>&1 | sed -n 's/.* version "\([0-9.]*\).*/\1/p' | sed 's/^1\.//' | cut -d. -f1`
          |# ZGC shrinking: return unused memory after 30 s rather than 300, and aim to keep the heap under
          |# 2 GB (a target: it grows past it to -Xmx under load, collecting harder while it is over)
          |ZGC_SHRINK="-XX:ZUncommitDelay=30 -XX:SoftMaxHeapSize=2g"
          |case "$JAVA_MAJOR" in
          |  "")        GC_OPTS="" ;; # unknown version: JVM defaults
          |  8|9|10|11) GC_OPTS="-XX:+UseG1GC -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=30" ;;
          |  1[2-9]|20) GC_OPTS="-XX:+UseG1GC -XX:G1PeriodicGCInterval=60000 -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=30" ;;
          |  21|22)     GC_OPTS="-XX:+UseZGC -XX:+ZGenerational $ZGC_SHRINK -XX:TrimNativeHeapInterval=60000" ;;
          |  *)         GC_OPTS="-XX:+UseZGC $ZGC_SHRINK -XX:TrimNativeHeapInterval=60000" ;;
          |esac
          |JAVA_OPTS="$GC_OPTS $JAVA_OPTS"
          |
          |""".stripMargin
      val script = IO.read(launcher)
      if (!script.contains("eval exec")) sys.error(s"$launcher: no `eval exec` line to insert GC selection before")
      IO.write(launcher, script.replaceFirst("eval exec", java.util.regex.Matcher.quoteReplacement(gcSelect) + "eval exec"))
      dir
    },
    packExtraClasspath := Map("psforever-server" -> Seq("${PROG_HOME}/config")),
    packResourceDir += ((psforever / baseDirectory).value / "config" -> "config")
  )
  // The server tests reuse the actor test harness and fixtures that live in the root
  // project's test sources, so the test classpath has to be shared as well.
  .dependsOn(psforever % "compile->compile;test->test")

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
  .enablePlugins(ScalaUnidocPlugin)
  .settings(psforeverSettings: _*)
  .settings(
    name := "psforever"
  )
  .aggregate(psforever, server, decodePackets)
