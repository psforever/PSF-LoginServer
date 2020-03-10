// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.generator

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.DamageableAmenity
import net.psforever.objects.serverobject.repair.{Repairable, RepairableAmenity}
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.serverobject.structures.Building.SendMapUpdate
import net.psforever.objects.vital.Vitality
import net.psforever.types.PlanetSideGeneratorState

/**
  * An `Actor` that handles messages being dispatched to a specific `Generator`.
  * @param gen the `Generator` object being governed
  */
class GeneratorControl(gen : Generator) extends Actor
  with FactionAffinityBehavior.Check
  with DamageableAmenity
  with RepairableAmenity {
  def FactionObject = gen
  def DamageableObject = gen
  def RepairableObject = gen

  def receive : Receive = checkBehavior
    //.orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse {
      case msg : Vitality.Damage =>
        val beforeDestroyed = gen.Destroyed
        takesDamage.apply(msg)
        if(gen.Destroyed != beforeDestroyed) {
          gen.Condition = PlanetSideGeneratorState.Critical
          gen.Owner.Actor ! SendMapUpdate(true)
        }

      case _ => ;
    }

  override def Restoration(obj : Repairable.Target) : Unit = {
    super.Restoration(obj)
    gen.Condition = PlanetSideGeneratorState.Normal
    gen.Owner match {
      case _ : Building => gen.Owner.Actor ! SendMapUpdate(true)
      case _ => ;
    }
  }
}
