// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  * @param unk1 na
  * @param unk2 na;
  *            defaults to 0
  * @see `DetailedREKData`
  */
final case class REKData(unk1 : Int,
                         unk2 : Int,
                         unk3 : Int = 0
                        ) extends ConstructorData {
  override def bitsize : Long = 50L
}

object REKData extends Marshallable[REKData] {
  implicit val codec : Codec[REKData] = (
    ("unk1" | uint4L) ::
      ("unk2" | uint4L) ::
      uint(28) ::
      ("unk3" | uint4L) ::
      uint(10)
    ).exmap[REKData] (
    {
      case unk1 :: unk2 :: 0 :: unk3 :: 0 :: HNil  =>
        Attempt.successful(REKData(unk1, unk2, unk3))
      case _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid rek data format"))
    },
    {
      case REKData(unk1, unk2, unk3) =>
        Attempt.successful(unk1 :: unk2 :: 0 :: unk3 :: 0 :: HNil)
    }
  )
}
