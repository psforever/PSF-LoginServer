// Copyright (c) 2016 PSForever.net to present
import akka.actor.{ActorSystem, Props}
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import psforever.crypto.CryptoInterface
import org.log4s._
import org.slf4j
import org.slf4j.LoggerFactory

import org.fusesource.jansi.Ansi._
import org.fusesource.jansi.Ansi.Color._

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

  def main(args : Array[String]) : Unit = {
    banner

    // assume SLF4J is bound to logback in the current environment
    val lc = slf4j.LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

    // print logback's internal status
    StatusPrinter.printInCaseOfErrorsOrWarnings(lc)

    try {
      CryptoInterface.initialize()
      logger.info("PSCrypto initialized")
    }
    catch {
      case e : UnsatisfiedLinkError =>
        logger.error(e)("Unable to initialize " + CryptoInterface.libName)
        sys.exit(1)
    }

    logger.info("Starting actor subsystems...")

    val system = ActorSystem("PsLogin")
    val session = system.actorOf(Props[SessionRouter], "session-router")
    val listener = system.actorOf(Props(new UdpListener(session)), "udp-listener")

    system.awaitTermination()
  }
}
