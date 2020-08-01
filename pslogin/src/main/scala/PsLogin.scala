package net.psforever.pslogin

import java.net.InetAddress
import java.util.Locale

import akka.{actor => classic}
import akka.actor.typed.scaladsl.adapter._
import akka.routing.RandomPool
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import net.psforever.crypto.CryptoInterface
import net.psforever.objects.Default
import net.psforever.objects.zones._
import net.psforever.objects.guid.TaskResolver
import org.slf4j
import org.fusesource.jansi.Ansi._
import org.fusesource.jansi.Ansi.Color._
import services.{InterstellarClusterService, ServiceManager}
import services.account.{AccountIntermediaryService, AccountPersistenceService}
import services.chat.ChatService
import services.galaxy.GalaxyService
import services.teamwork.SquadService
import kamon.Kamon
import org.apache.commons.io.FileUtils
import services.properties.PropertyOverrideManager
import org.flywaydb.core.Flyway
import java.nio.file.Paths

import scopt.OParser
import io.sentry.Sentry
import net.psforever.actors.session.SessionActor
import net.psforever.login.psadmin.PsAdminActor
import net.psforever.login.{
  CryptoSessionActor,
  LoginSessionActor,
  NetworkSimulatorParameters,
  PacketCodingActor,
  SessionPipeline,
  SessionRouter,
  TcpListener,
  UdpListener
}
import net.psforever.util.Config
import net.psforever.zones.Zones

object PsLogin {
  private val logger = org.log4s.getLogger

  case class CliConfig(
      command: String = "run",
      noAutoMigrate: Boolean = false,
      baselineOnMigrate: Boolean = false,
      bind: Option[String] = None
  )

  def printBanner(): Unit = {
    println(ansi().fgBright(BLUE).a("""   ___  ________"""))
    println(ansi().fgBright(BLUE).a("""  / _ \/ __/ __/__  _______ _  _____ ____"""))
    println(ansi().fgBright(MAGENTA).a(""" / ___/\ \/ _// _ \/ __/ -_) |/ / -_) __/"""))
    println(ansi().fgBright(RED).a("""/_/  /___/_/  \___/_/  \__/|___/\__/_/""").reset())
    println("""   Login Server - PSForever Project""")
    println("""        http://psforever.net""")
    println()
  }

  def systemInformation: String = {
    val processors = Runtime.getRuntime.availableProcessors()
    val maxMemory  = FileUtils.byteCountToDisplaySize(Runtime.getRuntime.maxMemory())

    s"""|~~~ System Information ~~~
        |SYS: ${System.getProperty("os.name")} (v. ${System.getProperty("os.version")}, ${System
      .getProperty("os.arch")})
        |CPU: Detected $processors available logical processor${if (processors != 1) "s" else ""}
        |MEM: $maxMemory available to the JVM (tune with -Xmx flag)
        |JVM: ${System.getProperty("java.vm.name")} (build ${System.getProperty("java.version")}), ${System.getProperty(
      "java.vendor"
    )} - ${System.getProperty("java.vendor.url")}
    """.stripMargin
  }

  def run(args: CliConfig): Unit = {
    val bindAddress: InetAddress =
      args.bind match {
        case Some(address) => InetAddress.getByName(address)         // address from first argument
        case None          => InetAddress.getByName(Config.app.bind) // address from config
      }

    if (Config.app.kamon.enable) {
      logger.info("Starting Kamon")
      Kamon.init()
    }

    if (Config.app.sentry.enable) {
      logger.info(s"Enabling Sentry")
      Sentry.init(Config.app.sentry.dsn)
    }

    /** Start up the main actor system. This "system" is the home for all actors running on this server */
    implicit val system = classic.ActorSystem("PsLogin")
    Default(system)

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
      SessionPipeline("crypto-session-", classic.Props[CryptoSessionActor]()),
      SessionPipeline("packet-session-", classic.Props[PacketCodingActor]()),
      SessionPipeline("login-session-", classic.Props[LoginSessionActor]())
    )
    val worldTemplate = List(
      SessionPipeline("crypto-session-", classic.Props[CryptoSessionActor]()),
      SessionPipeline("packet-session-", classic.Props[PacketCodingActor]()),
      SessionPipeline("world-session-", classic.Props[SessionActor]())
    )

    val netSim: Option[NetworkSimulatorParameters] = if (Config.app.developer.netSim.enable) {
      val params = NetworkSimulatorParameters(
        Config.app.developer.netSim.loss,
        Config.app.developer.netSim.delay.toMillis,
        Config.app.developer.netSim.reorderChance,
        Config.app.developer.netSim.reorderTime.toMillis
      )
      logger.warn("NetSim is active")
      logger.warn(params.toString)
      Some(params)
    } else {
      None
    }

    val zones = Zones.zones ++ Seq(Zone.Nowhere)

    system.spawn(ChatService(), ChatService.ChatServiceKey.id)
    system.spawn(InterstellarClusterService(zones), InterstellarClusterService.InterstellarClusterServiceKey.id)

