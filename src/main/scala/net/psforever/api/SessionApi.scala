package net.psforever.api

import java.net.InetAddress
import java.time.Instant

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{
  Authorization,
  HttpChallenges,
  OAuth2BearerToken,
  `Retry-After`,
  `WWW-Authenticate`
}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Directive1, Route}
import net.psforever.api.ApiDirectives._
import org.json4s.JValue
import org.json4s.native.JsonMethods.parse

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

/**
  * Sign-in sessions on the public surface, for the web portal acting on a player's behalf.
  *
  * - `POST /v1/sessions` signs in with `username`, `password` and, for an account that uses TOTP, `totp`.
  * - `DELETE /v1/sessions/current` signs this session out; `DELETE /v1/sessions` signs out everywhere.
  * - `GET /v1/me` describes the signed-in account.
  * - `POST /v1/me/totp` starts TOTP enrolment, `POST /v1/me/totp/confirm` finishes it with a first code,
  *   and `POST /v1/me/totp/disable` turns it off with a current code.
  *
  * Every route but sign-in takes the session token as `Authorization: Bearer <token>`. Sign-ins, and
  * wrong codes when confirming or disabling TOTP, go through the [[SignInThrottle]]. Because the portal
  * makes these calls for every player, it must be listed in `api.trusted-proxies` and send
  * `X-Forwarded-For`, or every player shares one address's throttle and rate limit.
  */
final class SessionApi(service: SessionService, throttle: SignInThrottle, idleTimeout: FiniteDuration) {

  /** The longest username or password accepted, well past anything real and far short of the body cap. */
  private val MaxFieldLength = 128

  private val invalidCredentials = "invalid username or password"

  private def unauthorized(message: String) =
    complete(
      error(StatusCodes.Unauthorized, message)
        .withHeaders(`WWW-Authenticate`(HttpChallenges.oAuth2("psforever")))
    )

  private def throttled(seconds: Long) =
    complete(error(StatusCodes.TooManyRequests, "too many failed attempts").withHeaders(`Retry-After`(seconds)))

  private val jsonBody: Directive1[JValue] =
    entity(as[String]).flatMap { text =>
      Try(parse(text)).toOption match {
        case Some(body) => provide(body)
        case None       => complete(error(StatusCodes.BadRequest, "the request body must be JSON"))
      }
    }

  private def field(body: JValue, name: String): Option[String] =
    (body \ name).extractOpt[String].filter(_.length <= MaxFieldLength)

  /** The bearer token sent with the request. */
  private val bearerToken: Directive1[String] =
    optionalHeaderValueByType(Authorization).flatMap {
      case Some(Authorization(OAuth2BearerToken(token))) => provide(token)
      case _                                             => unauthorized("a session token is required")
    }

  /** The signed-in caller, or 401. */
  val authenticated: Directive1[Caller] =
    bearerToken.flatMap { token =>
      onSuccess(service.authenticate(token)).flatMap {
        case Some(caller) => provide(caller)
        case None         => unauthorized("the session has ended; sign in again")
      }
    }

  /** Refuse with 403 unless the caller has this permission. */
  def requirePermission(caller: Caller, permission: Permission): Directive0 =
    if (caller.has(permission)) pass
    else complete(error(StatusCodes.Forbidden, s"this needs the ${permission.name} permission"))

  private def meJson(me: Me): Map[String, Any] =
    Map(
      "id"                       -> me.id,
      "username"                 -> me.username,
      "gm"                       -> me.gm,
      "gm_active"                -> me.gmActive,
      "totp_enabled"             -> me.totpEnabled,
      "totp_enrollment_required" -> me.totpEnrollmentRequired
    )

  private def signIn(client: InetAddress): Route =
    jsonBody { body =>
      (field(body, "username").map(_.trim).filter(_.nonEmpty), field(body, "password").filter(_.nonEmpty)) match {
        case (Some(username), Some(password)) =>
          throttle.check(client, username) match {
            case Some(wait) => throttled(wait)
            case None =>
              onSuccess(service.signIn(username, password, field(body, "totp"))) {
                case SignInResult.SignedIn(token, session) =>
                  throttle.success(username)
                  complete(
                    json(
                      StatusCodes.Created,
                      Map(
                        "token"                -> token,
                        "expires_at"           -> Instant.ofEpochMilli(service.expiresAt(session)).toString,
                        "idle_timeout_seconds" -> idleTimeout.toSeconds
                      )
                    )
                  )
                case SignInResult.InvalidCredentials =>
                  throttle.failure(client, username)
                  unauthorized(invalidCredentials)
                case SignInResult.TotpRequired =>
                  complete(
                    json(
                      StatusCodes.Unauthorized,
                      Map("message" -> "a two-factor code is required", "error" -> true, "totp_required" -> true)
                    )
                  )
                case SignInResult.TotpInvalid =>
                  throttle.failure(client, username)
                  complete(
                    json(
                      StatusCodes.Unauthorized,
                      Map("message" -> "invalid two-factor code", "error" -> true, "totp_required" -> true)
                    )
                  )
                case SignInResult.Busy =>
                  complete(error(StatusCodes.ServiceUnavailable, "too many sign-ins in progress; try again shortly"))
              }
          }
        case _ => complete(error(StatusCodes.BadRequest, "username and password are required"))
      }
    }

  /** A TOTP code check made while signed in, throttled like a sign-in. */
  private def withCode(client: InetAddress, caller: Caller)(check: String => scala.concurrent.Future[Boolean]): Route =
    jsonBody { body =>
      field(body, "code") match {
        case None => complete(error(StatusCodes.BadRequest, "code is required"))
        case Some(code) =>
          throttle.check(client, caller.account.username) match {
            case Some(wait) => throttled(wait)
            case None =>
              onSuccess(check(code)) {
                case true => complete(StatusCodes.NoContent)
                case false =>
                  throttle.failure(client, caller.account.username)
                  complete(error(StatusCodes.BadRequest, "invalid code"))
              }
          }
      }
    }

  /** The session routes, already inside `/v1`, for a request from `client`. */
  def route(client: InetAddress): Route =
    concat(
      path("sessions") {
        concat(
          post(signIn(client)),
          delete {
            authenticated { caller =>
              onSuccess(service.signOutEverywhere(caller))(complete(StatusCodes.NoContent))
            }
          }
        )
      },
      path("sessions" / "current") {
        delete {
          bearerToken { token =>
            onSuccess(service.signOut(token))(complete(StatusCodes.NoContent))
          }
        }
      },
      pathPrefix("me") {
        authenticated { caller =>
          requirePermission(caller, Permission.SignedIn) {
            concat(
              pathEnd {
                get {
                  onSuccess(service.me(caller))(me => complete(json(StatusCodes.OK, meJson(me))))
                }
              },
              path("totp") {
                post {
                  onSuccess(service.beginTotp(caller)) {
                    case Some(enrollment) =>
                      complete(
                        json(
                          StatusCodes.Created,
                          Map("secret" -> enrollment.secret, "otpauth_uri" -> enrollment.provisioningUri)
                        )
                      )
                    case None => complete(error(StatusCodes.Conflict, "two-factor sign-in is already on"))
                  }
                }
              },
              path("totp" / "confirm") {
                post(withCode(client, caller)(code => service.confirmTotp(caller, code)))
              },
              path("totp" / "disable") {
                post(withCode(client, caller)(code => service.disableTotp(caller, code)))
              }
            )
          }
        }
      }
    )
}
