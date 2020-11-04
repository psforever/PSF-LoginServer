// Copyright (c) 2020 PSForever
package net.psforever.objects.avatar

import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.zones.FillLine

final case class Submerged(obj: PlanetSideServerObject, fluid: FillLine)

final case class Surfaced(obj: PlanetSideServerObject, fluid: FillLine)

final case class RecoveredFromSubmerging()
