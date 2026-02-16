// Copyright (c) 2026 PSForever
package net.psforever.services.base.message

import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

final case class SetEmpire(object_guid: PlanetSideGUID, faction: PlanetSideEmpire.Value) extends SelfRespondingEvent
