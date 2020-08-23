// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

/**
  * An `Enumeration` of various access states for vehicle components, such as the seats and the trunk.
  * Organized to replicate the `PlanetsideAttributeMessage` value used for that given access level.
  */
object VehicleLockState extends Enumeration {
  type Type = Value

  val Locked = Value(0) //owner only
  val Group  = Value(1) //owner's squad/platoon only
  val Empire = Value(3) //owner's whole faction
}
