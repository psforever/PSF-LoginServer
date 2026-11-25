package net.psforever.api

import java.net.InetAddress

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorSystem => ClassicActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.ServerSettings
import akka.util.Timeout
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.util.{ApiConfig, Config}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
  * Starts the PSF-HTTP API, which replaces the raw-TCP PSAdmin protocol.
  *
  * Two listeners, so the two surfaces can be exposed independently: [[PublicApi]] may face the internet,
  * [[AdminApi]] must not, and only starts once it has a token. The configuration is checked before either
  * binds, and any problem stops the server, as a failure to bind always has.
  */
object ApiServer {
  private val log = org.log4s.getLogger

  /** The admin token's minimum length. */
  val MinimumTokenLength = 32

  /** Characters a bearer token may hold (RFC 6750 token68, without the `=` padding). */
  private val TokenCharacters = "^[A-Za-z0-9._~+/-]+$".r

  /** A public value is served this many times past its cache period while the world cannot be read. */
  val MaxStalePeriods = 12

  @volatile private var failed = false

  /** Whether the API stopped the server because it could not start; the process exits non-zero then. */
  def failedToStart: Boolean = failed

  /**
    * A checked configuration.
    *
    * @param proxies    the parsed trusted proxies
    * @param adminToken the admin token in force, if any
    * @param warnings   problems worth logging that do not stop the server
    */
  final case class Plan(proxies: TrustedProxies, adminToken: Option[String], warnings: Seq[String])

  /**
    * Check the configuration, reporting every problem at once.
    *
    * @param legacyAdminBlock whether the configuration still has PSAdmin's `admin { }` block
    * @param isLoopback       whether a bind address is loopback; replaceable in tests
    */
  def plan(
      config: ApiConfig,
      legacyAdminBlock: Boolean = false,
      isLoopback: String => Boolean = loopback
  ): Either[Seq[String], Plan] = {
    val proxies = TrustedProxies.parse(config.trustedProxies)
    val admin   = config.admin
    val token   = Option(admin.token).map(_.trim).filter(_.nonEmpty)
    val limit   = config.rateLimit

    val errors = Seq(
      proxies.left.toOption,
      Option.when(limit.requestsPerMinute <= 0)("api.rate-limit.requests-per-minute must be positive"),
      Option.when(limit.burst <= 0)("api.rate-limit.burst must be positive"),
      Option.when(limit.maxTrackedAddresses <= 0)("api.rate-limit.max-tracked-addresses must be positive"),
      Option.when(config.cacheTtl < 1.second)("api.cache-ttl must be at least one second"),
      Option.when(token.nonEmpty && admin.port == config.public.port)(
        s"api.admin.port and api.public.port are both ${admin.port}; the two surfaces need separate ports"
      ),
      Option.when(token.exists(_.length < MinimumTokenLength))(
        s"api.admin.token must be at least $MinimumTokenLength characters"
      ),
      Option.when(token.exists(t => TokenCharacters.findFirstIn(t).isEmpty))(
        "api.admin.token may hold only letters, digits and - . _ ~ + /"
      )
    ).flatten

    val warnings = Seq(
      Option.when(!isLoopback(config.public.bind) && config.trustedProxies.isEmpty)(
        s"the public PSF-HTTP API on ${config.public.bind} has no trusted proxy; serve it through a reverse " +
          "proxy that terminates TLS and list that proxy in api.trusted-proxies"
      ),
      Option.when(token.nonEmpty && !isLoopback(admin.bind))(
        s"the admin PSF-HTTP API is bound to ${admin.bind}; make sure that address is on a private network"
      ),
      Option.when(legacyAdminBlock)(
        "the `admin { }` settings belonged to the removed PSAdmin protocol and are ignored; " +
          "use api.public for what callers read and api.admin for administration"
      )
    ).flatten

    if (errors.nonEmpty) Left(errors)
    else Right(Plan(proxies.toOption.get, token, warnings))
  }

