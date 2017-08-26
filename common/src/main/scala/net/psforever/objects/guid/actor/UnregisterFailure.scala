// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.actor

import net.psforever.objects.entity.IdentifiableEntity

/**
  * A message for when an object has failed to be unregistered for some reason.
  * @param obj the object
  * @param ex the reason that the registration process failed
  */
final case class UnregisterFailure(obj : IdentifiableEntity, ex : Throwable)
