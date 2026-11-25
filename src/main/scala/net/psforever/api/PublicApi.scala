package net.psforever.api

import java.net.InetAddress

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.`Retry-After`
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Directive1, Route}
import net.psforever.api.ApiDirectives._

import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

/**
  * One character online right now.
  *
  * @param name       the character's name
  * @param faction_id the empire, as `PlanetSideEmpire` numbers it (0 TR, 1 NC, 2 VS)
  */
final case class OnlinePlayer(name: String, faction_id: Int)

/**
  * The public surface of the PSF-HTTP API: anonymous, read-only, rate-limited and cached.
  *
  * Everything here is safe to serve to the internet. Nothing needs a credential, nothing changes the
  * world, and every route reads the same cached player list, so request volume never reaches the game
  * world more than once per cache period. Callers include the web portal, the launcher and third-party
  * tools; there is no browser CORS, because browsers reach this data through the portal.
  *
  * @param players    the online players, cached; see [[CachedValue]]
  * @param serverName the server's name as the server browser shows it
  * @param serverType released, beta or development
  * @param limiter    the per-address rate limit
  * @param proxies    which peers may report a client address in `X-Forwarded-For`
  * @param cacheTtl   how long a response may be reused, advertised to caches downstream
  */
final class PublicApi(
    players: CachedValue[Seq[OnlinePlayer]],
    serverName: String,
    serverType: String,
    limiter: RateLimiter,
    proxies: TrustedProxies,
    cacheTtl: FiniteDuration
) {
  private val log = org.log4s.getLogger

  /**
    * The client's address, believing `X-Forwarded-For` only from a trusted proxy.
    *
    * Every `X-Forwarded-For` header is read, in order, as one list. Some proxies (HAProxy's `forwardfor`)
    * add their own header after whatever the client sent rather than appending to it, so reading only the
    * first header would hand the client the choice of address.
    */
  private val clientAddress: Directive1[InetAddress] =
    (peerAddress & extractRequest).tmap {
      case (peer, request) =>
        val forwarded = request.headers.filter(_.is("x-forwarded-for")).map(_.value)
        proxies.clientOf(peer, Option.when(forwarded.nonEmpty)(forwarded.mkString(",")))
    }

  /** Refuse with 429 once this address has used up its allowance. */
  private def rateLimited(client: InetAddress): Directive0 =
    limiter.acquire(client) match {
      case None => pass
      case Some(retryAfter) =>
        complete(
          error(StatusCodes.TooManyRequests, "too many requests")
            .withHeaders(`Retry-After`(retryAfter))
        )
    }

  /** Answer from the cached player list, or 503 if the world has never answered. */
  private def withPlayers(render: Seq[OnlinePlayer] => Any): Route =
    onComplete(players.get()) {
      case Success(list) => publicCache(cacheTtl.toSeconds)(complete(json(StatusCodes.OK, render(list))))
      case Failure(e) =>
        log.warn(s"PSF-HTTP API could not read the player list: ${e.getMessage}")
        complete(error(StatusCodes.ServiceUnavailable, "world unavailable"))
    }

  val route: Route =
    surface {
      clientAddress { client =>
        rateLimited(client) {
          pathPrefix("v1") {
            concat(
              // The shape PSAdmin's `list_players` answered with, so existing callers change transport only.
              path("players") {
                get(withPlayers(list => Map("player_count" -> list.size, "player_list" -> list)))
              },
              path("status") {
                get(withPlayers { list =>
                  Map(
                    "server_name"  -> serverName,
                    "server_type"  -> serverType,
                    "player_count" -> list.size,
                    "population"   -> PublicApi.population(list)
                  )
                })
              }
            )
          }
        }
      }
    }
}

object PublicApi {

  /** Players online per empire, always naming all three. */
  def population(players: Seq[OnlinePlayer]): Map[String, Int] = {
    val counts = players.groupBy(_.faction_id).view.mapValues(_.size).toMap
    Map("TR" -> counts.getOrElse(0, 0), "NC" -> counts.getOrElse(1, 0), "VS" -> counts.getOrElse(2, 0))
  }
}
