//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import net.psforever.objects.{Tool, Vehicle}

/**
  * The "control" `Actor` mixin for repair-handling code for `Vehicle` objects.
  */
trait RepairableVehicle extends RepairableEntity {
  def RepairableObject: Vehicle

  override def Restoration(obj: Repairable.Target): Unit = {
    obj.Health = 0
    obj.Destroyed = true
    /* no vanilla vehicles are capable of being restored from destruction */
    /* if you wanted to properly restore a destroyed vehicle, the quickest way is an ObjectCreateMessage packet */
    /* additionally, the vehicle deconstruction task must be cancelled */
  }

  override def RepairToolValue(item: Tool): Float = {
    super.RepairToolValue(item) +
    (if (RepairableObject.Definition.CanFly) {
      item.FireMode.Add.Damage2
    } else {
      item.FireMode.Add.Damage1
    })
  }
}
