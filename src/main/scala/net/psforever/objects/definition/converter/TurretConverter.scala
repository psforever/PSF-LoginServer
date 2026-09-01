// Copyright (c) 2026 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.serverobject.turret.WeaponTurret
import net.psforever.packet.game.objectcreate.InventoryItemData

object TurretConverter {
  def MakeMountings(obj: WeaponTurret): List[InventoryItemData.InventoryItem] = {
    obj.Weapons
      .map({
        case (index, slot) =>
          val equip: Equipment = slot.Equipment.get
          val equipDef         = equip.Definition
          InventoryItemData(equipDef.ObjectId, equip.GUID, index, equipDef.Packet.ConstructorData(equip).get)
      })
      .toList
  }
}
