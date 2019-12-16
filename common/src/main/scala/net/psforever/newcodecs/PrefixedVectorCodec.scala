// Copyright (c) 2019 PSForever
package net.psforever.newcodecs

import scodec._
import scodec.bits.BitVector

final class PrefixedVectorCodec[A](firstCodec: Codec[A], codec: Codec[A], limit: Option[Int] = None) extends Codec[Vector[A]] {

  def sizeBound = limit match {
    case None => SizeBound.unknown
    case Some(lim) => codec.sizeBound * lim.toLong
  }

  def encode(vector: Vector[A]) = Encoder.encodeSeq(firstCodec)(vector.slice(0,1)).map { bits =>
      if (vector.length > 1)
        bits ++ (Encoder.encodeSeq(codec)(vector.tail) getOrElse BitVector.empty)
      else
        bits
  }

  def decode(buffer: BitVector) : scodec.Attempt[scodec.DecodeResult[Vector[A]]] = {
      Decoder.decodeCollect[Vector, A](firstCodec,  Some(1))(buffer) match {
        case Attempt.Successful(firstValue) =>
          Decoder.decodeCollect[Vector, A](codec, limit map { _ - 1 })(firstValue.remainder) match {
            case Attempt.Successful(secondValue) =>
              Attempt.successful(DecodeResult(firstValue.value ++ secondValue.value, secondValue.remainder))
            case Attempt.Failure(e) => Attempt.failure(e)
          }
        case Attempt.Failure(e) => Attempt.failure(e)
      }
  }

  override def toString = s"vector($codec)"
}
