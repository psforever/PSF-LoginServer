package net.psforever.api

import java.net.InetAddress

/**
  * Slows down repeated failed sign-ins without ever locking an account.
  *
  * Failures are counted per client address and per username. The first `freeFailures` cost nothing; after
  * that each failure makes the next attempt from that address, or for that username, wait twice as long
  * as the last, starting at one second and capped. An account is never locked: once the wait is over, one
  * more attempt is allowed. A success clears the username's count only. Clearing the address too would let
  * someone who owns one working account reset their address between guesses at other accounts.
  *
  * A key forgets its failures after [[SignInThrottle.QuietPeriod]] without one. Memory is bounded at
  * `maxTracked` keys per kind. When a table is full, the least recently failed key is forgotten if it is no
  * longer making anyone wait; otherwise the new key is not tracked, so a flood of addresses can cost
  * throttling for newcomers but never locks anyone out.
  *
  * @param nanoTime the clock, replaceable in tests
  */
final class SignInThrottle(
    freeFailures: Int,
    maxDelayPerAddressSeconds: Long,
    maxDelayPerUsernameSeconds: Long,
    maxTracked: Int,
    nanoTime: () => Long = () => System.nanoTime()
) {
  require(freeFailures >= 0, "free-failures must not be negative")
  require(maxDelayPerAddressSeconds > 0, "max-delay-per-address must be positive")
  require(maxDelayPerUsernameSeconds > 0, "max-delay-per-username must be positive")
  require(maxTracked > 0, "max-tracked must be positive")

  private final class Entry(var failures: Int, var lastFailure: Long, var allowedFrom: Long)

  private final class Table(maxDelaySeconds: Long) {
    private val entries = new java.util.LinkedHashMap[String, Entry](16, 0.75f, true)

    private def live(key: String, now: Long): Option[Entry] =
      Option(entries.get(key)).flatMap { entry =>
        if (now - entry.lastFailure > SignInThrottle.QuietPeriodNanos) {
          entries.remove(key)
          None
        } else Some(entry)
      }

    def waitSeconds(key: String, now: Long): Long =
      live(key, now).map(e => SignInThrottle.ceilSeconds(e.allowedFrom - now)).filter(_ > 0).getOrElse(0L)

    def fail(key: String, now: Long): Unit = {
      val entry = live(key, now).getOrElse {
        if (entries.size() >= maxTracked) {
          val eldest = entries.entrySet().iterator().next()
          if (eldest.getValue.allowedFrom <= now) entries.remove(eldest.getKey)
        }
        val created = new Entry(0, now, now)
        if (entries.size() < maxTracked) entries.put(key, created)
        created
      }
      entry.failures += 1
      entry.lastFailure = now
      val over = entry.failures - freeFailures
      if (over > 0) {
        val seconds = math.min(maxDelaySeconds, 1L << math.min(over - 1, 30))
        entry.allowedFrom = now + seconds * 1000000000L
      }
    }

    def clear(key: String): Unit = entries.remove(key)

    def size: Int = entries.size()
  }

  private val addresses = new Table(maxDelayPerAddressSeconds)
  private val usernames = new Table(maxDelayPerUsernameSeconds)

  /** @return whole seconds until an attempt from this address for this username may be made, or None now */
  def check(address: InetAddress, username: String): Option[Long] =
    synchronized {
      val now = nanoTime()
      val wait =
        math.max(addresses.waitSeconds(RateLimiter.key(address), now), usernames.waitSeconds(key(username), now))
      Option.when(wait > 0)(wait)
    }

  /** Record a failed attempt. */
  def failure(address: InetAddress, username: String): Unit =
    synchronized {
      val now = nanoTime()
      addresses.fail(RateLimiter.key(address), now)
      usernames.fail(key(username), now)
    }

  /** Record a successful sign-in: the username's failures are forgotten, the address's are not. */
  def success(username: String): Unit = synchronized(usernames.clear(key(username)))

  /** Keys currently tracked, addresses and usernames. */
  def tracked: (Int, Int) = synchronized((addresses.size, usernames.size))

  private def key(username: String): String = username.trim.toLowerCase
}

object SignInThrottle {

  /** A key with no failure for this long starts again from nothing. */
  val QuietPeriodSeconds: Long = 60L * 60L

  private val QuietPeriodNanos = QuietPeriodSeconds * 1000000000L

  private def ceilSeconds(nanos: Long): Long = (nanos + 999999999L) / 1000000000L
}
