package net.psforever.util

import scodec.bits.ByteVector
import scala.collection.mutable.ListBuffer

object Md5Mac {
  val BLOCKSIZE  = 64
  val DIGESTSIZE = 16
  val MACLENGTH  = 16
  val KEYLENGTH  = 16

  /** Checks if two Message Authentication Codes are the same in constant time,
    * preventing a timing attack for MAC forgery
    * @param mac1 A MAC value
    * @param mac2 Another MAC value
    */
  def verifyMac(mac1: ByteVector, mac2: ByteVector): Boolean = {
    var okay = true

    // prevent byte by byte guessing
    if (mac1.length != mac2.length)
      return false

    for (i <- 0 until mac1.length.toInt) {
      okay = okay && mac1 { i } == mac2 { i }
    }

    okay
  }
}

/** MD5-MAC is a ancient MAC algorithm from the 90s that nobody uses anymore. Not to be confused with HMAC-MD5.
  * A description of the algorithm can be found at http://cacr.uwaterloo.ca/hac/about/chap9.pdf, 9.69 Algorithm MD5-MAC
  * There appear to be two implementations: In older versions of CryptoPP (2007) and OpenCL (2001) (nowadays called
  * Botan and not to be confused with the OpenCL standard from Khronos).
  * Both libraries have since removed this code. This file is a Scala port of the OpenCL implementation.
  * Source: https://github.com/sghiassy/Code-Reading-Book/blob/master/OpenCL/src/md5mac.cpp
  */
class Md5Mac(val key: ByteVector) {
  import Md5Mac._

  private val buffer: ListBuffer[Byte] = ListBuffer.fill(BLOCKSIZE)(0)
  private val digest: ListBuffer[Byte] = ListBuffer.fill(DIGESTSIZE)(0)
  private val m: ListBuffer[Byte]      = ListBuffer.fill(32)(0)
  private val k1: ListBuffer[Byte]     = ListBuffer.fill(16)(0)
  private val k2: ListBuffer[Byte]     = ListBuffer.fill(16)(0)
  private val k3: ListBuffer[Byte]     = ListBuffer.fill(BLOCKSIZE)(0)
  private var count: Long              = 0
  private var position: Int            = 0

  private val t: Seq[Seq[Byte]] = Seq(
    Seq(0x97, 0xef, 0x45, 0xac, 0x29, 0x0f, 0x43, 0xcd, 0x45, 0x7e, 0x1b, 0x55, 0x1c, 0x80, 0x11, 0x34),
    Seq(0xb1, 0x77, 0xce, 0x96, 0x2e, 0x72, 0x8e, 0x7c, 0x5f, 0x5a, 0xab, 0x0a, 0x36, 0x43, 0xbe, 0x18),
    Seq(0x9d, 0x21, 0xb4, 0x21, 0xbc, 0x87, 0xb9, 0x4d, 0xa2, 0x9d, 0x27, 0xbd, 0xc7, 0x5b, 0xd7, 0xc3)
  ).map(_.map(_.toByte))

  assert(key.length == KEYLENGTH, s"key length must be ${KEYLENGTH}, not ${key.length}")
  doKey()

