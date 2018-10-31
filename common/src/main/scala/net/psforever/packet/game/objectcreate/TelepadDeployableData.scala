// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire
import scodec.Codec
import scodec.codecs._

/**
  * A representation of simple objects that are spawned by the adaptive construction engine.
  * @param pos na
  * @param faction na
  * @param bops na
  * @param destroyed na
  * @param unk1 na
  * @param unk2 na
  * @param router_guid the associated Router vehicle;
  *                    this is an essential non-blank (16u 0x0) field;
  *                    a blanked field will cause the client to crash
  * @param owner_guid the owner of this telepad
  * @param unk3 na
  * @param unk4 na
  */
//TODO might be CommonFieldData
final case class TelepadDeployableData(pos : PlacementData,
                                       faction : PlanetSideEmpire.Value,
                                       bops : Boolean,
                                       destroyed : Boolean,
                                       unk1 : Int,
                                       unk2 : Boolean,
                                       router_guid : PlanetSideGUID,
                                       owner_guid : PlanetSideGUID,
                                       unk3 : Int,
                                       unk4 : Int) extends ConstructorData {
  override def bitsize : Long = {
    val posSize = pos.bitsize
    59 + posSize
  }
}

object TelepadDeployableData extends Marshallable[TelepadDeployableData] {
  implicit val codec : Codec[TelepadDeployableData] = (
    ("pos" | PlacementData.codec) ::
      ("faction" | PlanetSideEmpire.codec) ::
      ("bops" | bool) ::
      ("destroyed" | bool) ::
      ("unk1" | uint2L) :: //3 - na, 2 - common, 1 - na, 0 - common?
      ("unk2" | bool) ::
      ("router_guid" | PlanetSideGUID.codec) ::
      ("owner_guid" | PlanetSideGUID.codec) ::
      ("unk3" | uint16L) ::
      ("unk4" | uint4)
    ).as[TelepadDeployableData]
}
