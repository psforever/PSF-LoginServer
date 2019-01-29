// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the REK portion of `ObjectCreateDetailedMessage` packet data.
  * This data will help construct the "tool" called a Remote Electronics Kit.<br>
  * <br>
  * Of note is the first portion of the data which resembles the `DetailedWeaponData` format.
  * @param data na
  * @param unk na
  */
final case class DetailedREKData(data : CommonFieldData,
                                 unk : Int = 0
                                ) extends ConstructorData {
  override def bitsize : Long = {
    val dataSize = data.bitsize
    43L + dataSize
  }
}

object DetailedREKData extends Marshallable[DetailedREKData] {
  implicit val codec : Codec[DetailedREKData] = (
    ("data" | CommonFieldData.codec2) ::
      uint8 ::
      uint16L ::
      uint4L ::
      ("unk" | uint8) ::
      uint(7)
    ).exmap[DetailedREKData] (
    {
      case data :: 2 :: 0 :: 8 :: unk :: 0 :: HNil =>
        Attempt.successful(DetailedREKData(data, unk))
      case data =>
        Attempt.failure(Err(s"invalid detailed rek data format - $data"))
    },
    {
      case DetailedREKData(data, unk) =>
        Attempt.successful(data :: 2 :: 0 :: 8 :: unk :: 0 :: HNil)
    }
  )
}
