// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.objects.definition.ImplantDefinition
import net.psforever.objects.definition.converter.ImplantTerminalInterfaceConverter
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.packet.game.objectcreate.ObjectClass

/**
  * The `Definition` for any `Terminal` that is of a type "implant_terminal_interface."
  * Implant terminals are composed of two components.
  * This `Definition` constructs the invisible interface component (interacted with as a game window).
  * Unlike other `Terminal` objects in the game, this one must be constructed on the client and
  * attached as a child of the visible implant terminal component - the "implant_terminal_mech."
  */
class ImplantTerminalInterfaceDefinition extends TerminalDefinition(ObjectClass.implant_terminal_interface) {
  private val implants : Map[String, ImplantDefinition] = Map (
    "advanced_regen" -> GlobalDefinitions.advanced_regen,
    "targeting" -> GlobalDefinitions.targeting,
    "audio_amplifier" -> GlobalDefinitions.audio_amplifier,
    "darklight_vision" -> GlobalDefinitions.darklight_vision,
    "melee_booster" -> GlobalDefinitions.melee_booster,
    "personal_shield" -> GlobalDefinitions.personal_shield,
    "range_magnifier" -> GlobalDefinitions.range_magnifier,
    "second_wind" -> GlobalDefinitions.second_wind,
    "silent_run" -> GlobalDefinitions.silent_run,
    "surge" -> GlobalDefinitions.surge
  )
  Packet = new ImplantTerminalInterfaceConverter
  Name = "implante_terminal_interface"

  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    implants.get(msg.item_name) match {
      case Some(implant) =>
        Terminal.LearnImplant(implant)
      case None =>
        Terminal.NoDeal()
    }
  }

  override def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    implants.get(msg.item_name) match {
      case Some(implant) =>
        Terminal.SellImplant(implant)
      case None =>
        Terminal.NoDeal()
    }
  }
}
