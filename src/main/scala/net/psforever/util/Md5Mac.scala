package net.psforever.util

import scodec.bits.ByteVector

object Md5Mac {
  val BLOCKSIZE  = 64
  val DIGESTSIZE = 16
  val MACLENGTH  = 16
  val KEYLENGTH  = 16

  private val t: Array[Array[Byte]] = Array(
    Array(0x97, 0xef, 0x45, 0xac, 0x29, 0x0f, 0x43, 0xcd, 0x45, 0x7e, 0x1b, 0x55, 0x1c, 0x80, 0x11, 0x34),
    Array(0xb1, 0x77, 0xce, 0x96, 0x2e, 0x72, 0x8e, 0x7c, 0x5f, 0x5a, 0xab, 0x0a, 0x36, 0x43, 0xbe, 0x18),
    Array(0x9d, 0x21, 0xb4, 0x21, 0xbc, 0x87, 0xb9, 0x4d, 0xa2, 0x9d, 0x27, 0xbd, 0xc7, 0x5b, 0xd7, 0xc3)
  ).map(_.map(_.toByte))

  /** Read four bytes from `a` starting at `pos` as a big-endian, 32-bit integer. */
  private def mkInt(a: Array[Byte], pos: Int): Int = {
    ((a(pos) & 0xff) << 24) |
      ((a(pos + 1) & 0xff) << 16) |
      ((a(pos + 2) & 0xff) << 8) |
      (a(pos + 3) & 0xff)
  }

  /** Write `v` into `a` at `pos` as a big-endian, 32-bit integer. */
  private def putInt(a: Array[Byte], pos: Int, v: Int): Unit = {
    a(pos) = (v >>> 24).toByte
    a(pos + 1) = (v >>> 16).toByte
    a(pos + 2) = (v >>> 8).toByte
    a(pos + 3) = v.toByte
  }

  /** Checks if two Message Authentication Codes are the same in constant time,
    * preventing a timing attack for MAC forgery
    * @param mac1 A MAC value
    * @param mac2 Another MAC value
    */
  def verifyMac(mac1: ByteVector, mac2: ByteVector): Boolean = {
    // prevent byte by byte guessing
    if (mac1.length != mac2.length) {
      false
    } else {
      var okay = true
      for (i <- 0 until mac1.length.toInt) {
        okay = okay && mac1 { i } == mac2 { i }
      }
      okay
    }
  }
}

/**
 * MD5-MAC is a ancient MAC algorithm from the 90s that nobody uses anymore.
 * Not to be confused with HMAC-MD5.
 * A description of the algorithm can be found at http://cacr.uwaterloo.ca/hac/about/chap9.pdf, 9.69 Algorithm MD5-MAC.
 * There are two implementations:
 * one from older versions of CryptoPP (2007),
 * and one from OpenCL (2001) (nowadays called Botan and not to be confused with the OpenCL standard from Khronos).
 * Both libraries have since removed this code.
 * This file is a Scala port of the OpenCL implementation.
 * Source: https://github.com/sghiassy/Code-Reading-Book/blob/master/OpenCL/src/md5mac.cpp
 *
 * This implementation is deliberately allocation-free on the hot path:
 * every piece of working state is a primitive array owned by the instance,
 * `reset` restores that state with `System.arraycopy` from a stored template,
 * and the only object allocated per `doFinal` is the returned MAC itself.
 */
class Md5Mac(val key: ByteVector) {
  import Md5Mac._
  assert(key.length == KEYLENGTH, s"key length must be $KEYLENGTH, not ${key.length}")

  private var count: Long   = 0
  private var position: Int = 0

  /** the 64-byte accumulation block */
  private val buffer: Array[Byte] = new Array[Byte](BLOCKSIZE)
  /** the running 16-byte digest, held as four big-endian words */
  private val digest: Array[Byte] = new Array[Byte](DIGESTSIZE)
  /** the message schedule of the current block, already converted to little-endian words */
  private val m: Array[Int] = new Array[Int](16)
  /** key-derived constants; `k2` is pre-split into the four words the round functions need */
  private val k1: Array[Byte] = new Array[Byte](16)
  private val k3: Array[Byte] = new Array[Byte](BLOCKSIZE)
  private var k2a: Int        = 0
  private var k2b: Int        = 0
  private var k2c: Int        = 0
  private var k2d: Int        = 0
  /** the value `digest` is restored to by `reset` */
  private val digestTemplate: Array[Byte] = new Array[Byte](DIGESTSIZE)
  /** scratch space so whole blocks can be hashed straight out of a `ByteVector` without allocating */
  private val block: Array[Byte] = new Array[Byte](BLOCKSIZE)
  /** scratch space for the byte-swapped digest produced by `doFinal` */
  private val macScratch: Array[Byte] = new Array[Byte](MACLENGTH)

