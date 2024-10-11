// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.ActorContext
import net.psforever.actors.session.support.{SessionData, WeaponAndProjectileFunctions, WeaponAndProjectileOperations}
import net.psforever.login.WorldSession.{CountGrenades, FindEquipmentStock, FindToolThatUses, RemoveOldEquipmentFromInventory}
import net.psforever.objects.equipment.ChargeFireModeDefinition
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.{BoomerDeployable, BoomerTrigger, GlobalDefinitions, Tool}
import net.psforever.packet.game.{AIDamage, AvatarGrenadeStateMessage, ChangeAmmoMessage, ChangeFireModeMessage, ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, HitMessage, LashMessage, LongRangeProjectileInfoMessage, ProjectileStateMessage, QuantityUpdateMessage, ReloadMessage, SplashHitMessage, UplinkRequest, UplinkRequestType, UplinkResponse, WeaponDelayFireMessage, WeaponDryFireMessage, WeaponFireMessage, WeaponLazeTargetPositionMessage}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.PlanetSideGUID

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

  def handleUplinkRequest(packet: UplinkRequest): Unit = {
    val UplinkRequest(code, _, _) = packet
    val playerFaction = player.Faction
    //todo this is not correct
    code match {
      case UplinkRequestType.RevealFriendlies =>
        sendResponse(UplinkResponse(code.value, continent.LivePlayers.count(_.Faction == playerFaction)))
      case UplinkRequestType.RevealEnemies =>
        sendResponse(UplinkResponse(code.value, continent.LivePlayers.count(_.Faction != playerFaction)))
      case _ => ()
    }
  }

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
        fireStateStopWhenPlayer(tool, item_guid)
      case Some(tool: Tool) =>
        fireStateStopWhenMounted(tool, item_guid)
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

  /* support code */

  /**
   * After a weapon has finished shooting, determine if it needs to be sorted in a special way.
   * @param tool a weapon
   */
  private def FireCycleCleanup(tool: Tool): Unit = {
    //TODO replaced by more appropriate functionality in the future
    val tdef = tool.Definition
    if (GlobalDefinitions.isGrenade(tdef)) {
      val ammoType = tool.AmmoType
      FindEquipmentStock(player, FindToolThatUses(ammoType), 3, CountGrenades).reverse match { //do not search sidearm holsters
        case Nil =>
          log.info(s"${player.Name} has no more $ammoType grenades to throw")
          RemoveOldEquipmentFromInventory(player)(tool)

        case x :: xs => //this is similar to ReloadMessage
          val box = x.obj.asInstanceOf[Tool]
          val tailReloadValue: Int = if (xs.isEmpty) { 0 }
          else { xs.map(_.obj.asInstanceOf[Tool].Magazine).sum }
          val sumReloadValue: Int = box.Magazine + tailReloadValue
          val actualReloadValue = if (sumReloadValue <= 3) {
            RemoveOldEquipmentFromInventory(player)(x.obj)
            sumReloadValue
          } else {
            ops.modifyAmmunition(player)(box.AmmoSlot.Box, 3 - tailReloadValue)
            3
          }
          log.info(s"${player.Name} found $actualReloadValue more $ammoType grenades to throw")
          ops.modifyAmmunition(player)(
            tool.AmmoSlot.Box,
            -actualReloadValue
          ) //grenade item already in holster (negative because empty)
          xs.foreach(item => { RemoveOldEquipmentFromInventory(player)(item.obj) })
      }
    } else if (tdef == GlobalDefinitions.phoenix) {
      RemoveOldEquipmentFromInventory(player)(tool)
    }
  }

  private def fireStateStartPlayerMessages(itemGuid: PlanetSideGUID): Unit = {
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.ChangeFireState_Start(player.GUID, itemGuid)
    )
  }

  /*
  used by ChangeFireStateMessage_Stop handling
  */
  private def fireStateStopUpdateChargeAndCleanup(tool: Tool): Unit = {
    tool.FireMode match {
      case _: ChargeFireModeDefinition =>
        sendResponse(QuantityUpdateMessage(tool.AmmoSlot.Box.GUID, tool.Magazine))
      case _ => ()
    }
    if (tool.Magazine == 0) {
      FireCycleCleanup(tool)
    }
  }

  private def fireStateStopWhenPlayer(tool: Tool, itemGuid: PlanetSideGUID): Unit = {
    //the decimator does not send a ChangeFireState_Start on the last shot; heaven knows why
    //suppress the decimator's alternate fire mode, however
    if (
      tool.Definition == GlobalDefinitions.phoenix &&
        tool.Projectile != GlobalDefinitions.phoenix_missile_guided_projectile
    ) {
      fireStateStartPlayerMessages(itemGuid)
    }
    fireStateStopUpdateChargeAndCleanup(tool)
    ops.fireStateStopPlayerMessages(itemGuid)
  }

  private def fireStateStopWhenMounted(tool: Tool, itemGuid: PlanetSideGUID): Unit = {
    fireStateStopUpdateChargeAndCleanup(tool)
    ops.fireStateStopMountedMessages(itemGuid)
  }
}
