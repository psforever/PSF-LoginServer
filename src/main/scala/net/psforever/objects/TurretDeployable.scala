// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, Props}
import net.psforever.objects.ce.{Deployable, DeployableBehavior, DeployedItem}
import net.psforever.objects.definition.DeployableDefinition
import net.psforever.objects.definition.converter.SmallTurretConverter
import net.psforever.objects.equipment.{JammableMountedWeapons, JammableUnit}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.DamageableWeaponTurret
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.repair.RepairableWeaponTurret
import net.psforever.objects.serverobject.turret.{TurretDefinition, WeaponTurret}
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.vital.{SimpleResolutions, StandardVehicleResistance}

class TurretDeployable(tdef: TurretDeployableDefinition)
    extends Deployable(tdef)
    with WeaponTurret
    with JammableUnit
    with Hackable {
  WeaponTurret.LoadDefinition(this)

  override def Definition = tdef
}

class TurretDeployableDefinition(private val objectId: Int)
    extends DeployableDefinition(objectId)
    with TurretDefinition {
  Name = "turret_deployable"
  Packet = new SmallTurretConverter
  DamageUsing = DamageCalculations.AgainstVehicle
  ResistUsing = StandardVehicleResistance
  Model = SimpleResolutions.calculate

  //override to clarify inheritance conflict
  override def MaxHealth: Int = super[DeployableDefinition].MaxHealth
  //override to clarify inheritance conflict
  override def MaxHealth_=(max: Int): Int = super[DeployableDefinition].MaxHealth_=(max)

  override def Initialize(obj: Deployable, context: ActorContext) = {
    obj.Actor = context.actorOf(Props(classOf[TurretControl], obj), PlanetSideServerObject.UniqueActorName(obj))
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
    with DeployableBehavior
    with FactionAffinityBehavior.Check
    with JammableMountedWeapons //note: jammable status is reported as vehicle events, not local events
    with MountableBehavior
    with DamageableWeaponTurret
    with RepairableWeaponTurret {
  def DeployableObject = turret
  def MountableObject  = turret
  def JammableObject   = turret
  def FactionObject    = turret
  def DamageableObject = turret
  def RepairableObject = turret

  override def postStop(): Unit = {
    super.postStop()
    deployableBehaviorPostStop()
    damageableWeaponTurretPostStop()
  }

  def receive: Receive =
    deployableBehavior
      .orElse(checkBehavior)
      .orElse(jammableBehavior)
      .orElse(mountBehavior)
      .orElse(dismountBehavior)
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse {
        case _ => ;
      }

  override protected def mountTest(
                                    obj: PlanetSideServerObject with Mountable,
                                    seatNumber: Int,
                                    player: Player): Boolean = {
    (!turret.Definition.FactionLocked || player.Faction == obj.Faction) && !obj.Destroyed
  }

  override protected def DestructionAwareness(target: Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    Deployables.AnnounceDestroyDeployable(turret, None)
  }
}
