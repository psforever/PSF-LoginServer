// Copyright (c) 2022 PSForever
package net.psforever.services.teamwork

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success
//
import net.psforever.objects.{LivePlayerList, Player}
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.teamwork.{Member, SquadFeatures}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{SquadDetail, SquadPositionDetail, SquadPositionEntry, SquadAction => SquadRequestAction}
import net.psforever.types.{PlanetSideGUID, SquadResponseType}

class SquadInvitationManager(subs: SquadSubscriptionEntity, parent: ActorRef) {
  import SquadInvitationManager._

  private implicit val timeout: Timeout = Timeout(1.second)

  /**
    * key - a unique character identifier number; value - the active invitation object
    */
  private val invites: mutable.LongMap[Invitation] = mutable.LongMap[Invitation]()

  /**
    * key - a unique character identifier number; value - a list of inactive invitation objects waiting to be resolved
    */
  private val queuedInvites: mutable.LongMap[List[Invitation]] = mutable.LongMap[List[Invitation]]()

  /**
    * A placeholder for an absent active invite that has not (yet) been accepted or rejected,
    * equal to the then-current active invite.
    * Created when removing an active invite.
    * Checked when trying to add a new invite (if found, the new invite is queued).
    * Cleared when the next queued invite becomes active.<br>
    * key - unique character identifier number; value, unique character identifier number
    */
  private val previousInvites: mutable.LongMap[Invitation] = mutable.LongMap[Invitation]()

  /*
    When a player refuses an invitation by a squad leader,
    that squad leader will not be able to send further invitations (this field).
    If the player submits an invitation request for that squad,
    the current squad leader is cleared from the blocked list.
    When a squad leader refuses an invitation by a player,
    that player will not be able to send further invitations (field on sqaud's features).
    If the squad leader sends an invitation request for that player,
    the current player is cleared from the blocked list.
   */
  /**
    * The given player has refused participation into this other player's squad.<br>
    * key - a unique character identifier number;
    * value - a list of unique character identifier numbers; squad leaders or once-squad leaders
    */
  private val refused: mutable.LongMap[List[Long]] = mutable.LongMap[List[Long]]()

  private[this] val log = org.log4s.getLogger

  def postStop(): Unit = {
    invites.clear()
    queuedInvites.clear()
    previousInvites.clear()
    refused.clear()
  }

  def handleJoin(charId: Long): Unit = {
    refused.put(charId, List[Long]())
  }

  def createRequestRole(player: Player, features: SquadFeatures, position: Int): Unit = {
    //we could join directly but we need permission from the squad leader first
    val charId = features.Squad.Leader.CharId
    val requestRole = RequestRole(player, features, position)
    if (features.AutoApproveInvitationRequests) {
      SquadActionMembershipAcceptInvite(
        player,
        charId,
        Some(requestRole),
        None
      )
    } else {
      //circumvent tests in AddInviteAndRespond
      InviteResponseTemplate(indirectInviteResp)(
        requestRole,
        AddInvite(charId, requestRole),
        charId,
        invitingPlayer = 0L, //we ourselves technically are ...
        player.Name
      )
    }
  }

