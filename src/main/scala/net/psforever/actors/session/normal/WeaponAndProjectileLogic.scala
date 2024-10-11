// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.ActorContext
//import akka.actor.typed
//import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{SessionData, WeaponAndProjectileFunctions, WeaponAndProjectileOperations}
import net.psforever.login.WorldSession.{CountGrenades, FindEquipmentStock, FindToolThatUses, RemoveOldEquipmentFromInventory}
import net.psforever.objects.equipment.ChargeFireModeDefinition
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.{BoomerDeployable, BoomerTrigger, GlobalDefinitions, Player, SpecialEmp, Tool, Tools, Vehicle}
import net.psforever.objects.serverobject.turret.{FacilityTurret, VanuSentry}
import net.psforever.objects.vital.base.{DamageResolution, DamageType}
import net.psforever.objects.zones.{Zone, ZoneProjectile}
import net.psforever.packet.game.{AIDamage, AvatarGrenadeStateMessage, ChangeAmmoMessage, ChangeFireModeMessage, ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, HitMessage, LashMessage, LongRangeProjectileInfoMessage, ProjectileStateMessage, QuantityUpdateMessage, ReloadMessage, SplashHitMessage, UplinkRequest, WeaponDelayFireMessage, WeaponDryFireMessage, WeaponFireMessage, WeaponLazeTargetPositionMessage}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{PlanetSideGUID, Vector3}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object WeaponAndProjectileLogic {
  def apply(ops: WeaponAndProjectileOperations): WeaponAndProjectileLogic = {
    new WeaponAndProjectileLogic(ops, ops.context)
  }
}

