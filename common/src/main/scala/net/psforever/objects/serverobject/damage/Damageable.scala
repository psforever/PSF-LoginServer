//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.vital.Vitality

trait Damageable {
  def DamageableObject : PlanetSideServerObject with Vitality

  def takesDamage : Receive
}
