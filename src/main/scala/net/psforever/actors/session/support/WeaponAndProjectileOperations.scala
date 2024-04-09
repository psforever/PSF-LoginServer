// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, typed}
import net.psforever.objects.definition.ProjectileDefinition
import net.psforever.objects.serverobject.doors.InteriorDoorPassage
import net.psforever.objects.serverobject.interior.Sidedness
import net.psforever.objects.serverobject.turret.auto.{AutomatedTurret, AutomatedTurretBehavior}
import net.psforever.objects.zones.Zoning
import net.psforever.objects.serverobject.turret.VanuSentry
import net.psforever.objects.zones.exp.ToDatabase

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
//
import net.psforever.actors.session.{AvatarActor, ChatActor}
import net.psforever.login.WorldSession.{CountAmmunition, CountGrenades, FindAmmoBoxThatUses, FindEquipmentStock, FindToolThatUses, PutEquipmentInInventoryOrDrop, PutNewEquipmentInInventoryOrDrop, RemoveOldEquipmentFromInventory}
import net.psforever.objects.avatar.scoring.EquipmentStat
import net.psforever.objects.ballistics.{Projectile, ProjectileQuality}
import net.psforever.objects.entity.SimpleWorldEntity
import net.psforever.objects.equipment.{ChargeFireModeDefinition, Equipment, EquipmentSize, FireModeSwitch}
import net.psforever.objects.guid.{GUIDTask, TaskBundle, TaskWorkflow}
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.base.{DamageResolution, DamageType}
import net.psforever.objects.vital.etc.OicwLilBuddyReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.objects.zones.{Zone, ZoneProjectile}
import net.psforever.objects._
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.packet.game._
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{ExoSuitType, PlanetSideGUID, Vector3}
import net.psforever.util.Config

