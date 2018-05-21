// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

/**
  * The definition for any `Terminal` that is of a type "medical_terminal".
  * This includes the functionality of the formal medical terminals and some of the cavern crystals.
  * Do not confuse the game's internal "medical_terminal" object category and the actual `medical_terminal` object (529).
  */
class MedicalTerminalDefinition(objectId : Int) extends TerminalDefinition(objectId) with ProximityDefinition {
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
}
