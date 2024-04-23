// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.{ActorContext, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{SessionData, WeaponAndProjectileFunctions, WeaponAndProjectileOperations}
import net.psforever.login.WorldSession.{CountGrenades, FindEquipmentStock, FindToolThatUses, RemoveOldEquipmentFromInventory}
import net.psforever.objects.ballistics.{Projectile, ProjectileQuality}
import net.psforever.objects.definition.ProjectileDefinition
import net.psforever.objects.equipment.{ChargeFireModeDefinition, EquipmentSize}
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.doors.InteriorDoorPassage
import net.psforever.objects.{AmmoBox, BoomerDeployable, BoomerTrigger, DummyExplodingEntity, GlobalDefinitions, OwnableByPlayer, PlanetSideGameObject, SpecialEmp, Tool}
import net.psforever.objects.serverobject.interior.Sidedness
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.turret.auto.{AutomatedTurret, AutomatedTurretBehavior}
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.base.{DamageResolution, DamageType}
import net.psforever.objects.vital.etc.OicwLilBuddyReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.objects.zones.{Zone, ZoneProjectile}
import net.psforever.packet.game.{AIDamage, AvatarGrenadeStateMessage, ChainLashMessage, ChangeAmmoMessage, ChangeFireModeMessage, ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, HitMessage, InventoryStateMessage, LashMessage, LongRangeProjectileInfoMessage, ProjectileStateMessage, QuantityUpdateMessage, ReloadMessage, SplashHitMessage, UplinkRequest, UplinkRequestType, UplinkResponse, WeaponDelayFireMessage, WeaponDryFireMessage, WeaponFireMessage, WeaponLazeTargetPositionMessage}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.{PlanetSideGUID, Vector3}
import net.psforever.util.Config

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object WeaponAndProjectileLogic {
  def apply(ops: WeaponAndProjectileOperations): WeaponAndProjectileLogic = {
    new WeaponAndProjectileLogic(ops, ops.context)
  }

  /**
   * Does a line segment line intersect with a sphere?<br>
   * This most likely belongs in `Geometry` or `GeometryForm` or somehow in association with the `\objects\geometry\` package.
   * @param start first point of the line segment
   * @param end second point of the line segment
   * @param center center of the sphere
   * @param radius radius of the sphere
   * @return list of all points of intersection, if any
   * @see `Vector3.DistanceSquared`
   * @see `Vector3.MagnitudeSquared`
   */
  private def quickLineSphereIntersectionPoints(
                                                 start: Vector3,
                                                 end: Vector3,
                                                 center: Vector3,
                                                 radius: Float
                                               ): Iterable[Vector3] = {
    /*
    Algorithm adapted from code found on https://paulbourke.net/geometry/circlesphere/index.html#linesphere,
     because I kept messing up proper substitution of the line formula and the circle formula into the quadratic equation.
     */
    val Vector3(cx, cy, cz) = center
    val Vector3(sx, sy, sz) = start
    val vector = end - start
    //speed our way through a quadratic equation
    val (a, b) = {
      val Vector3(dx, dy, dz) = vector
      (
        dx * dx + dy * dy + dz * dz,
        2f * (dx * (sx - cx) + dy * (sy - cy) + dz * (sz - cz))
      )
    }
    val c = Vector3.MagnitudeSquared(center) + Vector3.MagnitudeSquared(start) - 2f * (cx * sx + cy * sy + cz * sz) - radius * radius
    val result = b * b - 4 * a * c
    if (result < 0f) {
      //negative, no intersection
      Seq()
    } else if (result < 0.00001f) {
      //zero-ish, one intersection point
      Seq(start - vector * (b / (2f * a)))
    } else {
      //positive, two intersection points
      val sqrt = math.sqrt(result).toFloat
      val endStart = vector / (2f * a)
      Seq(start + endStart * (sqrt - b), start + endStart * (b + sqrt) * -1f)
    }.filter(p => Vector3.DistanceSquared(start, p) <= a)
  }
  /**
   * Preparation for explosion damage that utilizes the Scorpion's little buddy sub-projectiles.
   * The main difference from "normal" server-side explosion
   * is that the owner of the projectile must be clarified explicitly.
   * @see `Zone::serverSideDamage`
   * @param zone where the explosion is taking place
   *             (`source` contains the coordinate location)
   * @param source a game object that represents the source of the explosion
   * @param owner who or what to accredit damage from the explosion to;
   *              clarifies a normal `SourceEntry(source)` accreditation
   */
  private def detonateLittleBuddy(
                                   zone: Zone,
                                   source: PlanetSideGameObject with FactionAffinity with Vitality,
                                   proxy: Projectile,
                                   owner: SourceEntry
                                 )(): Unit = {
    Zone.serverSideDamage(zone, source, littleBuddyExplosionDamage(owner, proxy.id, source.Position))
  }

  /**
   * Preparation for explosion damage that utilizes the Scorpion's little buddy sub-projectiles.
   * The main difference from "normal" server-side explosion
   * is that the owner of the projectile must be clarified explicitly.
   * The sub-projectiles will be the product of a normal projectile rather than a standard game object
   * so a custom `source` entity must wrap around it and fulfill the requirements of the field.
   * @see `Zone::explosionDamage`
   * @param owner who or what to accredit damage from the explosion to
   * @param explosionPosition where the explosion will be positioned in the game world
   * @param source a game object that represents the source of the explosion
   * @param target a game object that is affected by the explosion
   * @return a `DamageInteraction` object
   */
  private def littleBuddyExplosionDamage(
                                          owner: SourceEntry,
                                          projectileId: Long,
                                          explosionPosition: Vector3
                                        )
                                        (
                                          source: PlanetSideGameObject with FactionAffinity with Vitality,
                                          target: PlanetSideGameObject with FactionAffinity with Vitality
                                        ): DamageInteraction = {
    DamageInteraction(SourceEntry(target), OicwLilBuddyReason(owner, projectileId, target.DamageModel), explosionPosition)
  }
}

