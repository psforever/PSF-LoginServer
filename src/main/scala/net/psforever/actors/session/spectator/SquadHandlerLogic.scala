// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.{ActorContext, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.SessionSquadHandlers.{rethrowSquadServiceResponse, SquadUIElement}
import net.psforever.actors.session.support.{SessionData, SessionSquadHandlers, SpawnOperations, SquadHandlerFunctions}
import net.psforever.objects.{Default, LivePlayerList}
import net.psforever.objects.avatar.Avatar
import net.psforever.packet.game.{CharacterKnowledgeInfo, CharacterKnowledgeMessage, ChatMsg, PlanetsideAttributeMessage, ReplicationStreamMessage, SquadAction, SquadDefinitionActionMessage, SquadDetailDefinitionUpdateMessage, SquadListing, SquadMemberEvent, SquadMembershipRequest, SquadMembershipResponse, SquadState, SquadStateInfo, SquadWaypointEvent, SquadWaypointRequest, WaypointEventAction}
import net.psforever.services.chat.SquadChannel
import net.psforever.services.teamwork.SquadResponse
import net.psforever.types.{PlanetSideGUID, SquadListDecoration, SquadResponseType}

object SquadHandlerLogic {
  def apply(ops: SessionSquadHandlers): SquadHandlerLogic = {
    new SquadHandlerLogic(ops, ops.context)
  }
}

class SquadHandlerLogic(val ops: SessionSquadHandlers, implicit val context: ActorContext) extends SquadHandlerFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  private val avatarActor: typed.ActorRef[AvatarActor.Command] = ops.avatarActor

  /* packet */

  def handleSquadDefinitionAction(pkt: SquadDefinitionActionMessage): Unit = { /* intentionally blank */ }

  def handleSquadMemberRequest(pkt: SquadMembershipRequest): Unit = { /* intentionally blank */ }

  def handleSquadWaypointRequest(pkt: SquadWaypointRequest): Unit = { /* intentionally blank */ }

  /* response handlers */

  def handle(response: SquadResponse.Response, excluded: Iterable[Long]): Unit = {
    if (!excluded.exists(_ == avatar.id)) {
      response match {
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

        case SquadResponse.Detail(guid, detail) =>
          sendResponse(SquadDetailDefinitionUpdateMessage(guid, detail))

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

        case SquadResponse.CharacterKnowledge(charId, name, certs, u1, u2, zone) =>
          sendResponse(CharacterKnowledgeMessage(charId, Some(CharacterKnowledgeInfo(name, certs, u1, u2, zone))))

        case SquadResponse.WaypointEvent(WaypointEventAction.Remove, char_id, waypoint_type, _, _, _) =>
          sendResponse(SquadWaypointEvent.Remove(ops.squad_supplement_id, char_id, waypoint_type))

        case SquadResponse.SquadRelatedComment(comment, messageType) =>
          sendResponse(ChatMsg(messageType, comment))

        case _ => ()
      }
    }
  }
}
