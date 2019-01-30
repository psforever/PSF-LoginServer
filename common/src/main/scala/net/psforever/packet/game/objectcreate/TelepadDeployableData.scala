// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of simple objects that are spawned by the adaptive construction engine.
  * @param deploy data common to game objects
  * @param unk1 na
  * @param unk2 na
  */
final case class TelepadDeployableData(deploy : CommonFieldDataWithPlacement,
                                       unk1 : Int,
                                       unk2 : Int) extends ConstructorData {
  override def bitsize : Long = {
    20 + deploy.bitsize
  }
}

object TelepadDeployableData extends Marshallable[TelepadDeployableData] {
  implicit val codec : Codec[TelepadDeployableData] = (
    ("deploy" | CommonFieldDataWithPlacement.codec) ::
      ("unk1" | uint8) ::
      uint8 ::
      ("unk2" | uint4)
    ).exmap[TelepadDeployableData] (
    {
      case data :: unk1 :: 0 :: unk2 :: HNil  =>
        Attempt.successful(TelepadDeployableData(data, unk1, unk2))

      case data =>
        Attempt.failure(Err(s"invalid telepad data format - $data"))
    },
    {
      case TelepadDeployableData(data, unk1, unk2) =>
        Attempt.successful(data :: unk1 :: 0 :: unk2 :: HNil)
    }
  )
}
