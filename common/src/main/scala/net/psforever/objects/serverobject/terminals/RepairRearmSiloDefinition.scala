// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.packet.game.ItemTransactionMessage

class RepairRearmSiloDefinition(objectId : Int) extends EquipmentTerminalDefinition(objectId) {
  Name = "repair_silo"

  private val buyFunc : (Player, ItemTransactionMessage)=>Terminal.Exchange = EquipmentTerminalDefinition.Buy(Map.empty, Map.empty, Map.empty)

  override def Buy(player: Player, msg : ItemTransactionMessage) : Terminal.Exchange = buyFunc(player, msg)

  override def Loadout(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    if(msg.item_page == 4) { //Favorites tab
      player.LoadLoadout(msg.unk1) match {
        case Some(loadout) =>
          Terminal.VehicleLoadout(Nil, Nil)
        case None =>
          Terminal.NoDeal()
      }
    }
    else {
      Terminal.NoDeal()
    }
  }
}
