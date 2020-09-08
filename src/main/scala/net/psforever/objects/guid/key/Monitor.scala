// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.key

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.AvailabilityPolicy

trait Monitor {
  var policy: AvailabilityPolicy.Value

  var obj: Option[IdentifiableEntity]
}
