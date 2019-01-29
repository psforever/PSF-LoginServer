// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  * @param data na
  * @param unk na;
  *            defaults to 0
  * @see `DetailedREKData`
  */
final case class REKData(data : CommonFieldData,
                         unk : Int = 0
                        ) extends ConstructorData {
  override def bitsize : Long = 50L
}

object REKData extends Marshallable[REKData] {
  implicit val codec : Codec[REKData] = (
    ("data" | CommonFieldData.codec2) ::
      uint8 ::
      ("unk" | uint8) ::
      uint(10)
    ).exmap[REKData] (
    {
      case data :: 0 :: unk :: 0 :: HNil  =>
        Attempt.successful(REKData(data, unk))
      case data =>
        Attempt.failure(Err(s"invalid rek data format - $data"))
    },
    {
      case REKData(data, unk) =>
        Attempt.successful(data :: 0 :: unk :: 0 :: HNil)
    }
  )
}
