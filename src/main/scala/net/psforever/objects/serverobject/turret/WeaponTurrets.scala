// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.avatar.Certification
import net.psforever.objects.ce.Deployable
import net.psforever.objects.{Player, Tool, TurretDeployable}
import net.psforever.packet.game.{HackMessage, HackState, HackState1, HackState7, InventoryStateMessage}
import net.psforever.services.base.envelope.{BundledEnvelope, MessageEnvelope}
import net.psforever.services.base.message.{SendResponse, SetEmpire}
import net.psforever.services.local.LocalAction
import net.psforever.services.vehicle.support.{TurretEnvelope, TurretUpgrader}
import net.psforever.services.vehicle.VehicleAction
import net.psforever.types.PlanetSideGUID

object WeaponTurrets {
  private val log = org.log4s.getLogger("WeaponTurrets")

  /**
    * The process of upgrading a turret's weapon(s) is completed.
    * Pass the message onto the turret and onto the vehicle events system.
    * Additionally, force-deplete the ammunition count of the nano-dispenser used to perform the upgrade.
    *
    * @param target  the turret
    * @param tool    the nano-dispenser that was used to perform this upgrade
    * @param upgrade the new upgrade state
    */
  def FinishUpgradingMannedTurret(
      target: FacilityTurret,
      user: Player,
      tool: Tool,
      upgrade: TurretUpgrade.Value
  )(): Unit = {
    tool.Magazine = 0
    target.Zone.AvatarEvents ! MessageEnvelope(
      user.Name,
      SendResponse(InventoryStateMessage(tool.AmmoSlot.Box.GUID, tool.GUID, 0))
    )
    FinishUpgradingMannedTurret(target, upgrade)
  }

  /**
    * The process of upgrading a turret's weapon(s) is completed.
    * * Pass the message onto the turret and onto the vehicle events system.
    * @see `FacilityTurret`
    * @see `TurretUpgrade`
    * @see `TurretUpgrader.AddTask`
    * @see `TurretUpgrader.ClearSpecific`
    * @see `TurretMessage`
    * @param target the facility turret being upgraded
    * @param upgrade the upgrade being applied to the turret (usually, it's weapon system)
    */
  def FinishUpgradingMannedTurret(target: FacilityTurret, upgrade: TurretUpgrade.Value): Unit = {
    log.info(s"Manned wall turret weapon being converted to $upgrade")
    val zone   = target.Zone
    val events = zone.VehicleEvents
    events ! TurretEnvelope(TurretUpgrader.ClearSpecific(List(target), zone))
    events ! TurretEnvelope(TurretUpgrader.AddTask(target, zone, upgrade))
  }

  /**
   * Evaluate the progress of the user applying a tool to upgrade a facility turret.
   * This action is using the nano dispenser and requires separate handling from REK hacking.
   * Largely a copy/paste of the above, but some of it was removed as it doesn't work/apply with upgrading a turret.
   * @see `HackMessage`
   * @see `HackState`
   * @param progressType 1 - remote electronics kit hack (various ...);
   *                     2 - nano dispenser (upgrade canister) turret upgrade
   * @param tplayer the player performing the action
   * @param turret the object being affected
   * @param tool_guid the tool being used to affest the object
   * @param progress the current progress value
   * @return `true`, if the next cycle of progress should occur;
   *         `false`, otherwise
   */
  def TurretUpgradingTickAction(progressType: HackState1, tplayer: Player, turret: FacilityTurret, tool_guid: PlanetSideGUID)(
    progress: Float
  ): Boolean = {
    val (progressState, progressGrade) = if (progress <= 0L) {
      (HackState.Start, 0)
    } else if (progress >= 100L) {
      (HackState.Finished, 100)
    } else if (turret.Destroyed) {
      (HackState.Cancelled, 0)
    } else {
      turret.UpdateTurretUpgradeTime()
      (HackState.Ongoing, progress.toInt)
    }
    turret.Zone.AvatarEvents ! MessageEnvelope(
      tplayer.Name,
      SendResponse(
        HackMessage(progressType, turret.GUID, tplayer.GUID, progressGrade, -1f, progressState, HackState7.Unk8)
      )
    )
    progressState != HackState.Cancelled
  }

  def FinishHackingTurretDeployable(target: TurretDeployable, hacker: Player)(): Unit = {
    org.log4s.getLogger("TurretDeployable").info(s"${hacker.Name} has jacked a ${target.Definition.Name}")
    val zone = target.Zone
    val certs = hacker.avatar.certifications
    if (certs.contains(Certification.ExpertHacking) || certs.contains(Certification.ElectronicsExpert)) {
      // Forcefully dismount all seated occupants from the turret
      zone.VehicleEvents ! BundledEnvelope(target.Seats.values.flatMap { seat =>
        seat.occupant.collect {
          player: Player =>
            seat.unmount(player)
            player.VehicleSeated = None
            MessageEnvelope(
              zone.id,
              player.GUID,
              VehicleAction.KickPassenger(4, unk2 = false, target.GUID)
            )
        }
      })
      //hacker owns the deployable now
      target.OwnerGuid = None
      target.Actor ! Deployable.Ownership(hacker)
      //convert faction
      zone.AvatarEvents ! MessageEnvelope(
        zone.id,
        SetEmpire(target.GUID, hacker.Faction)
      )
      zone.LocalEvents ! MessageEnvelope(
        zone.id,
        hacker.GUID,
        LocalAction.TriggerSound(target.HackSound, target.Position, 30, 0.49803925f)
      )
    } else {
      //deconstruct
      target.Actor ! Deployable.Deconstruct()
    }
  }
}
