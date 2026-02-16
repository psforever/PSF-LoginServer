// Copyright (c) 2026 PSForever
package net.psforever.services.base.message

import net.psforever.types.PlanetSideGUID

//analogue for HitHint
final case class HintsAtAttacker(source_guid: PlanetSideGUID) extends SelfRespondingEvent
