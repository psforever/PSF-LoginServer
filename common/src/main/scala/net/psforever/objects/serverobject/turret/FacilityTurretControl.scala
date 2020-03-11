// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.objects.equipment.JammableMountedWeapons
import net.psforever.objects.serverobject.mount.MountableBehavior
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.DamageableWeaponTurret
import net.psforever.objects.serverobject.repair.Repairable.Target
import net.psforever.objects.serverobject.repair.RepairableWeaponTurret
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


/**
  * An `Actor` that handles messages being dispatched to a specific `MannedTurret`.<br>
  * <br>
  * Mounted turrets have only slightly different entry requirements than a normal vehicle
  * because they encompass both faction-specific facility turrets
  * and faction-blind cavern sentry turrets.
  * @param turret the `MannedTurret` object being governed
  */
class FacilityTurretControl(turret : FacilityTurret) extends Actor
  with FactionAffinityBehavior.Check
  with MountableBehavior.TurretMount
  with MountableBehavior.Dismount
  with DamageableWeaponTurret
  with RepairableWeaponTurret
  with JammableMountedWeapons {
  def FactionObject = turret
  def MountableObject = turret
  def JammableObject = turret
  def DamageableObject = turret
  def RepairableObject = turret
  if(turret.Definition == GlobalDefinitions.vanu_sentry_turret) {
    // todo: Schedule this to start when weapon is discharged, not all the time
    context.system.scheduler.schedule(3 seconds, 200 milliseconds, self, FacilityTurret.RechargeAmmo())
  }

  def receive : Receive = checkBehavior
    .orElse(jammableBehavior)
    .orElse(mountBehavior)
    .orElse(dismountBehavior)
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse {
      case FacilityTurret.RechargeAmmo() =>
        val weapon = turret.ControlledWeapon(1).get.asInstanceOf[net.psforever.objects.Tool]
        // recharge when last shot fired 3s delay, +1, 200ms interval
        if(weapon.Magazine < weapon.MaxMagazine && System.nanoTime() - weapon.LastDischarge > 3000000000L) {
          weapon.Magazine += 1
          val seat = turret.Seat(0).get
          seat.Occupant match {
            case Some(player: Player) => turret.Zone.LocalEvents ! LocalServiceMessage(turret.Zone.Id, LocalAction.RechargeVehicleWeapon(player.GUID, turret.GUID, weapon.GUID))
            case _ => ;
          }
        }

      case _ => ;
    }

  override protected def Destruction(target : Target) : Unit = {
    super.Destruction(target)
    val zone = target.Zone
    val zoneId = zone.Id
    val events = zone.AvatarEvents
    val tguid = target.GUID
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 50, 1))
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 51, 1))
  }

  override def Restoration(obj : Target) : Unit = {
    super.Restoration(obj)
    val zone = turret.Zone
    val zoneId = zone.Id
    val events = zone.AvatarEvents
    val tguid = turret.GUID
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 50, 0))
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 51, 0))
  }
}
