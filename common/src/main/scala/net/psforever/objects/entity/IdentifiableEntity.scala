// Copyright (c) 2017 PSForever
package net.psforever.objects.entity

import net.psforever.packet.game.PlanetSideGUID

/**
  * Represent any entity that must have its own globally unique identifier (GUID) to be functional.<br>
  * <br>
  * "Testing" the object refers to the act of acquiring a reference to the GUID the object is using.
  * This object starts with a container class that represents a unprepared GUID state and raises an `Exception` when tested.
  * Setting a proper `PlanetSideGUID` replaces that container class with a container class that returns the GUID when tested.
  * The object can be invalidated, restoring the previous `Exception`-raising condition.
  * @throws `NoGUIDException` if there is no GUID to give
  */
abstract class IdentifiableEntity extends Identifiable {
  private val container : GUIDContainable = GUIDContainer()
  private var current : GUIDContainable = IdentifiableEntity.noGUIDContainer

  def HasGUID : Boolean = current ne IdentifiableEntity.noGUIDContainer

  def GUID : PlanetSideGUID = current.GUID

  def GUID_=(guid : PlanetSideGUID) : PlanetSideGUID = {
    current = container
    current.GUID = guid
    GUID
  }

  def Invalidate() : Unit = {
    current = IdentifiableEntity.noGUIDContainer
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
    throw NoGUIDException("object has not initialized a global identifier")
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
