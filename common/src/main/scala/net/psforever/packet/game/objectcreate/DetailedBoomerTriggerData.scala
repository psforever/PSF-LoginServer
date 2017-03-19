// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the detonater utility that is created when putting down a Boomer with an ACE.
  */
final case class DetailedBoomerTriggerData() extends ConstructorData {
  override def bitsize : Long = 51L
}

object DetailedBoomerTriggerData extends Marshallable[DetailedBoomerTriggerData] {
  implicit val codec : Codec[DetailedBoomerTriggerData] = (
    uint8L ::
      uint(22) ::
      bool :: //true
      uint(17) ::
      bool :: //true
      uint2L
  ).exmap[DetailedBoomerTriggerData] (
    {
      case 0xC8 :: 0 :: true :: 0 :: true :: 0 :: HNil =>
        Attempt.successful(DetailedBoomerTriggerData())
      case _ :: _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid command detonater format"))
    },
    {
      case DetailedBoomerTriggerData() =>
        Attempt.successful(0xC8 :: 0 :: true :: 0 :: true :: 0 :: HNil)
    }
  )
}
