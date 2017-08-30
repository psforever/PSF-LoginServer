// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.actor

import net.psforever.objects.entity.IdentifiableEntity

/**
  * A message for when an object has been unregistered.
  * @param obj the object
  */
final case class UnregisterSuccess(obj : IdentifiableEntity)

