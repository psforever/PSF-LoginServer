// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.services.teamwork.SquadServiceResponse

import scala.collection.mutable
//
import net.psforever.actors.session.AvatarActor
import net.psforever.objects.teamwork.Squad
import net.psforever.objects.{Default, Player}
import net.psforever.packet.game._
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.teamwork.{SquadResponse, SquadServiceMessage, SquadAction => SquadServiceAction}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}

trait SquadHandlerFunctions extends CommonSessionInterfacingFunctionality {
  val ops: SessionSquadHandlers

  protected var waypointCooldown: Long = 0L

  def handleSquadDefinitionAction(pkt: SquadDefinitionActionMessage): Unit

  def handleSquadMemberRequest(pkt: SquadMembershipRequest): Unit

  def handleSquadWaypointRequest(pkt: SquadWaypointRequest): Unit

  def handle(response: SquadResponse.Response, excluded: Iterable[Long]): Unit
}

object SessionSquadHandlers {
  final case class SquadUIElement(
                                   name: String,
                                   outfit: Long,
                                   index: Int,
                                   zone: Int,
                                   health: Int,
                                   armor: Int,
                                   position: Vector3
                                 )
  def rethrowSquadServiceResponse(response: SquadResponse.Response)(sessionLogic: SessionData): Unit = {
    sessionLogic.context.self ! SquadServiceResponse("", response)
  }
}

class SessionSquadHandlers(
                            val sessionLogic: SessionData,
                            val avatarActor: typed.ActorRef[AvatarActor.Command],
                            val squadService: ActorRef,
                            implicit val context: ActorContext
                          ) extends CommonSessionInterfacingFunctionality {
  import SessionSquadHandlers._

  private[session] val squadUI: mutable.LongMap[SquadUIElement] = new mutable.LongMap[SquadUIElement]()
  private[session] var squad_supplement_id: Int = 0
  private[session] var squad_guid: PlanetSideGUID = PlanetSideGUID(0)
  /**
   * When joining or creating a squad, the original state of the avatar's internal LFS variable is blanked.
   * This `WorldSessionActor`-local variable is then used to indicate the ongoing state of the LFS UI component,
   * now called "Looking for Squad Member."
   * Only the squad leader may toggle the LFSM marquee.
   * Upon leaving or disbanding a squad, this value is made false.
   * Control switching between the `Avatar`-local and the `WorldSessionActor`-local variable is contingent on `squadUI` being populated.
   */
  private[session] var squadSetup: () => Unit = FirstTimeSquadSetup
  private[session] var squadUpdateCounter: Int = 0
  private[session] var updateSquad: () => Unit = NoSquadUpdates
  private[session] var updateSquadRef: ActorRef = Default.Actor
  private val queuedSquadActions: Seq[() => Unit] = Seq(SquadUpdates, NoSquadUpdates, NoSquadUpdates, NoSquadUpdates)

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
      case (None, _) => ()
    }
    //non-squad GUID-0 counts as the settings when not joined with a squad
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.IdentifyAsSquadLeader()))
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.SetListSquad()))
    sendResponse(SquadDefinitionActionMessage(PlanetSideGUID(0), 0, SquadAction.Unknown(18)))
    squadService ! SquadServiceMessage(player, continent, SquadServiceAction.InitSquadList())
    squadService ! SquadServiceMessage(player, continent, SquadServiceAction.InitCharId())
    cleanUpSquadCards()
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
            player.GUID,
            AvatarAction.PlanetsideAttribute(31, squad_supplement_id)
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
    continent.AvatarEvents ! AvatarServiceMessage(factionChannel, guid, AvatarAction.PlanetsideAttribute(31, value))
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

  def cleanUpSquadCards(): Unit = {
    squadUI.foreach { case (id, card) =>
      sendResponse(SquadMemberEvent.Remove(squad_supplement_id, id, card.index))
    }
    squadUI.clear()
    squad_supplement_id = 0
  }
}
