// Copyright (c) 2019 PSForever
package net.psforever.services.teamwork

import akka.actor.{Actor, ActorRef, Terminated}
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.definition.converter.StatConverter
import net.psforever.objects.loadouts.SquadLoadout
import net.psforever.objects.teamwork.{Member, Squad, SquadFeatures}
import net.psforever.objects.zones.Zone
import net.psforever.objects.{LivePlayerList, Player}
import net.psforever.packet.game.{
  PlanetSideZoneID,
  SquadDetail,
  SquadInfo,
  SquadPositionDetail,
  SquadPositionEntry,
  WaypointEventAction,
  WaypointInfo
}
import net.psforever.types._
import net.psforever.services.{GenericEventBus, Service}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class SquadService extends Actor {
  import SquadService._

  /**
    * The current unique squad identifier, to be wrapped in a `PlanetSideGUID` object later.
    * The count always starts at 1, even when reset.
    * A squad of `PlanetSideGUID(0)` indicates both a nonexistent squad and the default no-squad for clients.
    */
  private var sid: Int = 1

  /**
    * All squads.<br>
    * key - squad unique number; value - the squad wrapped around its attributes object
    */
  private var squadFeatures: TrieMap[PlanetSideGUID, SquadFeatures] = new TrieMap[PlanetSideGUID, SquadFeatures]()

  /**
    * The list of squads that each of the factions see for the purposes of keeping track of changes to the list.
    * These squads are considered public "listed" squads -
    * all the players of a certain faction can see them in the squad list
    * and may have limited interaction with their squad definition windows.<br>
    * key - squad unique number; value - the squad's unique identifier number
    */
  private val publishedLists: TrieMap[PlanetSideEmpire.Value, ListBuffer[PlanetSideGUID]] =
    TrieMap[PlanetSideEmpire.Value, ListBuffer[PlanetSideGUID]](
      PlanetSideEmpire.TR -> ListBuffer.empty,
      PlanetSideEmpire.NC -> ListBuffer.empty,
      PlanetSideEmpire.VS -> ListBuffer.empty
    )

  /**
    * key - a unique character identifier number; value - the squad to which this player is a member
    */
  private var memberToSquad: mutable.LongMap[Squad] = mutable.LongMap[Squad]()

  /**
    * key - a unique character identifier number; value - the active invitation object
    */
  private val invites: mutable.LongMap[Invitation] = mutable.LongMap[Invitation]()

  /**
    * key - a unique character identifier number; value - a list of inactive invitation objects waiting to be resolved
    */
  private val queuedInvites: mutable.LongMap[List[Invitation]] = mutable.LongMap[List[Invitation]]()

  /**
    * The given player has refused participation into this other player's squad.<br>
    * key - a unique character identifier number; value - a list of unique character identifier numbers
    */
  private val refused: mutable.LongMap[List[Long]] = mutable.LongMap[List[Long]]()

  /**
    * Players who are interested in updated details regarding a certain squad though they may not be a member of the squad.<br>
    * key - unique character identifier number; value - a squad identifier number
    */
  private val continueToMonitorDetails: mutable.LongMap[PlanetSideGUID] = mutable.LongMap[PlanetSideGUID]()

  /**
    * A placeholder for an absent active invite that has not (yet) been accepted or rejected,
    * equal to the then-current active invite.
    * Created when removing an active invite.
    * Checked when trying to add a new invite (if found, the new invite is queued).
    * Cleared when the next queued invite becomes active.<br>
    * key - unique character identifier number; value, unique character identifier number
    */
  private val previousInvites: mutable.LongMap[Invitation] = mutable.LongMap[Invitation]()

  /**
    * This is a formal `ActorEventBus` object that is reserved for faction-wide messages and squad-specific messages.
    * When the user joins the `SquadService` with a `Service.Join` message
    * that includes a confirmed faction affiliation identifier,
    * the origin `ActorRef` is added as a subscription.
    * Squad channels are produced when a squad is created,
    * and are subscribed to as users join the squad,
    * and unsubscribed from as users leave the squad.<br>
    * key - a `PlanetSideEmpire` value; value - `ActorRef` reference<br>
    * key - a consistent squad channel name; value - `ActorRef` reference
    * @see `CloseSquad`
    * @see `JoinSquad`
    * @see `LeaveSquad`
    * @see `Service.Join`
    * @see `Service.Leave`
    */
  private val SquadEvents = new GenericEventBus[SquadServiceResponse]

  /**
    * This collection contains the message-sending contact reference for individuals.
    * When the user joins the `SquadService` with a `Service.Join` message
    * that includes their unique character identifier,
    * and the origin `ActorRef` is added as a subscription.
    * It is maintained until they disconnect entirely.
    * The subscription is anticipated to belong to an instance of `WorldSessionActor`.<br>
    * key - unique character identifier number; value - `ActorRef` reference for that character
    * @see `Service.Join`
    */
  private val UserEvents: mutable.LongMap[ActorRef] = mutable.LongMap[ActorRef]()

  private[this] val log = org.log4s.getLogger

  private def debug(msg: String): Unit = {
    log.info(msg)
  }

  override def preStart(): Unit = {
    log.info("Starting...")
  }

  override def postStop(): Unit = {
    //invitations
    invites.clear()
    queuedInvites.clear()
    previousInvites.clear()
    refused.clear()
    continueToMonitorDetails.clear()
    //squads and members (users)
    squadFeatures.foreach {
      case (_, features) =>
        CloseSquad(features.Squad)
    }
    memberToSquad.clear()
    publishedLists.clear()
    UserEvents.foreach {
      case (_, actor) =>
        SquadEvents.unsubscribe(actor)
    }
    UserEvents.clear()
  }

  /**
    * Produce the next available unique squad identifier.
    * The first number is always 1.
    * The greatest possible identifier is 65535 (an unsigned 16-bit integer)
    * before it wraps back around to 1.
    * @return the current squad unique identifier number
    */
  def GetNextSquadId(): PlanetSideGUID = {
    val out = sid
    val j   = sid + 1
    if (j == 65536) {
      sid = 1
    } else {
      sid = j
    }
    PlanetSideGUID(out)
  }

  /**
    * Set the unique squad identifier back to the start (1) if no squads are active and no players are logged on.
    * @return `true`, if the identifier is reset; `false`, otherwise
    */
  def TryResetSquadId(): Boolean = {
    if (UserEvents.isEmpty && squadFeatures.isEmpty) {
      sid = 1
      true
    } else {
      false
    }
  }

  /**
    * If a squad exists for an identifier, return that squad.
    * @param id the squad unique identifier number
    * @return the discovered squad, or `None`
    */
  def GetSquad(id: PlanetSideGUID): Option[Squad] =
    squadFeatures.get(id) match {
      case Some(features) => Some(features.Squad)
      case None           => None
    }

  /**
    * If this player is a member of any squad, discover that squad.
    * @param player the potential member
    * @return the discovered squad, or `None`
    */
  def GetParticipatingSquad(player: Player): Option[Squad] = GetParticipatingSquad(player.CharId)

  /**
    * If the player associated with this unique character identifier number is a member of any squad, discover that squad.
    * @param charId the potential member identifier
    * @return the discovered squad, or `None`
    */
  def GetParticipatingSquad(charId: Long): Option[Squad] =
    memberToSquad.get(charId) match {
      case opt @ Some(_) =>
        opt
      case None =>
        None
    }

  /**
    * If this player is a member of any squad, discover that squad.
    * @see `GetParticipatingSquad`
    * @see `Squad::Leader`
    * @param player the potential member
    * @param opt an optional squad to check;
    *            the expectation is that the provided squad is a known participating squad
    * @return the discovered squad, or `None`
    */
  def GetLeadingSquad(player: Player, opt: Option[Squad]): Option[Squad] = GetLeadingSquad(player.CharId, opt)

  /**
    * If the player associated with this unique character identifier number is the leader of any squad, discover that squad.
    * @see `GetParticipatingSquad`
    * @see `Squad->Leader`
    * @param charId the potential member identifier
    * @param opt an optional squad to check;
    *            the expectation is that the provided squad is a known participating squad
    * @return the discovered squad, or `None`
    */
  def GetLeadingSquad(charId: Long, opt: Option[Squad]): Option[Squad] =
    opt.orElse(GetParticipatingSquad(charId)) match {
      case Some(squad) =>
        if (squad.Leader.CharId == charId) {
          Some(squad)
        } else {
          None
        }
      case _ =>
        None
    }

  /**
    * Overloaded message-sending operation.
    * The `Actor` version wraps around the expected `!` functionality.
    * @param to an `ActorRef` which to send the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    */
  def Publish(to: ActorRef, msg: SquadResponse.Response): Unit = {
    Publish(to, msg, Nil)
  }

  /**
    * Overloaded message-sending operation.
    * The `Actor` version wraps around the expected `!` functionality.
    * @param to an `ActorRef` which to send the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    * @param excluded a group of character identifier numbers who should not receive the message
    *                 (resolved at destination)
    */
  def Publish(to: ActorRef, msg: SquadResponse.Response, excluded: Iterable[Long]): Unit = {
    to ! SquadServiceResponse("", excluded, msg)
  }

  /**
    * Overloaded message-sending operation.
    * Always publishes on the `SquadEvents` object.
    * @param to a faction affiliation used as the channel for the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    */
  def Publish(to: PlanetSideEmpire.Type, msg: SquadResponse.Response): Unit = {
    Publish(to, msg, Nil)
  }

  /**
    * Overloaded message-sending operation.
    * Always publishes on the `SquadEvents` object.
    * @param to a faction affiliation used as the channel for the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    * @param excluded a group of character identifier numbers who should not receive the message
    *                 (resolved at destination)
    */
  def Publish(to: PlanetSideEmpire.Type, msg: SquadResponse.Response, excluded: Iterable[Long]): Unit = {
    SquadEvents.publish(SquadServiceResponse(s"/$to/Squad", excluded, msg))
  }

  /**
    * Overloaded message-sending operation.
    * Strings come in three accepted patterns.
    * The first resolves into a faction name, as determined by `PlanetSideEmpire` when transformed into a string.
    * The second resolves into a squad's dedicated channel, a name that is formulaic.
    * The third resolves as a unique character identifier number.
    * @param to a string used as the channel for the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    */
  def Publish(to: String, msg: SquadResponse.Response): Unit = {
    Publish(to, msg, Nil)
  }

  /**
    * Overloaded message-sending operation.
    * Strings come in three accepted patterns.
    * The first resolves into a faction name, as determined by `PlanetSideEmpire` when transformed into a string.
    * The second resolves into a squad's dedicated channel, a name that is formulaic.
    * The third resolves as a unique character identifier number.
    * @param to a string used as the channel for the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    * @param excluded a group of character identifier numbers who should not receive the message
    *                 (resolved at destination, usually)
    */
  def Publish(to: String, msg: SquadResponse.Response, excluded: Iterable[Long]): Unit = {
    to match {
      case str if "TRNCVS".indexOf(str) > -1 || str.matches("(TR|NC|VS)-Squad\\d+") =>
        SquadEvents.publish(SquadServiceResponse(s"/$str/Squad", excluded, msg))
      case str if str.matches("//d+") =>
        Publish(to.toLong, msg, excluded)
      case _ =>
        log.error(s"Publish(String): subscriber information is an unhandled format - $to")
    }
  }

  /**
    * Overloaded message-sending operation.
    * Always publishes on the `ActorRef` objects retained by the `UserEvents` object.
    * @param to a unique character identifier used as the channel for the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    */
  def Publish(to: Long, msg: SquadResponse.Response): Unit = {
    UserEvents.get(to) match {
      case Some(user) =>
        user ! SquadServiceResponse("", msg)
      case None =>
        log.error(s"Publish(Long): subscriber information can not be found - $to")
    }
  }

  /**
    * Overloaded message-sending operation.
    * Always publishes on the `ActorRef` objects retained by the `UserEvents` object.
    * @param to a unique character identifier used as the channel for the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    * @param excluded a group of character identifier numbers who should not receive the message
    */
  def Publish(to: Long, msg: SquadResponse.Response, excluded: Iterable[Long]): Unit = {
    if (!excluded.exists(_ == to)) {
      Publish(to, msg)
    }
  }

  /**
    * Overloaded message-sending operation.
    * No message can be sent using this distinction.
    * Log a warning.
    * @param to something that was expected to be used as the channel for the message
    *           but is not handled as such
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    */
  def Publish[ANY >: Any](to: ANY, msg: SquadResponse.Response): Unit = {
    log.warn(s"Publish(Any): subscriber information is an unhandled format - $to")
  }

  /**
    * Overloaded message-sending operation.
    * No message can be sent using this distinction.
    * Log a warning.
    * @param to something that was expected to be used as the channel for the message
    *           but is not handled as such
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    * @param excluded a group of character identifier numbers who should not receive the message
    */
  def Publish[ANY >: Any](to: ANY, msg: SquadResponse.Response, excluded: Iterable[Long]): Unit = {
    log.warn(s"Publish(Any): subscriber information is an unhandled format - $to")
  }

  def receive: Receive = {
    //subscribe to a faction's channel - necessary to receive updates about listed squads
    case Service.Join(faction) if "TRNCVS".indexOf(faction) > -1 =>
      val path = s"/$faction/Squad"
      val who  = sender()
      debug(s"$who has joined $path")
      SquadEvents.subscribe(who, path)

    //subscribe to the player's personal channel - necessary for future and previous squad information
    case Service.Join(char_id) =>
      try {
        val longCharId = char_id.toLong
        val path       = s"/$char_id/Squad"
        val who        = sender()
        debug(s"$who has joined $path")
        context.watch(who)
        UserEvents += longCharId -> who
        refused(longCharId) = Nil
      } catch {
        case _: ClassCastException =>
          log.warn(s"Service.Join: tried $char_id as a unique character identifier, but it could not be casted")
        case e: Exception =>
          log.error(s"Service.Join: unexpected exception using $char_id as data - ${e.getLocalizedMessage}")
          e.printStackTrace()
      }

    case Service.Leave(Some(faction)) if "TRNCVS".indexOf(faction) > -1 =>
      val path = s"/$faction/Squad"
      val who  = sender()
      debug(s"$who has left $path")
      SquadEvents.unsubscribe(who, path)

    case Service.Leave(Some(char_id)) =>
      try {
        LeaveService(char_id.toLong, sender())
      } catch {
        case _: ClassCastException =>
          log.warn(s"Service.Leave: tried $char_id as a unique character identifier, but it could not be casted")
        case e: Exception =>
          log.error(s"Service.Leave: unexpected exception using $char_id as data - ${e.getLocalizedMessage}")
          e.printStackTrace()
      }

    case Service.Leave(None) | Service.LeaveAll() =>
      UserEvents find { case (_, subscription) => subscription.path.equals(sender().path) } match {
        case Some((to, _)) =>
          LeaveService(to, sender())
        case _ => ;
      }

    case Terminated(actorRef) =>
      context.unwatch(actorRef)
      UserEvents find { case (_, subscription) => subscription eq actorRef } match {
        case Some((to, _)) =>
          LeaveService(to, sender())
        case _ => ;
      }

    case SquadServiceMessage(tplayer, zone, squad_action) =>
      squad_action match {
        case SquadAction.InitSquadList() =>
          Publish(sender(), SquadResponse.InitList(PublishedLists(tplayer.Faction))) //send initial squad catalog

        case SquadAction.InitCharId() =>
          val charId = tplayer.CharId
          memberToSquad.get(charId) match {
            case None => ;
            case Some(squad) =>
              val guid      = squad.GUID
              val toChannel = s"/${squadFeatures(guid).ToChannel}/Squad"
              val indices =
                squad.Membership.zipWithIndex.collect({ case (member, index) if member.CharId != 0 => index }).toList
              Publish(charId, SquadResponse.AssociateWithSquad(guid))
              Publish(charId, SquadResponse.Join(squad, indices, toChannel))
              InitSquadDetail(guid, Seq(charId), squad)
              InitWaypoints(charId, guid)
          }

        case SquadAction.Membership(SquadRequestType.Invite, invitingPlayer, Some(_invitedPlayer), invitedName, _) =>
          //this is just busy work; for actual joining operations, see SquadRequestType.Accept
          (if (invitedName.nonEmpty) {
             //validate player with name exists
             LivePlayerList
               .WorldPopulation({
                 case (_, a: Avatar) => a.name.equalsIgnoreCase(invitedName) && a.faction == tplayer.Faction
               })
               .headOption match {
               case Some(player) => UserEvents.keys.find(_ == player.id)
               case None         => None
             }
           } else {
             //validate player with id exists
             LivePlayerList
               .WorldPopulation({ case (_, a: Avatar) => a.id == _invitedPlayer && a.faction == tplayer.Faction })
               .headOption match {
               case Some(player) => Some(_invitedPlayer)
               case None         => None
             }
           }) match {
            case Some(invitedPlayer) if invitingPlayer != invitedPlayer =>
              (memberToSquad.get(invitingPlayer), memberToSquad.get(invitedPlayer)) match {
                case (Some(squad1), Some(squad2)) if squad1.GUID == squad2.GUID =>
                //both players are in the same squad; no need to do anything

                case (Some(squad1), Some(squad2))
                    if squad1.Leader.CharId == invitingPlayer && squad2.Leader.CharId == invitedPlayer &&
                      squad1.Size > 1 && squad2.Size > 1 =>
                //we might do some platoon chicanery with this case later
                //TODO platoons

                case (Some(squad1), Some(squad2)) if squad2.Size == 1 =>
                  //both players belong to squads, but the invitedPlayer's squad (squad2) is underutilized by comparison
                  //treat the same as "the classic situation" using squad1
                  if (!Refused(invitedPlayer).contains(invitingPlayer)) {
                    val charId = tplayer.CharId
                    AddInviteAndRespond(
                      invitedPlayer,
                      VacancyInvite(charId, tplayer.Name, squad1.GUID),
                      charId,
                      tplayer.Name
                    )
                  }

                case (Some(squad1), Some(squad2)) if squad1.Size == 1 =>
                  //both players belong to squads, but the invitingPlayer's squad is underutilized by comparison
                  //treat the same as "indirection ..." using squad2
                  val leader = squad2.Leader.CharId
                  if (Refused(invitingPlayer).contains(invitedPlayer)) {
                    debug(s"$invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
                  } else if (Refused(invitingPlayer).contains(leader)) {
                    debug(s"$invitedPlayer repeated a previous refusal to $leader's invitation offer")
                  } else {
                    AddInviteAndRespond(
                      leader,
                      IndirectInvite(tplayer, squad2.GUID),
                      invitingPlayer,
                      tplayer.Name
                    )
                  }

                case (Some(squad), None) =>
                  //the classic situation
                  if (!Refused(invitedPlayer).contains(invitingPlayer)) {
                    AddInviteAndRespond(
                      invitedPlayer,
                      VacancyInvite(tplayer.CharId, tplayer.Name, squad.GUID),
                      invitingPlayer,
                      tplayer.Name
                    )
                  } else {
                    debug(s"$invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
                  }

                case (None, Some(squad)) =>
                  //indirection;  we're trying to invite ourselves to someone else's squad
                  val leader = squad.Leader.CharId
                  if (Refused(invitingPlayer).contains(invitedPlayer)) {
                    debug(s"$invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
                  } else if (Refused(invitingPlayer).contains(leader)) {
                    debug(s"$invitedPlayer repeated a previous refusal to $leader's invitation offer")
                  } else {
                    AddInviteAndRespond(
                      squad.Leader.CharId,
                      IndirectInvite(tplayer, squad.GUID),
                      invitingPlayer,
                      tplayer.Name
                    )
                  }

                case (None, None) =>
                  //neither the invited player nor the inviting player belong to any squad
                  if (Refused(invitingPlayer).contains(invitedPlayer)) {
                    debug(s"$invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
                  } else if (Refused(invitedPlayer).contains(invitingPlayer)) {
                    debug(s"$invitingPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
                  } else {
                    AddInviteAndRespond(
                      invitedPlayer,
                      SpontaneousInvite(tplayer),
                      invitingPlayer,
                      tplayer.Name
                    )
                  }

                case _ => ;
              }
            case _ => ;
          }

        case SquadAction.Membership(SquadRequestType.ProximityInvite, invitingPlayer, _, _, _) =>
          GetLeadingSquad(invitingPlayer, None) match {
            case Some(squad) =>
              val sguid    = squad.GUID
              val features = squadFeatures(sguid)
              features.SearchForRole match {
                case Some(-1) =>
                  //we've already issued a proximity invitation; no need to do another
                  debug("ProximityInvite: wait for existing proximity invitations to clear")
                case _ =>
                  val outstandingActiveInvites = features.SearchForRole match {
                    case Some(pos) =>
                      RemoveQueuedInvitesForSquadAndPosition(sguid, pos)
                      invites.collect {
                        case (charId, LookingForSquadRoleInvite(_, _, squad_guid, role))
                            if squad_guid == sguid && role == pos =>
                          charId
                      }
                    case None =>
                      List.empty[Long]
                  }
                  val faction        = squad.Faction
                  val center         = tplayer.Position
                  val excusedInvites = features.Refuse
                  //positions that can be recruited to
                  val positions = squad.Membership.zipWithIndex
                    .collect { case (member, index) if member.CharId == 0 && squad.Availability(index) => member }
                  /*
                players who are:
                - the same faction as the squad
                - have Looking For Squad enabled
                - do not currently belong to a squad
                - are denied the opportunity to be invited
                - are a certain distance from the squad leader (n < 25m)
                   */
                  (zone.LivePlayers
                    .collect {
                      case player
                          if player.Faction == faction && player.avatar.lookingForSquad &&
                            (memberToSquad.get(player.CharId).isEmpty || memberToSquad(player.CharId).Size == 1) &&
                            !excusedInvites
                              .contains(player.CharId) && Refused(player.CharId).contains(squad.Leader.CharId) &&
                            Vector3.DistanceSquared(player.Position, center) < 625f && {
                            positions
                              .map { role =>
                                val requirementsToMeet = role.Requirements
                                requirementsToMeet.intersect(player.avatar.certifications) == requirementsToMeet
                              }
                              .foldLeft(false)(_ || _)
                          } =>
                        player.CharId
                    }
                    .partition { charId => outstandingActiveInvites.exists(_ == charId) } match {
                    case (Nil, Nil) =>
                      //no one found
                      outstandingActiveInvites foreach RemoveInvite
                      features.ProxyInvites = Nil
                      None
                    case (outstandingPlayerList, invitedPlayerList) =>
                      //players who were actively invited for the previous position and are eligible for the new position
                      features.SearchForRole = Some(-1)
                      outstandingPlayerList.foreach { charId =>
                        val bid = invites(charId).asInstanceOf[LookingForSquadRoleInvite]
                        invites(charId) = ProximityInvite(bid.char_id, bid.name, sguid)
                      }
                      //players who were actively invited for the previous position but are ineligible for the new position
                      (features.ProxyInvites filterNot (outstandingPlayerList contains)) foreach RemoveInvite
                      features.ProxyInvites = outstandingPlayerList ++ invitedPlayerList
                      Some(invitedPlayerList)
                  }) match {
                    //add invitations for position in squad
                    case Some(invitedPlayers) =>
                      val invitingPlayer = tplayer.CharId
                      val name           = tplayer.Name
                      invitedPlayers.foreach { invitedPlayer =>
                        AddInviteAndRespond(
                          invitedPlayer,
                          ProximityInvite(invitingPlayer, name, sguid),
                          invitingPlayer,
                          name
                        )
                      }
                    case None => ;
                  }
              }

            case None =>
          }

        case SquadAction.Membership(SquadRequestType.Accept, invitedPlayer, _, _, _) =>
          val acceptedInvite = RemoveInvite(invitedPlayer)
          acceptedInvite match {
            case Some(RequestRole(petitioner, guid, position))
                if EnsureEmptySquad(petitioner.CharId) && squadFeatures.get(guid).nonEmpty =>
              //player requested to join a squad's specific position
              //invitedPlayer is actually the squad leader; petitioner is the actual "invitedPlayer"
              val features = squadFeatures(guid)
              JoinSquad(petitioner, features.Squad, position)
              RemoveInvitesForSquadAndPosition(guid, position)

            case Some(IndirectInvite(recruit, guid)) if EnsureEmptySquad(recruit.CharId) =>
              //tplayer / invitedPlayer is actually the squad leader
              val recruitCharId = recruit.CharId
              HandleVacancyInvite(guid, recruitCharId, invitedPlayer, recruit) match {
                case Some((squad, line)) =>
                  Publish(
                    invitedPlayer,
                    SquadResponse.Membership(
                      SquadResponseType.Accept,
                      0,
                      0,
                      invitedPlayer,
                      Some(recruitCharId),
                      recruit.Name,
                      true,
                      Some(None)
                    )
                  )
                  JoinSquad(recruit, squad, line)
                  RemoveInvitesForSquadAndPosition(squad.GUID, line)
                //since we are the squad leader, we do not want to brush off our queued squad invite tasks
                case _ => ;
              }

            case Some(VacancyInvite(invitingPlayer, _, guid)) if EnsureEmptySquad(invitedPlayer) =>
              //accepted an invitation to join an existing squad
              HandleVacancyInvite(guid, invitedPlayer, invitingPlayer, tplayer) match {
                case Some((squad, line)) =>
                  Publish(
                    invitingPlayer,
                    SquadResponse.Membership(
                      SquadResponseType.Accept,
                      0,
                      0,
                      invitingPlayer,
                      Some(invitedPlayer),
                      tplayer.Name,
                      false,
                      Some(None)
                    )
                  )
                  Publish(
                    invitedPlayer,
                    SquadResponse.Membership(
                      SquadResponseType.Accept,
                      0,
                      0,
                      invitedPlayer,
                      Some(invitingPlayer),
                      "",
                      true,
                      Some(None)
                    )
                  )
                  JoinSquad(tplayer, squad, line)
                  RemoveQueuedInvites(invitedPlayer) //TODO deal with these somehow
                  RemoveInvitesForSquadAndPosition(squad.GUID, line)
                case _ => ;
              }

            case Some(SpontaneousInvite(invitingPlayer)) if EnsureEmptySquad(invitedPlayer) =>
              //originally, we were invited by someone into a new squad they would form
              val invitingPlayerCharId = invitingPlayer.CharId
              (GetParticipatingSquad(invitingPlayer) match {
                case Some(participating) =>
                  //invitingPlayer became part of a squad while invited player was answering the original summons
                  Some(participating)
                case _ =>
                  //generate a new squad, with invitingPlayer as the leader
                  val squad = StartSquad(invitingPlayer)
                  squad.Task = s"${invitingPlayer.Name}'s Squad"
                  Publish(invitingPlayerCharId, SquadResponse.AssociateWithSquad(squad.GUID))
                  Some(squad)
              }) match {
                case Some(squad) =>
                  HandleVacancyInvite(squad, tplayer.CharId, invitingPlayerCharId, tplayer) match {
                    case Some((_, line)) =>
                      Publish(
                        invitedPlayer,
                        SquadResponse.Membership(
                          SquadResponseType.Accept,
                          0,
                          0,
                          invitedPlayer,
                          Some(invitingPlayerCharId),
                          "",
                          true,
                          Some(None)
                        )
                      )
                      Publish(
                        invitingPlayerCharId,
                        SquadResponse.Membership(
                          SquadResponseType.Accept,
                          0,
                          0,
                          invitingPlayerCharId,
                          Some(invitedPlayer),
                          tplayer.Name,
                          false,
                          Some(None)
                        )
                      )
                      JoinSquad(tplayer, squad, line)
                      RemoveQueuedInvites(tplayer.CharId) //TODO deal with these somehow
                    case _ => ;
                  }
                case _ => ;
              }

            case Some(LookingForSquadRoleInvite(invitingPlayer, name, guid, position))
                if EnsureEmptySquad(invitedPlayer) =>
              squadFeatures.get(guid) match {
                case Some(features) if JoinSquad(tplayer, features.Squad, position) =>
                  //join this squad
                  Publish(
                    invitedPlayer,
                    SquadResponse.Membership(
                      SquadResponseType.Accept,
                      0,
                      0,
                      invitedPlayer,
                      Some(invitingPlayer),
                      "",
                      true,
                      Some(None)
                    )
                  )
                  Publish(
                    invitingPlayer,
                    SquadResponse.Membership(
                      SquadResponseType.Accept,
                      0,
                      0,
                      invitingPlayer,
                      Some(invitedPlayer),
                      tplayer.Name,
                      false,
                      Some(None)
                    )
                  )
                  RemoveQueuedInvites(tplayer.CharId)
                  features.ProxyInvites = Nil
                  features.SearchForRole = None
                  RemoveInvitesForSquadAndPosition(guid, position)

                case Some(features) =>
                  //can not join squad; position is unavailable or other reasons block action
                  features.ProxyInvites = features.ProxyInvites.filterNot(_ == invitedPlayer)

                case _ =>
                //squad no longer exists?
              }

            case Some(ProximityInvite(invitingPlayer, _, guid)) if EnsureEmptySquad(invitedPlayer) =>
              squadFeatures.get(guid) match {
                case Some(features) =>
                  val squad = features.Squad
                  if (squad.Size < squad.Capacity) {
                    val positions = (for {
                      (member, index) <- squad.Membership.zipWithIndex
                      if squad.isAvailable(index, tplayer.avatar.certifications)
                    } yield (index, member.Requirements.size)).toList
                      .sortBy({ case (_, reqs) => reqs })
                    ((positions.headOption, positions.lastOption) match {
                      case (Some((first, size1)), Some((_, size2))) if size1 == size2 =>
                        Some(first) //join the first available position
                      case (Some(_), Some((last, _))) => Some(last) //join the most demanding position
                      case _                          => None
                    }) match {
                      case Some(position) if JoinSquad(tplayer, squad, position) =>
                        //join this squad
                        Publish(
                          invitedPlayer,
                          SquadResponse.Membership(
                            SquadResponseType.Accept,
                            0,
                            0,
                            invitedPlayer,
                            Some(invitingPlayer),
                            "",
                            true,
                            Some(None)
                          )
                        )
                        Publish(
                          invitingPlayer,
                          SquadResponse.Membership(
                            SquadResponseType.Accept,
                            0,
                            0,
                            invitingPlayer,
                            Some(invitedPlayer),
                            tplayer.Name,
                            false,
                            Some(None)
                          )
                        )
                        RemoveQueuedInvites(invitedPlayer)
                        features.ProxyInvites = features.ProxyInvites.filterNot(_ == invitedPlayer)
                      case _ =>
                    }
                  }
                  if (features.ProxyInvites.isEmpty) {
                    //all invitations exhausted; this invitation period is concluded
                    features.SearchForRole = None
                  } else if (squad.Size == squad.Capacity) {
                    //all available squad positions filled; terminate all remaining invitations
                    RemoveProximityInvites(guid)
                    RemoveAllInvitesToSquad(guid)
                    //RemoveAllInvitesWithPlayer(invitingPlayer)
                  }

                case _ =>
                //squad no longer exists?
              }

            case _ =>
              //the invite either timed-out or was withdrawn or is now invalid
              (previousInvites.get(invitedPlayer) match {
                case Some(SpontaneousInvite(leader))                     => (leader.CharId, leader.Name)
                case Some(VacancyInvite(charId, name, _))                => (charId, name)
                case Some(ProximityInvite(charId, name, _))              => (charId, name)
                case Some(LookingForSquadRoleInvite(charId, name, _, _)) => (charId, name)
                case _                                                   => (0L, "")
              }) match {
                case (0L, "") => ;
                case (charId, name) =>
                  Publish(
                    charId,
                    SquadResponse.Membership(SquadResponseType.Cancel, 0, 0, charId, Some(0L), name, false, Some(None))
                  )
              }
          }
          NextInviteAndRespond(invitedPlayer)

        case SquadAction.Membership(SquadRequestType.Leave, actingPlayer, _leavingPlayer, name, _) =>
          GetParticipatingSquad(actingPlayer) match {
            case Some(squad) =>
              val leader = squad.Leader.CharId
              (if (name.nonEmpty) {
                 //validate player with name
                 LivePlayerList
                   .WorldPopulation({ case (_, a: Avatar) => a.name.equalsIgnoreCase(name) })
                   .headOption match {
                   case Some(a) => UserEvents.keys.find(_ == a.id)
                   case None    => None
                 }
               } else {
                 //validate player with id
                 _leavingPlayer match {
                   case Some(id) => UserEvents.keys.find(_ == id)
                   case None     => None
                 }
               }) match {
                case out @ Some(leavingPlayer)
                    if GetParticipatingSquad(leavingPlayer).contains(squad) => //kicked player must be in the same squad
                  if (actingPlayer == leader) {
                    if (leavingPlayer == leader || squad.Size == 2) {
                      //squad leader is leaving his own squad, so it will be disbanded
                      //OR squad is only composed of two people, so it will be closed-out when one of them leaves
                      DisbandSquad(squad)
                    } else {
                      //kicked by the squad leader
                      Publish(
                        leavingPlayer,
                        SquadResponse.Membership(
                          SquadResponseType.Leave,
                          0,
                          0,
                          leavingPlayer,
                          Some(leader),
                          tplayer.Name,
                          false,
                          Some(None)
                        )
                      )
                      Publish(
                        leader,
                        SquadResponse.Membership(
                          SquadResponseType.Leave,
                          0,
                          0,
                          leader,
                          Some(leavingPlayer),
                          "",
                          true,
                          Some(None)
                        )
                      )
                      squadFeatures(squad.GUID).Refuse = leavingPlayer
                      LeaveSquad(leavingPlayer, squad)
                    }
                  } else if (leavingPlayer == actingPlayer) {
                    if (squad.Size == 2) {
                      //squad is only composed of two people, so it will be closed-out when one of them leaves
                      DisbandSquad(squad)
                    } else {
                      //leaving the squad of own accord
                      LeaveSquad(actingPlayer, squad)
                    }
                  }

                case _ => ;
              }
            case _ => ;
          }

        case SquadAction.Membership(SquadRequestType.Reject, rejectingPlayer, _, _, _) =>
          val rejectedBid = RemoveInvite(rejectingPlayer)
          //(A, B) -> person who made the rejection, person who was rejected
          (rejectedBid match {
            case Some(SpontaneousInvite(leader)) =>
              //rejectingPlayer is the would-be squad member; the squad leader's request was rejected
              val invitingPlayerCharId = leader.CharId
              Refused(rejectingPlayer, invitingPlayerCharId)
              (Some(rejectingPlayer), Some(invitingPlayerCharId))

            case Some(VacancyInvite(leader, _, guid))
                if squadFeatures.get(guid).nonEmpty && squadFeatures(guid).Squad.Leader.CharId != rejectingPlayer =>
              //rejectingPlayer is the would-be squad member; the squad leader's request was rejected
              Refused(rejectingPlayer, leader)
              (Some(rejectingPlayer), Some(leader))

            case Some(ProximityInvite(_, _, guid))
                if squadFeatures.get(guid).nonEmpty && squadFeatures(guid).Squad.Leader.CharId != rejectingPlayer =>
              //rejectingPlayer is the would-be squad member; the squad leader's request was rejected
              val features = squadFeatures(guid)
              features.Refuse = rejectingPlayer //do not bother this player anymore
              if ((features.ProxyInvites = features.ProxyInvites.filterNot(_ == rejectingPlayer)).isEmpty) {
                features.SearchForRole = None
              }
              (None, None)

            case Some(LookingForSquadRoleInvite(leader, _, guid, _))
                if squadFeatures.get(guid).nonEmpty && squadFeatures(guid).Squad.Leader.CharId != rejectingPlayer =>
              //rejectingPlayer is the would-be squad member; the squad leader's request was rejected
              Refused(rejectingPlayer, leader)
              val features = squadFeatures(guid)
              features.Refuse = rejectingPlayer
              if ((features.ProxyInvites = features.ProxyInvites.filterNot(_ == rejectingPlayer)).isEmpty) {
                features.SearchForRole = None
              }
              (None, None)

            case Some(RequestRole(_, guid, _))
                if squadFeatures.get(guid).nonEmpty && squadFeatures(guid).Squad.Leader.CharId == rejectingPlayer =>
              //rejectingPlayer is the squad leader; candidate is the would-be squad member who was rejected
              val features = squadFeatures(guid)
              features.Refuse = rejectingPlayer
              (Some(rejectingPlayer), None)

            case _ => ;
              (None, None)
          }) match {
            case (Some(rejected), Some(invited)) =>
              Publish(
                rejected,
                SquadResponse.Membership(SquadResponseType.Reject, 0, 0, rejected, Some(invited), "", true, Some(None))
              )
              Publish(
                invited,
                SquadResponse.Membership(
                  SquadResponseType.Reject,
                  0,
                  0,
                  invited,
                  Some(rejected),
                  tplayer.Name,
                  false,
                  Some(None)
                )
              )
            case (Some(rejected), None) =>
              Publish(
                rejected,
                SquadResponse.Membership(SquadResponseType.Reject, 0, 0, rejected, Some(rejected), "", true, Some(None))
              )
            case _ => ;
          }
          NextInviteAndRespond(rejectingPlayer)

        case SquadAction.Membership(SquadRequestType.Disband, char_id, _, _, _) =>
          GetLeadingSquad(char_id, None) match {
            case Some(squad) =>
              DisbandSquad(squad)
            case None => ;
          }

        case SquadAction.Membership(SquadRequestType.Cancel, cancellingPlayer, _, _, _) =>
          //get rid of SpontaneousInvite objects and VacancyInvite objects
          invites.collect {
            case (id, invite: SpontaneousInvite) if invite.InviterCharId == cancellingPlayer =>
              RemoveInvite(id)
            case (id, invite: VacancyInvite) if invite.InviterCharId == cancellingPlayer =>
              RemoveInvite(id)
            case (id, invite: LookingForSquadRoleInvite) if invite.InviterCharId == cancellingPlayer =>
              RemoveInvite(id)
          }
          queuedInvites.foreach {
            case (id: Long, inviteList) =>
              val inList = inviteList.filterNot {
                case invite: SpontaneousInvite if invite.InviterCharId == cancellingPlayer         => true
                case invite: VacancyInvite if invite.InviterCharId == cancellingPlayer             => true
                case invite: LookingForSquadRoleInvite if invite.InviterCharId == cancellingPlayer => true
                case _                                                                             => false
              }
              if (inList.isEmpty) {
                queuedInvites.remove(id)
              } else {
                queuedInvites(id) = inList
              }
          }
          //get rid of ProximityInvite objects
          RemoveProximityInvites(cancellingPlayer)

        case SquadAction.Membership(
              SquadRequestType.Promote,
              promotingPlayer,
              Some(_promotedPlayer),
              promotedName,
              _
            ) =>
          val promotedPlayer = (if (promotedName.nonEmpty) {
                                  //validate player with name exists
                                  LivePlayerList
                                    .WorldPopulation({ case (_, a: Avatar) => a.name == promotedName })
                                    .headOption match {
                                    case Some(player) => UserEvents.keys.find(_ == player.id)
                                    case None         => Some(_promotedPlayer)
                                  }
                                } else {
                                  Some(_promotedPlayer)
                                }) match {
            case Some(player) => player
            case None         => -1L
          }
          (GetLeadingSquad(promotingPlayer, None), GetParticipatingSquad(promotedPlayer)) match {
            case (Some(squad), Some(squad2)) if squad.GUID == squad2.GUID =>
              val membership = squad.Membership.filter { _member => _member.CharId > 0 }
              val leader     = squad.Leader
              val (member, index) = membership.zipWithIndex.find {
                case (_member, _) => _member.CharId == promotedPlayer
              }.get
              val features = squadFeatures(squad.GUID)
              SwapMemberPosition(leader, member)
              //move around invites so that the proper squad leader deals with them
              val leaderInvite        = invites.remove(promotingPlayer)
              val leaderQueuedInvites = queuedInvites.remove(promotingPlayer).toList.flatten
              invites.get(promotedPlayer).orElse(previousInvites.get(promotedPlayer)) match {
                case Some(_) =>
                  //the promoted player has an active invite; queue these
                  queuedInvites += promotedPlayer -> (leaderInvite.toList ++ leaderQueuedInvites ++ queuedInvites
                    .remove(promotedPlayer)
                    .toList
                    .flatten)
                case None if leaderInvite.nonEmpty =>
                  //no active invite for the promoted player, but the leader had an active invite; trade the queued invites
                  val invitation = leaderInvite.get
                  AddInviteAndRespond(promotedPlayer, invitation, invitation.InviterCharId, invitation.InviterName)
                  queuedInvites += promotedPlayer -> (leaderQueuedInvites ++ queuedInvites
                    .remove(promotedPlayer)
                    .toList
                    .flatten)
                case None =>
                  //no active invites for anyone; assign the first queued invite from the promoting player, if available, and queue the rest
                  leaderQueuedInvites match {
                    case Nil => ;
                    case x :: xs =>
                      AddInviteAndRespond(promotedPlayer, x, x.InviterCharId, x.InviterName)
                      queuedInvites += promotedPlayer -> (xs ++ queuedInvites.remove(promotedPlayer).toList.flatten)
                  }
              }
              debug(s"Promoting player ${leader.Name} to be the leader of ${squad.Task}")
              Publish(features.ToChannel, SquadResponse.PromoteMember(squad, promotedPlayer, index, 0))
              if (features.Listed) {
                Publish(promotingPlayer, SquadResponse.SetListSquad(PlanetSideGUID(0)))
                Publish(promotedPlayer, SquadResponse.SetListSquad(squad.GUID))
              }
              UpdateSquadListWhenListed(
                features,
                SquadInfo().Leader(leader.Name)
              )
              UpdateSquadDetail(
                squad.GUID,
                SquadDetail()
                  .LeaderCharId(leader.CharId)
                  .Field3(value = 0L)
                  .LeaderName(leader.Name)
                  .Members(
                    List(
                      SquadPositionEntry(0, SquadPositionDetail().CharId(leader.CharId).Name(leader.Name)),
                      SquadPositionEntry(index, SquadPositionDetail().CharId(member.CharId).Name(member.Name))
                    )
                  )
              )
            case _ => ;
          }

        case SquadAction.Membership(event, _, _, _, _) =>
          debug(s"SquadAction.Membership: $event is not yet supported")

        case SquadAction.Waypoint(_, wtype, _, info) =>
          val playerCharId = tplayer.CharId
          (GetLeadingSquad(tplayer, None) match {
            case Some(squad) =>
              info match {
                case Some(winfo) =>
                  (Some(squad), AddWaypoint(squad.GUID, wtype, winfo))
                case _ =>
                  RemoveWaypoint(squad.GUID, wtype)
                  (Some(squad), None)
              }
            case _ => (None, None)
          }) match {
            case (Some(squad), Some(_)) =>
              //waypoint added or updated
              Publish(
                s"${squadFeatures(squad.GUID).ToChannel}",
                SquadResponse.WaypointEvent(WaypointEventAction.Add, playerCharId, wtype, None, info, 1),
                Seq(tplayer.CharId)
              )
            case (Some(squad), None) =>
              //waypoint removed
              Publish(
                s"${squadFeatures(squad.GUID).ToChannel}",
                SquadResponse.WaypointEvent(WaypointEventAction.Remove, playerCharId, wtype, None, None, 0),
                Seq(tplayer.CharId)
              )

            case msg =>
              log.warn(s"Unsupported squad waypoint behavior: $msg")
          }

        case SquadAction.Definition(guid, line, action) =>
          import net.psforever.packet.game.SquadAction._
          val pSquadOpt = GetParticipatingSquad(tplayer)
          val lSquadOpt = GetLeadingSquad(tplayer, pSquadOpt)
          //the following actions can only be performed by a squad's leader
          action match {
            case SaveSquadFavorite() =>
              val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
              if (squad.Task.nonEmpty && squad.ZoneId > 0) {
                tplayer.squadLoadouts.SaveLoadout(squad, squad.Task, line)
                Publish(sender(), SquadResponse.ListSquadFavorite(line, squad.Task))
              }

            case LoadSquadFavorite() =>
              if (pSquadOpt.isEmpty || pSquadOpt == lSquadOpt) {
                val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
                tplayer.squadLoadouts.LoadLoadout(line) match {
                  case Some(loadout: SquadLoadout) if squad.Size == 1 =>
                    SquadService.LoadSquadDefinition(squad, loadout)
                    UpdateSquadListWhenListed(squadFeatures(squad.GUID), SquadService.SquadList.Publish(squad))
                    Publish(sender(), SquadResponse.AssociateWithSquad(PlanetSideGUID(0)))
                    InitSquadDetail(PlanetSideGUID(0), Seq(tplayer.CharId), squad)
                    UpdateSquadDetail(squad)
                    Publish(sender(), SquadResponse.AssociateWithSquad(squad.GUID))
                  case _ =>
                }
              }

            case DeleteSquadFavorite() =>
              tplayer.squadLoadouts.DeleteLoadout(line)
              Publish(sender(), SquadResponse.ListSquadFavorite(line, ""))

            case ChangeSquadPurpose(purpose) =>
              val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
              squad.Task = purpose
              UpdateSquadListWhenListed(squadFeatures(squad.GUID), SquadInfo().Task(purpose))
              UpdateSquadDetail(squad.GUID, SquadDetail().Task(purpose))

            case ChangeSquadZone(zone_id) =>
              val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
              squad.ZoneId = zone_id.zoneId.toInt
              UpdateSquadListWhenListed(squadFeatures(squad.GUID), SquadInfo().ZoneId(zone_id))
              InitialAssociation(squad)
              Publish(sender(), SquadResponse.Detail(squad.GUID, SquadService.Detail.Publish(squad)))
              UpdateSquadDetail(
                squad.GUID,
                squad.GUID,
                Seq(squad.Leader.CharId),
                SquadDetail().ZoneId(zone_id)
              )

            case CloseSquadMemberPosition(position) =>
              val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
              squad.Availability.lift(position) match {
                case Some(true) if position > 0 => //do not close squad leader position; undefined behavior
                  squad.Availability.update(position, false)
                  val memberPosition = squad.Membership(position)
                  if (memberPosition.CharId > 0) {
                    LeaveSquad(memberPosition.CharId, squad)
                  }
                  UpdateSquadListWhenListed(squadFeatures(squad.GUID), SquadInfo().Capacity(squad.Capacity))
                  UpdateSquadDetail(
                    squad.GUID,
                    SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail.Closed)))
                  )
                case Some(_) | None => ;
              }

            case AddSquadMemberPosition(position) =>
              val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
              squad.Availability.lift(position) match {
                case Some(false) =>
                  squad.Availability.update(position, true)
                  UpdateSquadListWhenListed(squadFeatures(squad.GUID), SquadInfo().Capacity(squad.Capacity))
                  UpdateSquadDetail(
                    squad.GUID,
                    SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail.Open)))
                  )
                case Some(true) | None => ;
              }

            case ChangeSquadMemberRequirementsRole(position, role) =>
              val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
              squad.Availability.lift(position) match {
                case Some(true) =>
                  squad.Membership(position).Role = role
                  UpdateSquadDetail(
                    squad.GUID,
                    SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail().Role(role))))
                  )
                case Some(false) | None => ;
              }

            case ChangeSquadMemberRequirementsDetailedOrders(position, orders) =>
              val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
              squad.Availability.lift(position) match {
                case Some(true) =>
                  squad.Membership(position).Orders = orders
                  UpdateSquadDetail(
                    squad.GUID,
                    SquadDetail().Members(
                      List(SquadPositionEntry(position, SquadPositionDetail().DetailedOrders(orders)))
                    )
                  )
                case Some(false) | None => ;
              }

            case ChangeSquadMemberRequirementsCertifications(position, certs) =>
              val squad = lSquadOpt.getOrElse(StartSquad(tplayer))
              squad.Availability.lift(position) match {
                case Some(true) =>
                  squad.Membership(position).Requirements = certs
                  UpdateSquadDetail(
                    squad.GUID,
                    SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail().Requirements(certs))))
                  )
                case Some(false) | None => ;
              }

            case LocationFollowsSquadLead(state) =>
              val features = squadFeatures(lSquadOpt.getOrElse(StartSquad(tplayer)).GUID)
              features.LocationFollowsSquadLead = state

            case AutoApproveInvitationRequests(state) =>
              val features = squadFeatures(lSquadOpt.getOrElse(StartSquad(tplayer)).GUID)
              features.AutoApproveInvitationRequests = state
              if (state) {
                //allowed auto-approval - resolve the requests (only)
                val charId = tplayer.CharId
                val (requests, others) = (invites.get(charId).toList ++ queuedInvites.get(charId).toList)
                  .partition({ case _: RequestRole => true })
                invites.remove(charId)
                queuedInvites.remove(charId)
                previousInvites.remove(charId)
                requests.foreach {
                  case request: RequestRole =>
                    JoinSquad(request.player, features.Squad, request.position)
                  case _ => ;
                }
                others.collect { case invite: Invitation => invite } match {
                  case Nil => ;
                  case x :: Nil =>
                    AddInviteAndRespond(charId, x, x.InviterCharId, x.InviterName)
                  case x :: xs =>
                    AddInviteAndRespond(charId, x, x.InviterCharId, x.InviterName)
                    queuedInvites += charId -> xs
                }
              }

            case FindLfsSoldiersForRole(position) =>
              lSquadOpt match {
                case Some(squad) =>
                  val sguid    = squad.GUID
                  val features = squadFeatures(sguid)
                  features.SearchForRole match {
                    case Some(-1) =>
                      //a proximity invitation has not yet cleared; nothing will be gained by trying to invite for a specific role
                      debug("FindLfsSoldiersForRole: waiting for proximity invitations to clear")
                    case _ =>
                      //either no role has ever been recruited, or some other role has been recruited
                      //normal LFS recruitment for the given position
                      val excusedInvites     = features.Refuse
                      val faction            = squad.Faction
                      val requirementsToMeet = squad.Membership(position).Requirements
                      val outstandingActiveInvites = features.SearchForRole match {
                        case Some(pos) =>
                          RemoveQueuedInvitesForSquadAndPosition(sguid, pos)
                          invites.collect {
                            case (charId, LookingForSquadRoleInvite(_, _, squad_guid, role))
                                if squad_guid == sguid && role == pos =>
                              charId
                          }
                        case None =>
                          List.empty[Long]
                      }
                      features.SearchForRole = position
                      //this will update the role entry in the GUI to visually indicate being searched for; only one will be displayed at a time
                      Publish(
                        tplayer.CharId,
                        SquadResponse.Detail(
                          sguid,
                          SquadDetail().Members(
                            List(
                              SquadPositionEntry(position, SquadPositionDetail().CharId(char_id = 0L).Name(name = ""))
                            )
                          )
                        )
                      )
                      //collect all players that are eligible for invitation to the new position
                      //divide into players with an active invite (A) and players with a queued invite (B)
                      //further filter (A) into players whose invitation is renewed (A1) and new invitations (A2)
                      //TODO only checks the leader's current zone; should check all zones
                      (zone.LivePlayers
                        .collect {
                          case player
                              if !excusedInvites.contains(player.CharId) &&
                                faction == player.Faction && player.avatar.lookingForSquad && !memberToSquad.contains(
                                player.CharId
                              ) &&
                                requirementsToMeet.intersect(player.avatar.certifications) == requirementsToMeet =>
                            player.CharId
                        }
                        .partition { charId => outstandingActiveInvites.exists(charId == _) } match {
                        case (Nil, Nil) =>
                          outstandingActiveInvites foreach RemoveInvite
                          features.ProxyInvites = Nil
                          //TODO cancel the LFS search from the server so that the client updates properly; how?
                          None
                        case (outstandingPlayerList, invitedPlayerList) =>
                          //players who were actively invited for the previous position and are eligible for the new position
                          outstandingPlayerList.foreach { charId =>
                            val bid = invites(charId).asInstanceOf[LookingForSquadRoleInvite]
                            invites(charId) = LookingForSquadRoleInvite(bid.char_id, bid.name, sguid, position)
                          }
                          //players who were actively invited for the previous position but are ineligible for the new position
                          (features.ProxyInvites filterNot (outstandingPlayerList contains)) foreach RemoveInvite
                          features.ProxyInvites = outstandingPlayerList ++ invitedPlayerList
                          Some(invitedPlayerList)
                      }) match {
                        //add invitations for position in squad
                        case Some(invitedPlayers) =>
                          val invitingPlayer = tplayer.CharId
                          val name           = tplayer.Name
                          invitedPlayers.foreach { invitedPlayer =>
                            AddInviteAndRespond(
                              invitedPlayer,
                              LookingForSquadRoleInvite(invitingPlayer, name, sguid, position),
                              invitingPlayer,
                              name
                            )
                          }
                        case None => ;
                      }
                  }

                case _ => ;
              }

            case CancelFind() =>
              lSquadOpt match {
                case Some(squad) =>
                  val sguid    = squad.GUID
                  val position = squadFeatures(sguid).SearchForRole
                  squadFeatures(sguid).SearchForRole = None
                  //remove active invites
                  invites
                    .filter {
                      case (_, LookingForSquadRoleInvite(_, _, _guid, pos)) => _guid == sguid && position.contains(pos)
                      case _                                                => false
                    }
                    .keys
                    .foreach { charId =>
                      RemoveInvite(charId)
                    }
                  //remove queued invites
                  queuedInvites.foreach {
                    case (charId, queue) =>
                      val filtered = queue.filterNot {
                        case LookingForSquadRoleInvite(_, _, _guid, _) => _guid == sguid
                        case _                                         => false
                      }
                      queuedInvites += charId -> filtered
                      if (filtered.isEmpty) {
                        queuedInvites.remove(charId)
                      }
                  }
                  //remove yet-to-be invitedPlayers
                  squadFeatures(sguid).ProxyInvites = Nil
                case _ => ;
              }

            case RequestListSquad() =>
              val squad    = lSquadOpt.getOrElse(StartSquad(tplayer))
              val features = squadFeatures(squad.GUID)
              if (!features.Listed && squad.Task.nonEmpty && squad.ZoneId > 0) {
                features.Listed = true
                InitialAssociation(squad)
                Publish(sender(), SquadResponse.SetListSquad(squad.GUID))
                UpdateSquadList(squad, None)
              }

            case StopListSquad() =>
              val squad    = lSquadOpt.getOrElse(StartSquad(tplayer))
              val features = squadFeatures(squad.GUID)
              if (features.Listed) {
                features.Listed = false
                Publish(sender(), SquadResponse.SetListSquad(PlanetSideGUID(0)))
                UpdateSquadList(squad, None)
              }

            case ResetAll() =>
              lSquadOpt match {
                case Some(squad) if squad.Size > 1 =>
                  val guid = squad.GUID
                  squad.Task = ""
                  squad.ZoneId = None
                  squad.Availability.indices.foreach { i =>
                    squad.Availability.update(i, true)
                  }
                  squad.Membership.foreach(position => {
                    position.Role = ""
                    position.Orders = ""
                    position.Requirements = Set()
                  })
                  val features = squadFeatures(squad.GUID)
                  features.LocationFollowsSquadLead = true
                  features.AutoApproveInvitationRequests = true
                  if (features.Listed) {
                    //unlist the squad
                    features.Listed = false
                    Publish(features.ToChannel, SquadResponse.SetListSquad(PlanetSideGUID(0)))
                    UpdateSquadList(squad, None)
                  }
                  UpdateSquadDetail(squad)
                  InitialAssociation(squad)
                  squadFeatures(guid).InitialAssociation = true
                case Some(squad) =>
                  //underutilized squad; just close it out
                  CloseSquad(squad)
                case _ => ;
              }

            case _ =>
              (pSquadOpt, action) match {
                //the following action can be performed by the squad leader and maybe an unaffiliated player
                case (Some(_), SelectRoleForYourself(_)) =>
                //TODO should be possible, but doesn't work correctly due to UI squad cards not updating as expected
//                if(squad.Leader.CharId == tplayer.CharId) {
//                  //squad leader currently disallowed
//                } else
//                //the squad leader may swap to any open position; a normal member has to validate against requirements
//                if(squad.Leader.CharId == tplayer.CharId || squad.isAvailable(position, tplayer.Certifications)) {
//                  squad.Membership.zipWithIndex.find { case (member, _) => member.CharId == tplayer.CharId } match {
//                    case Some((fromMember, fromIndex)) =>
//                      SwapMemberPosition(squad.Membership(position), fromMember)
//                      Publish(squadFeatures(squad.GUID).ToChannel, SquadResponse.AssignMember(squad, fromIndex, position))
//                      UpdateSquadDetail(squad)
//                    case _ => ;
//                    //somehow, this is not our squad; do nothing, for now
//                  }
//                }
//                else {
//                  //not qualified for requested position
//                }

                //the following action can be performed by an unaffiliated player
                case (None, SelectRoleForYourself(position)) =>
                  //not a member of any squad, but we might become a member of this one
                  GetSquad(guid) match {
                    case Some(squad) =>
                      if (squad.isAvailable(position, tplayer.avatar.certifications)) {
                        //we could join but we may need permission from the squad leader first
                        AddInviteAndRespond(
                          squad.Leader.CharId,
                          RequestRole(tplayer, guid, position),
                          invitingPlayer = 0L, //we ourselves technically are ...
                          tplayer.Name
                        )
                      }
                    case None => ;
                    //squad does not exist? assume old local data; force update to correct discrepancy
                  }

                //the following action can be performed by anyone who has tried to join a squad
                case (_, CancelSelectRoleForYourself(_)) =>
                  val cancellingPlayer = tplayer.CharId
                  GetSquad(guid) match {
                    case Some(squad) =>
                      //assumption: a player who is cancelling will rarely end up with their invite queued
                      val leaderCharId = squad.Leader.CharId
                      //clean up any active RequestRole invite entry where we are the player who wants to join the leader's squad
                      ((invites.get(leaderCharId) match {
                        case out @ Some(entry)
                            if entry.isInstanceOf[RequestRole] &&
                              entry.asInstanceOf[RequestRole].player.CharId == cancellingPlayer =>
                          out
                        case _ =>
                          None
                      }) match {
                        case Some(entry: RequestRole) =>
                          RemoveInvite(leaderCharId)
                          Publish(
                            leaderCharId,
                            SquadResponse.Membership(
                              SquadResponseType.Cancel,
                              0,
                              0,
                              cancellingPlayer,
                              None,
                              entry.player.Name,
                              false,
                              Some(None)
                            )
                          )
                          NextInviteAndRespond(leaderCharId)
                          Some(true)
                        case _ =>
                          None
                      }).orElse(
                        //look for a queued RequestRole entry where we are the player who wants to join the leader's squad
                        (queuedInvites.get(leaderCharId) match {
                          case Some(_list) =>
                            (
                              _list,
                              _list.indexWhere { entry =>
                                entry.isInstanceOf[RequestRole] &&
                                entry.asInstanceOf[RequestRole].player.CharId == cancellingPlayer
                              }
                            )
                          case None =>
                            (Nil, -1)
                        }) match {
                          case (_, -1) =>
                            None //no change
                          case (list, _) if list.size == 1 =>
                            val entry = list.head.asInstanceOf[RequestRole]
                            Publish(
                              leaderCharId,
                              SquadResponse.Membership(
                                SquadResponseType.Cancel,
                                0,
                                0,
                                cancellingPlayer,
                                None,
                                entry.player.Name,
                                false,
                                Some(None)
                              )
                            )
                            queuedInvites.remove(leaderCharId)
                            Some(true)
                          case (list, index) =>
                            val entry = list(index).asInstanceOf[RequestRole]
                            Publish(
                              leaderCharId,
                              SquadResponse.Membership(
                                SquadResponseType.Cancel,
                                0,
                                0,
                                cancellingPlayer,
                                None,
                                entry.player.Name,
                                false,
                                Some(None)
                              )
                            )
                            queuedInvites(leaderCharId) = list.take(index) ++ list.drop(index + 1)
                            Some(true)
                        }
                      )

                    case _ => ;
                  }

                //the following action can be performed by ???
                case (Some(squad), AssignSquadMemberToRole(position, char_id)) =>
                  val membership = squad.Membership.zipWithIndex
                  (membership.find({ case (member, _) => member.CharId == char_id }), membership(position)) match {
                    //TODO squad leader currently disallowed
                    case (Some((fromMember, fromPosition)), (toMember, _)) if fromPosition != 0 =>
                      val name = fromMember.Name
                      SwapMemberPosition(toMember, fromMember)
                      Publish(squadFeatures(guid).ToChannel, SquadResponse.AssignMember(squad, fromPosition, position))
                      UpdateSquadDetail(
                        squad.GUID,
                        SquadDetail().Members(
                          List(
                            SquadPositionEntry(
                              position,
                              SquadPositionDetail().CharId(fromMember.CharId).Name(fromMember.Name)
                            ),
                            SquadPositionEntry(fromPosition, SquadPositionDetail().CharId(char_id).Name(name))
                          )
                        )
                      )
                    case _ => ;
                  }

                //the following action can be peprformed by anyone
                case (
                      _,
                      SearchForSquadsWithParticularRole(
                        _ /*role*/,
                        _ /*requirements*/,
                        _ /*zone_id*/,
                        _ /*search_mode*/
                      )
                    ) =>
                  //though we should be able correctly search squads as is intended
                  //I don't know how search results should be prioritized or even how to return search results to the user
                  Publish(sender(), SquadResponse.SquadSearchResults())

                //the following action can be performed by anyone
                case (_, DisplaySquad()) =>
                  val charId = tplayer.CharId
                  GetSquad(guid) match {
                    case Some(squad) if memberToSquad.get(charId).isEmpty =>
                      continueToMonitorDetails += charId -> squad.GUID
                      Publish(sender(), SquadResponse.Detail(squad.GUID, SquadService.Detail.Publish(squad)))
                    case Some(squad) =>
                      Publish(sender(), SquadResponse.Detail(squad.GUID, SquadService.Detail.Publish(squad)))
                    case _ => ;
                  }

                //the following message is feedback from a specific client, awaiting proper initialization
                case (_, SquadMemberInitializationIssue()) =>
//            GetSquad(guid) match {
//              case Some(squad) =>
//                Publish(sender, SquadResponse.Detail(squad.GUID, SquadService.Detail.Publish(squad)))
//              case None => ;
//            }

                case msg => ;
                  log.warn(s"Unsupported squad definition behavior: $msg")
              }
          }

        case SquadAction.Update(char_id, health, max_health, armor, max_armor, pos, zone_number) =>
          memberToSquad.get(char_id) match {
            case Some(squad) =>
              squad.Membership.find(_.CharId == char_id) match {
                case Some(member) =>
                  member.Health = StatConverter.Health(health, max_health, min = 1, max = 64)
                  member.Armor = StatConverter.Health(armor, max_armor, min = 1, max = 64)
                  member.Position = pos
                  member.ZoneId = zone_number
                  Publish(
                    sender(),
                    SquadResponse.UpdateMembers(
                      squad,
                      squad.Membership
                        .filterNot {
                          _.CharId == 0
                        }
                        .map { member =>
                          SquadAction
                            .Update(member.CharId, member.Health, 0, member.Armor, 0, member.Position, member.ZoneId)
                        }
                        .toList
                    )
                  )
                case _ => ;
              }

            case None => ;
          }

        case msg =>
          debug(s"Unhandled message $msg from ${sender()}")
      }

    case msg =>
      debug(s"Unhandled message $msg from ${sender()}")
  }

  /**
    * This player has refused to join squad leader's squads or some other players's offers to form a squad.
    * @param charId the player who refused other players
    * @return the list of other players who have been refused
    */
  def Refused(charId: Long): List[Long] = refused.getOrElse(charId, Nil)

  /**
    * This player has refused to join squad leader's squads or some other players's offers to form a squad.
    * @param charId the player who is doing the refusal
    * @param refusedCharId the player who is refused
    * @return the list of other players who have been refused
    */
  def Refused(charId: Long, refusedCharId: Long): List[Long] = {
    if (charId != refusedCharId) {
      Refused(charId, List(refusedCharId))
    } else {
      Nil
    }
  }

  /**
    * This player has refused to join squad leader's squads or some other players's offers to form a squad.
    * @param charId the player who is doing the refusal
    * @param list the players who are refused
    * @return the list of other players who have been refused
    */
  def Refused(charId: Long, list: List[Long]): List[Long] = {
    refused.get(charId) match {
      case Some(refusedList) =>
        refused(charId) = list ++ refusedList
        Refused(charId)
      case None =>
        Nil
    }
  }

  /**
    * Assign a provided invitation object to either the active or inactive position for a player.<br>
    * <br>
    * The determination for the active position is whether or not something is currently in the active position
    * or whether some mechanism tried to shift invitation object into the active position
    * but found nothing to shift.
    * If an invitation object originating from the reported player already exists,
    * a new one is not appended to the inactive queue.
    * This method should always be used as the entry point for the active and inactive invitation options
    * or as a part of the entry point for the aforesaid options.
    * @see `AddInviteAndRespond`
    * @see `AltAddInviteAndRespond`
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @param invite the "new" invitation envelop object
    * @return an optional invite;
    *         the invitation object in the active invite position;
    *         `None`, if it is not added to either the active option or inactive position
    */
  def AddInvite(invitedPlayer: Long, invite: Invitation): Option[Invitation] = {
    invites.get(invitedPlayer).orElse(previousInvites.get(invitedPlayer)) match {
      case Some(_bid) =>
        //the active invite does not interact with the given invite; add to queued invites
        queuedInvites.get(invitedPlayer) match {
          case Some(bidList) =>
            //ensure that new invite does not interact with the queue's invites by invitingPlayer info
            if (
              _bid.InviterCharId != invite.InviterCharId && !bidList.exists { eachBid =>
                eachBid.InviterCharId == invite.InviterCharId
              }
            ) {
              queuedInvites(invitedPlayer) = invite match {
                case _: RequestRole =>
                  val (normals, others) = bidList.partition(_.isInstanceOf[RequestRole])
                  (normals :+ invite) ++ others
                case _ =>
                  bidList :+ invite
              }
              Some(_bid)
            } else {
              None
            }
          case None =>
            if (_bid.InviterCharId != invite.InviterCharId) {
              queuedInvites(invitedPlayer) = List[Invitation](invite)
              Some(_bid)
            } else {
              None
            }
        }

      case None =>
        invites(invitedPlayer) = invite
        Some(invite)
    }
  }

  /**
    * Component method used for the response behavior for processing the invitation object as an `IndirectInvite` object.
    * @see `HandleRequestRole`
    * @param invite the original invitation object that started this process
    * @param player the target of the response and invitation
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object;
    *                      not useful here
    * @param invitingPlayer the unique character identifier for the player who invited the former;
    *                       not useful here
    * @param name a name to be used in message composition;
    *             not useful here
    * @return na
    */
  def indirectInviteResp(
      invite: IndirectInvite,
      player: Player,
      invitedPlayer: Long,
      invitingPlayer: Long,
      name: String
  ): Boolean = {
    HandleRequestRole(invite, player)
  }

  /**
    * Component method used for the response behavior for processing the invitation object as an `IndirectInvite` object.
    * @see `HandleRequestRole`
    * @param invite the original invitation object that started this process
    * @param player the target of the response and invitation
    * @param invitedPlayer the unique character identifier for the player being invited
    *                      in actuality, represents the player who will address the invitation object
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param name a name to be used in message composition
    * @return na
    */
  def altIndirectInviteResp(
      invite: IndirectInvite,
      player: Player,
      invitedPlayer: Long,
      invitingPlayer: Long,
      name: String
  ): Boolean = {
    Publish(
      invitingPlayer,
      SquadResponse.Membership(
        SquadResponseType.Accept,
        0,
        0,
        invitingPlayer,
        Some(invitedPlayer),
        player.Name,
        false,
        Some(None)
      )
    )
    HandleRequestRole(invite, player)
  }

  /**
    * A branched response for processing (new) invitation objects that have been submitted to the system.<br>
    * <br>
    * A comparison is performed between the original invitation object and an invitation object
    * that represents the potential modification or redirection of the current active invitation obect.
    * Any further action is only performed when an "is equal" comparison is `true`.
    * When passing, the system publishes up to two messages
    * to users that would anticipate being informed of squad join activity.
    * @param indirectVacancyFunc the method that cans the respondign behavior should an `IndirectVacancy` object being consumed
    * @param targetInvite a comparison invitation object;
    *                     represents the unmodified, unadjusted invite
    * @param actualInvite a comparaison invitation object;
    *                     proper use of this field should be the output of another process upon the following `actualInvite`
    * @param invitedPlayer the unique character identifier for the player being invited
    *                      in actuality, represents the player who will address the invitation object
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param name a name to be used in message composition
    */
  def InviteResponseTemplate(indirectVacancyFunc: (IndirectInvite, Player, Long, Long, String) => Boolean)(
      targetInvite: Invitation,
      actualInvite: Option[Invitation],
      invitedPlayer: Long,
      invitingPlayer: Long,
      name: String
  ): Unit = {
    if (actualInvite.contains(targetInvite)) {
      //immediately respond
      targetInvite match {
        case VacancyInvite(charId, _name, _) =>
          Publish(
            invitedPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              charId,
              Some(invitedPlayer),
              _name,
              false,
              Some(None)
            )
          )
          Publish(
            charId,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              invitedPlayer,
              Some(charId),
              _name,
              true,
              Some(None)
            )
          )

        case _bid @ IndirectInvite(player, _) =>
          indirectVacancyFunc(_bid, player, invitedPlayer, invitingPlayer, name)

        case _bid @ SpontaneousInvite(player) =>
          val bidInvitingPlayer = _bid.InviterCharId
          Publish(
            invitedPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              bidInvitingPlayer,
              Some(invitedPlayer),
              player.Name,
              false,
              Some(None)
            )
          )
          Publish(
            bidInvitingPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              invitedPlayer,
              Some(bidInvitingPlayer),
              player.Name,
              true,
              Some(None)
            )
          )

        case _bid @ RequestRole(player, _, _) =>
          HandleRequestRole(_bid, player)

        case LookingForSquadRoleInvite(charId, _name, _, _) =>
          Publish(
            invitedPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              invitedPlayer,
              Some(charId),
              _name,
              false,
              Some(None)
            )
          )

        case ProximityInvite(charId, _name, _) =>
          Publish(
            invitedPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              invitedPlayer,
              Some(charId),
              _name,
              false,
              Some(None)
            )
          )

        case _ =>
          log.warn(s"AddInviteAndRespond: can not parse discovered unhandled invitation type - $targetInvite")
      }
    }
  }

  /**
    * Enqueue a newly-submitted invitation object
    * either as the active position or into the inactive positions
    * and dispatch a response for any invitation object that is discovered.
    * Implementation of a workflow.
    * @see `AddInvite`
    * @see `indirectInviteResp`
    * @param targetInvite a comparison invitation object
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param name a name to be used in message composition
    */
  def AddInviteAndRespond(invitedPlayer: Long, targetInvite: Invitation, invitingPlayer: Long, name: String): Unit = {
    InviteResponseTemplate(indirectInviteResp)(
      targetInvite,
      AddInvite(invitedPlayer, targetInvite),
      invitedPlayer,
      invitingPlayer,
      name
    )
  }

  /**
    * Enqueue a newly-submitted invitation object
    * either as the active position or into the inactive positions
    * and dispatch a response for any invitation object that is discovered.
    * Implementation of a workflow.
    * @see `AddInvite`
    * @see `altIndirectInviteResp`
    * @param targetInvite a comparison invitation object
    * @param invitedPlayer the unique character identifier for the player being invited
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param name a name to be used in message composition
    */
  def AltAddInviteAndRespond(
      invitedPlayer: Long,
      targetInvite: Invitation,
      invitingPlayer: Long,
      name: String
  ): Unit = {
    InviteResponseTemplate(altIndirectInviteResp)(
      targetInvite,
      AddInvite(invitedPlayer, targetInvite),
      invitedPlayer,
      invitingPlayer,
      name
    )
  }

  /**
    * Select the next invitation object to be shifted into the active position.<br>
    * <br>
    * The determination for the active position is whether or not something is currently in the active position
    * or whether some mechanism tried to shift invitation object into the active position
    * but found nothing to shift.
    * After handling of the previous invitation object has completed or finished,
    * the temporary block on adding new invitations is removed
    * and any queued inactive invitation on the head of the inactive queue is shifted into the active position.
    * @see `NextInviteAndRespond`
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @return an optional invite;
    *         the invitation object in the active invite position;
    *         `None`, if not shifted into the active position
    */
  def NextInvite(invitedPlayer: Long): Option[Invitation] = {
    previousInvites.remove(invitedPlayer)
    invites.get(invitedPlayer) match {
      case None =>
        queuedInvites.get(invitedPlayer) match {
          case Some(list) =>
            list match {
              case Nil =>
                None
              case x :: Nil =>
                invites(invitedPlayer) = x
                queuedInvites.remove(invitedPlayer)
                Some(x)
              case x :: xs =>
                invites(invitedPlayer) = x
                queuedInvites(invitedPlayer) = xs
                Some(x)
            }

          case None =>
            None
        }
      case Some(_) =>
        None
    }
  }

  /**
    * Select the next invitation object to be shifted into the active position
    * and dispatch a response for any invitation object that is discovered.
    * @see `InviteResponseTemplate`
    * @see `NextInvite`
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @return an optional invite;
    *         the invitation object in the active invite position;
    *         `None`, if not shifted into the active position
    */
  def NextInviteAndRespond(invitedPlayer: Long): Unit = {
    NextInvite(invitedPlayer) match {
      case Some(invite) =>
        InviteResponseTemplate(indirectInviteResp)(
          invite,
          Some(invite),
          invitedPlayer,
          invite.InviterCharId,
          invite.InviterName
        )
      case None => ;
    }
  }

  /**
    * Remove any invitation object from the active position.
    * Flag the temporary field to indicate that the active position, while technically available,
    * should not yet have a new invitation object shifted into it yet.
    * This is the "proper" way to demote invitation objects from the active position
    * whether or not they are to be handled.
    * @see `NextInvite`
    * @see `NextInviteAndRespond`
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @return an optional invite;
    *         the invitation object formerly in the active invite position;
    *         `None`, if no invitation was in the active position
    */
  def RemoveInvite(invitedPlayer: Long): Option[Invitation] = {
    invites.remove(invitedPlayer) match {
      case out @ Some(invite) =>
        previousInvites += invitedPlayer -> invite
        out
      case None =>
        None
    }
  }

  /**
    * Remove all inactive invites.
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @return a list of the removed inactive invitation objects
    */
  def RemoveQueuedInvites(invitedPlayer: Long): List[Invitation] = {
    queuedInvites.remove(invitedPlayer) match {
      case Some(_bidList) => _bidList
      case None           => Nil
    }
  }

  /**
    * Remove all active invitation objects that are related to the particular squad and the particular role in the squad.
    * Specifically used to safely disarm obsolete invitation objects related to the specific criteria.
    * Affects only certain invitation object types.
    * @see `RequestRole`
    * @see `LookingForSquadRoleInvite`
    * @see `RemoveInvite`
    * @see `RemoveQueuedInvitesForSquadAndPosition`
    * @param guid the squad identifier
    * @param position the role position index
    */
  def RemoveInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): Unit = {
    //eliminate active invites for this role
    invites.collect {
      case (charId, LookingForSquadRoleInvite(_, _, sguid, pos)) if sguid == guid && pos == position =>
        RemoveInvite(charId)
      case (charId, RequestRole(_, sguid, pos)) if sguid == guid && pos == position =>
        RemoveInvite(charId)
    }
    RemoveQueuedInvitesForSquadAndPosition(guid, position)
  }

  /**
    * Remove all inactive invitation objects that are related to the particular squad and the particular role in the squad.
    * Specifically used to safely disarm obsolete invitation objects by specific criteria.
    * Affects only certain invitation object types.
    * @see `RequestRole`
    * @see `LookingForSquadRoleInvite`
    * @see `RemoveInvitesForSquadAndPosition`
    * @param guid the squad identifier
    * @param position the role position index
    */
  def RemoveQueuedInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): Unit = {
    //eliminate other invites for this role
    queuedInvites.foreach {
      case (charId, queue) =>
        val filtered = queue.filterNot {
          case LookingForSquadRoleInvite(_, _, sguid, pos) => sguid == guid && pos == position
          case RequestRole(_, sguid, pos)                  => sguid == guid && pos == position
          case _                                           => false
        }
        if (filtered.isEmpty) {
          queuedInvites.remove(charId)
        } else if (queue.size != filtered.size) {
          queuedInvites += charId -> filtered
        }
    }
  }

  /**
    * Remove all active and inactive invitation objects that are related to the particular squad.
    * Specifically used to safely disarm obsolete invitation objects by specific criteria.
    * Affects all invitation object types and all data structures that deal with the squad.
    * @see `RequestRole`
    * @see `IndirectInvite`
    * @see `LookingForSquadRoleInvite`
    * @see `ProximityInvite`
    * @see `RemoveInvite`
    * @see `VacancyInvite`
    * @param sguid the squad identifier
    */
  def RemoveAllInvitesToSquad(sguid: PlanetSideGUID): Unit = {
    //clean up invites
    invites.collect {
      case (id, VacancyInvite(_, _, guid)) if sguid == guid =>
        RemoveInvite(id)
      case (id, IndirectInvite(_, guid)) if sguid == guid =>
        RemoveInvite(id)
      case (id, LookingForSquadRoleInvite(_, _, guid, _)) if sguid == guid =>
        RemoveInvite(id)
      case (id, RequestRole(_, guid, _)) if sguid == guid =>
        RemoveInvite(id)
      case (id, ProximityInvite(_, _, guid)) if sguid == guid =>
        RemoveInvite(id)
    }
    //tidy the queued invitations
    queuedInvites.foreach {
      case (id, queue) =>
        val filteredQueue = queue.filterNot {
          case VacancyInvite(_, _, guid)                => sguid == guid
          case IndirectInvite(_, guid)                  => sguid == guid
          case LookingForSquadRoleInvite(_, _, guid, _) => sguid == guid
          case RequestRole(_, guid, _)                  => sguid == guid
          case ProximityInvite(_, _, guid)              => sguid == guid
          case _                                        => false
        }
        if (filteredQueue.isEmpty) {
          queuedInvites.remove(id)
        } else if (filteredQueue.size != queue.size) {
          queuedInvites.update(id, filteredQueue)
        }
    }
    squadFeatures(sguid).ProxyInvites = Nil
    squadFeatures(sguid).SearchForRole match {
      case None => ;
      case Some(_) =>
        squadFeatures(sguid).SearchForRole = None
    }
    continueToMonitorDetails.collect {
      case (charId, guid) if sguid == guid =>
        continueToMonitorDetails.remove(charId)
    }
  }

  /**
    * Remove all active and inactive invitation objects that are related to the particular player.
    * Specifically used to safely disarm obsolete invitation objects by specific criteria.
    * Affects all invitation object types and all data structures that deal with the player.
    * @see `RequestRole`
    * @see `IndirectInvite`
    * @see `LookingForSquadRoleInvite`
    * @see `RemoveInvite`
    * @see `RemoveProximityInvites`
    * @see `VacancyInvite`
    * @param charId the player's unique identifier number
    */
  def RemoveAllInvitesWithPlayer(charId: Long): Unit = {
    RemoveInvite(charId)
    invites.collect {
      case (id, SpontaneousInvite(player)) if player.CharId == charId =>
        RemoveInvite(id)
      case (id, VacancyInvite(_charId, _, _)) if _charId == charId =>
        RemoveInvite(id)
      case (id, IndirectInvite(player, _)) if player.CharId == charId =>
        RemoveInvite(id)
      case (id, LookingForSquadRoleInvite(_charId, _, _, _)) if _charId == charId =>
        RemoveInvite(id)
      case (id, RequestRole(player, _, _)) if player.CharId == charId =>
        RemoveInvite(id)
    }
    //tidy the queued invitations
    queuedInvites.remove(charId)
    queuedInvites.foreach {
      case (id, queue) =>
        val filteredQueue = queue.filterNot {
          case SpontaneousInvite(player)                  => player.CharId == charId
          case VacancyInvite(player, _, _)                => player == charId
          case IndirectInvite(player, _)                  => player.CharId == charId
          case LookingForSquadRoleInvite(player, _, _, _) => player == charId
          case RequestRole(player, _, _)                  => player.CharId == charId
          case _                                          => false
        }
        if (filteredQueue.isEmpty) {
          queuedInvites.remove(id)
        } else if (filteredQueue.size != queue.size) {
          queuedInvites.update(id, filteredQueue)
        }
    }
    continueToMonitorDetails.remove(charId)
    RemoveProximityInvites(charId)
  }

  /**
    * Remove all active and inactive proximity squad invites related to the recruiter.
    * @see `RemoveProximityInvites(Iterable[(Long, PlanetSideGUID)])`
    * @param invitingPlayer the player who did the recruiting
    * @return a list of all players (unique character identifier number and name) who had active proximity invitations
    */
  def RemoveProximityInvites(invitingPlayer: Long): Iterable[(Long, String)] = {
    //invites
    val (removedInvites, out) = invites.collect {
      case (id, ProximityInvite(inviterCharId, inviterName, squadGUID)) if inviterCharId == invitingPlayer =>
        RemoveInvite(id)
        ((id, squadGUID), (id, inviterName))
    }.unzip
    RemoveProximityInvites(removedInvites)
    //queued
    RemoveProximityInvites(queuedInvites.flatMap {
      case (id: Long, inviteList) =>
        val (outList, inList) = inviteList.partition {
          case ProximityInvite(inviterCharId, _, _) if inviterCharId == invitingPlayer => true
          case _                                                                       => false
        }
        if (inList.isEmpty) {
          queuedInvites.remove(id)
        } else {
          queuedInvites(id) = inList
        }
        outList.collect { case ProximityInvite(_, _, sguid: PlanetSideGUID) => id -> sguid }
    })
    out.toSeq.distinct
  }

  /**
    * Remove all queued proximity squad invite information retained by the squad object.
    * @see `RemoveProximityInvites(Long)`
    * @see `SquadFeatures.ProxyInvites`
    * @see `SquadFeatures.SearchForRole`
    * @param list a list of players to squads with expected entry redundancy
    */
  def RemoveProximityInvites(list: Iterable[(Long, PlanetSideGUID)]): Unit = {
    val (_, squads) = list.unzip
    squads.toSeq.distinct.foreach { squad =>
      squadFeatures.get(squad) match {
        case Some(features) =>
          val out = list.collect { case (id, sguid) if sguid == squad => id }.toSeq
          if ((features.ProxyInvites = features.ProxyInvites filterNot out.contains) isEmpty) {
            features.SearchForRole = None
          }
        case _ => ;
      }
    }
  }

  /**
    * Remove all active and inactive proximity squad invites for a specific squad.
    * @param guid the squad
    * @return a list of all players (unique character identifier number and name) who had active proximity invitations
    */
  def RemoveProximityInvites(guid: PlanetSideGUID): Iterable[(Long, String)] = {
    //invites
    val (removedInvites, out) = invites.collect {
      case (id, ProximityInvite(_, inviterName, squadGUID)) if squadGUID == guid =>
        RemoveInvite(id)
        (squadGUID, (id, inviterName))
    }.unzip
    removedInvites.foreach { sguid =>
      squadFeatures(sguid).ProxyInvites = Nil
      squadFeatures(sguid).SearchForRole = None
    }
    //queued
    queuedInvites.flatMap {
      case (id: Long, inviteList) =>
        val (outList, inList) = inviteList.partition {
          case ProximityInvite(_, _, squadGUID) if squadGUID == guid => true
          case _                                                     => false
        }
        if (inList.isEmpty) {
          queuedInvites.remove(id)
        } else {
          queuedInvites(id) = inList
        }
        outList.collect {
          case ProximityInvite(_, _, sguid: PlanetSideGUID) =>
            squadFeatures(sguid).ProxyInvites = Nil
            squadFeatures(sguid).SearchForRole = None
        }
    }
    out.toSeq.distinct
  }

  /**
    * Resolve an invitation to a general, not guaranteed, position in someone else's squad.
    * For the moment, just validate the provided parameters and confirm the eligibility of the user.
    * @see `VacancyInvite`
    * @param squad_guid the unique squad identifier number
    * @param invitedPlayer the unique character identifier for the player being invited
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param recruit the player being invited
    * @return the squad object and a role position index, if properly invited;
    *         `None`, otherwise
    */
  def HandleVacancyInvite(
      squad_guid: PlanetSideGUID,
      invitedPlayer: Long,
      invitingPlayer: Long,
      recruit: Player
  ): Option[(Squad, Int)] = {
    squadFeatures.get(squad_guid) match {
      case Some(features) =>
        val squad = features.Squad
        memberToSquad.get(invitedPlayer) match {
          case Some(enrolledSquad) =>
            if (enrolledSquad eq squad) {
              log.warn(s"HandleVacancyInvite: ${recruit.Name} is already a member of squad ${squad.Task}")
            } else {
              log.warn(
                s"HandleVacancyInvite: ${recruit.Name} is a member of squad ${enrolledSquad.Task} and can not join squad ${squad.Task}"
              )
            }
            None
          case _ =>
            HandleVacancyInvite(squad, invitedPlayer, invitingPlayer, recruit)
        }

      case _ =>
        log.warn(s"HandleVacancyInvite: the squad #${squad_guid.guid} no longer exists")
        None
    }
  }

  /**
    * Resolve an invitation to a general, not guaranteed, position in someone else's squad.<br>
    * <br>
    * Originally, the instigating type of invitation object was a "`VacancyInvite`"
    * which indicated a type of undirected invitation extended from the squad leader to another player
    * but the resolution is generalized enough to suffice for a number of invitation objects.
    * First, an actual position is determined;
    * then, the squad is tested for recruitment conditions,
    * including whether the person who solicited the would-be member is still the squad leader.
    * If the recruitment is manual and the squad leader is not the same as the recruiting player,
    * then the real squad leader is sent an indirect query regarding the player's eligibility.
    * These `IndirectInvite` invitation objects also are handled by calls to `HandleVacancyInvite`.
    * @see `AltAddInviteAndRespond`
    * @see `IndirectInvite`
    * @see `SquadFeatures::AutoApproveInvitationRequests`
    * @see `VacancyInvite`
    * @param squad the squad
    * @param invitedPlayer the unique character identifier for the player being invited
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param recruit the player being invited
    * @return the squad object and a role position index, if properly invited;
    *         `None`, otherwise
    */
  def HandleVacancyInvite(
      squad: Squad,
      invitedPlayer: Long,
      invitingPlayer: Long,
      recruit: Player
  ): Option[(Squad, Int)] = {
    //accepted an invitation to join an existing squad
    squad.Membership.zipWithIndex.find({
      case (_, index) =>
        squad.isAvailable(index, recruit.avatar.certifications)
    }) match {
      case Some((_, line)) =>
        //position in squad found
        val sguid    = squad.GUID
        val features = squadFeatures(sguid)
        if (!features.AutoApproveInvitationRequests && squad.Leader.CharId != invitingPlayer) {
          //the inviting player was not the squad leader and this decision should be bounced off the squad leader
          AltAddInviteAndRespond(
            squad.Leader.CharId,
            IndirectInvite(recruit, sguid),
            invitingPlayer,
            name = ""
          )
          debug(s"HandleVacancyInvite: ${recruit.Name} must await an invitation from the leader of squad ${squad.Task}")
          None
        } else {
          Some((squad, line))
        }
      case _ =>
        None
    }
  }

  /**
    * An overloaded entry point to the functionality for handling one player requesting a specific squad role.
    * @param bid a specific kind of `Invitation` object
    * @param player the player who wants to join the squad
    * @return `true`, if the player is not denied the possibility of joining the squad;
    *        `false`, otherwise, of it the squad does not exist
    */
  def HandleRequestRole(bid: RequestRole, player: Player): Boolean = {
    HandleRequestRole(bid, bid.squad_guid, player)
  }

  /**
    * An overloaded entry point to the functionality for handling indirection when messaging the squad leader about an invite.
    * @param bid a specific kind of `Invitation` object
    * @param player the player who wants to join the squad
    * @return `true`, if the player is not denied the possibility of joining the squad;
    *        `false`, otherwise, of it the squad does not exist
    */
  def HandleRequestRole(bid: IndirectInvite, player: Player): Boolean = {
    HandleRequestRole(bid, bid.squad_guid, player)
  }

  /**
    * The functionality for handling indirection
    * for handling one player requesting a specific squad role
    * or when messaging the squad leader about an invite.<br>
    * <br>
    * At this point in the squad join process, the only consent required is that of the squad leader.
    * An automatic consent flag exists on the squad;
    * but, if that is not set, then the squad leader must be asked whether or not to accept or to reject the recruit.
    * If the squad leader changes in the middle of the latter half of the process,
    * the invitation may still fail even if the old squad leader accepts.
    * If the squad leader changes in the middle of the latter half of the process,
    * the inquiry might be posed again of the new squad leader, of whether to accept or to reject the recruit.
    * @param bid the `Invitation` object that was the target of this request
    * @param guid the unique squad identifier number
    * @param player the player who wants to join the squad
    * @return `true`, if the player is not denied the possibility of joining the squad;
    *        `false`, otherwise, of it the squad does not exist
    */
  def HandleRequestRole(bid: Invitation, guid: PlanetSideGUID, player: Player): Boolean = {
    squadFeatures.get(guid) match {
      case Some(features) =>
        val leaderCharId = features.Squad.Leader.CharId
        if (features.AutoApproveInvitationRequests) {
          self ! SquadServiceMessage(
            player,
            Zone.Nowhere,
            SquadAction.Membership(SquadRequestType.Accept, leaderCharId, None, "", None)
          )
        } else {
          Publish(leaderCharId, SquadResponse.WantsSquadPosition(leaderCharId, player.Name))
        }
        true
      case _ =>
        //squad is missing
        log.error(s"Attempted to process ${bid.InviterName}'s bid for a position in a squad that does not exist")
        false
    }
  }

  /**
    * Pertains to the original message of squad synchronicity sent to the squad leader by the server under specific conditions.
    * The initial formation of a squad of two players is the most common expected situation.
    * While the underlying flag is normally only set once, its state can be reset and triggered anew if necessary.
    * @see `Publish`
    * @see ``ResetAll
    * @see `SquadResponse.AssociateWithSquad`
    * @see `SquadResponse.Detail`
    * @see `SquadService.Detail.Publish`
    * @param squad the squad
    */
  def InitialAssociation(squad: Squad): Unit = {
    val guid = squad.GUID
    if (squadFeatures(guid).InitialAssociation) {
      squadFeatures(guid).InitialAssociation = false
      val charId = squad.Leader.CharId
      Publish(charId, SquadResponse.AssociateWithSquad(guid))
      Publish(charId, SquadResponse.Detail(guid, SquadService.Detail.Publish(squad)))
    }
  }

  /**
    * Establish a new squad.
    * Create all of the support structures for the squad and link into them.
    * At a minimum, by default, the squad needs a squad leader
    * and a stronger, more exposed connection between the squad and leader needs to be recognized.<br>
    * <br>
    * Usually, a squad is created by modifying some aspect of its necessary fields.
    * The primary necessary fields required for a squad include the squad's task and the squad's zone of operation.
    * @see `GetNextSquadId`
    * @see `Squad`
    * @see `SquadFeatures`
    * @see `SquadFeatures::Start`
    * @param player the player who would become the squad leader
    * @return the squad that has been created
    */
  def StartSquad(player: Player): Squad = {
    val faction      = player.Faction
    val name         = player.Name
    val squad        = new Squad(GetNextSquadId(), faction)
    val leadPosition = squad.Membership(0)
    leadPosition.Name = name
    leadPosition.CharId = player.CharId
    leadPosition.Health = StatConverter.Health(player.Health, player.MaxHealth, min = 1, max = 64)
    leadPosition.Armor = StatConverter.Health(player.Armor, player.MaxArmor, min = 1, max = 64)
    leadPosition.Position = player.Position
    leadPosition.ZoneId = 1
    squadFeatures += squad.GUID          -> new SquadFeatures(squad).Start
    memberToSquad += squad.Leader.CharId -> squad
    debug(s"$name-$faction has created a new squad")
    squad
  }

  /**
    * Behaviors and exchanges necessary to complete the fulfilled recruitment process for the squad role.<br>
    * <br>
    * This operation is fairly safe to call whenever a player is to be inducted into a squad.
    * The aforementioned player must have a callback retained in `UserEvents`
    * and conditions imposed by both the role and the player must be satisfied.
    * @see `InitialAssociation`
    * @see `InitSquadDetail`
    * @see `InitWaypoints`
    * @see `Publish`
    * @see `RemoveAllInvitesWithPlayer`
    * @see `SquadDetail`
    * @see `SquadInfo`
    * @see `SquadPositionDetail`
    * @see `SquadPositionEntry`
    * @see `SquadResponse.Join`
    * @see `StatConverter.Health`
    * @see `UpdateSquadListWhenListed`
    * @param player the new squad member;
    *               this player is NOT the squad leader
    * @param squad the squad the player is joining
    * @param position the squad member role that the player will be filling
    * @return `true`, if the player joined the squad in some capacity;
    *         `false`, if the player did not join the squad or is already a squad member
    */
  def JoinSquad(player: Player, squad: Squad, position: Int): Boolean = {
    val charId = player.CharId
    val role   = squad.Membership(position)
    UserEvents.get(charId) match {
      case Some(events) if squad.Leader.CharId != charId && squad.isAvailable(position, player.avatar.certifications) =>
        role.Name = player.Name
        role.CharId = charId
        role.Health = StatConverter.Health(player.Health, player.MaxHealth, min = 1, max = 64)
        role.Armor = StatConverter.Health(player.Armor, player.MaxArmor, min = 1, max = 64)
        role.Position = player.Position
        role.ZoneId = 1
        memberToSquad(charId) = squad

        continueToMonitorDetails.remove(charId)
        RemoveAllInvitesWithPlayer(charId)
        InitialAssociation(squad)
        Publish(charId, SquadResponse.AssociateWithSquad(squad.GUID))
        val features = squadFeatures(squad.GUID)
        val size     = squad.Size
        if (size == 2) {
          //first squad member after leader; both members fully initialize
          val (memberCharIds, indices) = squad.Membership.zipWithIndex
            .filterNot { case (member, _) => member.CharId == 0 }
            .toList
            .unzip { case (member, index) => (member.CharId, index) }
          val toChannel = features.ToChannel
          memberCharIds.foreach { charId =>
            SquadEvents.subscribe(events, s"/$toChannel/Squad")
            Publish(
              charId,
              SquadResponse.Join(
                squad,
                indices.filterNot(_ == position) :+ position,
                toChannel
              )
            )
            InitWaypoints(charId, squad.GUID)
          }
          //fully update for all users
          InitSquadDetail(squad)
        } else {
          //joining an active squad; everybody updates differently
          val updatedIndex = List(position)
          val toChannel    = features.ToChannel
          //new member gets full squad UI updates
          Publish(
            charId,
            SquadResponse.Join(
              squad,
              position +: squad.Membership.zipWithIndex
                .collect({ case (member, index) if member.CharId > 0 => index })
                .filterNot(_ == position)
                .toList,
              toChannel
            )
          )
          //other squad members see new member joining the squad
          Publish(toChannel, SquadResponse.Join(squad, updatedIndex, ""))
          InitWaypoints(charId, squad.GUID)
          InitSquadDetail(squad.GUID, Seq(charId), squad)
          UpdateSquadDetail(
            squad.GUID,
            SquadDetail().Members(
              List(SquadPositionEntry(position, SquadPositionDetail().CharId(charId).Name(player.Name)))
            )
          )
          SquadEvents.subscribe(events, s"/$toChannel/Squad")
        }
        UpdateSquadListWhenListed(features, SquadInfo().Size(size))
        true
      case _ =>
        false
    }
  }

  /**
    * Determine whether a player is sufficiently unemployed
    * and has no grand delusions of being a squad leader.
    * @see `CloseSquad`
    * @param charId the player
    * @return `true`, if the target player possesses no squad or a squad that is suitably nonexistent;
    *        `false`, otherwise
    */
  def EnsureEmptySquad(charId: Long): Boolean = {
    memberToSquad.get(charId) match {
      case None =>
        true
      case Some(squad) if squad.Size == 1 =>
        CloseSquad(squad)
        true
      case _ =>
        log.warn("EnsureEmptySquad: the invited player is already a member of a squad and can not join a second one")
        false
    }
  }

  /**
    * Behaviors and exchanges necessary to undo the recruitment process for the squad role.
    * @see `PanicLeaveSquad`
    * @see `Publish`
    * @param charId the player
    * @param squad the squad
    * @return `true`, if the player, formerly a normal member of the squad, has been ejected from the squad;
    *        `false`, otherwise
    */
  def LeaveSquad(charId: Long, squad: Squad): Boolean = {
    val membership = squad.Membership.zipWithIndex
    membership.find { case (_member, _) => _member.CharId == charId } match {
      case data @ Some((_, index)) if squad.Leader.CharId != charId =>
        PanicLeaveSquad(charId, squad, data)
        //member leaves the squad completely (see PanicSquadLeave)
        Publish(
          charId,
          SquadResponse.Leave(
            squad,
            (charId, index) +: membership.collect {
              case (_member, _index) if _member.CharId > 0 && _member.CharId != charId => (_member.CharId, _index)
            }.toList
          )
        )
        SquadEvents.unsubscribe(UserEvents(charId), s"/${squadFeatures(squad.GUID).ToChannel}/Squad")
        true
      case _ =>
        false
    }
  }

  /**
    * Behaviors and exchanges necessary to undo the recruitment process for the squad role.<br>
    * <br>
    * The complement of the prior `LeaveSquad` method.
    * This method deals entirely with other squad members observing the given squad member leaving the squad
    * while the other method handles messaging only for the squad member who is leaving.
    * The distinction is useful when unsubscribing from this service,
    * as the `ActorRef` object used to message the player's client is no longer reliable
    * and has probably ceased to exist.
    * @see `LeaveSquad`
    * @see `Publish`
    * @see `SquadDetail`
    * @see `SquadInfo`
    * @see `SquadPositionDetail`
    * @see `SquadPositionEntry`
    * @see `SquadResponse.Leave`
    * @see `UpdateSquadDetail`
    * @see `UpdateSquadListWhenListed`
    * @param charId the player
    * @param squad the squad
    * @param entry a paired membership role with its index in the squad
    * @return if a role/index pair is provided
    */
  def PanicLeaveSquad(charId: Long, squad: Squad, entry: Option[(Member, Int)]): Boolean = {
    entry match {
      case Some((member, index)) =>
        val entry = (charId, index)
        //member leaves the squad completely
        memberToSquad.remove(charId)
        member.Name = ""
        member.CharId = 0
        //other squad members see the member leaving
        Publish(squadFeatures(squad.GUID).ToChannel, SquadResponse.Leave(squad, List(entry)), Seq(charId))
        UpdateSquadListWhenListed(squadFeatures(squad.GUID), SquadInfo().Size(squad.Size))
        UpdateSquadDetail(
          squad.GUID,
          SquadDetail().Members(List(SquadPositionEntry(index, SquadPositionDetail().Player(char_id = 0, name = ""))))
        )
        true
      case None =>
        false
    }
  }

  /**
    * All players are made to leave the squad and the squad will stop existing.
    * Any member of the squad missing an `ActorRef` object used to message the player's client
    * will still leave the squad, but will not attempt to send feedback to the said unreachable client.
    * If the player is in the process of unsubscribing from the service,
    * the no-messaging pathway is useful to avoid accumulating dead letters.
    * @see `Publish`
    * @see `RemoveAllInvitesToSquad`
    * @see `SquadDetail`
    * @see `TryResetSquadId`
    * @see `UpdateSquadList`
    * @param squad the squad
    */
  def CloseSquad(squad: Squad): Unit = {
    val guid = squad.GUID
    RemoveAllInvitesToSquad(guid)
    val membership = squad.Membership.zipWithIndex
    val (updateMembers, updateIndices) = membership.collect {
      case (member, index) if member.CharId > 0 =>
        ((member, member.CharId, index, UserEvents.get(member.CharId)), (member.CharId, index))
    }.unzip
    val updateIndicesList          = updateIndices.toList
    val completelyBlankSquadDetail = SquadDetail().Complete
    val features                   = squadFeatures(guid)
    val channel                    = s"/${features.ToChannel}/Squad"
    if (features.Listed) {
      Publish(squad.Leader.CharId, SquadResponse.SetListSquad(PlanetSideGUID(0)))
    }
    updateMembers
      .foreach {
        case (member, charId, _, None) =>
          memberToSquad.remove(charId)
          member.Name = ""
          member.CharId = 0L
        case (member, charId, index, Some(actor)) =>
          memberToSquad.remove(charId)
          member.Name = ""
          member.CharId = 0L
          SquadEvents.unsubscribe(actor, channel)
          Publish(
            charId,
            SquadResponse.Leave(
              squad,
              updateIndicesList.filterNot {
                case (_, outIndex) => outIndex == index
              } :+ (charId, index) //we need to be last
            )
          )
          Publish(charId, SquadResponse.AssociateWithSquad(PlanetSideGUID(0)))
          Publish(charId, SquadResponse.Detail(PlanetSideGUID(0), completelyBlankSquadDetail))
      }
    UpdateSquadListWhenListed(
      squadFeatures.remove(guid).get.Stop,
      None
    )
  }

  /**
    * All players are made to leave the squad and the squad will stop existing.
    * Essentially, perform the same operations as `CloseSquad`
    * but treat the process as if the squad is being disbanded in terms of messaging.
    * @see `PanicDisbandSquad`
    * @see `Publish`
    * @see `SquadResponse.Membership`
    * @param squad the squad
    */
  def DisbandSquad(squad: Squad): Unit = {
    val leader = squad.Leader.CharId
    PanicDisbandSquad(
      squad,
      squad.Membership.collect { case member if member.CharId > 0 && member.CharId != leader => member.CharId }
    )
    //the squad is being disbanded, the squad events channel is also going away; use cached character ids
    Publish(leader, SquadResponse.Membership(SquadResponseType.Disband, 0, 0, leader, None, "", true, Some(None)))
  }

  /**
    * All players are made to leave the squad and the squad will stop existing.<br>
    * <br>
    * The complement of the prior `DisbandSquad` method.
    * This method deals entirely with other squad members observing the squad become abandoned.
    * The distinction is useful when unsubscribing from this service,
    * as the `ActorRef` object used to message the player's client is no longer reliable
    * and has probably ceased to exist.
    * @see `CloseSquad`
    * @see `DisbandSquad`
    * @see `Publish`
    * @see `SquadResponse.Membership`
    * @param squad the squad
    * @param membership the unique character identifier numbers of the other squad members
    * @return if a role/index pair is provided
    */
  def PanicDisbandSquad(squad: Squad, membership: Iterable[Long]): Unit = {
    CloseSquad(squad)
    membership.foreach { charId =>
      Publish(charId, SquadResponse.Membership(SquadResponseType.Disband, 0, 0, charId, None, "", false, Some(None)))
    }
  }

  /**
    * Move one player into one squad role and,
    * if encountering a player already recruited to the destination role,
    * swap that other player into the first player's position.
    * If no encounter, just blank the original role.
    * @see `AssignSquadMemberToRole`
    * @see `SelectRoleForYourself`
    * @param toMember the squad role where the player is being placed
    * @param fromMember the squad role where the player is being encountered;
    *                   if a conflicting player is discovered, swap that player into `fromMember`
    */
  def SwapMemberPosition(toMember: Member, fromMember: Member): Unit = {
    val (name, charId, zoneId, pos, health, armor) =
      (fromMember.Name, fromMember.CharId, fromMember.ZoneId, fromMember.Position, fromMember.Health, fromMember.Armor)
    if (toMember.CharId > 0) {
      fromMember.Name = toMember.Name
      fromMember.CharId = toMember.CharId
      fromMember.ZoneId = toMember.ZoneId
      fromMember.Position = toMember.Position
      fromMember.Health = toMember.Health
      fromMember.Armor = toMember.Armor
    } else {
      fromMember.Name = ""
      fromMember.CharId = 0L
    }
    toMember.Name = name
    toMember.CharId = charId
    toMember.ZoneId = zoneId
    toMember.Position = pos
    toMember.Health = health
    toMember.Armor = armor
  }

  /**
    * Display the indicated waypoint.<br>
    * <br>
    * Despite the name, no waypoints are actually "added."
    * All of the waypoints constantly exist as long as the squad to which they are attached exists.
    * They are merely "activated" and "deactivated."
    * @see `SquadWaypointRequest`
    * @see `WaypointInfo`
    * @param guid the squad's unique identifier
    * @param waypointType the type of the waypoint
    * @param info information about the waypoint, as was reported by the client's packet
    * @return the waypoint data, if the waypoint type is changed
    */
  def AddWaypoint(
      guid: PlanetSideGUID,
      waypointType: SquadWaypoints.Value,
      info: WaypointInfo
  ): Option[WaypointData] = {
    squadFeatures.get(guid) match {
      case Some(features) =>
        features.Waypoints.lift(waypointType.id) match {
          case Some(point) =>
            point.zone_number = info.zone_number
            point.pos = info.pos
            Some(point)
          case None =>
            log.error(s"no squad waypoint $waypointType found")
            None
        }
      case None =>
        log.error(s"no squad waypoint $waypointType found")
        None
    }
  }

  /**
    * Hide the indicated waypoint.
    * Unused waypoints are marked by having a non-zero z-coordinate.<br>
    * <br>
    * Despite the name, no waypoints are actually "removed."
    * All of the waypoints constantly exist as long as the squad to which they are attached exists.
    * They are merely "activated" and "deactivated."
    * @param guid the squad's unique identifier
    * @param waypointType the type of the waypoint
    */
  def RemoveWaypoint(guid: PlanetSideGUID, waypointType: SquadWaypoints.Value): Unit = {
    squadFeatures.get(guid) match {
      case Some(features) =>
        features.Waypoints.lift(waypointType.id) match {
          case Some(point) =>
            point.pos = Vector3.z(1)
          case _ =>
            log.warn(s"no squad waypoint $waypointType found")
        }
      case _ =>
        log.warn(s"no squad #$guid found")
    }
  }

  /**
    * Dispatch all of the information about a given squad's waypoints to a user.
    * @param toCharId the player to whom the waypoint data will be dispatched
    * @param guid the squad's unique identifier
    */
  def InitWaypoints(toCharId: Long, guid: PlanetSideGUID): Unit = {
    squadFeatures.get(guid) match {
      case Some(features) =>
        val squad = features.Squad
        val vz1   = Vector3.z(value = 1)
        val list  = features.Waypoints
        Publish(
          toCharId,
          SquadResponse.InitWaypoints(
            squad.Leader.CharId,
            list.zipWithIndex.collect {
              case (point, index) if point.pos != vz1 =>
                (SquadWaypoints(index), WaypointInfo(point.zone_number, point.pos), 1)
            }
          )
        )
      case None => ;
    }
  }

  /**
    * na
    * @param charId the player's unique character identifier number
    * @param sender the `ActorRef` associated with this character
    */
  def LeaveService(charId: String, sender: ActorRef): Unit = {
    LeaveService(charId.toLong, sender)
  }

  /**
    * na
    * @param charId the player's unique character identifier number
    * @param sender the `ActorRef` associated with this character
    */
  def LeaveService(charId: Long, sender: ActorRef): Unit = {
    refused.remove(charId)
    continueToMonitorDetails.remove(charId)
    RemoveAllInvitesWithPlayer(charId)
    val pSquadOpt = GetParticipatingSquad(charId)
    val lSquadOpt = GetLeadingSquad(charId, pSquadOpt)
    pSquadOpt match {
      //member of the squad; leave the squad
      case Some(squad) =>
        val size = squad.Size
        SquadEvents.unsubscribe(UserEvents(charId), s"/${squadFeatures(squad.GUID).ToChannel}/Squad")
        UserEvents.remove(charId)
        lSquadOpt match {
          case Some(_) =>
            //leader of a squad; the squad will be disbanded
            PanicDisbandSquad(
              squad,
              squad.Membership.collect { case member if member.CharId > 0 && member.CharId != charId => member.CharId }
            )
          case None if size == 2 =>
            //one of the last two members of a squad; the squad will be disbanded
            PanicDisbandSquad(
              squad,
              squad.Membership.collect { case member if member.CharId > 0 && member.CharId != charId => member.CharId }
            )
          case None =>
            //not the leader of the squad; tell other members that we are leaving
            PanicLeaveSquad(
              charId,
              squad,
              squad.Membership.zipWithIndex.find { case (_member, _) => _member.CharId == charId }
            )
        }
      case None =>
        //not a member of any squad; nothing to do here
        UserEvents.remove(charId)
    }
    SquadEvents.unsubscribe(sender) //just to make certain
    TryResetSquadId()
  }

  /**
    * Dispatch a message entailing the composition of this squad when that squad is publicly available
    * and focus on any specific aspects of it, purported as being changed recently.
    * @see `SquadInfo`
    * @see `UpdateSquadList(Squad, Option[SquadInfo])`
    * @param features the related information about the squad
    * @param changes the highlighted aspects of the squad;
    *                these "changes" do not have to reflect the actual squad but are related to the contents of the message
    */
  private def UpdateSquadListWhenListed(features: SquadFeatures, changes: SquadInfo): Unit = {
    UpdateSquadListWhenListed(features, Some(changes))
  }

  /**
    * Dispatch a message entailing the composition of this squad when that squad is publicly available
    * and focus on any specific aspects of it, purported as being changed recently.
    * The only requirement is that the squad is publicly available for recruitment ("listed").
    * @see `SquadInfo`
    * @see `UpdateSquadList(Squad, Option[SquadInfo])`
    * @param features the related information about the squad
    * @param changes the optional highlighted aspects of the squad;
    *                these "changes" do not have to reflect the actual squad but are related to the contents of the message
    */
  private def UpdateSquadListWhenListed(features: SquadFeatures, changes: Option[SquadInfo]): Unit = {
    val squad = features.Squad
    if (features.Listed) {
      UpdateSquadList(squad, changes)
    }
  }

  /**
    * Dispatch a message entailing the composition of this squad
    * and focus on any specific aspects of it, purported as being changed recently.<br>
    * <br>
    * What sort of message is dispatched is not only based on the input parameters
    * but also on the state of previously listed squad information.
    * Listed squad information is queued when it is first published, organized first by faction affinity, then by chronology.
    * The output is first determinate on whether the squad had previously been listed as available.
    * If so, it will either update its data to all valid faction associated entities with the provided changed data;
    * or, it will be removed from the list of available squads, if there is no provided change data.
    * If the squad can not be found,
    * the change data, whatever it is, is unimportant, and the squad will be listed in full for the first time.<br>
    * <br>
    * When a squad is first introduced to the aforementioned list,
    * thus first being published to all faction-associated parties,
    * the entirety of the squad list for that faction will be updated in one go.
    * It is not necessary to do this, but doing so saves index and unique squad identifier management
    * at the cost of the size of the packet to be dispatched.
    * When a squad is removed to the aforementioned list,
    * the same process occurs where the full list for that faction affiliation is sent as an update.
    * The procedure for updating individual squad fields is precise and targeted,
    * and has been or should be prepared in advance by the caller to this method.
    * As a consequence, when updating the entry for that squad,
    * the information used as the update does not necessarily reflect the actual information currently in the squad.
    * @see `SquadResponse.InitList`
    * @see `SquadResponse.UpdateList`
    * @see `SquadService.SquadList.Publish`
    * @param squad the squad
    * @param changes the optional highlighted aspects of the squad;
    *                these "changes" do not have to reflect the actual squad but are related to the contents of the message
    */
  def UpdateSquadList(squad: Squad, changes: Option[SquadInfo]): Unit = {
    val guid            = squad.GUID
    val faction         = squad.Faction
    val factionListings = publishedLists(faction)
    factionListings.find(_ == guid) match {
      case Some(listedSquad) =>
        val index = factionListings.indexOf(listedSquad)
        changes match {
          case Some(changedFields) =>
            //squad information update
            Publish(faction, SquadResponse.UpdateList(Seq((index, changedFields))))
          case None =>
            //remove squad from listing
            factionListings.remove(index)
            //Publish(faction, SquadResponse.RemoveFromList(Seq(index)))
            Publish(faction, SquadResponse.InitList(PublishedLists(factionListings)))
        }
      case None =>
        //first time being published
        factionListings += guid
        Publish(faction, SquadResponse.InitList(PublishedLists(factionListings)))
    }
  }

  /**
    * Dispatch a message entailing the composition of this squad.
    * This is considered the first time this information will be dispatched to any relevant observers
    * so the details of the squad will be updated in full and be sent to all relevant observers,
    * namely, all the occupants of the squad.
    * External observers are ignored.
    * @see `InitSquadDetail(PlanetSideGUID, Iterable[Long], Squad)`
    * @param squad the squad
    */
  def InitSquadDetail(squad: Squad): Unit = {
    InitSquadDetail(
      squad.GUID,
      squad.Membership.collect { case member if member.CharId > 0 => member.CharId },
      squad
    )
  }

  /**
    * Dispatch an intial message entailing the strategic information and the composition of this squad.
    * The details of the squad will be updated in full and be sent to all indicated observers.
    * @see `SquadService.Detail.Publish`
    * @param guid the unique squad identifier to be used when composing the details for this message
    * @param to the unique character identifier numbers of the players who will receive this message
    * @param squad the squad from which the squad details shall be composed
    */
  def InitSquadDetail(guid: PlanetSideGUID, to: Iterable[Long], squad: Squad): Unit = {
    val output = SquadResponse.Detail(guid, SquadService.Detail.Publish(squad))
    to.foreach { Publish(_, output) }
  }

  /**
    * Send a message entailing the strategic information and the composition of the squad to the existing members of the squad.
    * @see `SquadService.Detail.Publish`
    * @see `UpdateSquadDetail(PlanetSideGUID, PlanetSideGUID, List[Long], SquadDetail)`
    * @param squad the squad
    */
  def UpdateSquadDetail(squad: Squad): Unit = {
    UpdateSquadDetail(
      squad.GUID,
      squad.GUID,
      Nil,
      SquadService.Detail.Publish(squad)
    )
  }

  /**
    * Send a message entailing the strategic information and the composition of the squad to the existing members of the squad.
    * Rather than using the squad's existing unique identifier number,
    * a meaningful substitute identifier will be employed in the message.
    * The "meaningful substitute" is usually `PlanetSideGUID(0)`
    * which indicates the local non-squad squad data on the client of a squad leader.
    * @see `SquadService.Detail.Publish`
    * @see `UpdateSquadDetail(PlanetSideGUID, PlanetSideGUID, List[Long], SquadDetail)`
    * @param squad the squad
    */
  def UpdateSquadDetail(guid: PlanetSideGUID, squad: Squad): Unit = {
    UpdateSquadDetail(
      guid,
      squad.GUID,
      Nil,
      SquadService.Detail.Publish(squad)
    )
  }

  /**
    * Send Send a message entailing some of the strategic information and the composition to the existing members of the squad.
    * @see `SquadResponse.Detail`
    * @see `UpdateSquadDetail(PlanetSideGUID, PlanetSideGUID, List[Long], SquadDetail)`
    * @param guid the unique identifier number of the squad
    * @param details the squad details to be included in the message
    */
  def UpdateSquadDetail(guid: PlanetSideGUID, details: SquadDetail): Unit = {
    UpdateSquadDetail(
      guid,
      guid,
      Nil,
      details
    )
  }

  /**
    * Send a message entailing some of the strategic information and the composition to the existing members of the squad.
    * Also send the same information to any users who are watching the squad, potentially for want to join it.
    * The squad-specific message is contingent on finding the squad's features using the unique identifier number
    * and, from that, reporting to the specific squad's messaging channel.
    * Anyone watching the squad will always be updated the given details.
    * @see `DisplaySquad`
    * @see `Publish`
    * @see `SquadDetail`
    * @see `SquadResponse.Detail`
    * @param guid the unique squad identifier number to be used for the squad detail message
    * @param toGuid the unique squad identifier number indicating the squad broadcast channel name
    * @param excluding the explicit unique character identifier numbers of individuals who should not receive the message
    * @param details the squad details to be included in the message
    */
  def UpdateSquadDetail(
      guid: PlanetSideGUID,
      toGuid: PlanetSideGUID,
      excluding: Iterable[Long],
      details: SquadDetail
  ): Unit = {
    val output = SquadResponse.Detail(guid, details)
    squadFeatures.get(toGuid) match {
      case Some(features) =>
        Publish(features.ToChannel, output, excluding)
      case _ => ;
    }
    continueToMonitorDetails
      .collect {
        case (charId, sguid) if sguid == guid && !excluding.exists(_ == charId) =>
          Publish(charId, output, Nil)
      }
  }

  /**
    * Transform a list of squad unique identifiers into a list of `SquadInfo` objects for updating the squad list window.
    * @param faction the faction to which the squads belong
    * @return a `Vector` of transformed squad data
    */
  def PublishedLists(faction: PlanetSideEmpire.Type): Vector[SquadInfo] = {
    PublishedLists(publishedLists(faction))
  }

  /**
    * Transform a list of squad unique identifiers into a list of `SquadInfo` objects for updating the squad list window.
    * @param guids the list of squad unique identifier numbers
    * @return a `Vector` of transformed squad data
    */
  def PublishedLists(guids: Iterable[PlanetSideGUID]): Vector[SquadInfo] = {
    guids.map { guid => SquadService.SquadList.Publish(squadFeatures(guid).Squad) }.toVector
  }
}

