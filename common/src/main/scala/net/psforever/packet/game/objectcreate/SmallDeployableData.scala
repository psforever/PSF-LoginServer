// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire
import scodec.Codec
import scodec.codecs._

/**
  * A representation of simple objects that are spawned by the adaptive construction engine.
  * //@param deploy data common to objects spawned by the (advanced) adaptive construction engine
  */
final case class SmallDeployableData(pos : PlacementData,
                                     faction : PlanetSideEmpire.Value,
                                     bops : Boolean,
                                     destroyed : Boolean,
                                     unk1 : Int,
                                     jammered : Boolean,
                                     unk2 : Boolean,
                                     owner_guid : PlanetSideGUID) extends ConstructorData {
  override def bitsize : Long = {
    val posSize = pos.bitsize
    24 + posSize
  }
}

object SmallDeployableData extends Marshallable[SmallDeployableData] {
  def apply(pos : PlacementData, faction : PlanetSideEmpire.Value, unk1 : Int, jammered : Boolean, unk2 : Boolean) : SmallDeployableData = {
    SmallDeployableData(pos, faction, false, false, unk1, jammered, unk2, PlanetSideGUID(0))
  }

  implicit val codec : Codec[SmallDeployableData] = (
    ("pos" | PlacementData.codec) ::
      ("faction" | PlanetSideEmpire.codec) ::
      ("bops" | bool) ::
      ("destroyed" | bool) ::
      ("unk1" | uint2L) :: //3 - na, 2 - common, 1 - na, 0 - common?
      ("jammered" | bool) ::
      ("unk2" | bool) ::
      ("owner_guid" | PlanetSideGUID.codec)
    ).as[SmallDeployableData]
}
