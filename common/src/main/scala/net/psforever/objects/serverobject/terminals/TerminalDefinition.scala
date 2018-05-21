// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.objects.definition.converter.TerminalConverter
import net.psforever.packet.game.ItemTransactionMessage

/**
  * The basic definition for any `Terminal`.
  * @param objectId the object's identifier number
  */
abstract class TerminalDefinition(objectId : Int) extends net.psforever.objects.definition.ObjectDefinition(objectId) {
  Name = "terminal"
  Packet = new TerminalConverter

  /**
    * The unimplemented functionality for this `Terminal`'s `TransactionType.Buy` and `TransactionType.Learn` activity.
    */
  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange

  /**
    * The unimplemented functionality for this `Terminal`'s `TransactionType.Sell` activity.
    */
  def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()

  /**
    * The unimplemented functionality for this `Terminal`'s `TransactionType.Loadout` activity.
    */
  def Loadout(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
}