  def createVacancyInvite(player: Player, invitedPlayer: Long, features: SquadFeatures): Unit = {
    val invitingPlayer = player.CharId
    val squad = features.Squad
    Allowed(invitedPlayer, invitingPlayer)
    if (squad.Size == squad.Capacity) {
      log.debug(s"$invitingPlayer tried to invite $invitedPlayer to a squad without available positions")
    } else if (Refused(invitingPlayer).contains(invitedPlayer)) {
      log.debug(s"$invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
    } else {
      features.AllowedPlayers(invitingPlayer)
      AddInviteAndRespond(
        invitedPlayer,
        VacancyInvite(invitingPlayer, player.Name, features),
        invitingPlayer,
        player.Name
      )
    }
  }

  def createIndirectInvite(player: Player, invitingPlayer: Long, features: SquadFeatures): Unit = {
    val invitedPlayer = player.CharId
    val squad2 = features.Squad
    val leader = squad2.Leader.CharId
    Allowed(invitedPlayer, invitingPlayer)
    Allowed(leader, invitingPlayer)
    if (squad2.Size == squad2.Capacity) {
      log.debug(s"$invitingPlayer's invitation got reversed to $invitedPlayer's squad, but the squad has no available positions")
    } else if (Refused(invitingPlayer).contains(invitedPlayer)) {
      log.debug(s"$invitingPlayer's invitation got reversed to $invitedPlayer's squad, but $invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
    } else if (Refused(invitingPlayer).contains(leader)) {
      log.debug(s"$invitingPlayer's invitation got reversed to $invitedPlayer's squad, but $leader repeated a previous refusal to $invitingPlayer's invitation offer")
    } else if (features.DeniedPlayers().contains(invitingPlayer)) {
      log.debug(s"$invitingPlayer's invitation got reversed to $invitedPlayer's squad, but $invitingPlayer is denied the invitation")
    } else {
      features.AllowedPlayers(invitedPlayer)
      AddInviteAndRespond(
        leader,
        IndirectInvite(player, features),
        invitingPlayer,
        player.Name
      )
    }
  }

  def createSpontaneousInvite(player: Player, invitedPlayer: Long): Unit = {
    //neither the invited player nor the inviting player belong to any squad
    val invitingPlayer = player.CharId
    Allowed(invitedPlayer, invitingPlayer)
    if (Refused(invitingPlayer).contains(invitedPlayer)) {
      log.debug(s"$invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
    } else if (Refused(invitedPlayer).contains(invitingPlayer)) {
      log.debug(s"$invitingPlayer repeated a previous refusal to $invitedPlayer's invitation offer")
    } else {
      AddInviteAndRespond(
        invitedPlayer,
        SpontaneousInvite(player),
        invitingPlayer,
        player.Name
      )
    }
  }

  def SquadActionMembershipAcceptInvite(
                                         tplayer: Player,
                                         invitedPlayer: Long,
                                         acceptedInvite: Option[Invitation],
                                         invitedPlayerSquadOpt: Option[SquadFeatures]
                                       ): Unit = {
    val availableForJoiningSquad = notLimitedByEnrollmentInSquad(invitedPlayerSquadOpt, invitedPlayer)
    acceptedInvite match {
      case Some(RequestRole(petitioner, features, position))
        if availableForJoiningSquad && canEnrollInSquad(features, petitioner.CharId) =>
        //player requested to join a squad's specific position
        //invitedPlayer is actually the squad leader; petitioner is the actual "invitedPlayer"
        if (JoinSquad(petitioner, features, position)) {
          DeliverAcceptanceMessages(invitedPlayer, petitioner.CharId, petitioner.Name)
          CleanUpInvitesForSquadAndPosition(features, position)
        }

      case Some(IndirectInvite(recruit, features))
        if availableForJoiningSquad && canEnrollInSquad(features, recruit.CharId) =>
        //tplayer / invitedPlayer is actually the squad leader
        val recruitCharId = recruit.CharId
        HandleVacancyInvite(features, recruitCharId, invitedPlayer, recruit) match {
          case Some((_, line)) =>
            DeliverAcceptanceMessages(invitedPlayer, recruitCharId, recruit.Name)
            JoinSquad(recruit, features, line)
            CleanUpAllInvitesWithPlayer(recruitCharId)
            CleanUpInvitesForSquadAndPosition(features, line)
          //TODO since we are the squad leader, we do not want to brush off our queued squad invite tasks
          case _ => ;
        }

      case Some(VacancyInvite(invitingPlayer, _, features))
        if availableForJoiningSquad && canEnrollInSquad(features, invitedPlayer) =>
        //accepted an invitation to join an existing squad
        HandleVacancyInvite(features, invitedPlayer, invitingPlayer, tplayer) match {
          case Some((_, line)) =>
            DeliverAcceptanceMessages(invitingPlayer, invitedPlayer, tplayer.Name)
            JoinSquad(tplayer, features, line)
            CleanUpQueuedInvites(invitedPlayer)
            CleanUpInvitesForSquadAndPosition(features, line)
          case _ => ;
        }

      case Some(SpontaneousInvite(invitingPlayer))
        if availableForJoiningSquad =>
        SquadMembershipAcceptInviteAction(invitingPlayer, tplayer, invitedPlayer)

      case Some(LookingForSquadRoleInvite(member, features, position))
        if availableForJoiningSquad && canEnrollInSquad(features, invitedPlayer) =>
        val invitingPlayer = member.CharId
        features.ProxyInvites = features.ProxyInvites.filterNot { _ == invitedPlayer }
        if (JoinSquad(tplayer, features, position)) {
          //join this squad
          DeliverAcceptanceMessages(invitingPlayer, invitedPlayer, tplayer.Name)
          CleanUpQueuedInvites(tplayer.CharId)
          CleanUpInvitesForSquadAndPosition(features, position)
        }

      case Some(ProximityInvite(member, features, position))
        if availableForJoiningSquad && canEnrollInSquad(features, invitedPlayer) =>
        val invitingPlayer = member.CharId
        features.ProxyInvites = features.ProxyInvites.filterNot { _ == invitedPlayer }
        if (JoinSquad(tplayer, features, position)) {
          //join this squad
          DeliverAcceptanceMessages(invitingPlayer, invitedPlayer, tplayer.Name)
          CleanUpAllInvitesWithPlayer(invitedPlayer)
          val squad = features.Squad
          if (squad.Size == squad.Capacity) {
            //all available squad positions filled; terminate all remaining invitations
            CleanUpAllInvitesToSquad(features)
          }
        } else {
          ReloadProximityInvite(tplayer.Zone.Players, invitedPlayer, features, position) //TODO ?
        }

      case _ =>
        //the invite either timed-out or was withdrawn or is now invalid
        (previousInvites.get(invitedPlayer) match {
          case Some(SpontaneousInvite(leader)) => (leader.CharId, leader.Name)
          case Some(VacancyInvite(charId, name, _)) => (charId, name)
          case Some(ProximityInvite(member, _, _)) => (member.CharId, member.Name)
          case Some(LookingForSquadRoleInvite(member, _, _)) => (member.CharId, member.Name)
          case _ => (0L, "")
        }) match {
          case (0L, "") => ;
          case (charId, name) =>
            subs.Publish(
              charId,
              SquadResponse.Membership(SquadResponseType.Cancel, 0, 0, charId, Some(0L), name, unk5 = false, Some(None))
            )
        }
    }
  }

  def DeliverAcceptanceMessages(
                                 squadLeader: Long,
                                 joiningPlayer: Long,
                                 joiningPlayerName: String
                               ): Unit = {
    val msg = SquadResponse.Membership(
      SquadResponseType.Accept,
      0,
      0,
      joiningPlayer,
      Some(squadLeader),
      joiningPlayerName,
      unk5 = false,
      Some(None)
    )
    subs.Publish(squadLeader, msg)
    subs.Publish(joiningPlayer, msg.copy(unk5 = true))
  }

  def notLimitedByEnrollmentInSquad(squadOpt: Option[SquadFeatures], charId: Long): Boolean = {
    squadOpt match {
      case Some(features) if features.Squad.Membership.exists { _.CharId == charId } =>
        EnsureEmptySquad(features)
      case Some(_) =>
        false
      case None =>
        true
    }
  }

  def canEnrollInSquad(features: SquadFeatures, charId: Long): Boolean = {
    !features.Squad.Membership.exists { _.CharId == charId }
  }

  def SquadMembershipAcceptInviteAction(invitingPlayer: Player, player: Player, invitedPlayer: Long): Unit = {
    //originally, we were invited by someone into a new squad they would form
    val invitingPlayerCharId = invitingPlayer.CharId
    if (invitingPlayerCharId != invitedPlayer) {
      //generate a new squad, with invitingPlayer as the leader
      val result = ask(parent, SquadService.PerformStartSquad(invitingPlayer))
      result.onComplete {
        case Success(FinishStartSquad(features)) =>
          HandleVacancyInvite(features, player.CharId, invitingPlayerCharId, player) match {
            case Some((_, line)) =>
              subs.Publish(
                invitedPlayer,
                SquadResponse.Membership(
                  SquadResponseType.Accept,
                  0,
                  0,
                  invitedPlayer,
                  Some(invitingPlayerCharId),
                  "",
                  unk5=true,
                  Some(None)
                )
              )
              subs.Publish(
                invitingPlayerCharId,
                SquadResponse.Membership(
                  SquadResponseType.Accept,
                  0,
                  0,
                  invitingPlayerCharId,
                  Some(invitedPlayer),
                  player.Name,
                  unk5=false,
                  Some(None)
                )
              )
              JoinSquad(player, features, line)
              CleanUpQueuedInvites(player.CharId)
            case _ => ;
          }
        case _ => ;
      }
    }
  }

  def handleProximityInvite(zone: Zone, invitingPlayer: Long, features: SquadFeatures): Unit = {
    val squad = features.Squad
    val sguid    = squad.GUID
    val origSearchForRole = features.SearchForRole
    var newRecruitment = List[Long]()
    val positionsToRecruitFor = (origSearchForRole match {
      case Some(-1) =>
        //we've already issued a proximity invitation; no need to do another
        log.debug("ProximityInvite: wait for existing proximity invitations to clear")
        Array[(Member, Int)]()
      case Some(position) =>
        //already searching for the given position; retain the key if the invite is active, or clear if if not
        features.SearchForRole = -1
        invites.find {
          case (_, LookingForSquadRoleInvite(_, features, role)) =>
            features.Squad.GUID == sguid && role == position
          case _ =>
            false
        } match {
          case Some((key, _)) =>
            newRecruitment = newRecruitment :+ key
            squad.Membership.zipWithIndex.filterNot { case (_, index) => index == position }
          case None =>
            CleanUpQueuedInvitesForSquadAndPosition(features, position)
            squad.Membership.zipWithIndex
        }
      case _ =>
        features.SearchForRole = -1
        squad.Membership.zipWithIndex
    })
      .collect { case (member, index) if member.CharId == 0 && squad.Availability(index) => (member, index) }
      .sortBy({ _._1.Requirements.foldLeft(0)(_ + _.value) })(Ordering.Int.reverse)
    //find recruits
    val players = zone.LivePlayers.map { _.avatar }
    if (positionsToRecruitFor.nonEmpty && players.nonEmpty) {
      //does this do anything?
      subs.Publish(
        invitingPlayer,
        SquadResponse.Membership(
          SquadResponseType.ProximityInvite,
          19,
          0,
          invitingPlayer,
          None,
          "",
          unk5=true,
          Some(None)
        )
      )
      positionsToRecruitFor.foreach { case (_, position) =>
        FindSoldiersWithinScopeAndInvite(
          squad.Leader,
          features,
          position,
          players,
          features.ProxyInvites ++ newRecruitment,
          ProximityEnvelope
        ) match {
          case None => ;
          case Some(id) =>
            newRecruitment = newRecruitment :+ id
        }
      }
    }
    if (newRecruitment.isEmpty) {
      features.SearchForRole = origSearchForRole
    } else {
      features.ProxyInvites = features.ProxyInvites ++ newRecruitment
      //if searching for a position originally, convert the active invite to proximity invite, or remove it
      val key = newRecruitment.head
      (origSearchForRole, invites.get(key)) match {
        case (Some(-1), _) => ;
        case (Some(position), Some(LookingForSquadRoleInvite(member, _, _))) =>
          invites(key) = ProximityInvite(member, features, position)
        case _ => ;
      }
    }
  }

  def handleAcceptance(player: Player, charId: Long, squadOpt: Option[SquadFeatures]): Unit = {
    SquadActionMembershipAcceptInvite(player, charId, RemoveInvite(charId), squadOpt)
    NextInviteAndRespond(charId)
  }

  def handleRejection(
                       tplayer: Player,
                       rejectingPlayer: Long,
                       squadsToLeaders: List[(PlanetSideGUID, Long)]
                     ): Unit = {
    val rejectedBid = RemoveInvite(rejectingPlayer)
    (rejectedBid match {
      case Some(SpontaneousInvite(leader)) =>
        //rejectingPlayer is the would-be squad member; the would-be squad leader sent the request and was rejected
        val invitingPlayerCharId = leader.CharId
        Refused(rejectingPlayer, invitingPlayerCharId)
        (Some(rejectingPlayer), Some(invitingPlayerCharId))

      case Some(VacancyInvite(leader, _, features))
        if notLeaderOfThisSquad(squadsToLeaders, features.Squad.GUID, rejectingPlayer) =>
        //rejectingPlayer is the would-be squad member; the squad leader sent the request and was rejected
        Refused(rejectingPlayer, leader)
        (Some(rejectingPlayer), Some(leader))

      case Some(ProximityInvite(_, features, position))
        if notLeaderOfThisSquad(squadsToLeaders, features.Squad.GUID, rejectingPlayer) =>
        //rejectingPlayer is the would-be squad member; the squad leader sent the request and was rejected
        ReloadProximityInvite(
          tplayer.Zone.Players,
          rejectingPlayer,
          features,
          position
        )
        (Some(rejectingPlayer), None)

      case Some(LookingForSquadRoleInvite(member, guid, position))
        if member.CharId != rejectingPlayer =>
        val leaderCharId = member.CharId
        //rejectingPlayer is the would-be squad member; the squad leader sent the request and was rejected
        ReloadSearchForRoleInvite(
          LivePlayerList.WorldPopulation { _ => true },
          rejectingPlayer,
          guid,
          position
        )
        (Some(rejectingPlayer), Some(leaderCharId))

      case Some(RequestRole(rejected, features, _))
        if notLeaderOfThisSquad(squadsToLeaders, features.Squad.GUID, rejectingPlayer) =>
        //rejected is the would-be squad member; rejectingPlayer is the squad leader who rejected the request
        features.DeniedPlayers(rejected.CharId)
        (Some(rejectingPlayer), None)

      case _ => ; //TODO IndirectInvite, etc., but how to handle them?
        (None, None)
    }) match {
      case (Some(rejected), Some(invited)) =>
        subs.Publish(
          rejected,
          SquadResponse.Membership(SquadResponseType.Reject, 0, 0, rejected, Some(invited), "", unk5=true, Some(None))
        )
        subs.Publish(
          invited,
          SquadResponse.Membership(SquadResponseType.Reject, 0, 0, invited, Some(rejected), tplayer.Name, unk5=false, Some(None))
        )
      case (Some(rejected), None) =>
        subs.Publish(
          rejected,
          SquadResponse.Membership(SquadResponseType.Reject, 0, 0, rejected, Some(rejected), "", unk5=true, Some(None))
        )
      case _ => ;
    }
    NextInviteAndRespond(rejectingPlayer)
  }

  def notLeaderOfThisSquad(squadsToLeaders: List[(PlanetSideGUID, Long)], guid: PlanetSideGUID, charId: Long): Boolean = {
    squadsToLeaders.find { case (squadGuid, _) => squadGuid == guid } match {
      case Some((_, leaderId)) => leaderId != charId
      case None                => false
    }
  }

  def ReloadSearchForRoleInvite(
                                 scope: List[Avatar],
                                 rejectingPlayer: Long,
                                 features: SquadFeatures,
                                 position: Int
                               ) : Unit = {
    //rejectingPlayer is the would-be squad member; the squad leader rejected the request
    val squadLeader = features.Squad.Leader
    Refused(rejectingPlayer, squadLeader.CharId)
    features.ProxyInvites = features.ProxyInvites.filterNot { _ == rejectingPlayer }
    FindSoldiersWithinScopeAndInvite(
      squadLeader,
      features,
      position,
      scope,
      features.ProxyInvites,
      LookingForSquadRoleEnvelope
    ) match {
      case None =>
        if (features.SearchForRole.contains(position) && features.ProxyInvites.isEmpty) {
          features.SearchForRole = None
          //TODO message the squadLeader.CharId to indicate that there are no more candidates for this position
        }
      case Some(id) =>
        features.ProxyInvites = features.ProxyInvites :+ id
    }
    if (features.ProxyInvites.isEmpty) {
      features.SearchForRole = None
    }
  }

  def ReloadProximityInvite(
                             scope: List[Avatar],
                             rejectingPlayer: Long,
                             features: SquadFeatures,
                             position: Int
                           ): Unit = {
    //rejectingPlayer is the would-be squad member; the squad leader rejected the request
    val squadLeader = features.Squad.Leader
    Refused(rejectingPlayer, squadLeader.CharId)
    features.ProxyInvites = features.ProxyInvites.filterNot { _ == rejectingPlayer }
    FindSoldiersWithinScopeAndInvite(
      squadLeader,
      features,
      position,
      scope,
      features.ProxyInvites,
      ProximityEnvelope
    ) match {
      case None =>
        if (features.SearchForRole.contains(-1) && features.ProxyInvites.isEmpty) {
          features.SearchForRole = None
          //TODO message the squadLeader.CharId to indicate that there are no more candidates for this position
        }
      case Some(id) =>
        features.ProxyInvites = features.ProxyInvites :+ id
    }
  }

  def handleDisbanding(features: SquadFeatures): Unit = {
    CleanUpAllInvitesToSquad(features)
  }

  def handleCancelling(cancellingPlayer: Long): Unit = {
    //get rid of SpontaneousInvite objects and VacancyInvite objects
    invites.collect {
      case (id, invite: SpontaneousInvite) if invite.InviterCharId == cancellingPlayer =>         RemoveInvite(id)
      case (id, invite: VacancyInvite) if invite.InviterCharId == cancellingPlayer =>             RemoveInvite(id)
      case (id, invite: LookingForSquadRoleInvite) if invite.InviterCharId == cancellingPlayer => RemoveInvite(id)
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
    CleanUpAllProximityInvites(cancellingPlayer)
  }

  def handlePromotion(
                                     sponsoringPlayer: Long,
                                     promotedPlayer: Long,
                                   ): Unit = {
    ShiftInvitesToPromotedSquadLeader(sponsoringPlayer, promotedPlayer)
  }

  def handleDefinitionAction(
                             player: Player,
                             action: SquadRequestAction,
                             features: SquadFeatures
                           ): Unit = {
    import SquadRequestAction._
    action match {
      //the following actions cause changes with the squad composition or with invitations
      case AutoApproveInvitationRequests(_) =>
        SquadActionDefinitionAutoApproveInvitationRequests(player, features)
      case FindLfsSoldiersForRole(position) =>
        SquadActionDefinitionFindLfsSoldiersForRole(player, position, features)
      case CancelFind() =>
        SquadActionDefinitionCancelFind(None)
      case SelectRoleForYourself(position) =>
        SquadActionDefinitionSelectRoleForYourselfAsInvite(player, features, position)
      case _: CancelSelectRoleForYourself =>
        SquadActionDefinitionCancelSelectRoleForYourself(player, features)
      case _ => ;
    }
  }

  def SquadActionDefinitionAutoApproveInvitationRequests(
                                                          tplayer: Player,
                                                          features: SquadFeatures
                                                        ): Unit = {
    //allowed auto-approval - resolve the requests (only)
    val charId = tplayer.CharId
    val (requests, others) =
      (invites.get(charId) match {
        case Some(invite) => invite +: queuedInvites.getOrElse(charId, Nil)
        case None => queuedInvites.getOrElse(charId, Nil)
      })
        .partition({ case _: RequestRole => true; case _ => false })
    invites.remove(charId)
    queuedInvites.remove(charId)
    previousInvites.remove(charId)
    //RequestRole invitations that still have to be handled
    val squad = features.Squad
    var remainingRequests = requests.collect {
      case request: RequestRole => (request, request.player)
    }
    var unfulfilled = List[Player]()
    //give roles to people who requested specific positions
    (1 to 9).foreach { position =>
      val (discovered, remainder) = remainingRequests.partition {
        case (request: RequestRole, _) => request.position == position
        case _                         => false
      }
      unfulfilled ++= (discovered
        .find { case (_, player) => JoinSquad(player, features, position) } match {
        case Some((_, player)) =>
          remainingRequests = remainder.filterNot { case (_, p) => p.CharId == player.CharId }
          discovered.filterNot { case (_, p) => p.CharId == player.CharId }
        case None =>
          remainingRequests = remainder
          discovered
      }).map { _._2 }
    }
    //fill any incomplete role by trying to match all sorts of unfulfilled invitations
    var otherInvites = unfulfilled ++
      others.collect {
        case invite: SpontaneousInvite => invite.player
        case invite: IndirectInvite    => invite.player
      }
        .distinctBy { _.CharId }
    (1 to 9).foreach { position =>
      if (squad.Availability(position)) {
        otherInvites.zipWithIndex.find { case (invitedPlayer, _) =>
          JoinSquad(invitedPlayer, features, position)
        } match {
          case Some((_, index)) =>
            otherInvites = otherInvites.take(index) ++ otherInvites.drop(index+1)
          case None => ;
        }
      }
    }
    //cleanup searches by squad leader
    features.SearchForRole match {
      case Some(-1) => CleanUpAllProximityInvites(charId)
      case Some(_)  => SquadActionDefinitionCancelFind(Some(features))
      case None => ;
    }
  }

  def SquadActionDefinitionFindLfsSoldiersForRole(
                                                   tplayer: Player,
                                                   position: Int,
                                                   features: SquadFeatures
                                                 ): Unit = {
    val squad = features.Squad
    val sguid = squad.GUID
    (features.SearchForRole match {
      case None =>
        Some(Nil)
      case Some(-1) =>
        //a proximity invitation has not yet cleared; nothing will be gained by trying to invite for a specific role
        log.debug("FindLfsSoldiersForRole: waiting for proximity invitations to clear")
        None
      case Some(pos) if pos == position =>
        //already recruiting for this specific position in the squad? do nothing
        log.debug("FindLfsSoldiersForRole: already recruiting for this position; client-server mismatch?")
        None
      case Some(pos) =>
        //some other role is undergoing recruitment; cancel and redirect efforts for new position
        features.SearchForRole = None
        CleanUpQueuedInvitesForSquadAndPosition(features, pos)
        Some(
          invites.filter {
            case (_, LookingForSquadRoleInvite(_, _features, role)) => _features.Squad.GUID == sguid && role == pos
            case _                                                  => false
          }.keys.toList
        )
    }) match {
      case None =>
        features.SearchForRole = None
      case Some(list) =>
        //this will update the role entry in the GUI to visually indicate being searched for; only one will be displayed at a time
        subs.Publish(
          tplayer.CharId,
          SquadResponse.Detail(
            sguid,
            SquadDetail().Members(
              List(SquadPositionEntry(position, SquadPositionDetail().CharId(char_id = 0L).Name(name = "")))
            )
          )
        )
        //search!
        FindSoldiersWithinScopeAndInvite(
          squad.Leader,
          features,
          position,
          LivePlayerList.WorldPopulation { _ => true },
          list,
          LookingForSquadRoleEnvelope
        ) match {
          case None => ;
          case Some(id) =>
            features.ProxyInvites = List(id)
            features.SearchForRole = position
        }
    }
  }

  def SquadActionDefinitionCancelFind(lSquadOpt: Option[SquadFeatures]): Unit = {
    lSquadOpt match {
      case Some(features) =>
        features.SearchForRole match {
          case Some(position) if position > -1 =>
            val squad = features.Squad
            val sguid = squad.GUID
            features.SearchForRole = None
            //remove active invites
            invites
              .filter {
                case (_, LookingForSquadRoleInvite(_, _features, _)) => _features.Squad.GUID == sguid
                case _                                               => false
              }
              .keys
              .foreach { charId =>
                RemoveInvite(charId)
              }
            //remove queued invites
            queuedInvites.foreach {
              case (charId, queue) =>
                val filtered = queue.filterNot {
                  case LookingForSquadRoleInvite(_, _features, _) => _features.Squad.GUID == sguid
                  case _                                          => false
                }
                queuedInvites += charId -> filtered
                if (filtered.isEmpty) {
                  queuedInvites.remove(charId)
                }
            }
            //remove yet-to-be invitedPlayers
            features.ProxyInvites = Nil
          case _ => ;
        }
      case _ => ;
    }
  }

  /** the following action can be performed by an unaffiliated player */
  def SquadActionDefinitionSelectRoleForYourselfAsInvite(
                                                          tplayer: Player,
                                                          features: SquadFeatures,
                                                          position: Int
                                                        ): Unit = {
    //not a member of any squad, but we might become a member of this one
    val squad = features.Squad
    if (squad.isAvailable(position, tplayer.avatar.certifications)) {
      //we could join directly but we need permission from the squad leader first
      if (features.AutoApproveInvitationRequests) {
        SquadActionMembershipAcceptInvite(
          tplayer,
          squad.Leader.CharId,
          Some(RequestRole(tplayer, features, position)),
          None
        )
      } else {
        //circumvent tests in AddInviteAndRespond
        val requestRole = RequestRole(tplayer, features, position)
        val charId = squad.Leader.CharId
        InviteResponseTemplate(indirectInviteResp)(
          requestRole,
          AddInvite(charId, requestRole),
          charId,
          invitingPlayer = 0L, //we ourselves technically are ...
          tplayer.Name
        )
      }
    }
  }

  /** the following action can be performed by anyone who has tried to join a squad */
  def SquadActionDefinitionCancelSelectRoleForYourself(
                                                        tplayer: Player,
                                                        features: SquadFeatures
                                                      ): Unit = {
    val cancellingPlayer = tplayer.CharId
    //assumption: a player who is cancelling will rarely end up with their invite queued
    val squad = features.Squad
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
        subs.Publish(
          leaderCharId,
          SquadResponse.Membership(
            SquadResponseType.Cancel,
            0,
            0,
            cancellingPlayer,
            None,
            entry.player.Name,
            unk5=false,
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
          subs.Publish(
            leaderCharId,
            SquadResponse.Membership(
              SquadResponseType.Cancel,
              0,
              0,
              cancellingPlayer,
              None,
              entry.player.Name,
              unk5=false,
              Some(None)
            )
          )
          queuedInvites.remove(leaderCharId)
          Some(true)
        case (list, index) =>
          val entry = list(index).asInstanceOf[RequestRole]
          subs.Publish(
            leaderCharId,
            SquadResponse.Membership(
              SquadResponseType.Cancel,
              0,
              0,
              cancellingPlayer,
              None,
              entry.player.Name,
              unk5=false,
              Some(None)
            )
          )
          queuedInvites(leaderCharId) = list.take(index) ++ list.drop(index + 1)
          Some(true)
      }
    )
  }

  def handleClosingSquad(features: SquadFeatures): Unit = {
    CleanUpAllInvitesToSquad(features)
  }

  def handleCleanup(charId: Long): Unit = {
    CleanUpAllInvitesWithPlayer(charId)
  }

  def handleLeave(charId: Long): Unit = {
    refused.remove(charId)
    CleanUpAllInvitesWithPlayer(charId)
  }

  def resendActiveInvite(charId: Long): Unit = {
    invites.get(charId) match {
      case Some(invite) =>
        RespondToInvite(charId, invite)
      case None => ;
    }
  }

  def ShiftInvitesToPromotedSquadLeader(
                                         sponsoringPlayer: Long,
                                         promotedPlayer: Long
                                       ): Unit = {
    val leaderInvite        = invites.remove(sponsoringPlayer)
    val leaderQueuedInvites = queuedInvites.remove(sponsoringPlayer).toList.flatten
    val (invitesToConvert, invitesToAppend) = (invites.remove(promotedPlayer).orElse(previousInvites.get(promotedPlayer)), leaderInvite) match {
      case (Some(activePromotedInvite), Some(outLeaderInvite)) =>
        //the promoted player has an active invite; queue these
        val promotedQueuedInvites = queuedInvites.remove(promotedPlayer).toList.flatten
        NextInvite(promotedPlayer)
        NextInvite(sponsoringPlayer)
        (activePromotedInvite +: (outLeaderInvite +: leaderQueuedInvites), promotedQueuedInvites)

      case (Some(activePromotedInvite), None) =>
        //the promoted player has an active invite; queue these
        val promotedQueuedInvites = queuedInvites.remove(promotedPlayer).toList.flatten
        NextInvite(promotedPlayer)
        (activePromotedInvite :: leaderQueuedInvites, promotedQueuedInvites)

      case (None, Some(outLeaderInvite)) =>
        //no active invite for the promoted player, but the leader had an active invite; trade the queued invites
        NextInvite(sponsoringPlayer)
        (outLeaderInvite +: leaderQueuedInvites, queuedInvites.remove(promotedPlayer).toList.flatten)

      case (None, None) =>
        //no active invites for anyone; assign the first queued invite from the promoting player, if available, and queue the rest
        (leaderQueuedInvites, queuedInvites.remove(promotedPlayer).toList.flatten)
    }
    moveOverPromotedInvites(promotedPlayer, invitesToConvert, invitesToAppend)
  }

  def moveOverPromotedInvites(
                               targetPlayer: Long,
                               convertableInvites: List[Invitation],
                               otherInvitations: List[Invitation]
                             ): Unit = {
    convertableInvites ++ otherInvitations match {
      case Nil => ;
      case x :: Nil =>
        AddInviteAndRespond(targetPlayer, x, x.InviterCharId, x.InviterName)
      case x :: xs =>
        AddInviteAndRespond(targetPlayer, x, x.InviterCharId, x.InviterName)
        queuedInvites += targetPlayer -> xs
    }
  }

  /**
    * Enqueue a newly-submitted invitation object
    * either as the active position or into the inactive positions
    * and dispatch a response for any invitation object that is discovered.
    * Implementation of a workflow.
    *
    * @see `AddInvite`
    * @see `indirectInviteResp`
    * @param invitedPlayer  the unique character identifier for the player being invited;
    *                       in actuality, represents the player who will address the invitation object
    * @param targetInvite   a comparison invitation object
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param name           a name to be used in message composition
    */
  def AddInviteAndRespond(
                           invitedPlayer: Long,
                           targetInvite: Invitation,
                           invitingPlayer: Long,
                           name: String,
                           autoApprove: Boolean = false
                         ): Unit = {
    val (player, approval) = targetInvite match {
      case IndirectInvite(_player, _) => (_player, autoApprove)
      case RequestRole(_player, _, _) => (_player, autoApprove)
      case _ => (null, false)
    }
    if (approval) {
      SquadActionMembershipAcceptInvite(player, invitingPlayer, Some(targetInvite), None)
    } else {
      InviteResponseTemplate(indirectInviteResp)(
        targetInvite,
        AddInvite(invitedPlayer, targetInvite),
        invitedPlayer,
        invitingPlayer,
        name
      )
    }
  }

  /**
    * Enqueue a newly-submitted invitation object
    * either as the active position or into the inactive positions
    * and dispatch a response for any invitation object that is discovered.
    * Implementation of a workflow.
    *
    * @see `AddInvite`
    * @see `altIndirectInviteResp`
    * @param targetInvite   a comparison invitation object
    * @param invitedPlayer  the unique character identifier for the player being invited
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param name           a name to be used in message composition
    */
  def AltAddInviteAndRespond(
                              invitedPlayer: Long,
                              targetInvite: Invitation,
                              invitingPlayer: Long,
                              name: String,
                              autoApprove: Boolean = false
                            ): Unit = {
    val (player, approval) = targetInvite match {
      case IndirectInvite(_player, _) => (_player, autoApprove)
      case RequestRole(_player, _, _) => (_player, autoApprove)
      case _ => (null, false)
    }
    if (approval) {
      SquadActionMembershipAcceptInvite(player, invitingPlayer, Some(targetInvite), None)
    } else {
      InviteResponseTemplate(altIndirectInviteResp)(
        targetInvite,
        AddInvite(invitedPlayer, targetInvite),
        invitedPlayer,
        invitingPlayer,
        name
      )
    }
  }

  /**
    * Component method used for the response behavior for processing the invitation object as an `IndirectInvite` object.
    *
    * @see `HandleRequestRole`
    * @param invite         the original invitation object that started this process
    * @param player         the target of the response and invitation
    * @param invitedPlayer  the unique character identifier for the player being invited;
    *                       in actuality, represents the player who will address the invitation object;
    *                       not useful here
    * @param invitingPlayer the unique character identifier for the player who invited the former;
    *                       not useful here
    * @param name           a name to be used in message composition;
    *                       not useful here
    * @return na
    */
  def indirectInviteResp(
                          invite: IndirectInvite,
                          player: Player,
                          invitedPlayer: Long,
                          invitingPlayer: Long,
                          name: String
                        ): Boolean = {
    HandleRequestRole(player, invite)
  }

  /**
    * Component method used for the response behavior for processing the invitation object as an `IndirectInvite` object.
    *
    * @see `HandleRequestRole`
    * @param invite         the original invitation object that started this process
    * @param player         the target of the response and invitation
    * @param invitedPlayer  the unique character identifier for the player being invited
    *                       in actuality, represents the player who will address the invitation object
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param name           a name to be used in message composition
    * @return na
    */
  def altIndirectInviteResp(
                             invite: IndirectInvite,
                             player: Player,
                             invitedPlayer: Long,
                             invitingPlayer: Long,
                             name: String
                           ): Boolean = {
    subs.Publish(
      invitingPlayer,
      SquadResponse.Membership(
        SquadResponseType.Accept,
        0,
        0,
        invitingPlayer,
        Some(invitedPlayer),
        player.Name,
        unk5 = false,
        Some(None)
      )
    )
    HandleRequestRole(player, invite)
  }

  /**
    * A branched response for processing (new) invitation objects that have been submitted to the system.<br>
    * <br>
    * A comparison is performed between the original invitation object and an invitation object
    * that represents the potential modification or redirection of the current active invitation obect.
    * Any further action is only performed when an "is equal" comparison is `true`.
    * When passing, the system publishes up to two messages
    * to users that would anticipate being informed of squad join activity.
    *
    * @param indirectInviteFunc the method that cans the responding behavior should an `IndirectInvite` object being consumed
    * @param targetInvite       a comparison invitation object;
    *                           represents the unmodified, unadjusted invite
    * @param actualInvite       a comparaison invitation object;
    *                           proper use of this field should be the output of another process upon the following `actualInvite`
    * @param invitedPlayer      the unique character identifier for the player being invited
    *                           in actuality, represents the player who will address the invitation object
    * @param invitingPlayer     the unique character identifier for the player who invited the former
    * @param name               a name to be used in message composition
    */
  def InviteResponseTemplate(indirectInviteFunc: (IndirectInvite, Player, Long, Long, String) => Boolean)(
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
          subs.Publish(
            invitedPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              charId,
              Some(invitedPlayer),
              _name,
              unk5 = false,
              Some(None)
            )
          )
          subs.Publish(
            charId,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              invitedPlayer,
              Some(charId),
              _name,
              unk5 = true,
              Some(None)
            )
          )

        case _bid@IndirectInvite(player, _) =>
          indirectInviteFunc(_bid, player, invitedPlayer, invitingPlayer, name)

        case _bid@SpontaneousInvite(player) =>
          val bidInvitingPlayer = _bid.InviterCharId
          subs.Publish(
            invitedPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              bidInvitingPlayer,
              Some(invitedPlayer),
              player.Name,
              unk5 = false,
              Some(None)
            )
          )
          subs.Publish(
            bidInvitingPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              invitedPlayer,
              Some(bidInvitingPlayer),
              player.Name,
              unk5 = true,
              Some(None)
            )
          )

        case _bid@RequestRole(player, _, _) =>
          HandleRequestRole(player, _bid)

        case LookingForSquadRoleInvite(member, _, _) =>
          subs.Publish(
            invitedPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              invitedPlayer,
              Some(member.CharId),
              member.Name,
              unk5 = false,
              Some(None)
            )
          )

