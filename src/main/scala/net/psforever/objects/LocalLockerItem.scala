// Copyright (c) 2020 PSForever
package net.psforever.objects

import akka.actor.ActorContext
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.types.PlanetSideEmpire

/**
  * A `LocalLockerItem` is a server-side object designed to populate a fake unshared space within a shared space.
  * It is a placeholder intended to block out the existence of locker objects that may or may not exist.
  * All clients reserve the same internal range of user-generated GUID's from 40150 to 40449, inclusive.
  * All clients recognize this same range independent of each other as "equipment in their own locker."
  * The GUID's in this locker-space can be reflected onto the zone GUID.
  * The item of that GUID may exist to the client.
  * The item of that GUID does not formally exist to the server, but it can be searched.
  * @see `Zone.MakeReservedObjects`
  */
class LocalLockerItem extends PlanetSideServerObject {
  def Faction = PlanetSideEmpire.NEUTRAL

  def Definition = LocalLockerItem.local
}

object LocalLockerItem {
  import net.psforever.objects.definition.ObjectDefinition
  def local = new ObjectDefinition(0) { Name = "locker-equipment" }

  /**
    * Instantiate and configure a `LocalProjectile` object.
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `LocalProjectile` object
    */
  def Constructor(id: Int, context: ActorContext): LocalLockerItem = {
    new LocalLockerItem()
  }
}