class WeaponAndProjectileLogic(val ops: WeaponAndProjectileOperations, implicit val context: ActorContext) extends WeaponAndProjectileFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  /* packets */

  def handleWeaponFire(pkt: WeaponFireMessage): Unit = { /* intentionally blank */ }

  def handleWeaponDelayFire(pkt: WeaponDelayFireMessage): Unit = { /* intentionally blank */ }

  def handleWeaponDryFire(pkt: WeaponDryFireMessage): Unit = { /* intentionally blank */ }

  def handleWeaponLazeTargetPosition(pkt: WeaponLazeTargetPositionMessage): Unit = { /* intentionally blank */ }

  def handleUplinkRequest(packet: UplinkRequest): Unit = {
    val UplinkRequest(code, _, _) = packet
    val playerFaction = player.Faction
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
    val ProjectileStateMessage(projectile_guid, shot_pos, shot_vel, shot_orient, seq, end, target_guid) = pkt
    val index = projectile_guid.guid - Projectile.baseUID
    ops.projectiles(index) match {
      case Some(projectile) if projectile.HasGUID =>
        val projectileGlobalUID = projectile.GUID
        projectile.Position = shot_pos
        projectile.Orientation = shot_orient
        projectile.Velocity = shot_vel
        continent.AvatarEvents ! AvatarServiceMessage(
          continent.id,
          AvatarAction.ProjectileState(
            player.GUID,
            projectileGlobalUID,
            shot_pos,
            shot_vel,
            shot_orient,
            seq,
            end,
            target_guid
          )
        )
      case _ if seq == 0 =>
      /* missing the first packet in the sequence is permissible */
      case _ =>
        log.warn(s"ProjectileState: constructed projectile ${projectile_guid.guid} can not be found")
    }
  }

  def handleLongRangeProjectileState(pkt: LongRangeProjectileInfoMessage): Unit = { /* intentionally blank */ }

  def handleDirectHit(pkt: HitMessage): Unit = { /* intentionally blank */ }

  def handleSplashHit(pkt: SplashHitMessage): Unit = {
    val SplashHitMessage(
    _,
    projectile_guid,
    explosion_pos,
    direct_victim_uid,
    _,
    projectile_vel,
    _,
    targets
    ) = pkt
    ops.FindProjectileEntry(projectile_guid) match {
      case Some(projectile) =>
        val profile = projectile.profile
        projectile.Velocity = projectile_vel
        val (resolution1, resolution2) = profile.Aggravated match {
          case Some(_) if profile.ProjectileDamageTypes.contains(DamageType.Aggravated) =>
            (DamageResolution.AggravatedDirect, DamageResolution.AggravatedSplash)
          case _ =>
            (DamageResolution.Splash, DamageResolution.Splash)
        }
        //direct_victim_uid
        sessionLogic.validObject(direct_victim_uid, decorator = "SplashHit/direct_victim") match {
          case Some(target: PlanetSideGameObject with FactionAffinity with Vitality) =>
            CheckForHitPositionDiscrepancy(projectile_guid, explosion_pos, target)
            ResolveProjectileInteraction(projectile, resolution1, target, target.Position).collect { resprojectile =>
              addShotsLanded(resprojectile.cause.attribution, shots = 1)
              sessionLogic.handleDealingDamage(target, resprojectile)
            }
          case _ => ()
        }
        //other victims
        targets.foreach(elem => {
          sessionLogic.validObject(elem.uid, decorator = "SplashHit/other_victims") match {
            case Some(target: PlanetSideGameObject with FactionAffinity with Vitality) =>
              CheckForHitPositionDiscrepancy(projectile_guid, explosion_pos, target)
              ResolveProjectileInteraction(projectile, resolution2, target, explosion_pos).collect { resprojectile =>
                addShotsLanded(resprojectile.cause.attribution, shots = 1)
                sessionLogic.handleDealingDamage(target, resprojectile)
              }
            case _ => ()
          }
        })
        //...
        HandleDamageProxy(projectile, projectile_guid, explosion_pos)
        if (
          projectile.profile.HasJammedEffectDuration ||
            projectile.profile.JammerProjectile ||
            projectile.profile.SympatheticExplosion
        ) {
          //can also substitute 'projectile.profile' for 'SpecialEmp.emp'
          Zone.serverSideDamage(
            continent,
            player,
            SpecialEmp.emp,
            SpecialEmp.createEmpInteraction(SpecialEmp.emp, explosion_pos),
            SpecialEmp.prepareDistanceCheck(player, explosion_pos, player.Faction),
            SpecialEmp.findAllBoomers(profile.DamageRadius)
          )
        }
        if (profile.ExistsOnRemoteClients && projectile.HasGUID) {
          //cleanup
          if (projectile.HasGUID) {
            continent.Projectile ! ZoneProjectile.Remove(projectile.GUID)
          }
        }
      case None => ()
    }
  }

  def handleLashHit(pkt: LashMessage): Unit = { /* intentionally blank */ }

  def handleAIDamage(pkt: AIDamage): Unit = {
    val AIDamage(targetGuid, attackerGuid, projectileTypeId, _, _) = pkt
    (continent.GUID(player.VehicleSeated) match {
      case Some(tobj: PlanetSideServerObject with FactionAffinity with Vitality with OwnableByPlayer)
        if tobj.GUID == targetGuid &&
          tobj.OwnerGuid.contains(player.GUID) =>
        //deployable turrets
        Some(tobj)
      case Some(tobj: PlanetSideServerObject with FactionAffinity with Vitality with Mountable)
        if tobj.GUID == targetGuid &&
          tobj.Seats.values.flatMap(_.occupants.map(_.GUID)).toSeq.contains(player.GUID) =>
        //facility turrets, etc.
        Some(tobj)
      case _
        if player.GUID == targetGuid =>
        //player avatars
        Some(player)
      case _ =>
        None
    }).collect {
      case target: AutomatedTurret.Target =>
        sessionLogic.validObject(attackerGuid, decorator = "AIDamage/AutomatedTurret")
          .collect {
            case turret: AutomatedTurret if turret.Target.isEmpty =>
              turret.Actor ! AutomatedTurretBehavior.ConfirmShot(target)
              Some(target)

            case turret: AutomatedTurret =>
              turret.Actor ! AutomatedTurretBehavior.ConfirmShot(target)
              HandleAIDamage(target, CompileAutomatedTurretDamageData(turret, turret.TurretOwner, projectileTypeId))
              Some(target)
          }
    }
      .orElse {
        //occasionally, something that is not technically a turret's natural target may be attacked
        sessionLogic.validObject(targetGuid, decorator = "AIDamage/Target")
          .collect {
            case target: PlanetSideServerObject with FactionAffinity with Vitality =>
              sessionLogic.validObject(attackerGuid, decorator = "AIDamage/Attacker")
                .collect {
                  case turret: AutomatedTurret if turret.Target.nonEmpty =>
                    //the turret must be shooting at something (else) first
                    HandleAIDamage(target, CompileAutomatedTurretDamageData(turret, turret.TurretOwner, projectileTypeId))
                }
              Some(target)
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
            ModifyAmmunition(player)(box.AmmoSlot.Box, 3 - tailReloadValue)
            3
          }
          log.info(s"${player.Name} found $actualReloadValue more $ammoType grenades to throw")
          ModifyAmmunition(player)(
            tool.AmmoSlot.Box,
            -actualReloadValue
          ) //grenade item already in holster (negative because empty)
          xs.foreach(item => { RemoveOldEquipmentFromInventory(player)(item.obj) })
      }
    } else if (tdef == GlobalDefinitions.phoenix) {
      RemoveOldEquipmentFromInventory(player)(tool)
    }
  }

  /**
   * Given an object that contains a box of amunition in its `Inventory` at a certain location,
   * change the amount of ammunition within that box.
   * @param obj the `Container`
   * @param box an `AmmoBox` to modify
   * @param reloadValue the value to modify the `AmmoBox`;
   *                    subtracted from the current `Capacity` of `Box`
   */
  private def ModifyAmmunition(obj: PlanetSideGameObject with Container)(box: AmmoBox, reloadValue: Int): Unit = {
    val capacity = box.Capacity - reloadValue
    box.Capacity = capacity
    sendResponse(InventoryStateMessage(box.GUID, obj.GUID, capacity))
  }

  private def CheckForHitPositionDiscrepancy(
                                              projectile_guid: PlanetSideGUID,
                                              hitPos: Vector3,
                                              target: PlanetSideGameObject with FactionAffinity with Vitality
                                            ): Unit = {
    val hitPositionDiscrepancy = Vector3.DistanceSquared(hitPos, target.Position)
    if (hitPositionDiscrepancy > Config.app.antiCheat.hitPositionDiscrepancyThreshold) {
      // If the target position on the server does not match the position where the projectile landed within reason there may be foul play
      log.warn(
        s"${player.Name}'s shot #${projectile_guid.guid} has hit discrepancy with target. Target: ${target.Position}, Reported: $hitPos, Distance: $hitPositionDiscrepancy / ${math.sqrt(hitPositionDiscrepancy).toFloat}; suspect"
      )
    }
  }

  /**
   * na
   * @param projectile the projectile object
   * @param resolution the resolution status to promote the projectile
   * @return a copy of the projectile
   */
  private def ResolveProjectileInteraction(
                                            projectile: Projectile,
                                            resolution: DamageResolution.Value,
                                            target: PlanetSideGameObject with FactionAffinity with Vitality,
                                            pos: Vector3
                                          ): Option[DamageInteraction] = {
    if (projectile.isMiss) {
      log.warn("expected projectile was already counted as a missed shot; can not resolve any further")
      None
    } else {
      val outProjectile = ProjectileQuality.modifiers(projectile, resolution, target, pos, Some(player))
      if (projectile.tool_def.Size == EquipmentSize.Melee && outProjectile.quality == ProjectileQuality.Modified(25)) {
        avatarActor ! AvatarActor.ConsumeStamina(10)
      }
      Some(DamageInteraction(SourceEntry(target), ProjectileReason(resolution, outProjectile, target.DamageModel), pos))
    }
  }

  /**
   * Take a projectile that was introduced into the game world and
   * determine if it generates a secondary damage projectile or
   * an method of damage causation that requires additional management.
   * @param projectile the projectile
   * @param pguid the client-local projectile identifier
   * @param hitPos the game world position where the projectile is being recorded
   * @return a for all affected targets, a combination of projectiles, projectile location, and the target's location;
   *         nothing if no targets were affected
   */
  private def HandleDamageProxy(
                                 projectile: Projectile,
                                 pguid: PlanetSideGUID,
                                 hitPos: Vector3
                               ): List[(PlanetSideGameObject with FactionAffinity with Vitality, Projectile, Vector3, Vector3)] = {
    GlobalDefinitions.getDamageProxy(projectile, hitPos) match {
      case Nil =>
        Nil
      case list if list.isEmpty =>
        Nil
      case list =>
        HandleDamageProxySetupLittleBuddy(list, hitPos)
        UpdateProjectileSidednessAfterHit(projectile, hitPos)
        val projectileSide = projectile.WhichSide
        list.flatMap { proxy =>
          if (proxy.profile.ExistsOnRemoteClients) {
            proxy.Position = hitPos
            proxy.WhichSide = projectileSide
            continent.Projectile ! ZoneProjectile.Add(player.GUID, proxy)
            Nil
          } else if (proxy.tool_def == GlobalDefinitions.maelstrom) {
            //server-side maelstrom grenade target selection
            val radius = proxy.profile.LashRadius * proxy.profile.LashRadius
            val targets = Zone.findAllTargets(continent, hitPos, proxy.profile.LashRadius, { _.livePlayerList })
              .filter { target =>
                Vector3.DistanceSquared(target.Position, hitPos) <= radius
              }
            //chainlash is separated from the actual damage application for convenience
            continent.AvatarEvents ! AvatarServiceMessage(
              continent.id,
              AvatarAction.SendResponse(
                PlanetSideGUID(0),
                ChainLashMessage(
                  hitPos,
                  projectile.profile.ObjectId,
                  targets.map { _.GUID }
                )
              )
            )
            targets.map { target =>
              CheckForHitPositionDiscrepancy(pguid, hitPos, target)
              (target, proxy, hitPos, target.Position)
            }
          } else {
            Nil
          }
        }
    }
  }

  private def HandleDamageProxySetupLittleBuddy(listOfProjectiles: List[Projectile], detonationPosition: Vector3): Boolean = {
    val listOfLittleBuddies: List[Projectile] = listOfProjectiles.filter { _.tool_def == GlobalDefinitions.oicw }
    val size: Int = listOfLittleBuddies.size
    if (size > 0) {
      val desiredDownwardsProjectiles: Int = 2
      val firstHalf: Int = math.min(size, desiredDownwardsProjectiles) //number that fly straight down
      val secondHalf: Int = math.max(size - firstHalf, 0) //number that are flared out
      val z: Float = player.Orientation.z //player's standing direction
      val north: Vector3 = Vector3(0,1,0) //map North
      val speed: Float = 144f //speed (packet discovered)
      val dist: Float = 25 //distance (client defined)
      val downwardsAngle: Float = -85f
      val flaredAngle: Float = -70f
      //angle of separation for downwards, degrees from vertical for flared out
      val (smallStep, smallAngle): (Float, Float) = if (firstHalf > 1) {
        (360f / firstHalf, downwardsAngle)
      } else {
        (0f, 0f)
      }
      val (largeStep, largeAngle): (Float, Float) = if (secondHalf > 1) {
        (360f / secondHalf, flaredAngle)
      } else {
        (0f, 0f)
      }
      val smallRotOffset: Float = z + 90f
      val largeRotOffset: Float = z + math.random().toFloat * 45f
      val verticalCorrection = Vector3.z(dist - dist * math.sin(math.toRadians(90 - smallAngle + largeAngle)).toFloat)
      //downwards projectiles
      var i: Int = 0
      listOfLittleBuddies.take(firstHalf).foreach { proxy =>
        val facing = (smallRotOffset + smallStep * i.toFloat) % 360
        val dir = north.Rx(smallAngle).Rz(facing)
        proxy.Position = detonationPosition + dir.xy + verticalCorrection
        proxy.Velocity = dir * speed
        proxy.Orientation = Vector3(0, (360f + smallAngle) % 360, facing)
        HandleDamageProxyLittleBuddyExplosion(proxy, dir, dist)
        i += 1
      }
      //flared out projectiles
      i = 0
      listOfLittleBuddies.drop(firstHalf).foreach { proxy =>
        val facing = (largeRotOffset + largeStep * i.toFloat) % 360
        val dir = north.Rx(largeAngle).Rz(facing)
        proxy.Position = detonationPosition + dir
        proxy.Velocity = dir * speed
        proxy.Orientation = Vector3(0, (360f + largeAngle) % 360, facing)
        HandleDamageProxyLittleBuddyExplosion(proxy, dir, dist)
        i += 1
      }
      true
    } else {
      false
    }
  }

  private def HandleDamageProxyLittleBuddyExplosion(proxy: Projectile, orientation: Vector3, distance: Float): Unit = {
    //explosion
    val obj = new DummyExplodingEntity(proxy, proxy.owner.Faction)
    obj.Position = obj.Position + orientation * distance
    val explosionFunc: ()=>Unit = WeaponAndProjectileLogic.detonateLittleBuddy(continent, obj, proxy, proxy.owner)
    context.system.scheduler.scheduleOnce(500.milliseconds) { explosionFunc() }
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

  //noinspection SameParameterValue
  private def addShotsLanded(weaponId: Int, shots: Int): Unit = {
    ops.addShotsToMap(ops.shotsLanded, weaponId, shots)
  }

  private def CompileAutomatedTurretDamageData(
                                                turret: AutomatedTurret,
                                                owner: SourceEntry,
                                                projectileTypeId: Long
                                              ): Option[(AutomatedTurret, Tool, SourceEntry, ProjectileDefinition)] = {
    turret.Weapons
      .values
      .flatMap { _.Equipment }
      .collect { case weapon: Tool => (turret, weapon, owner, weapon.Projectile) }
      .find { case (_, _, _, p) => p.ObjectId == projectileTypeId }
  }

  private def HandleAIDamage(
                              target: PlanetSideServerObject with FactionAffinity with Vitality,
                              results: Option[(AutomatedTurret, Tool, SourceEntry, ProjectileDefinition)]
                            ): Unit = {
    results.collect {
      case (obj, tool, owner, projectileInfo) =>
        val angle = Vector3.Unit(target.Position - obj.Position)
        val proj = new Projectile(
          projectileInfo,
          tool.Definition,
          tool.FireMode,
          None,
          owner,
          obj.Definition.ObjectId,
          obj.Position + Vector3.z(value = 1f),
          angle,
          Some(angle * projectileInfo.FinalVelocity)
        )
        val hitPos = target.Position + Vector3.z(value = 1f)
        ResolveProjectileInteraction(proj, DamageResolution.Hit, target, hitPos).collect { resprojectile =>
          addShotsLanded(resprojectile.cause.attribution, shots = 1)
          sessionLogic.handleDealingDamage(target, resprojectile)
        }
    }
  }

  private def UpdateProjectileSidednessAfterHit(projectile: Projectile, hitPosition: Vector3): Unit = {
    val origin = projectile.Position
    val distance = Vector3.Magnitude(hitPosition - origin)
    continent.blockMap
      .sector(hitPosition, distance)
      .environmentList
      .collect { case o: InteriorDoorPassage =>
        val door = o.door
        val intersectTest = WeaponAndProjectileLogic.quickLineSphereIntersectionPoints(
          origin,
          hitPosition,
          door.Position,
          door.Definition.UseRadius + 0.1f
        )
        (door, intersectTest)
      }
      .collect { case (door, intersectionTest) if intersectionTest.nonEmpty =>
        (door, Vector3.Magnitude(hitPosition - door.Position), intersectionTest)
      }
      .minByOption { case (_, dist, _) => dist }
      .foreach { case (door, _, intersects) =>
        val strictly = if (Vector3.DotProduct(Vector3.Unit(hitPosition - door.Position), door.Outwards) > 0f) {
          Sidedness.OutsideOf
        } else {
          Sidedness.InsideOf
        }
        projectile.WhichSide = if (intersects.size == 1) {
          Sidedness.InBetweenSides(door, strictly)
        } else {
          strictly
        }
      }
  }
}
