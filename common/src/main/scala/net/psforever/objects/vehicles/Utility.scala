// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.Vehicle
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire

import scala.annotation.switch

/**
  * A `Utility` represents an unknown but functional entity that is attached to a `Vehicle` and is not a weapon or a seat.
  * This is a placeholder solution until a better system is established.
  * @param objectId the object id that is associated with this sort of `Utility`
  * @param vehicle the `Vehicle` to which this `Utility` is attached
  */
class Utility(val objectId : Int, vehicle : Vehicle) extends IdentifiableEntity {
  private var active : Boolean = false

  /**
    * The faction association of this `Utility` is tied directly to the connected `Vehicle`.
    * @return the faction association
    */
  def Faction : PlanetSideEmpire.Value = {
    vehicle.Faction
  }

  /**
    * An "active" `Utility` can be used by players; an "inactive" one can not be used in its current state.
    * @return whether this `Utility` is active.
    */
  def ActiveState : Boolean = {
    this.active
  }

  /**
    * Change the "active" state of this `Utility`.
    * @param state the new active state
    * @return the current active state after being changed
    */
  def ActiveState_=(state : Boolean) : Boolean = {
    this.active = state
    state
  }

  /**
    * Override the string representation to provide additional information.
    * @return the string output
    */
  override def toString : String = {
    Utility.toString(this)
  }
}

object Utility {
  /**
    * An overloaded constructor.
    * @param objectId the object id the is associated with this sort of `Utility`
    * @param vehicle the `Vehicle` to which this `Utility` is attached
    * @return a `Utility` object
    */
  def apply(objectId : Int, vehicle : Vehicle) : Utility = {
    new Utility(objectId, vehicle)
  }

  /**
    * An overloaded constructor.
    * @param objectId the object id the is associated with this sort of `Utility`
    * @param vehicle the `Vehicle` to which this `Utility` is attached
    * @return a `Utility` object
    */
  def apply(guid : PlanetSideGUID, objectId : Int, vehicle : Vehicle) : Utility = {
    val obj = new Utility(objectId, vehicle)
    obj.GUID = guid
    obj
  }

  /**
    * Create one of a specific type of utilities.
    * @param objectId the object id that is associated with this sort of `Utility`
    * @param vehicle the `Vehicle` to which this `Utility` is attached
    * @return a permitted `Utility` object
    */
  def Select(objectId : Int, vehicle : Vehicle) : Utility = {
    (objectId : @switch) match {
      case 60 => //this is the object id of an ANT
        ANTResourceUtility(objectId, vehicle)

      case 49 | 519 | 613 | 614 => //ams parts
        Utility(objectId, vehicle)

      case _ =>
        throw new IllegalArgumentException(s"the requested objectID #$objectId is not accepted as a valid Utility")
    }
  }

  /**
    * Provide a fixed string representation.
    * @return the string output
    */
  def toString(obj : Utility) : String = {
    s"{utility-${obj.objectId}}"
  }
}
