// Copyright (c) 2017 PSForever
package net.psforever.objects.guid


/**
  * The availability of individual global unique identifier (GUID) keys is maintained by the given policy.
  */
sealed trait AvailabilityPolicy

object AvailabilityPolicy {
  /**An `Available` key is ready and waiting to be `Leased` for use. */
  case object Available extends AvailabilityPolicy

  /** A `Leased` key has been issued and is currently being used for some purpose.*/
  case object Leased extends AvailabilityPolicy

  /** A `Dangling` key ia a unique sort of key that has been `Leased` but has not yet been applied for any specific purpose.
    * As a policy, it should be used as a status to check but should not be designated on any key. */
  case object Dangling extends AvailabilityPolicy
}
