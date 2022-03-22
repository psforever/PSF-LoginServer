// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player

/**
  * na
  */
class WeaponRechargeTerminalDefinition(objectId: Int)
  extends ProximityTerminalDefinition(objectId) {
  private var ammoAmount: Int = 1

  def AmmoAmount: Int = ammoAmount

  def AmmoAmount_=(amount: Int): Int = {
    ammoAmount = amount
    AmmoAmount
  }

  override def Request(player: Player, msg: Any): Terminal.Exchange = Terminal.NoDeal()
}
