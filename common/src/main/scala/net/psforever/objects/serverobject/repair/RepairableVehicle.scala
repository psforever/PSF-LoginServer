//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import net.psforever.objects.Tool

trait RepairableVehicle extends RepairableEntity {
  override def RepairValue(item : Tool) : Int = item.FireMode.Modifiers.Damage1

  override def Restoration(obj : Repairable.Target) : Unit = {
    obj.Health = 0
    //super.Restoration(obj)
    /* to properly restore a destroyed vehicle, an ObjectCreateMessage packet must be dispatched */
    /* additionally, the vehicle deconstruction task must be cancelled */
    /* no vanilla vehicles are capable of being restored from destruction */
  }
}
