// Copyright (c) 2017 PSForever
package net.psforever.objects.terminals

import net.psforever.types.PlanetSideEmpire

//temporary location for these temporary messages
object TemporaryTerminalMessages {
  //TODO send original packets along with these messages
  final case class Convert(faction : PlanetSideEmpire.Value)
  final case class Hacked(faction : Option[PlanetSideEmpire.Value])
  final case class Damaged(dm : Int)
  final case class Repaired(rep : Int)
}
