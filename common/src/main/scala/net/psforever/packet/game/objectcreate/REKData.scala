// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  * @param data na
  * @param unk1 na;
  *            defaults to 0
  * @see `DetailedREKData`
  */
final case class REKData(data : CommonFieldData,
                         unk1 : Int,
                         unk2 : Int
                        ) extends ConstructorData {
  override def bitsize : Long = 50L
}

object REKData extends Marshallable[REKData] {
  def apply(data : CommonFieldData) : REKData = REKData(data, 0, 0)

  implicit val codec : Codec[REKData] = (
    ("data" | CommonFieldData.codec2) ::
      ("unk1" | uint16) ::
      ("unk2" | uint(10))
    ).exmap[REKData] (
    {
      case data :: u1 :: u2 :: HNil  =>
        Attempt.successful(REKData(data, u1, u2))
      case data =>
        Attempt.failure(Err(s"invalid rek data format - $data"))
    },
    {
      case REKData(data, u1, u2) =>
        Attempt.successful(data :: u1 :: u2 :: HNil)
    }
  )
}
