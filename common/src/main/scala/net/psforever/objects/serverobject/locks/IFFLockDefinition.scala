// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.locks

import net.psforever.objects.definition.ObjectDefinition

/**
  * The definition for any `IFFLock`.
  * Object Id 451 is a generic external lock.
  */
class IFFLockDefinition extends ObjectDefinition(451) {
  Name = "iff_lock"
}
