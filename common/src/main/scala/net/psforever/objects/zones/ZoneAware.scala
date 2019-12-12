// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import net.psforever.objects.zones.{ Zone => World }

/**
  * The entity must be able to recall on which of the defined game worlds (zones) that it exists on command.
  * The game world identifier string produced should be equivalent to a `Zone.Id` string for some equivalent `Zone` object.
  * The identifier "nowhere" is recommended as the default invalid location.
  * @see `InterstellarCluster`
  * @see `WorldEntity`
  * @see `Zone`
  */
trait ZoneAware {
  private var zoneRef : World = World.Nowhere
  /**
    * Normally, an entity is the resident of a `Zone` object and that is what it considers its "continent".
    * Since the entity may switch between `Zone` objects, however,
    * if the type of entity is allowed to do that,
    * it may become useful to allow the entity to identify as belonging to its future zone earlier than reference assignment.
    */
  private var continent : Option[String] = None

  def Zone : World = zoneRef

  /**
    * When assigning a new `Zone` object for the `Vehicle` object, eliminate
    * @param zone a reference to the `Zone` object
    * @return a reference to the `Zone` object
    */
  def Zone_=(zone : World) : World = {
    continent = None
    zoneRef = zone
    Zone
  }

  def Continent : String = continent.getOrElse(Zone.Id)

  /**
    * Give the entity a custom `Zone` identifier.
    * @param zoneId the custom identifier of the `Zone` object
    * @return the identifier of the `Zone` object
    */
  def Continent_=(zoneId : String) : String = {
    continent = Some(zoneId)
    Continent
  }
}
