// Copyright (c) 2017 PSForever
package net.psforever.newcodecs

import scodec.Attempt
import scodec.Attempt.{Failure, Successful}
import scodec._
import scodec.bits.BitVector

package object newcodecs {
  def q_double(min: Double, max: Double, bits: Int): Codec[Double] = new QuantizedDoubleCodec(min, max, bits)

  def q_float(min : Double, max : Double, bits : Int): Codec[Float] = q_double(min, max, bits).narrow(v => Attempt.successful(v.toFloat), _.toDouble)

  def binary_choice[A](choice: Boolean, codec_true: => Codec[A], codec_false: => Codec[A]): Codec[A] = new BinaryChoiceCodec(choice, codec_true, codec_false)

  def prefixedVectorOfN[A](countCodec: Codec[Int], firstValueCodec: Codec[A], valueCodec: Codec[A]): Codec[Vector[A]] =
    countCodec.
      flatZip { count => new PrefixedVectorCodec(firstValueCodec, valueCodec, Some(count)) }.
      narrow[Vector[A]]({ case (cnt, xs) =>
        if (xs.size == cnt) Attempt.successful(xs)
        else Attempt.failure(Err(s"Insufficient number of elements: decoded ${xs.size} but should have decoded $cnt"))
      }, xs => (xs.size, xs)).
      withToString(s"vectorOfN($countCodec, $valueCodec)")
}
