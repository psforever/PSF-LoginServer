// Copyright (c) 2021 PSForever
package net.psforever.objects.locker

import net.psforever.objects.definition.EquipmentDefinition
import net.psforever.objects.definition.converter.LockerContainerConverter
import net.psforever.objects.equipment.EquipmentSize

class LockerContainerDefinition extends EquipmentDefinition(objectId = 456) {
  Name = "locker_container"
  Size = EquipmentSize.Inventory
  Packet = LockerContainerDefinition.converter
  registerAs = "lockers"
}

object LockerContainerDefinition {
  val converter = new LockerContainerConverter()
}