object SquadService {

  /**
    * Information necessary to display a specific map marker.
    */
  class WaypointData() {
    var zone_number: Int = 1
    var pos: Vector3     = Vector3.z(1) //a waypoint with a non-zero z-coordinate will flag as not getting drawn
  }

  /**
    * The base of all objects that exist for the purpose of communicating invitation from one player to the next.
    * @param char_id the inviting player's unique identifier number
    * @param name the inviting player's name
    */
  abstract class Invitation(char_id: Long, name: String) {
    def InviterCharId: Long = char_id
    def InviterName: String = name
  }

  /**
    * Utilized when one player attempts to join an existing squad in a specific role.
    * Accessed by the joining player from the squad detail window.
    * @param player the player who requested the role
    * @param squad_guid the squad with the role
    * @param position the index of the role
    */
  final case class RequestRole(player: Player, squad_guid: PlanetSideGUID, position: Int)
      extends Invitation(player.CharId, player.Name)

  /**
    * Utilized when one squad member issues an invite for some other player.
    * Accessed by an existing squad member using the "Invite" menu option on another player.
    * @param char_id the unique character identifier of the player who sent the invite
    * @param name the name the player who sent the invite
    * @param squad_guid the squad
    */
  final case class VacancyInvite(char_id: Long, name: String, squad_guid: PlanetSideGUID)
      extends Invitation(char_id, name)

