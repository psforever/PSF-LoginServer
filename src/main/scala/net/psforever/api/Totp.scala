package net.psforever.api

import java.nio.ByteBuffer
import java.security.SecureRandom

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
  * Time-based one-time passwords (RFC 6238) as authenticator apps generate them: HMAC-SHA1, a 30-second
  * step and six digits. Built on the JDK's own HMAC so no dependency is needed.
  */
object Totp {

  /** Seconds per code. */
  val StepSeconds = 30L

  /** Digits per code. */
  val Digits = 6

  /** Steps either side of the current one that are still accepted, for clocks that disagree a little. */
  val Window = 1

  /** Secret length in bytes; 20 is the length RFC 4226 recommends for HMAC-SHA1. */
  val SecretBytes = 20

  /** A fresh random secret. */
  def newSecret(random: SecureRandom): Array[Byte] = {
    val secret = new Array[Byte](SecretBytes)
    random.nextBytes(secret)
    secret
  }

  /** The step a moment falls in. */
  def stepAt(epochSeconds: Long): Long = Math.floorDiv(epochSeconds, StepSeconds)

  /** The code for one step, zero-padded to [[Digits]] digits. */
  def code(secret: Array[Byte], step: Long): String = {
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(new SecretKeySpec(secret, "HmacSHA1"))
    val hash   = mac.doFinal(ByteBuffer.allocate(8).putLong(step).array())
    val offset = hash(hash.length - 1) & 0x0f
    val binary =
      ((hash(offset) & 0x7f) << 24) | ((hash(offset + 1) & 0xff) << 16) |
        ((hash(offset + 2) & 0xff) << 8) | (hash(offset + 3) & 0xff)
    val value = binary % math.pow(10, Digits).toInt
    s"%0${Digits}d".format(value)
  }

  /**
    * Check a code typed by a person.
    *
    * @param lastUsedStep the step of the last code accepted for this secret; that step and every earlier
    *                     one are refused, so an observed code cannot be replayed
    * @return the step the code matched, if it is valid and unused
    */
  def verify(secret: Array[Byte], typed: String, epochSeconds: Long, lastUsedStep: Long): Option[Long] = {
    val digits = typed.filterNot(_.isWhitespace)
    if (digits.length != Digits || !digits.forall(_.isDigit)) None
    else {
      val current = stepAt(epochSeconds)
      (current - Window to current + Window)
        .filter(_ > lastUsedStep)
        .find(step => constantTimeEquals(code(secret, step), digits))
    }
  }

  /** The `otpauth://` URI authenticator apps read from a QR code. */
  def provisioningUri(issuer: String, accountName: String, secret: Array[Byte]): String = {
    def encode(text: String): String = java.net.URLEncoder.encode(text, "UTF-8").replace("+", "%20")
    s"otpauth://totp/${encode(issuer)}:${encode(accountName)}" +
      s"?secret=${Base32.encode(secret)}&issuer=${encode(issuer)}&algorithm=SHA1&digits=$Digits&period=$StepSeconds"
  }

  private def constantTimeEquals(a: String, b: String): Boolean =
    java.security.MessageDigest.isEqual(a.getBytes("US-ASCII"), b.getBytes("US-ASCII"))
}

/** RFC 4648 base32 without padding, the form authenticator apps expect secrets in. */
object Base32 {
  private val Alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

  def encode(bytes: Array[Byte]): String = {
    val out    = new StringBuilder
    var buffer = 0
    var bits   = 0
    bytes.foreach { b =>
      buffer = (buffer << 8) | (b & 0xff)
      bits += 8
      while (bits >= 5) {
        out.append(Alphabet((buffer >> (bits - 5)) & 0x1f))
        bits -= 5
      }
    }
    if (bits > 0) out.append(Alphabet((buffer << (5 - bits)) & 0x1f))
    out.toString
  }

  /** Decode, ignoring case, spaces and padding; None if any other character appears. */
  def decode(text: String): Option[Array[Byte]] = {
    val cleaned = text.toUpperCase.filterNot(c => c == '=' || c.isWhitespace)
    if (!cleaned.forall(Alphabet.contains(_))) None
    else {
      val out    = Array.newBuilder[Byte]
      var buffer = 0
      var bits   = 0
      cleaned.foreach { c =>
        buffer = (buffer << 5) | Alphabet.indexOf(c.toInt)
        bits += 5
        if (bits >= 8) {
          out += ((buffer >> (bits - 8)) & 0xff).toByte
          bits -= 8
        }
      }
      Some(out.result())
    }
  }
}
