// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.GridInventory
import net.psforever.objects.locker.LockerEquipment
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

import scala.util.{Success, Try}

class LockerContainerConverter extends ObjectCreateConverter[LockerEquipment]() {
  override def ConstructorData(obj: LockerEquipment): Try[LockerContainerData] = {
    MakeInventory(obj.Inventory) match {
      case Nil =>
        Success(LockerContainerData(None))
      case list =>
        Success(LockerContainerData(InventoryData(list)))
    }
  }

  override def DetailedConstructorData(obj: LockerEquipment): Try[DetailedLockerContainerData] = {
    if (obj.Inventory.Size > 0) {
      Success(
        DetailedLockerContainerData(
          CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0)),
          Some(InventoryData(MakeDetailedInventory(obj.Inventory)))
        )
      )
    } else {
      Success(
        DetailedLockerContainerData(
          CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0)),
          None
        )
      )
    }
  }

  /**
    * Transform a list of contained items into a list of contained `InternalSlot` objects.
    * All objects will take the form of data as if found in an `0x17` packet.
    * @param inv the inventory container
    * @return a list of all items that were in the inventory in decoded packet form
    */
  private def MakeInventory(inv: GridInventory): List[InternalSlot] = {
    inv.Items
      .map(item => {
        val equip: Equipment = item.obj
        InternalSlot(
          equip.Definition.ObjectId,
          equip.GUID,
          item.start,
          equip.Definition.Packet.ConstructorData(equip).get
        )
      })
  }

  /**
    * Transform a list of contained items into a list of contained `InternalSlot` objects.
    * All objects will take the form of data as if found in an `0x18` packet.
    * @param inv the inventory container
    * @return a list of all items that were in the inventory in decoded packet form
    */
  private def MakeDetailedInventory(inv: GridInventory): List[InternalSlot] = {
    inv.Items
      .map(item => {
        val equip: Equipment = item.obj
        InternalSlot(
          equip.Definition.ObjectId,
          equip.GUID,
          item.start,
          equip.Definition.Packet.DetailedConstructorData(equip).get
        )
      })
  }
}
