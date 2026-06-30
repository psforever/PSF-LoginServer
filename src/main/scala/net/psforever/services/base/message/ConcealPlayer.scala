// Copyright (c) 2026 PSForever
package net.psforever.services.base.message

import net.psforever.types.PlanetSideGUID

final case class ConcealPlayer(player_guid: PlanetSideGUID) extends SelfRespondingEvent
