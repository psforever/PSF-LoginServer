// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

/**
  * A size-checked unit of storage (or mounting) for `Equipment`.
  * Unlike inventory space, anything placed in "slot" space is expected to be visible to the game world in some fashion.
  */
class EquipmentSlot {
  private var size: EquipmentSize.Value = EquipmentSize.Blocked
  private var tool: Option[Equipment]   = None

  def Size: EquipmentSize.Value = size

  def Size_=(assignSize: EquipmentSize.Value): EquipmentSize.Value = {
    if (tool.isEmpty) {
      size = assignSize
    }
    Size
  }

  /**
    * Determine what `Equipment` is stowed in the given position.
    * @return the `Equipment` in this slot
    */
  def Equipment: Option[Equipment] = tool

  /**
    * Attempt to stow an item at the given position.
    * @param assignEquipment the change in `Equipment` for this slot
    * @return the `Equipment` in this slot
    */
  def Equipment_=(assignEquipment: Equipment): Option[Equipment] = {
    Equipment = Some(assignEquipment)
  }

  /**
    * Attempt to stow an item at the given position.
    * @param assignEquipment the change in `Equipment` for this slot
    * @return the `Equipment` in this slot
    */
  def Equipment_=(assignEquipment: Option[Equipment]): Option[Equipment] = {
    if (assignEquipment.isDefined) { //if new equipment is defined, don't put it in the slot if the slot is being used
      if (tool.isEmpty && EquipmentSize.isEqual(size, assignEquipment.get.Size)) {
        tool = assignEquipment
      }
    } else {
      tool = None
    }
    Equipment
  }
}

object EquipmentSlot {
  def apply(): EquipmentSlot = {
    new EquipmentSlot()
  }

  def apply(size: EquipmentSize.Value): EquipmentSlot = {
    val slot = new EquipmentSlot()
    slot.Size = size
    slot
  }
}
