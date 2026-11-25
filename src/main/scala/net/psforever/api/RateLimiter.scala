package net.psforever.api

import java.net.{Inet6Address, InetAddress}

/**
  * A token-bucket rate limit per client address.
  *
  * Each address may make `burst` requests at once and then `requestsPerMinute` a minute after that.
  * IPv6 clients are grouped by their /64, because a single host is routinely handed a whole /64 and
  * could otherwise take a fresh bucket for every request.
  *
  * Memory is bounded: at most `maxTrackedAddresses` buckets are held. When the table is full, a new address
  * may take the place of the least recently used one only if that bucket has refilled completely, so
  * forgetting it gives nothing away. Otherwise the newcomer shares a single overflow bucket. Evicting busy
  * buckets instead would let a client that controls many addresses (an IPv6 /48 is 65,536 /64s) cycle
  * through them faster than they are remembered and never be limited at all.
  *
  * @param nanoTime the clock, replaceable in tests
  */
final class RateLimiter(
    requestsPerMinute: Int,
    burst: Int,
    maxTrackedAddresses: Int,
    nanoTime: () => Long = () => System.nanoTime()
) {
  require(requestsPerMinute > 0, "requests-per-minute must be positive")
  require(burst > 0, "burst must be positive")
  require(maxTrackedAddresses > 0, "max-tracked-addresses must be positive")

  private val refillPerNano: Double = requestsPerMinute.toDouble / 60e9

  private final class Bucket(var tokens: Double, var updatedAt: Long)

  /** Access-ordered, so the first entry is always the least recently used. */
  private val buckets = new java.util.LinkedHashMap[String, Bucket](16, 0.75f, true)

  /** Shared by every address that arrives while the table is full of buckets still in use. */
  private val overflow = new Bucket(burst.toDouble, nanoTime())

  private def refill(bucket: Bucket, now: Long): Unit = {
    bucket.tokens = math.min(burst.toDouble, bucket.tokens + (now - bucket.updatedAt) * refillPerNano)
    bucket.updatedAt = now
  }

  /** The bucket for this key, making room for it if that costs nothing. */
  private def bucketFor(key: String, now: Long): Bucket =
    Option(buckets.get(key)).getOrElse {
      if (buckets.size() >= maxTrackedAddresses) {
        val eldest = buckets.entrySet().iterator().next()
        refill(eldest.getValue, now)
        if (eldest.getValue.tokens >= burst - RateLimiter.Tolerance) buckets.remove(eldest.getKey)
      }
      if (buckets.size() < maxTrackedAddresses) {
        val bucket = new Bucket(burst.toDouble, now)
        buckets.put(key, bucket)
        bucket
      } else overflow
    }

  /**
    * Take one request's worth from this address's bucket.
    *
    * @return None if the request may proceed, or how many whole seconds until it would be allowed
    */
  def acquire(address: InetAddress): Option[Long] =
    buckets.synchronized {
      val now    = nanoTime()
      val bucket = bucketFor(RateLimiter.key(address), now)
      refill(bucket, now)
      // The tolerance absorbs floating-point drift, so a bucket refilled for exactly one request's worth
      // of time admits that request.
      if (bucket.tokens >= 1.0 - RateLimiter.Tolerance) {
        bucket.tokens = math.max(0.0, bucket.tokens - 1.0)
        None
      } else {
        Some(math.max(1L, math.ceil((1.0 - bucket.tokens) / refillPerNano / 1e9).toLong))
      }
    }

  /** The number of addresses currently tracked, not counting the overflow bucket. */
  def tracked: Int = buckets.synchronized(buckets.size())
}

object RateLimiter {
  private val Tolerance = 1e-9

  /** The bucket an address shares: the address itself for IPv4, its /64 for IPv6. */
  def key(address: InetAddress): String =
    address match {
      case v6: Inet6Address => v6.getAddress.take(8).map(b => f"${b & 0xff}%02x").mkString + "::/64"
      case v4               => v4.getHostAddress
    }
}
