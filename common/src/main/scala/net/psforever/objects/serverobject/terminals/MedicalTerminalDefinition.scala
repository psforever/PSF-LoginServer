// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.packet.game.ItemTransactionMessage

class MedicalTerminalDefinition(objectId : Int) extends TerminalDefinition(objectId) {
  Name = if(objectId == 38) {
    "adv_med_terminal"
  }
  else if(objectId == 225) {
    "crystals_health_a"
  }
  else if(objectId == 226) {
    "crystals_health_b"
  }
  else if(objectId == 529) {
    "medical_terminal"
  }
  else if(objectId == 689) {
    "portable_med_terminal"
  }
  else {
    throw new IllegalArgumentException("terminal must be either object id 38, object id 529, or object id 689")
  }

  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
}