  private def doKey() = {
    val ek: ListBuffer[Byte]   = ListBuffer.fill(48)(0)
    val data: ListBuffer[Byte] = ListBuffer.fill(128)(0)
    (0 until 16).foreach(j => {
      data(j) = key(j % key.length)
      data(j + 112) = key(j % key.length)
    })

    (0 until 3).foreach(j => {
      digest.patchInPlace(0, ByteVector.fromInt(0x67452301).toArray, 4)
      digest.patchInPlace(4, ByteVector.fromInt(0xefcdab89).toArray, 4)
      digest.patchInPlace(8, ByteVector.fromInt(0x98badcfe).toArray, 4)
      digest.patchInPlace(12, ByteVector.fromInt(0x10325476).toArray, 4)

      (16 until 112).foreach(k => data(k) = t((j + (k - 16) / 16) % 3)(k % 16))

      hash(data.toSeq)
      hash(data.drop(64).toSeq)

      ek.patchInPlace(4 * 4 * j, digest.slice(0, 4), 4)
      ek.patchInPlace((4 * 4 * j) + 4, digest.slice(4, 8), 4)
      ek.patchInPlace((4 * 4 * j) + 8, digest.slice(8, 12), 4)
      ek.patchInPlace((4 * 4 * j) + 12, digest.slice(12, 16), 4)
    })

    k1.patchInPlace(0, ek.take(16), 16)
    digest.patchInPlace(0, ek.take(16), 16)
    k2.patchInPlace(0, ek.slice(16, 32), 16)

    (0 until 16).foreach(j => k3(j) = ek(((8 + j / 4) * 4) + (3 - j % 4)))
    (16 until 64).foreach(j => k3(j) = (k3(j % 16) ^ t((j - 16) / 16)(j % 16)).toByte)
  }

