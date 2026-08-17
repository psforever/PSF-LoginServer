// Copyright (c) 2021 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.types.PlanetSideEmpire
import scodec.codecs._
import scodec.Codec
import shapeless.{::, HNil}

/**
  * A representation of a stationary projectile field.
  * @param pos where the vehicle is and how it is oriented in the game world
  * @param faction faction affinity
  */
final case class RadiationCloudData(
                                     pos: PlacementData,
                                     faction: PlanetSideEmpire.Value
                                   ) extends ConstructorData {
  override def bitsize: Long = {
    pos.bitsize + 24L
  }
}

object RadiationCloudData extends Marshallable[RadiationCloudData] {
  implicit val codec: Codec[RadiationCloudData] = {
    ("pos" | PlacementData.codec) ::
    ignore(size = 1) ::
    ("faction" | PlanetSideEmpire.codec) ::
    uint(bits = 4) ::
    ignore(size = 17)
  }.xmap[RadiationCloudData] (
    {
      case pos :: _ :: fac :: _ :: _ :: HNil =>
        RadiationCloudData(pos, fac)
    },
    {
      case RadiationCloudData(pos, fac) =>
        pos :: () :: fac :: 4 :: () :: HNil
    }
  )
}
