//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair


/**
  * The "control" `Actor` mixin for repair-handling code for `Vehicle` objects.
  */
trait RepairableVehicle extends RepairableEntity {
  override def Restoration(obj: Repairable.Target): Unit = {
    obj.Health = 0
    obj.Destroyed = true
    /* no vanilla vehicles are capable of being restored from destruction */
    /* if you wanted to properly restore a destroyed vehicle, the quickest way is an ObjectCreateMessage packet */
    /* additionally, the vehicle deconstruction task must be cancelled */
  }
}
