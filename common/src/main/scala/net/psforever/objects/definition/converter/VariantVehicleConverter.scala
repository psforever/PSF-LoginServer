// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Vehicle
import net.psforever.packet.game.objectcreate.{VariantVehicleData, VehicleFormat}

class VariantVehicleConverter extends VehicleConverter {
  override protected def SpecificFormatModifier: VehicleFormat.Value = VehicleFormat.Variant

  override protected def SpecificFormatData(obj: Vehicle) = {
    /*
    landed is 0
    flying is 7
     */
    Some(
      VariantVehicleData(
        if (obj.Definition.CanFly && obj.Flying) 7 else 0
      )
    )
  }
}
