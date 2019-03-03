// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Tool
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.{CommonFieldData, DetailedWeaponData, InternalSlot, WeaponData}

import scala.util.{Success, Try}

class ToolConverter extends ObjectCreateConverter[Tool]() {
  override def ConstructorData(obj : Tool) : Try[WeaponData] = {
    val slots : List[InternalSlot] = (0 until obj.MaxAmmoSlot).map(index => {
      val box = obj.AmmoSlots(index).Box
      InternalSlot(box.Definition.ObjectId, box.GUID, index, box.Definition.Packet.ConstructorData(box).get)
    }).toList
    Success(
      WeaponData(
        CommonFieldData(
          obj.Faction,
          bops = false,
          alternate = false,
          true,
          None,
          false,
          None,
          None,
          PlanetSideGUID(0)
        ),
        obj.FireModeIndex,
        slots
      )
    )
  }

  override def DetailedConstructorData(obj : Tool) : Try[DetailedWeaponData] = {
    val slots : List[InternalSlot] = (0 until obj.MaxAmmoSlot).map(index => {
      val box = obj.AmmoSlots(index).Box
      InternalSlot(box.Definition.ObjectId, box.GUID, index, box.Definition.Packet.DetailedConstructorData(box).get)
    }).toList
    Success(
      DetailedWeaponData(
        CommonFieldData(
          obj.Faction,
          bops = false,
          alternate = false,
          true,
          None,
          false,
          None,
          None,
          PlanetSideGUID(0)
        ),
        obj.FireModeIndex,
        slots
      )
    )
  }
}