class WeaponAndProjectileLogic(val ops: WeaponAndProjectileOperations, implicit val context: ActorContext) extends WeaponAndProjectileFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  //private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  /* packets */

  def handleWeaponFire(pkt: WeaponFireMessage): Unit = {
    ops.handleWeaponFireOperations(pkt)
  }

  def handleWeaponDelayFire(pkt: WeaponDelayFireMessage): Unit = {
    val WeaponDelayFireMessage(_, _) = pkt
    log.info(s"${player.Name} - $pkt")
  }

  def handleWeaponDryFire(pkt: WeaponDryFireMessage): Unit = {
    ops.handleWeaponDryFire(pkt)
  }

  def handleWeaponLazeTargetPosition(pkt: WeaponLazeTargetPositionMessage): Unit = {
    val WeaponLazeTargetPositionMessage(_, _, _) = pkt
    //do not need to handle the progress bar animation/state on the server
    //laze waypoint is requested by client upon completion (see SquadWaypointRequest)
    val purpose = if (sessionLogic.squad.squad_supplement_id > 0) {
      s" for ${player.Sex.possessive} squad (#${sessionLogic.squad.squad_supplement_id -1})"
    } else {
      " ..."
    }
    log.info(s"${player.Name} is lazing a position$purpose")
  }

  def handleUplinkRequest(packet: UplinkRequest): Unit = {
    sessionLogic.administrativeKick(player)
  }

  def handleAvatarGrenadeState(pkt: AvatarGrenadeStateMessage): Unit = {
    //grenades are handled elsewhere
    val AvatarGrenadeStateMessage(_, state) = pkt
    log.info(s"${player.Name} has $state ${player.Sex.possessive} grenade")
  }

  def handleChangeFireStateStart(pkt: ChangeFireStateMessage_Start): Unit = {
    val ChangeFireStateMessage_Start(item_guid) = pkt
    if (ops.shooting.isEmpty) {
      sessionLogic.findEquipment(item_guid) match {
        case Some(tool: Tool) if player.VehicleSeated.isEmpty =>
          fireStateStartWhenPlayer(tool, item_guid)
        case Some(tool: Tool) =>
          fireStateStartWhenMounted(tool, item_guid)
        case Some(_) if player.VehicleSeated.isEmpty =>
          fireStateStartSetup(item_guid)
          fireStateStartPlayerMessages(item_guid)
        case Some(_) =>
          fireStateStartSetup(item_guid)
          fireStateStartMountedMessages(item_guid)
        case None =>
          log.warn(s"ChangeFireState_Start: can not find $item_guid")
      }
    }
  }

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
      case _ =>
        log.warn(s"ChangeFireState_Stop: can not find $item_guid")
    }
    sessionLogic.general.progressBarUpdate.cancel()
    sessionLogic.general.progressBarValue = None
  }

  def handleReload(pkt: ReloadMessage): Unit = {
    val ReloadMessage(item_guid, _, unk1) = pkt
    ops.FindContainedWeapon match {
      case (Some(obj: Player), tools) =>
        handleReloadWhenPlayer(item_guid, obj, tools, unk1)
      case (Some(obj: PlanetSideServerObject with Container), tools) =>
        handleReloadWhenMountable(item_guid, obj, tools, unk1)
      case (_, _) =>
        log.warn(s"ReloadMessage: either can not find $item_guid or the object found was not a Tool")
    }
  }

  def handleChangeAmmo(pkt: ChangeAmmoMessage): Unit = {
    ops.handleChangeAmmo(pkt)
  }

  def handleChangeFireMode(pkt: ChangeFireModeMessage): Unit = {
    ops.handleChangeFireMode(pkt)
  }

  def handleProjectileState(pkt: ProjectileStateMessage): Unit = {
    ops.handleProjectileState(pkt)
  }

  def handleLongRangeProjectileState(pkt: LongRangeProjectileInfoMessage): Unit = {
    val LongRangeProjectileInfoMessage(guid, _, _) = pkt
    ops.FindContainedWeapon match {
      case (Some(_: Vehicle), weapons)
        if weapons.exists { _.GUID == guid } => () //now what?
      case _ => ()
    }
  }

  def handleDirectHit(pkt: HitMessage): Unit = {
    val list = ops.composeDirectDamageInformation(pkt)
      .collect {
        case (target, projectile, hitPos, _) =>
          ops.checkForHitPositionDiscrepancy(projectile.GUID, hitPos, target)
          ops.resolveProjectileInteraction(target, projectile, DamageResolution.Hit, hitPos)
          projectile
      }
    //...
    if (list.isEmpty) {
      val proxyList = ops
        .FindProjectileEntry(pkt.projectile_guid)
        .map(projectile => ops.resolveDamageProxy(projectile, projectile.GUID, pkt.hit_info.map(_.hit_pos).getOrElse(Vector3.Zero)))
        .getOrElse(Nil)
      proxyList.collectFirst {
        case (_, proxy, _, _) if proxy.tool_def == GlobalDefinitions.oicw =>
          ops.performLittleBuddyExplosion(proxyList.map(_._2))
      }
      proxyList.foreach {
        case (target, proxy, hitPos, _) if proxy.tool_def == GlobalDefinitions.oicw =>
          ops.checkForHitPositionDiscrepancy(proxy.GUID, hitPos, target)
      }
    }
  }

  def handleSplashHit(pkt: SplashHitMessage): Unit = {
    val list = ops.composeSplashDamageInformation(pkt)
    if (list.nonEmpty) {
      val projectile = list.head._2
      val explosionPosition = projectile.Position
      val projectileGuid = projectile.GUID
      val profile = projectile.profile
      val (resolution1, resolution2) = profile.Aggravated match {
        case Some(_) if profile.ProjectileDamageTypes.contains(DamageType.Aggravated) =>
          (DamageResolution.AggravatedDirect, DamageResolution.AggravatedSplash)
        case _ =>
          (DamageResolution.Splash, DamageResolution.Splash)
      }
      //...
      val (direct, others) = list.partition { case (_, _, hitPos, targetPos) => hitPos == targetPos }
      direct.foreach {
        case (target, _, hitPos, _) =>
          ops.checkForHitPositionDiscrepancy(projectile.GUID, hitPos, target)
          ops.resolveProjectileInteraction(target, projectile, resolution1, hitPos)
      }
      others.foreach {
        case (target, _, hitPos, _) =>
          ops.checkForHitPositionDiscrepancy(projectile.GUID, hitPos, target)
          ops.resolveProjectileInteraction(target, projectile, resolution2, hitPos)
      }
      //...
      val proxyList = ops.resolveDamageProxy(projectile, projectileGuid, explosionPosition)
      if (proxyList.nonEmpty) {
        proxyList.foreach {
          case (target, _, hitPos, _) => ops.checkForHitPositionDiscrepancy(projectileGuid, hitPos, target)
        }
        if (profile == GlobalDefinitions.oicw_projectile) {
          ops.performLittleBuddyExplosion(proxyList.map(_._2))
        }
      }
      //...
      if (
        profile.HasJammedEffectDuration ||
          profile.JammerProjectile ||
          profile.SympatheticExplosion
      ) {
        //can also substitute 'profile' for 'SpecialEmp.emp'
        Zone.serverSideDamage(
          continent,
          player,
          SpecialEmp.emp,
          SpecialEmp.createEmpInteraction(SpecialEmp.emp, explosionPosition),
          SpecialEmp.prepareDistanceCheck(player, explosionPosition, player.Faction),
          SpecialEmp.findAllBoomers(profile.DamageRadius)
        )
      }
      if (profile.ExistsOnRemoteClients && projectile.HasGUID) {
        //cleanup
        continent.Projectile ! ZoneProjectile.Remove(projectile.GUID)
      }
    }
  }

  def handleLashHit(pkt: LashMessage): Unit = {
    val list = ops.composeLashDamageInformation(pkt)
    list.foreach {
      case (target, projectile, hitPos, _) =>
        ops.checkForHitPositionDiscrepancy(projectile.GUID, hitPos, target)
        ops.resolveProjectileInteraction(target, projectile, DamageResolution.Lash, hitPos)
    }
  }

  def handleAIDamage(pkt: AIDamage): Unit = {
    val list = ops.composeAIDamageInformation(pkt)
    if (ops.confirmAIDamageTarget(pkt, list.map(_._1))) {
      list.foreach {
        case (target, projectile, hitPos, _) =>
          ops.checkForHitPositionDiscrepancy(projectile.GUID, hitPos, target)
          ops.resolveProjectileInteraction(target, projectile, DamageResolution.Hit, hitPos)
      }
    }
  }

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

  /*
  used by ChangeFireStateMessage_Start handling
   */
  private def fireStateStartSetup(itemGuid: PlanetSideGUID): Unit = {
    ops.prefire -= itemGuid
    ops.shooting += itemGuid
    ops.shootingStart += itemGuid -> System.currentTimeMillis()
  }

  private def fireStateStartChargeMode(tool: Tool): Unit = {
    //charge ammunition drain
    tool.FireMode match {
      case mode: ChargeFireModeDefinition =>
        sessionLogic.general.progressBarValue = Some(0f)
        sessionLogic.general.progressBarUpdate = context.system.scheduler.scheduleOnce(
          (mode.Time + mode.DrainInterval) milliseconds,
          context.self,
          CommonMessages.ProgressEvent(1f, () => {}, Tools.ChargeFireMode(player, tool), mode.DrainInterval)
        )
      case _ => ()
    }
  }

  private def fireStateStartPlayerMessages(itemGuid: PlanetSideGUID): Unit = {
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.ChangeFireState_Start(player.GUID, itemGuid)
    )
  }

  private def fireStateStartMountedMessages(itemGuid: PlanetSideGUID): Unit = {
    sessionLogic.findContainedEquipment()._1.collect {
      case turret: FacilityTurret if continent.map.cavern =>
        turret.Actor ! VanuSentry.ChangeFireStart
    }
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.ChangeFireState_Start(player.GUID, itemGuid)
    )
  }

  private def enforceEmptyMagazine(tool: Tool, itemGuid: PlanetSideGUID): Unit = {
    log.warn(
      s"ChangeFireState_Start: ${player.Name}'s ${tool.Definition.Name} magazine was empty before trying to shoot"
    )
    ops.emptyMagazine(itemGuid, tool)
  }

  private def fireStateStartWhenPlayer(tool: Tool, itemGuid: PlanetSideGUID): Unit = {
    if (ops.allowFireStateChangeStart(tool, itemGuid)) {
      fireStateStartSetup(itemGuid)
      //special case - suppress the decimator's alternate fire mode, by projectile
      if (tool.Projectile != GlobalDefinitions.phoenix_missile_guided_projectile) {
        fireStateStartPlayerMessages(itemGuid)
      }
      fireStateStartChargeMode(tool)
    } else {
      enforceEmptyMagazine(tool, itemGuid)
    }
  }

  private def fireStateStartWhenMounted(tool: Tool, itemGuid: PlanetSideGUID): Unit = {
    if (ops.allowFireStateChangeStart(tool, itemGuid)) {
      fireStateStartSetup(itemGuid)
      fireStateStartMountedMessages(itemGuid)
      fireStateStartChargeMode(tool)
    } else {
      enforceEmptyMagazine(tool, itemGuid)
    }
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

  /*
  used by ReloadMessage handling
  */
  private def reloadPlayerMessages(itemGuid: PlanetSideGUID): Unit = {
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.Reload(player.GUID, itemGuid)
    )
  }

  private def reloadVehicleMessages(itemGuid: PlanetSideGUID): Unit = {
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.Reload(player.GUID, itemGuid)
    )
  }

  private def handleReloadWhenPlayer(
                                      itemGuid: PlanetSideGUID,
                                      obj: Player,
                                      tools: Set[Tool],
                                      unk1: Int
                                    ): Unit = {
    ops.handleReloadProcedure(
      itemGuid,
      obj,
      tools,
      unk1,
      RemoveOldEquipmentFromInventory(obj)(_),
      ops.modifyAmmunition(obj)(_, _),
      reloadPlayerMessages
    )
  }

  private def handleReloadWhenMountable(
                                         itemGuid: PlanetSideGUID,
                                         obj: PlanetSideServerObject with Container,
                                         tools: Set[Tool],
                                         unk1: Int
                                       ): Unit = {
    ops.handleReloadProcedure(
      itemGuid,
      obj,
      tools,
      unk1,
      RemoveOldEquipmentFromInventory(obj)(_),
      ops.modifyAmmunitionInMountable(obj)(_, _),
      reloadVehicleMessages
    )
  }
}
