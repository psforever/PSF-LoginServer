// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.key

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.AvailabilityPolicy

trait Monitor {
  def Policy: AvailabilityPolicy.Value

  def Object: Option[IdentifiableEntity]

  def Object_=(objct: Option[IdentifiableEntity]): Option[IdentifiableEntity]
}
