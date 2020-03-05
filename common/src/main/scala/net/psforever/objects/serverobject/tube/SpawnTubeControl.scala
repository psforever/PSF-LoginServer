// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.tube

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.DamageableAmenity
import net.psforever.objects.serverobject.repair.{Repairable, RepairableAmenity}
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.serverobject.structures.Building.SendMapUpdate
import net.psforever.objects.vital.Vitality

/**
  * An `Actor` that handles messages being dispatched to a specific `SpawnTube`.
  * @param tube the `SpawnTube` object being governed
  */
class SpawnTubeControl(tube : SpawnTube) extends Actor
  with FactionAffinityBehavior.Check
  with DamageableAmenity
  with RepairableAmenity {
  def FactionObject = tube
  def DamageableObject = tube
  def RepairableObject = tube

  def receive : Receive = checkBehavior
    .orElse(canBeRepairedByNanoDispenser)
    .orElse {
      case msg : Vitality.Damage =>
        val destroyedBefore = tube.Destroyed
        takesDamage.apply(msg)
        if(tube.Destroyed != destroyedBefore) {
          tube.Owner.Actor ! SendMapUpdate(true)
        }

//      case msg : CommonMessages.Use =>
//        val destroyedBefore = tube.Destroyed
//        canBeRepairedByNanoDispenser.apply(msg)
//        if(tube.Destroyed != destroyedBefore) {
//          tube.Owner.Actor ! SendMapUpdate(true)
//        }

      case _ => ;
    }

  override def Restoration(obj : Repairable.Target) : Unit = {
    super.Restoration(obj)
    tube.Owner match {
      case _ : Building => tube.Owner.Actor ! SendMapUpdate(true)
      case _ => ;
    }
  }

  override def toString : String = tube.Definition.Name
}
