// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

/**
  * An `Enumeration` of various access states for vehicle components, such as the seats and the trunk.
  */
object VehicleLockState extends Enumeration {
  type Type = Value

  val Empire, //owner's whole faction
  Group, //owner's squad/platoon only
  Locked //owner only
  = Value
}