  doKey()

  private def doKey(): Unit = {
    val ek: Array[Byte]   = new Array[Byte](48)
    val data: Array[Byte] = new Array[Byte](128)

    var j = 0
    while (j < 16) {
      data(j) = key(j.toLong % key.length)
      data(j + 112) = key(j.toLong % key.length)
      j += 1
    }

    j = 0
    while (j < 3) {
      putInt(digest, 0, 0x67452301)
      putInt(digest, 4, 0xefcdab89)
      putInt(digest, 8, 0x98badcfe)
      putInt(digest, 12, 0x10325476)

      var k = 16
      while (k < 112) {
        data(k) = t((j + (k - 16) / 16) % 3)(k % 16)
        k += 1
      }

      hash(data, 0)
      hash(data, 64)

      System.arraycopy(digest, 0, ek, 4 * 4 * j, 16)
      j += 1
    }

    System.arraycopy(ek, 0, k1, 0, 16)
    System.arraycopy(ek, 0, digest, 0, 16)
    // k2, split into the four big-endian words consumed by the round functions
    k2a = mkInt(ek, 16)
    k2b = mkInt(ek, 20)
    k2c = mkInt(ek, 24)
    k2d = mkInt(ek, 28)

    j = 0
    while (j < 16) {
      k3(j) = ek(((8 + j / 4) * 4) + (3 - j % 4))
      j += 1
    }
    j = 16
    while (j < 64) {
      k3(j) = (k3(j % 16) ^ t((j - 16) / 16)(j % 16)).toByte
      j += 1
    }
    // `buffer` is still all zeroes here, which is exactly what `reset` needs to restore
    System.arraycopy(digest, 0, digestTemplate, 0, DIGESTSIZE)
  }

