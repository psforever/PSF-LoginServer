// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.types.{PlanetSideEmpire, Vector3}

/**
  * Amenities are elements of the game that belong to other elements of the game.<br>
  * <br>
  * Normal `PlanetSideServerObject` entities (server objects) tend to have properties that are completely internalized.
  * An `Amenity` is a server object that maintains a fixed association with another server object.
  * This association strips away at the internalization and redirects a reference to some properties somewhere else.
  * An `Amenity` object belongs to its `Owner` object;
  * the `Amenity` objects looks to its `Owner` object for some of its properties.
  * @see `FactionAffinity`
  */
abstract class Amenity extends PlanetSideServerObject {
  /** what other entity has authority over this amenity; usually either a building or a vehicle */
  private var owner : PlanetSideServerObject = Building.NoBuilding
  /** if the entity exists at a specific position relative to the owner's position */
  private var offset : Option[Vector3] = None

  def Faction : PlanetSideEmpire.Value = Owner.Faction

  /**
    * Reference the object that is in direct association with (is superior to) this one.
    * @return the object associated as this object's "owner"
    */
  def Owner : PlanetSideServerObject = owner

  /**
    * Set an object to have a direct association with (be superior to) this one.
    * @see `Amenity.AmenityTarget`
    * @param obj the object trying to become associated as this object's "owner"
    * @tparam T a validation of the type of object that can be an owner
    * @return the object associated as this object's "owner"
    */
  def Owner_=[T : Amenity.AmenityTarget](obj : T) : PlanetSideServerObject = {
    owner = obj.asInstanceOf[PlanetSideServerObject]
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

  override def Continent = Owner match {
    case o : Building => o.Zone.Id
    case o : Vehicle => o.Continent
    case _ => super.Continent
  }
}

object Amenity {
  /**
    * A `trait` for validating the type of object that can be allowed to become an `Amenity` object's `Owner`.<br>
    * <br>
    * The `Owner` defaults to a type of `PlanetSideServerObject` in reference type;
    * but, that distinction is mainly to allow for a common ancestor with appropriate methods.
    * Only certain types of `PlanetSideServerObject` are formally allowed to be owners.
    * In execution, the `T` is the type of object that implicitly converts into an acceptable type of sub-object.
    * The companion object maintains the hardcoded conversions.
    * If such an implicit conversion does not exist, the assignment is unacceptable at compile time.
    * @tparam T the permitted type of object
    */
  sealed trait AmenityTarget[T]

  object AmenityTarget {
    import net.psforever.objects.Vehicle
    implicit object BuildingTarget extends AmenityTarget[Building] { }
    implicit object VehicleTarget extends AmenityTarget[Vehicle] { }
  }
}
