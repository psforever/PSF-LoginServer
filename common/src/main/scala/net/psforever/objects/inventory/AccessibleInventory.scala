// Copyright (c) 2017 PSForever
package net.psforever.objects.inventory

import net.psforever.objects.Player
import net.psforever.packet.game.PlanetSideGUID

trait AccessibleInventory {
  def Inventory : GridInventory

  def CanAccess(who : Player) : Boolean
  def Access(who : PlanetSideGUID) : Boolean
  def Unaccess : Boolean
}