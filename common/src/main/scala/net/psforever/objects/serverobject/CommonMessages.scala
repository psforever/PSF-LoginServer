// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject

import net.psforever.objects.Player
import net.psforever.objects.serverobject.hackable.Hackable

//temporary location for these messages
object CommonMessages {
  final case class Use(player : Player, data : Option[Any] = None)
  final case class Unuse(player : Player, data : Option[Any] = None)
  final case class Hack(player : Player, obj : PlanetSideServerObject with Hackable, data : Option[Any] = None)
  final case class ClearHack()
}
