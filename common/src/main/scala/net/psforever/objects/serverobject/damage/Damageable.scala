//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.ZoneAware

trait Damageable {
  def DamageableObject : PlanetSideServerObject with Vitality

  def takesDamage : Receive

  protected def Destruction(target : Damageable.Target) : Unit = {
    target.Destroyed = true
  }
}

object Damageable {
  type Target = PlanetSideServerObject with Vitality with FactionAffinity with ZoneAware
}
