// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.ActorContext
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.types.PlanetSideEmpire

/**
  * A `LocalProjectile` is a server-side object designed to populate a fake shared space.
  * It is a placeholder intended to block out the existence of projectiles communicated from clients.
  * All clients reserve the same internal range of user-generated GUID's from 40100 to 40124, inclusive.
  * All clients recognize this same range independent of each other as "their own featureless projectiles."
  * @see `Zone.MakeReservedObjects`<br>
  *       `Projectile.BaseUID`<br>
  *       `Projectile.RangeUID`
  */
class LocalProjectile extends PlanetSideServerObject {
  def Faction = PlanetSideEmpire.NEUTRAL

  def Definition = LocalProjectile.local
}

object LocalProjectile {
  import net.psforever.objects.definition.ObjectDefinition
  def local = new ObjectDefinition(0) { Name = "projectile" }

  /**
    * Instantiate and configure a `LocalProjectile` object.
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `LocalProjectile` object
    */
  def Constructor(id: Int, context: ActorContext): LocalProjectile = {
    new LocalProjectile()
  }
}
