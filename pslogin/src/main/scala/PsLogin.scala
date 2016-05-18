// Copyright (c) 2016 PSForever.net to present
import java.net.InetAddress

import akka.actor.{ActorSystem, Props}
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.status._
import ch.qos.logback.core.util.StatusPrinter
import com.typesafe.config.ConfigFactory
import net.psforever.crypto.CryptoInterface
import org.slf4j
import org.fusesource.jansi.Ansi._
import org.fusesource.jansi.Ansi.Color._

import scala.collection.JavaConverters._

object PsLogin {
  private val logger = org.log4s.getLogger

  def banner = {
    println(ansi().fgBright(BLUE).a("""   ___  ________"""))
    println(ansi().fgBright(BLUE).a("""  / _ \/ __/ __/__  _______ _  _____ ____"""))
    println(ansi().fgBright(MAGENTA).a(""" / ___/\ \/ _// _ \/ __/ -_) |/ / -_) __/"""))
    println(ansi().fgBright(RED).a("""/_/  /___/_/  \___/_/  \__/|___/\__/_/""").reset())
    println("""   Login Server - PSForever Project""")
    println
  }

  def loggerHasErrors(context : LoggerContext) = {
    val statusUtil = new StatusUtil(context)

    statusUtil.getHighestLevel(0) >= Status.WARN
  }

  def main(args : Array[String]) : Unit = {
    banner

    // assume SLF4J is bound to logback in the current environment
    val lc = slf4j.LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

    if(loggerHasErrors(lc)) {
      StatusPrinter.printInCaseOfErrorsOrWarnings(lc)
      sys.exit(1)
    }

    try {
      CryptoInterface.initialize()
      logger.info("PSCrypto initialized")
    }
    catch {
      case e : UnsatisfiedLinkError =>
        logger.error(e)("Unable to initialize " + CryptoInterface.libName)
        sys.exit(1)
    }

    logger.info(s"Detected ${Runtime.getRuntime.availableProcessors()} available logical processors")

    logger.info("Starting actor subsystems...")

    val config = Map(
      "akka.loggers" -> List("akka.event.slf4j.Slf4jLogger").asJava,
      "akka.loglevel" -> "INFO",
      "akka.logging-filter" -> "akka.event.slf4j.Slf4jLoggingFilter"
    ).asJava

    //val system = ActorSystem("PsLogin", Some(ConfigFactory.parseMap(config)), None, Some(MDCPropagatingExecutionContextWrapper(ExecutionContext.Implicits.global)))
    val system = ActorSystem("PsLogin", ConfigFactory.parseMap(config))

    val loginTemplate = List(SessionPipeline("crypto-session-", Props[CryptoSessionActor]),
      SessionPipeline("login-session-", Props[LoginSessionActor]))
    val worldTemplate = List(SessionPipeline("crypto-session-", Props[CryptoSessionActor]),
      SessionPipeline("world-session-", Props[WorldSessionActor]))

    val listener = system.actorOf(Props(new UdpListener(Props(new SessionRouter(loginTemplate)), "login-session-router",
      InetAddress.getLocalHost, 51000)), "login-udp-endpoint")
    val worldListener = system.actorOf(Props(new UdpListener(Props(new SessionRouter(worldTemplate)), "world-session-router",
      InetAddress.getLocalHost, 51001)), "world-udp-endpoint")
  }
}
