// Copyright (c) 2017 PSForever
package net.psforever.objects.guid

/**
  * The availability of individual GUIDs is maintained by the given policy.
  */
object AvailabilityPolicy extends Enumeration {
  type Type = Value

  /**
    * An `AVAILABLE` GUID is ready and waiting to be `LEASED` for use.
    * A `LEASED` GUID has been issued and is currently being used.
    * A `RESTRICTED` GUID can never be freed.  It is allowed, however, to be assigned once as if it were `LEASED`.
    */
  val Available, Leased, Restricted = Value
}
