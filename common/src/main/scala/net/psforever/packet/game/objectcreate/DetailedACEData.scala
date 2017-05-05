// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of an adaptive construction engine (ACE).
  * This one-time-use item deploys a variety of utilities into the game environment.
  * Has an advanced version internally called an `advanced_ace` and commonly called a Field Deployment Unit (FDU).
  * @param unk na
  */
final case class DetailedACEData(unk : Int) extends ConstructorData {
  override def bitsize : Long = 51L
}

object DetailedACEData extends Marshallable[DetailedACEData] {
  implicit val codec : Codec[DetailedACEData] = (
    ("unk" | uint4L) ::
      uint4L ::
      uintL(20) ::
      uint4L ::
      uint16L ::
      uint(3)
    ).exmap[DetailedACEData] (
    {
      case code :: 8 :: 0 :: 2 :: 0 :: 4 :: HNil =>
        Attempt.successful(DetailedACEData(code))
      case _ =>
        Attempt.failure(Err("invalid ace data format"))
    },
    {
      case DetailedACEData(code) =>
        Attempt.successful(code :: 8 :: 0 :: 2 :: 0 :: 4 :: HNil)
    }
  )
}
