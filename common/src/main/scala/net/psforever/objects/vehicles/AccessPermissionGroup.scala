// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

/**
  * An `Enumeration` of various permission groups that control access to aspects of a vehicle.<br>
  * - `Driver` is a seat that is always seat number 0.<br>
  * - `Gunner` is a seat that is not the `Driver` and controls a mounted weapon.<br>
  * - `Passenger` is a seat that is not the `Driver` and does not have control of a mounted weapon.<br>
  * - `Trunk` represnts access to the vehicle's internal storage space.<br>
  * Organized to replicate the `PlanetsideAttributeMessage` value used for that given access level.
  * In their respective `PlanetsideAttributeMessage` packet, the groups are indexed in the same order as 10 through 13.
  */
object AccessPermissionGroup extends Enumeration {
  type Type = Value

  val Driver, Gunner, Passenger, Trunk = Value
}
