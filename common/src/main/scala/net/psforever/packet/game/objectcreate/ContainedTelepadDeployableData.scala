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
final case class ContainedTelepadDeployableData(unk : Int,
                                                router_guid : PlanetSideGUID) extends ConstructorData {
  override def bitsize : Long = 59L
}

object ContainedTelepadDeployableData extends Marshallable[ContainedTelepadDeployableData] {
  implicit val codec : Codec[ContainedTelepadDeployableData] = (
    ("unk" | uint(7)) ::
      ("router_guid" | PlanetSideGUID.codec) ::
      uint16 ::
      uint4 ::
      uint16
    ).exmap[ContainedTelepadDeployableData] (
    {
      case unk :: rguid :: 0 :: 8 :: 0 :: HNil  =>
        Attempt.successful(ContainedTelepadDeployableData(unk, rguid))
      case _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid rek data format"))
    },
    {
      case ContainedTelepadDeployableData(unk, rguid) =>
        Attempt.successful(unk :: rguid :: 0 :: 8 :: 0 :: HNil)
    }
  )
}
