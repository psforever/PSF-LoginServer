// Copyright (c) 2017 PSForever
package net.psforever.newcodecs

import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}
import scodec.bits.BitVector

final class QuantizedDoubleCodec(min: Double, max: Double, bits: Int) extends Codec[Double] {

  require(bits > 0 && bits <= 32, "bits must be in range [1, 32]")

  private val bitsL = bits.toLong

  private def description = s"$bits-bit q_double [$min, $max]"

  override def sizeBound = SizeBound.exact(bitsL)

  def QuantizeDouble(value: Double): Int = {
    val range: Double = max - min;

    if (range == 0.0)
      return 0

    val bit_max: Int           = 1 << bits;
    val rounded_quantized: Int = math.floor((value - min) * bit_max.toDouble / range + 0.5).toInt

    if (rounded_quantized < 0)
      return 0

    if (rounded_quantized > bit_max - 1)
      return (bit_max - 1)

    return rounded_quantized
  }

  def UnquantizeDouble(value: Int): Double = {
    return ((max - min) * value.toDouble / (1 << bitsL.toInt).toDouble + min)
  }

  /*
   * scodec's little-endian layout, for widths that are not whole bytes too: the whole bytes,
   * least significant first, then the bits % 8 most significant bits. scodec reorders through
   * intermediate vectors; these do it with arithmetic.
   */

  /** `BitVector.fromInt(value, bits, ByteOrdering.LittleEndian)` */
  private def toLittleEndian(value: Int): BitVector = {
    val whole   = bits / 8
    val tail    = bits % 8
    var layout  = 0L // the bits in order, as a bits-wide number
    var b       = 0
    while (b < whole) {
      layout = (layout << 8) | ((value >>> (8 * b)) & 0xff)
      b += 1
    }
    layout = (layout << tail) | ((value >>> (8 * whole)) & ((1 << tail) - 1))
    val aligned = layout << (64 - bits) // to the top of the long, then out as bytes
    val out     = new Array[Byte]((bits + 7) / 8)
    b = 0
    while (b < out.length) {
      out(b) = (aligned >>> (56 - 8 * b)).toByte
      b += 1
    }
    BitVector.view(out, bitsL)
  }

  /** `buffer.take(bits).toInt(signed = false, ByteOrdering.LittleEndian)` */
  private def fromLittleEndian(buffer: BitVector): Int = {
    val whole = bits / 8
    var value = 0
    var i     = 0
    var b     = 0
    while (b < whole) {
      var byte = 0
      val end  = i + 8
      while (i < end) {
        byte = (byte << 1) | (if (buffer.get(i.toLong)) 1 else 0)
        i += 1
      }
      value |= byte << (8 * b)
      b += 1
    }
    var top = 0
    while (i < bits) {
      top = (top << 1) | (if (buffer.get(i.toLong)) 1 else 0)
      i += 1
    }
    value | (top << (8 * whole))
  }

  override def encode(value: Double) = {
    Attempt.successful(toLittleEndian(QuantizeDouble(value)))
  }

  override def decode(buffer: BitVector) = {
    if (buffer.sizeGreaterThanOrEqual(bitsL))
      Attempt.successful(
        DecodeResult(UnquantizeDouble(fromLittleEndian(buffer)), buffer.drop(bitsL))
      )
    else
      Attempt.failure(Err.insufficientBits(bitsL, buffer.size))
  }

  override def toString = description
}
