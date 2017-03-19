// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of an object that can be interacted with when using a variety of terminals.
  * This object is generally invisible.
  * @param pos where and how the object is oriented
  */
final case class CommonTerminalData(pos : PlacementData) extends ConstructorData {
  override def bitsize : Long = 24L + pos.bitsize
}

object CommonTerminalData extends Marshallable[CommonTerminalData] {
  implicit val codec : Codec[CommonTerminalData] = (
    ("pos" | PlacementData.codec) ::
      bool ::
      bool ::
      uint(22)
    ).exmap[CommonTerminalData] (
    {
      case pos :: false :: true :: 0 :: HNil =>
        Attempt.successful(CommonTerminalData(pos))
      case _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid terminal data format"))
    },
    {
      case CommonTerminalData(pos) =>
        Attempt.successful(pos :: false :: true :: 0 :: HNil)
    }
  )
}