    val serviceManager = ServiceManager.boot
    serviceManager ! ServiceManager.Register(classic.Props[AccountIntermediaryService](), "accountIntermediary")
    serviceManager ! ServiceManager.Register(RandomPool(150).props(classic.Props[TaskResolver]()), "taskResolver")
    serviceManager ! ServiceManager.Register(classic.Props[GalaxyService](), "galaxy")
    serviceManager ! ServiceManager.Register(classic.Props[SquadService](), "squad")
    serviceManager ! ServiceManager.Register(classic.Props[AccountPersistenceService](), "accountPersistence")
    serviceManager ! ServiceManager.Register(classic.Props[PropertyOverrideManager](), "propertyOverrideManager")

    val loginRouter = classic.Props(new SessionRouter("Login", loginTemplate))
    val worldRouter = classic.Props(new SessionRouter("World", worldTemplate))
    val loginListener = system.actorOf(
      classic.Props(new UdpListener(loginRouter, "login-session-router", bindAddress, Config.app.login.port, netSim)),
      "login-udp-endpoint"
    )
    val worldListener = system.actorOf(
      classic.Props(new UdpListener(worldRouter, "world-session-router", bindAddress, Config.app.world.port, netSim)),
      "world-udp-endpoint"
    )

    val adminListener = system.actorOf(
      classic.Props(
        new TcpListener(
          classOf[PsAdminActor],
          "net.psforever.login.psadmin-client-",
          InetAddress.getByName(Config.app.admin.bind),
          Config.app.admin.port
        )
      ),
      "net.psforever.login.psadmin-tcp-endpoint"
    )

    logger.info(
      s"Login server is running on ${InetAddress.getByName(Config.app.public).getHostAddress}:${Config.app.login.port}"
    )

    // Add our shutdown hook (this works for Control+C as well, but not in Cygwin)
    sys addShutdownHook {
      // TODO: clean up active sessions and close resources safely
      logger.info("Login server now shutting down...")
    }
  }

  def flyway(args: CliConfig): Flyway = {
    Flyway
      .configure()
      .dataSource(Config.app.database.toJdbc, Config.app.database.username, Config.app.database.password)
      .baselineOnMigrate(args.baselineOnMigrate)
      .load()
  }

  def migrate(args: CliConfig): Unit = {
    flyway(args).migrate()
  }

  def main(args: Array[String]): Unit = {
    Locale.setDefault(Locale.US); // to have floats with dots, not comma

    printBanner()
    println(systemInformation)

    val loggerConfigPath = Paths.get(Config.directory, "logback.xml").toAbsolutePath.toString
    val loggerContext    = slf4j.LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    val configurator     = new JoranConfigurator()
    configurator.setContext(loggerContext)
    loggerContext.reset()
    configurator.doConfigure(loggerConfigPath)

    Config.result match {
      case Left(failures) =>
        logger.error("Loading config failed")
        failures.toList.foreach { failure =>
          logger.error(failure.toString)
        }
        sys.exit(1)
      case Right(_) =>
    }

    /** Initialize the PSCrypto native library
      *
      * PSCrypto provides PlanetSide specific crypto that is required to communicate with it.
      * It has to be distributed as a native library because there is no Scala version of the required
      * cryptographic primitives (MD5MAC). See https://github.com/psforever/PSCrypto for more information.
      */
    try {
      CryptoInterface.initialize()
    } catch {
      case e: UnsatisfiedLinkError =>
        logger.error("Unable to initialize " + CryptoInterface.libName)
        logger.error(e)(
          "This means that your PSCrypto version is out of date. Get the latest version from the README" +
            " https://github.com/psforever/PSF-LoginServer#downloading-pscrypto"
        )
        sys.exit(1)
      case e: IllegalArgumentException =>
        logger.error("Unable to initialize " + CryptoInterface.libName)
        logger.error(e)(
          "This means that your PSCrypto version is out of date. Get the latest version from the README" +
            " https://github.com/psforever/PSF-LoginServer#downloading-pscrypto"
        )
        sys.exit(1)
    }

    val builder = OParser.builder[CliConfig]

    val parser = {
      import builder._
      OParser.sequence(
        programName("ps-login"),
        opt[Unit]("no-auto-migrate")
          .action((_, c) => c.copy(noAutoMigrate = true))
          .text("Do not auto migrate database."),
        opt[Unit]("baseline-on-migrate")
          .action((_, c) => c.copy(baselineOnMigrate = true))
          .text("Automatically baseline existing databases."),
        cmd("run")
          .action((_, c) => c.copy(command = "run"))
          .text("Run server.")
          .children(
            opt[String]("bind")
              .action((x, c) => c.copy(bind = Some(x)))
              .text("Bind address")
          ),
        cmd("migrate")
          .action((_, c) => c.copy(command = "migrate"))
          .text("Apply database migrations.")
      )
    }

    OParser.parse(parser, args, CliConfig()) match {
      case Some(config) =>
        config.command match {
          case "run" =>
            if (config.noAutoMigrate) {
              flyway(config).validate()
            } else {
              migrate(config)
            }
            run(config)
          case "migrate" =>
            migrate(config)
        }
      case _ =>
        sys.exit(1)
    }

  }
}
