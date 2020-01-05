// Copyright (c) 2017 PSForever
import java.net.InetAddress
import java.io.File
import java.util.Locale

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.RandomPool
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.status._
import ch.qos.logback.core.util.StatusPrinter
import com.typesafe.config.ConfigFactory
import net.psforever.config.{Valid, Invalid}
import net.psforever.crypto.CryptoInterface
import net.psforever.objects.zones._
import net.psforever.objects.guid.TaskResolver
import org.slf4j
import org.fusesource.jansi.Ansi._
import org.fusesource.jansi.Ansi.Color._
import services.ServiceManager
import services.account.AccountIntermediaryService
import services.chat.ChatService
import services.galaxy.GalaxyService
import services.teamwork.SquadService

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

object PsLogin {
  private val logger = org.log4s.getLogger

  var args : Array[String] = Array()
  var config : java.util.Map[String,Object] = null
  implicit var system : ActorSystem = null
  var loginRouter : Props = Props.empty
  var worldRouter : Props = Props.empty
  var loginListener : ActorRef = ActorRef.noSender
  var worldListener : ActorRef = ActorRef.noSender

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
    val procNum = Runtime.getRuntime.availableProcessors();
    val processorString = if(procNum == 1) {
      "Detected 1 available logical processor"
    }
    else {
      s"Detected $procNum available logical processors"
    }

    val freeMemory = Runtime.getRuntime.freeMemory() / 1048576;
    // how much memory has been allocated out of the maximum that can be
    val totalMemory = Runtime.getRuntime.totalMemory() / 1048576;
    // the maximum amount of memory that the JVM can hold before OOM errors
    val maxMemory = Runtime.getRuntime.maxMemory() / 1048576;

