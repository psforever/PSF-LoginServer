// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

/**
  * The object must be able to recall on which of the defined game worlds (zones) that it exists on command.
  * The game world identifier string produced should be equivalent to a `Zone.Id` string for some equivalent `Zone` object.
  * The identifier "nowhere" is recommended as the default invalid location.
  * @see `InterstellarCluster`
  * @see `WorldEntity`
  * @see `Zone`
  */
trait ZoneAware {
  def Continent : String

  def Continent_=(zone : String) : String
}
