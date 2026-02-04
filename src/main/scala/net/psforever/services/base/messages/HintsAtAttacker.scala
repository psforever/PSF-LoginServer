// Copyright (c) 2026 PSForever
package net.psforever.services.base.messages

import net.psforever.services.base.SelfRespondingEvent
import net.psforever.types.PlanetSideGUID

//analogue for HitHint
final case class HintsAtAttacker(source_guid: PlanetSideGUID) extends SelfRespondingEvent
