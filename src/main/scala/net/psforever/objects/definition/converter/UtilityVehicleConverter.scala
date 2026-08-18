// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Vehicle
import net.psforever.packet.game.objectcreate.UtilityVehicleData
import net.psforever.types.VehicleFormat

object UtilityVehicleConverter extends BasicVehicleConverter() {
  override protected def SpecificFormatModifier: VehicleFormat = VehicleFormat.Utility

  override protected def SpecificFormatData(obj: Vehicle): Some[UtilityVehicleData] = Some(UtilityVehicleData(0))
}
