// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.locks

import net.psforever.objects.serverobject.structures.AmenityDefinition

/**
  * The definition for any `IFFLock`.
  * Object Id 451 is a generic external lock.
  */
class IFFLockDefinition extends AmenityDefinition(451) {
  Name = "iff_lock"
}