  private def hash(input: Seq[Byte]) = {
    (0 until 16).foreach(j => {
      m.patchInPlace(j * 4, Array[Byte](input(4 * j + 3), input(4 * j + 2), input(4 * j + 1), input(4 * j + 0)), 4)
    })

    var a = mkInt(digest.drop(0))
    var c = mkInt(digest.drop(2 * 4))
    var b = mkInt(digest.drop(1 * 4))
    var d = mkInt(digest.drop(3 * 4))

    a = ff(a, b, c, d, mkInt(m, 0 * 4), 7, 0xd76aa478)
    d = ff(d, a, b, c, mkInt(m, 1 * 4), 12, 0xe8c7b756)
    c = ff(c, d, a, b, mkInt(m, 2 * 4), 17, 0x242070db)
    b = ff(b, c, d, a, mkInt(m, 3 * 4), 22, 0xc1bdceee)
    a = ff(a, b, c, d, mkInt(m, 4 * 4), 7, 0xf57c0faf)
    d = ff(d, a, b, c, mkInt(m, 5 * 4), 12, 0x4787c62a)
    c = ff(c, d, a, b, mkInt(m, 6 * 4), 17, 0xa8304613)
    b = ff(b, c, d, a, mkInt(m, 7 * 4), 22, 0xfd469501)
    a = ff(a, b, c, d, mkInt(m, 8 * 4), 7, 0x698098d8)
    d = ff(d, a, b, c, mkInt(m, 9 * 4), 12, 0x8b44f7af)
    c = ff(c, d, a, b, mkInt(m, 10 * 4), 17, 0xffff5bb1)
    b = ff(b, c, d, a, mkInt(m, 11 * 4), 22, 0x895cd7be)
    a = ff(a, b, c, d, mkInt(m, 12 * 4), 7, 0x6b901122)
    d = ff(d, a, b, c, mkInt(m, 13 * 4), 12, 0xfd987193)
    c = ff(c, d, a, b, mkInt(m, 14 * 4), 17, 0xa679438e)
    b = ff(b, c, d, a, mkInt(m, 15 * 4), 22, 0x49b40821)

    a = gg(a, b, c, d, mkInt(m, 1 * 4), 5, 0xf61e2562)
    d = gg(d, a, b, c, mkInt(m, 6 * 4), 9, 0xc040b340)
    c = gg(c, d, a, b, mkInt(m, 11 * 4), 14, 0x265e5a51)
    b = gg(b, c, d, a, mkInt(m, 0 * 4), 20, 0xe9b6c7aa)
    a = gg(a, b, c, d, mkInt(m, 5 * 4), 5, 0xd62f105d)
    d = gg(d, a, b, c, mkInt(m, 10 * 4), 9, 0x02441453)
    c = gg(c, d, a, b, mkInt(m, 15 * 4), 14, 0xd8a1e681)
    b = gg(b, c, d, a, mkInt(m, 4 * 4), 20, 0xe7d3fbc8)
    a = gg(a, b, c, d, mkInt(m, 9 * 4), 5, 0x21e1cde6)
    d = gg(d, a, b, c, mkInt(m, 14 * 4), 9, 0xc33707d6)
    c = gg(c, d, a, b, mkInt(m, 3 * 4), 14, 0xf4d50d87)
    b = gg(b, c, d, a, mkInt(m, 8 * 4), 20, 0x455a14ed)
    a = gg(a, b, c, d, mkInt(m, 13 * 4), 5, 0xa9e3e905)
    d = gg(d, a, b, c, mkInt(m, 2 * 4), 9, 0xfcefa3f8)
    c = gg(c, d, a, b, mkInt(m, 7 * 4), 14, 0x676f02d9)
    b = gg(b, c, d, a, mkInt(m, 12 * 4), 20, 0x8d2a4c8a)

    a = hh(a, b, c, d, mkInt(m, 5 * 4), 4, 0xfffa3942)
    d = hh(d, a, b, c, mkInt(m, 8 * 4), 11, 0x8771f681)
    c = hh(c, d, a, b, mkInt(m, 11 * 4), 16, 0x6d9d6122)
    b = hh(b, c, d, a, mkInt(m, 14 * 4), 23, 0xfde5380c)
    a = hh(a, b, c, d, mkInt(m, 1 * 4), 4, 0xa4beea44)
    d = hh(d, a, b, c, mkInt(m, 4 * 4), 11, 0x4bdecfa9)
    c = hh(c, d, a, b, mkInt(m, 7 * 4), 16, 0xf6bb4b60)
    b = hh(b, c, d, a, mkInt(m, 10 * 4), 23, 0xbebfbc70)
    a = hh(a, b, c, d, mkInt(m, 13 * 4), 4, 0x289b7ec6)
    d = hh(d, a, b, c, mkInt(m, 0 * 4), 11, 0xeaa127fa)
    c = hh(c, d, a, b, mkInt(m, 3 * 4), 16, 0xd4ef3085)
    b = hh(b, c, d, a, mkInt(m, 6 * 4), 23, 0x04881d05)
    a = hh(a, b, c, d, mkInt(m, 9 * 4), 4, 0xd9d4d039)
    d = hh(d, a, b, c, mkInt(m, 12 * 4), 11, 0xe6db99e5)
    c = hh(c, d, a, b, mkInt(m, 15 * 4), 16, 0x1fa27cf8)
    b = hh(b, c, d, a, mkInt(m, 2 * 4), 23, 0xc4ac5665)

    a = ii(a, b, c, d, mkInt(m, 0 * 4), 6, 0xf4292244)
    d = ii(d, a, b, c, mkInt(m, 7 * 4), 10, 0x432aff97)
    c = ii(c, d, a, b, mkInt(m, 14 * 4), 15, 0xab9423a7)
    b = ii(b, c, d, a, mkInt(m, 5 * 4), 21, 0xfc93a039)
    a = ii(a, b, c, d, mkInt(m, 12 * 4), 6, 0x655b59c3)
    d = ii(d, a, b, c, mkInt(m, 3 * 4), 10, 0x8f0ccc92)
    c = ii(c, d, a, b, mkInt(m, 10 * 4), 15, 0xffeff47d)
    b = ii(b, c, d, a, mkInt(m, 1 * 4), 21, 0x85845dd1)
    a = ii(a, b, c, d, mkInt(m, 8 * 4), 6, 0x6fa87e4f)
    d = ii(d, a, b, c, mkInt(m, 15 * 4), 10, 0xfe2ce6e0)
    c = ii(c, d, a, b, mkInt(m, 6 * 4), 15, 0xa3014314)
    b = ii(b, c, d, a, mkInt(m, 13 * 4), 21, 0x4e0811a1)
    a = ii(a, b, c, d, mkInt(m, 4 * 4), 6, 0xf7537e82)
    d = ii(d, a, b, c, mkInt(m, 11 * 4), 10, 0xbd3af235)
    c = ii(c, d, a, b, mkInt(m, 2 * 4), 15, 0x2ad7d2bb)
    b = ii(b, c, d, a, mkInt(m, 9 * 4), 21, 0xeb86d391)

    digest.patchInPlace(0, ByteVector.fromInt(mkInt(digest, 0) + a).toArray, 4)
    digest.patchInPlace(4, ByteVector.fromInt(mkInt(digest, 4) + b).toArray, 4)
    digest.patchInPlace(8, ByteVector.fromInt(mkInt(digest, 8) + c).toArray, 4)
    digest.patchInPlace(12, ByteVector.fromInt(mkInt(digest, 12) + d).toArray, 4)
  }

