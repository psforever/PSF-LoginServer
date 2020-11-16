// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.tube

import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.DamageableAmenity
import net.psforever.objects.serverobject.repair.{AmenityAutoRepair, Repairable, RepairableAmenity}
import net.psforever.objects.serverobject.structures.{Building, PoweredAmenityControl}

/**
  * An `Actor` that handles messages being dispatched to a specific `SpawnTube`.
  * @param tube the `SpawnTube` object being governed
  */
class SpawnTubeControl(tube: SpawnTube)
    extends PoweredAmenityControl
    with FactionAffinityBehavior.Check
    with DamageableAmenity
    with RepairableAmenity
    with AmenityAutoRepair {
  def FactionObject    = tube
  def DamageableObject = tube
  def RepairableObject = tube
  def AutoRepairObject = tube

  val commonBehavior: Receive = checkBehavior
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse(autoRepairBehavior)

  def poweredStateLogic: Receive =
    commonBehavior
      .orElse {
        case _ => ;
      }

  def unpoweredStateLogic: Receive =
    commonBehavior
      .orElse {
        case _ => ;
      }

  override protected def DamageAwareness(target : Target, cause : ResolvedProjectile, amount : Any) : Unit = {
    tryAutoRepair()
    super.DamageAwareness(target, cause, amount)
  }

  override protected def DestructionAwareness(target: Target, cause: ResolvedProjectile): Unit = {
    tryAutoRepair()
    super.DestructionAwareness(target, cause)
    tube.Owner match {
      case b: Building => b.Actor ! BuildingActor.AmenityStateChange(tube)
      case _           => ;
    }
  }

  override def PerformRepairs(target : Target, amount : Int) : Int = {
    val newHealth = super.PerformRepairs(target, amount)
    if(newHealth == target.Definition.MaxHealth) {
      stopAutoRepair()
    }
    newHealth
  }

  override def Restoration(obj: Repairable.Target): Unit = {
    super.Restoration(obj)
    tube.Owner match {
      case b: Building => b.Actor ! BuildingActor.AmenityStateChange(tube)
      case _           => ;
    }
  }

  override def tryAutoRepair() : Boolean = {
    isPowered && super.tryAutoRepair()
  }

  def powerTurnOffCallback(): Unit = {
    tube.offline = false
    stopAutoRepair()
    tube.Owner match {
      case b: Building => b.Actor ! BuildingActor.AmenityStateChange(tube)
      case _           => ;
    }
  }

  def powerTurnOnCallback(): Unit = {
    tube.offline = true
    tryAutoRepair()
    tube.Owner match {
      case b: Building => b.Actor ! BuildingActor.AmenityStateChange(tube)
      case _           => ;
    }
  }

  override def toString: String = tube.Definition.Name
}
