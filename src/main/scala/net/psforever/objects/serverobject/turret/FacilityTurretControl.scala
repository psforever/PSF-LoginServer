// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.{GlobalDefinitions, Player, Tool}
import net.psforever.objects.equipment.Ammo
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.repair.AmenityAutoRepair
import net.psforever.objects.serverobject.structures.PoweredAmenityControl
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminalAwareBehavior
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.packet.game.ChangeFireModeMessage
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

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
    with AmenityAutoRepair
    with MountableTurretControl
    with AutomatedTurretBehavior
    with CaptureTerminalAwareBehavior {
  def TurretObject: FacilityTurret               = turret
  def FactionObject: FacilityTurret              = turret
  def MountableObject: FacilityTurret            = turret
  def JammableObject: FacilityTurret             = turret
  def DamageableObject: FacilityTurret           = turret
  def RepairableObject: FacilityTurret           = turret
  def AutoRepairObject: FacilityTurret           = turret
  def AutomatedTurretObject: FacilityTurret      = turret
  def CaptureTerminalAwareObject: FacilityTurret = turret

  private var testToResetToDefaultFireMode: Boolean = false

  override def postStop(): Unit = {
    super.postStop()
    damageableWeaponTurretPostStop()
    automaticTurretPostStop()
    stopAutoRepair()
  }

  private val upgradeableTurret: Receive = {
    case CommonMessages.Use(player, Some((item: Tool, upgradeValue: Int)))
      if player.Faction == TurretObject.Faction &&
        item.Definition == GlobalDefinitions.nano_dispenser && item.AmmoType == Ammo.upgrade_canister &&
        item.Magazine > 0 && TurretObject.Seats.values.forall(!_.isOccupied) =>
      TurretUpgrade.values.find(_.id == upgradeValue).foreach {
        case upgrade
          if TurretObject.Upgrade != upgrade && TurretObject.Definition.WeaponPaths.values
            .flatMap(_.keySet)
            .exists(_ == upgrade) =>
          TurretObject.setMiddleOfUpgrade(true)
          sender() ! CommonMessages.Progress(
            1.25f,
            WeaponTurrets.FinishUpgradingMannedTurret(TurretObject, player, item, upgrade),
            GenericHackables.TurretUpgradingTickAction(progressType = 2, player, TurretObject, item.GUID)
          )
      }
  }

  override def commonBehavior: Receive = super.commonBehavior
    .orElse(automatedTurretBehavior)
    .orElse(captureTerminalAwareBehaviour)

  override def poweredStateLogic: Receive =
    commonBehavior
      .orElse(mountBehavior)
      .orElse(upgradeableTurret)
      .orElse {
        case _ => ()
      }

  override def unpoweredStateLogic: Receive =
    commonBehavior
      .orElse {
        case _ => ()
      }

  override protected def mountTest(
                                    obj: PlanetSideServerObject with Mountable,
                                    seatNumber: Int,
                                    player: Player): Boolean = {
    super.mountTest(obj, seatNumber, player) &&
      (!TurretObject.isUpgrading || System.currentTimeMillis() - GenericHackables.getTurretUpgradeTime >= 1500L) &&
      AutomaticOperation_=(state = false)
  }

  override protected def dismountTest(obj: Mountable with WorldEntity, seatNumber: Int, user: Player): Boolean = {
    super.dismountTest(obj, seatNumber, user) &&
      AutomaticOperation_=(autoTurretFunctionalityChecks)
  }

  override protected def DamageAwareness(target: Damageable.Target, cause: DamageResult, amount: Any) : Unit = {
    tryAutoRepair()
    if (AutomaticOperation) {
      if (TurretObject.Health < TurretObject.Definition.DamageDisablesAt) {
        AutomaticOperation = false
      } else if (AutomatedTurretObject.Definition.AutoFire.exists(_.retaliatoryDuration > 0)) {
        //turret retribution
        AutomatedTurretBehavior.getAttackerFromCause(target.Zone, cause).collect {
          case attacker if attacker.Faction != target.Faction =>
            engageNewDetectedTarget(attacker)
        }
      }
    }
    super.DamageAwareness(target, cause, amount)
  }

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    tryAutoRepair()
    AutomaticOperation = false
  }

  override def PerformRepairs(target : Damageable.Target, amount : Int) : Int = {
    val newHealth = super.PerformRepairs(target, amount)
    if (newHealth == target.Definition.MaxHealth) {
      stopAutoRepair()
    }
    newHealth
  }

  override def Restoration(obj: Damageable.Target): Unit = {
    super.Restoration(obj)
    AutomaticOperation = autoTurretFunctionalityChecks
  }

  override def tryAutoRepair() : Boolean = {
    isPowered && super.tryAutoRepair()
  }

  def powerTurnOffCallback(): Unit = {
    stopAutoRepair()
    AutomaticOperation = false
    //kick all occupants
    val guid = TurretObject.GUID
    val zone = TurretObject.Zone
    val zoneId = zone.id
    val events = zone.VehicleEvents
    TurretObject.Seats.values.foreach(seat =>
      seat.occupant match {
        case Some(player) =>
          seat.unmount(player)
          player.VehicleSeated = None
          if (player.HasGUID) {
            events ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(player.GUID, 4, unk2=false, guid))
          }
        case None => ()
      }
    )
  }

  def powerTurnOnCallback(): Unit = {
    tryAutoRepair()
    AutomaticOperation = autoTurretFunctionalityChecks
  }

  override def AutomaticOperation_=(state: Boolean): Boolean = {
    val result = super.AutomaticOperation_=(state)
    testToResetToDefaultFireMode = result && TurretObject.Definition.AutoFire.exists(_.revertToDefaultFireMode)
    result
  }

  private def autoTurretFunctionalityChecks: Boolean = {
    isPowered &&
      !JammableObject.Jammed &&
      TurretObject.Health > TurretObject.Definition.DamageDisablesAt &&
      !TurretObject.Seats.values.exists(_.isOccupied)
  }

  private def primaryWeaponFireModeOnly(): Unit = {
    if (testToResetToDefaultFireMode) {
      val zone = TurretObject.Zone
      val zoneid = zone.id
      val events = zone.VehicleEvents
      TurretObject.Weapons.values
        .flatMap(_.Equipment)
        .collect { case weapon: Tool if weapon.FireModeIndex > 0 =>
          weapon.FireModeIndex = 0
          events ! VehicleServiceMessage(
            zoneid,
            VehicleAction.SendResponse(Service.defaultPlayerGUID, ChangeFireModeMessage(weapon.GUID, 0))
          )
        }
    }
    testToResetToDefaultFireMode = false
  }

  override protected def trySelectNewTarget(): Option[AutomatedTurret.Target] = {
    if (autoTurretFunctionalityChecks) {
      primaryWeaponFireModeOnly()
      super.trySelectNewTarget()
    } else {
      None
    }
  }

  override def engageNewDetectedTarget(target: AutomatedTurret.Target): Unit = {
    if (autoTurretFunctionalityChecks) {
      primaryWeaponFireModeOnly()
      super.engageNewDetectedTarget(target)
    }
  }

  override def TryJammerEffectActivate(target: Any, cause: DamageResult): Unit = {
    val startsUnjammed = !JammableObject.Jammed
    super.TryJammerEffectActivate(target, cause)
    if (startsUnjammed && JammableObject.Jammed && AutomatedTurretObject.Definition.AutoFire.exists(_.retaliatoryDuration > 0)) {
      AutomaticOperation = false
      //look in direction of cause of jamming
      val zone = JammableObject.Zone
      AutomatedTurretBehavior.getAttackerFromCause(zone, cause).foreach {
        attacker =>
          val channel = zone.id
          val guid = AutomatedTurretObject.GUID
          AutomatedTurretBehavior.startTrackingTargets(zone, channel, guid, List(attacker.GUID))
          AutomatedTurretBehavior.stopTrackingTargets(zone, channel, guid) //TODO delay by a few milliseconds?
      }
    }
  }

  override def CancelJammeredStatus(target: Any): Unit = {
    val startsJammed = JammableObject.Jammed
    super.CancelJammeredStatus(target)
    startsJammed && AutomaticOperation_=(autoTurretFunctionalityChecks)
  }
}
