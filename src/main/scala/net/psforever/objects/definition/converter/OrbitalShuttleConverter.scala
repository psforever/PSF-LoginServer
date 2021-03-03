// Copyright (c) 2021 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Vehicle
import net.psforever.packet.game.objectcreate._

import scala.util.{Failure, Success, Try}

class OrbitalShuttleConverter extends ObjectCreateConverter[Vehicle]() {
  override def ConstructorData(obj: Vehicle): Try[OrbitalShuttleData] = {
//    if (obj.MountedIn.nonEmpty) {
//      Success(OrbitalShuttleData(obj.Faction, None))
//    } else {
      Success(OrbitalShuttleData(obj.Faction, Some(PlacementData(obj.Position, obj.Orientation))))
//    }
  }

  override def DetailedConstructorData(obj: Vehicle): Try[OrbitalShuttleData] =
    Failure(new Exception("OrbitalShuttleConverter should not be used to generate detailed DroppodData (nothing should)"))
}