  private def hash(input: Array[Byte], offset: Int): Unit = {
    val mm     = m
    val digest = this.digest
    var j      = 0
    while (j < 16) {
      val p = offset + j * 4
      // the original stored the bytes reversed and then read them back big-endian: a little-endian read
      mm(j) = ((input(p + 3) & 0xff) << 24) |
        ((input(p + 2) & 0xff) << 16) |
        ((input(p + 1) & 0xff) << 8) |
        (input(p) & 0xff)
      j += 1
    }

    val a0 = mkInt(digest, 0)
    val b0 = mkInt(digest, 4)
    val c0 = mkInt(digest, 8)
    val d0 = mkInt(digest, 12)

    var a = a0
    var b = b0
    var c = c0
    var d = d0

    a = ff(a, b, c, d, mm(0), 7, 0xd76aa478)
    d = ff(d, a, b, c, mm(1), 12, 0xe8c7b756)
    c = ff(c, d, a, b, mm(2), 17, 0x242070db)
    b = ff(b, c, d, a, mm(3), 22, 0xc1bdceee)
    a = ff(a, b, c, d, mm(4), 7, 0xf57c0faf)
    d = ff(d, a, b, c, mm(5), 12, 0x4787c62a)
    c = ff(c, d, a, b, mm(6), 17, 0xa8304613)
    b = ff(b, c, d, a, mm(7), 22, 0xfd469501)
    a = ff(a, b, c, d, mm(8), 7, 0x698098d8)
    d = ff(d, a, b, c, mm(9), 12, 0x8b44f7af)
    c = ff(c, d, a, b, mm(10), 17, 0xffff5bb1)
    b = ff(b, c, d, a, mm(11), 22, 0x895cd7be)
    a = ff(a, b, c, d, mm(12), 7, 0x6b901122)
    d = ff(d, a, b, c, mm(13), 12, 0xfd987193)
    c = ff(c, d, a, b, mm(14), 17, 0xa679438e)
    b = ff(b, c, d, a, mm(15), 22, 0x49b40821)

    a = gg(a, b, c, d, mm(1), 5, 0xf61e2562)
    d = gg(d, a, b, c, mm(6), 9, 0xc040b340)
    c = gg(c, d, a, b, mm(11), 14, 0x265e5a51)
    b = gg(b, c, d, a, mm(0), 20, 0xe9b6c7aa)
    a = gg(a, b, c, d, mm(5), 5, 0xd62f105d)
    d = gg(d, a, b, c, mm(10), 9, 0x02441453)
    c = gg(c, d, a, b, mm(15), 14, 0xd8a1e681)
    b = gg(b, c, d, a, mm(4), 20, 0xe7d3fbc8)
    a = gg(a, b, c, d, mm(9), 5, 0x21e1cde6)
    d = gg(d, a, b, c, mm(14), 9, 0xc33707d6)
    c = gg(c, d, a, b, mm(3), 14, 0xf4d50d87)
    b = gg(b, c, d, a, mm(8), 20, 0x455a14ed)
    a = gg(a, b, c, d, mm(13), 5, 0xa9e3e905)
    d = gg(d, a, b, c, mm(2), 9, 0xfcefa3f8)
    c = gg(c, d, a, b, mm(7), 14, 0x676f02d9)
    b = gg(b, c, d, a, mm(12), 20, 0x8d2a4c8a)

    a = hh(a, b, c, d, mm(5), 4, 0xfffa3942)
    d = hh(d, a, b, c, mm(8), 11, 0x8771f681)
    c = hh(c, d, a, b, mm(11), 16, 0x6d9d6122)
    b = hh(b, c, d, a, mm(14), 23, 0xfde5380c)
    a = hh(a, b, c, d, mm(1), 4, 0xa4beea44)
    d = hh(d, a, b, c, mm(4), 11, 0x4bdecfa9)
    c = hh(c, d, a, b, mm(7), 16, 0xf6bb4b60)
    b = hh(b, c, d, a, mm(10), 23, 0xbebfbc70)
    a = hh(a, b, c, d, mm(13), 4, 0x289b7ec6)
    d = hh(d, a, b, c, mm(0), 11, 0xeaa127fa)
    c = hh(c, d, a, b, mm(3), 16, 0xd4ef3085)
    b = hh(b, c, d, a, mm(6), 23, 0x04881d05)
    a = hh(a, b, c, d, mm(9), 4, 0xd9d4d039)
    d = hh(d, a, b, c, mm(12), 11, 0xe6db99e5)
    c = hh(c, d, a, b, mm(15), 16, 0x1fa27cf8)
    b = hh(b, c, d, a, mm(2), 23, 0xc4ac5665)

    a = ii(a, b, c, d, mm(0), 6, 0xf4292244)
    d = ii(d, a, b, c, mm(7), 10, 0x432aff97)
    c = ii(c, d, a, b, mm(14), 15, 0xab9423a7)
    b = ii(b, c, d, a, mm(5), 21, 0xfc93a039)
    a = ii(a, b, c, d, mm(12), 6, 0x655b59c3)
    d = ii(d, a, b, c, mm(3), 10, 0x8f0ccc92)
    c = ii(c, d, a, b, mm(10), 15, 0xffeff47d)
    b = ii(b, c, d, a, mm(1), 21, 0x85845dd1)
    a = ii(a, b, c, d, mm(8), 6, 0x6fa87e4f)
    d = ii(d, a, b, c, mm(15), 10, 0xfe2ce6e0)
    c = ii(c, d, a, b, mm(6), 15, 0xa3014314)
    b = ii(b, c, d, a, mm(13), 21, 0x4e0811a1)
    a = ii(a, b, c, d, mm(4), 6, 0xf7537e82)
    d = ii(d, a, b, c, mm(11), 10, 0xbd3af235)
    c = ii(c, d, a, b, mm(2), 15, 0x2ad7d2bb)
    b = ii(b, c, d, a, mm(9), 21, 0xeb86d391)

    putInt(digest, 0, a0 + a)
    putInt(digest, 4, b0 + b)
    putInt(digest, 8, c0 + c)
    putInt(digest, 12, d0 + d)
  }

