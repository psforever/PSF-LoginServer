// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  */
final case class InternalTelepadDeployableData(data : CommonFieldData,
                                               unk1 : Int,
                                               unk2 : Int
                                              ) extends ConstructorData {
  override def bitsize : Long = 59L
}

object InternalTelepadDeployableData extends Marshallable[InternalTelepadDeployableData] {
  implicit val codec : Codec[InternalTelepadDeployableData] = (
    ("data" | CommonFieldData.codec) ::
      ("unk1" | uint8) ::
      uint8 ::
      ("unk2" | uint4)
    ).exmap[InternalTelepadDeployableData] (
    {
      case data :: unk1 :: 0 :: unk2 :: HNil  =>
        Attempt.successful(InternalTelepadDeployableData(data, unk1, unk2))

      case data =>
        Attempt.failure(Err(s"invalid internal telepad data format - $data"))
    },
    {
      case InternalTelepadDeployableData(data, unk1, unk2) =>
        Attempt.successful(data :: unk1 :: 0 :: unk2 :: HNil)
    }
  )
}
