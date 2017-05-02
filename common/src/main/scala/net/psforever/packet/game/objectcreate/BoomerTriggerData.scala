// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the detonator utility that is created when putting down a Boomer with an ACE.
  * @param unk na
  */
final case class BoomerTriggerData(unk : Int = 0x8) extends ConstructorData {
  override def bitsize : Long = 34L
}

object BoomerTriggerData extends Marshallable[BoomerTriggerData] {
  implicit val codec : Codec[BoomerTriggerData] = (
    uint4L ::
      uint4L ::
      uint(26)
    ).exmap[BoomerTriggerData] (
    {
      case 0xC :: unk :: 0 :: HNil =>
        Attempt.successful(BoomerTriggerData(unk))
      case _ =>
        Attempt.failure(Err("invalid command detonater format"))
    },
    {
      case BoomerTriggerData(unk) =>
        Attempt.successful(0xC :: unk :: 0 :: HNil)
    }
  )
}
