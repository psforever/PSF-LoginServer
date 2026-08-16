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
final case class DetailedREKData(data: CommonFieldData, unk: Int = 0) extends ConstructorData {
  override def bitsize: Long = {
    val dataSize = data.bitsize
    ToolPatternData.bitsize + dataSize + 15L
  }
}

object DetailedREKData extends Marshallable[DetailedREKData] {
  private val base: ToolPatternData = ToolPatternData(u1 = 2, u2 = 0, u3 = true, u4 = false, u5 = None, u6 = false)

  implicit val codec: Codec[DetailedREKData] = (
    ("data" | CommonFieldData.codec(extra = true)) ::
      ToolPatternData.codec ::
      ("unk" | uint8) ::
      uint(bits = 7)
  ).exmap[DetailedREKData](
    {
      case data :: _ :: unk :: 0 :: HNil =>
        Attempt.successful(DetailedREKData(data, unk))
      case data =>
        Attempt.failure(Err(s"invalid detailed rek data format - $data"))
    },
    {
      case DetailedREKData(data, unk) =>
        Attempt.successful(data :: base :: unk :: 0 :: HNil)
    }
  )
}
