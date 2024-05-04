// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, typed}
import net.psforever.objects.zones.Zoning
import net.psforever.objects.serverobject.turret.VanuSentry
import net.psforever.objects.zones.exp.ToDatabase

import scala.collection.mutable
import scala.concurrent.duration._
//
import net.psforever.actors.session.AvatarActor
import net.psforever.objects.avatar.scoring.EquipmentStat
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.equipment.EquipmentSize
import net.psforever.objects.inventory.Container
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects._
import net.psforever.packet.game._
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{ExoSuitType, PlanetSideGUID}

trait WeaponAndProjectileFunctions extends CommonSessionInterfacingFunctionality {
  def ops: WeaponAndProjectileOperations

  def handleWeaponFire(pkt: WeaponFireMessage): Unit

  def handleWeaponDelayFire(pkt: WeaponDelayFireMessage): Unit

  def handleWeaponDryFire(pkt: WeaponDryFireMessage): Unit

  def handleWeaponLazeTargetPosition(pkt: WeaponLazeTargetPositionMessage): Unit

  def handleUplinkRequest(pkt: UplinkRequest): Unit

  def handleAvatarGrenadeState(pkt: AvatarGrenadeStateMessage): Unit

  def handleChangeFireStateStart(pkt: ChangeFireStateMessage_Start): Unit

  def handleChangeFireStateStop(pkt: ChangeFireStateMessage_Stop): Unit

  def handleReload(pkt: ReloadMessage): Unit

  def handleChangeAmmo(pkt: ChangeAmmoMessage): Unit

  def handleChangeFireMode(pkt: ChangeFireModeMessage): Unit

  def handleProjectileState(pkt: ProjectileStateMessage): Unit

  def handleLongRangeProjectileState(pkt: LongRangeProjectileInfoMessage): Unit

  def handleDirectHit(pkt: HitMessage): Unit

  def handleSplashHit(pkt: SplashHitMessage): Unit

  def handleLashHit(pkt: LashMessage): Unit

  def handleAIDamage(pkt: AIDamage): Unit
}

class WeaponAndProjectileOperations(
                                     val sessionLogic: SessionData,
                                     val avatarActor: typed.ActorRef[AvatarActor.Command],
                                     implicit val context: ActorContext
                                   ) extends CommonSessionInterfacingFunctionality {
  var shooting: mutable.Set[PlanetSideGUID] = mutable.Set.empty //ChangeFireStateMessage_Start
  var prefire: mutable.Set[PlanetSideGUID] = mutable.Set.empty //if WeaponFireMessage precedes ChangeFireStateMessage_Start
  private[session] var shootingStart: mutable.HashMap[PlanetSideGUID, Long] = mutable.HashMap[PlanetSideGUID, Long]()
  private[session] var shootingStop: mutable.HashMap[PlanetSideGUID, Long] = mutable.HashMap[PlanetSideGUID, Long]()
  private[session] val shotsFired: mutable.HashMap[Int,Int] = mutable.HashMap[Int,Int]()
  private[session] val shotsLanded: mutable.HashMap[Int,Int] = mutable.HashMap[Int,Int]()
  private[session] var shotsWhileDead: Int = 0
  private[session] val projectiles: Array[Option[Projectile]] = {
    Array.fill[Option[Projectile]](Projectile.rangeUID - Projectile.baseUID)(None)
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
            projectiles(projectileGUID.guid - Projectile.baseUID) = None
            (None, None)
          } else if (!player.isAlive) { //proper internal accounting, but no projectile
            prefire += weaponGUID
            tool.Discharge()
            projectiles(projectileGUID.guid - Projectile.baseUID) = None
            shotsWhileDead += 1
            (None, None)
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
            (o, Some(tool))
          }
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

  def fireStateStopPlayerMessages(itemGuid: PlanetSideGUID): Unit = {
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.ChangeFireState_Stop(player.GUID, itemGuid)
    )
  }

  def fireStateStopMountedMessages(itemGuid: PlanetSideGUID): Unit = {
    sessionLogic.findContainedEquipment()._1.collect {
      case turret: FacilityTurret if continent.map.cavern =>
        turret.Actor ! VanuSentry.ChangeFireStop
    }
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.ChangeFireState_Stop(player.GUID, itemGuid)
    )
  }

  private def addShotsFired(weaponId: Int, shots: Int): Unit = {
    addShotsToMap(shotsFired, weaponId, shots)
  }

  def addShotsToMap(map: mutable.HashMap[Int, Int], weaponId: Int, shots: Int): Unit = {
    map.put(
      weaponId,
      map.get(weaponId) match {
        case Some(previousShots) => previousShots + shots
        case None                => shots
      }
    )
  }

  private def reportOngoingShots(reportFunc: (Long, Int, Int, Int) => Unit): Unit = {
    reportOngoingShots(player.CharId, reportFunc)
  }

  private def reportOngoingShots(avatarId: Long, reportFunc: (Long, Int, Int, Int) => Unit): Unit = {
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

  private def reportOngoingShotsToDatabase(avatarId: Long, weaponId: Int, fired: Int, landed: Int): Unit = {
    ToDatabase.reportToolDischarge(avatarId, EquipmentStat(weaponId, fired, landed, 0, 0))
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

  override protected[session] def stop(): Unit = {
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
