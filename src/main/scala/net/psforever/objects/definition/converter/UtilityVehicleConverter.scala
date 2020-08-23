// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Vehicle
import net.psforever.packet.game.objectcreate.{UtilityVehicleData, VehicleFormat}

class UtilityVehicleConverter extends VehicleConverter {
  override protected def SpecificFormatModifier: VehicleFormat.Value = VehicleFormat.Utility

  override protected def SpecificFormatData(obj: Vehicle) = Some(UtilityVehicleData(0))
}
