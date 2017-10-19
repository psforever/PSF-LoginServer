// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.{EquipmentSlot, Vehicle}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.{InventoryItemData, _}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

class VehicleConverter extends ObjectCreateConverter[Vehicle]() {
  override def DetailedConstructorData(obj : Vehicle) : Try[VehicleData] = Failure(new Exception("VehicleConverter should not be used to generate detailed VehicleData"))

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
        false, false,
        DriveState.Mobile,
        false,
        false,
        false,
        SpecificFormatData(obj),
        Some(InventoryData(MakeMountings(obj).sortBy(_.parentSlot)))
      )(SpecificFormatModifier)
    )
  }

  /**
    * For an object with a list of weapon mountings, convert those weapons into data as if found in an `0x17` packet.
    * @param obj the Vehicle game object
    * @return the converted data
    */
  private def MakeMountings(obj : Vehicle) : List[InventoryItemData.InventoryItem] = recursiveMakeMountings(obj.Weapons.iterator)

  @tailrec private def recursiveMakeMountings(iter : Iterator[(Int,EquipmentSlot)], list : List[InventoryItemData.InventoryItem] = Nil) : List[InventoryItemData.InventoryItem] = {
    if(!iter.hasNext) {
      list
    }
    else {
      val (index, slot) = iter.next
      if(slot.Equipment.isDefined) {
        val equip : Equipment = slot.Equipment.get
        recursiveMakeMountings(
          iter,
          list :+ InventoryItemData(equip.Definition.ObjectId, equip.GUID, index, equip.Definition.Packet.ConstructorData(equip).get)
        )
      }
      else {
        recursiveMakeMountings(iter, list)
      }
    }
  }

  protected def SpecificFormatModifier : VehicleFormat.Value = VehicleFormat.Normal

  protected def SpecificFormatData(obj : Vehicle) : Option[SpecificVehicleData] = None
}
