// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Vehicle
import net.psforever.packet.game.objectcreate.{DestroyedVehicleData, PlacementData}

import scala.util.{Failure, Success, Try}

class DestroyedVehicleConverter extends ObjectCreateConverter[Vehicle]() {
  override def DetailedConstructorData(obj: Vehicle): Try[DestroyedVehicleData] =
    Failure(
      new Exception(
        "DestroyedVehicleConverter should not be used to generate detailed DestroyedVehicleData (nothing should)"
      )
    )

  override def ConstructorData(obj: Vehicle): Try[DestroyedVehicleData] = {
    if (obj.Health > 0) {
      Failure(new Exception("Vehicle used on DestroyedVehicleConverter has not yet been destroyed (Health == 0)"))
    } else {
      Success(DestroyedVehicleData(PlacementData(obj.Position, obj.Orientation)))
    }
  }
}

object DestroyedVehicleConverter {
  final val converter = new DestroyedVehicleConverter
}
