// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, typed}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.{Default, PlanetSideGameObject, Player, Vehicle}
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry, UniquePlayer}
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.objects.zones.exp
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage, AvatarServiceResponse}
import net.psforever.services.base.EventResponse
import net.psforever.services.chat.OutfitChannel

import scala.collection.mutable
//
import net.psforever.actors.session.AvatarActor
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.packet.game._
import net.psforever.types._
import net.psforever.util.Config

trait AvatarHandlerFunctions extends CommonSessionInterfacingFunctionality {
  val ops: SessionAvatarHandlers

  def handle(toChannel: String, guid: PlanetSideGUID, reply: EventResponse): Unit
}

class SessionAvatarHandlers(
                             val sessionLogic: SessionData,
                             val avatarActor: typed.ActorRef[AvatarActor.Command],
                             implicit val context: ActorContext
                           ) extends CommonSessionInterfacingFunctionality {
  //TODO player characters only exist within a certain range of GUIDs for a given zone; this is overkill
  private[session] var lastSeenStreamMessage: mutable.LongMap[SessionAvatarHandlers.LastUpstream] =
    mutable.LongMap[SessionAvatarHandlers.LastUpstream]()
  private[session] val hidingPlayerRandomizer = new scala.util.Random

  def changeAmmoProcedures(
                                    weaponGuid: PlanetSideGUID,
                                    previousAmmoGuid: PlanetSideGUID,
                                    ammoTypeId: Int,
                                    ammoGuid: PlanetSideGUID,
                                    ammoSlot: Int,
                                    ammoData: ConstructorData
                                  ): Unit = {
    sendResponse(ObjectDetachMessage(weaponGuid, previousAmmoGuid, Vector3.Zero, 0))
    //TODO? sendResponse(ObjectDeleteMessage(previousAmmoGuid, 0))
    sendResponse(
      ObjectCreateMessage(
        ammoTypeId,
        ammoGuid,
        ObjectCreateMessageParent(weaponGuid, ammoSlot),
        ammoData
      )
    )
  }

  def facilityCaptureRewards(buildingId: Int, zoneNumber: Int, cep: Long): Unit = {
    //must be in a squad to earn experience
    val delay = continent.Building(buildingId).map(building => building.CaptureTerminal.get.Definition.FacilityHackTime.toSeconds / 6)
    val charId = player.CharId
    val squadUI = sessionLogic.squad.squadUI
    val participation = continent
      .Building(buildingId)
      .map { building =>
        building.Participation.PlayerContribution(delay.getOrElse(100))
      }
    squadUI
      .find { _._1 == charId }
      .collect {
        case (_, elem) if elem.index == 0 =>
          val cepConfig = Config.app.game.experience.cep
          //squad leader earns CEP, modified by squad effort, capped by squad size present during the capture
          val squadParticipation = participation match {
            case Some(map) => map.filter { case (id, _) => squadUI.contains(id) }
            case _ => Map.empty[Long, Float]
          }
          val maxCepBySquadSize: Long = {
            val maxCepList = cepConfig.maximumPerSquadSize
            val squadSize: Int = squadParticipation.size
            maxCepList.lift(squadSize - 1).getOrElse(squadSize * maxCepList.head).toLong
          }
          val groupContribution: Float = squadUI
            .map { case (id, _) => (id, squadParticipation.getOrElse(id, 0f)) }
            .values
            .max
          val modifiedExp: Long = (cep.toFloat * groupContribution).toLong
          val cappedModifiedExp: Long = math.min(modifiedExp, maxCepBySquadSize)
          val finalExp: Long = if (modifiedExp > cappedModifiedExp) {
            val overLimitOverflow = if (cepConfig.squadSizeLimitOverflow == -1) {
              cep.toFloat
            } else {
              cepConfig.squadSizeLimitOverflow.toFloat
            }
            cappedModifiedExp + (overLimitOverflow * (math.random().toFloat % cepConfig.squadSizeLimitOverflowMultiplier)).toLong
          } else {
            cappedModifiedExp
          }
          exp.ToDatabase.reportFacilityCapture(charId, zoneNumber, buildingId, finalExp, expType="cep")
          avatarActor ! AvatarActor.AwardCep(finalExp)
          Some(finalExp)

        case _ =>
          //squad member earns BEP based on CEP, modified by personal effort
          val individualContribution = {
            val contributionList = for {
              facilityMap <- participation
              if facilityMap.contains(charId)
            } yield facilityMap(charId)
            if (contributionList.nonEmpty) {
              contributionList.max
            } else {
              0f
            }
          }
          val modifiedExp = (cep * individualContribution).toLong
          val finalBep = math.min(modifiedExp, 2250L) // 2250 max bep for capture
          exp.ToDatabase.reportFacilityCapture(charId, zoneNumber, buildingId, finalBep, expType="bep")
          avatarActor ! AvatarActor.AwardFacilityCaptureBep(finalBep)
          Some(finalBep)
      }
  }

  /**
    *
    * @param killer the player who got the kill
    * @param exp the amount of bep they received for the kill
    * Squad members of a "killer" will receive a split of the experience if they are both alive
    * and in the same zone as the killer. The amount received is
    * based on the size of the squad. Each squad member that meets the criteria will receive a fractional split.
    */
  def shareKillExperienceWithSquad(killer: Player, exp: Long): Unit = {
    //TODO consider squad experience waypoint in exp calculation
    val squadUI = sessionLogic.squad.squadUI
    val squadSize = squadUI.size
    if (squadSize > 1) {
      val expSplit = exp / squadSize
      val squadMembers = squadUI.filterNot(_._1 == killer.CharId).map { case (_, member) => member }.toList.map(_.name)
      val playersInZone = killer.Zone.Players.map { avatar => (avatar.id, avatar.basic.name) }
      val squadMembersHere = playersInZone.filter(member => squadMembers.contains(member._2))
      squadMembersHere.foreach { member =>
        killer.Zone.AvatarEvents ! AvatarServiceMessage(
          member._2,
          AvatarAction.AwardBep(member._1, expSplit, ExperienceType.Normal))
      }
    }
  }

  def shareAntExperienceWithSquad(driver: UniquePlayer, exp: Long, vehicle: Vehicle): Unit = {
    val squadUI = sessionLogic.squad.squadUI
    val squadSize = squadUI.size
    if (squadSize > 1) {
      val squadMembers = squadUI.filterNot(_._1 == driver.charId).map { case (_, member) => member }.toList.map(_.name)
      val playersInZone = vehicle.Zone.Players.map { avatar => (avatar.id, avatar.basic.name) }
      val squadMembersHere = playersInZone.filter(member => squadMembers.contains(member._2))
      squadMembersHere.foreach { member =>
        vehicle.Zone.AvatarEvents ! AvatarServiceMessage(
          member._2,
          AvatarAction.AwardBep(member._1, exp, ExperienceType.Normal))
      }
    }
  }

  /**
   * Properly format a `DestroyDisplayMessage` packet
   * given sufficient information about a target (victim) and an actor (killer).
   * For the packet, the `charId` field is important for determining distinction between players.
   * @param killer the killer's entry
   * @param victim the victim's entry
   * @param method the manner of death
   * @param unk na;
   *            defaults to 121, the object id of `avatar`
   * @return a `DestroyDisplayMessage` packet that is properly formatted
   */
  def destroyDisplayMessage(
                             killer: SourceEntry,
                             victim: SourceEntry,
                             method: Int,
                             unk: Int = 121
                           ): DestroyDisplayMessage = {
    val killerSeated = killer match {
      case obj: PlayerSource => obj.Seated
      case _                 => false
    }
    val victimSeated = victim match {
      case obj: PlayerSource => obj.Seated
      case _                 => false
    }
    new DestroyDisplayMessage(
      killer.Name,
      killer.CharId,
      killer.Faction,
      killerSeated,
      unk,
      method,
      victim.Name,
      victim.CharId,
      victim.Faction,
      victimSeated
    )
  }

  def revive(): Unit = {
    val spawn = sessionLogic.zoning.spawn
    spawn.reviveTimer.cancel()
    spawn.reviveTimer = Default.Cancellable
    spawn.respawnTimer.cancel()
    spawn.respawnTimer = Default.Cancellable
  }

  def killedWhileMounted(obj: PlanetSideGameObject with Mountable, playerGuid: PlanetSideGUID): Unit = {
    val playerName = player.Name
    //boot cadaver from mount on client
    context.self ! AvatarServiceResponse(
      playerName,
      Service.defaultPlayerGUID,
      AvatarAction.SendResponse(
        ObjectDetachMessage(obj.GUID, playerGuid, player.Position, Vector3.Zero)
      )
    )
    //player no longer seated
    obj.PassengerInSeat(player).foreach { seatNumber =>
      //boot cadaver from mount internally (vehicle perspective)
      obj.Seats(seatNumber).unmount(player)
      //inform client-specific logic
      context.self ! Mountable.MountMessages(
        player,
        Mountable.CanDismount(obj, seatNumber, 0)
      )
    }
    player.VehicleSeated = None
  }

  def removeFromOutfitChat(outfit_id: Long): Unit = {
    sessionLogic.chat.LeaveChannel(OutfitChannel(outfit_id))
  }
}

object SessionAvatarHandlers {
  private[session] case class LastUpstream(
                                            msg: Option[AvatarAction.PlayerState],
                                            visible: Boolean,
                                            shooting: Option[PlanetSideGUID],
                                            time: Long
                                          )
}
