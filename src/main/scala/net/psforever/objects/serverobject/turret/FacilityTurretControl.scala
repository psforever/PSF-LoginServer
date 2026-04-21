// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.{GlobalDefinitions, Player, Tool}
import net.psforever.objects.equipment.Ammo
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.repair.AmenityAutoRepair
import net.psforever.objects.serverobject.structures.{Building, PoweredAmenityControl}
import net.psforever.objects.serverobject.terminals.capture.{CaptureTerminal, CaptureTerminalAwareBehavior}
import net.psforever.objects.serverobject.turret.auto.AutomatedTurret.Target
import net.psforever.objects.serverobject.turret.auto.{AffectedByAutomaticTurretFire, AutomatedTurret, AutomatedTurretBehavior}
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.packet.game.{ChangeFireModeMessage, HackState1}
import net.psforever.services.Service
import net.psforever.services.vehicle.support.TurretUpgrader
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{BailType, PlanetSideEmpire, PlanetSideGUID}

/**
 * A control agency that handles messages being dispatched to a specific `FacilityTurret`.
 * These turrets are attached specifically to surface-level facilities and field towers.
 * @param turret the `FacilityTurret` object being governed
 */
class FacilityTurretControl(turret: FacilityTurret)
  extends PoweredAmenityControl
    with AmenityAutoRepair
    with MountableTurretControl
    with AutomatedTurretBehavior
    with AffectedByAutomaticTurretFire
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
  def AffectedObject: FacilityTurret             = turret

  private var testToResetToDefaultFireMode: Boolean = false

  AutomaticOperation = true

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
          AutomaticOperation = false
          sender() ! CommonMessages.Progress(
            1.25f,
            WeaponTurrets.FinishUpgradingMannedTurret(TurretObject, player, item, upgrade),
            WeaponTurrets.TurretUpgradingTickAction(HackState1.Unk2, player, TurretObject, item.GUID)
          )
      }
    case TurretUpgrader.UpgradeCompleted(_) =>
      CurrentTargetLastShotReported = System.currentTimeMillis() + 2000L
      AutomaticOperation = true
  }

  override def commonBehavior: Receive = super.commonBehavior
    .orElse(automatedTurretBehavior)
    .orElse(takeAutomatedDamage)
    .orElse(autoRepairBehavior)
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
      (!TurretObject.isUpgrading || System.currentTimeMillis() - TurretObject.CheckTurretUpgradeTime >= 1500L)
  }

  override protected def tryMount(obj: PlanetSideServerObject with Mountable, seatNumber: Int, player: Player): Boolean = {
    val originalAutoState = AutomaticOperation
    AutomaticOperation = false //turn off
    if (AutomaticOperationPossible && JammableObject.Jammed) {
      val zone = TurretObject.Zone
      AutomatedTurretBehavior.stopTracking(zone, zone.id, TurretObject.GUID) //can not recover lost jamming aggro
    }
    if (!super.tryMount(obj, seatNumber, player)) {
      AutomaticOperation = originalAutoState //revert
      false
    } else {
      true
    }
  }

  override protected def tryDismount(obj: Mountable, seatNumber: Int, player: Player, bailType: BailType.Value): Boolean = {
    AutomaticOperation = AutomaticOperationFunctionalityChecksExceptMounting //turn on, if can turn on
    if (!super.tryDismount(obj, seatNumber, player, bailType)) {
      AutomaticOperation = false //revert
      false
    } else {
      CurrentTargetLastShotReported = System.currentTimeMillis() + 4000L
      true
    }
  }

  override protected def DamageAwareness(target: Damageable.Target, cause: DamageResult, amount: Any) : Unit = {
    tryAutoRepair()
    if (AutomaticOperation) {
      if (TurretObject.Health < TurretObject.Definition.DamageDisablesAt) {
        AutomaticOperation = false
      } else {
        amount match {
          case 0 => ()
          case _ => attemptRetaliation(target, cause)
        }
      }
    }
    super.DamageAwareness(target, cause, amount)
  }

  override protected def DestructionAwareness(target: Damageable.Target, cause: DamageResult): Unit = {
    super.DestructionAwareness(target, cause)
    tryAutoRepair()
    AutomaticOperation = false
    selfReportingCleanUp()
  }

  override def PerformRepairs(target : Damageable.Target, amount : Int) : Int = {
    val newHealth = super.PerformRepairs(target, amount)
    if (!AutomaticOperation && newHealth > target.Definition.DamageDisablesAt) {
      AutomaticOperation = true
    }
    if (newHealth == target.Definition.MaxHealth) {
      stopAutoRepair()
    }
    newHealth
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
    AutomaticOperation = true
  }

  override def AutomaticOperation_=(state: Boolean): Boolean = {
    val result = super.AutomaticOperation_=(state)
    testToResetToDefaultFireMode = result && TurretObject.Definition.AutoFire.exists(_.revertToDefaultFireMode)
    result
  }

  override protected def AutomaticOperationFunctionalityChecks: Boolean = {
    AutomaticOperationFunctionalityChecksExceptMounting &&
      !TurretObject.Seats.values.exists(_.isOccupied)
  }

  private def AutomaticOperationFunctionalityChecksExceptMounting: Boolean = {
    AutomaticOperationFunctionalityChecksExceptMountingAndHacking &&
      (TurretObject.Owner match {
        case b: Building => !b.CaptureTerminalIsHacked
        case _ => false
      })
  }

  private def AutomaticOperationFunctionalityChecksExceptMountingAndHacking: Boolean = {
    super.AutomaticOperationFunctionalityChecks &&
      isPowered &&
      TurretObject.Owner.Faction != PlanetSideEmpire.NEUTRAL &&
      !JammableObject.Jammed &&
      TurretObject.Health >= TurretObject.Definition.DamageDisablesAt &&
      !TurretObject.isUpgrading
  }

  override def AutomaticOperationPossible: Boolean = {
    super.AutomaticOperationPossible &&
      (turret.Owner match {
        case b: Building if b.CaptureTerminal.isEmpty => false
        case b: Building => !b.CaptureTerminal.exists(_.Definition == GlobalDefinitions.secondary_capture)
        case _ => false
      })
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
    primaryWeaponFireModeOnly()
    super.trySelectNewTarget()
  }

  protected def engageNewDetectedTarget(
                                         target: Target,
                                         channel: String,
                                         turretGuid: PlanetSideGUID,
                                         weaponGuid: PlanetSideGUID
                                       ): Unit = {
    val zone = target.Zone
    primaryWeaponFireModeOnly()
    AutomatedTurretBehavior.startTracking(zone, channel, turretGuid, List(target.GUID))
    AutomatedTurretBehavior.startShooting(zone, channel, weaponGuid)
  }

  protected def noLongerEngageTarget(
                                      target: Target,
                                      channel: String,
                                      turretGuid: PlanetSideGUID,
                                      weaponGuid: PlanetSideGUID
                                    ): Option[Target] = {
    val zone = target.Zone
    AutomatedTurretBehavior.stopTracking(zone, channel, turretGuid)
    AutomatedTurretBehavior.stopShooting(zone, channel, weaponGuid)
    None
  }

  protected def testNewDetected(
                                 target: Target,
                                 channel: String,
                                 turretGuid: PlanetSideGUID,
                                 weaponGuid: PlanetSideGUID
                               ): Unit = {
    val zone = target.Zone
    AutomatedTurretBehavior.startTracking(zone, channel, turretGuid, List(target.GUID))
    AutomatedTurretBehavior.startShooting(zone, channel, weaponGuid)
    AutomatedTurretBehavior.stopShooting(zone, channel, weaponGuid)
    AutomatedTurretBehavior.stopTracking(zone, channel, turretGuid)
  }

  protected def testKnownDetected(
                                   target: Target,
                                   channel: String,
                                   turretGuid: PlanetSideGUID,
                                   weaponGuid: PlanetSideGUID
                                 ): Unit = {
    val zone = target.Zone
    AutomatedTurretBehavior.startTracking(zone, channel, turretGuid, List(target.GUID))
    AutomatedTurretBehavior.startShooting(zone, channel, weaponGuid)
    AutomatedTurretBehavior.stopShooting(zone, channel, weaponGuid)
    AutomatedTurretBehavior.stopTracking(zone, channel, turretGuid)
  }

  override def TryJammerEffectActivate(target: Any, cause: DamageResult): Unit = {
    super.TryJammerEffectActivate(target, cause)
    if (AutomaticOperationPossible && AutomaticOperation && JammableObject.Jammed) {
      AutomaticOperation = false
      if (!MountableObject.Seats.values.exists(_.isOccupied) && AutomatedTurretObject.Definition.AutoFire.exists(_.retaliatoryDelay > 0)) {
        //look in direction of cause of jamming
        val zone = JammableObject.Zone
        AutomatedTurretBehavior.getAttackVectorFromCause(zone, cause).foreach { attacker =>
          AutomatedTurretBehavior.startTracking(zone, zone.id, JammableObject.GUID, List(attacker.GUID))
        }
      }
    }
  }

  override def CancelJammeredStatus(target: Any): Unit = {
    val startsJammed = JammableObject.Jammed
    super.CancelJammeredStatus(target)
    if (startsJammed && AutomaticOperation_=(state = true)) {
      val zone = TurretObject.Zone
      AutomatedTurretBehavior.stopTracking(zone, zone.id, TurretObject.GUID)
    }
  }

  override protected def captureTerminalIsResecured(terminal: CaptureTerminal): Unit = {
    captureTerminalChanges(terminal, super.captureTerminalIsResecured, actionDelays = 2000L)
  }

  override protected def captureTerminalIsHacked(terminal: CaptureTerminal): Unit = {
    super.captureTerminalIsHacked(terminal)
    // Remove seated occupants
    val guid = turret.GUID
    val zone = turret.Zone
    val zoneId = zone.id
    val events = zone.VehicleEvents
    turret.Seats.values.zipWithIndex.foreach {
      case (seat, seat_num) =>
        seat.occupant.collect {
          case player =>
            seat.unmount(player)
            player.VehicleSeated = None
            events ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(player.GUID, seat_num, unk2=true, guid))
        }
    }
    captureTerminalChanges(terminal, super.captureTerminalIsHacked, actionDelays = 3000L)
  }

  private def captureTerminalChanges(
                                      terminal: CaptureTerminal,
                                      changeFunc: CaptureTerminal=>Unit,
                                      actionDelays: Long
                                    ): Unit = {
    AutomaticOperation = false
    changeFunc(terminal)
    if (AutomaticOperationFunctionalityChecks) {
      CurrentTargetLastShotReported = System.currentTimeMillis() + actionDelays
      AutomaticOperation = true
    }
  }
}
