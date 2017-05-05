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
final case class CommandDetonaterData(unk1 : Int = 0,
                                      unk2 : Int = 0) extends ConstructorData {
  override def bitsize : Long = 34L
}

object CommandDetonaterData extends Marshallable[CommandDetonaterData] {
  implicit val codec : Codec[CommandDetonaterData] = (
    ("unk1" | uint4L) ::
      ("unk2" | uint4L) ::
      uint(26)
    ).exmap[CommandDetonaterData] (
    {
      case unk1 :: unk2 :: 0 :: HNil =>
        Attempt.successful(CommandDetonaterData(unk1, unk2))
      case _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid command detonator data format"))
    },
    {
      case CommandDetonaterData(unk1, unk2) =>
        Attempt.successful(unk1 :: unk2 :: 0 :: HNil)
    }
  )
}
