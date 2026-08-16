// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Vehicle
import net.psforever.packet.objectcreate.UtilityVehicleData
import net.psforever.types.VehicleFormat

class UtilityVehicleConverter extends VehicleConverter {
  override protected def SpecificFormatModifier: VehicleFormat = VehicleFormat.Utility

  override protected def SpecificFormatData(obj: Vehicle): Some[UtilityVehicleData] = Some(UtilityVehicleData(0))
}
