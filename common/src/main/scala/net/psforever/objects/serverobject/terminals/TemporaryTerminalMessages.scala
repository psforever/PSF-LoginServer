// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.types.PlanetSideEmpire

//temporary location for these temporary messages
object TemporaryTerminalMessages {
  //TODO send original packets along with these messages
  final case class UseItem(player : Player)
  final case class Convert(faction : PlanetSideEmpire.Value)
  final case class Hack(player : Player)
  final case class ClearHack()
  final case class Damaged(dm : Int)
  final case class Repaired(rep : Int)
}
