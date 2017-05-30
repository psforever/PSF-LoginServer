// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Vehicle
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.{ANTData, CommonFieldData, PlacementData}

import scala.util.{Success, Try}

class ANTConverter extends ObjectCreateConverter[Vehicle] {
  /* Vehicles do not have a conversion for `0x18` packet data. */

  override def ConstructorData(obj : Vehicle) : Try[ANTData] = {
    Success(
      ANTData(
        CommonFieldData(
          PlacementData(obj.Position, obj.Orientation,obj.Velocity),
          obj.Faction,
          0,
          if(obj.Owner.isDefined) { obj.Owner.get } else { PlanetSideGUID(0) } //this is the owner field, right?
        ),
        0,
        obj.Health,
        0,
        obj.Configuration
      )
    )
  }
}
