// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.equipment.{EquipmentSize, EquipmentSlot}

/**
  * A size-checked unit of storage (or mounting) for `Equipment`.
  * Unlike conventional `EquipmentSlot` space, this size of allowable `Equipment` is fixed.
  * @param size the permanent size of the `Equipment` allowed in this slot
  */
class OffhandEquipmentSlot(size : EquipmentSize.Value) extends EquipmentSlot {
  super.Size_=(size)

  /**
    * Not allowed to change the slot size manually.
    * @param assignSize the changed in capacity for this slot
    * @return the capacity for this slot
    */
  override def Size_=(assignSize : EquipmentSize.Value) : EquipmentSize.Value = Size
}

object OffhandEquipmentSlot {
  /**
    * An `EquipmentSlot` that can not be manipulated because its size is `Blocked` permanently.
    */
  final val BlockedSlot = new OffhandEquipmentSlot(EquipmentSize.Blocked)
}
