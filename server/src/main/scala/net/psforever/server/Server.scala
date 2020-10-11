package net.psforever.server

import java.net.{InetAddress, InetSocketAddress}
import java.nio.file.Paths
import java.util.Locale
import java.util.UUID.randomUUID

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import akka.{actor => classic}
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import io.sentry.Sentry
import kamon.Kamon
import net.psforever.actors.net.{LoginActor, MiddlewareActor, SocketActor}
import net.psforever.actors.session.SessionActor
import net.psforever.login.psadmin.PsAdminActor
import net.psforever.login._
import net.psforever.objects.Default
import net.psforever.objects.zones._
import net.psforever.services.account.{AccountIntermediaryService, AccountPersistenceService}
import net.psforever.services.chat.ChatService
import net.psforever.services.galaxy.GalaxyService
import net.psforever.services.properties.PropertyOverrideManager
import net.psforever.services.teamwork.SquadService
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.util.Config
import net.psforever.zones.Zones
import org.apache.commons.io.FileUtils
import org.flywaydb.core.Flyway
import org.fusesource.jansi.Ansi.Color._
import org.fusesource.jansi.Ansi._
import org.slf4j
import scopt.OParser
import akka.actor.typed.scaladsl.adapter._
import net.psforever.packet.PlanetSidePacket

object Server {
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
    println("""   PSForever Server - PSForever Project""")
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
    implicit val system: ActorSystem = classic.ActorSystem("PsLogin")
    Default(system)

    // typed to classic wrappers for login and session actors
    val login = (ref: ActorRef[MiddlewareActor.Command], connectionId: String) => {
      Behaviors.setup[PlanetSidePacket](context => {
        val actor = context.actorOf(classic.Props(new LoginActor(ref, connectionId)), "login")
        Behaviors.receiveMessage(message => {
          actor ! message
          Behaviors.same
        })
      })
    }
    val session = (ref: ActorRef[MiddlewareActor.Command], connectionId: String) => {
      Behaviors.setup[PlanetSidePacket](context => {
        val uuid  = randomUUID().toString
        val actor = context.actorOf(classic.Props(new SessionActor(ref, connectionId)), s"session-${uuid}")
        Behaviors.receiveMessage(message => {
          actor ! message
          Behaviors.same
        })
      })
    }

    val zones = Zones.zones ++ Seq(Zone.Nowhere)

    system.spawn(ChatService(), ChatService.ChatServiceKey.id)
    system.spawn(InterstellarClusterService(zones), InterstellarClusterService.InterstellarClusterServiceKey.id)

    val serviceManager = ServiceManager.boot
    serviceManager ! ServiceManager.Register(classic.Props[AccountIntermediaryService](), "accountIntermediary")
    serviceManager ! ServiceManager.Register(classic.Props[GalaxyService](), "galaxy")
    serviceManager ! ServiceManager.Register(classic.Props[SquadService](), "squad")
    serviceManager ! ServiceManager.Register(classic.Props[AccountPersistenceService](), "accountPersistence")
    serviceManager ! ServiceManager.Register(classic.Props[PropertyOverrideManager](), "propertyOverrideManager")

    system.spawn(SocketActor(new InetSocketAddress(bindAddress, Config.app.login.port), login), "login-socket")
    system.spawn(SocketActor(new InetSocketAddress(bindAddress, Config.app.world.port), session), "world-socket")

    val adminListener = system.actorOf(
      classic.Props(
        new TcpListener(
          classOf[PsAdminActor],
          "psadmin-client-",
          InetAddress.getByName(Config.app.admin.bind),
          Config.app.admin.port
        )
      ),
      "psadmin-tcp-endpoint"
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

    val builder = OParser.builder[CliConfig]

    val parser = {
      import builder._
      OParser.sequence(
        programName("psforever-server"),
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
