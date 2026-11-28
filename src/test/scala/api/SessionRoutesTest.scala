// Copyright (c) 2026 PSForever
package api

import java.net.InetAddress
import java.time.Instant

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken, `X-Forwarded-For`}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import net.psforever.api._
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats, JValue}
import org.specs2.mutable._
import org.specs2.specification.AfterAll

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class SessionRoutesTest extends Specification with AfterAll {
  private implicit val system: ActorSystem = ActorSystem("SessionRoutesTest")
  private implicit val formats: Formats    = DefaultFormats
  import system.dispatcher

  // Room for every example this specification runs at once; see SessionServiceTest.
  private val passwords = new PasswordChecks(4, 64)

  override def afterAll(): Unit = {
    passwords.shutdown()
    Await.ready(system.terminate(), 10.seconds)
  }

  private def ip(text: String): InetAddress = IpLiteral.parse(text).get

  private final class Fixture(gmTotpRequired: Boolean = false, freeFailures: Int = 5, proxies: Seq[String] = Nil) {
    var now: Long = 1700000000000L
    val store     = new InMemoryApiStore
    val service = new SessionService(
      store,
      passwords,
      SessionSettings(1.day.toMillis, 7.days.toMillis, gmTotpRequired, "PSForever", bcryptRounds = 4),
      () => now
    )
    val sessions = new SessionApi(service, new SignInThrottle(freeFailures, 900L, 60L, 100), 1.day)
    val route: Route = new PublicApi(
      new CachedValue[Seq[OnlinePlayer]](5.seconds, 60.seconds, () => Future.successful(Nil)),
      "Test Server",
      "development",
      new RateLimiter(600, 600, 100),
      TrustedProxies.parse(proxies).toOption.get,
      5.seconds,
      Some(sessions)
    ).route

    def call(request: HttpRequest, peer: String = "203.0.113.1"): (HttpResponse, String) = {
      val withPeer = request.addAttribute(AttributeKeys.remoteAddress, RemoteAddress(ip(peer)))
      val response = Await.result(Route.toFunction(route).apply(withPeer), 10.seconds)
      (response, Await.result(response.entity.toStrict(5.seconds), 5.seconds).data.utf8String)
    }

    def signIn(
        username: String,
        password: String,
        totp: Option[String] = None,
        peer: String = "203.0.113.1",
        headers: Seq[HttpHeader] = Nil
    ): (HttpResponse, String) = {
      val fields = Seq("username" -> username, "password" -> password) ++ totp.map("totp" -> _)
      val body   = fields.map { case (k, v) => s""""$k":"$v"""" }.mkString("{", ",", "}")
      call(
        HttpRequest(HttpMethods.POST, "/v1/sessions", headers, HttpEntity(ContentTypes.`application/json`, body)),
        peer
      )
    }

    def token(username: String, password: String, totp: Option[String] = None): String = {
      val (response, body) = signIn(username, password, totp)
      response.status mustEqual StatusCodes.Created
      (parse(body) \ "token").extract[String]
    }

    def authed(method: HttpMethod, uri: String, token: String, body: Option[String] = None): (HttpResponse, String) =
      call(
        HttpRequest(
          method,
          uri,
          List(Authorization(OAuth2BearerToken(token))),
          body.map(HttpEntity(ContentTypes.`application/json`, _)).getOrElse(HttpEntity.Empty)
        )
      )

    def code(accountId: Int): String =
      Totp.code(Base32.decode(store.totps(accountId).secret).get, Totp.stepAt(now / 1000L))
  }

  private def header(response: HttpResponse, name: String): Option[String] =
    response.headers.find(_.is(name.toLowerCase)).map(_.value)

  private def json(body: String): JValue = parse(body)

  "POST /v1/sessions" should {
    "issue a token with its expiry, never cacheable" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val (response, body) = f.signIn("player", "hunter2")
      response.status mustEqual StatusCodes.Created
      val token = (json(body) \ "token").extract[String]
      token must beMatching("[A-Za-z0-9_-]{43}")
      Instant.parse((json(body) \ "expires_at").extract[String]).toEpochMilli mustEqual f.now + 1.day.toMillis
      (json(body) \ "idle_timeout_seconds").extract[Long] mustEqual 86400L
      header(response, "Cache-Control") must beSome("no-store")
    }

    "answer a wrong password, an unknown username and a banned account with the same response" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      f.store.addAccount(2, "banned", "hunter2")
      f.store.update(2)(_.copy(inactive = true))
      val answers = Seq(f.signIn("player", "nope"), f.signIn("nobody", "hunter2"), f.signIn("banned", "hunter2"))
      answers.map(_._1.status).distinct mustEqual Seq(StatusCodes.Unauthorized)
      answers.map(_._2).distinct must haveSize(1)
      answers.forall(a => header(a._1, "WWW-Authenticate").exists(_.startsWith("Bearer"))) must beTrue
    }

    "refuse a request without both fields, or that isn't JSON" in {
      val f = new Fixture()
      f.signIn("player", "")._1.status mustEqual StatusCodes.BadRequest
      f.call(
        HttpRequest(HttpMethods.POST, "/v1/sessions", entity = HttpEntity(ContentTypes.`application/json`, "nope"))
      )._1
        .status mustEqual StatusCodes.BadRequest
    }

    "slow down repeated failures with 429 and Retry-After" in {
      val f = new Fixture(freeFailures = 2)
      f.store.addAccount(1, "player", "hunter2")
      (1 to 3).foreach(_ => f.signIn("player", "nope")._1.status mustEqual StatusCodes.Unauthorized)
      val (limited, _) = f.signIn("player", "hunter2")
      limited.status mustEqual StatusCodes.TooManyRequests
      header(limited, "Retry-After") must beSome("1")
    }

    "throttle each player behind the portal separately when the portal is a trusted proxy" in {
      val f      = new Fixture(freeFailures = 1, proxies = Seq("10.0.0.5"))
      val portal = "10.0.0.5"
      def via(client: String, user: String) =
        f.signIn(user, "nope", peer = portal, headers = Seq(`X-Forwarded-For`(RemoteAddress(ip(client)))))
      (1 to 3).foreach(i => via("198.51.100.1", s"guess$i"))
      via("198.51.100.1", "another")._1.status mustEqual StatusCodes.TooManyRequests
      via("198.51.100.2", "another")._1.status mustEqual StatusCodes.Unauthorized
    }
  }

  "GET /v1/me" should {
    "need a token, and say so with a Bearer challenge" in {
      val f             = new Fixture()
      val (response, _) = f.call(HttpRequest(uri = "/v1/me"))
      response.status mustEqual StatusCodes.Unauthorized
      header(response, "WWW-Authenticate").exists(_.startsWith("Bearer")) must beTrue
      f.authed(HttpMethods.GET, "/v1/me", "A" * 43)._1.status mustEqual StatusCodes.Unauthorized
    }

    "describe the signed-in account" in {
      val f = new Fixture(gmTotpRequired = true)
      f.store.addAccount(7, "TheGm", "hunter2", gm = true)
      val (response, body) = f.authed(HttpMethods.GET, "/v1/me", f.token("thegm", "hunter2"))
      response.status mustEqual StatusCodes.OK
      val me = json(body)
      (
        (me \ "id").extract[Int],
        (me \ "username").extract[String],
        (me \ "gm").extract[Boolean],
        (me \ "gm_active").extract[Boolean],
        (me \ "totp_enabled").extract[Boolean],
        (me \ "totp_enrollment_required").extract[Boolean]
      ) mustEqual ((7, "TheGm", true, false, false, true))
    }
  }

  "signing out" should {
    "end this session with DELETE /v1/sessions/current" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val (mine, other) = (f.token("player", "hunter2"), f.token("player", "hunter2"))
      f.authed(HttpMethods.DELETE, "/v1/sessions/current", mine)._1.status mustEqual StatusCodes.NoContent
      f.authed(HttpMethods.GET, "/v1/me", mine)._1.status mustEqual StatusCodes.Unauthorized
      f.authed(HttpMethods.GET, "/v1/me", other)._1.status mustEqual StatusCodes.OK
    }

    "end every session of the account with DELETE /v1/sessions" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val (mine, other) = (f.token("player", "hunter2"), f.token("player", "hunter2"))
      f.authed(HttpMethods.DELETE, "/v1/sessions", mine)._1.status mustEqual StatusCodes.NoContent
      f.authed(HttpMethods.GET, "/v1/me", other)._1.status mustEqual StatusCodes.Unauthorized
    }
  }

  "TOTP over HTTP" should {
    "enrol, confirm, then be required at sign-in" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val token = f.token("player", "hunter2")

      val (started, body) = f.authed(HttpMethods.POST, "/v1/me/totp", token)
      started.status mustEqual StatusCodes.Created
      (json(body) \ "otpauth_uri").extract[String] must startWith("otpauth://totp/PSForever:player?secret=")

      f.authed(HttpMethods.POST, "/v1/me/totp/confirm", token, Some("""{"code":"12"}"""))._1.status mustEqual
        StatusCodes.BadRequest
      f.authed(HttpMethods.POST, "/v1/me/totp/confirm", token, Some(s"""{"code":"${f.code(1)}"}"""))._1.status mustEqual
        StatusCodes.NoContent
      f.authed(HttpMethods.POST, "/v1/me/totp", token)._1.status mustEqual StatusCodes.Conflict

      f.now += 31000
      val (asked, askedBody) = f.signIn("player", "hunter2")
      asked.status mustEqual StatusCodes.Unauthorized
      (json(askedBody) \ "totp_required").extract[Boolean] must beTrue
      f.signIn("player", "hunter2", Some(f.code(1)))._1.status mustEqual StatusCodes.Created
    }
  }

  "requirePermission" should {
    "refuse with 403 a caller without the permission" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      f.store.addAccount(2, "gm", "hunter2", gm = true)
      val gmOnly: Route = f.sessions.authenticated { caller =>
        f.sessions.requirePermission(caller, Permission.Gm)(complete(StatusCodes.NoContent))
      }
      def ask(token: String) =
        Await
          .result(
            Route.toFunction(gmOnly).apply(HttpRequest(headers = List(Authorization(OAuth2BearerToken(token))))),
            10.seconds
          )
          .status
      (ask(f.token("player", "hunter2")), ask(f.token("gm", "hunter2"))) mustEqual
        ((StatusCodes.Forbidden, StatusCodes.NoContent))
    }
  }
}
