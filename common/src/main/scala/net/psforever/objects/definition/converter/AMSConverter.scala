// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Vehicle
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.{AMSData, CommonFieldData, ObjectClass, PlacementData}

import scala.util.{Success, Try}

class AMSConverter extends ObjectCreateConverter[Vehicle] {
  /* Vehicles do not have a conversion for `0x18` packet data. */

  override def ConstructorData(obj : Vehicle) : Try[AMSData] = {
    Success(
      AMSData(
        CommonFieldData(
          PlacementData(obj.Position, obj.Orientation, obj.Velocity),
          obj.Faction,
          0,
          if(obj.Owner.isDefined) { obj.Owner.get } else { PlanetSideGUID(0) } //this is the owner field, right?
        ),
        0,
        obj.Health,
        0,
        obj.Configuration,
        0,
        ReferenceUtility(obj, ObjectClass.matrix_terminalc),
        ReferenceUtility(obj, ObjectClass.ams_respawn_tube),
        ReferenceUtility(obj, ObjectClass.order_terminala),
        ReferenceUtility(obj, ObjectClass.order_terminalb)
      )
    )
  }

  /**
    * For an object with a list of utilities, find a specific kind of utility.
    * @param obj the game object
    * @param objectId the utility being sought
    * @return the global unique identifier of the utility
    */
  private def ReferenceUtility(obj : Vehicle, objectId : Int) : PlanetSideGUID = {
    obj.Utilities.find(util => util.objectId == objectId).head.GUID
  }
}
