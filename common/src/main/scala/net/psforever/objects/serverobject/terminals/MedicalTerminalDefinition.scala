// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.packet.game.ItemTransactionMessage

/**
  * The definition for any `Terminal` that is of a type "medical_terminal".
  * This includes the limited proximity-based functionality of the formal medical terminals
  * and the actual proximity-based functionality of the cavern crystals.<br>
  * <br>
  * Do not confuse the "medical_terminal" category and the actual `medical_terminal` object (529).
  * Objects created by this definition being linked by their use of `ProximityTerminalUseMessage` is more accurate.
  */
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
    throw new IllegalArgumentException("medical terminal must be either object id 38, 225, 226, 529, or 689")
  }

  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
}
