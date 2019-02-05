// Copyright (c) 2019 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.packet.game.ItemTransactionMessage

class ProximityTerminalDefinition(objectId : Int) extends TerminalDefinition(objectId) with ProximityDefinition {
  override def Request(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
}
