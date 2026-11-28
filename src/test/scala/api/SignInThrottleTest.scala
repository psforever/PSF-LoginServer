// Copyright (c) 2026 PSForever
package api

import java.net.InetAddress

import net.psforever.api.{IpLiteral, SignInThrottle}
import org.specs2.mutable._

class SignInThrottleTest extends Specification {
  private def ip(text: String): InetAddress = IpLiteral.parse(text).get

  private final class Clock(var nanos: Long = 0L) {
    def advance(seconds: Long): Unit = nanos += seconds * 1000000000L
  }

  private def throttle(clock: Clock, free: Int = 3, perAddress: Long = 900L, perUsername: Long = 60L, max: Int = 100) =
    new SignInThrottle(free, perAddress, perUsername, max, () => clock.nanos)

  private val alice = ip("203.0.113.1")
  private val bob   = ip("203.0.113.2")

  "the sign-in throttle" should {
    "let the free failures through without any wait" in {
      val clock = new Clock()
      val t     = throttle(clock)
      (1 to 3).foreach(_ => t.failure(alice, "victim"))
      t.check(alice, "victim") must beNone
    }

    "double the wait for each failure past the free ones" in {
      val clock = new Clock()
      val t     = throttle(clock)
      (1 to 3).foreach(_ => t.failure(alice, "victim"))
      val waits = (1 to 4).map { _ =>
        t.failure(alice, "victim")
        val wait = t.check(alice, "victim").getOrElse(0L)
        clock.advance(wait)
        wait
      }
      waits mustEqual Seq(1L, 2L, 4L, 8L)
    }

    "never lock: once the wait is over, the next attempt is allowed" in {
      val clock = new Clock()
      val t     = throttle(clock, free = 0)
      (1 to 20).foreach { _ =>
        t.failure(alice, "victim")
        clock.advance(t.check(alice, "victim").getOrElse(0L))
      }
      t.check(alice, "victim") must beNone
    }

    "cap the wait per username lower than per address" in {
      val clock = new Clock()
      val t     = throttle(clock, free = 0, perAddress = 900L, perUsername = 60L)
      (1 to 20).foreach(_ => t.failure(alice, "victim"))
      t.check(alice, "victim") must beSome(900L)
      // From a fresh address only the username's wait applies.
      t.check(bob, "victim") must beSome(60L)
    }

    "throttle an address guessing at many usernames" in {
      val clock = new Clock()
      val t     = throttle(clock, free = 2)
      Seq("a", "b", "c", "d").foreach(u => t.failure(alice, u))
      t.check(alice, "someone-new") must beSome
      t.check(bob, "someone-new") must beNone
    }

    "match usernames regardless of case and surrounding spaces" in {
      val clock = new Clock()
      val t     = throttle(clock, free = 0)
      t.failure(alice, "Victim")
      t.check(bob, " victim ") must beSome
    }

    "clear only the username on success, not the address" in {
      val clock = new Clock()
      val t     = throttle(clock, free = 0)
      t.failure(alice, "victim")
      t.success("victim")
      t.check(bob, "victim") must beNone
      t.check(alice, "other") must beSome
    }

    "forget a key after an hour without failures" in {
      val clock = new Clock()
      val t     = throttle(clock, free = 0, perAddress = 7200L)
      (1 to 15).foreach(_ => t.failure(alice, "victim"))
      clock.advance(SignInThrottle.QuietPeriodSeconds + 1)
      t.failure(alice, "victim")
      t.check(alice, "victim") must beSome(1L)
    }

    "stay within its memory bound, keeping keys that still make someone wait" in {
      val clock = new Clock()
      val t     = throttle(clock, free = 0, max = 3)
      Seq("u1", "u2", "u3").foreach(u => t.failure(ip(s"198.51.100.${u.last}"), u))
      t.failure(ip("198.51.100.9"), "u9")
      t.tracked mustEqual ((3, 3))
      t.check(ip("198.51.100.1"), "u1") must beSome
      // Once the eldest has stopped making anyone wait, it gives up its place.
      clock.advance(2)
      t.failure(ip("198.51.100.9"), "u9")
      t.check(ip("198.51.100.9"), "u9") must beSome
      t.tracked mustEqual ((3, 3))
    }
  }
}
