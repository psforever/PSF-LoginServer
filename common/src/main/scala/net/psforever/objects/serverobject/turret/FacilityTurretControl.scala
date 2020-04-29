// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.Actor
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.{DefaultCancellable, GlobalDefinitions, Player, Tool}
import net.psforever.objects.equipment.{Ammo, JammableMountedWeapons}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.mount.MountableBehavior
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.DamageableWeaponTurret
import net.psforever.objects.serverobject.hackable.GenericHackables
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

  // Used for timing ammo recharge for vanu turrets in caves
  var weaponAmmoRechargeTimer = DefaultCancellable.obj

  def receive : Receive = checkBehavior
    .orElse(jammableBehavior)
    .orElse(mountBehavior)
    .orElse(dismountBehavior)
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse {
      case CommonMessages.Use(player, Some((item : Tool, upgradeValue : Int)))
        if player.Faction == turret.Faction &&
          item.Definition == GlobalDefinitions.nano_dispenser && item.AmmoType == Ammo.upgrade_canister &&
          item.Magazine > 0 && turret.Seats.values.forall(!_.isOccupied) =>
        TurretUpgrade.values.find(_.id == upgradeValue) match {
          case Some(upgrade) if turret.Upgrade != upgrade && turret.Definition.Weapons.values.flatMap( _.keySet).exists(_ == upgrade) =>
            sender ! CommonMessages.Progress(
              1.25f,
              WeaponTurrets.FinishUpgradingMannedTurret(turret, player, item, upgrade),
              GenericHackables.HackingTickAction(progressType = 2, player, turret, item.GUID)
            )
          case _ => ;
        }

      case FacilityTurret.WeaponDischarged() =>
        if(weaponAmmoRechargeTimer != DefaultCancellable.obj) {
          weaponAmmoRechargeTimer.cancel()
        }

        weaponAmmoRechargeTimer = context.system.scheduler.schedule(3 seconds, 200 milliseconds, self, FacilityTurret.RechargeAmmo())

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

        if(weapon.Magazine == weapon.MaxMagazine && weaponAmmoRechargeTimer != DefaultCancellable.obj) {
          weaponAmmoRechargeTimer.cancel()
          weaponAmmoRechargeTimer = DefaultCancellable.obj
        }

      case _ => ;
    }

  override protected def DestructionAwareness(target : Target, cause : ResolvedProjectile) : Unit = {
    super.DestructionAwareness(target, cause)
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
