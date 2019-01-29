// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Tool
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.{CommonFieldData, DetailedWeaponData, InternalSlot, WeaponData}
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable.ListBuffer
import scala.util.{Success, Try}

class ToolConverter extends ObjectCreateConverter[Tool]() {
  override def ConstructorData(obj : Tool) : Try[WeaponData] = {
    val maxSlot : Int = obj.MaxAmmoSlot
    val slots : ListBuffer[InternalSlot] = ListBuffer[InternalSlot]()
    (0 until maxSlot).foreach(index => {
      val box = obj.AmmoSlots(index).Box
      slots += InternalSlot(box.Definition.ObjectId, box.GUID, index, box.Definition.Packet.ConstructorData(box).get)
    })
    Success(
      WeaponData(
        CommonFieldData(
          PlanetSideEmpire.NEUTRAL, //TODO need faction affinity
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
        slots.toList
      )
    )
  }

  override def DetailedConstructorData(obj : Tool) : Try[DetailedWeaponData] = {
    val maxSlot : Int = obj.MaxAmmoSlot
    val slots : ListBuffer[InternalSlot] = ListBuffer[InternalSlot]()
    (0 until maxSlot).foreach(index => {
      val box = obj.AmmoSlots(index).Box
      slots += InternalSlot(box.Definition.ObjectId, box.GUID, index, box.Definition.Packet.DetailedConstructorData(box).get)
    })
    Success(
      DetailedWeaponData(
        CommonFieldData(
          PlanetSideEmpire.NEUTRAL, //TODO need faction affinity
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
        slots.toList
      )
    )
  }
}
