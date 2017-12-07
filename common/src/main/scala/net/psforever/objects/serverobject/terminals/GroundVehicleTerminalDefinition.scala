// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.packet.game.ItemTransactionMessage

class GroundVehicleTerminalDefinition extends TerminalDefinition(386) {
  Name = "ground_vehicle_terminal"

  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    groundVehicles.get(msg.item_name) match {
      case Some(vehicle) =>
        Terminal.BuyVehicle(vehicle(), Nil)
      case None =>
        Terminal.NoDeal()
    }
  }
}
