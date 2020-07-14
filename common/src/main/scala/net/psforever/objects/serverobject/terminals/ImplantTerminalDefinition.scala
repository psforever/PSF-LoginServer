// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.definition.ImplantDefinition

/**
  * Data for the `Definition` for any `Terminal` that is of a type "implant_terminal_interface."
  * Implant terminals are composed of two components.
  * This `Definition` constructs the invisible interface component (interacted with as a game window).
  * Unlike other `Terminal` objects in the game, this one must be constructed on the client and
  * attached as a child of the visible implant terminal component - the "implant_terminal_mech."
  */
object ImplantTerminalDefinition {
  val implants: Map[String, ImplantDefinition] = Map(
    "advanced_regen"   -> GlobalDefinitions.advanced_regen,
    "targeting"        -> GlobalDefinitions.targeting,
    "audio_amplifier"  -> GlobalDefinitions.audio_amplifier,
    "darklight_vision" -> GlobalDefinitions.darklight_vision,
    "melee_booster"    -> GlobalDefinitions.melee_booster,
    "personal_shield"  -> GlobalDefinitions.personal_shield,
    "range_magnifier"  -> GlobalDefinitions.range_magnifier,
    "second_wind"      -> GlobalDefinitions.second_wind,
    "silent_run"       -> GlobalDefinitions.silent_run,
    "surge"            -> GlobalDefinitions.surge
  )
}
