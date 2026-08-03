logLevel := Level.Warn

addSbtPlugin("org.xerial.sbt" % "sbt-pack"          % "0.17")
// scalac-scoverage-plugin is cross-published against the *full* Scala version, and the
// 2.0.7 compiler plugin stops at 2.13.10 -- so `sbt coverage` could not resolve on 2.13.16
// either, and CI's coverage step was already broken before this bump. The current
// sbt-scoverage pulls a compiler plugin that is published for 2.13.18.
addSbtPlugin("org.scoverage"  % "sbt-scoverage"     % "2.4.4")
addSbtPlugin("io.kamon"       % "sbt-kanela-runner" % "2.1.1")
//addSbtPlugin("com.eed3si9n"   % "sbt-unidoc"        % "0.4.3")
addSbtPlugin("org.scalameta"  % "sbt-scalafmt"      % "2.5.0")
addSbtPlugin("ch.epfl.scala"  % "sbt-scalafix"      % "0.14.7")
