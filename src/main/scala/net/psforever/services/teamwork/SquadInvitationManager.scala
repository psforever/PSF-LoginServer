// Copyright (c) 2022 PSForever
package net.psforever.services.teamwork

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.Future
//
import net.psforever.objects.Player
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.teamwork.{Member, SquadFeatures}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{SquadDetail, SquadPositionDetail, SquadPositionEntry}
import net.psforever.services.teamwork.invitations.{
  IndirectInvite,
  Invitation,
  InvitationToCreateASquad,
  InvitationToJoinSquad,
  LookingForSquadRoleInvite,
  ProximityInvite,
  RequestToJoinSquadRole
}
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
    that player will not be able to send further invitations (field on squad's features).
    If the squad leader sends an invitation request for that player,
    the current player is cleared from the blocked list.
   */
  /**
    * The given player has refused participation into this other player's squad.<br>
    * key - a unique character identifier number;
    * value - a list of unique character identifier numbers; squad leaders or once-squad leaders
    */
  private val refusedPlayers: mutable.LongMap[List[Long]] = mutable.LongMap[List[Long]]()

  private[this] val log = org.log4s.getLogger

  def postStop(): Unit = {
    invites.clear()
    queuedInvites.clear()
    previousInvites.clear()
    refusedPlayers.clear()
  }

  /* whenever a new player joins */

  def handleJoin(charId: Long): Unit = {
    refusedPlayers.put(charId, List[Long]())
  }

  /* create invitations */

  def createRequestToJoinSquadRole(player: Player, features: SquadFeatures, position: Int): Unit = {
    //we could join directly but we need permission from the squad leader first
    val charId = features.Squad.Leader.CharId
    val requestRole = RequestToJoinSquadRole(player, features, position)
    if (features.AutoApproveInvitationRequests) {
      requestRole.handleAcceptance(manager = this, player, charId, None)
    } else if (addInvite(charId, requestRole).contains(requestRole)) {
      //circumvent tests in AddInviteAndRespond
      requestRole.handleInvitation(altIndirectInviteResp)(
        manager = this,
        charId,
        invitingPlayer = 0L, /* we ourselves technically are ... */
        player.Name
      )
    }
  }

  def createInvitationToJoinSquad(player: Player, invitedPlayer: Long, features: SquadFeatures): Unit = {
    val invitingPlayer = player.CharId
    val squad = features.Squad
    allowed(invitedPlayer, invitingPlayer)
    if (squad.Size == squad.Capacity) {
      log.debug(s"$invitingPlayer tried to invite $invitedPlayer to a squad without available positions")
    } else if (refused(invitingPlayer).contains(invitedPlayer)) {
      log.debug(s"$invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
    } else {
      features.AllowedPlayers(invitingPlayer)
      addInviteAndRespond(
        invitedPlayer,
        InvitationToJoinSquad(invitingPlayer, player.Name, features),
        invitingPlayer,
        player.Name
      )
    }
  }

  def createPermissionToRedirectInvite(player: Player, invitingPlayer: Long, features: SquadFeatures): Unit = {
    val leader = features.Squad.Leader.CharId
    addInviteAndRespond(
      leader,
      IndirectInvite(player, features),
      invitingPlayer,
      player.Name
    )
  }

  def createIndirectInvite(player: Player, invitingPlayer: Long, features: SquadFeatures): Unit = {
    val invitedPlayer = player.CharId
    val squad = features.Squad
    val leader = squad.Leader.CharId
    allowed(invitedPlayer, invitingPlayer)
    allowed(leader, invitingPlayer)
    lazy val preface = s"$invitingPlayer's invitation got reversed to $invitedPlayer's squad, but"
    if (squad.Size == squad.Capacity) {
      log.debug(s"$preface the squad has no available positions")
    } else if (refused(invitingPlayer).contains(invitedPlayer)) {
      log.debug(s"$preface $invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
    } else if (refused(invitingPlayer).contains(leader)) {
      log.debug(s"$preface $leader repeated a previous refusal to $invitingPlayer's invitation offer")
    } else if (features.DeniedPlayers().contains(invitingPlayer)) {
      log.debug(s"$preface $invitingPlayer is denied the invitation")
    } else {
      features.AllowedPlayers(invitedPlayer)
      addInviteAndRespond(
        leader,
        IndirectInvite(player, features),
        invitingPlayer,
        player.Name
      )
    }
  }

  def createInvitationToCreateASquad(player: Player, invitedPlayer: Long): Unit = {
    //neither the invited player nor the inviting player belong to any squad
    val invitingPlayer = player.CharId
    allowed(invitedPlayer, invitingPlayer)
    if (refused(invitingPlayer).contains(invitedPlayer)) {
      log.debug(s"$invitedPlayer repeated a previous refusal to $invitingPlayer's invitation offer")
    } else if (refused(invitedPlayer).contains(invitingPlayer)) {
      log.debug(s"$invitingPlayer repeated a previous refusal to $invitedPlayer's invitation offer")
    } else {
      addInviteAndRespond(
        invitedPlayer,
        InvitationToCreateASquad(player),
        invitingPlayer,
        player.Name
      )
    }
  }

  def createProximityInvite(zone: Zone, invitingPlayer: Long, features: SquadFeatures): Unit = {
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
            val comment = s"An invitation to squad '${features.Squad.Task}' was filled by a different player."
            cleanUpQueuedInvitesForSquadAndPosition(features.Squad.GUID, position)
              .foreach { id => subs.Publish(id, SquadResponse.SquadRelatedComment(comment)) }
            squad.Membership.zipWithIndex
        }
      case _ =>
        features.SearchForRole = -1
        squad.Membership.zipWithIndex
    })
      .collect { case (member, index) if member.CharId == 0 && squad.Availability(index) => (member, index) }
      .sortBy({ _._1.Requirements.foldLeft(0)(_ + _.value) })(Ordering.Int.reverse)
    //find recruits
    val faction = squad.Faction
    val players = zone.Players.filter(_.faction == faction)
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
      positionsToRecruitFor
        .foreach { case (_, position) =>
          findSoldiersWithinScopeAndInvite(
            squad.Leader,
            features,
            position,
            players,
            features.ProxyInvites ++ newRecruitment,
            proximityEnvelope
          )
          .collect { id =>
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
        case (Some(-1), _) => ()
        case (Some(position), Some(LookingForSquadRoleInvite(member, _, _))) =>
          invites.put(key, ProximityInvite(member, features, position))
        case _ => ()
      }
    }
  }

  /* invitation reloads */

  def listCurrentInvitations(charId: Long): List[String] = {
    ((invites.get(charId), queuedInvites.get(charId)) match {
      case (Some(invite), Some(invites)) =>
        invite +: invites
      case (Some(invite), None) =>
        List(invite)
      case (None, Some(invites)) =>
        invites
      case _ =>
        List()
    }).collect {
      case RequestToJoinSquadRole(player, _, _) => player.Name
      case IndirectInvite(player, features) if !features.Squad.Leader.Name.equals(player.Name) => player.Name
    }
  }

  def reloadActiveInvite(charId: Long): Unit = {
    invites
      .get(charId)
      .foreach(respondToInvite(charId, _))
  }

  def reloadSearchForRoleInvite(
                                 scope: List[Avatar],
                                 rejectingPlayer: Long,
                                 features: SquadFeatures,
                                 position: Int
                               ) : Unit = {
    //rejectingPlayer is the would-be squad member; the squad leader rejected the request
    val squadLeader = features.Squad.Leader
    refused(rejectingPlayer, squadLeader.CharId)
    features.ProxyInvites = features.ProxyInvites.filterNot { _ == rejectingPlayer }
    findSoldiersWithinScopeAndInvite(
      squadLeader,
      features,
      position,
      scope,
      features.ProxyInvites,
      lookingForSquadRoleEnvelope
    ) match {
      case None =>
        if (features.SearchForRole.contains(position) && features.ProxyInvites.isEmpty) {
          features.SearchForRole = None
          subs.Publish(
            squadLeader.CharId,
            SquadResponse.SquadRelatedComment("Exhausted all possible candidates to fill the open squad position.")
          )
        }
      case Some(id) =>
        features.ProxyInvites = features.ProxyInvites :+ id
    }
    if (features.ProxyInvites.isEmpty) {
      features.SearchForRole = None
    }
  }

  def reloadProximityInvite(
                             scope: List[Avatar],
                             rejectingPlayer: Long,
                             features: SquadFeatures,
                             position: Int
                           ): Unit = {
    //rejectingPlayer is the would-be squad member; the squad leader rejected the request
    val squadLeader = features.Squad.Leader
    refused(rejectingPlayer, squadLeader.CharId)
    features.ProxyInvites = features.ProxyInvites.filterNot { _ == rejectingPlayer }
    findSoldiersWithinScopeAndInvite(
      squadLeader,
      features,
      position,
      scope,
      features.ProxyInvites,
      proximityEnvelope
    )
      .collect { id =>
        features.ProxyInvites = features.ProxyInvites :+ id
        id
      }
      .orElse {
        if (features.SearchForRole.contains(-1) && features.ProxyInvites.isEmpty) {
          features.SearchForRole = None
          subs.Publish(
            squadLeader.CharId,
            SquadResponse.SquadRelatedComment("Exhausted all possible local candidates to fill the open squad positions.")
          )
        }
        None
      }
  }

  /* acceptance */

  def handleAcceptance(player: Player, charId: Long, squadOpt: Option[SquadFeatures]): Unit = {
    removeInvite(charId)
      .map { invite =>
        invite.handleAcceptance(manager = this, player, charId, squadOpt)
        invite
      }
      .orElse {
        //the invite either timed-out or was withdrawn or is now invalid
        //originally, only InvitationToCreateASquad, InvitationToJoinSquad, ProximityInvite, LookingForSquadRoleInvite
        previousInvites
          .get(charId)
          .map { invite => (invite.inviterCharId, invite.inviterName) } match {
          case Some((0L, "")) => ()
          case Some((charId, name)) =>
            subs.Publish(
              charId,
              SquadResponse.Membership(SquadResponseType.Cancel, charId, Some(0L), name, unk5 = false)
            )
        }
        None
      }
    nextInviteAndRespond(charId)
  }

  def tryChainAcceptance(
                          inviter: Player,
                          charId: Long,
                          list: List[Long],
                          features: SquadFeatures
                        ): Unit = {
    //filter queued invites
    lazy val squadName = features.Squad.Task
    var foundPairs: List[(Player, Invitation)] = List()
    val unmatchedInvites = queuedInvites
      .getOrElse(charId, Nil)
      .filter {
        case invite @ RequestToJoinSquadRole(invitee, _, _)
          if list.contains(invitee.CharId) && !features.Squad.Leader.Name.equals(invitee.Name) =>
          foundPairs = foundPairs :+ (invitee, invite)
          false
        case invite @ IndirectInvite(invitee, _)
          if list.contains(invitee.CharId) && !features.Squad.Leader.Name.equals(invitee.Name) =>
          foundPairs = foundPairs :+ (invitee, invite)
          false
        case _ =>
          true
      }
    //handle active invite
    val clearedActiveInvite = invites
      .get(charId)
      .collect {
        case invite @ RequestToJoinSquadRole(invitee, _, _)
          if list.contains(invitee.CharId) && !features.Squad.Leader.Name.equals(invitee.Name) =>
          invite.handleAcceptance(manager = this, inviter, invitee.CharId, Some(features))
          invites.remove(charId)
          true
        case invite @ IndirectInvite(invitee, _)
          if list.contains(invitee.CharId) && !features.Squad.Leader.Name.equals(invitee.Name) =>
          invite.handleAcceptance(manager = this, inviter, invitee.CharId, Some(features))
          invites.remove(charId)
          true
        case _ =>
          false
      }
    //handle selected queued invites
    val pairIterator = foundPairs.iterator
    while (pairIterator.hasNext && features.Squad.Capacity < features.Squad.Size) {
      val (player, invite) = pairIterator.next()
      invite.handleAcceptance(manager = this, inviter, player.CharId, Some(features))
    }
    //evaluate final squad composition
    if (features.Squad.Capacity < features.Squad.Size) {
      //replace unfiltered invites
      if (unmatchedInvites.isEmpty) {
        queuedInvites.remove(charId)
      } else {
        queuedInvites.put(charId, unmatchedInvites)
      }
      //manage next invitation
      clearedActiveInvite.collect {
        case true => nextInviteAndRespond(charId)
      }
    } else {
      //squad is full
      previousInvites.remove(charId)
      queuedInvites.remove(charId)
      clearedActiveInvite.collect {
        case true => invites.remove(charId)
      }
      unmatchedInvites.foreach { _ =>
        subs.Publish(inviter.CharId, SquadResponse.SquadRelatedComment(s"Your request to join squad '$squadName' has been refused."))
      }
      nextInviteAndRespond(charId)
    }
    //clean up any incomplete selected invites
    pairIterator.foreach { case (_, _) =>
      subs.Publish(inviter.CharId, SquadResponse.SquadRelatedComment(s"Your request to join squad '$squadName' has been refused."))
    }
  }

  def acceptanceMessages(
                          squadLeader: Long,
                          joiningPlayer: Long,
                          joiningPlayerName: String
                        ): Unit = {
    val msg = SquadResponse.Membership(SquadResponseType.Accept, joiningPlayer, Some(squadLeader), joiningPlayerName, unk5 = false)
    subs.Publish(squadLeader, msg)
    subs.Publish(joiningPlayer, msg.copy(unk5 = true))
  }

  /* rejection */

  def handleRejection(
                       tplayer: Player,
                       rejectingPlayer: Long,
                       squadsToLeaders: List[(PlanetSideGUID, SquadFeatures)]
                     ): Unit = {
    removeInvite(rejectingPlayer)
      .foreach { invite =>
        invite.handleRejection(manager = this, tplayer, rejectingPlayer, squadsToLeaders)
        invite
      }
    nextInviteAndRespond(rejectingPlayer)
  }

  def tryChainRejection(
                         inviter: Player,
                         charId: Long,
                         list: List[Long],
                         features: SquadFeatures): Unit = {
    //handle queued invites
    lazy val squadName = features.Squad.Task
    val unmatchedInvites = queuedInvites
      .getOrElse(charId, Nil)
      .filter {
        case invite @ RequestToJoinSquadRole(invitee, _, _)
          if list.contains(invitee.CharId) && !features.Squad.Leader.Name.equals(invitee.Name) =>
          invite.doRejection(manager = this, inviter, charId)
          subs.Publish(charId, SquadResponse.SquadRelatedComment(s"Your request to join squad '$squadName' has been refused."))
          false
        case invite @ IndirectInvite(invitee, _)
          if list.contains(invitee.CharId) && !features.Squad.Leader.Name.equals(invitee.Name) =>
          invite.doRejection(manager = this, inviter, charId)
          subs.Publish(charId, SquadResponse.SquadRelatedComment(s"Your request to join squad '$squadName' has been refused."))
          false
        case _ =>
          true
      }
    queuedInvites.put(charId, unmatchedInvites)
    //handle active invite
    invites
      .get(charId)
      .collect {
        case invite @ RequestToJoinSquadRole(player, features, _)
          if list.contains(player.CharId) && !features.Squad.Leader.Name.equals(player.Name) =>
          invite.doRejection(manager = this, inviter, charId)
          subs.Publish(charId, SquadResponse.SquadRelatedComment(s"Your request to join squad '$squadName' has been refused."))
          invites.remove(charId)
          nextInviteAndRespond(charId)
        case invite @ IndirectInvite(player, features)
          if list.contains(player.CharId) && !features.Squad.Leader.Name.equals(player.Name) =>
          invite.doRejection(manager = this, inviter, charId)
          subs.Publish(charId, SquadResponse.SquadRelatedComment(s"Your request to join squad '$squadName' has been refused."))
          invites.remove(charId)
          nextInviteAndRespond(charId)
        case _ => ()
      }
  }

  def tryChainRejectionAll(charId: Long, features: SquadFeatures): Unit = {
    val comment = s"Your request to join squad '${features.Squad.Task}' has been refused."
    cleanUpAllInvitesForSquad(features.Squad.GUID)
      .map(_._1)
      .filterNot(_ == charId)
      .foreach { refusedId =>
        subs.Publish(refusedId, SquadResponse.SquadRelatedComment(comment))
      }
  }

  def rejectionMessage(rejectingPlayer: Long): Unit = {
    subs.Publish(
      rejectingPlayer,
      SquadResponse.Membership(SquadResponseType.Reject, rejectingPlayer, Some(rejectingPlayer), "", unk5 = true)
    )
  }

  def rejectionMessages(
                         rejectingPlayer: Long,
                         leaderCharId: Long,
                         name: String
                       ): Unit = {
    subs.Publish(
      rejectingPlayer,
      SquadResponse.Membership(SquadResponseType.Reject, rejectingPlayer, Some(leaderCharId), "", unk5 = true)
    )
    subs.Publish(
      leaderCharId,
      SquadResponse.Membership(SquadResponseType.Reject, leaderCharId, Some(rejectingPlayer), name, unk5 = false)
    )
  }

  /* other special actions */

  def handleDisbanding(features: SquadFeatures): Unit = {
    cleanUpAllInvitesForSquad(features.Squad.GUID)
  }

  def handleCancelling(
                        cancellingPlayer: Long,
                        player: Player,
                        featureOpt: Option[SquadFeatures]
                      ): Unit = {
    featureOpt
      .collect {
        case features if features.SearchForRole.contains(-1L) =>
          //cancel proximity invites
          features.SearchForRole = None
          features.ProxyInvites = Nil
          val queuedButCancelled = cleanUpQueuedProximityInvitesForPlayer(cancellingPlayer)
          val activeButCancelled = cleanUpActiveProximityInvitesForPlayer(cancellingPlayer)
          activeButCancelled.collect { case (id, invites) =>
            invites.foreach(_.handleCancel(manager = this, player, id))
          }
          if (queuedButCancelled.nonEmpty || activeButCancelled.nonEmpty) {
            subs.Publish(
              cancellingPlayer,
              SquadResponse.SquadRelatedComment("You have cancelled proximity invitations for your squad recruitment.")
            )
          }
          true
        case features if features.SearchForRole.nonEmpty =>
          //cancel search for role
          cancelSelectRoleForYourself(player, features)
          subs.Publish(
            cancellingPlayer,
            SquadResponse.SquadRelatedComment("You have cancelled search for role invitations for your squad recruitment.")
          )
          true
      }
      .orElse {
        //todo cancel any request to join squad role or invitation to create a squad
        None
      }
      .orElse {
        //we have nothing special to cancel; search everything and see what we have dipped our feet in
        val queuedButCancelled = cleanUpQueuedInvitesForPlayer(cancellingPlayer)
        val activeButCancelled = cleanUpActiveInvitesForPlayer(cancellingPlayer)
        activeButCancelled.collect { case (id, invites) =>
          invites.foreach(_.handleCancel(manager = this, player, id))
        }
        if (queuedButCancelled.nonEmpty || activeButCancelled.nonEmpty) {
          subs.Publish(
            cancellingPlayer,
            SquadResponse.SquadRelatedComment("You have cancelled some invitations and/or squad requests.")
          )
        }
        None
      }
  }

  def handleClosingSquad(features: SquadFeatures): Unit = {
    cleanUpAllInvitesForSquad(features.Squad.GUID)
  }

  def handleCleanup(charId: Long): Unit = {
    cleanUpAllInvitesForPlayer(charId)
  }

  def handleLeave(charId: Long): Unit = {
    refusedPlayers.remove(charId)
    cleanUpAllInvitesForPlayer(charId)
  }

  /* other special actions, promotion */

  def handlePromotion(
                       sponsoringPlayer: Long,
                       promotedPlayer: Long,
                     ): Unit = {
    shiftInvitesToPromotedSquadLeader(sponsoringPlayer, promotedPlayer)
  }

  def shiftInvitesToPromotedSquadLeader(
                                         sponsoringPlayer: Long,
                                         promotedPlayer: Long
                                       ): Unit = {
    val leaderInvite        = invites.remove(sponsoringPlayer)
    val leaderQueuedInvites = queuedInvites.remove(sponsoringPlayer).toList.flatten
    val (invitesToConvert, invitesToAppend) = (invites.remove(promotedPlayer).orElse(previousInvites.get(promotedPlayer)), leaderInvite) match {
      case (Some(activePromotedInvite), Some(outLeaderInvite)) =>
        //the promoted player has an active invite; queue these
        val promotedQueuedInvites = queuedInvites.remove(promotedPlayer).toList.flatten
        nextInvite(promotedPlayer)
        nextInvite(sponsoringPlayer)
        (activePromotedInvite +: (outLeaderInvite +: leaderQueuedInvites), promotedQueuedInvites)

      case (Some(activePromotedInvite), None) =>
        //the promoted player has an active invite; queue these
        val promotedQueuedInvites = queuedInvites.remove(promotedPlayer).toList.flatten
        nextInvite(promotedPlayer)
        (activePromotedInvite :: leaderQueuedInvites, promotedQueuedInvites)

      case (None, Some(outLeaderInvite)) =>
        //no active invite for the promoted player, but the leader had an active invite; trade the queued invites
        nextInvite(sponsoringPlayer)
        (outLeaderInvite +: leaderQueuedInvites, queuedInvites.remove(promotedPlayer).toList.flatten)

      case (None, None) =>
        //no active invites for anyone; assign the first queued invite from the promoting player, if available, and queue the rest
        (leaderQueuedInvites, queuedInvites.remove(promotedPlayer).toList.flatten)
    }
    //move over promoted invites
    invitesToConvert ++ invitesToAppend match {
      case Nil => ()
      case x :: Nil =>
        addInviteAndRespond(promotedPlayer, x, x.inviterCharId, x.inviterName)
      case x :: xs =>
        addInviteAndRespond(promotedPlayer, x, x.inviterCharId, x.inviterName)
        queuedInvites.put(promotedPlayer, xs)
    }
  }

  /* squad definition features */

  def autoApproveInvitationRequests(
                                     charId: Long,
                                     features: SquadFeatures
                                   ): Unit = {
    //allowed auto-approval - resolve the requests (only)
    val (requests, others) =
      (invites.get(charId) match {
        case Some(invite) => invite +: queuedInvites.getOrElse(charId, Nil)
        case None => queuedInvites.getOrElse(charId, Nil)
      })
        .partition({ case _: RequestToJoinSquadRole => true; case _ => false })
    invites.remove(charId)
    queuedInvites.remove(charId)
    previousInvites.remove(charId)
    //RequestToJoinSquadRole invitations that still have to be handled
    val squad = features.Squad
    var remainingRequests = requests.collect {
      case request: RequestToJoinSquadRole => (request, request.requestee)
    }
    var unfulfilled = List[Player]()
    //give roles to people who requested specific positions
    (1 to 9).foreach { position =>
      val (discovered, remainder) = remainingRequests.partition {
        case (request: RequestToJoinSquadRole, _) => request.position == position
        case _                         => false
      }
      unfulfilled ++= (discovered
        .find { case (_, player) => joinSquad(player, features, position) } match {
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
        case invite: InvitationToCreateASquad => invite.futureSquadLeader
        case invite: IndirectInvite    => invite.originalRequester
      }
        .distinctBy { _.CharId }
    (1 to 9).foreach { position =>
      if (squad.Availability(position)) {
        otherInvites.zipWithIndex.find { case (invitedPlayer, _) =>
          joinSquad(invitedPlayer, features, position)
        } match {
          case Some((_, index)) =>
            otherInvites = otherInvites.take(index) ++ otherInvites.drop(index+1)
          case None => ()
        }
      }
    }
    //cleanup searches by squad leader
    features.SearchForRole match {
      case Some(-1) => cleanUpAllProximityInvitesForPlayer(charId)
      case Some(_)  => cancelFind(Some(features))
      case None => ()
    }
  }

  def findLfsSoldiersForRole(
                              tplayer: Player,
                              features: SquadFeatures,
                              position: Int
                            ): List[(Long, List[Invitation])] = {
    val squad = features.Squad
    val sguid = squad.GUID
    val list = features
      .SearchForRole
      .collect {
      case -1 =>
        //a proximity invitation has not yet cleared; nothing will be gained by trying to invite for a specific role
        log.debug("FindLfsSoldiersForRole: waiting for proximity invitations to clear")
        None
      case pos if pos == position =>
        //already recruiting for this specific position in the squad? do nothing
        log.debug("FindLfsSoldiersForRole: already recruiting for this position; client-server mismatch?")
        None
      case pos =>
        //some other role is undergoing recruitment; cancel and redirect efforts for new position
        val comment = s"An invitation to join squad '${features.Squad.Task}' has been rescinded."
        features.SearchForRole = None
        val retiredQueuedInvitations = cleanUpQueuedInvitesForSquadAndPosition(features.Squad.GUID, pos)
        val retiredInvites = invites.collect { case (id, invite) if invite.appliesToSquadAndPosition(sguid, pos) => (id, List(invite)) }
        retiredInvites.foreach { case (id, _) =>
          invites.remove(id)
          nextInviteAndRespond(id)
        }
        SquadInvitationManager.moveListElementsToMap(retiredQueuedInvitations, retiredInvites)
        retiredInvites.foreach { case (id, _) =>
          subs.Publish(id, SquadResponse.SquadRelatedComment(comment))
        }
        Some(retiredInvites.toList)
      }
      .flatten
      .getOrElse(Nil)
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
    val faction = squad.Faction
    findSoldiersWithinScopeAndInvite(
      squad.Leader,
      features,
      position,
      tplayer.Zone.Players.filter(_.faction == faction),
      Nil,
      lookingForSquadRoleEnvelope
    ).collect { id =>
      features.ProxyInvites = List(id)
      features.SearchForRole = position
    }
    list
  }

  def cancelFind(lSquadOpt: Option[SquadFeatures]): Unit = {
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
                removeInvite(charId)
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
          case _ => ()
        }
      case _ => ()
    }
  }

  /* the following action can be performed by an unaffiliated player */
  def selectRoleForYourselfAsInvite(
                                     tplayer: Player,
                                     features: SquadFeatures,
                                     position: Int
                                   ): Unit = {
    //not a member of any squad, but we might become a member of this one
    val squad = features.Squad
    if (squad.isAvailable(position, tplayer.avatar.certifications)) {
      //we could join directly but we need permission from the squad leader first
      if (features.AutoApproveInvitationRequests) {
        invitations.RequestToJoinSquadRole(tplayer, features, position).handleAcceptance(manager = this, tplayer, squad.Leader.CharId, None)
      } else {
        //circumvent tests in AddInviteAndRespond
        val charId = squad.Leader.CharId
        val requestRole = invitations.RequestToJoinSquadRole(tplayer, features, position)
        if (addInvite(charId, requestRole).contains(requestRole)) {
          requestRole.handleInvitation(indirectInviteResp)(
            manager = this,
            charId,
            invitingPlayer = 0L, //we ourselves technically are ...
            tplayer.Name
          )
        }
      }
    }
  }

  /* the following action can be performed by anyone who has tried to join a squad */
  def cancelSelectRoleForYourself(
                                   tplayer: Player,
                                   features: SquadFeatures
                                 ): Unit = {
    val cancellingPlayer = tplayer.CharId
    //assumption: a player who is cancelling will rarely end up with their invite queued
    val squad = features.Squad
    val leaderCharId = squad.Leader.CharId
    //clean up any active RequestToJoinSquadRole invite entry where we are the player who wants to join the leader's squad
    ((invites.get(leaderCharId) match {
      case out @ Some(entry)
        if entry.isInstanceOf[RequestToJoinSquadRole] &&
          entry.asInstanceOf[RequestToJoinSquadRole].requestee.CharId == cancellingPlayer =>
        out
      case _ =>
        None
    }) match {
      case Some(entry: RequestToJoinSquadRole) =>
        removeInvite(leaderCharId)
        subs.Publish(
          leaderCharId,
          SquadResponse.Membership(SquadResponseType.Cancel, cancellingPlayer, None, entry.requestee.Name, unk5 = false )
        )
        nextInviteAndRespond(leaderCharId)
        Some(true)
      case _ =>
        None
    }).orElse(
      //look for a queued RequestToJoinSquadRole entry where we are the player who wants to join the leader's squad
      (queuedInvites.get(leaderCharId) match {
        case Some(_list) =>
          (
            _list,
            _list.indexWhere { entry =>
              entry.isInstanceOf[RequestToJoinSquadRole] &&
                entry.asInstanceOf[RequestToJoinSquadRole].requestee.CharId == cancellingPlayer
            }
          )
        case None =>
          (Nil, -1)
      }) match {
        case (_, -1) =>
          None //no change
        case (list, _) if list.size == 1 =>
          val entry = list.head.asInstanceOf[RequestToJoinSquadRole]
          subs.Publish(
            leaderCharId,
            SquadResponse.Membership(SquadResponseType.Cancel, cancellingPlayer, None, entry.requestee.Name, unk5 = false)
          )
          queuedInvites.remove(leaderCharId)
          Some(true)
        case (list, index) =>
          val entry = list(index).asInstanceOf[RequestToJoinSquadRole]
          subs.Publish(
            leaderCharId,
            SquadResponse.Membership(SquadResponseType.Cancel, cancellingPlayer, None, entry.requestee.Name, unk5 = false)
          )
          queuedInvites.put(leaderCharId, list.take(index) ++ list.drop(index + 1))
          Some(true)
      }
    )
  }

  /* squad interaction */

  def askToCreateANewSquad(invitingPlayer: Player): Future[Any] = {
    //originally, we were invited by someone into a new squad they would form
    //generate a new squad, with invitingPlayer as the leader
    ask(parent, SquadService.PerformStartSquad(invitingPlayer))
  }

  def notLimitedByEnrollmentInSquad(squadOpt: Option[SquadFeatures], charId: Long): Boolean = {
    squadOpt match {
      case Some(features) if features.Squad.Membership.exists { _.CharId == charId } =>
        ensureEmptySquad(features)
      case Some(_) =>
        false
      case None =>
        true
    }
  }

  /**
    * Determine whether a player is sufficiently unemployed
    * and has no grand delusions of being a squad leader.
    * @see `CloseSquad`
    * @param features an optional squad
    * @return `true`, if the target player possesses no squad or the squad is nonexistent;
    *         `false`, otherwise
    */
  def ensureEmptySquad(features: Option[SquadFeatures]): Boolean = {
    features match {
      case Some(squad) => ensureEmptySquad(squad)
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
  def ensureEmptySquad(features: SquadFeatures): Boolean = {
    val ensuredEmpty = features.Squad.Size <= 1
    if (ensuredEmpty) {
      cleanUpAllInvitesForSquad(features.Squad.GUID)
    }
    ensuredEmpty
  }

  /**
    * Behaviors and exchanges necessary to complete the fulfilled recruitment process for the squad role.<br>
    * <br>
    * This operation is fairly safe to call whenever a player is to be inducted into a squad.
    * The aforementioned player must have a callback retained in `subs.UserEvents`
    * and conditions imposed by both the role and the player must be satisfied.
    * @see `CleanUpAllInvitesForPlayer`
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
  def joinSquad(player: Player, features: SquadFeatures, position: Int): Boolean = {
    cleanUpAllInvitesForPlayer(player.CharId)
    parent ! SquadService.PerformJoinSquad(player, features, position)
    true
  }

  /* refusal and allowance */

  /**
    * This player has been refused to join squads by these players, or to form squads with these players.
    * @param charId the player who refused other players
    * @return the list of other players who have refused this player
    */
  def refused(charId: Long): List[Long] = refusedPlayers.getOrElse(charId, Nil)

  /**
    * This player has been refused to join squads by this squad leaders, or to form squads with this other player.
    * @param charId the player who is being refused
    * @param refusedCharId the player who refused
    * @return the list of other players who have refused this player
    */
  def refused(charId: Long, refusedCharId: Long): List[Long] = {
    if (charId != refusedCharId) {
      refused(charId, List(refusedCharId))
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
  def refused(charId: Long, list: List[Long]): List[Long] = {
    refusedPlayers.get(charId) match {
      case Some(refusedList) =>
        refusedPlayers.put(charId, list ++ refusedList)
        refused(charId)
      case None =>
        Nil
    }
  }

  /**
    * This player was previously refused to join squads by this squad leaders, or to form squads with this other player.
    * They are now allowed.
    * @param charId the player who is being refused
    * @param permittedCharId the player who was previously refused
    * @return the list of other players who have refused this player
    */
  def allowed(charId: Long, permittedCharId: Long): List[Long] = {
    if (charId != permittedCharId) {
      allowed(charId, List(permittedCharId))
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
  def allowed(charId: Long, list: List[Long]): List[Long] = {
    refusedPlayers.get(charId) match {
      case Some(refusedList) =>
        refusedPlayers.put(charId, refusedList.filterNot(list.contains))
        refused(charId)
      case None =>
        Nil
    }
  }

  /* enqueue invite */

  /**
   * Enqueue a newly-submitted invitation object
   * either as the active position or into the inactive positions
   * and dispatch a response for any invitation object that is discovered.
   * Implementation of a workflow.
   * @see `AddInvite`
   * @see `indirectInviteResp`
   * @param invitedPlayer  the unique character identifier for the player being invited;
   *                       in actuality, represents the player who will address the invitation object
   * @param targetInvite   a comparison invitation object
   * @param invitingPlayer the unique character identifier for the player who invited the former
   * @param name           a name to be used in message composition
   */
  def addInviteAndRespond(
                           invitedPlayer: Long,
                           targetInvite: Invitation,
                           invitingPlayer: Long,
                           name: String,
                           autoApprove: Boolean = false
                         ): Unit = {
    if (targetInvite.canBeAutoApproved && autoApprove) {
      targetInvite.handleAcceptance(manager = this, targetInvite.getPlayer, invitingPlayer, None)
    } else if (addInvite(invitedPlayer, targetInvite).contains(targetInvite)) {
      targetInvite.handleInvitation(indirectInviteResp)(
        manager = this,
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
   * @see `AddInvite`
   * @see `altIndirectInviteResp`
   * @param targetInvite   a comparison invitation object
   * @param invitedPlayer  the unique character identifier for the player being invited
   * @param invitingPlayer the unique character identifier for the player who invited the former
   * @param name           a name to be used in message composition
   */
  def altAddInviteAndRespond(
                              invitedPlayer: Long,
                              targetInvite: Invitation,
                              invitingPlayer: Long,
                              name: String,
                              autoApprove: Boolean = false
                            ): Unit = {
    if (targetInvite.canBeAutoApproved && autoApprove) {
      targetInvite.handleAcceptance(manager = this, targetInvite.getPlayer, invitingPlayer, None)
    } else if (addInvite(invitedPlayer, targetInvite).contains(targetInvite)) {
      targetInvite.handleInvitation(altIndirectInviteResp)(
        manager = this,
        invitedPlayer,
        invitingPlayer,
        name
      )
    }
  }

  /**
   * Component method used for the response behavior for processing the invitation object as an `IndirectInvite` object.
   * @see `SquadInvitationManager.HandleRequestRole`
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
    SquadInvitationManager.handleRequestRole(manager = this, player, invite)
  }

  /**
   * Component method used for the response behavior for processing the invitation object as an `IndirectInvite` object.
   * @see `SquadInvitationManager.HandleRequestRole`
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
      SquadResponse.Membership(SquadResponseType.Accept, invitingPlayer, Some(invitedPlayer), player.Name, unk5 = false)
    )
    SquadInvitationManager.handleRequestRole(manager = this, player, invite)
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
  def addInvite(invitedPlayer: Long, invite: Invitation): Option[Invitation] = {
    invites.get(invitedPlayer).orElse(previousInvites.get(invitedPlayer)) match {
      case Some(_bid) =>
        //the new invite may not interact with the active invite; add to queued invites
        queuedInvites.get(invitedPlayer) match {
          case Some(bidList) =>
            //ensure that new invite does not interact with the queue's invites by invitingPlayer info
            val inviteInviterCharId = invite.inviterCharId
            if (
              _bid.inviterCharId != inviteInviterCharId && !bidList.exists { eachBid =>
                eachBid.inviterCharId == inviteInviterCharId
              }
            ) {
              val restoredInvites = invite match {
                case _: RequestToJoinSquadRole =>
                  //RequestToJoinSquadRole is to be expedited
                  val (normals, others) = bidList.partition(_.isInstanceOf[RequestToJoinSquadRole])
                  (normals :+ invite) ++ others
                case _ =>
                  bidList :+ invite
              }
              queuedInvites.put(invitedPlayer, restoredInvites)
              Some(_bid)
            } else {
              None
            }
          case None =>
            if (_bid.inviterCharId != invite.inviterCharId) {
              queuedInvites.put(invitedPlayer, List(invite))
              Some(_bid)
            } else {
              None
            }
        }

      case None =>
        invites.put(invitedPlayer, invite)
        Some(invite)
    }
  }

  /**
   * Resolve an invitation to a general, not guaranteed, position in someone else's squad.<br>
   * <br>
   * Originally, the instigating type of invitation object was a "`InvitationToJoinSquad`"
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
   * @see `InvitationToJoinSquad`
   * @param features the squad
   * @param invitedPlayer the unique character identifier for the player being invited
   * @param invitingPlayer the unique character identifier for the player who invited the former
   * @param recruit the player being invited
   * @return the squad object and a role position index, if properly invited;
   *         `None`, otherwise
   */
  def handleVacancyInvite(
                           features: SquadFeatures,
                           invitedPlayer: Long,
                           invitingPlayer: Long,
                           recruit: Player
                         ): Option[(SquadFeatures, Int)] = {
    //accepted an invitation to join an existing squad
    val squad = features.Squad
    squad
      .Membership
      .zipWithIndex
      .find { case (_, index) => squad.isAvailable(index, recruit.avatar.certifications) } match {
      case Some((_, line)) =>
        //position in squad found
        if (!features.AutoApproveInvitationRequests && squad.Leader.CharId != invitingPlayer) {
          //the inviting player was not the squad leader and this decision should be bounced off the squad leader
          altAddInviteAndRespond(
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
  def nextInvite(invitedPlayer: Long): Option[Invitation] = {
    previousInvites.remove(invitedPlayer)
    invites.get(invitedPlayer) match {
      case None =>
        queuedInvites.get(invitedPlayer) match {
          case Some(list) =>
            list match {
              case Nil =>
                None
              case x :: Nil =>
                invites.put(invitedPlayer, x)
                queuedInvites.remove(invitedPlayer)
                Some(x)
              case x :: xs =>
                invites.put(invitedPlayer, x)
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
  def nextInviteAndRespond(invitedPlayer: Long): Unit = {
    nextInvite(invitedPlayer)
      .collect { invite =>
        respondToInvite(invitedPlayer, invite)
        invite
      }
  }

  /**
   * Compose the response to an invitation.
   * Use standard handling methods for `IndirectInvite` invitation envelops.
   * @param invitedPlayer the unique character identifier for the player being invited;
   *                      in actuality, represents the player who will address the invitation object
   * @param invite the invitation envelope used to recover information about the action being taken
   */
  def respondToInvite(invitedPlayer: Long, invite: Invitation): Unit = {
    invite.handleInvitation(indirectInviteResp)(
      manager = this,
      invitedPlayer,
      invite.inviterCharId,
      invite.inviterName
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
  def removeInvite(invitedPlayer: Long): Option[Invitation] = {
    invites.remove(invitedPlayer) match {
      case out @ Some(invite) =>
        previousInvites.put(invitedPlayer, invite)
        out
      case None =>
        None
    }
  }

  /* search */

  def findSoldiersWithinScopeAndInvite(
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
    val deniedAndExcluded = (features.DeniedPlayers() ++ excluded) :+ squadLeader
    val requirementsToMeet = squad.Membership(position).Requirements
    //find a player who is of the same faction as the squad, is LFS, and is eligible for the squad position
    scope
      .find { avatar =>
        val charId = avatar.id
        avatar.lookingForSquad &&
          avatar.faction == faction &&
          charId != squadLeader &&
          charId != invitingPlayerCharId &&
          !deniedAndExcluded.contains(charId) &&
          !refusedPlayers(charId).contains(squadLeader) &&
          requirementsToMeet.intersect(avatar.certifications) == requirementsToMeet
      }
      .collect { invitedPlayer =>
        //add invitation for position in squad
        val id = invitedPlayer.id
        addInviteAndRespond(
          id,
          invitationEnvelopFunc(invitingPlayer, features, position),
          invitingPlayerCharId,
          invitingPlayerName
        )
        id
    }
  }

  /* invite clean-up */

  private def removeActiveInvites(invitationFilteringRule: (Long, Invitation) => Boolean): List[(Long, List[Invitation])] = {
    invites
      .collect {
        case out @ (id, invite) if invitationFilteringRule(id, invite) => out
      }
      .map { case (id, invite) =>
        removeInvite(id)
        nextInviteAndRespond(id)
        (id, List(invite))
      }
      .toList
  }

  /**
   * Remove all active invitation objects that are related to the particular player.
   * Specifically used to safely disarm obsolete invitation objects by specific criteria.
   * Affects the active invitation list and any squads related to that player.
   * @param charId the player's unique identifier number
   * @return a list of the removed inactive invitation objects
   */
  def cleanUpActiveInvitesForPlayer(charId: Long): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpInvites(removeActiveInvitesForPlayer(charId), List(charId))
  }

  /**
   * Remove all active invitation objects that are related to the particular player.
   * Specifically used to safely disarm obsolete invitation objects by specific criteria.
   * Affects the active invitation list.
   * @param charId the player's unique identifier number
   * @return a list of the removed inactive invitation objects
   */
  private def removeActiveInvitesForPlayer(charId: Long): List[(Long, List[Invitation])] = {
    removeActiveInvites({ (id: Long, invite: Invitation) => id == charId || invite.appliesToPlayer(charId) })
  }

  /**
   * Remove all inactive invitation objects that are related to the particular player.
   * Specifically used to safely disarm obsolete invitation objects by specific criteria.
   * Affects the queued invitation list and any squads related to that player.
   * @param charId the player's unique identifier number
   * @return a list of the removed inactive invitation objects
   */
  def cleanUpQueuedInvitesForPlayer(charId: Long): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpInvites(removeQueuedInvitesForPlayer(charId), List(charId))
  }

  /**
   * Remove all inactive invitation objects that are related to the particular player.
   * Specifically used to safely disarm obsolete invitation objects by specific criteria.
   * Affects the inactive invitation list.
   * @param charId the player's unique identifier number
   * @return a list of the removed inactive invitation objects
   */
  private def removeQueuedInvitesForPlayer(charId: Long): List[(Long, List[Invitation])] = {
    SquadInvitationManager.removeQueuedInvites(
      queuedInvites,
      { invite: Invitation => invite.appliesToPlayer(charId) }
    )
  }

  /**
   * Remove all invitation objects that are related to the particular player.
   * Specifically used to safely disarm obsolete invitation objects by specific criteria.
   * Affects the active invitation list and the inactive invitation list and any squads related to that player.
   * @param charId the player's unique identifier number
   * @return a list of the removed inactive invitation objects
   */
  def cleanUpAllInvitesForPlayer(charId: Long): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpAllInvites(
      removeQueuedInvitesForPlayer(charId),
      removeActiveInvitesForPlayer(charId)
    )
  }

  /**
   * Remove all active invitation objects that are related to the particular squad.
   * Specifically used to safely disarm obsolete invitation objects by specific criteria.
   * @param guid squad identifier
   */
  def cleanUpActiveInvitesForSquad(guid: PlanetSideGUID): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpInvites(removeActiveInvitesForSquad(guid), Nil)
  }

  private def removeActiveInvitesForSquad(guid: PlanetSideGUID): List[(Long, List[Invitation])] = {
    removeActiveInvites({ (_: Long, invite: Invitation) => invite.appliesToSquad(guid) })
  }

  /**
   * Remove all queued invitation objects that are related to the particular squad.
   * Specifically used to safely disarm obsolete invitation objects by specific criteria.
   * @param guid squad identifier
   */
  def cleanUpQueuedInvitesForSquad(guid: PlanetSideGUID): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpInvites(removeQueuedInvitesForSquad(guid), Nil)
  }

  private def removeQueuedInvitesForSquad(guid: PlanetSideGUID): List[(Long, List[Invitation])] = {
    SquadInvitationManager.removeQueuedInvites(
      queuedInvites,
      { invite: Invitation => invite.appliesToSquad(guid) }
    )
  }

  /**
   * Remove all invitation objects that are related to the particular squad.
   * Specifically used to safely disarm obsolete invitation objects by specific criteria.
   * Affects all invitation object types and all data structures that deal with the squad.
   * @param guid squad identifier
   */
  def cleanUpAllInvitesForSquad(guid: PlanetSideGUID): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpAllInvites(
      removeQueuedInvitesForSquad(guid),
      removeActiveInvitesForSquad(guid)
    )
  }

  /**
   * Remove all active invitation objects
   * that are related to the particular squad and the particular role in the squad.
   * Specifically used to safely disarm obsolete invitation objects related to the specific criteria.
   * Affects only certain invitation object types
   * including "player requesting role" and "leader requesting recruiting role".
   * @param guid the squad identifier
   * @param position the role position index
   * @return the character ids of all players whose invites were removed
   */
  def cleanUpActiveInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpInvites(removeActiveInvitesForSquadAndPosition(guid, position), Nil)
  }

  private def removeActiveInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): List[(Long, List[Invitation])] = {
    removeActiveInvites({ (_: Long, invite: Invitation) => invite.appliesToSquadAndPosition(guid, position) })
  }

  /**
   * Remove all inactive invitation objects that are related to the particular squad and the particular role in the squad.
   * Specifically used to safely disarm obsolete invitation objects by specific criteria.
   * Affects only certain invitation object types.
   * @param guid the squad identifier
   * @param position the role position index
   * @return the character ids of all players whose invites were removed
   */
  def cleanUpQueuedInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpInvites(removeQueuedInvitesForSquadAndPosition(guid, position), Nil)
  }

  private def removeQueuedInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): List[(Long, List[Invitation])] = {
    SquadInvitationManager.removeQueuedInvites(
      queuedInvites,
      { invite: Invitation => invite.appliesToSquadAndPosition(guid, position) }
    )
  }

  /**
   * Remove all invitation objects
   * that are related to the particular squad and the particular role in the squad.
   * Specifically used to safely disarm obsolete invitation objects related to the specific criteria.
   * Affects only certain invitation object types
   * including "player requesting role" and "leader requesting recruiting role".
   * @see `RemoveActiveInvitesForSquadAndPosition`
   * @see `RemoveQueuedInvitesForSquadAndPosition`
   * @param guid squad identifier
   * @param position the role position index
   */
  def cleanUpAllInvitesForSquadAndPosition(guid: PlanetSideGUID, position: Int): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpAllInvites(
      removeQueuedInvitesForSquadAndPosition(guid, position),
      removeActiveInvitesForSquadAndPosition(guid, position)
    )
  }

  def cleanUpActiveProximityInvitesForPlayer(charId: Long): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpInvites(removeActiveProximityInvitesForPlayer(charId), List(charId))
  }

  private def removeActiveProximityInvitesForPlayer(charId: Long): List[(Long, List[Invitation])] = {
    removeActiveInvites({
      (_: Long, invite: Invitation) => invite match {
        case invite: ProximityInvite => invite.appliesToPlayer(charId)
        case _ => false
      }
    })
  }

  def cleanUpQueuedProximityInvitesForPlayer(charId: Long): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpInvites(removeQueuedProximityInvitesForPlayer(charId), Nil)
  }

  private def removeQueuedProximityInvitesForPlayer(charId: Long): List[(Long, List[Invitation])] = {
    SquadInvitationManager.removeQueuedInvites(
      queuedInvites,
      {
        case invite: ProximityInvite => invite.appliesToPlayer(charId)
        case _ => false
      }
    )
  }

  /**
    * Remove all active and inactive proximity squad invites.
    * This is related to recruitment from the perspective of the recruiter.
    * @param charId the player
    */
  def cleanUpAllProximityInvitesForPlayer(charId: Long): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpAllInvites(
      removeQueuedProximityInvitesForPlayer(charId),
      removeActiveProximityInvitesForPlayer(charId)
    )
  }

  private def removeActiveProximityInvitesForSquad(guid: PlanetSideGUID): List[(Long, List[Invitation])] = {
    removeActiveInvites({
      (_: Long, invite: Invitation) => invite match {
        case invite: ProximityInvite => invite.appliesToSquad(guid)
        case _ => false
      }
    })
  }

  private def removeQueuedProximityInvitesForSquad(guid: PlanetSideGUID): List[(Long, List[Invitation])] = {
    SquadInvitationManager.removeQueuedInvites(
      queuedInvites,
      {
        case invite: ProximityInvite => invite.appliesToSquad(guid)
        case _ => false
      }
    )
  }

  /**
    * Remove all active and inactive proximity squad invites for a specific squad.
    * @param guid squad identifier
    */
  def cleanUpAllProximityInvitesForSquad(guid: PlanetSideGUID): List[(Long, List[Invitation])] = {
    SquadInvitationManager.cleanUpAllInvites(
      removeQueuedProximityInvitesForSquad(guid),
      removeActiveProximityInvitesForSquad(guid)
    )
  }

  def publish(to: Long, msg: SquadResponse.Response): Unit = {
    subs.Publish(to, msg)
  }
}

object SquadInvitationManager {
  final case class Join(charId: Long)

  final case class FinishStartSquad(features: SquadFeatures)

  /**
    * na
    * @param invitingPlayer na
    * @param features na
    * @param position na
    * @return na
    */
  private def proximityEnvelope(
                                 invitingPlayer: Member,
                                 features: SquadFeatures,
                                 position: Int
                               ): Invitation = {
    invitations.ProximityInvite(invitingPlayer, features, position)
  }

  /**
    * na
    * @param invitingPlayer na
    * @param features na
    * @param position na
    * @return na
    */
  private def lookingForSquadRoleEnvelope(
                                           invitingPlayer: Member,
                                           features: SquadFeatures,
                                           position: Int
                                         ): Invitation = {
    invitations.LookingForSquadRoleInvite(invitingPlayer, features, position)
  }

  /**
   * Overloaded entry point to functionality for handling indirection
   * for handling one player requesting a specific squad role
   * or when messaging the squad leader about an invite.
   * @param player the player who wants to join the squad
   * @param bid    a specific kind of `Invitation` object
   * @return `true`, if the player is not denied the possibility of joining the squad;
   *         `false`, otherwise, of it the squad does not exist
   */
  def handleRequestRole(manager: SquadInvitationManager, player: Player, bid: Invitation): Boolean = {
    bid.getOptionalSquad.exists(handleRequestRole(manager, player, _, bid))
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
  def handleRequestRole(manager: SquadInvitationManager, player: Player, features: SquadFeatures, bid: Invitation): Boolean = {
    val leaderCharId = features.Squad.Leader.CharId
    manager.publish(leaderCharId, SquadResponse.WantsSquadPosition(leaderCharId, player.Name))
    true
  }

  def canEnrollInSquad(features: SquadFeatures, charId: Long): Boolean = {
    !features.Squad.Membership.exists { _.CharId == charId }
  }

  def notLeaderOfThisSquad(squadsToLeaders: List[(PlanetSideGUID, SquadFeatures)], guid: PlanetSideGUID, charId: Long): Boolean = {
    squadsToLeaders.find { case (squadGuid, _) => squadGuid == guid } match {
      case Some((_, features)) => features.Squad.Leader.CharId != charId
      case None                => false
    }
  }

  private def moveListElementsToMap[T](fromList: List[(Long, List[T])], toMap: mutable.LongMap[List[T]]): Unit = {
    fromList.foreach { case (id, listElements) =>
      toMap.get(id) match {
        case None =>
          toMap.put(id, listElements)
        case Some(mapElements) =>
          toMap.put(id, listElements ++ mapElements)
      }
    }
  }

  private def cleanUpSquadFeatures(removed: List[Long], features: SquadFeatures, position: Int): Unit = {
    features.ProxyInvites = features.ProxyInvites.filterNot(removed.contains)
    if (features.ProxyInvites.isEmpty) {
      features.SearchForRole = None
    }
  }

  /**
   * Remove all active invitation objects that are related to the particular player.
   * Specifically used to safely disarm obsolete invitation objects by specific criteria.
   * Affects the active invitation list and any squads related to that player.
   * @param proposedRemovalIds list of unique character identifiers to be eliminated from squad information;
   *                           if empty, the unique character identifiers from the other parameter will be used instead
   * @return a list of the removed inactive invitation objects
   */
  private def cleanUpInvites(
                              list: List[(Long, List[Invitation])],
                              proposedRemovalIds: List[Long]
                            ): List[(Long, List[Invitation])] = {
    val (idList, invites) = list.unzip
    val removalList = proposedRemovalIds
      .collectFirst(_ => proposedRemovalIds)
      .getOrElse(idList)
    invites
      .flatten
      .flatMap(_.getOptionalSquad)
      .distinctBy(_.Squad.GUID)
      .foreach(cleanUpSquadFeatures(removalList, _, position = -1))
    list
  }

  /**
   * Remove all invitation objects that are related to the particular squad.
   * Specifically used to safely disarm obsolete invitation objects by specific criteria.
   * Affects all invitation object types and all data structures that deal with the squad.
   */
  private def cleanUpAllInvites(
                                 queuedList: List[(Long, List[Invitation])],
                                 activeList: List[(Long, List[Invitation])]
                               ): List[(Long, List[Invitation])] = {
    val activeInvitesMap = mutable.LongMap.from(activeList)
    val (removalList, featureList) = {
      val (ids, inviteLists) = (activeInvitesMap ++ queuedList).unzip
      (
        ids.toList,
        inviteLists
          .flatMap { invites =>
            invites.flatMap(_.getOptionalSquad)
          }
      )
    }
    moveListElementsToMap(queuedList, activeInvitesMap)
    featureList
      .toSeq
      .distinctBy(_.Squad.GUID)
      .foreach { features =>
        cleanUpSquadFeatures(removalList, features, position = -1)
      }
    activeInvitesMap.toList
  }

  private def removeQueuedInvites(
                                   inviteMap: mutable.LongMap[List[Invitation]],
                                   partitionRule: Invitation => Boolean
                                 ): List[(Long, List[Invitation])] = {
    inviteMap
      .toList
      .flatMap {
        case (id, invites) =>
          val (found, retained) = invites.partition(partitionRule)
          if (retained.nonEmpty) {
            inviteMap.put(id, retained)
          } else {
            inviteMap.remove(id)
          }
          found.collectFirst { _ => (id, found) }
      }
  }
}
