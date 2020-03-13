//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import akka.actor.Actor.Receive
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.ZoneAware

trait Damageable {
  def DamageableObject : PlanetSideServerObject with Vitality

  final val takesDamage : Receive = TakesDamage

  protected def TakesDamage : Receive
}

object Damageable {
  type Target = PlanetSideServerObject with Vitality with FactionAffinity with ZoneAware

  def DestructionAwareness(target : Damageable.Target, cause : ResolvedProjectile) : Unit = {
    target.Destroyed = true
  }
}
