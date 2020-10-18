// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.{Default, GlobalDefinitions, Player, Tool}
import net.psforever.objects.equipment.{Ammo, JammableMountedWeapons}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.mount.MountableBehavior
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.{Damageable, DamageableWeaponTurret}
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.repair.{AmenityAutoRepair, RepairableWeaponTurret}
import net.psforever.objects.serverobject.structures.PoweredAmenityControl
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles messages being dispatched to a specific `MannedTurret`.<br>
  * <br>
  * Mounted turrets have only slightly different entry requirements than a normal vehicle
  * because they encompass both faction-specific facility turrets
  * and faction-blind cavern sentry turrets.
  *
  * @param turret the `MannedTurret` object being governed
  */
class FacilityTurretControl(turret: FacilityTurret)
    extends PoweredAmenityControl
    with FactionAffinityBehavior.Check
    with MountableBehavior.TurretMount
    with MountableBehavior.Dismount
    with DamageableWeaponTurret
    with RepairableWeaponTurret
    with AmenityAutoRepair
    with JammableMountedWeapons {
  def FactionObject    = turret
  def MountableObject  = turret
  def JammableObject   = turret
  def DamageableObject = turret
  def RepairableObject = turret
  def AutoRepairObject = turret

  // Used for timing ammo recharge for vanu turrets in caves
  var weaponAmmoRechargeTimer = Default.Cancellable

  override def postStop(): Unit = {
    super.postStop()
    damageableWeaponTurretPostStop()
    stopAutoRepair()
  }

  def commonBehavior: Receive =
    checkBehavior
      .orElse(jammableBehavior)
      .orElse(dismountBehavior)
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse(autoRepairBehavior)

  def poweredStateLogic: Receive =
    commonBehavior
      .orElse(mountBehavior)
      .orElse {
        case CommonMessages.Use(player, Some((item: Tool, upgradeValue: Int)))
            if player.Faction == turret.Faction &&
              item.Definition == GlobalDefinitions.nano_dispenser && item.AmmoType == Ammo.upgrade_canister &&
              item.Magazine > 0 && turret.Seats.values.forall(!_.isOccupied) =>
          TurretUpgrade.values.find(_.id == upgradeValue) match {
            case Some(upgrade)
                if turret.Upgrade != upgrade && turret.Definition.Weapons.values
                  .flatMap(_.keySet)
                  .exists(_ == upgrade) =>
              sender() ! CommonMessages.Progress(
                1.25f,
                WeaponTurrets.FinishUpgradingMannedTurret(turret, player, item, upgrade),
                GenericHackables.HackingTickAction(progressType = 2, player, turret, item.GUID)
              )
            case _ => ;
          }

        case FacilityTurret.WeaponDischarged() =>
          if (weaponAmmoRechargeTimer != Default.Cancellable) {
            weaponAmmoRechargeTimer.cancel()
          }

          weaponAmmoRechargeTimer = context.system.scheduler.scheduleWithFixedDelay(
            3 seconds,
            200 milliseconds,
            self,
            FacilityTurret.RechargeAmmo()
          )

        case FacilityTurret.RechargeAmmo() =>
          val weapon = turret.ControlledWeapon(1).get.asInstanceOf[net.psforever.objects.Tool]
          // recharge when last shot fired 3s delay, +1, 200ms interval
          if (weapon.Magazine < weapon.MaxMagazine && System.nanoTime() - weapon.LastDischarge > 3000000000L) {
            weapon.Magazine += 1
            val seat = turret.Seat(0).get
            seat.Occupant match {
              case Some(player: Player) =>
                turret.Zone.LocalEvents ! LocalServiceMessage(
                  turret.Zone.id,
                  LocalAction.RechargeVehicleWeapon(player.GUID, turret.GUID, weapon.GUID)
                )
              case _ => ;
            }
          }

          if (weapon.Magazine == weapon.MaxMagazine && weaponAmmoRechargeTimer != Default.Cancellable) {
            weaponAmmoRechargeTimer.cancel()
            weaponAmmoRechargeTimer = Default.Cancellable
          }

        case _ => ;
      }

  def unpoweredStateLogic: Receive =
    commonBehavior
      .orElse {
        case _ => ;
      }

  override protected def DamageAwareness(target : Damageable.Target, cause : ResolvedProjectile, amount : Any) : Unit = {
    tryAutoRepair()
    super.DamageAwareness(target, cause, amount)
  }

  override protected def DestructionAwareness(target: Damageable.Target, cause: ResolvedProjectile): Unit = {
    tryAutoRepair()
    super.DestructionAwareness(target, cause)
    val zone   = target.Zone
    val zoneId = zone.id
    val events = zone.AvatarEvents
    val tguid  = target.GUID
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 50, 1))
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 51, 1))
  }

  override def PerformRepairs(target : Damageable.Target, amount : Int) : Int = {
    val newHealth = super.PerformRepairs(target, amount)
    if(newHealth == target.Definition.MaxHealth) {
      stopAutoRepair()
    }
    newHealth
  }

  override def Restoration(obj: Damageable.Target): Unit = {
    super.Restoration(obj)
    val zone   = turret.Zone
    val zoneId = zone.id
    val events = zone.AvatarEvents
    val tguid  = turret.GUID
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 50, 0))
    events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 51, 0))
  }

  override def tryAutoRepair() : Boolean = {
    isPowered && super.tryAutoRepair()
  }

  def powerTurnOffCallback(): Unit = {
    stopAutoRepair()
    //kick all occupants
    val guid = turret.GUID
    val zone = turret.Zone
    val zoneId = zone.id
    val events = zone.VehicleEvents
    turret.Seats.values.foreach(seat =>
      seat.Occupant match {
        case Some(player) =>
          seat.Occupant = None
          player.VehicleSeated = None
          if (player.HasGUID) {
            events ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(player.GUID, 4, false, guid))
          }
        case None => ;
      }
    )
  }

  def powerTurnOnCallback(): Unit = {
    tryAutoRepair()
  }
}
