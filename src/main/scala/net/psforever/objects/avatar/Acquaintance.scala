// Copyright (c) 2022 PSForever
package net.psforever.objects.avatar

import net.psforever.types.PlanetSideEmpire

case class Friend(
                   charId: Long = 0,
                   name: String = "",
                   faction: PlanetSideEmpire.Value,
                   online: Boolean = false
                 )

case class Ignored(
                    charId: Long = 0,
                    name: String = "",
                    online: Boolean = false
                  ) {
  val faction: PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
}