    s"""|~~~ System Information ~~~
       |SYS: ${System.getProperty("os.name")} (v. ${System.getProperty("os.version")}, ${System.getProperty("os.arch")})
       |CPU: $processorString
       |MEM: ${maxMemory}MB available to the JVM (tune with -Xmx flag)
       |JVM: ${System.getProperty("java.vm.name")} (build ${System.getProperty("java.version")}), ${System.getProperty("java.vendor")} - ${System.getProperty("java.vendor.url")}
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
      case _ : JoranException => ;
    }

    if(loggerHasErrors(lc)) {
      println("Loading log settings failed")
      StatusPrinter.printInCaseOfErrorsOrWarnings(lc)
      sys.exit(1)
    }
  }

  def loadConfig(configDirectory : String) = {
    val worldConfigFile = configDirectory + File.separator + "worldserver.ini"
    // For fallback when no user-specific config file has been created
    val worldDefaultConfigFile = configDirectory + File.separator + "worldserver.ini.dist"

    val worldConfigToLoad = if ((new File(worldConfigFile)).exists()) {
      worldConfigFile
    } else if ((new File(worldDefaultConfigFile)).exists()) {
      println("WARNING: loading the default worldserver.ini.dist config file")
      println("WARNING: Please create a worldserver.ini file to override server defaults")

      worldDefaultConfigFile
    } else {
      println("FATAL: unable to load any worldserver.ini file")
      sys.exit(1)
    }

    WorldConfig.Load(worldConfigToLoad) match {
      case Valid =>
        println("Loaded world config from " + worldConfigToLoad)
      case i : Invalid =>
        println("FATAL: Error loading config from " + worldConfigToLoad)
        println(WorldConfig.FormatErrors(i).mkString("\n"))
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

  def run() : Unit = {
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

    parseArgs(this.args)

    val loggingConfigFile = configDirectory + File.separator + "logback.xml"

    loadConfig(configDirectory)

    println(s"Initializing logging from $loggingConfigFile...")
    initializeLogging(loggingConfigFile)

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

    logger.info("Starting actor subsystems...")

    /** Make sure we capture Akka messages (but only INFO and above)
      *
      * This same config can be specified in a configuration file, but that's more work at this point.
      * In the future we will have a unified configuration file specific to this server
      */
    config = Map(
      "akka.loggers" -> List("akka.event.slf4j.Slf4jLogger").asJava,
      "akka.loglevel" -> "INFO",
      "akka.logging-filter" -> "akka.event.slf4j.Slf4jLoggingFilter"
    ).asJava

    /** Start up the main actor system. This "system" is the home for all actors running on this server */
    system = ActorSystem("PsLogin", ConfigFactory.parseMap(config))

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
      SessionPipeline("packet-session-", Props[PacketCodingActor]),
      SessionPipeline("login-session-", Props[LoginSessionActor])
    )
    val worldTemplate = List(
      SessionPipeline("crypto-session-", Props[CryptoSessionActor]),
      SessionPipeline("packet-session-", Props[PacketCodingActor]),
      SessionPipeline("world-session-", Props[WorldSessionActor])
    )

    val loginServerPort = WorldConfig.Get[Int]("loginserver.ListeningPort")
    val worldServerPort = WorldConfig.Get[Int]("worldserver.ListeningPort")

    val netSim : Option[NetworkSimulatorParameters] = WorldConfig.Get[Boolean]("developer.NetSim.Active") match {
      case true =>
        val params = NetworkSimulatorParameters(
          WorldConfig.Get[Float]("developer.NetSim.Loss"),
          WorldConfig.Get[Duration]("developer.NetSim.Delay").toMillis,
          WorldConfig.Get[Float]("developer.NetSim.ReorderChance"),
          WorldConfig.Get[Duration]("developer.NetSim.ReorderTime").toMillis
        )
        logger.warn("NetSim is active")
        logger.warn(params.toString)
        Some(params)
      case false => None
    }

    val continentList = createContinents()
    val serviceManager = ServiceManager.boot
    serviceManager ! ServiceManager.Register(Props[AccountIntermediaryService], "accountIntermediary")
    serviceManager ! ServiceManager.Register(RandomPool(50).props(Props[TaskResolver]), "taskResolver")
    serviceManager ! ServiceManager.Register(Props[ChatService], "chat")
    serviceManager ! ServiceManager.Register(Props[GalaxyService], "galaxy")
    serviceManager ! ServiceManager.Register(Props[SquadService], "squad")
    serviceManager ! ServiceManager.Register(Props(classOf[InterstellarCluster], continentList), "cluster")

    /** Create two actors for handling the login and world server endpoints */
    loginRouter = Props(new SessionRouter("Login", loginTemplate))
    worldRouter = Props(new SessionRouter("World", worldTemplate))
    loginListener = system.actorOf(Props(new UdpListener(loginRouter, "login-session-router", LoginConfig.serverIpAddress, loginServerPort, netSim)), "login-udp-endpoint")
    worldListener = system.actorOf(Props(new UdpListener(worldRouter, "world-session-router", LoginConfig.serverIpAddress, worldServerPort, netSim)), "world-udp-endpoint")

    logger.info(s"NOTE: Set client.ini to point to ${LoginConfig.serverIpAddress.getHostAddress}:$loginServerPort")

    // Add our shutdown hook (this works for Control+C as well, but not in Cygwin)
    sys addShutdownHook {
      // TODO: clean up active sessions and close resources safely
      logger.info("Login server now shutting down...")
    }
  }

  def createContinents() : List[Zone] = {
    import Zones._
    List(
      Zone.Nowhere,
      z1, z2, z3, z4, z5, z6, z7, z8, z9, z10,
      home1, tzshtr, tzdrtr, tzcotr,
      home2, tzshnc, tzdrnc, tzconc,
      home3, tzshvs, tzdrvs, tzcovs,
      c1, c2, c3, c4, c5, c6,
      i1, i2, i3, i4
    )
  }

  def main(args : Array[String]) : Unit = {
    Locale.setDefault(Locale.US); // to have floats with dots, not comma...
    this.args = args
    run()

    // Wait forever until the actor system shuts down
    Await.result(system.whenTerminated, Duration.Inf)
  }
}
