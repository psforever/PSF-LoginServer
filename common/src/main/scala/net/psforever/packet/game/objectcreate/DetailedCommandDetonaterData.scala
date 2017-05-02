// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the command uplink device.<br>
  * I don't know much about the command uplink device so someone else has to provide this commentary.
  */
final case class DetailedCommandDetonaterData(unk1 : Int = 8,
                                              unk2 : Int = 0) extends ConstructorData {
  override def bitsize : Long = 51L
}

object DetailedCommandDetonaterData extends Marshallable[DetailedCommandDetonaterData] {
  implicit val codec : Codec[DetailedCommandDetonaterData] = (
    ("unk1" | uint4L) ::
      ("unk2" | uint4L) ::
      uint(20) ::
      uint4L ::
      uint16 ::
      uint(3)
    ).exmap[DetailedCommandDetonaterData] (
    {
      case unk1 :: unk2 :: 0 :: 2 :: 0 :: 4 :: HNil =>
        Attempt.successful(DetailedCommandDetonaterData(unk1, unk2))
      case _ =>
        Attempt.failure(Err("invalid command detonator data format"))
    },
    {
      case DetailedCommandDetonaterData(unk1, unk2) =>
        Attempt.successful(unk1 :: unk2 :: 0 :: 2 :: 0 :: 4 :: HNil)
    }
  )
}
