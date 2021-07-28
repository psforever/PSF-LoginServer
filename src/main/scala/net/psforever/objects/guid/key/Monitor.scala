// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.key

import net.psforever.objects.entity.IdentifiableEntity

trait Monitor {
  var policy: AvailabilityPolicy

  var obj: Option[IdentifiableEntity]
}
