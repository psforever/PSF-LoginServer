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
final case class InternalTelepadDeployableData(data : CommonFieldData) extends ConstructorData {
  override def bitsize : Long = 59L
}

object InternalTelepadDeployableData extends Marshallable[InternalTelepadDeployableData] {
  implicit val codec : Codec[InternalTelepadDeployableData] = (
    ("data" | CommonFieldData.codec) ::
      uint4 ::
      uint16
    ).exmap[InternalTelepadDeployableData] (
    {
      case data :: 8 :: 0 :: HNil  =>
        Attempt.successful(InternalTelepadDeployableData(data))

      case data =>
        Attempt.failure(Err(s"invalid internal telepad data format - $data"))
    },
    {
      case InternalTelepadDeployableData(data) =>
        Attempt.successful(data :: 8 :: 0 :: HNil)
    }
  )
}
