// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.ActorContext
import net.psforever.objects.Player
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.ItemTransactionMessage

class TeleportPadTerminalDefinition extends EquipmentTerminalDefinition(853) {
  Name = "teleport_pad_terminal"

  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    Terminal.BuyEquipment(EquipmentTerminalDefinition.routerTerminal("router_telepad")())
  }
}

object TeleportPadTerminalDefinition {
  /**
    * Assemble some logic for a provided object.
    * @param obj an `Amenity` object;
    *            anticipating a `Terminal` object using this same definition
    * @param context hook to the local `Actor` system
    */
  def Setup(obj : Amenity, context : ActorContext) : Unit = {
    import akka.actor.{ActorRef, Props}
    if(obj.Actor == ActorRef.noSender) {
      obj.Actor = context.actorOf(Props(classOf[TerminalControl], obj), s"${obj.Definition.Name}_${obj.GUID.guid}")
    }
  }
}
