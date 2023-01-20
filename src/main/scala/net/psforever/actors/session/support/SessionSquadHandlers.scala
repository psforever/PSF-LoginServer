// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, ActorRef, typed}
import scala.collection.mutable
//
import net.psforever.actors.session.{AvatarActor, ChatActor}
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.teamwork.Squad
import net.psforever.objects.{Default, LivePlayerList, Player}
import net.psforever.packet.game._
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.chat.ChatService
import net.psforever.services.teamwork.{SquadResponse, SquadServiceMessage, SquadAction => SquadServiceAction}
import net.psforever.types.{ChatMessageType, PlanetSideEmpire, PlanetSideGUID, SquadListDecoration, SquadResponseType, Vector3, WaypointSubtype}

object SessionSquadHandlers {
  protected final case class SquadUIElement(
                                             name: String,
                                             outfit: Long,
                                             index: Int,
                                             zone: Int,
                                             health: Int,
                                             armor: Int,
                                             position: Vector3
                                           )
}

class SessionSquadHandlers(
                            val sessionData: SessionData,
                            avatarActor: typed.ActorRef[AvatarActor.Command],
                            chatActor: typed.ActorRef[ChatActor.Command],
                            squadService: ActorRef,
                            implicit val context: ActorContext
                          ) extends CommonSessionInterfacingFunctionality {
  import SessionSquadHandlers._

  private var waypointCooldown: Long = 0L
  val squadUI: mutable.LongMap[SquadUIElement] = new mutable.LongMap[SquadUIElement]()
  var squad_supplement_id: Int = 0
  /**
   * When joining or creating a squad, the original state of the avatar's internal LFS variable is blanked.
   * This `WorldSessionActor`-local variable is then used to indicate the ongoing state of the LFS UI component,
   * now called "Looking for Squad Member."
   * Only the squad leader may toggle the LFSM marquee.
   * Upon leaving or disbanding a squad, this value is made false.
   * Control switching between the `Avatar`-local and the `WorldSessionActor`-local variable is contingent on `squadUI` being populated.
   */
  private[support] var lfsm: Boolean = false
  private[support] var squadSetup: () => Unit = FirstTimeSquadSetup
  private var squadUpdateCounter: Int = 0
  private val queuedSquadActions: Seq[() => Unit] = Seq(SquadUpdates, NoSquadUpdates, NoSquadUpdates, NoSquadUpdates)
  private[support] var updateSquad: () => Unit = NoSquadUpdates
  private var updateSquadRef: ActorRef                                       = Default.Actor

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
            squadUI.nonEmpty ||
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

        case SquadResponse.IdentifyAsSquadLeader(squad_guid) =>
          sendResponse(SquadDefinitionActionMessage(squad_guid, 0, SquadAction.IdentifyAsSquadLeader()))

        case SquadResponse.SetListSquad(squad_guid) =>
          sendResponse(SquadDefinitionActionMessage(squad_guid, 0, SquadAction.SetListSquad()))

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
              squad_supplement_id = squad.GUID.guid + 1
              membershipPositions.foreach {
                case (member, index) =>
                  sendResponse(
                    SquadMemberEvent.Add(
                      squad_supplement_id,
                      member.CharId,
                      index,
                      member.Name,
                      member.ZoneId,
                      outfit_id = 0
                    )
                  )
                  squadUI(member.CharId) =
                    SquadUIElement(member.Name, outfit=0L, index, member.ZoneId, member.Health, member.Armor, member.Position)
              }
              //repeat our entry
              sendResponse(
                SquadMemberEvent.Add(
                  squad_supplement_id,
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
              GiveSquadColorsToMembers()
              GiveSquadColorsForOthers(playerGuid, factionChannel, squad_supplement_id)
              //associate with member position in squad
              sendResponse(PlanetsideAttributeMessage(playerGuid, 32, ourIndex))
              //a finalization? what does this do?
              sendResponse(SquadDefinitionActionMessage(squad.GUID, 0, SquadAction.Unknown(18)))
              squadService ! SquadServiceMessage(player, continent, SquadServiceAction.ReloadDecoration())
              updateSquadRef = ref
              updateSquad = PeriodicUpdatesWhenEnrolledInSquad
              chatActor ! ChatActor.JoinChannel(ChatService.ChatChannel.Squad(squad.GUID))
            case _ =>
              //other player is joining our squad
              //load each member's entry
              GiveSquadColorsToMembers(
                membershipPositions.map {
                  case (member, index) =>
                    val charId = member.CharId
                    sendResponse(
                      SquadMemberEvent.Add(squad_supplement_id, charId, index, member.Name, member.ZoneId, outfit_id = 0)
                    )
                    squadUI(charId) =
                      SquadUIElement(member.Name, outfit=0L, index, member.ZoneId, member.Health, member.Armor, member.Position)
                    charId
                }
              )
          }
          //send an initial dummy update for map icon(s)
          sendResponse(
            SquadState(
              PlanetSideGUID(squad_supplement_id),
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
              updateSquadRef = Default.Actor
              positionsToUpdate.foreach {
                case (member, index) =>
                  sendResponse(SquadMemberEvent.Remove(squad_supplement_id, member, index))
                  squadUI.remove(member)
              }
              //uninitialize
              val playerGuid = player.GUID
              sendResponse(SquadMemberEvent.Remove(squad_supplement_id, ourMember, ourIndex)) //repeat of our entry
              GiveSquadColorsToSelf(value = 0)
              sendResponse(PlanetsideAttributeMessage(playerGuid, 32, 0))           //disassociate with member position in squad?
              sendResponse(PlanetsideAttributeMessage(playerGuid, 34, 4294967295L)) //unknown, perhaps unrelated?
              lfsm = false
              avatarActor ! AvatarActor.SetLookingForSquad(false)
              //a finalization? what does this do?
              sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.Unknown(18)))
              squad_supplement_id = 0
              squadUpdateCounter = 0
              updateSquad = NoSquadUpdates
              chatActor ! ChatActor.LeaveChannel(ChatService.ChatChannel.Squad(squad.GUID))
            case _ =>
              //remove each member's entry
              GiveSquadColorsToMembers(
                positionsToUpdate.map {
                  case (member, index) =>
                    sendResponse(SquadMemberEvent.Remove(squad_supplement_id, member, index))
                    squadUI.remove(member)
                    member
                },
                value = 0
              )
          }

        case SquadResponse.AssignMember(squad, from_index, to_index) =>
          //we've already swapped position internally; now we swap the cards
          SwapSquadUIElements(squad, from_index, to_index)

        case SquadResponse.PromoteMember(squad, promotedPlayer, from_index) =>
          if (promotedPlayer != player.CharId) {
            //demoted from leader; no longer lfsm
            if (lfsm) {
              lfsm = false
              AvatarActor.displayLookingForSquad(session, state = 0)
            }
          }
          sendResponse(SquadMemberEvent(MemberEvent.Promote, squad.GUID.guid, promotedPlayer, position = 0))
          //the players have already been swapped in the backend object
          PromoteSquadUIElements(squad, from_index)

        case SquadResponse.UpdateMembers(_, positions) =>
          val pairedEntries = positions.collect {
            case entry if squadUI.contains(entry.char_id) =>
              (entry, squadUI(entry.char_id))
          }
          //prune entries
          val updatedEntries = pairedEntries
            .collect({
              case (entry, element) if entry.zone_number != element.zone =>
                //zone gets updated for these entries
                sendResponse(
                  SquadMemberEvent.UpdateZone(squad_supplement_id, entry.char_id, element.index, entry.zone_number)
                )
                squadUI(entry.char_id) =
                  SquadUIElement(element.name, element.outfit, element.index, entry.zone_number, entry.health, entry.armor, entry.pos)
                entry
              case (entry, element)
                if entry.health != element.health || entry.armor != element.armor || entry.pos != element.position =>
                //other elements that need to be updated
                squadUI(entry.char_id) =
                  SquadUIElement(element.name, element.outfit, element.index, entry.zone_number, entry.health, entry.armor, entry.pos)
                entry
            })
            .filterNot(_.char_id == avatar.id) //we want to update our backend, but not our frontend
          if (updatedEntries.nonEmpty) {
            sendResponse(
              SquadState(
                PlanetSideGUID(squad_supplement_id),
                updatedEntries.map { entry =>
                  SquadStateInfo(entry.char_id, entry.health, entry.armor, entry.pos)
                }
              )
            )
          }

        case SquadResponse.CharacterKnowledge(charId, name, certs, u1, u2, zone) =>
          sendResponse(CharacterKnowledgeMessage(charId, Some(CharacterKnowledgeInfo(name, certs, u1, u2, zone))))

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

        case SquadResponse.InitWaypoints(char_id, waypoints) =>
          waypoints.foreach {
            case (waypoint_type, info, unk) =>
              sendResponse(
                SquadWaypointEvent.Add(
                  squad_supplement_id,
                  char_id,
                  waypoint_type,
                  WaypointEvent(info.zone_number, info.pos, unk)
                )
              )
          }

        case SquadResponse.WaypointEvent(WaypointEventAction.Add, char_id, waypoint_type, _, Some(info), unk) =>
          sendResponse(
            SquadWaypointEvent.Add(
              squad_supplement_id,
              char_id,
              waypoint_type,
              WaypointEvent(info.zone_number, info.pos, unk)
            )
          )

        case SquadResponse.WaypointEvent(WaypointEventAction.Remove, char_id, waypoint_type, _, _, _) =>
          sendResponse(SquadWaypointEvent.Remove(squad_supplement_id, char_id, waypoint_type))

        case _ => ;
      }
    }
  }

  /**
   * These messages are dispatched when first starting up the client and connecting to the server for the first time.
   * While many of these messages will be reused for other situations, they appear in this order only during startup.
   */
  def FirstTimeSquadSetup(): Unit = {
    sendResponse(SquadDetailDefinitionUpdateMessage.Init)
    sendResponse(ReplicationStreamMessage(5, Some(6), Vector.empty)) //clear squad list
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.Unknown(6)))
    //only need to load these once - they persist between zone transfers and respawns
    avatar.loadouts.squad.zipWithIndex.foreach {
      case (Some(loadout), index) =>
        sendResponse(
          SquadDefinitionActionMessage(PlanetSideGUID(0), index, SquadAction.ListSquadFavorite(loadout.task))
        )
      case (None, _) => ;
    }
    //non-squad GUID-0 counts as the settings when not joined with a squad
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.IdentifyAsSquadLeader()))
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.SetListSquad()))
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.Unknown(18)))
    squadService ! SquadServiceMessage(player, continent, SquadServiceAction.InitSquadList())
    squadService ! SquadServiceMessage(player, continent, SquadServiceAction.InitCharId())
    squadSetup = RespawnSquadSetup
  }

  /**
   * These messages are used during each subsequent respawn to reset the squad colors on player nameplates and marquees.
   * By using `squadUI` to maintain relevant information about squad members,
   * especially the unique character identifier number,
   * only the zone-specific squad members will receive the important messages about their squad member's spawn.
   */
  def RespawnSquadSetup(): Unit = {
    if (squad_supplement_id > 0) {
      squadUI.get(player.CharId) match {
        case Some(elem) =>
          sendResponse(PlanetsideAttributeMessage(player.GUID, 31, squad_supplement_id))
          continent.AvatarEvents ! AvatarServiceMessage(
            s"${player.Faction}",
            AvatarAction.PlanetsideAttribute(player.GUID, 31, squad_supplement_id)
          )
          sendResponse(PlanetsideAttributeMessage(player.GUID, 32, elem.index))
        case _ =>
          log.warn(s"RespawnSquadSetup: asked to redraw squad information, but ${player.Name} has no squad element for squad $squad_supplement_id")
      }
    }
  }

  /**
   * These messages are used during each subsequent respawn to reset the squad colors on player nameplates and marquees.
   * During a zone change,
   * on top of other squad mates in the zone needing to have their knowledge of this player's squad colors changed,
   * the player must also set squad colors for each other squad members.
   * Default respawn functionality may resume afterwards.
   */
  def ZoneChangeSquadSetup(): Unit = {
    RespawnSquadSetup()
    squadService ! SquadServiceMessage(player, continent, SquadServiceAction.InitSquadList())
    GiveSquadColorsInZone()
    squadSetup = RespawnSquadSetup
  }

  def NoSquadUpdates(): Unit = {}

  def SquadUpdates(): Unit = {
    updateSquadRef ! SquadServiceMessage(
      player,
      continent,
      SquadServiceAction.Update(
        player.CharId,
        player.GUID,
        player.Health,
        player.MaxHealth,
        player.Armor,
        player.MaxArmor,
        player.avatar.certifications,
        player.Position,
        continent.Number
      )
    )
  }

  def PeriodicUpdatesWhenEnrolledInSquad(): Unit = {
    queuedSquadActions(squadUpdateCounter)()
    squadUpdateCounter = (squadUpdateCounter + 1) % queuedSquadActions.length
  }

  /**
   * Allocate all squad members in zone and give their nameplates and their marquees the appropriate squad color.
   */
  def GiveSquadColorsInZone(): Unit = {
    GiveSquadColorsInZone(squadUI.keys, squad_supplement_id)
  }

  /**
   * Allocate the listed squad members in zone and give their nameplates and their marquees the appropriate squad color.
   *
   * @param members members of the squad to target
   */
  def GiveSquadColorsInZone(members: Iterable[Long]): Unit = {
    GiveSquadColorsInZone(members, squad_supplement_id)
  }

  /**
   * Allocate the listed squad members in zone and give their nameplates and their marquees the appropriate squad color.
   *
   * @see `PlanetsideAttributeMessage`
   * @param members members of the squad to target
   * @param value   the assignment value
   */
  def GiveSquadColorsInZone(members: Iterable[Long], value: Long): Unit = {
    SquadMembersInZone(members).foreach { members =>
      sendResponse(PlanetsideAttributeMessage(members.GUID, 31, value))
    }
  }

  /**
   * For the listed squad member unique character identifier numbers,
   * find and return all squad members in the current zone.
   *
   * @param members members of the squad to target
   * @return a list of `Player` objects
   */
  def SquadMembersInZone(members: Iterable[Long]): Iterable[Player] = {
    val players = continent.LivePlayers
    for {
      charId <- members
      player = players.find {
        _.CharId == charId
      }
      if player.nonEmpty
    } yield player.get
  }

  def SwapSquadUIElements(squad: Squad, fromIndex: Int, toIndex: Int): Unit = {
    if (squadUI.nonEmpty) {
      val fromMember = squad.Membership(toIndex)   //the players have already been swapped in the backend object
      val fromCharId = fromMember.CharId
      val toMember   = squad.Membership(fromIndex) //the players have already been swapped in the backend object
      val toCharId   = toMember.CharId
      val id         = 11
      if (toCharId > 0) {
        //toMember and fromMember have swapped places
        val fromElem = squadUI(fromCharId)
        val toElem   = squadUI(toCharId)
        squadUI(toCharId) =
          SquadUIElement(fromElem.name, fromElem.outfit, toIndex, fromElem.zone, fromElem.health, fromElem.armor, fromElem.position)
        squadUI(fromCharId) =
          SquadUIElement(toElem.name, toElem.outfit, fromIndex, toElem.zone, toElem.health, toElem.armor, toElem.position)
        sendResponse(SquadMemberEvent.Add(id, toCharId, toIndex, fromElem.name, fromElem.zone, outfit_id = 0))
        sendResponse(SquadMemberEvent.Add(id, fromCharId, fromIndex, toElem.name, toElem.zone, outfit_id = 0))
        sendResponse(
          SquadState(
            PlanetSideGUID(id),
            List(
              SquadStateInfo(fromCharId, toElem.health, toElem.armor, toElem.position, 2, 2, unk6=false, 429, None, None),
              SquadStateInfo(toCharId, fromElem.health, fromElem.armor, fromElem.position, 2, 2, unk6=false, 429, None, None)
            )
          )
        )
      } else {
        //previous fromMember has moved toMember
        val elem = squadUI(fromCharId)
        squadUI(fromCharId) = SquadUIElement(elem.name, elem.outfit, toIndex, elem.zone, elem.health, elem.armor, elem.position)
        sendResponse(SquadMemberEvent.Remove(id, fromCharId, fromIndex))
        sendResponse(SquadMemberEvent.Add(id, fromCharId, toIndex, elem.name, elem.zone, outfit_id = 0))
        sendResponse(
          SquadState(
            PlanetSideGUID(id),
            List(SquadStateInfo(fromCharId, elem.health, elem.armor, elem.position, 2, 2, unk6=false, 429, None, None))
          )
        )
      }
      val charId = avatar.id
      if (toCharId == charId) {
        sendResponse(PlanetsideAttributeMessage(player.GUID, 32, toIndex))
      } else if (fromCharId == charId) {
        sendResponse(PlanetsideAttributeMessage(player.GUID, 32, fromIndex))
      }
    }
  }

  /**
   * Give the squad colors associated with the current squad to the client's player character.
   * @param value value to associate the player
   */
  def GiveSquadColorsToSelf(value: Long): Unit = {
    GiveSquadColorsToSelf(player.GUID, player.Faction, value)
  }

  /**
   * Give the squad colors associated with the current squad to the client's player character.
   * @param guid player guid
   * @param faction faction for targeted updates to other players
   * @param value value to associate the player
   */
  def GiveSquadColorsToSelf(guid: PlanetSideGUID, faction: PlanetSideEmpire.Value, value: Long): Unit = {
    sendResponse(PlanetsideAttributeMessage(guid, 31, value))
    GiveSquadColorsForOthers(guid, faction, value)
  }

  /**
   * Give the squad colors associated with the current squad to the client's player character.
   * @param guid player guid
   * @param faction faction for targeted updates to other players
   * @param value value to associate the player
   */
  def GiveSquadColorsForOthers(guid: PlanetSideGUID, faction: PlanetSideEmpire.Value, value: Long): Unit = {
    GiveSquadColorsForOthers(guid, faction.toString, value)
  }

  /**
   * Give the squad colors associated with the current squad to the client's player character to other players.
   * @param guid player guid
   * @param factionChannel faction for targeted updates to other players
   * @param value value to associate the player
   */
  def GiveSquadColorsForOthers(guid: PlanetSideGUID, factionChannel: String, value: Long): Unit = {
    continent.AvatarEvents ! AvatarServiceMessage(factionChannel, AvatarAction.PlanetsideAttribute(guid, 31, value))
  }

  /**
   * Allocate all squad members in zone and give their nameplates and their marquees the appropriate squad color.
   */
  def GiveSquadColorsToMembers(): Unit = {
    GiveSquadColorsToMembers(squadUI.keys, squad_supplement_id)
  }

  /**
   * Allocate the listed squad members in zone and give their nameplates and their marquees the appropriate squad color.
   * @param members members of the squad to target
   */
  def GiveSquadColorsToMembers(members: Iterable[Long]): Unit = {
    GiveSquadColorsToMembers(members, squad_supplement_id)
  }

  /**
   * Allocate the listed squad members in zone and give their nameplates and their marquees the appropriate squad color.
   * @see `PlanetsideAttributeMessage`
   * @param members members of the squad to target
   * @param value   the assignment value
   */
  def GiveSquadColorsToMembers(members: Iterable[Long], value: Long): Unit = {
    SquadMembersInZone(members).foreach { members =>
      sendResponse(PlanetsideAttributeMessage(members.GUID, 31, value))
    }
  }

  def PromoteSquadUIElements(squad: Squad, fromIndex: Int): Unit = {
    //the players should have already been swapped in the backend object
    val firstMember = squad.Membership(0)
    val firstCharId  = firstMember.CharId
    val secondMember = squad.Membership(fromIndex)
    val secondCharId = secondMember.CharId
    if (squadUI.nonEmpty && fromIndex != 0 && firstCharId > 0 && secondCharId > 0) {
      val newFirstElem  = squadUI(firstCharId).copy(index = 0)
      val newSecondElem = squadUI(secondCharId).copy(index = fromIndex)
      val charId          = player.CharId
      val pguid           = player.GUID
      val sguid           = squad.GUID
      val id              = squad_supplement_id
      //secondMember and firstMember swap places
      squadUI.put(firstCharId, newFirstElem)
      squadUI.put(secondCharId, newSecondElem)
      sendResponse(SquadMemberEvent(MemberEvent.Promote, id, firstCharId, position = 0))
      //player is being either promoted or demoted?
      if (firstCharId == charId) {
        sendResponse(PlanetsideAttributeMessage(pguid, 32, 0))
        sendResponse(SquadDefinitionActionMessage(sguid, 0, SquadAction.IdentifyAsSquadLeader()))
        sendResponse(SquadDefinitionActionMessage(sguid, 0, SquadAction.Unknown(18)))
      } else if (secondCharId == charId) {
        sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.IdentifyAsSquadLeader()))
        sendResponse(PlanetsideAttributeMessage(pguid, 32, fromIndex))
        sendResponse(SquadDefinitionActionMessage(sguid, 0, SquadAction.Unknown(18)))
      }
      //seed updates (just for the swapped players)
      sendResponse(
        SquadState(PlanetSideGUID(id), List(
          SquadStateInfo(firstCharId, newFirstElem.health, newFirstElem.armor, newFirstElem.position),
          SquadStateInfo(secondCharId, newSecondElem.health, newSecondElem.armor, newSecondElem.position)
        ))
      )
    }
  }
}
