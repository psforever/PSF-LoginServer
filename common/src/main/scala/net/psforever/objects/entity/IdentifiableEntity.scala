// Copyright (c) 2017-2019 PSForever
package net.psforever.objects.entity

import net.psforever.types.{PlanetSideGUID, StalePlanetSideGUID}

/**
  * Represent any entity that must have its own valid global unique identifier (GUID) to be functional.<br>
  * <br>
  * The basic design philosophy of the workflow of a GUID at this stage is a deterministic state machine.
  * At the start, an `Exception` will be thrown while the default conditions of the accessor and mutator are maintained.
  * ("The ability to set a new valid GUID".)
  * Only a valid GUID may be set and, once it does, that changes the conditions of the accessor and mutator
  * to one where it will return the valid GUID and one where it will no longer accept a new GUID (valid or invalid).
  * That GUID will continue being the GUID reported by the object, even if another valid GUID tries to be set.
  * (No error or exception will be thrown.)
  * To set a new GUID, the current one must be invalidated with the appropriate function,
  * and this turns both the object and any object reference that can be acquired from the object "stale."
  * Doing this prior to setting the initial valid GUID is fruitless
  * as it restores the object to its default mutation option ("the ability to set a new valid GUID").
  * Access to the GUID is retained.
  * This can be done as many times as is necessary by following the same order of actions.<br>
  * <br>
  * The "staleness" of the object and the "staleness" of the GUID are related.
  * The condition in general indicates that the object has somehow become externally disconnected from its GUID reference
  * though the two still share something similar to their prior relationship internally.
  * Do not expect a "stale" GUID to refer to the same object through some mapping mechanism.
  * Do not expect a "stale" object to give you a GUID that will map back to itself.
  * @throws `NoGUIDException` if a GUID has not yet been assigned
  */
abstract class IdentifiableEntity extends Identifiable {

  /** storage for the GUID information; starts as a `StalePlanetSideGUID` */
  private var current: PlanetSideGUID = StalePlanetSideGUID(0)

  /** indicate the validity of the current GUID */
  private var guidValid: Boolean = false

  /** the current accessor; can be re-assigned */
  private var guidAccessor: IdentifiableEntity => PlanetSideGUID = IdentifiableEntity.getWhenNoGUID

  /** the current mutator; can be re-assigned */
  private var guidMutator: (IdentifiableEntity, PlanetSideGUID) => PlanetSideGUID = IdentifiableEntity.setWhenNoGUID

  def GUID: PlanetSideGUID = guidAccessor(this)

  /**
    * Always intercept `StalePlanetSideGUID` references when attempting to mutate the GUID value.
    * @param guid the valid GUID to assign
    * @throws `AssigningGUIDException` always
    * @return never returns
    */
  def GUID_=(guid: StalePlanetSideGUID): PlanetSideGUID = {
    throw new AssigningGUIDException(s"attempting to assign a stale global identifier to an entity", this, guid)
  }

  def GUID_=(guid: PlanetSideGUID): PlanetSideGUID = guidMutator(this, guid)

  /**
    * Flag when the object has no GUID (initial condition) or is considered stale.
    * @return whether the value of the GUID is a valid representation for this object
    */
  def HasGUID: Boolean = guidValid

  /**
    * Indicate that the current GUID is no longer a valid representation of the object.
    * Transforms whatever the current GUID is into a `StalePlanetSideGUID` entity with the same value.
    * Doing this restores the object to its default mutation option ("the ability to set a new valid GUID").
    * The current GUID will still be accessed as if it were valid, but it will be wrapped in the new stale object.
    */
  def Invalidate(): Unit = {
    guidValid = false
    current = StalePlanetSideGUID(current.guid)
    guidMutator = IdentifiableEntity.setWhenNoGUID
  }
}

object IdentifiableEntity {

  /**
    * Raise an `Exception` because the entity is never considered having a GUID to give.
    * @param o the any entity with a GUID
    * @throws `NoGUIDException` always
    * @return never returns
    */
  def getWhenNoGUID(o: IdentifiableEntity): PlanetSideGUID = {
    throw new NoGUIDException(s"did not initialize this object $o with a valid global identifier")
  }

  /**
    * Provide the entity with a valid GUID replacing an invalid GUID.
    * Modify the accessor and mutator function literals to ensure the entity will remain stable.
    * It will not be mutated by a new valid value without the existing valid value having to first be invalidated.
    * Its access is made standard.
    * @param o the any entity with a GUID
    * @param guid the valid GUID to assign
    * @return the GUID
    */
  def setWhenNoGUID(o: IdentifiableEntity, guid: PlanetSideGUID): PlanetSideGUID = {
    o.current = guid
    o.guidValid = true
    o.guidAccessor = getWhenValidGUID
    o.guidMutator = setWhenValidGUID
    guid
  }

  /**
    * The entity should have a valid GUID that can be provided.
    * @param o the entity
    * @return the entity's GUID
    */
  def getWhenValidGUID(o: IdentifiableEntity): PlanetSideGUID = o.current

  /**
    * The entity is in a condition where it can not be assigned the new valid GUID.
    * This state establishes itself after setting the very first valid GUID and
    * will persist until the entity is invalidated.
    * @param o the any entity with a GUID
    * @param guid the valid GUID to assign
    * @throws `AssigningGUIDException` always
    * @return never returns
    */
  def setWhenValidGUID(o: IdentifiableEntity, guid: PlanetSideGUID): PlanetSideGUID = {
    throw new AssigningGUIDException("attempting to set GUID to already valid object; try invalidating it", o, guid)
  }
}
