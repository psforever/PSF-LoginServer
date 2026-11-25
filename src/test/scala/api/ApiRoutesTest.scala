// Copyright (c) 2026 PSForever
package api

import java.net.InetAddress
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken, RawHeader, `X-Forwarded-For`}
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import net.psforever.api._
import org.json4s.native.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import org.specs2.mutable._
import org.specs2.specification.AfterAll

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ApiRoutesTest extends Specification with AfterAll {
  private implicit val system: ActorSystem = ActorSystem("ApiRoutesTest")
  private implicit val formats: Formats    = DefaultFormats
  import system.dispatcher

  override def afterAll(): Unit = Await.ready(system.terminate(), 10.seconds)

  private def ip(text: String): InetAddress = IpLiteral.parse(text).get

  /** Run a request through a route as if it arrived from `peer`. */
  private def call(route: Route, request: HttpRequest, peer: String = "203.0.113.1"): (HttpResponse, String) = {
    val withPeer = request.addAttribute(AttributeKeys.remoteAddress, RemoteAddress(ip(peer)))
    val response = Await.result(Route.toFunction(route).apply(withPeer), 5.seconds)
    val body     = Await.result(response.entity.toStrict(5.seconds), 5.seconds).data.utf8String
    (response, body)
  }

  private def header(response: HttpResponse, name: String): Option[String] =
    response.headers.find(_.is(name.toLowerCase)).map(_.value)

  private val online = Seq(OnlinePlayer("Alpha", 0), OnlinePlayer("Bravo", 2), OnlinePlayer("Charlie", 2))

  private def publicApi(
      players: () => Future[Seq[OnlinePlayer]] = () => Future.successful(online),
      burst: Int = 100,
      proxies: Seq[String] = Nil
  ): PublicApi =
    new PublicApi(
      new CachedValue[Seq[OnlinePlayer]](5.seconds, 60.seconds, players),
      "Test Server",
      "development",
      new RateLimiter(60, burst, 100),
      TrustedProxies.parse(proxies).toOption.get,
      5.seconds
    )

  "the public surface" should {
    "list online players in PSAdmin's shape" in {
      val (response, body) = call(publicApi().route, HttpRequest(uri = "/v1/players"))
      response.status mustEqual StatusCodes.OK
      val json = parse(body)
      (json \ "player_count").extract[Int] mustEqual 3
      (json \ "player_list").extract[List[OnlinePlayer]] mustEqual online.toList
    }

    "report status and population per empire" in {
      val (response, body) = call(publicApi().route, HttpRequest(uri = "/v1/status"))
      response.status mustEqual StatusCodes.OK
      val json = parse(body)
      (json \ "server_name").extract[String] mustEqual "Test Server"
      (json \ "population").extract[Map[String, Int]] mustEqual Map("TR" -> 1, "NC" -> 0, "VS" -> 2)
    }

    "send hardening and cache headers" in {
      val (response, _) = call(publicApi().route, HttpRequest(uri = "/v1/players"))
      header(response, "X-Content-Type-Options") must beSome("nosniff")
      header(response, "Content-Security-Policy") must beSome(contain("default-src 'none'"))
      header(response, "Cache-Control") must beSome(contain("max-age=5"))
    }

    "reuse one world read for many requests" in {
      val reads = new AtomicInteger
      val api   = publicApi(players = () => { reads.incrementAndGet(); Future.successful(online) })
      (1 to 20).foreach(_ => call(api.route, HttpRequest(uri = "/v1/status")))
      reads.get mustEqual 1
    }

    "answer 503 without detail when the world cannot be read" in {
      val api              = publicApi(players = () => Future.failed(new RuntimeException("secret internals")))
      val (response, body) = call(api.route, HttpRequest(uri = "/v1/players"))
      response.status mustEqual StatusCodes.ServiceUnavailable
      body must not(contain("secret internals"))
      header(response, "Cache-Control") must beSome("no-store")
    }

    "rate-limit by client address with Retry-After" in {
      val api = publicApi(burst = 2)
      call(api.route, HttpRequest(uri = "/v1/players"))._1.status mustEqual StatusCodes.OK
      call(api.route, HttpRequest(uri = "/v1/players"))._1.status mustEqual StatusCodes.OK
      val (limited, _) = call(api.route, HttpRequest(uri = "/v1/players"))
      limited.status mustEqual StatusCodes.TooManyRequests
      header(limited, "Retry-After") must beSome
      call(api.route, HttpRequest(uri = "/v1/players"), peer = "203.0.113.2")._1.status mustEqual StatusCodes.OK
    }

    "not let an untrusted caller dodge the limit with X-Forwarded-For" in {
      val api = publicApi(burst = 1)
      def spoofed(n: Int) =
        HttpRequest(uri = "/v1/players", headers = List(RawHeader("X-Forwarded-For", s"198.51.100.$n")))
      call(api.route, spoofed(1))._1.status mustEqual StatusCodes.OK
      call(api.route, spoofed(2))._1.status mustEqual StatusCodes.TooManyRequests
    }

    "rate-limit the forwarded client when the peer is a trusted proxy" in {
      val api = publicApi(burst = 1, proxies = Seq("10.0.0.0/8"))
      def via(client: String) =
        HttpRequest(uri = "/v1/players", headers = List(RawHeader("X-Forwarded-For", client)))
      call(api.route, via("198.51.100.1"), peer = "10.0.0.2")._1.status mustEqual StatusCodes.OK
      call(api.route, via("198.51.100.2"), peer = "10.0.0.2")._1.status mustEqual StatusCodes.OK
      call(api.route, via("198.51.100.1"), peer = "10.0.0.2")._1.status mustEqual StatusCodes.TooManyRequests
    }

    "read every X-Forwarded-For header, so a client cannot get in ahead of the proxy's own" in {
      val api = publicApi(burst = 1, proxies = Seq("10.0.0.0/8"))
      // HAProxy's `forwardfor` adds a header after the one the client sent instead of appending to it.
      def spoofed(n: Int) =
        HttpRequest(
          uri = "/v1/players",
          headers = List(RawHeader("X-Forwarded-For", s"198.51.100.$n"), RawHeader("X-Forwarded-For", "203.0.113.50"))
        )
      call(api.route, spoofed(1), peer = "10.0.0.2")._1.status mustEqual StatusCodes.OK
      call(api.route, spoofed(2), peer = "10.0.0.2")._1.status mustEqual StatusCodes.TooManyRequests
    }

    "refuse writes and unknown paths as JSON" in {
      val (post, postBody) = call(publicApi().route, HttpRequest(HttpMethods.POST, uri = "/v1/players"))
      post.status mustEqual StatusCodes.MethodNotAllowed
      (parse(postBody) \ "error").extract[Boolean] must beTrue
      val (missing, missingBody) = call(publicApi().route, HttpRequest(uri = "/v1/admin/config"))
      missing.status mustEqual StatusCodes.NotFound
      missing.entity.contentType mustEqual ContentTypes.`application/json`
      (parse(missingBody) \ "error").extract[Boolean] must beTrue
    }
  }

  private val token = "0123456789abcdef0123456789abcdef"

  private val runningConfig = ConfigFactory.parseString(
    """database { host = "db", password = "hunter2" }
      |kamon.apm.api-key = "kamon-secret"
      |sentry.dsn = "https://key@sentry.example/1"
      |api.admin.token = "0123456789abcdef0123456789abcdef"
      |world.server-name = "Test Server"
      |""".stripMargin
  )

  private def adminApi(shutdowns: AtomicInteger = new AtomicInteger) =
    new AdminApi(token, () => runningConfig, () => shutdowns.incrementAndGet())

  private def bearer(value: String) = List(Authorization(OAuth2BearerToken(value)))

  "the admin surface" should {
    "require the bearer token" in {
      call(adminApi().route, HttpRequest(uri = "/v1/admin/config"))._1.status mustEqual StatusCodes.Unauthorized
      call(adminApi().route, HttpRequest(uri = "/v1/admin/config", headers = bearer("wrong")))._1.status mustEqual
        StatusCodes.Unauthorized
    }

    "dump the configuration with every credential redacted" in {
      val (response, body) = call(adminApi().route, HttpRequest(uri = "/v1/admin/config", headers = bearer(token)))
      response.status mustEqual StatusCodes.OK
      body must contain("Test Server")
      body must not(contain("hunter2"))
      body must not(contain("kamon-secret"))
      body must not(contain("sentry.example"))
      body must not(contain(token))
    }

    "dump threads" in {
      val (response, body) = call(adminApi().route, HttpRequest(uri = "/v1/admin/threads", headers = bearer(token)))
      response.status mustEqual StatusCodes.OK
      (parse(body) \ "trace").extract[List[String]] must not(beEmpty)
    }

    "shut down only on an authorised POST" in {
      val shutdowns = new AtomicInteger
      val api       = adminApi(shutdowns)
      call(api.route, HttpRequest(HttpMethods.POST, uri = "/v1/admin/shutdown"))._1.status mustEqual
        StatusCodes.Unauthorized
      call(api.route, HttpRequest(uri = "/v1/admin/shutdown", headers = bearer(token)))._1.status mustEqual
        StatusCodes.MethodNotAllowed
      shutdowns.get mustEqual 0
      call(
        api.route,
        HttpRequest(HttpMethods.POST, uri = "/v1/admin/shutdown", headers = bearer(token))
      )._1.status mustEqual
        StatusCodes.Accepted
      shutdowns.get mustEqual 1
    }
  }

  "a real listener with the API's server settings" should {
    "see the TCP peer, answer HEAD and not name its server software" in {
      // Route tests above set the peer address by hand; this checks the listener really records it. The
      // peer is trusted here, so if the address went missing every request would fall into one bucket and
      // the second address would be refused.
      val api = publicApi(burst = 1, proxies = Seq("127.0.0.1"))
      val binding =
        Await.result(Http().newServerAt("127.0.0.1", 0).withSettings(ApiServer.settings).bind(api.route), 10.seconds)
      try {
        val port = binding.localAddress.getPort
        def send(client: String, method: HttpMethod = HttpMethods.GET): HttpResponse = {
          val response = Await.result(
            Http().singleRequest(
              HttpRequest(
                method,
                uri = s"http://127.0.0.1:$port/v1/players",
                headers = List(`X-Forwarded-For`(RemoteAddress(ip(client))))
              )
            ),
            10.seconds
          )
          Await.result(response.entity.toStrict(5.seconds), 5.seconds)
          response
        }
        send("198.51.100.1").status mustEqual StatusCodes.OK
        send("198.51.100.2").status mustEqual StatusCodes.OK
        send("198.51.100.1").status mustEqual StatusCodes.TooManyRequests
        val head = send("198.51.100.3", HttpMethods.HEAD)
        head.status mustEqual StatusCodes.OK
        head.headers.exists(_.is("server")) must beFalse
      } finally Await.ready(binding.unbind(), 10.seconds)
    }
  }
}