private[support] class WeaponAndProjectileOperations(
                                                      val sessionLogic: SessionLogic,
                                                      avatarActor: typed.ActorRef[AvatarActor.Command],
                                                      chatActor: typed.ActorRef[ChatActor.Command],
                                                      implicit val context: ActorContext
                                   ) extends CommonSessionInterfacingFunctionality {
  var shooting: mutable.Set[PlanetSideGUID] = mutable.Set.empty //ChangeFireStateMessage_Start
  var prefire: mutable.Set[PlanetSideGUID] = mutable.Set.empty //if WeaponFireMessage precedes ChangeFireStateMessage_Start
  private[support] var shootingStart: mutable.HashMap[PlanetSideGUID, Long] = mutable.HashMap[PlanetSideGUID, Long]()
  private[support] var shootingStop: mutable.HashMap[PlanetSideGUID, Long] = mutable.HashMap[PlanetSideGUID, Long]()
  private val shotsFired: mutable.HashMap[Int,Int] = mutable.HashMap[Int,Int]()
  private val shotsLanded: mutable.HashMap[Int,Int] = mutable.HashMap[Int,Int]()
  private[support] var shotsWhileDead: Int = 0
  private val projectiles: Array[Option[Projectile]] =
    Array.fill[Option[Projectile]](Projectile.rangeUID - Projectile.baseUID)(None)

  /* packets */

  def handleWeaponFire(pkt: WeaponFireMessage): Unit = {
    val WeaponFireMessage(
    _,
    weapon_guid,
    projectile_guid,
    shot_origin,
    _,
    _,
    _,
    _/*max_distance,*/,
    _,
    _/*projectile_type,*/,
    thrown_projectile_vel
    ) = pkt
    HandleWeaponFireOperations(weapon_guid, projectile_guid, shot_origin, thrown_projectile_vel.flatten)
  }

  def handleWeaponDelayFire(pkt: WeaponDelayFireMessage): Unit = {
    val WeaponDelayFireMessage(_, _) = pkt
    log.info(s"${player.Name} - $pkt")
  }

  def handleWeaponDryFire(pkt: WeaponDryFireMessage): Unit = {
    val WeaponDryFireMessage(weapon_guid) = pkt
    val (containerOpt, tools) = FindContainedWeapon
    tools
      .find { _.GUID == weapon_guid }
      .orElse { continent.GUID(weapon_guid) }
      .collect {
        case _: Equipment if containerOpt.exists(_.isInstanceOf[Player]) =>
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.WeaponDryFire(player.GUID, weapon_guid)
          )
        case _: Equipment =>
          continent.VehicleEvents ! VehicleServiceMessage(
            continent.id,
            VehicleAction.WeaponDryFire(player.GUID, weapon_guid)
          )
      }
      .orElse {
        log.warn(
          s"WeaponDryFire: ${player.Name}'s weapon ${weapon_guid.guid} is either not a weapon or does not exist"
        )
        None
      }
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

  def handleAvatarGrenadeState(pkt: AvatarGrenadeStateMessage): Unit = {
    val AvatarGrenadeStateMessage(_, state) = pkt
    //TODO I thought I had this working?
    log.info(s"${player.Name} has $state ${player.Sex.possessive} grenade")
  }

  def handleChangeFireStateStart(pkt: ChangeFireStateMessage_Start)(implicit context: ActorContext): Unit = {
    val ChangeFireStateMessage_Start(item_guid) = pkt
    if (shooting.isEmpty) {
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
    prefire -= item_guid
    shootingStop += item_guid -> now
    shooting -= item_guid
    sessionLogic.findEquipment(item_guid) match {
      case Some(tool: Tool) if player.VehicleSeated.isEmpty =>
        fireStateStopWhenPlayer(tool, item_guid)
      case Some(tool: Tool) =>
        fireStateStopWhenMounted(tool, item_guid)
      case Some(trigger: BoomerTrigger) =>
        fireStateStopPlayerMessages(item_guid)
        continent.GUID(trigger.Companion).collect {
          case boomer: BoomerDeployable =>
            boomer.Actor ! CommonMessages.Use(player, Some(trigger))
        }
      case Some(_) if player.VehicleSeated.isEmpty =>
        fireStateStopPlayerMessages(item_guid)
      case Some(_) =>
        fireStateStopMountedMessages(item_guid)
      case _ =>
        log.warn(s"ChangeFireState_Stop: can not find $item_guid")
    }
    sessionLogic.general.progressBarUpdate.cancel()
    sessionLogic.general.progressBarValue = None
  }

  def handleReload(pkt: ReloadMessage): Unit = {
    val ReloadMessage(item_guid, _, unk1) = pkt
    FindContainedWeapon match {
      case (Some(obj: Player), tools) =>
        handleReloadWhenPlayer(item_guid, obj, tools, unk1)
      case (Some(obj: PlanetSideServerObject with Container), tools) =>
        handleReloadWhenMountable(item_guid, obj, tools, unk1)
      case (_, _) =>
        log.warn(s"ReloadMessage: either can not find $item_guid or the object found was not a Tool")
    }
  }

  def handleChangeAmmo(pkt: ChangeAmmoMessage): Unit = {
    val ChangeAmmoMessage(item_guid, _) = pkt
    val (thing, equipment) = sessionLogic.findContainedEquipment()
    if (equipment.isEmpty) {
      log.warn(s"ChangeAmmo: either can not find $item_guid or the object found was not Equipment")
    } else {
      equipment foreach {
        case obj: ConstructionItem =>
          if (Deployables.performConstructionItemAmmoChange(player.avatar.certifications, obj, obj.AmmoTypeIndex)) {
            log.info(
              s"${player.Name} switched ${player.Sex.possessive} ${obj.Definition.Name} to construct ${obj.AmmoType} (option #${obj.FireModeIndex})"
            )
            sendResponse(ChangeAmmoMessage(obj.GUID, obj.AmmoTypeIndex))
          }
        case tool: Tool =>
          thing match {
            case Some(player: Player) =>
              PerformToolAmmoChange(tool, player, ModifyAmmunition(player))
            case Some(mountable: PlanetSideServerObject with Container) =>
              PerformToolAmmoChange(tool, mountable, ModifyAmmunitionInMountable(mountable))
            case _ =>
              log.warn(s"ChangeAmmo: the ${thing.get.Definition.Name} in ${player.Name}'s is not the correct type")
          }
        case obj =>
          log.warn(s"ChangeAmmo: the ${obj.Definition.Name} in ${player.Name}'s hands does not contain ammunition")
      }
    }
  }

  def handleChangeFireMode(pkt: ChangeFireModeMessage): Unit = {
    val ChangeFireModeMessage(item_guid, _/*fire_mode*/) = pkt
    sessionLogic.findEquipment(item_guid) match {
      case Some(obj: PlanetSideGameObject with FireModeSwitch[_]) =>
        val originalModeIndex = obj.FireModeIndex
        if (obj match {
          case citem: ConstructionItem =>
            val modeChanged = Deployables.performConstructionItemFireModeChange(
              player.avatar.certifications,
              citem,
              originalModeIndex
            )
            modeChanged
          case _ =>
            obj.NextFireMode
            obj.FireModeIndex != originalModeIndex
        }) {
          val modeIndex = obj.FireModeIndex
          obj match {
            case citem: ConstructionItem =>
              log.info(s"${player.Name} switched ${player.Sex.possessive} ${obj.Definition.Name} to construct ${citem.AmmoType} (mode #$modeIndex)")
            case _ =>
              log.info(s"${player.Name} changed ${player.Sex.possessive} her ${obj.Definition.Name}'s fire mode to #$modeIndex")
          }
          sendResponse(ChangeFireModeMessage(item_guid, modeIndex))
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.ChangeFireMode(player.GUID, item_guid, modeIndex)
          )
        }
      case Some(_) =>
        log.warn(s"ChangeFireMode: the object that was found for $item_guid does not possess fire modes")
      case None =>
        log.warn(s"ChangeFireMode: can not find $item_guid")
    }
  }

  def handleProjectileState(pkt: ProjectileStateMessage): Unit = {
    val ProjectileStateMessage(projectile_guid, shot_pos, shot_vel, shot_orient, seq, end, target_guid) = pkt
    val index = projectile_guid.guid - Projectile.baseUID
    projectiles(index) match {
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

  def handleLongRangeProjectileState(pkt: LongRangeProjectileInfoMessage): Unit = {
    val LongRangeProjectileInfoMessage(guid, _, _) = pkt
    FindContainedWeapon match {
      case (Some(_: Vehicle), weapons)
        if weapons.exists { _.GUID == guid } => () //now what?
      case _ => ()
    }
  }

  def handleDirectHit(pkt: HitMessage): Unit = {
    val HitMessage(
    _,
    projectile_guid,
    _,
    hit_info,
    _,
    _,
    _
    ) = pkt
    //find defined projectile
    FindProjectileEntry(projectile_guid) match {
      case Some(projectile) =>
        //find target(s)
        (hit_info match {
          case Some(hitInfo) =>
            val hitPos = hitInfo.hit_pos
            sessionLogic.validObject(hitInfo.hitobject_guid, decorator = "Hit/hitInfo") match {
              case _ if projectile.profile == GlobalDefinitions.flail_projectile =>
                val radius  = projectile.profile.DamageRadius * projectile.profile.DamageRadius
                val targets = Zone.findAllTargets(continent, player, hitPos, projectile.profile)
                  .filter { target =>
                    Vector3.DistanceSquared(target.Position, hitPos) <= radius
                  }
                targets.map { target =>
                  CheckForHitPositionDiscrepancy(projectile_guid, hitPos, target)
                  (target, projectile, hitPos, target.Position)
                }

              case Some(target: PlanetSideGameObject with FactionAffinity with Vitality) =>
                CheckForHitPositionDiscrepancy(projectile_guid, hitPos, target)
                List((target, projectile, hitInfo.shot_origin, hitPos))

              case None =>
                HandleDamageProxy(projectile, projectile_guid, hitPos)

              case _ =>
                Nil
            }
          case None =>
            Nil
        })
          .foreach {
            case (
              target: PlanetSideGameObject with FactionAffinity with Vitality,
              proj: Projectile,
              _: Vector3,
              hitPos: Vector3
              ) =>
              ResolveProjectileInteraction(proj, DamageResolution.Hit, target, hitPos).collect { resprojectile =>
                addShotsLanded(resprojectile.cause.attribution, shots = 1)
                sessionLogic.handleDealingDamage(target, resprojectile)
              }
            case _ => ()
          }
      case None =>
        log.warn(s"ResolveProjectile: expected projectile, but ${projectile_guid.guid} not found")
    }
  }

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
    FindProjectileEntry(projectile_guid) match {
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

  def handleLashHit(pkt: LashMessage): Unit = {
    val LashMessage(_, _, victim_guid, projectile_guid, hit_pos, _) = pkt
    sessionLogic.validObject(victim_guid, decorator = "Lash") match {
      case Some(target: PlanetSideGameObject with FactionAffinity with Vitality) =>
        CheckForHitPositionDiscrepancy(projectile_guid, hit_pos, target)
        ResolveProjectileInteraction(projectile_guid, DamageResolution.Lash, target, hit_pos).foreach {
          resprojectile =>
            addShotsLanded(resprojectile.cause.attribution, shots = 1)
            sessionLogic.handleDealingDamage(target, resprojectile)
        }
      case _ => ()
    }
  }

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

  def HandleWeaponFireOperations(
                                  weaponGUID: PlanetSideGUID,
                                  projectileGUID: PlanetSideGUID,
                                  shotOrigin: Vector3,
                                  shotVelocity: Option[Vector3]
                                ): Unit = {
    HandleWeaponFireAccountability(weaponGUID, projectileGUID) match {
      case (Some(obj), Some(tool)) =>
        val projectileIndex = projectileGUID.guid - Projectile.baseUID
        val projectilePlace = projectiles(projectileIndex)
        if (
          projectilePlace match {
            case Some(projectile) =>
              !projectile.isResolved && System.currentTimeMillis() - projectile.fire_time < projectile.profile.Lifespan.toLong
            case None =>
              false
          }
        ) {
          log.debug(
            s"WeaponFireMessage: overwriting unresolved projectile ${projectileGUID.guid}, known to ${player.Name}"
          )
        }
        val (angle, attribution, acceptableDistanceToOwner) = obj match {
          case p: Player =>
            (
              SimpleWorldEntity.validateOrientationEntry(
                p.Orientation + Vector3.z(p.FacingYawUpper)
              ),
              tool.Definition.ObjectId,
              10f + (if (p.Velocity.nonEmpty) {
                5f
              } else {
                0f
              })
            )
          case v: Vehicle if v.Definition.CanFly =>
            (tool.Orientation, obj.Definition.ObjectId, 1000f) //TODO this is too simplistic to find proper angle
          case _: Vehicle =>
            (tool.Orientation, obj.Definition.ObjectId, 225f) //TODO this is too simplistic to find proper angle
          case _ =>
            (obj.Orientation, obj.Definition.ObjectId, 300f)
        }
        val distanceToOwner = Vector3.DistanceSquared(shotOrigin, player.Position)
        if (distanceToOwner <= acceptableDistanceToOwner) {
          val projectile_info = tool.Projectile
          val wguid = weaponGUID.guid
          val mountedIn = (continent.turretToWeapon
            .find { case (guid, _) => guid == wguid } match {
            case Some((_, turretGuid)) => Some((
              turretGuid,
              continent.GUID(turretGuid).collect { case o: PlanetSideGameObject with FactionAffinity => SourceEntry(o) }
            ))
            case _                     => None
          }) match {
            case Some((guid, Some(entity))) => Some((guid, entity))
            case _                          => None
          }
          val projectile = new Projectile(
            projectile_info,
            tool.Definition,
            tool.FireMode,
            mountedIn,
            PlayerSource(player),
            attribution,
            shotOrigin,
            angle,
            shotVelocity
          )
          val initialQuality = tool.FireMode match {
            case mode: ChargeFireModeDefinition =>
              ProjectileQuality.Modified(
                {
                  val timeInterval = projectile.fire_time - shootingStart.getOrElse(tool.GUID, System.currentTimeMillis())
                  timeInterval.toFloat / mode.Time.toFloat
                }
              )
            case _ =>
              ProjectileQuality.Normal
          }
          val qualityprojectile = projectile.quality(initialQuality)
          qualityprojectile.WhichSide = player.WhichSide
          projectiles(projectileIndex) = Some(qualityprojectile)
          if (projectile_info.ExistsOnRemoteClients) {
            log.trace(
              s"WeaponFireMessage: ${player.Name}'s ${projectile_info.Name} is a remote projectile"
            )
            continent.Projectile ! ZoneProjectile.Add(player.GUID, qualityprojectile)
          }
        } else {
          log.warn(
            s"WeaponFireMessage: ${player.Name}'s ${tool.Definition.Name} projectile is too far from owner position at time of discharge ($distanceToOwner > $acceptableDistanceToOwner); suspect"
          )
        }

      case _ => ()
    }
  }

  def HandleWeaponFireAccountability(
                                      weaponGUID: PlanetSideGUID,
                                      projectileGUID: PlanetSideGUID
                                    ): (Option[PlanetSideGameObject with Container], Option[Tool]) = {
    if (player.ZoningRequest != Zoning.Method.None) {
      sessionLogic.zoning.CancelZoningProcessWithDescriptiveReason("cancel_fire")
    }
    if (player.isShielded) {
      // Cancel NC MAX shield if it's active
      sessionLogic.general.toggleMaxSpecialState(enable = false)
    }
    val (o, tools) = FindContainedWeapon
    val (_, enabledTools) = FindEnabledWeaponsToHandleWeaponFireAccountability(o, tools)
    if (enabledTools.size != tools.size) {
      o match {
        case Some(v: Vehicle) =>
          //assert subsystem states
          v.SubsystemMessages().foreach { sendResponse }
        case _ => ()
      }
    }
    if (enabledTools.nonEmpty) {
      val collectedTools = enabledTools.collect {
        case tool: Tool if tool.GUID == weaponGUID =>
          if (tool.Magazine <= 0) { //safety: enforce ammunition depletion
            prefire -= weaponGUID
            EmptyMagazine(weaponGUID, tool)
          } else if (!player.isAlive) { //proper internal accounting, but no projectile
            prefire += weaponGUID
            tool.Discharge()
            projectiles(projectileGUID.guid - Projectile.baseUID) = None
            shotsWhileDead += 1
          } else { //shooting
            if (
              avatar.stamina > 0 &&
                tool.FireModeIndex == 1 &&
                (tool.Definition.Name == "anniversary_guna"
                  || tool.Definition.Name == "anniversary_gun"
                  || tool.Definition.Name == "anniversary_gunb")
            ) {
              avatarActor ! AvatarActor.ConsumeStamina(avatar.stamina)
            }
            avatarActor ! AvatarActor.SuspendStaminaRegeneration(3.seconds)
            tool.Discharge()
            prefire += weaponGUID
            addShotsFired(tool.Definition.ObjectId, tool.AmmoSlot.Chamber)
          }
          (o, Some(tool))
      }
      collectedTools.headOption.getOrElse((None, None))
    } else {
      (None, None)
    }
  }

  def FindEnabledWeaponsToHandleWeaponFireAccountability(
                                                          o: Option[PlanetSideGameObject with Container],
                                                          tools: Set[Tool]
                                                        ): (Option[PlanetSideGameObject with Container], Set[Tool]) = {
    val enabledTools = o match {
      case Some(v: Vehicle)
        if GlobalDefinitions.isBattleFrameVehicle(v.Definition) =>
        val filteredTools = tools.filter { tool: Tool =>
          v.Weapons.find {
            case (index, slot) =>
              //arm mounted weapon?
              //index = 1 or 2 for bfr_flight; index = 2 3 or 4 for bfr_gunner
              index > 0 && index < 4 && slot.Equipment.nonEmpty && (tool eq slot.Equipment.get)
          } match {
            case Some((index, _)) =>
              val mountIsEnabled = v.Subsystems(if (v.Weapons.keys.min == index) {
                "BattleframeLeftArm"
              } else {
                "BattleframeRightArm"
              }).get.Enabled
              if (!mountIsEnabled) {
                //can't stop the local discharge, but it will not actually shoot anything; assert the magazine
                sendResponse(QuantityUpdateMessage(tool.AmmoSlot.Box.GUID, tool.Magazine))
              }
              mountIsEnabled
            case None =>
              //gunner mounted weapon?
              tool.Size == EquipmentSize.BFRGunnerWeapon
          }
        }
        filteredTools
      case Some(_) =>
        tools
      case None =>
        Set[Tool]()
    }
    (o, enabledTools)
  }

  /**
   * For a certain weapon that cna load ammunition, enforce that its magazine is empty.
   * @param weapon_guid the weapon
   */
  def EmptyMagazine(weapon_guid: PlanetSideGUID): Unit = {
    continent.GUID(weapon_guid) match {
      case Some(tool: Tool) =>
        EmptyMagazine(weapon_guid, tool)
      case _ => ()
    }
  }

  /**
   * For a certain weapon that can load ammunition, enforce that its magazine is empty.
   * Punctuate that emptiness with a ceasation of weapons fire and a dry fire sound effect.
   * @param weapon_guid the weapon (GUID)
   * @param tool the weapon (object)
   */
  def EmptyMagazine(weapon_guid: PlanetSideGUID, tool: Tool): Unit = {
    tool.Magazine = 0
    sendResponse(InventoryStateMessage(tool.AmmoSlot.Box.GUID, weapon_guid, 0))
    sendResponse(ChangeFireStateMessage_Stop(weapon_guid))
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.ChangeFireState_Stop(player.GUID, weapon_guid)
    )
    sendResponse(WeaponDryFireMessage(weapon_guid))
    continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.WeaponDryFire(player.GUID, weapon_guid))
  }

  /**
   * After a weapon has finished shooting, determine if it needs to be sorted in a special way.
   * @param tool a weapon
   */
  def FireCycleCleanup(tool: Tool): Unit = {
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
  def ModifyAmmunition(obj: PlanetSideGameObject with Container)(box: AmmoBox, reloadValue: Int): Unit = {
    val capacity = box.Capacity - reloadValue
    box.Capacity = capacity
    sendResponse(InventoryStateMessage(box.GUID, obj.GUID, capacity))
  }

  /**
   * Given a vehicle that contains a box of ammunition in its `Trunk` at a certain location,
   * change the amount of ammunition within that box.
   * @param obj the `Container`
   * @param box an `AmmoBox` to modify
   * @param reloadValue the value to modify the `AmmoBox`;
   *                    subtracted from the current `Capacity` of `Box`
   */
  def ModifyAmmunitionInMountable(obj: PlanetSideServerObject with Container)(box: AmmoBox, reloadValue: Int): Unit = {
    ModifyAmmunition(obj)(box, reloadValue)
    obj.Find(box).collect { index =>
      continent.VehicleEvents ! VehicleServiceMessage(
        s"${obj.Actor}",
        VehicleAction.InventoryState(
          player.GUID,
          box,
          obj.GUID,
          index,
          box.Definition.Packet.DetailedConstructorData(box).get
        )
      )
    }
  }

  /**
   * na
   * @param tool na
   * @param obj na
   */
  def PerformToolAmmoChange(
                             tool: Tool,
                             obj: PlanetSideServerObject with Container,
                             modifyFunc: (AmmoBox, Int) => Unit
                           ): Unit = {
    val originalAmmoType = tool.AmmoType
    do {
      val requestedAmmoType = tool.NextAmmoType
      val fullMagazine      = tool.MaxMagazine
      if (requestedAmmoType != tool.AmmoSlot.Box.AmmoType) {
        FindEquipmentStock(obj, FindAmmoBoxThatUses(requestedAmmoType), fullMagazine, CountAmmunition).reverse match {
          case Nil => ()
          case x :: xs =>
            val stowNewFunc: Equipment => TaskBundle = PutNewEquipmentInInventoryOrDrop(obj)
            val stowFunc: Equipment => Future[Any]   = PutEquipmentInInventoryOrDrop(obj)

            xs.foreach(item => {
              obj.Inventory -= item.start
              sendResponse(ObjectDeleteMessage(item.obj.GUID, 0))
              TaskWorkflow.execute(GUIDTask.unregisterObject(continent.GUID, item.obj))
            })

            //box will be the replacement ammo; give it the discovered magazine and load it into the weapon
            val box                 = x.obj.asInstanceOf[AmmoBox]
            //previousBox is the current magazine in tool; it will be removed from the weapon
            val previousBox         = tool.AmmoSlot.Box
            val originalBoxCapacity = box.Capacity
            val tailReloadValue: Int = if (xs.isEmpty) {
              0
            } else {
              xs.map(_.obj.asInstanceOf[AmmoBox].Capacity).sum
            }
            val sumReloadValue: Int = originalBoxCapacity + tailReloadValue
            val ammoSlotIndex       = tool.FireMode.AmmoSlotIndex
            val box_guid            = box.GUID
            val tool_guid           = tool.GUID
            obj.Inventory -= x.start //remove replacement ammo from inventory
            tool.AmmoSlots(ammoSlotIndex).Box = box //put replacement ammo in tool
            sendResponse(ObjectDetachMessage(tool_guid, previousBox.GUID, Vector3.Zero, 0f))
            sendResponse(ObjectDetachMessage(obj.GUID, box_guid, Vector3.Zero, 0f))
            sendResponse(ObjectAttachMessage(tool_guid, box_guid, ammoSlotIndex))

            //announce swapped ammunition box in weapon
            val previous_box_guid = previousBox.GUID
            val boxDef            = box.Definition
            sendResponse(ChangeAmmoMessage(tool_guid, box.Capacity))
            continent.AvatarEvents ! AvatarServiceMessage(
              continent.id,
              AvatarAction.ChangeAmmo(
                player.GUID,
                tool_guid,
                ammoSlotIndex,
                previous_box_guid,
                boxDef.ObjectId,
                box.GUID,
                boxDef.Packet.ConstructorData(box).get
              )
            )

            //handle inventory contents
            box.Capacity = if (sumReloadValue <= fullMagazine) {
              sumReloadValue
            } else {
              val splitReloadAmmo: Int = sumReloadValue - fullMagazine
              log.trace(
                s"PerformToolAmmoChange: ${player.Name} takes ${originalBoxCapacity - splitReloadAmmo} from a box of $originalBoxCapacity $requestedAmmoType ammo"
              )
              val boxForInventory = AmmoBox(box.Definition, splitReloadAmmo)
              TaskWorkflow.execute(stowNewFunc(boxForInventory))
              fullMagazine
            }
            sendResponse(
              InventoryStateMessage(box.GUID, tool.GUID, box.Capacity)
            ) //should work for both players and vehicles
            log.info(s"${player.Name} loads ${box.Capacity} $requestedAmmoType into the ${tool.Definition.Name}")
            if (previousBox.Capacity > 0) {
              //divide capacity across other existing and not full boxes of that ammo type
              var capacity = previousBox.Capacity
              val iter = obj.Inventory.Items
                .filter(entry => {
                  entry.obj match {
                    case item: AmmoBox =>
                      item.AmmoType == originalAmmoType && item.FullCapacity != item.Capacity
                    case _ =>
                      false
                  }
                })
                .sortBy(_.start)
                .iterator
              while (capacity > 0 && iter.hasNext) {
                val entry         = iter.next()
                val item: AmmoBox = entry.obj.asInstanceOf[AmmoBox]
                val ammoAllocated = math.min(item.FullCapacity - item.Capacity, capacity)
                log.info(s"${player.Name} put $ammoAllocated back into a box of ${item.Capacity} $originalAmmoType")
                capacity -= ammoAllocated
                modifyFunc(item, -ammoAllocated)
              }
              previousBox.Capacity = capacity
            }

            if (previousBox.Capacity > 0) {
              //split previousBox into AmmoBox objects of appropriate max capacity, e.g., 100 9mm -> 2 x 50 9mm
              obj.Inventory.Fit(previousBox) match {
                case Some(_) =>
                  stowFunc(previousBox)
                case None =>
                  sessionLogic.general.normalItemDrop(player, continent)(previousBox)
              }
              AmmoBox.Split(previousBox) match {
                case Nil | List(_) => () //done (the former case is technically not possible)
                case _ :: toUpdate =>
                  modifyFunc(previousBox, 0) //update to changed capacity value
                  toUpdate.foreach(box => { TaskWorkflow.execute(stowNewFunc(box)) })
              }
            } else {
              TaskWorkflow.execute(GUIDTask.unregisterObject(continent.GUID, previousBox))
            }
        }
      }
    } while (tool.AmmoType != originalAmmoType && tool.AmmoType != tool.AmmoSlot.Box.AmmoType)
  }

  /**
   * The main purpose of this method is to determine which targets will receive "locked on" warnings from remote projectiles.
   * For a given series of globally unique identifiers, indicating targets,
   * and that may include mounted elements (players),
   * estimate a series of channel names for communication with the vulnerable targets.
   * @param targets the globally unique identifiers of the immediate detected targets
   * @return channels names that allow direct communication to specific realized targets
   */
  def FindDetectedProjectileTargets(targets: Iterable[PlanetSideGUID]): Iterable[String] = {
    targets
      .map { sessionLogic.validObject(_, decorator="FindDetectedProjectileTargets") }
      .flatMap {
        case Some(obj: Vehicle) if !obj.Cloaked =>
          //TODO hint: vehicleService ! VehicleServiceMessage(s"${obj.Actor}", VehicleAction.ProjectileAutoLockAwareness(mode))
          obj.Seats.values.flatMap { seat => seat.occupants.map(_.Name) }
        case Some(obj: Mountable) =>
          obj.Seats.values.flatMap { seat => seat.occupants.map(_.Name) }
        case Some(obj: Player) if obj.ExoSuit == ExoSuitType.MAX =>
          Seq(obj.Name)
        case _ =>
          Seq.empty[String]
      }
  }

  def CheckForHitPositionDiscrepancy(
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
   * Given a globally unique identifier in the 40100 to 40124 range
   * (with an optional 25 as buffer),
   * find a projectile.
   * @param projectile_guid the projectile's GUID
   * @return the discovered projectile
   */
  def FindProjectileEntry(projectile_guid: PlanetSideGUID): Option[Projectile] = {
    val index = projectile_guid.guid - Projectile.baseUID
    if (0 <= index && index < projectiles.length) {
      projectiles(index)
    } else {
      log.trace(s"ResolveProjectile: ${player.Name} expected projectile, but ${projectile_guid.guid} not found")
      None
    }
  }

  /**
   * Find a projectile with the given globally unique identifier and mark it as a resolved shot.
   * A `Resolved` shot has either encountered an obstacle or is being cleaned up for not finding an obstacle.
   * @param projectile_guid the projectile GUID
   * @param resolution the resolution status to promote the projectile
   * @return the projectile
   */
  def ResolveProjectileInteraction(
                                    projectile_guid: PlanetSideGUID,
                                    resolution: DamageResolution.Value,
                                    target: PlanetSideGameObject with FactionAffinity with Vitality,
                                    pos: Vector3
                                  ): Option[DamageInteraction] = {
    FindProjectileEntry(projectile_guid) match {
      case Some(projectile) =>
        ResolveProjectileInteraction(projectile, resolution, target, pos)
      case None =>
        log.trace(s"ResolveProjectile: ${player.Name} expected projectile, but ${projectile_guid.guid} not found")
        None
    }
  }

  /**
   * Find a projectile with the given globally unique identifier and mark it as a resolved shot.
   * @param projectile the projectile object
   * @param index where the projectile was found
   * @param resolution the resolution status to promote the projectile
   * @return a copy of the projectile
   */
  def ResolveProjectileInteraction(
                                    projectile: Projectile,
                                    index: Int,
                                    resolution: DamageResolution.Value,
                                    target: PlanetSideGameObject with FactionAffinity with Vitality,
                                    pos: Vector3
                                  ): Option[DamageInteraction] = {
    if (!projectiles(index).contains(projectile)) {
      log.error(s"expected projectile could not be found at $index; can not resolve")
      None
    } else {
      ResolveProjectileInteraction(projectile, resolution, target, pos)
    }
  }

  /**
   * na
   * @param projectile the projectile object
   * @param resolution the resolution status to promote the projectile
   * @return a copy of the projectile
   */
  def ResolveProjectileInteraction(
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
  def HandleDamageProxy(
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

  def HandleDamageProxySetupLittleBuddy(listOfProjectiles: List[Projectile], detonationPosition: Vector3): Boolean = {
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

  def HandleDamageProxyLittleBuddyExplosion(proxy: Projectile, orientation: Vector3, distance: Float): Unit = {
    //explosion
    val obj = new DummyExplodingEntity(proxy, proxy.owner.Faction)
    obj.Position = obj.Position + orientation * distance
    val explosionFunc: ()=>Unit = WeaponAndProjectileOperations.detonateLittleBuddy(continent, obj, proxy, proxy.owner)
    context.system.scheduler.scheduleOnce(500.milliseconds) { explosionFunc() }
  }

  /**
   * Check two locations for a controlled piece of equipment that is associated with the `player`.
   * Filter for discovered `Tool`-type `Equipment`.
   * @return a `Tuple` of the returned values;
   *         the first value is a `Container` object;
   *         the second value is an `Tool` object in the former
   */
  def FindContainedWeapon: (Option[PlanetSideGameObject with Container], Set[Tool]) = {
    sessionLogic.findContainedEquipment() match {
      case (container, equipment) =>
        (container, equipment collect { case t: Tool => t })
      case _ =>
        (None, Set.empty)
    }
  }

  /**
   * Check two locations for a controlled piece of equipment that is associated with the `player`.
   * Filter for discovered `Tool`-type `Equipment` with a specific global unique identifier number.
   * @return a `Tuple` of the returned values;
   *         the first value is a `Container` object;
   *         the second value is an `Tool` object in the former
   */
  def FindContainedWeapon(
                           guid: PlanetSideGUID
                         ): (Option[PlanetSideGameObject with Container], Set[Tool]) = {
    val (o, equipment) = FindContainedWeapon
    equipment.find { _.GUID == guid } match {
      case Some(equip) => (o, Set(equip))
      case None        => (None, Set.empty)
    }
  }

  /**
   * Runs `FindContainedWeapon` but ignores the `Container` object output.
   * @return a `Tool` object
   */
  def FindWeapon: Set[Tool] = FindContainedWeapon._2

  /*
  used by ChangeFireStateMessage_Start handling
   */
  private def fireStateStartSetup(itemGuid: PlanetSideGUID): Unit = {
    prefire -= itemGuid
    shooting += itemGuid
    shootingStart += itemGuid -> System.currentTimeMillis()
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

  private def allowFireStateChangeStart(tool: Tool, itemGuid: PlanetSideGUID): Boolean = {
    tool.FireMode.RoundsPerShot == 0 || tool.Magazine > 0 || prefire.contains(itemGuid)
  }

  private def enforceEmptyMagazine(tool: Tool, itemGuid: PlanetSideGUID): Unit = {
    log.warn(
      s"ChangeFireState_Start: ${player.Name}'s ${tool.Definition.Name} magazine was empty before trying to shoot"
    )
    EmptyMagazine(itemGuid, tool)
  }

  private def fireStateStartWhenPlayer(tool: Tool, itemGuid: PlanetSideGUID): Unit = {
    if (allowFireStateChangeStart(tool, itemGuid)) {
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
    if (allowFireStateChangeStart(tool, itemGuid)) {
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

  private def fireStateStopPlayerMessages(itemGuid: PlanetSideGUID): Unit = {
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.ChangeFireState_Stop(player.GUID, itemGuid)
    )
  }

  private def fireStateStopMountedMessages(itemGuid: PlanetSideGUID): Unit = {
    sessionLogic.findContainedEquipment()._1.collect {
      case turret: FacilityTurret if continent.map.cavern =>
        turret.Actor ! VanuSentry.ChangeFireStop
    }
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.ChangeFireState_Stop(player.GUID, itemGuid)
    )
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
    fireStateStopPlayerMessages(itemGuid)
  }

  private def fireStateStopWhenMounted(tool: Tool, itemGuid: PlanetSideGUID): Unit = {
    fireStateStopUpdateChargeAndCleanup(tool)
    fireStateStopMountedMessages(itemGuid)
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

  private def handleReloadProcedure(
                                     itemGuid: PlanetSideGUID,
                                     obj: PlanetSideGameObject with Container,
                                     tools: Set[Tool],
                                     unk1: Int,
                                     deleteFunc: Equipment => Future[Any],
                                     modifyFunc: (AmmoBox, Int) => Unit,
                                     messageFunc: PlanetSideGUID => Unit
                                   ): Unit = {
    tools
      .filter { _.GUID == itemGuid }
      .foreach { tool =>
        val currentMagazine : Int = tool.Magazine
        val magazineSize : Int = tool.MaxMagazine
        val reloadValue : Int = magazineSize - currentMagazine
        if (magazineSize > 0 && reloadValue > 0) {
          FindEquipmentStock(obj, FindAmmoBoxThatUses(tool.AmmoType), reloadValue, CountAmmunition).reverse match {
            case Nil => ()
            case x :: xs =>
              xs.foreach { item => deleteFunc(item.obj) }
              val box = x.obj.asInstanceOf[AmmoBox]
              val tailReloadValue : Int = if (xs.isEmpty) {
                0
              }
              else {
                xs.map(_.obj.asInstanceOf[AmmoBox].Capacity).sum
              }
              val sumReloadValue : Int = box.Capacity + tailReloadValue
              val actualReloadValue = if (sumReloadValue <= reloadValue) {
                deleteFunc(box)
                sumReloadValue
              }
              else {
                modifyFunc(box, reloadValue - tailReloadValue)
                reloadValue
              }
              val finalReloadValue = actualReloadValue + currentMagazine
              log.info(
                s"${player.Name} successfully reloaded $reloadValue ${tool.AmmoType} into ${tool.Definition.Name}"
              )
              tool.Magazine = finalReloadValue
              sendResponse(ReloadMessage(itemGuid, finalReloadValue, unk1))
              messageFunc(itemGuid)
          }
        } else {
          //the weapon can not reload due to full magazine; the UI for the magazine is obvious bugged, so fix it
          sendResponse(QuantityUpdateMessage(tool.AmmoSlot.Box.GUID, magazineSize))
        }
      }
  }

  private def handleReloadWhenPlayer(
                                      itemGuid: PlanetSideGUID,
                                      obj: Player,
                                      tools: Set[Tool],
                                      unk1: Int
                                    ): Unit = {
    handleReloadProcedure(
      itemGuid,
      obj,
      tools,
      unk1,
      RemoveOldEquipmentFromInventory(obj)(_),
      ModifyAmmunition(obj)(_, _),
      reloadPlayerMessages
    )
  }

  private def handleReloadWhenMountable(
                                         itemGuid: PlanetSideGUID,
                                         obj: PlanetSideServerObject with Container,
                                         tools: Set[Tool],
                                         unk1: Int
                                       ): Unit = {
    handleReloadProcedure(
      itemGuid,
      obj,
      tools,
      unk1,
      RemoveOldEquipmentFromInventory(obj)(_),
      ModifyAmmunitionInMountable(obj)(_, _),
      reloadVehicleMessages
    )
  }

  private def addShotsFired(weaponId: Int, shots: Int): Unit = {
    addShotsToMap(shotsFired, weaponId, shots)
  }

  //noinspection SameParameterValue
  private def addShotsLanded(weaponId: Int, shots: Int): Unit = {
    addShotsToMap(shotsLanded, weaponId, shots)
  }

  private def addShotsToMap(map: mutable.HashMap[Int, Int], weaponId: Int, shots: Int): Unit = {
    map.put(
      weaponId,
      map.get(weaponId) match {
        case Some(previousShots) => previousShots + shots
        case None                => shots
      }
    )
  }

  private[support] def reportOngoingShots(reportFunc: (Long, Int, Int, Int) => Unit): Unit = {
    reportOngoingShots(player.CharId, reportFunc)
  }

  private[support] def reportOngoingShots(avatarId: Long, reportFunc: (Long, Int, Int, Int) => Unit): Unit = {
    //only shots that have been reported as fired count
    //if somehow shots had reported as landed but never reported as fired, they are ignored
    //these are just raw counts; there's only numeric connection between the entries of fired and of landed
    shotsFired.foreach { case (weaponId, fired) =>
      val landed = math.min(shotsLanded.getOrElse(weaponId, 0), fired)
      reportFunc(avatarId, weaponId, fired, landed)
    }
    shotsFired.clear()
    shotsLanded.clear()
  }

  //noinspection ScalaUnusedSymbol
  private[support] def reportOngoingShotsToAvatar(avatarId: Long, weaponId: Int, fired: Int, landed: Int): Unit = {
    avatarActor ! AvatarActor.UpdateToolDischarge(EquipmentStat(weaponId, fired, landed, 0, 0))
  }

  private[support] def reportOngoingShotsToDatabase(avatarId: Long, weaponId: Int, fired: Int, landed: Int): Unit = {
    ToDatabase.reportToolDischarge(avatarId, EquipmentStat(weaponId, fired, landed, 0, 0))
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
        val intersectTest = quickLineSphereIntersectionPoints(
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

  override protected[support] def actionsToCancel(): Unit = {
    shootingStart.clear()
    shootingStop.clear()
    (prefire ++ shooting).foreach { guid =>
      sendResponse(ChangeFireStateMessage_Stop(guid))
      continent.AvatarEvents ! AvatarServiceMessage(
        continent.id,
        AvatarAction.ChangeFireState_Stop(player.GUID, guid)
      )
    }
    prefire.clear()
    shooting.clear()
  }

  override protected[support] def stop(): Unit = {
    if (player != null && player.HasGUID) {
      (prefire ++ shooting).foreach { guid =>
        //do I need to do this? (maybe)
        fireStateStopPlayerMessages(guid)
        fireStateStopMountedMessages(guid)
      }
      projectiles.indices.foreach { projectiles.update(_, None) }
      reportOngoingShots(reportOngoingShotsToDatabase)
    }
  }
}

object WeaponAndProjectileOperations {
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
