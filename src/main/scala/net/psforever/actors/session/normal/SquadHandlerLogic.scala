// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.SessionSquadHandlers.{rethrowSquadServiceResponse, SquadUIElement}
import net.psforever.actors.session.support.{SessionData, SessionSquadHandlers, SpawnOperations, SquadHandlerFunctions}
import net.psforever.objects.{Default, LivePlayerList}
import net.psforever.objects.avatar.Avatar
import net.psforever.packet.game.{CharacterKnowledgeInfo, CharacterKnowledgeMessage, ChatMsg, MemberEvent, PlanetsideAttributeMessage, ReplicationStreamMessage, SquadAction, SquadDefinitionActionMessage, SquadDetailDefinitionUpdateMessage, SquadListing, SquadMemberEvent, SquadMembershipRequest, SquadMembershipResponse, SquadState, SquadStateInfo, SquadWaypointEvent, SquadWaypointRequest, WaypointEvent, WaypointEventAction}
import net.psforever.services.chat.SquadChannel
import net.psforever.services.teamwork.{SquadResponse, SquadServiceMessage, SquadServiceResponse, SquadAction => SquadServiceAction}
import net.psforever.types.{ChatMessageType, PlanetSideGUID, SquadListDecoration, SquadResponseType, WaypointSubtype}

object SquadHandlerLogic {
  def apply(ops: SessionSquadHandlers): SquadHandlerLogic = {
    new SquadHandlerLogic(ops, ops.context)
  }

  def rethrowSquadServiceResponse(response: SquadResponse.Response)(sessionLogic: SessionData): Unit = {
    sessionLogic.context.self ! SquadServiceResponse("", response)
  }
}

