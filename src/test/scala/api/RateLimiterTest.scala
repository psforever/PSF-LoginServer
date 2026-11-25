// Copyright (c) 2026 PSForever
package api

import java.net.InetAddress

import net.psforever.api.{IpLiteral, RateLimiter}
import org.specs2.mutable._

class RateLimiterTest extends Specification {
  private def ip(text: String): InetAddress = IpLiteral.parse(text).get

  private class Clock { var now = 0L; def apply(): Long = now }

  "RateLimiter" should {
    "allow a burst, then refuse with a retry delay" in {
      val clock   = new Clock
      val limiter = new RateLimiter(60, 3, 100, () => clock())
      val client  = ip("203.0.113.1")
      (1 to 3).map(_ => limiter.acquire(client)) must contain(beNone).forall
      limiter.acquire(client) must beSome(1L)
    }

    "refill at the configured rate" in {
      val clock   = new Clock
      val limiter = new RateLimiter(60, 1, 100, () => clock())
      val client  = ip("203.0.113.1")
      limiter.acquire(client) must beNone
      limiter.acquire(client) must beSome
      clock.now += 1000000000L // one second: 60 a minute is one a second
      limiter.acquire(client) must beNone
    }

    "keep separate buckets per IPv4 address" in {
      val limiter = new RateLimiter(60, 1, 100, () => 0L)
      limiter.acquire(ip("203.0.113.1")) must beNone
      limiter.acquire(ip("203.0.113.2")) must beNone
    }

    "share one bucket across an IPv6 /64" in {
      val limiter = new RateLimiter(60, 1, 100, () => 0L)
      limiter.acquire(ip("2001:db8:0:1::1")) must beNone
      limiter.acquire(ip("2001:db8:0:1::ffff")) must beSome
      limiter.acquire(ip("2001:db8:0:2::1")) must beNone
    }

    "never track more addresses than configured" in {
      val limiter = new RateLimiter(60, 1, 3, () => 0L)
      (1 to 10).foreach(n => limiter.acquire(ip(s"203.0.113.$n")))
      limiter.tracked mustEqual 3
    }

    "make newcomers share one overflow bucket while every tracked bucket is in use" in {
      val limiter = new RateLimiter(60, 1, 3, () => 0L)
      (1 to 3).foreach(n => limiter.acquire(ip(s"203.0.113.$n")) must beNone)
      limiter.acquire(ip("203.0.113.4")) must beNone // the overflow bucket's one token
      limiter.acquire(ip("203.0.113.5")) must beSome // churning addresses gains nothing
      limiter.acquire(ip("2001:db8:0:9::1")) must beSome
    }

    "give a newcomer the place of a bucket that has refilled" in {
      val clock   = new Clock
      val limiter = new RateLimiter(60, 1, 2, () => clock())
      limiter.acquire(ip("203.0.113.1")) must beNone
      limiter.acquire(ip("203.0.113.2")) must beNone
      clock.now += 1000000000L                       // both buckets are full again
      limiter.acquire(ip("203.0.113.2")) must beNone // .2 is busy again; .1 is idle and full
      limiter.acquire(ip("203.0.113.3")) must beNone // takes .1's place
      limiter.tracked mustEqual 2
      // .3 really has a bucket of its own: the overflow bucket's token is still there for the next newcomer.
      limiter.acquire(ip("203.0.113.4")) must beNone
      limiter.acquire(ip("203.0.113.5")) must beSome
    }
  }
}
