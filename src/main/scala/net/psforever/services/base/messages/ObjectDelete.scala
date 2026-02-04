// Copyright (c) 2026 PSForever
package net.psforever.services.base.messages

import net.psforever.services.base.SelfRespondingEvent
import net.psforever.types.PlanetSideGUID

final case class ObjectDelete(obj_guid: PlanetSideGUID, unk: Int = 0) extends SelfRespondingEvent
