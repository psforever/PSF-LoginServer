// Copyright (c) 2026 PSForever
package net.psforever.services.base.messages

import net.psforever.services.base.SelfRespondingEvent
import net.psforever.types.PlanetSideGUID

final case class GenericObjectAction(object_guid: PlanetSideGUID, action_code: Int) extends SelfRespondingEvent
