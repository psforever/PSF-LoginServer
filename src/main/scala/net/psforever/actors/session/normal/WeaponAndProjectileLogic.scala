// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.ActorContext
import net.psforever.actors.session.support.{SessionData, WeaponAndProjectileFunctions, WeaponAndProjectileOperations}
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.{BoomerDeployable, BoomerTrigger, Player, SpecialEmp, Tool, Vehicle}
import net.psforever.objects.vital.base.{DamageResolution, DamageType}
import net.psforever.objects.zones.{Zone, ZoneProjectile}
import net.psforever.packet.game.{AIDamage, AvatarGrenadeStateMessage, ChangeAmmoMessage, ChangeFireModeMessage, ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, HitMessage, LashMessage, LongRangeProjectileInfoMessage, OrbitalStrikeWaypointMessage, ProjectileStateMessage, ReloadMessage, SplashHitMessage, TriggerEffectMessage, TriggeredEffectLocation, UplinkRequest, UplinkRequestType, UplinkResponse, WeaponDelayFireMessage, WeaponDryFireMessage, WeaponFireMessage, WeaponLazeTargetPositionMessage}
import net.psforever.types.{ValidPlanetSideGUID, Vector3}

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
    ops.handleUplinkRequest(packet)
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
          ops.fireStateStartWhenPlayer(tool, item_guid)
        case Some(tool: Tool) =>
          ops.fireStateStartWhenMounted(tool, item_guid)
        case Some(_) if player.VehicleSeated.isEmpty =>
          ops.fireStateStartSetup(item_guid)
          ops.fireStateStartPlayerMessages(item_guid)
        case Some(_) =>
          ops.fireStateStartSetup(item_guid)
          ops.fireStateStartMountedMessages(item_guid)
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
        ops.handleReloadWhenPlayer(item_guid, obj, tools, unk1)
      case (Some(obj: PlanetSideServerObject with Container), tools) =>
        ops.handleReloadWhenMountable(item_guid, obj, tools, unk1)
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
        if weapons.exists(_.GUID == guid) => () //now what?
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
      ops.handleProxyDamage(pkt.projectile_guid, pkt.hit_info.map(_.hit_pos).getOrElse(Vector3.Zero)).foreach {
        case (target, proxy, hitPos, _) =>
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
          ops.checkForHitPositionDiscrepancy(projectileGuid, hitPos, target)
          ops.resolveProjectileInteraction(target, projectile, resolution1, hitPos)
      }
      others.foreach {
        case (target, _, hitPos, _) =>
          ops.checkForHitPositionDiscrepancy(projectileGuid, hitPos, target)
          ops.resolveProjectileInteraction(target, projectile, resolution2, hitPos)
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
    //...
    ops.handleProxyDamage(pkt.projectile_uid, pkt.projectile_pos).foreach {
      case (target, proxy, hitPos, _) =>
        ops.checkForHitPositionDiscrepancy(proxy.GUID, hitPos, target)
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
          ops.checkForHitPositionDiscrepancy(pkt.attacker_guid, hitPos, target)
          ops.resolveProjectileInteraction(target, projectile, DamageResolution.Hit, hitPos)
      }
    }
  }
}
