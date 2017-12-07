// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.packet.game.ItemTransactionMessage

class DropshipVehicleTerminalDefinition extends TerminalDefinition(263) {
  private val flightVehicles = flight1Vehicles ++ flight2Vehicles
  Name = "dropship_vehicle_terminal"

  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    flightVehicles.get(msg.item_name) match {
      case Some(vehicle) =>
        Terminal.BuyVehicle(vehicle(), Nil)
      case None =>
        Terminal.NoDeal()
    }
  }
}