  /**
    * Utilized to redirect an (accepted) invitation request to the proper squad leader.
    * No direct action causes this message.
    * @param player the player who would be joining the squad;
    *               may or may not have actually requested it in the first place
    * @param squad_guid the squad
    */
  final case class IndirectInvite(player: Player, squad_guid: PlanetSideGUID)
      extends Invitation(player.CharId, player.Name)

  /**
    * Utilized in conjunction with an external queuing data structure
    * to search for and submit requests to other players
    * for the purposes of fill out unoccupied squad roles.
    * @param char_id the unique character identifier of the squad leader
    * @param name the name of the squad leader
    * @param squad_guid the squad
    */
  final case class ProximityInvite(char_id: Long, name: String, squad_guid: PlanetSideGUID)
      extends Invitation(char_id, name)

  /**
    * Utilized in conjunction with an external queuing data structure
    * to search for and submit requests to other players
    * for the purposes of fill out an unoccupied squad role.
    * @param char_id the unique character identifier of the squad leader
    * @param name the name of the squad leader
    * @param squad_guid the squad with the role
    * @param position the index of the role
    */
  final case class LookingForSquadRoleInvite(char_id: Long, name: String, squad_guid: PlanetSideGUID, position: Int)
      extends Invitation(char_id, name)

  /**
    * Utilized when one player issues an invite for some other player for a squad that does not yet exist.
    * @param player na
    */
  final case class SpontaneousInvite(player: Player) extends Invitation(player.CharId, player.Name)

