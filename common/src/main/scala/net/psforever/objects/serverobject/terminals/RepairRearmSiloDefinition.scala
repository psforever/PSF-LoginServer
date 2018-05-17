// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.loadouts.VehicleLoadout
import net.psforever.objects.serverobject.terminals.EquipmentTerminalDefinition.BuildSimplifiedPattern
import net.psforever.packet.game.ItemTransactionMessage

/**
  * The `Definition` for any `Terminal` that is of a type "repair_silo."
  * Has both proximity-based operation and direct access purchasing power.
  */
class RepairRearmSiloDefinition(objectId : Int) extends EquipmentTerminalDefinition(objectId) with ProximityDefinition {
  Name = if(objectId == 719) {
    "pad_landing"
  }
  else if(objectId == 729) {
    "repair_silo"
  }
  else  {
    throw new IllegalArgumentException("repair re-arm terminal must be either object id 719 or 729")
  }

  private val buyFunc : (Player, ItemTransactionMessage)=>Terminal.Exchange = EquipmentTerminalDefinition.Buy(Map.empty, Map.empty, Map.empty)

  override def Buy(player: Player, msg : ItemTransactionMessage) : Terminal.Exchange = buyFunc(player, msg)

  override def Loadout(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    if(msg.item_page == 4) { //Favorites tab
      player.LoadLoadout(msg.unk1 + 10) match {
        case Some(loadout : VehicleLoadout) =>
          val weapons = loadout.visible_slots.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
          val inventory = loadout.inventory.map(entry => { InventoryItem(BuildSimplifiedPattern(entry.item), entry.index) })
          Terminal.VehicleLoadout(loadout.vehicle_definition, weapons, inventory)
        case _ =>
          Terminal.NoDeal()
      }
    }
    else {
      Terminal.NoDeal()
    }
  }
}
