// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Vehicle
import net.psforever.packet.game.objectcreate.{VariantVehicleData, VehicleFormat}

class VariantVehicleConverter extends VehicleConverter {
  override protected def SpecificFormatModifier : VehicleFormat.Value = VehicleFormat.Variant

  override protected def SpecificFormatData(obj : Vehicle) = Some(VariantVehicleData(0))
}
