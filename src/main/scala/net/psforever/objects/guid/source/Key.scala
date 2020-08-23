// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.source

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.AvailabilityPolicy
import net.psforever.objects.guid.key.Monitor

private class Key extends Monitor {
  private var policy: AvailabilityPolicy.Value = AvailabilityPolicy.Available
  private var obj: Option[IdentifiableEntity]  = None

  def Policy: AvailabilityPolicy.Value = policy

  def Policy_=(pol: AvailabilityPolicy.Value): AvailabilityPolicy.Value = {
    policy = pol
    Policy
  }

  def Object: Option[IdentifiableEntity] = obj

  def Object_=(objct: Option[IdentifiableEntity]): Option[IdentifiableEntity] = {
    obj = objct
    Object
  }
}
