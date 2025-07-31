// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.ActorContext
import net.psforever.actors.session.support.{SessionData, WeaponAndProjectileFunctions, WeaponAndProjectileOperations}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.{BoomerDeployable, BoomerTrigger, Tool}
import net.psforever.packet.game.{AIDamage, AvatarGrenadeStateMessage, ChangeAmmoMessage, ChangeFireModeMessage, ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, HitMessage, LashMessage, LongRangeProjectileInfoMessage, ProjectileStateMessage, ReloadMessage, SplashHitMessage, UplinkRequest, UplinkRequestType, UplinkResponse, WeaponDelayFireMessage, WeaponDryFireMessage, WeaponFireMessage, WeaponLazeTargetPositionMessage}

object WeaponAndProjectileLogic {
  def apply(ops: WeaponAndProjectileOperations): WeaponAndProjectileLogic = {
    new WeaponAndProjectileLogic(ops, ops.context)
  }
}

class WeaponAndProjectileLogic(val ops: WeaponAndProjectileOperations, implicit val context: ActorContext) extends WeaponAndProjectileFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  //private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  /* packets */

  def handleWeaponFire(pkt: WeaponFireMessage): Unit = { /* intentionally blank */ }

  def handleWeaponDelayFire(pkt: WeaponDelayFireMessage): Unit = { /* intentionally blank */ }

  def handleWeaponDryFire(pkt: WeaponDryFireMessage): Unit = { /* intentionally blank */ }

  def handleWeaponLazeTargetPosition(pkt: WeaponLazeTargetPositionMessage): Unit = { /* intentionally blank */ }

  def handleUplinkRequest(packet: UplinkRequest): Unit = { /* intentionally blank */ }

  def handleAvatarGrenadeState(pkt: AvatarGrenadeStateMessage): Unit = { /* intentionally blank */ }

  def handleChangeFireStateStart(pkt: ChangeFireStateMessage_Start): Unit = { /* intentionally blank */ }

  def handleChangeFireStateStop(pkt: ChangeFireStateMessage_Stop): Unit = {
    val ChangeFireStateMessage_Stop(item_guid) = pkt
    val now = System.currentTimeMillis()
    ops.prefire -= item_guid
    ops.shootingStop += item_guid -> now
    ops.shooting -= item_guid
    sessionLogic.findEquipment(item_guid) match {
      case Some(tool: Tool) if player.VehicleSeated.isEmpty =>
        ops.fireStateStopWhenPlayer(tool, item_guid)
      case Some(tool: Tool) =>
        ops.fireStateStopWhenMounted(tool, item_guid)
      case Some(trigger: BoomerTrigger) =>
        ops.fireStateStopPlayerMessages(item_guid)
        continent.GUID(trigger.Companion).collect {
          case boomer: BoomerDeployable =>
            boomer.Actor ! CommonMessages.Use(player, Some(trigger))
        }
      case Some(_) if player.VehicleSeated.isEmpty =>
        ops.fireStateStopPlayerMessages(item_guid)
      case Some(_) =>
        ops.fireStateStopMountedMessages(item_guid)
      case _ => ()
    }
    sessionLogic.general.progressBarUpdate.cancel()
    sessionLogic.general.progressBarValue = None
  }

  def handleReload(pkt: ReloadMessage): Unit = { /* intentionally blank */ }

  def handleChangeAmmo(pkt: ChangeAmmoMessage): Unit = { /* intentionally blank */ }

  def handleChangeFireMode(pkt: ChangeFireModeMessage): Unit = { /* intentionally blank */ }

  def handleProjectileState(pkt: ProjectileStateMessage): Unit = {
    ops.handleProjectileState(pkt)
  }

  def handleLongRangeProjectileState(pkt: LongRangeProjectileInfoMessage): Unit = { /* intentionally blank */ }

  def handleDirectHit(pkt: HitMessage): Unit = { /* intentionally blank */ }

  def handleSplashHit(pkt: SplashHitMessage): Unit = { /* intentionally blank */ }

  def handleLashHit(pkt: LashMessage): Unit = { /* intentionally blank */ }

  def handleAIDamage(pkt: AIDamage): Unit = { /* intentionally blank */ }
}
