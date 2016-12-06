// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import scodec.bits.BitVector

case class RecoveredData(data : BitVector
                     ) extends ConstructorData {
  override def bsize : Long = data.size
}

object RecoveredData extends Marshallable[RecoveredData] {
  implicit val codec : Codec[RecoveredData] = (
    "data" | bits
    ).as[RecoveredData]

  val genericCodec : Codec[ConstructorData.genericPattern] = codec.exmap[ConstructorData.genericPattern] (
    {
      case _ =>
        Attempt.failure(Err("un-parsed byte data preserved when decoding failed"))
    },
    {
      case _ =>
        Attempt.failure(Err("can not encode object"))
    }
  )
}
