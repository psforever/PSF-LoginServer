// Copyright (c) 2016 PSForever.net to present
import java.net.InetAddress

import akka.actor.{ActorSystem, Props}
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.status._
import ch.qos.logback.core.util.StatusPrinter
import com.typesafe.config.ConfigFactory
import net.psforever.crypto.CryptoInterface
import org.slf4j
import org.fusesource.jansi.Ansi._
import org.fusesource.jansi.Ansi.Color._

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

object PsLogin {
  private val logger = org.log4s.getLogger

  def banner() : Unit = {
    println(ansi().fgBright(BLUE).a("""   ___  ________"""))
    println(ansi().fgBright(BLUE).a("""  / _ \/ __/ __/__  _______ _  _____ ____"""))
    println(ansi().fgBright(MAGENTA).a(""" / ___/\ \/ _// _ \/ __/ -_) |/ / -_) __/"""))
    println(ansi().fgBright(RED).a("""/_/  /___/_/  \___/_/  \__/|___/\__/_/""").reset())
    println("""   Login Server - PSForever Project""")
    println("""        http://psforever.net""")
    println
  }

  /** Grabs the most essential system information and returns it as a preformatted string */
  def systemInformation : String = {
    s"""|~~~ System Information ~~~
       |${System.getProperty("os.name")} (v. ${System.getProperty("os.version")}, ${System.getProperty("os.arch")})
       |${System.getProperty("java.vm.name")} (build ${System.getProperty("java.version")}), ${System.getProperty("java.vendor")} - ${System.getProperty("java.vendor.url")}
    """.stripMargin
  }

  /** Used to enumerate all of the Java properties. Used in testing only */
  def enumerateAllProperties() : Unit = {
    val props = System.getProperties
    val enums = props.propertyNames()

    while(enums.hasMoreElements) {
      val key = enums.nextElement.toString
      System.out.println(key + " : " + props.getProperty(key))
    }
  }

  /**
    * Checks the current logger context
    * @param context SLF4J logger context
    * @return Boolean return true if context has errors
    */
  def loggerHasErrors(context : LoggerContext) = {
    val statusUtil = new StatusUtil(context)

    statusUtil.getHighestLevel(0) >= Status.WARN
  }

  /** Loads the logging configuration and starts logging */
  def initializeLogging(logfile : String) : Unit = {
    // assume SLF4J is bound to logback in the current environment
    val lc = slf4j.LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

    try {
      val configurator = new JoranConfigurator()
      configurator.setContext(lc)

      // reset any loaded settings
      lc.reset()
      configurator.doConfigure(logfile)
    }
    catch {
      case je : JoranException => ;
    }

    if(loggerHasErrors(lc)) {
      println("Loading log settings failed")
      StatusPrinter.printInCaseOfErrorsOrWarnings(lc)
      sys.exit(1)
    }
  }

  def main(args : Array[String]) : Unit = {
    // Early start up
    banner()
    println(systemInformation)

    initializeLogging("logback.xml")

    /** Initialize the PSCrypto native library
      *
      * PSCrypto provides PlanetSide specific crypto that is required to communicate with it.
      * It has to be distributed as a native library because there is no Scala version of the required
      * cryptographic primitives (MD5MAC). See https://github.com/psforever/PSCrypto for more information.
      */
    try {
      CryptoInterface.initialize()
      logger.info("PSCrypto initialized")
    }
    catch {
      case e : UnsatisfiedLinkError =>
        logger.error(e)("Unable to initialize " + CryptoInterface.libName)
        CryptoInterface.printEnvironment()
        sys.exit(1)
    }

    // TODO: pluralize "processors"
    logger.info(s"Detected ${Runtime.getRuntime.availableProcessors()} available logical processors")
    logger.info("Starting actor subsystems...")

    /** Make sure we capture Akka messages (but only INFO and above)
      *
      * This same config can be specified in a configuration file, but that's more work at this point.
      * In the future we will have a unified configuration file specific to this server
      */
    val config = Map(
      "akka.loggers" -> List("akka.event.slf4j.Slf4jLogger").asJava,
      "akka.loglevel" -> "INFO",
      "akka.logging-filter" -> "akka.event.slf4j.Slf4jLoggingFilter"
    ).asJava

    /** Start up the main actor system. This "system" is the home for all actors running on this server */
    val system = ActorSystem("PsLogin", ConfigFactory.parseMap(config))

    /** Create pipelines for the login and world servers
      *
      * The first node in the pipe is an Actor that handles the crypto for protecting packets.
      * After any crypto operations have been applied or unapplied, the packets are passed on to the next
      * actor in the chain. For an incoming packet, this is a player session handler. For an outgoing packet
      * this is the session router, which returns the packet to the sending host.
      *
      * See SessionRouter.scala for a diagram
      */
    val loginTemplate = List(
      SessionPipeline("crypto-session-", Props[CryptoSessionActor]),
      SessionPipeline("login-session-", Props[LoginSessionActor])
    )
    val worldTemplate = List(
      SessionPipeline("crypto-session-", Props[CryptoSessionActor]),
      SessionPipeline("world-session-", Props[WorldSessionActor])
    )

    val loginServerPort = 51000
    val worldServerPort = 51001

    /** Create two actors for handling the login and world server endpoints */
    val listener = system.actorOf(Props(new UdpListener(Props(new SessionRouter(loginTemplate)), "login-session-router",
      InetAddress.getLocalHost, loginServerPort)), "login-udp-endpoint")
    val worldListener = system.actorOf(Props(new UdpListener(Props(new SessionRouter(worldTemplate)), "world-session-router",
      InetAddress.getLocalHost, worldServerPort)), "world-udp-endpoint")

    logger.info(s"NOTE: Set client.ini to point to ${InetAddress.getLocalHost.getHostAddress}:$loginServerPort")

    // Wait forever until the actor system shuts down
    Await.result(system.whenTerminated, Duration.Inf)
  }
}
