// Copyright (c) 2026 PSForever
package api

import java.security.SecureRandom

import net.psforever.api.{Base32, Totp}
import org.specs2.mutable._

class TotpTest extends Specification {
  // RFC 6238 appendix B, SHA-1 rows. The RFC prints eight digits; authenticator apps show the last six.
  private val rfcSecret = "12345678901234567890".getBytes("US-ASCII")
  private val rfcVectors = Seq(
    59L          -> "287082",
    1111111109L  -> "081804",
    1111111111L  -> "050471",
    1234567890L  -> "005924",
    2000000000L  -> "279037",
    20000000000L -> "353130"
  )

  "Totp.code" should {
    "match the RFC 6238 SHA-1 test vectors" in {
      rfcVectors.map { case (time, expected) => Totp.code(rfcSecret, Totp.stepAt(time)) } mustEqual
        rfcVectors.map(_._2)
    }
  }

  "Totp.verify" should {
    val now  = 1111111111L
    val step = Totp.stepAt(now)

    "accept the current code and report its step" in {
      Totp.verify(rfcSecret, Totp.code(rfcSecret, step), now, 0L) must beSome(step)
    }
    "accept a code one step either side, for clocks that disagree a little" in {
      Totp.verify(rfcSecret, Totp.code(rfcSecret, step - 1), now, 0L) must beSome(step - 1)
      Totp.verify(rfcSecret, Totp.code(rfcSecret, step + 1), now, 0L) must beSome(step + 1)
    }
    "refuse a code two steps away" in {
      Totp.verify(rfcSecret, Totp.code(rfcSecret, step - 2), now, 0L) must beNone
      Totp.verify(rfcSecret, Totp.code(rfcSecret, step + 2), now, 0L) must beNone
    }
    "refuse a code from a step already used, so an observed code can't be replayed" in {
      Totp.verify(rfcSecret, Totp.code(rfcSecret, step), now, step) must beNone
      Totp.verify(rfcSecret, Totp.code(rfcSecret, step - 1), now, step - 1) must beNone
    }
    "ignore spaces and refuse anything that isn't six digits" in {
      val typed = Totp.code(rfcSecret, step)
      Totp.verify(rfcSecret, typed.take(3) + " " + typed.drop(3), now, 0L) must beSome(step)
      Totp.verify(rfcSecret, typed.take(5), now, 0L) must beNone
      Totp.verify(rfcSecret, "abcdef", now, 0L) must beNone
    }
  }

  "Totp.provisioningUri" should {
    "carry the issuer, account and base32 secret authenticator apps need" in {
      val uri = Totp.provisioningUri("PS Forever", "some player", rfcSecret)
      uri must startWith("otpauth://totp/PS%20Forever:some%20player?secret=")
      uri must contain(s"secret=${Base32.encode(rfcSecret)}&issuer=PS%20Forever")
      uri must contain("digits=6&period=30")
    }
  }

  "Base32" should {
    "encode the RFC 4648 test vectors without padding" in {
      Seq("", "f", "fo", "foo", "foob", "fooba", "foobar").map(s => Base32.encode(s.getBytes("US-ASCII"))) mustEqual
        Seq("", "MY", "MZXQ", "MZXW6", "MZXW6YQ", "MZXW6YTB", "MZXW6YTBOI")
    }
    "round-trip random secrets, ignoring case, spaces and padding" in {
      val random = new SecureRandom()
      val secret = Totp.newSecret(random)
      val text   = Base32.encode(secret)
      Base32.decode(text).map(_.toSeq) must beSome(secret.toSeq)
      Base32.decode(text.toLowerCase.grouped(4).mkString(" ") + "==").map(_.toSeq) must beSome(secret.toSeq)
    }
    "refuse characters outside the alphabet" in {
      Base32.decode("MZXW1") must beNone
    }
  }
}
