// Copyright (c) 2017-2026 PSForever
package net.psforever.services.galaxy

import net.psforever.services.base.GenericEventService

class GalaxyService
  extends GenericEventService[GalaxyServiceMessage, GalaxyServiceResponse](busName = "Galaxy")
