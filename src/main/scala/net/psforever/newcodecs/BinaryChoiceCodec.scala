// Copyright (c) 2017 PSForever
package net.psforever.newcodecs

import scodec.Codec
import scodec.bits.BitVector

private[newcodecs] final class BinaryChoiceCodec[A](choice: Boolean, codec_true: => Codec[A], codec_false: => Codec[A])
    extends Codec[A] {

  private lazy val evaluatedCodec_true = codec_true

  private lazy val evaluatedCodec_false = codec_false

  override def sizeBound = if (choice) evaluatedCodec_true.sizeBound else evaluatedCodec_false.sizeBound

  override def encode(a: A) = {
    if (choice)
      evaluatedCodec_true.encode(a)
    else
      evaluatedCodec_false.encode(a)
  }

  override def decode(buffer: BitVector) = {
    if (choice)
      evaluatedCodec_true.decode(buffer)
    else
      evaluatedCodec_false.decode(buffer)
  }

  override def toString =
    if (choice) s"binarychoice(true, $evaluatedCodec_true, ?)" else "binarychoice(false, ?, $evaluatedCodec_false)"

}
