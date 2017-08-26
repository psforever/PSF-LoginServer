// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.{EquipmentSlot, Vehicle}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.MountItem.MountItem
import net.psforever.packet.game.objectcreate.{CommonFieldData, DriveState, MountItem, PlacementData, VehicleData}

import scala.annotation.tailrec
import scala.util.{Success, Try}

class VehicleConverter extends ObjectCreateConverter[Vehicle]() {
  /* Vehicles do not have a conversion for `0x18` packet data. */

  override def ConstructorData(obj : Vehicle) : Try[VehicleData] = {
    Success(
      VehicleData(
        CommonFieldData(
          PlacementData(obj.Position, obj.Orientation, obj.Velocity),
          obj.Faction,
          0,
          if(obj.Owner.isDefined) { obj.Owner.get } else { PlanetSideGUID(0) } //this is the owner field, right?
        ),
        0,
        obj.Health / obj.MaxHealth * 255, //TODO not precise
        0,
        DriveState.Mobile,
        false,
        0,
        Some(MakeMountings(obj).sortBy(_.parentSlot))
      )
    )
    //TODO work utilities into this mess?
  }

  /**
    * For an object with a list of weapon mountings, convert those weapons into data as if found in an `0x17` packet.
    * @param obj the Vehicle game object
    * @return the converted data
    */
  private def MakeMountings(obj : Vehicle) : List[MountItem] = recursiveMakeMountings(obj.Weapons.iterator)

  @tailrec private def recursiveMakeMountings(iter : Iterator[(Int,EquipmentSlot)], list : List[MountItem] = Nil) : List[MountItem] = {
    if(!iter.hasNext) {
      list
    }
    else {
      val (index, slot) = iter.next
      if(slot.Equipment.isDefined) {
        val equip : Equipment = slot.Equipment.get
        recursiveMakeMountings(
          iter,
          list :+ MountItem(equip.Definition.ObjectId, equip.GUID, index, equip.Definition.Packet.ConstructorData(equip).get)
        )
      }
      else {
        recursiveMakeMountings(iter, list)
      }
    }
  }
}
