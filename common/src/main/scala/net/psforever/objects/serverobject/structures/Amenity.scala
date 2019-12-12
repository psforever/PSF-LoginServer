// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.zones.Zone
import net.psforever.types.{PlanetSideEmpire, Vector3}

/**
  * Amenities are elements of the game that belong to other elements of the game.<br>
  * <br>
  * Normal `PlanetSideServerObject` entities (server objects) tend to have properties that are completely internalized.
  * An `Amenity` is a server object that maintains a fixed association with another server object.
  * This association strips away at the internalization and redirects a reference to some properties somewhere else.
  * An `Amenity` object belongs to its `Owner` object;
  * the `Amenity` objects look to its `Owner` object for some of its properties.
  * @see `FactionAffinity`
  */
abstract class Amenity extends PlanetSideServerObject {
  /** what other entity has authority over this amenity; usually either a building or a vehicle */
  private var owner : AmenityOwner = Building.NoBuilding
  /** if the entity exists at a specific position relative to the owner's position */
  private var offset : Option[Vector3] = None

  def Faction : PlanetSideEmpire.Value = Owner.Faction

  /**
    * Reference the object that is in direct association with (is superior to) this one.
    * @return the object associated as this object's "owner"
    */
  def Owner : AmenityOwner = owner

  /**
    * Set an object to have a direct association with (be superior to) this one.
    * @see `Amenity.AmenityTarget`
    * @param obj the object trying to become associated as this object's "owner"
    * @return the object associated as this object's "owner"
    */
  def Owner_=(obj : AmenityOwner) : AmenityOwner = {
    owner = obj
    Owner
  }

  def LocationOffset : Vector3 = offset.getOrElse(Vector3.Zero)

  def LocationOffset_=(off : Vector3) : Vector3 = LocationOffset_=(Some(off))

  def LocationOffset_=(off : Option[Vector3]) : Vector3 = {
    off match {
      case Some(Vector3.Zero) =>
        offset = None
      case _ =>
        offset = off
    }
    LocationOffset
  }

  override def Zone : Zone = Owner.Zone

  override def Zone_=(zone : Zone) = Owner.Zone

  override def Continent : String = Owner.Continent

  override def Continent_=(str : String) : String = Owner.Continent
}
