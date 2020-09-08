// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.source

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.AvailabilityPolicy
import net.psforever.objects.guid.key.Monitor

private class Key extends Monitor {
  var policy: AvailabilityPolicy.Value = AvailabilityPolicy.Available

  var obj: Option[IdentifiableEntity]  = None
}
