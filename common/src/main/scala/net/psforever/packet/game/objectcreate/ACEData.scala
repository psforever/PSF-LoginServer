// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of an adaptive construction engine (ACE).
  * This one-time-use item deploys a variety of utilities into the game environment.
  * Has an advanced version internally called an `advanced_ace` and commonly called a Field Deployment Unit (FDU).
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  */
final case class ACEData(unk1 : Int,
                         unk2 : Int,
                         unk3 : Int = 0
                        ) extends ConstructorData {
  override def bitsize : Long = 34L
}

object ACEData extends Marshallable[ACEData] {
  implicit val codec : Codec[ACEData] = (
    ("unk1" | uint4L) ::
      ("unk2" | uint4L) ::
      uint(20) ::
      ("unk3" | uint4L) ::
      uint2L
  ).exmap[ACEData] (
    {
      case unk1 :: unk2 :: 0 :: unk3 :: 0 :: HNil =>
        Attempt.successful(ACEData(unk1, unk2, unk3))
      case _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid ace data format"))
    },
    {
      case ACEData(unk1, unk2, unk3) =>
        Attempt.successful(unk1 :: unk2 :: 0 :: unk3 :: 0 :: HNil)
    }
  )
}