  private def mkInt(lb: Iterable[Byte], pos: Int = 0): Int = {
    ByteVector.view(lb.slice(pos, pos + 4).toArray).toInt()
  }

  private def ff(a: Int, b: Int, c: Int, d: Int, msg: Int, shift: Int, magic: Int): Int = {
    val r = a + ((d ^ (b & (c ^ d))) + msg + magic + mkInt(k2, 0))
    Integer.rotateLeft(r, shift) + b
  }

  private def gg(a: Int, b: Int, c: Int, d: Int, msg: Int, shift: Int, magic: Int): Int = {
    val r = a + ((c ^ ((b ^ c) & d)) + msg + magic + mkInt(k2, 4))
    Integer.rotateLeft(r, shift) + b
  }

  private def hh(a: Int, b: Int, c: Int, d: Int, msg: Int, shift: Int, magic: Int): Int = {
    val r = a + ((b ^ c ^ d) + msg + magic + mkInt(k2, 8))
    Integer.rotateLeft(r, shift) + b
  }

  private def ii(a: Int, b: Int, c: Int, d: Int, msg: Int, shift: Int, magic: Int): Int = {
    val r = a + ((c ^ (b | ~d)) + msg + magic + mkInt(k2, 12))
    Integer.rotateLeft(r, shift) + b
  }

  def update(bytes: ByteVector) = {
    count += bytes.length
    var length = bytes.length
    buffer.patchInPlace(
      position,
      bytes.take(math.min(BLOCKSIZE, bytes.length)).toIterable,
      math.min(BLOCKSIZE, bytes.length).toInt
    )

    if (position + bytes.length >= BLOCKSIZE) {
      hash(buffer.toSeq)
      var input = bytes.drop(BLOCKSIZE - position)
      length = bytes.length - (BLOCKSIZE - position)
      while (length >= BLOCKSIZE) {
        hash(input.toSeq)
        input = input.drop(BLOCKSIZE)
        length -= BLOCKSIZE
      }
      buffer.patchInPlace(0, input.toIterable, input.length.toInt)
      position = 0
    }
    position += length.toInt
  }

  /** Perform final hash calculations and reset the state
    * @return the hash
    */
  def doFinal(length: Int = MACLENGTH): ByteVector = {
    val output: ListBuffer[Byte] = ListBuffer.fill(MACLENGTH)(0)
    buffer(position) = 0x80.toByte
    (position + 1 until BLOCKSIZE).foreach(i => buffer(i) = 0)
    if (position >= BLOCKSIZE - 8) {
      hash(buffer.toSeq)
      buffer.mapInPlace(_ => 0)
    }

    (BLOCKSIZE - 8 until BLOCKSIZE).foreach(i => buffer(i) = ByteVector.fromLong(8 * count)(7 - (i % 8)))

    hash(buffer.toSeq)
    hash(k3.toSeq)

    (0 until MACLENGTH).foreach(i => output(i) = digest((i / 4) * 4 + (3 - (i % 4))))

    count = 0
    position = 0
    digest.patchInPlace(0, k1, digest.length)

    if (length == MACLENGTH) {
      ByteVector.view(output.toArray)
    } else {
      ByteVector.view((0 until length).map(i => output(i % Md5Mac.DIGESTSIZE)).toArray)
    }
  }

  /** Shorthand for `update` and `doFinal` */
  def updateFinal(bytes: ByteVector, length: Int = MACLENGTH): ByteVector = {
    update(bytes)
    doFinal(length)
  }

}
