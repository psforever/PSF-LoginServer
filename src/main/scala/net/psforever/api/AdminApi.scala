package net.psforever.api

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import akka.http.scaladsl.model.headers.{Authorization, HttpChallenges, OAuth2BearerToken, `WWW-Authenticate`}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import com.typesafe.config.{ConfigRenderOptions, ConfigValueFactory, Config => TypesafeConfig}
import net.psforever.api.ApiDirectives._

import scala.jdk.CollectionConverters._

/**
  * The admin surface of the PSF-HTTP API: the operations the old PSAdmin protocol offered besides its
  * player list, which moved to the public surface.
  *
  * This surface is never meant to face the internet. It listens on its own address, loopback by default,
  * and every request needs the bearer token, even on loopback: a web page open on the same machine can
  * send requests to a loopback port, but cannot attach an `Authorization` header to them without the
  * browser asking this server first, which it never agrees to. Every request is logged with its peer,
  * refused ones included.
  *
  * @param token    the bearer token every request must carry
  * @param config   the running configuration to report, before redaction
  * @param shutdown stops the server; called after the response has been sent
  */
final class AdminApi(token: String, config: () => TypesafeConfig, shutdown: () => Unit) {
  private val log = org.log4s.getLogger

  private val expected: Array[Byte] = token.getBytes(StandardCharsets.UTF_8)

  /** Require the bearer token, compared in constant time. */
  private val authorized: Directive0 =
    (optionalHeaderValueByType(Authorization) & peerAddress & extractUri).tflatMap {
      case (Some(Authorization(OAuth2BearerToken(given))), _, _)
          if MessageDigest.isEqual(given.getBytes(StandardCharsets.UTF_8), expected) =>
        pass
      case (_, peer, uri) =>
        log.warn(s"PSF-HTTP admin: refused ${uri.path} from ${peer.getHostAddress} without a valid token")
        complete(
          error(StatusCodes.Unauthorized, "a valid bearer token is required")
            .withHeaders(`WWW-Authenticate`(HttpChallenges.oAuth2("psforever-admin")))
        )
    }

  /** Log every admin request that got past authorisation. */
  private def audited(action: String): Directive0 =
    peerAddress.flatMap { peer =>
      log.info(s"PSF-HTTP admin: $action from ${peer.getHostAddress}")
      pass
    }

  val route: Route =
    surface {
      authorized {
        pathPrefix("v1" / "admin") {
          concat(
            path("config") {
              get {
                audited("config") {
                  complete(
                    HttpResponse(
                      StatusCodes.OK,
                      entity = HttpEntity(
                        ContentTypes.`application/json`,
                        AdminApi.redacted(config()).root().render(ConfigRenderOptions.concise())
                      )
                    )
                  )
                }
              }
            },
            path("threads") {
              get {
                audited("threads") {
                  val traces = Thread.getAllStackTraces.asScala.toSeq.map {
                    case (thread, trace) => s"Thread ${thread.getId} - ${thread.getName}\n${trace.mkString("\n")}"
                  }
                  complete(json(StatusCodes.OK, Map("message" -> s"Dump of ${traces.size} threads", "trace" -> traces)))
                }
              }
            },
            path("shutdown") {
              post {
                audited("shutdown") {
                  mapResponse { response =>
                    shutdown()
                    response
                  } {
                    complete(json(StatusCodes.Accepted, Map("message" -> "Shutting down")))
                  }
                }
              }
            }
          )
        }
      }
    }
}

object AdminApi {

  /** Any configuration path naming one of these holds a credential. */
  private val Secret = "(?i)(password|secret|token|api-key|apikey|dsn)".r

  val RedactedValue = "<redacted>"

  /**
    * The configuration with every credential replaced. PSAdmin's `dump_config` returned the database
    * password; a token-protected surface on a private network is still no reason to hand it out.
    */
  def redacted(config: TypesafeConfig): TypesafeConfig =
    config
      .entrySet()
      .asScala
      .map(_.getKey)
      .filter(key => Secret.findFirstIn(key).isDefined)
      .foldLeft(config)((c, key) => c.withValue(key, ConfigValueFactory.fromAnyRef(RedactedValue)))
}
