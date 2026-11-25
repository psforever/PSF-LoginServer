// Copyright (c) 2026 PSForever
package api

import java.net.InetAddress

import net.psforever.api.{Cidr, IpLiteral, TrustedProxies}
import org.specs2.mutable._

class ClientAddressTest extends Specification {
  private def ip(text: String): InetAddress = IpLiteral.parse(text).get

  "IpLiteral" should {
    "parse IPv4 and IPv6 literals" in {
      IpLiteral.parse("203.0.113.7").map(_.getHostAddress) must beSome("203.0.113.7")
      IpLiteral.parse("2001:db8::1").isDefined must beTrue
    }

    "refuse host names rather than resolve them" in {
      IpLiteral.parse("localhost") must beNone
      IpLiteral.parse("example.com") must beNone
      IpLiteral.parse("") must beNone
    }
  }

  "Cidr" should {
    "match addresses inside an IPv4 range only" in {
      val range = Cidr.parse("10.0.0.0/8").toOption.get
      range.contains(ip("10.200.3.4")) must beTrue
      range.contains(ip("11.0.0.1")) must beFalse
    }

    "treat a bare address as a single-address range" in {
      val range = Cidr.parse("192.0.2.10").toOption.get
      range.contains(ip("192.0.2.10")) must beTrue
      range.contains(ip("192.0.2.11")) must beFalse
    }

    "match IPv6 ranges and never mix address families" in {
      val range = Cidr.parse("2001:db8::/32").toOption.get
      range.contains(ip("2001:db8:1::5")) must beTrue
      range.contains(ip("2001:db9::5")) must beFalse
      range.contains(ip("10.0.0.1")) must beFalse
    }

    "reject malformed entries" in {
      Cidr.parse("10.0.0.0/33") must beLeft
      Cidr.parse("10.0.0.0/x") must beLeft
      Cidr.parse("proxy.internal") must beLeft
    }
  }

  "TrustedProxies" should {
    val proxies = TrustedProxies.parse(Seq("10.0.0.0/8")).toOption.get

    "ignore X-Forwarded-For from an untrusted peer" in {
      proxies.clientOf(ip("198.51.100.1"), Some("203.0.113.9")) mustEqual ip("198.51.100.1")
    }

    "take the nearest untrusted hop from a trusted peer" in {
      proxies.clientOf(ip("10.0.0.2"), Some("203.0.113.9")) mustEqual ip("203.0.113.9")
    }

    "ignore whatever the client wrote to the left of the real hop" in {
      proxies.clientOf(ip("10.0.0.2"), Some("1.2.3.4, 203.0.113.9, 10.0.0.5")) mustEqual ip("203.0.113.9")
    }

    "stop at an entry that is not an address" in {
      proxies.clientOf(ip("10.0.0.2"), Some("203.0.113.9, garbage, 10.0.0.5")) mustEqual ip("10.0.0.5")
    }

    "use the peer when a trusted proxy sends no header" in {
      proxies.clientOf(ip("10.0.0.2"), None) mustEqual ip("10.0.0.2")
    }

    "report every invalid entry" in {
      TrustedProxies.parse(Seq("10.0.0.0/8", "nope", "1.2.3.4/99")) must beLeft.like {
        case message => (message must contain("nope")) and (message must contain("1.2.3.4/99"))
      }
    }
  }
}
