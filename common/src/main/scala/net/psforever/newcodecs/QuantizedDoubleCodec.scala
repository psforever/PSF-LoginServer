// Copyright (c) 2017 PSForever
package net.psforever.newcodecs

import scodec.{Attempt, Codec, DecodeResult, Err, SizeBound}
import scodec.bits.{BitVector, ByteOrdering}

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

  override def encode(value: Double) = {
    Attempt.successful(BitVector.fromInt(QuantizeDouble(value), bits, ByteOrdering.LittleEndian))
  }

  override def decode(buffer: BitVector) = {
    if (buffer.sizeGreaterThanOrEqual(bitsL))
      Attempt.successful(
        DecodeResult(UnquantizeDouble(buffer.take(bitsL).toInt(false, ByteOrdering.LittleEndian)), buffer.drop(bitsL))
      )
    else
      Attempt.failure(Err.insufficientBits(bitsL, buffer.size))
  }

  override def toString = description
}