  /** Start both surfaces, or stop the server if the configuration is unusable or a bind fails. */
  def start(config: ApiConfig)(implicit system: ClassicActorSystem): Unit =
    plan(config, legacyAdminBlock = Config.config.hasPath("admin")) match {
      case Left(errors) =>
        errors.foreach(e => log.error(s"PSF-HTTP API configuration: $e"))
        fail()
      case Right(plan) =>
        plan.warnings.foreach(w => log.warn(s"PSF-HTTP API: $w"))
        val players = new CachedValue[Seq[OnlinePlayer]](
          config.cacheTtl,
          config.cacheTtl * MaxStalePeriods.toLong,
          () => onlinePlayers()
        )(system.dispatcher)
        val public = new PublicApi(
          players,
          Config.app.world.serverName,
          Config.app.world.serverType.name,
          new RateLimiter(
            config.rateLimit.requestsPerMinute,
            config.rateLimit.burst,
            config.rateLimit.maxTrackedAddresses
          ),
          plan.proxies,
          config.cacheTtl
        )
        bind("public", config.public.bind, config.public.port, public.route)
        plan.adminToken match {
          case Some(token) =>
            val admin = new AdminApi(
              token,
              () => Config.config,
              () => system.scheduler.scheduleOnce(1.second)(system.terminate())(system.dispatcher)
            )
            bind("admin", config.admin.bind, config.admin.port, admin.route)
          case None =>
            log.info("PSF-HTTP API (admin) is off; set api.admin.token to turn it on")
        }
    }

  private def fail()(implicit system: ClassicActorSystem): Unit = {
    failed = true
    system.terminate()
  }

  /**
    * Server settings for both listeners. The peer address is recorded on each request so the rate limit
    * and the admin log can use it, and requests are kept small and short: the API takes no uploads. HEAD is
    * answered as GET without a body, and no `Server` header advertises the akka-http version. Idle
    * connections are closed sooner than akka's default, so one caller holding many sockets open ties up
    * fewer of them; a reverse proxy in front is still what limits connections properly.
    */
  def settings(implicit system: ClassicActorSystem): ServerSettings = {
    val base = ServerSettings(system)
    base
      .withRemoteAddressAttribute(true)
      .withServerHeader(None)
      .withTransparentHeadRequests(true)
      .withParserSettings(base.parserSettings.withMaxContentLength(4096))
      .withTimeouts(base.timeouts.withRequestTimeout(10.seconds).withIdleTimeout(15.seconds))
  }

  private def bind(surface: String, address: String, port: Int, route: Route)(implicit
      system: ClassicActorSystem
  ): Unit = {
    import system.dispatcher
    Http()
      .newServerAt(address, port)
      .withSettings(settings)
      .bind(route)
      .onComplete {
        case Success(binding) => log.info(s"PSF-HTTP API ($surface) listening on ${binding.localAddress}")
        case Failure(e) =>
          log.error(s"PSF-HTTP API ($surface) failed to bind to $address:$port: ${e.getMessage}")
          fail()
      }
  }

  /** Ask the interstellar cluster who is online. */
  private def onlinePlayers()(implicit system: ClassicActorSystem): Future[Seq[OnlinePlayer]] = {
    implicit val typed: akka.actor.typed.ActorSystem[Nothing] = system.toTyped
    implicit val timeout: Timeout                             = 5.seconds
    import system.dispatcher
    val key = InterstellarClusterService.InterstellarClusterServiceKey
    ServiceManager.receptionist
      .ask[Receptionist.Listing](Receptionist.Find(key, _))
      .flatMap { listing =>
        listing.serviceInstances(key).headOption match {
          case Some(cluster) =>
            cluster.ask[InterstellarClusterService.PlayersResponse](InterstellarClusterService.GetPlayers)
          case None => Future.failed(new IllegalStateException("the interstellar cluster is not running"))
        }
      }
      .map(_.players.map(a => OnlinePlayer(a.name, a.faction.id)).sortBy(_.name))
  }

  /** Whether a configured bind address is loopback. Names are resolved here, once, at startup. */
  private def loopback(address: String): Boolean =
    Try(InetAddress.getByName(address).isLoopbackAddress).getOrElse(false)
}
