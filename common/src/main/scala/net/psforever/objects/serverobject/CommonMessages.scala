// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject

import net.psforever.objects.{PlanetSideGameObject, Player}

//temporary location for these messages
object CommonMessages {
  final case class Use(player : Player, data : Option[Any] = None)
  final case class Unuse(player : Player, data : Option[Any] = None)
  final case class Hack(player : Player)
  final case class ClearHack()
}