  object SquadList {

    /**
      * Produce complete squad information.
      * @see `SquadInfo`
      * @param squad the squad
      * @return the squad's information to be used in the squad list
      */
    def Publish(squad: Squad): SquadInfo = {
      SquadInfo(
        squad.Leader.Name,
        squad.Task,
        PlanetSideZoneID(squad.ZoneId),
        squad.Size,
        squad.Capacity,
        squad.GUID
      )
    }
  }

  object Detail {

    /**
      * Produce complete squad membership details.
      * @see `SquadDetail`
      * @param squad the squad
      * @return the squad's information to be used in the squad's detail window
      */
    def Publish(squad: Squad): SquadDetail = {
      SquadDetail()
        .Field1(squad.GUID.guid)
        .LeaderCharId(squad.Leader.CharId)
        .LeaderName(squad.Leader.Name)
        .Task(squad.Task)
        .ZoneId(PlanetSideZoneID(squad.ZoneId))
        .Members(
          squad.Membership.zipWithIndex
            .map({
              case (p, index) =>
                SquadPositionEntry(
                  index,
                  if (squad.Availability(index)) {
                    SquadPositionDetail(p.Role, p.Orders, p.Requirements, p.CharId, p.Name)
                  } else {
                    SquadPositionDetail.Closed
                  }
                )
            })
            .toList
        )
        .Complete
    }
  }

  /**
    * Clear the current detail about a squad's membership and replace it with a previously stored details.
    * @param squad the squad
    * @param favorite the loadout object
    */
  def LoadSquadDefinition(squad: Squad, favorite: SquadLoadout): Unit = {
    squad.Task = favorite.task
    squad.ZoneId = favorite.zone_id.getOrElse(squad.ZoneId)
    squad.Availability.indices.foreach { index => squad.Availability.update(index, false) }
    squad.Membership.foreach { position =>
      position.Role = ""
      position.Orders = ""
      position.Requirements = Set()
    }
    favorite.members.foreach { position =>
      squad.Availability.update(position.index, true)
      val member = squad.Membership(position.index)
      member.Role = position.role
      member.Orders = position.orders
      member.Requirements = position.requirements
    }
  }
}