        case ProximityInvite(member, _, _) =>
          subs.Publish(
            invitedPlayer,
            SquadResponse.Membership(
              SquadResponseType.Invite,
              0,
              0,
              invitedPlayer,
              Some(member.CharId),
              member.Name,
              unk5 = false,
              Some(None)
            )
          )

        case _ =>
          log.warn(s"AddInviteAndRespond: can not parse discovered unhandled invitation type - $targetInvite")
      }
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
    *
    * @see `AddInviteAndRespond`
    * @see `AltAddInviteAndRespond`
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @param invite        the "new" invitation envelop object
    * @return an optional invite;
    *         the invitation object in the active invite position;
    *         `None`, if it is not added to either the active option or inactive position
    */
  def AddInvite(invitedPlayer: Long, invite: Invitation): Option[Invitation] = {
    invites.get(invitedPlayer).orElse(previousInvites.get(invitedPlayer)) match {
      case Some(_bid) =>
        //the new invite may not interact with the active invite; add to queued invites
        queuedInvites.get(invitedPlayer) match {
          case Some(bidList) =>
            //ensure that new invite does not interact with the queue's invites by invitingPlayer info
            val inviteInviterCharId = invite.InviterCharId
            if (
              _bid.InviterCharId != inviteInviterCharId && !bidList.exists { eachBid =>
                eachBid.InviterCharId == inviteInviterCharId
              }
            ) {
              queuedInvites(invitedPlayer) = invite match {
                case _: RequestRole =>
                  //RequestRole is to be expedited
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
              queuedInvites(invitedPlayer) = List(invite)
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
    * @see `NextInvite`
    * @see `RespondToInvite`
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    */
  def NextInviteAndRespond(invitedPlayer: Long): Unit = {
    NextInvite(invitedPlayer) match {
      case Some(invite) =>
        RespondToInvite(invitedPlayer, invite)
      case None => ;
    }
  }

  /**
    * Compose the response to an invitation.
    * Use standard handling methods for `IndirectInvite` invitation envelops.
    * @see `InviteResponseTemplate`
    * @param invitedPlayer the unique character identifier for the player being invited;
    *                      in actuality, represents the player who will address the invitation object
    * @param invite the invitation envelope used to recover information about the action being taken
    */
  def RespondToInvite(invitedPlayer: Long, invite: Invitation): Unit = {
    InviteResponseTemplate(indirectInviteResp)(
      invite,
      Some(invite),
      invitedPlayer,
      invite.InviterCharId,
      invite.InviterName
    )
  }

  /**
    * Remove any invitation object from the active position.
    * Flag the temporary field to indicate that the active position, while technically available,
    * should not yet have a new invitation object shifted into it yet.
    * This is the "proper" way to demote invitation objects from the active position
    * whether or not they are to be handled
    * except in cases of manipulative cleanup.
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
    * @param features the squad
    * @param invitedPlayer the unique character identifier for the player being invited
    * @param invitingPlayer the unique character identifier for the player who invited the former
    * @param recruit the player being invited
    * @return the squad object and a role position index, if properly invited;
    *         `None`, otherwise
    */
  def HandleVacancyInvite(
                           features: SquadFeatures,
                           invitedPlayer: Long,
                           invitingPlayer: Long,
                           recruit: Player
                         ): Option[(SquadFeatures, Int)] = {
    //accepted an invitation to join an existing squad
    val squad = features.Squad
    squad.Membership.zipWithIndex.find({
      case (_, index) =>
        squad.isAvailable(index, recruit.avatar.certifications)
    }) match {
      case Some((_, line)) =>
        //position in squad found
        if (!features.AutoApproveInvitationRequests && squad.Leader.CharId != invitingPlayer) {
          //the inviting player was not the squad leader and this decision should be bounced off the squad leader
          AltAddInviteAndRespond(
            squad.Leader.CharId,
            IndirectInvite(recruit, features),
            invitingPlayer,
            name = ""
          )
          log.debug(s"HandleVacancyInvite: ${recruit.Name} must await an invitation from the leader of squad ${squad.Task}")
          None
        } else {
          Some((features, line))
        }
      case _ =>
        None
    }
  }

  /**
    * An overloaded entry point to the functionality for handling one player requesting a specific squad role.
    *
    * @param player the player who wants to join the squad
    * @param bid    a specific kind of `Invitation` object
    * @return `true`, if the player is not denied the possibility of joining the squad;
    *         `false`, otherwise, of it the squad does not exist
    */
  def HandleRequestRole(player: Player, bid: RequestRole): Boolean = {
    HandleRequestRole(player, bid.features, bid)
  }

  /**
    * An overloaded entry point to the functionality for handling indirection when messaging the squad leader about an invite.
    *
    * @param player the player who wants to join the squad
    * @param bid    a specific kind of `Invitation` object
    * @return `true`, if the player is not denied the possibility of joining the squad;
    *         `false`, otherwise, of it the squad does not exist
    */
  def HandleRequestRole(player: Player, bid: IndirectInvite): Boolean = {
    HandleRequestRole(player, bid.features, bid)
  }

  /**
    * The functionality for handling indirection
    * for handling one player requesting a specific squad role
    * or when messaging the squad leader about an invite.<br>
    * <br>
    * At this point in the squad join process, the only consent required is that of the squad leader.
    * An automatic consent flag exists on the squad;
    * but, if that is not set, then the squad leader must be asked whether or not to accept or to reject the recruit.
    * If the squad leader changes in the middle or the latter half of the process,
    * the invitation may still fail even if the old squad leader accepts.
    * If the squad leader changes in the middle of the latter half of the process,
    * the inquiry might be posed again of the new squad leader, of whether to accept or to reject the recruit.
    *
    * @param player   the player who wants to join the squad
    * @param features the squad
    * @param bid      the `Invitation` object that was the target of this request
    * @return `true`, if the player is not denied the possibility of joining the squad;
    *         `false`, otherwise, of it the squad does not exist
    */
  def HandleRequestRole(player: Player, features: SquadFeatures, bid: Invitation): Boolean = {
    val leaderCharId = features.Squad.Leader.CharId
    subs.Publish(leaderCharId, SquadResponse.WantsSquadPosition(leaderCharId, player.Name))
    true
  }

  /**
    * Determine whether a player is sufficiently unemployed
    * and has no grand delusions of being a squad leader.
    * @see `CloseSquad`
    * @param features an optional squad
    * @return `true`, if the target player possesses no squad or the squad is nonexistent;
    *         `false`, otherwise
    */
  def EnsureEmptySquad(features: Option[SquadFeatures]): Boolean = {
    features match {
      case Some(squad) => EnsureEmptySquad(squad)
      case None        => true
    }
  }

  /**
    * Determine whether a player is sufficiently unemployed
    * and has no grand delusions of being a squad leader.
    * @see `CloseSquad`
    * @param features the squad
    * @return `true`, if the target player possesses no squad or a squad that is suitably nonexistent;
    *         `false`, otherwise
    */
  def EnsureEmptySquad(features: SquadFeatures): Boolean = {
    val ensuredEmpty = features.Squad.Size <= 1
    if (ensuredEmpty) {
      CleanUpAllInvitesToSquad(features)
    }
    ensuredEmpty
  }

  /**
    * Behaviors and exchanges necessary to complete the fulfilled recruitment process for the squad role.<br>
    * <br>
    * This operation is fairly safe to call whenever a player is to be inducted into a squad.
    * The aforementioned player must have a callback retained in `subs.UserEvents`
    * and conditions imposed by both the role and the player must be satisfied.
    * @see `CleanUpAllInvitesWithPlayer`
    * @see `Squad.isAvailable`
    * @see `Squad.Switchboard`
    * @see `SquadSubscriptionEntity.MonitorSquadDetails`
    * @see `SquadSubscriptionEntity.Publish`
    * @see `SquadSubscriptionEntity.Join`
    * @see `SquadSubscriptionEntity.UserEvents`
    * @param player the new squad member;
    *               this player is NOT the squad leader
    * @param features the squad the player is joining
    * @param position the squad member role that the player will be filling
    * @return `true`, if the player joined the squad in some capacity;
    *         `false`, if the player did not join the squad or is already a squad member
    */
  def JoinSquad(player: Player, features: SquadFeatures, position: Int): Boolean = {
    CleanUpAllInvitesWithPlayer(player.CharId)
    parent ! SquadService.PerformJoinSquad(player, features, position)
    true
  }

  /**
    * This player has been refused to join squads by these players, or to form squads with these players.
    * @param charId the player who refused other players
    * @return the list of other players who have refused this player
    */
  def Refused(charId: Long): List[Long] = refused.getOrElse(charId, Nil)

  /**
    * This player has been refused to join squads by this squad leaders, or to form squads with this other player.
    * @param charId the player who is being refused
    * @param refusedCharId the player who refused
    * @return the list of other players who have refused this player
    */
  def Refused(charId: Long, refusedCharId: Long): List[Long] = {
    if (charId != refusedCharId) {
      Refused(charId, List(refusedCharId))
    } else {
      Nil
    }
  }

  /**
    * This player has been refused to join squads by these squad leaders, or to form squads with these other players.
    * @param charId the player who is being refused
    * @param list the players who refused
    * @return the list of other players who have refused this player
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
    * This player has been refused to join squads by this squad leaders, or to form squads with this other player.
    * They are now allowed.
    * @param charId the player who is being refused
    * @param permittedCharId the player who was previously refused
    * @return the list of other players who have refused this player
    */
  def Allowed(charId: Long, permittedCharId: Long): List[Long] = {
    if (charId != permittedCharId) {
      Allowed(charId, List(permittedCharId))
    } else {
      Nil
    }
  }

  /**
    * This player has been refused to join squads by these squad leaders, or to form squads with these other players.
    * They are now allowed.
    * @param charId the player who is being refused
    * @param list the players who was previously refused
    * @return the list of other players who have refused this player
    */
  def Allowed(charId: Long, list: List[Long]): List[Long] = {
    refused.get(charId) match {
      case Some(refusedList) =>
        refused(charId) = refusedList.filterNot(list.contains)
        Refused(charId)
      case None =>
        Nil
    }
  }

  /**
    * Remove all inactive invites associated with this player.
    * @param charId the unique character identifier for the player being invited;
    *               in actuality, represents the player who will address the invitation object
    * @return a list of the removed inactive invitation objects
    */
  def CleanUpQueuedInvites(charId: Long): Unit = {
    val allSquadGuids = queuedInvites.remove(charId) match {
      case Some(bidList) =>
        bidList.collect {
          case VacancyInvite(_, _, guid)             => guid
          case IndirectInvite(_, guid)               => guid
          case LookingForSquadRoleInvite(_, guid, _) => guid
          case ProximityInvite(_, guid, _)           => guid
          case RequestRole(_, guid, _)               => guid
        }
      case None =>
        Nil
    }
    val list = List(charId)
    allSquadGuids.foreach { CleanUpSquadFeatures(list, _, position = -1) }
  }

  def CleanUpSquadFeatures(removed: List[Long], features: SquadFeatures, position: Int): Unit = {
    features.ProxyInvites = features.ProxyInvites.filterNot(removed.contains)
    if (features.ProxyInvites.isEmpty) {
      features.SearchForRole = None
    }
  }

  /**
    * Remove all invitation objects
    * that are related to the particular squad and the particular role in the squad.
    * Specifically used to safely disarm obsolete invitation objects related to the specific criteria.
    * Affects only certain invitation object types
    * including "player requesting role" and "leader requesting recruiting role".
    * @see `RemoveActiveInvitesForSquadAndPosition`
    * @see `RemoveQueuedInvitesForSquadAndPosition`
    * @param features the squad
    * @param position the role position index
    */
  def CleanUpInvitesForSquadAndPosition(features: SquadFeatures, position: Int): Unit = {
    val guid = features.Squad.GUID
    CleanUpSquadFeatures(
      RemoveActiveInvitesForSquadAndPosition(guid, position) ++ RemoveQueuedInvitesForSquadAndPosition(guid, position),
      features,
      position
    )
  }

  /**
    * Remove all inactive invitation objects that are related to the particular squad and the particular role in the squad.
    * Specifically used to safely disarm obsolete invitation objects by specific criteria.
    * Affects only certain invitation object types.
    * @see `RequestRole`
    * @see `LookingForSquadRoleInvite`
    * @see `CleanUpInvitesForSquadAndPosition`
    * @param features the squa
    * @param position the role position index
    */
  def CleanUpQueuedInvitesForSquadAndPosition(features: SquadFeatures, position: Int): Unit = {
    CleanUpSquadFeatures(
      RemoveQueuedInvitesForSquadAndPosition(features.Squad.GUID, position),
      features,
      position
    )
  }

  /**
    * Remove all invitation objects that are related to the particular squad.
    * Specifically used to safely disarm obsolete invitation objects by specific criteria.
    * Affects all invitation object types and all data structures that deal with the squad.
    * @see `RequestRole`
    * @see `IndirectInvite`
    * @see `LookingForSquadRoleInvite`
    * @see `ProximityInvite`
    * @see `RemoveInvite`
    * @see `VacancyInvite`
    * @param features the squad identifier
    */
  def CleanUpAllInvitesToSquad(features: SquadFeatures): Unit = {
    val guid = features.Squad.GUID
    //clean up invites
    val activeInviteIds = {
      val keys = invites.keys.toSeq
      invites.values.zipWithIndex
        .collect {
          case (VacancyInvite(_, _, _squad), index) if _squad.Squad.GUID == guid             => index
          case (IndirectInvite(_, _squad), index) if _squad.Squad.GUID == guid               => index
          case (LookingForSquadRoleInvite(_, _squad, _), index) if _squad.Squad.GUID == guid => index
          case (ProximityInvite(_, _squad, _), index) if _squad.Squad.GUID == guid           => index
          case (RequestRole(_, _squad, _), index) if _squad.Squad.GUID == guid               => index
        }
        .map { index =>
          val key = keys(index)
          RemoveInvite(key)
          key
        }
        .toList
    }
    //tidy the queued invitations
    val queuedInviteIds = {
      val keys = queuedInvites.keys.toSeq
      queuedInvites.values.zipWithIndex
        .collect {
          case (queue, index) =>
            val key = keys(index)
            val (targets, retained) = queue.partition {
              case VacancyInvite(_, _, _squad)             => _squad.Squad.GUID == guid
              case IndirectInvite(_, _squad)               => _squad.Squad.GUID == guid
              case LookingForSquadRoleInvite(_, _squad, _) => _squad.Squad.GUID == guid
              case ProximityInvite(_, _squad, _)           => _squad.Squad.GUID == guid
              case RequestRole(_, _squad, _)               => _squad.Squad.GUID == guid
              case _                                       => false
            }
            if (retained.isEmpty) {
              queuedInvites.remove(key)
            } else {
              queuedInvites += key -> retained
            }
            if (targets.nonEmpty) {
              Some(key)
            } else {
              None
            }
        }
        .flatten
        .toList
    }
    CleanUpSquadFeatures(activeInviteIds ++ queuedInviteIds, features, position = -1)
  }

  /**
    * Remove all active and inactive invitation objects that are related to the particular player.
    * Specifically used to safely disarm obsolete invitation objects by specific criteria.
    * Affects all invitation object types and all data structures that deal with the player.
    * @see `RequestRole`
    * @see `IndirectInvite`
    * @see `LookingForSquadRoleInvite`
    * @see `RemoveInvite`
    * @see `CleanUpAllProximityInvites`
    * @see `VacancyInvite`
    * @param charId the player's unique identifier number
    */
  def CleanUpAllInvitesWithPlayer(charId: Long): Unit = {
    //clean up our active invitation
    val charIdInviteSquadGuid = RemoveInvite(charId) match {
      case Some(VacancyInvite(_, _, guid))             => Some(guid)
      case Some(IndirectInvite(_, guid))               => Some(guid)
      case Some(LookingForSquadRoleInvite(_, guid, _)) => Some(guid)
      case Some(ProximityInvite(_, guid, _))           => Some(guid)
      case Some(RequestRole(_, guid, _))               => Some(guid)
      case _                                           => None
    }
    //clean up invites
    val (activeInviteIds, activeSquadGuids) = {
      val keys = invites.keys.toSeq
      invites.values.zipWithIndex
        .collect {
          case (SpontaneousInvite(player), index) if player.CharId == charId                  => (index, None)
          case (VacancyInvite(player, _, guid), index) if player == charId                    => (index, Some(guid))
          case (IndirectInvite(player, guid), index) if player.CharId == charId               => (index, Some(guid))
          case (LookingForSquadRoleInvite(member, guid, _), index) if member.CharId == charId => (index, Some(guid))
          case (ProximityInvite(member, guid, _), index) if member.CharId == charId           => (index, Some(guid))
          case (RequestRole(player, guid, _), index) if player.CharId == charId               => (index, Some(guid))
        }
        .map { case (index, guid) =>
          val key = keys(index)
          RemoveInvite(key)
          (key, guid)
        }
        .unzip
    }
    //tidy the queued invitations
    val (queuedInviteIds, queuedSquadGuids) = {
      val keys = queuedInvites.keys.toSeq
      queuedInvites.values.zipWithIndex
        .collect {
          case (queue, index) =>
            val key = keys(index)
            val (targets, retained) = if(key != charId) {
              queue.partition {
                case SpontaneousInvite(player)               => player.CharId == charId
                case VacancyInvite(player, _, _)             => player == charId
                case IndirectInvite(player, _)               => player.CharId == charId
                case LookingForSquadRoleInvite(member, _, _) => member.CharId == charId
                case ProximityInvite(member, _, _)           => member.CharId == charId
                case RequestRole(player, _, _)               => player.CharId == charId
                case _                                       => false
              }
            } else {
              (queue, Nil)
            }
            if (retained.isEmpty) {
              queuedInvites.remove(key)
            } else {
              queuedInvites += key -> retained
            }
            if (targets.nonEmpty) {
              Some((
                key,
                targets.collect {
                  case VacancyInvite(_, _, guid)             => guid
                  case IndirectInvite(_, guid)               => guid
                  case LookingForSquadRoleInvite(_, guid, _) => guid
                  case ProximityInvite(_, guid, _)           => guid
                  case RequestRole(_, guid, _)               => guid
                }
              ))
            } else {
              None
            }
        }
        .flatten
        .toList
        .unzip
    }
    val allInvites = (activeInviteIds ++ queuedInviteIds).toList.distinct
    ((activeSquadGuids.toSeq :+ charIdInviteSquadGuid) ++ queuedSquadGuids)
      .flatten
      .distinct
      .foreach { guid => CleanUpSquadFeatures(allInvites, guid, position = -1) }
  }

  /**
    * Remove all active and inactive proximity squad invites.
    * This is related to recruitment from the perspective of the recruiter.
    * @param charId the player
    */
  def CleanUpAllProximityInvites(charId: Long): Unit = {
    //clean up invites
    val (activeInviteIds, activeSquadGuids) = {
      val keys = invites.keys.toSeq
      invites.values.zipWithIndex
        .collect { case (ProximityInvite(member, guid, _), index) if member.CharId == charId => (index, Some(guid)) }
        .map { case (index, guid) =>
          val key = keys(index)
          RemoveInvite(key)
          (key, guid)
        }
        .unzip
    }
    //tidy the queued invitations
    val (queuedInviteIds, queuedSquadGuids) = {
      val keys = queuedInvites.keys.toSeq
      queuedInvites.values.zipWithIndex
        .collect {
          case (queue, index) =>
            val key = keys(index)
            val (targets, retained) = queue.partition {
              case ProximityInvite(member, _, _) => member.CharId == charId
              case _                             => false
            }
            if (retained.isEmpty) {
              queuedInvites.remove(key)
            } else {
              queuedInvites += key -> retained
            }
            if (targets.nonEmpty) {
              Some((key, targets.collect { case ProximityInvite(_, guid, _) => guid } ))
            } else {
              None
            }
        }
        .flatten
        .toList
        .unzip
    }
    val allInvites = (activeInviteIds ++ queuedInviteIds).toList.distinct
    (activeSquadGuids.toSeq ++ queuedSquadGuids)
      .flatten
      .distinct
      .foreach { guid => CleanUpSquadFeatures(allInvites, guid, position = -1) }
  }

  /**
    * Remove all active and inactive proximity squad invites for a specific squad.
    * @param features the squad
    */
  def CleanUpProximityInvites(features: SquadFeatures): Unit = {
    val squadGuid = features.Squad.GUID
    //clean up invites
    val activeInviteIds = {
      val keys = invites.keys.toSeq
      invites.values.zipWithIndex
        .collect {
          case (ProximityInvite(_, _squad, _), index) if _squad.Squad.GUID == squadGuid => index
        }
        .map { index =>
          val key = keys(index)
          RemoveInvite(key)
          key
        }
    }
    //tidy the queued invitations
    val queuedInviteIds = {
      val keys = queuedInvites.keys.toSeq
      queuedInvites.values.zipWithIndex
        .collect {
          case (queue, index) =>
            val key = keys(index)
            val (targets, retained) = queue.partition {
              case ProximityInvite(_, _squad, _) => _squad.Squad.GUID == squadGuid
              case _                           => false
            }
            if (retained.isEmpty) {
              queuedInvites.remove(key)
            } else {
              queuedInvites += key -> retained
            }
            if (targets.nonEmpty) {
              keys.lift(index)
            } else {
              None
            }
        }
        .flatten
        .toList
    }
    CleanUpSquadFeatures((activeInviteIds ++ queuedInviteIds).toList.distinct, features, position = -1)
  }

  /**
    * Remove all active invitation objects
    * that are related to the particular squad and the particular role in the squad.
    * Specifically used to safely disarm obsolete invitation objects related to the specific criteria.
    * Affects only certain invitation object types
    * including "player requesting role" and "leader requesting recruiting role".
    * @see `RequestRole`
    * @see `LookingForSquadRoleInvite`
    * @see `ProximityInvite`
    * @see `RemoveInvite`
    * @param guid the squad identifier
    * @param position the role position index
    * @return the character ids of all players whose invites were removed
    */
  def RemoveActiveInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): List[Long] = {
    val keys = invites.keys.toSeq
    invites.values.zipWithIndex
      .collect {
        case (LookingForSquadRoleInvite(_, _squad, pos), index) if _squad.Squad.GUID == guid && pos == position => index
        case (ProximityInvite(_, _squad, pos), index) if _squad.Squad.GUID == guid && pos == position           => index
        case (RequestRole(_, _squad, pos), index) if _squad.Squad.GUID == guid && pos == position               => index
      }
      .map { index =>
        val key = keys(index)
        RemoveInvite(key)
        key
      }
      .toList
  }

  /**
    * Remove all inactive invitation objects that are related to the particular squad and the particular role in the squad.
    * Specifically used to safely disarm obsolete invitation objects by specific criteria.
    * Affects only certain invitation object types.
    * @see `RequestRole`
    * @see `LookingForSquadRoleInvite`
    * @see `CleanUpInvitesForSquadAndPosition`
    * @param guid the squad identifier
    * @param position the role position index
    * @return the character ids of all players whose invites were removed
    */
  def RemoveQueuedInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): List[Long] = {
    val keys = queuedInvites.keys.toSeq
    queuedInvites.values.zipWithIndex
      .collect {
        case (queue, index) =>
          val key = keys(index)
          val (targets, retained) = queue.partition {
            case LookingForSquadRoleInvite(_, _squad, pos) => _squad.Squad.GUID == guid && pos == position
            case ProximityInvite(_, _squad, pos)           => _squad.Squad.GUID == guid && pos == position
            case RequestRole(_, _squad, pos)               => _squad.Squad.GUID == guid && pos == position
            case _                                         => false
          }
          if (retained.isEmpty) {
            queuedInvites.remove(key)
          } else {
            queuedInvites += key -> retained
          }
          if (targets.nonEmpty) {
            Some(key)
          } else {
            None
          }
      }
      .flatten
      .toList
  }

  def FindSoldiersWithinScopeAndInvite(
                                        invitingPlayer: Member,
                                        features: SquadFeatures,
                                        position: Int,
                                        scope: List[Avatar],
                                        excluded: List[Long],
                                        invitationEnvelopFunc: (Member, SquadFeatures, Int) => Invitation
                                      ): Option[Long] = {
    val invitingPlayerCharId = invitingPlayer.CharId
    val invitingPlayerName = invitingPlayer.Name
    val squad = features.Squad
    val faction = squad.Faction
    val squadLeader = squad.Leader.CharId
    val deniedAndExcluded = features.DeniedPlayers() ++ excluded
    val requirementsToMeet = squad.Membership(position).Requirements
    //find a player who is of the same faction as the squad, is LFS, and is eligible for the squad position
    scope
      .find { avatar =>
        val charId = avatar.id
        faction == avatar.faction &&
          avatar.lookingForSquad &&
          !deniedAndExcluded.contains(charId) &&
          !refused(charId).contains(squadLeader) &&
          requirementsToMeet.intersect(avatar.certifications) == requirementsToMeet
      } match {
      case None =>
        None
      case Some(invitedPlayer) =>
        //add invitation for position in squad
        val invite = invitationEnvelopFunc(invitingPlayer, features, position)
        val id = invitedPlayer.id
        AddInviteAndRespond(id, invite, invitingPlayerCharId, invitingPlayerName)
        Some(id)
    }
  }
}

object SquadInvitationManager {
  final case class Join(charId: Long)

  /**
    * The base of all objects that exist for the purpose of communicating invitation from one player to the next.
    * @param char_id the inviting player's unique identifier number
    * @param name the inviting player's name
    */
  sealed abstract class Invitation(char_id: Long, name: String) {
    def InviterCharId: Long = char_id
    def InviterName: String = name
  }

  /**
    * Utilized when one player attempts to join an existing squad in a specific role.
    * Accessed by the joining player from the squad detail window.
    * This invitation is handled by the squad leader.
    * @param player the player who requested the role
    * @param features the squad with the role
    * @param position the index of the role
    */
  private case class RequestRole(player: Player, features: SquadFeatures, position: Int)
    extends Invitation(player.CharId, player.Name)

  /**
    * Utilized when one squad member issues an invite for some other player.
    * Accessed by an existing squad member using the "Invite" menu option on another player.
    * This invitation is handled by the player who would join the squad.
    * @param char_id the unique character identifier of the player who sent the invite
    * @param name the name the player who sent the invite
    * @param features the squad
    */
  private case class VacancyInvite(char_id: Long, name: String, features: SquadFeatures)
    extends Invitation(char_id, name)

  /**
    * Utilized to redirect an (accepted) invitation request to the proper squad leader.
    * No direct action causes this message.
    * Depending on the situation, either the squad leader or the player who would join the squad handle this invitation.
    * @param player the player who would be joining the squad;
    *               may or may not have actually requested it in the first place
    * @param features the squad
    */
  private case class IndirectInvite(player: Player, features: SquadFeatures)
    extends Invitation(player.CharId, player.Name)

  /**
    * Utilized in conjunction with an external queuing data structure
    * to search for and submit requests to other players
    * for the purposes of fill out unoccupied squad roles.
    * This invitation is handled by the player who would be joining the squad.
    * @param leader the squad leader
    * @param features the squad
    * @param position the index of a role
    */
  private case class ProximityInvite(leader: Member, features: SquadFeatures, position: Int)
    extends Invitation(leader.CharId, leader.Name)

  /**
    * Utilized in conjunction with an external queuing data structure
    * to search for and submit requests to other players
    * for the purposes of fill out an unoccupied squad role.
    * This invitation is handled by the player who would be joining the squad.
    * @param leader the squad leader
    * @param features the squad with the role
    * @param position the index of the role
    */
  private case class LookingForSquadRoleInvite(leader: Member, features: SquadFeatures, position: Int)
    extends Invitation(leader.CharId, leader.Name)

  /**
    * Utilized when one player issues an invite for some other player for a squad that does not yet exist.
    * This invitation is handled by the player who would be joining the squad.
    * @param player the player who wishes to become the leader of a squad
    */
  private case class SpontaneousInvite(player: Player) extends Invitation(player.CharId, player.Name)

  /**
    * na
    * @param invitingPlayer na
    * @param features na
    * @param position na
    * @return na
    */
  private def ProximityEnvelope(
                         invitingPlayer: Member,
                         features: SquadFeatures,
                         position: Int
                       ): Invitation = {
    ProximityInvite(invitingPlayer, features, position)
  }

  /**
    * na
    * @param invitingPlayer na
    * @param features na
    * @param position na
    * @return na
    */
  private def LookingForSquadRoleEnvelope(
                                   invitingPlayer: Member,
                                   features: SquadFeatures,
                                   position: Int
                                 ): Invitation = {
    LookingForSquadRoleInvite(invitingPlayer, features, position)
  }

  final case class FinishStartSquad(features: SquadFeatures)
}
