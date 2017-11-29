// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.packet.game.ItemTransactionMessage

class BFRTerminalDefinition extends TerminalDefinition(143) {
  Name = "bfr_terminal"

  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    bfrVehicles.get(msg.item_name) match {
      case Some(vehicle) =>
        //Terminal.BuyVehicle(vehicle, Nil)
        Terminal.NoDeal()
      case None =>
        Terminal.NoDeal()
    }
  }
}
