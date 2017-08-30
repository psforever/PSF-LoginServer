// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.LockerContainer
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.GridInventory
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate._

import scala.util.{Success, Try}

class LockerContainerConverter extends ObjectCreateConverter[LockerContainer]() {
  override def ConstructorData(obj : LockerContainer) : Try[LockerContainerData] = {
    Success(LockerContainerData(InventoryData(MakeInventory(obj.Inventory))))
  }

  override def DetailedConstructorData(obj : LockerContainer) : Try[DetailedLockerContainerData] = {
    Success(DetailedLockerContainerData(8))
  }

  /**
    * Transform a list of contained items into a list of contained `InternalSlot` objects.
    * All objects will take the form of data as if found in an `0x17` packet.
    * @param inv the inventory container
    * @return a list of all items that were in the inventory in decoded packet form
    */
  private def MakeInventory(inv : GridInventory) : List[InternalSlot] = {
    inv.Items
      .map({
        case(guid, item) =>
          val equip : Equipment = item.obj
          InternalSlot(equip.Definition.ObjectId, PlanetSideGUID(guid), item.start, equip.Definition.Packet.ConstructorData(equip).get)
      }).toList
    }
}