class SquadHandlerLogic(val ops: SessionSquadHandlers, implicit val context: ActorContext) extends SquadHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  private val squadService: ActorRef = ops.squadService

  /* packet */

  def handleSquadDefinitionAction(pkt: SquadDefinitionActionMessage): Unit = {
    val SquadDefinitionActionMessage(u1, u2, action) = pkt
    squadService ! SquadServiceMessage(player, continent, SquadServiceAction.Definition(u1, u2, action))
  }

  def handleSquadMemberRequest(pkt: SquadMembershipRequest): Unit = {
    val SquadMembershipRequest(request_type, char_id, unk3, player_name, unk5) = pkt
    squadService ! SquadServiceMessage(
      player,
      continent,
      SquadServiceAction.Membership(request_type, char_id, unk3, player_name, unk5)
    )
  }

  def handleSquadWaypointRequest(pkt: SquadWaypointRequest): Unit = {
    val SquadWaypointRequest(request, _, wtype, unk, info) = pkt
    val time = System.currentTimeMillis()
    val subtype = wtype.subtype
    if(subtype == WaypointSubtype.Squad) {
      squadService ! SquadServiceMessage(player, continent, SquadServiceAction.Waypoint(request, wtype, unk, info))
    } else if (subtype == WaypointSubtype.Laze && time - waypointCooldown > 1000) {
      //guarding against duplicating laze waypoints
      waypointCooldown = time
      squadService ! SquadServiceMessage(player, continent, SquadServiceAction.Waypoint(request, wtype, unk, info))
    }
  }

  /* response handlers */

  def handle(response: SquadResponse.Response, excluded: Iterable[Long]): Unit = {
    if (!excluded.exists(_ == avatar.id)) {
      response match {
        /* these messages will never be queued for later */
        case SquadResponse.ListSquadFavorite(line, task) =>
          sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), line, SquadAction.ListSquadFavorite(task)))

        case SquadResponse.InitList(infos) =>
          sendResponse(ReplicationStreamMessage(infos))

        case SquadResponse.UpdateList(infos) if infos.nonEmpty =>
          sendResponse(
            ReplicationStreamMessage(
              6,
              None,
              infos.map {
                case (index, squadInfo) =>
                  SquadListing(index, squadInfo)
              }.toVector
            )
          )

        case SquadResponse.RemoveFromList(infos) if infos.nonEmpty =>
          sendResponse(
            ReplicationStreamMessage(
              1,
              None,
              infos.map { index =>
                SquadListing(index, None)
              }.toVector
            )
          )

        case SquadResponse.SquadDecoration(guid, squad) =>
          val decoration = if (
            ops.squadUI.nonEmpty ||
              squad.Size == squad.Capacity ||
              {
                val offer = avatar.certifications
                !squad.Membership.exists { _.isAvailable(offer) }
              }
          ) {
            SquadListDecoration.NotAvailable
          } else {
            SquadListDecoration.Available
          }
          sendResponse(SquadDefinitionActionMessage(guid, 0, SquadAction.SquadListDecorator(decoration)))

        case SquadResponse.Detail(guid, detail) =>
          sendResponse(SquadDetailDefinitionUpdateMessage(guid, detail))

        case SquadResponse.SetListSquad(squad_guid) =>
          sendResponse(SquadDefinitionActionMessage(squad_guid, 0, SquadAction.SetListSquad()))

        case SquadResponse.SquadSearchResults(results) =>
        //TODO positive squad search results message?
          if(results.nonEmpty) {
            results.foreach { guid =>
              sendResponse(SquadDefinitionActionMessage(
                guid,
                0,
                SquadAction.SquadListDecorator(SquadListDecoration.SearchResult))
              )
            }
          } else {
            sendResponse(SquadDefinitionActionMessage(player.GUID, 0, SquadAction.NoSquadSearchResults()))
          }
          sendResponse(SquadDefinitionActionMessage(player.GUID, 0, SquadAction.CancelSquadSearch()))

        case SquadResponse.UpdateMembers(_, positions) =>
          val pairedEntries = positions.collect {
            case entry if ops.squadUI.contains(entry.char_id) =>
              (entry, ops.squadUI(entry.char_id))
          }
          //prune entries
          val updatedEntries = pairedEntries
            .collect({
              case (entry, element) if entry.zone_number != element.zone =>
                //zone gets updated for these entries
                sendResponse(
                  SquadMemberEvent.UpdateZone(ops.squad_supplement_id, entry.char_id, element.index, entry.zone_number)
                )
                ops.squadUI(entry.char_id) =
                  SquadUIElement(element.name, element.outfit, element.index, entry.zone_number, entry.health, entry.armor, entry.pos)
                entry
              case (entry, element)
                if entry.health != element.health || entry.armor != element.armor || entry.pos != element.position =>
                //other elements that need to be updated
                ops.squadUI(entry.char_id) =
                  SquadUIElement(element.name, element.outfit, element.index, entry.zone_number, entry.health, entry.armor, entry.pos)
                entry
            })
            .filterNot(_.char_id == avatar.id) //we want to update our backend, but not our frontend
          if (updatedEntries.nonEmpty) {
            sendResponse(
              SquadState(
                PlanetSideGUID(ops.squad_supplement_id),
                updatedEntries.map { entry =>
                  SquadStateInfo(entry.char_id, entry.health, entry.armor, entry.pos)
                }
              )
            )
          }

        /* queue below messages for later if the initial conditions are inappropriate */
        case msg if !sessionLogic.zoning.spawn.startEnqueueSquadMessages =>
          sessionLogic.zoning.spawn.enqueueNewActivity(
            SpawnOperations.ActivityQueuedTask(rethrowSquadServiceResponse(msg), 1)
          )

        /* these messages will be queued for later if initial conditions are inappropriate */
        case SquadResponse.IdentifyAsSquadLeader(squad_guid) =>
          sendResponse(SquadDefinitionActionMessage(squad_guid, 0, SquadAction.IdentifyAsSquadLeader()))

        case SquadResponse.Membership(request_type, unk1, unk2, charId, opt_char_id, player_name, unk5, unk6) =>
          val name = request_type match {
            case SquadResponseType.Invite if unk5 =>
              //the name of the player indicated by unk3 is needed
              LivePlayerList.WorldPopulation({ case (_, a: Avatar) => charId == a.id }).headOption match {
                case Some(player) =>
                  player.name
                case None =>
                  player_name
              }
            case _ =>
              player_name
          }
          sendResponse(SquadMembershipResponse(request_type, unk1, unk2, charId, opt_char_id, name, unk5, unk6))

        case SquadResponse.WantsSquadPosition(_, name) =>
          sendResponse(
            ChatMsg(
              ChatMessageType.CMT_SQUAD,
              wideContents=true,
              name,
              s"\\#6 would like to join your squad. (respond with \\#3/accept\\#6 or \\#3/reject\\#6)",
              None
            )
          )

        case SquadResponse.Join(squad, positionsToUpdate, _, ref) =>
          val avatarId = avatar.id
          val membershipPositions = (positionsToUpdate map squad.Membership.zipWithIndex)
            .filter { case (mem, index) =>
              mem.CharId > 0 && positionsToUpdate.contains(index)
            }
          membershipPositions.find { case (mem, _) => mem.CharId == avatarId } match {
            case Some((ourMember, ourIndex)) =>
              //we are joining the squad
              //load each member's entry (our own too)
              ops.squad_guid = squad.GUID
              ops.squad_supplement_id = squad.GUID.guid + 1
              membershipPositions.foreach {
                case (member, index) =>
                  sendResponse(
                    SquadMemberEvent.Add(
                      ops.squad_supplement_id,
                      member.CharId,
                      index,
                      member.Name,
                      member.ZoneId,
                      outfit_id = 0
                    )
                  )
                  ops.squadUI(member.CharId) =
                    SquadUIElement(member.Name, outfit=0L, index, member.ZoneId, member.Health, member.Armor, member.Position)
              }
              //repeat our entry
              sendResponse(
                SquadMemberEvent.Add(
                  ops.squad_supplement_id,
                  ourMember.CharId,
                  ourIndex,
                  ourMember.Name,
                  ourMember.ZoneId,
                  outfit_id = 0
                )
              )
              //turn lfs off
              if (avatar.lookingForSquad) {
                avatarActor ! AvatarActor.SetLookingForSquad(false)
              }
              val playerGuid = player.GUID
              val factionChannel = s"${player.Faction}"
              //squad colors
              ops.GiveSquadColorsToMembers()
              ops.GiveSquadColorsForOthers(playerGuid, factionChannel, ops.squad_supplement_id)
              //associate with member position in squad
              sendResponse(PlanetsideAttributeMessage(playerGuid, 32, ourIndex))
              //a finalization? what does this do?
              sendResponse(SquadDefinitionActionMessage(squad.GUID, 0, SquadAction.Unknown(18)))
              squadService ! SquadServiceMessage(player, continent, SquadServiceAction.ReloadDecoration())
              ops.updateSquadRef = ref
              ops.updateSquad = ops.PeriodicUpdatesWhenEnrolledInSquad
              sessionLogic.chat.JoinChannel(SquadChannel(squad.GUID))
            case _ =>
              //other player is joining our squad
              //load each member's entry
              ops.GiveSquadColorsToMembers(
                membershipPositions.map {
                  case (member, index) =>
                    val charId = member.CharId
                    sendResponse(
                      SquadMemberEvent.Add(ops.squad_supplement_id, charId, index, member.Name, member.ZoneId, outfit_id = 0)
                    )
                    ops.squadUI(charId) =
                      SquadUIElement(member.Name, outfit=0L, index, member.ZoneId, member.Health, member.Armor, member.Position)
                    charId
                }
              )
          }
          //send an initial dummy update for map icon(s)
          sendResponse(
            SquadState(
              PlanetSideGUID(ops.squad_supplement_id),
              membershipPositions.map { case (member, _) =>
                SquadStateInfo(member.CharId, member.Health, member.Armor, member.Position)
              }
            )
          )

        case SquadResponse.Leave(squad, positionsToUpdate) =>
          positionsToUpdate.find({ case (member, _) => member == avatar.id }) match {
            case Some((ourMember, ourIndex)) =>
              //we are leaving the squad
              //remove each member's entry (our own too)
              ops.updateSquadRef = Default.Actor
              positionsToUpdate.foreach {
                case (member, index) =>
                  sendResponse(SquadMemberEvent.Remove(ops.squad_supplement_id, member, index))
                  ops.squadUI.remove(member)
              }
              //uninitialize
              val playerGuid = player.GUID
              sendResponse(SquadMemberEvent.Remove(ops.squad_supplement_id, ourMember, ourIndex)) //repeat of our entry
              ops.GiveSquadColorsToSelf(value = 0)
              sendResponse(PlanetsideAttributeMessage(playerGuid, 32, 0))           //disassociate with member position in squad?
              sendResponse(PlanetsideAttributeMessage(playerGuid, 34, 4294967295L)) //unknown, perhaps unrelated?
              avatarActor ! AvatarActor.SetLookingForSquad(false)
              //a finalization? what does this do?
              sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.Unknown(18)))
              ops.squad_guid = PlanetSideGUID(0)
              ops.squad_supplement_id = 0
              ops.squadUpdateCounter = 0
              ops.updateSquad = ops.NoSquadUpdates
              sessionLogic.chat.LeaveChannel(SquadChannel(squad.GUID))
            case _ =>
              //remove each member's entry
              ops.GiveSquadColorsToMembers(
                positionsToUpdate.map {
                  case (member, index) =>
                    sendResponse(SquadMemberEvent.Remove(ops.squad_supplement_id, member, index))
                    ops.squadUI.remove(member)
                    member
                },
                value = 0
              )
          }

        case SquadResponse.AssignMember(squad, from_index, to_index) =>
          //we've already swapped position internally; now we swap the cards
          ops.SwapSquadUIElements(squad, from_index, to_index)

        case SquadResponse.PromoteMember(squad, promotedPlayer, from_index) =>
          if (promotedPlayer != player.CharId) {
            //demoted from leader; no longer lfsm
            if (player.avatar.lookingForSquad) {
              avatarActor ! AvatarActor.SetLookingForSquad(false)
            }
          }
          sendResponse(SquadMemberEvent(MemberEvent.Promote, squad.GUID.guid, promotedPlayer, position = 0))
          //the players have already been swapped in the backend object
          ops.PromoteSquadUIElements(squad, from_index)

        case SquadResponse.CharacterKnowledge(charId, name, certs, u1, u2, zone) =>
          sendResponse(CharacterKnowledgeMessage(charId, Some(CharacterKnowledgeInfo(name, certs, u1, u2, zone))))

        case SquadResponse.InitWaypoints(char_id, waypoints) =>
          waypoints.foreach {
            case (waypoint_type, info, unk) =>
              sendResponse(
                SquadWaypointEvent.Add(
                  ops.squad_supplement_id,
                  char_id,
                  waypoint_type,
                  WaypointEvent(info.zone_number, info.pos, unk)
                )
              )
          }

        case SquadResponse.WaypointEvent(WaypointEventAction.Add, char_id, waypoint_type, _, Some(info), unk) =>
          sendResponse(
            SquadWaypointEvent.Add(
              ops.squad_supplement_id,
              char_id,
              waypoint_type,
              WaypointEvent(info.zone_number, info.pos, unk)
            )
          )

        case SquadResponse.WaypointEvent(WaypointEventAction.Remove, char_id, waypoint_type, _, _, _) =>
          sendResponse(SquadWaypointEvent.Remove(ops.squad_supplement_id, char_id, waypoint_type))

        case SquadResponse.SquadRelatedComment(comment, messageType) =>
          sendResponse(ChatMsg(messageType, comment))

        case _ => ()
      }
    }
  }
}
