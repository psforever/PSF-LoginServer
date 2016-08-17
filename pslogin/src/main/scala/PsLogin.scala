// Copyright (c) 2016 PSForever.net to present
import java.net.InetAddress
import java.io.File

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

// Little job to made some data from gcap files
  import scala.io.Source
  import java.io.FileWriter
  import net.psforever.packet.PacketCoding
  import net.psforever.packet.game.{BuildingInfoUpdateMessage, PlanetSideEmpire, PlanetSideGUID, PlanetSideGeneratorState}
  import scodec.bits._

  val FileToRead = "D:\\all-captures-07-13-16\\a0_1.txt"
  val FileToWrite = "D:\\all-captures-07-13-16\\a0_1_W.txt"
  val fw = new FileWriter(FileToWrite, true)

  for (line <- Source.fromFile(FileToRead).getLines()) {
    //fw.write( "//" + line.drop(line.lastIndexOf(' ')) + System.getProperty("line.separator") )

    val string = ByteVector.fromValidHex(line.drop(line.lastIndexOf(' ')))
      PacketCoding.DecodePacket(string).require match {
        case BuildingInfoUpdateMessage(continent_guid: PlanetSideGUID,
        building_guid: PlanetSideGUID,
        ntu_level: Int,
        is_hacked: Boolean,
        empire_hack: PlanetSideEmpire.Value,
        hack_time_remaining: Long,
        empire_own: PlanetSideEmpire.Value,
        unk1: Long,
        generator_state: PlanetSideGeneratorState.Value,
        spawn_tubes_normal: Boolean,
        force_dome_active: Boolean,
        lattice_benefit: Int,
        unk3: Int,
        unk4: Int,
        unk5: Long,
        unk6: Boolean,
        unk7: Int,
        boost_spawn_pain: Boolean,
        boost_generator_pain: Boolean) =>
//          val tata = "sendResponse(PacketCoding.CreateGamePacket(0,BuildingInfoUpdateMessage("+continent_guid+","+building_guid.toString+","+ntu_level+","+is_hacked+",PlanetSideEmpire."+empire_hack+","+hack_time_remaining+",PlanetSideEmpire."+empire_own+","+
//            unk1+",PlanetSideGeneratorState."+generator_state+","+spawn_tubes_normal+","+force_dome_active+","+lattice_benefit+","+unk3+","+unk4+","+unk5+","+unk6+","+
//            unk7+","+boost_spawn_pain+","+boost_generator_pain+")))"
          val tata = continent_guid+","+building_guid.toString+","+ntu_level+","+is_hacked+",PlanetSideEmpire."+empire_hack+","+hack_time_remaining+",PlanetSideEmpire."+empire_own+","+
            unk1+",PlanetSideGeneratorState."+generator_state+","+spawn_tubes_normal+","+force_dome_active+","+lattice_benefit+","+unk3+","+unk4+","+unk5+","+unk6+","+
            unk7+","+boost_spawn_pain+","+boost_generator_pain
          //fw.write( tata + " //" + line.drop(line.lastIndexOf(' ')) + System.getProperty("line.separator") )
          fw.write( tata + System.getProperty("line.separator") )

      }

  }
  fw.close()


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

  def parseArgs(args : Array[String]) : Unit = {
    if(args.length == 1) {
      LoginConfig.serverIpAddress = InetAddress.getByName(args{0})
    }
    else {
      LoginConfig.serverIpAddress = InetAddress.getLocalHost
    }
  }

  def main(args : Array[String]) : Unit = {
    // Early start up
    banner()
    println(systemInformation)

    // Config directory
    // Assume a default of the current directory
    var configDirectory = "config"

    // This is defined when we are running from SBT pack
    if(System.getProperty("prog.home") != null) {
      configDirectory = System.getProperty("prog.home") + File.separator + "config"
    }

    initializeLogging(configDirectory + File.separator + "logback.xml")
    parseArgs(args)

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
        logger.error("Unable to initialize " + CryptoInterface.libName)
        logger.error(e)("This means that your PSCrypto version is out of date. Get the latest version from the README" +
          " https://github.com/psforever/PSF-LoginServer#downloading-pscrypto")
        sys.exit(1)
      case e : IllegalArgumentException =>
        logger.error("Unable to initialize " + CryptoInterface.libName)
        logger.error(e)("This means that your PSCrypto version is out of date. Get the latest version from the README" +
          " https://github.com/psforever/PSF-LoginServer#downloading-pscrypto")
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


    // Uncomment for network simulation
    // TODO: make this config or command flag
    /*
    val netParams = NetworkSimulatorParameters(
      packetLoss = 0.02,
      packetDelay = 500,
      packetReorderingChance = 0.005,
      packetReorderingTime = 400
    )
    */

    /** Create two actors for handling the login and world server endpoints */
    val listener = system.actorOf(Props(new UdpListener(Props(new SessionRouter("Login", loginTemplate)), "login-session-router",
      LoginConfig.serverIpAddress, loginServerPort, None)), "login-udp-endpoint")
    val worldListener = system.actorOf(Props(new UdpListener(Props(new SessionRouter("World", worldTemplate)), "world-session-router",
      LoginConfig.serverIpAddress, worldServerPort, None)), "world-udp-endpoint")

    logger.info(s"NOTE: Set client.ini to point to ${LoginConfig.serverIpAddress.getHostAddress}:$loginServerPort")

    // Add our shutdown hook (this works for Control+C as well, but not in Cygwin)
    sys addShutdownHook {
      // TODO: clean up active sessions and close resources safely
      logger.info("Login server now shutting down...")
    }

    // Wait forever until the actor system shuts down
    Await.result(system.whenTerminated, Duration.Inf)
  }
}
