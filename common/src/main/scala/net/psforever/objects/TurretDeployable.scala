// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.ce.{ComplexDeployable, Deployable, DeployedItem}
import net.psforever.objects.definition.{ComplexDeployableDefinition, SimpleDeployableDefinition}
import net.psforever.objects.definition.converter.SmallTurretConverter
import net.psforever.objects.equipment.{JammableMountedWeapons, JammableUnit}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.DamageableWeaponTurret
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.mount.MountableBehavior
import net.psforever.objects.serverobject.repair.RepairableWeaponTurret
import net.psforever.objects.serverobject.turret.{TurretDefinition, WeaponTurret}
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.{StandardResolutions, StandardVehicleResistance}

class TurretDeployable(tdef: TurretDeployableDefinition)
    extends ComplexDeployable(tdef)
    with WeaponTurret
    with JammableUnit
    with Hackable {
  WeaponTurret.LoadDefinition(this)

  def MountPoints: Map[Int, Int] = Definition.MountPoints.toMap

  override def Definition = tdef
}

class TurretDeployableDefinition(private val objectId: Int)
    extends ComplexDeployableDefinition(objectId)
    with TurretDefinition {
  Name = "turret_deployable"
  Packet = new SmallTurretConverter
  DamageUsing = DamageCalculations.AgainstVehicle
  ResistUsing = StandardVehicleResistance
  Model = StandardResolutions.FacilityTurrets

  //override to clarify inheritance conflict
  override def MaxHealth: Int = super[ComplexDeployableDefinition].MaxHealth
  //override to clarify inheritance conflict
  override def MaxHealth_=(max: Int): Int = super[ComplexDeployableDefinition].MaxHealth_=(max)

  override def Initialize(obj: PlanetSideServerObject with Deployable, context: ActorContext) = {
    obj.Actor = context.actorOf(Props(classOf[TurretControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }

  override def Uninitialize(obj: PlanetSideServerObject with Deployable, context: ActorContext) = {
    SimpleDeployableDefinition.SimpleUninitialize(obj, context)
  }
}

object TurretDeployableDefinition {
  def apply(dtype: DeployedItem.Value): TurretDeployableDefinition = {
    new TurretDeployableDefinition(dtype.id)
  }
}

/** control actors */

class TurretControl(turret: TurretDeployable)
    extends Actor
    with FactionAffinityBehavior.Check
    with JammableMountedWeapons //note: jammable status is reported as vehicle events, not local events
    with MountableBehavior.TurretMount
    with MountableBehavior.Dismount
    with DamageableWeaponTurret
    with RepairableWeaponTurret {
  def MountableObject  = turret
  def JammableObject   = turret
  def FactionObject    = turret
  def DamageableObject = turret
  def RepairableObject = turret

  override def postStop(): Unit = {
    super.postStop()
    damageableWeaponTurretPostStop()
  }

  def receive: Receive =
    checkBehavior
      .orElse(jammableBehavior)
      .orElse(mountBehavior)
      .orElse(dismountBehavior)
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse {
        case _ => ;
      }

  override protected def DestructionAwareness(target: Target, cause: ResolvedProjectile): Unit = {
    super.DestructionAwareness(target, cause)
    Deployables.AnnounceDestroyDeployable(turret, None)
  }
}