  @inline private def ff(a: Int, b: Int, c: Int, d: Int, msg: Int, shift: Int, magic: Int): Int = {
    val r = a + ((d ^ (b & (c ^ d))) + msg + magic + k2a)
    Integer.rotateLeft(r, shift) + b
  }

  @inline private def gg(a: Int, b: Int, c: Int, d: Int, msg: Int, shift: Int, magic: Int): Int = {
    val r = a + ((c ^ ((b ^ c) & d)) + msg + magic + k2b)
    Integer.rotateLeft(r, shift) + b
  }

  @inline private def hh(a: Int, b: Int, c: Int, d: Int, msg: Int, shift: Int, magic: Int): Int = {
    val r = a + ((b ^ c ^ d) + msg + magic + k2c)
    Integer.rotateLeft(r, shift) + b
  }

  @inline private def ii(a: Int, b: Int, c: Int, d: Int, msg: Int, shift: Int, magic: Int): Int = {
    val r = a + ((c ^ (b | ~d)) + msg + magic + k2d)
    Integer.rotateLeft(r, shift) + b
  }

  /** Copy `size` bytes out of `src` (starting at `srcOffset`) into `dest` at `destOffset`. */
  private def copyOut(src: ByteVector, srcOffset: Long, dest: Array[Byte], destOffset: Int, size: Int): Unit = {
    var i = 0
    while (i < size) {
      dest(destOffset + i) = src(srcOffset + i)
      i += 1
    }
  }

  def update(bytes: ByteVector): Unit = {
    val total = bytes.length
    count += total
    var length = total
    // fill the accumulation block from `position` onwards, never past the end of the block
    val head = math.min(math.min(BLOCKSIZE.toLong, total), (BLOCKSIZE - position).toLong).toInt
    copyOut(bytes, 0L, buffer, position, head)

    if (position + total >= BLOCKSIZE) {
      hash(buffer, 0)
      var offset = (BLOCKSIZE - position).toLong
      length = total - offset
      while (length >= BLOCKSIZE) {
        copyOut(bytes, offset, block, 0, BLOCKSIZE)
        hash(block, 0)
        offset += BLOCKSIZE
        length -= BLOCKSIZE
      }
      copyOut(bytes, offset, buffer, 0, length.toInt)
      position = 0
    }
    position += length.toInt
  }

  /** Perform final hash calculations and reset the state
    * @return the hash
    */
  def doFinal(length: Int = MACLENGTH): ByteVector = {
    buffer(position) = 0x80.toByte
    var i = position + 1
    while (i < BLOCKSIZE) {
      buffer(i) = 0
      i += 1
    }
    if (position >= BLOCKSIZE - 8) {
      hash(buffer, 0)
      java.util.Arrays.fill(buffer, 0.toByte)
    }

    val bitCount = 8 * count
    i = 0
    while (i < 8) {
      // little-endian bit count in the last eight bytes of the block
      buffer(BLOCKSIZE - 8 + i) = (bitCount >>> (8 * i)).toByte
      i += 1
    }

    hash(buffer, 0)
    hash(k3, 0)

    val out = macScratch
    i = 0
    while (i < MACLENGTH) {
      out(i) = digest((i / 4) * 4 + (3 - (i % 4)))
      i += 1
    }

    count = 0
    position = 0
    System.arraycopy(k1, 0, digest, 0, DIGESTSIZE)

    val result = new Array[Byte](length)
    if (length == MACLENGTH) {
      System.arraycopy(out, 0, result, 0, MACLENGTH)
    } else {
      i = 0
      while (i < length) {
        result(i) = out(i % Md5Mac.DIGESTSIZE)
        i += 1
      }
    }
    ByteVector.view(result)
  }

  /** Shorthand for `update` and `doFinal` */
  def updateFinal(bytes: ByteVector, length: Int = MACLENGTH): ByteVector = {
    update(bytes)
    doFinal(length)
  }

  /**
   * Restore the original cryptographic information (state) for this MAC algorithm.
   * The primary key is being reused and,
   * without random elements in the calculation,
   * the original cryptographic information only needs to be reloaded.
   * @return this MAC algorithm container
   */
  def reset(): Md5Mac = {
    count = 0
    position = 0
    java.util.Arrays.fill(buffer, 0.toByte)
    System.arraycopy(digestTemplate, 0, digest, 0, DIGESTSIZE)
    this
  }
}
