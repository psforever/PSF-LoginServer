// Copyright (c) 2017 PSForever
package net.psforever.objects

/**
  * A `LocalProjectile` is a server-side object designed to populate a fake shared space.
  * It is a placeholder intended to block out the existence of projectiles communicated from clients.
  * All clients reserve the same internal range of user-generated GUID's from 40100 to 40124, inclusive.
  * All clients recognize this same range independent of each other as "their own featureless projectiles."
  * @see `Zone.MakeReservedObjects`<br>
  *       `Projectile.BaseUID`<br>
  *       `Projectile.RangeUID`
  */
class LocalProjectile extends PlanetSideGameObject {
  def Definition = LocalProjectile.local
}

object LocalProjectile {
  import net.psforever.objects.definition.ObjectDefinition
  def local = new ObjectDefinition(0) { Name = "projectile" }
}
