package scodec.interop

import scodec.bits.ByteVector

import _root_.akka.util.ByteString

package object akka {

  implicit class EnrichedByteString(val value: ByteString) extends AnyVal {
    def toByteVector: ByteVector = ByteVector.viewAt((idx: Long) => value(idx.toInt), value.size.toLong)
  }

  implicit class EnrichedByteVector(val value: ByteVector) extends AnyVal {
    def toByteString: ByteString = PrivacyHelper.createByteString1C(value.toArray)
  }
}
