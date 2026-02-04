// Copyright (c) 2026 PSForever
package net.psforever.services.base.messages

import net.psforever.services.base.SelfRespondingEvent
import net.psforever.types.PlanetSideGUID

final case class PlanetsideAttribute(
                                      target_guid: PlanetSideGUID,
                                      attribute_type: Int,
                                      attribute_value: Long
                                    ) extends SelfRespondingEvent
