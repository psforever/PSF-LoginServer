// Copyright (c) 2017 PSForever
package net.psforever.objects.entity

import net.psforever.packet.game.PlanetSideGUID

/**
  * Represent any entity that must have its own globally unique identifier (GUID) to be functional.<br>
  * <br>
  * "Testing" the object refers to the act of acquiring a reference to the GUID the object is using.
  * This object starts with a container class that represents a unprepared GUID state and raises an `Exception` when tested.
  * Setting a proper `PlanetSideGUID` replaces that container class with a container class that returns the GUID when tested.
  * The object can be invalidated, retaining the previous identifier number, but marking that object as being "stale."
  * "Staleness" is a property indicating whether or not the number can be used as a valid representation of the object.
  * @throws `NoGUIDException` if a GUID has not yet been assigned
  */
abstract class IdentifiableEntity extends Identifiable {
  /** indicate the validity of the current GUID */
  private var stale : Boolean = true
  /** storage for the active GUID */
  private val container : GUIDContainable = GUIDContainer()
  /** the handle for the active GUID; starts as exception-throwing */
  private var current : GUIDContainable = IdentifiableEntity.noGUIDContainer

  /**
    * The object will not originally having a valid GUID,
    * so "stale" will be used to expressed "not initialized."
    * After being set and then properly invalidated, then it will indicate proper staleness.
    * @return whether the value of the GUID is a valid representation for this object
    */
  def HasGUID : Boolean = !stale

  def GUID : PlanetSideGUID = current.GUID

  def GUID_=(guid : PlanetSideGUID) : PlanetSideGUID = {
    stale = false
    current = container
    current.GUID = guid
    GUID
  }

  /**
    * Set the staleness to indicate whether the GUID has ever been set
    * or that the set GUID is not a proper representation of the object.
    * It is always set to `true`.
    */
  def Invalidate() : Unit = {
    stale = true
  }
}

object IdentifiableEntity {
  private val noGUIDContainer : GUIDContainable = new NoGUIDContainer
}

/**
  * Mask the `Identifiable` `trait`.
  */
sealed trait GUIDContainable extends Identifiable

/**
  * Hidden container that represents an object that is not ready to be used by the game.
  */
private case class NoGUIDContainer() extends GUIDContainable {
  /**
    * Raise an `Exception` because we have no GUID to give.
    * @throws `NoGUIDException` always
    * @return never returns
    */
  def GUID : PlanetSideGUID = {
    throw NoGUIDException(s"object $this has not initialized a global identifier")
  }

  /**
    * Normally, this should never be called.
    * @param toGuid the globally unique identifier
    * @return never returns
    */
  def GUID_=(toGuid : PlanetSideGUID) : PlanetSideGUID = {
    throw NoGUIDException("can not initialize a global identifier with this object")
  }
}

/**
  * Hidden container that represents an object that has a working GUID and is ready to be used by the game.
  * @param guid the object's globally unique identifier;
  *             defaults to a GUID equal to 0
  */
private case class GUIDContainer(private var guid : PlanetSideGUID = PlanetSideGUID(0)) extends GUIDContainable {
  /**
    * Provide the GUID used to initialize this object.
    * @return the GUID
    */
  def GUID : PlanetSideGUID = guid

  /**
    * Exchange the previous GUID for a new one, re-using this container.
    * @param toGuid the globally unique identifier
    * @return the GUID
    */
  def GUID_=(toGuid : PlanetSideGUID) : PlanetSideGUID = {
    guid = toGuid
    GUID
  }
}
