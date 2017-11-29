// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.packet.game.ItemTransactionMessage

class VehicleTerminalCombinedDefinition extends TerminalDefinition(952) {
  private val vehicles = groundVehicles ++ flight1Vehicles
  Name = "vehicle_terminal_combined"

  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    vehicles.get(msg.item_name) match {
      case Some(vehicle) =>
        Terminal.BuyVehicle(vehicle(), Nil)
      case None =>
        Terminal.NoDeal()
    }
  }
}
