// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, typed}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.{Default, PlanetSideGameObject}
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.objects.zones.exp
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage, AvatarServiceResponse}

import scala.collection.mutable
//
import net.psforever.actors.session.AvatarActor
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.packet.game._
import net.psforever.services.avatar.AvatarResponse
import net.psforever.types._
import net.psforever.util.Config

trait AvatarHandlerFunctions extends CommonSessionInterfacingFunctionality {
  val ops: SessionAvatarHandlers

  def handle(toChannel: String, guid: PlanetSideGUID, reply: AvatarResponse.Response): Unit
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
    //TODO squad services deactivated, participation trophy rewards for now - 11-20-2023
    //must be in a squad to earn experience
    val charId = player.CharId
    val squadUI = sessionLogic.squad.squadUI
    val participation = continent
      .Building(buildingId)
      .map { building =>
        building.Participation.PlayerContribution()
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
            .map { case (id, _) => (id, squadParticipation.getOrElse(id, 0f) / 10f) }
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
          exp.ToDatabase.reportFacilityCapture(charId, buildingId, zoneNumber, finalExp, expType="cep")
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
          exp.ToDatabase.reportFacilityCapture(charId, buildingId, zoneNumber, modifiedExp, expType="bep")
          avatarActor ! AvatarActor.AwardFacilityCaptureBep(modifiedExp)
          Some(modifiedExp)
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

  def revive(revivalTargetGuid: PlanetSideGUID): Unit = {
    val spawn = sessionLogic.zoning.spawn
    spawn.reviveTimer.cancel()
    spawn.reviveTimer = Default.Cancellable
    spawn.respawnTimer.cancel()
    spawn.respawnTimer = Default.Cancellable
    player.Revive
    val health = player.Health
    sendResponse(PlanetsideAttributeMessage(revivalTargetGuid, attribute_type=0, health))
    sendResponse(AvatarDeadStateMessage(DeadState.Alive, timer_max=0, timer=0, player.Position, player.Faction, unk5=true))
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.PlanetsideAttributeToAll(revivalTargetGuid, attribute_type=0, health)
    )
  }

  def killedWhileMounted(obj: PlanetSideGameObject with Mountable, playerGuid: PlanetSideGUID): Unit = {
    val playerName = player.Name
    //boot cadaver from mount on client
    context.self ! AvatarServiceResponse(
      playerName,
      Service.defaultPlayerGUID,
      AvatarResponse.SendResponse(
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
}

object SessionAvatarHandlers {
  private[session] case class LastUpstream(
                                            msg: Option[AvatarResponse.PlayerState],
                                            visible: Boolean,
                                            shooting: Option[PlanetSideGUID],
                                            time: Long
                                          )
}
