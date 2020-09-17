package net.psforever.util

import java.security.SecureRandom

/** Simple DH implementation
  * We can not use Java's built-in DH because it requires much larger p values than the ones that are used
  * for key exchange by the client (which are 128 bits).
  */
case class DiffieHellman(p: Array[Byte], g: Array[Byte]) {
  import DiffieHellman._

  private val _p                 = BigInt(1, p)
  private val _g                 = BigInt(1, g)
  private val privateKey: BigInt = BigInt(128, random)

  val publicKey: Array[Byte] = bytes(_g.modPow(privateKey, _p))

  /** Agree on shared key */
  def agree(otherKey: Array[Byte]): Array[Byte] = {
    bytes(BigInt(1, otherKey).modPow(privateKey, _p))
  }

  /** Return BigInt as 16 byte array (same size as private key) */
  private def bytes(b: BigInt): Array[Byte] = {
    b.toByteArray.takeRight(16).reverse.padTo(16, 0x0.toByte).reverse
  }
}

object DiffieHellman {
  private val random = new SecureRandom()
}
