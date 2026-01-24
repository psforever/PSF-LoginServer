// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.Actor
import net.psforever.objects.equipment.JammableMountedWeapons
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.{Damageable, DamageableWeaponTurret}
import net.psforever.objects.serverobject.repair.RepairableWeaponTurret
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

trait TurretControl
  extends Actor
    with FactionAffinityBehavior.Check
    with DamageableWeaponTurret
    with RepairableWeaponTurret
    with JammableMountedWeapons { /* note: jammable status is reported as vehicle events, not local events */
  def TurretObject: PlanetSideServerObject with WeaponTurret

  override def postStop(): Unit = {
    super.postStop()
    damageableWeaponTurretPostStop()
  }

  def commonBehavior: Receive =
    checkBehavior
      .orElse(jammableBehavior)
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)

  /**
   * An override for `Restoration`, best for turrets.
   * @param obj the entity being restored
   */
  override def Restoration(obj: Damageable.Target): Unit = {
    super.Restoration(obj)
    val zone   = TurretObject.Zone
    val zoneId = zone.id
    val events = zone.AvatarEvents
    val tguid  = TurretObject.GUID
    events ! AvatarServiceMessage(zoneId, tguid, AvatarAction.PlanetsideAttributeToAll(50, 0))
    events ! AvatarServiceMessage(zoneId, tguid, AvatarAction.PlanetsideAttributeToAll(51, 0))
  }

  /**
   * An override for `DamageAwareness`, best for turrets.
   * @param target the entity being destroyed
   * @param cause historical information about the damage
   */
  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    val zone   = target.Zone
    val zoneId = zone.id
    val events = zone.AvatarEvents
    val tguid  = target.GUID
    CancelJammeredSound(target)
    CancelJammeredStatus(target)
    events ! AvatarServiceMessage(zoneId, tguid, AvatarAction.PlanetsideAttributeToAll(50, 1))
    events ! AvatarServiceMessage(zoneId, tguid, AvatarAction.PlanetsideAttributeToAll(51, 1))
  }
}
