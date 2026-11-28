// Copyright (c) 2026 PSForever
package api

import net.psforever.api.ApiServer
import net.psforever.util.{
  AdminApiConfig,
  ApiConfig,
  ApiDatabaseConfig,
  ApiListenerConfig,
  ApiRateLimitConfig,
  ApiSessionsConfig,
  ApiSignInConfig
}
import org.specs2.mutable._

import scala.concurrent.duration._

class ApiServerPlanTest extends Specification {
  private val loopbackOnly: String => Boolean = _ == "127.0.0.1"

  private def config(
      publicBind: String = "127.0.0.1",
      adminBind: String = "127.0.0.1",
      adminPort: Int = 51003,
      token: String = "",
      proxies: Seq[String] = Nil,
      databaseConnections: Int = 4,
      sessions: ApiSessionsConfig = ApiSessionsConfig(1.day, 7.days, gmTotpRequired = false, "PSForever"),
      signIn: ApiSignInConfig = ApiSignInConfig(5, 15.minutes, 1.minute, 2, 8)
  ): ApiConfig =
    ApiConfig(
      ApiListenerConfig(publicBind, 51002),
      AdminApiConfig(adminBind, adminPort, token),
      proxies,
      ApiRateLimitConfig(60, 20, 10000),
      5.seconds,
      ApiDatabaseConfig(databaseConnections),
      sessions,
      signIn
    )

  private def plan(c: ApiConfig, legacy: Boolean = false) = ApiServer.plan(c, legacy, loopbackOnly)

  private val goodToken = "0123456789abcdef" * 2

  "ApiServer.plan" should {
    "accept the shipped defaults, with the admin surface off" in {
      plan(config()) must beRight.like { case p => p.adminToken must beNone }
    }

    "turn the admin surface on with a token" in {
      plan(config(token = goodToken)) must beRight.like { case p => p.adminToken must beSome(goodToken) }
    }

    "refuse a short token" in {
      plan(config(token = "short")) must beLeft
    }

    "refuse a token a bearer header cannot carry" in {
      plan(config(token = goodToken + " ; extra")) must beLeft.like {
        case errors => errors.mkString must contain("letters, digits")
      }
    }

    "refuse both surfaces on one port" in {
      plan(config(adminPort = 51002, token = goodToken)) must beLeft
      plan(config(adminPort = 51002)) must beRight // the admin surface is off, so nothing collides
    }

    "warn when the admin surface leaves loopback" in {
      plan(config(adminBind = "0.0.0.0", token = goodToken)) must beRight.like {
        case p => p.warnings.mkString must contain("private network")
      }
    }

    "refuse invalid trusted proxies" in {
      plan(config(proxies = Seq("not-an-address"))) must beLeft
    }

    "refuse an API database pool with no connections" in {
      plan(config(databaseConnections = 0)) must beLeft
    }

    "refuse session lifetimes that contradict each other" in {
      plan(config(sessions = ApiSessionsConfig(2.days, 1.day, gmTotpRequired = false, "PSForever"))) must beLeft
      plan(config(sessions = ApiSessionsConfig(10.seconds, 1.day, gmTotpRequired = false, "PSForever"))) must beLeft
    }

    "refuse a TOTP issuer authenticator apps could not show" in {
      plan(config(sessions = ApiSessionsConfig(1.day, 7.days, gmTotpRequired = true, "PS:Forever"))) must beLeft
      plan(config(sessions = ApiSessionsConfig(1.day, 7.days, gmTotpRequired = true, " "))) must beLeft
    }

    "refuse sign-in limits that would stop every sign-in or never slow one down" in {
      plan(config(signIn = ApiSignInConfig(5, 15.minutes, 1.minute, 0, 8))) must beLeft
      plan(config(signIn = ApiSignInConfig(-1, 15.minutes, 1.minute, 2, 8))) must beLeft
      plan(config(signIn = ApiSignInConfig(5, 0.seconds, 1.minute, 2, 8))) must beLeft
    }

    "warn when the public surface is reachable with no proxy in front" in {
      plan(config(publicBind = "0.0.0.0")) must beRight.like {
        case p => p.warnings must not(beEmpty)
      }
      plan(config(publicBind = "0.0.0.0", proxies = Seq("10.0.0.1"))) must beRight.like {
        case p => p.warnings must beEmpty
      }
    }

    "warn that PSAdmin's old admin settings are ignored" in {
      plan(config(), legacy = true) must beRight.like {
        case p => p.warnings.mkString must contain("PSAdmin")
      }
    }
  }
}
