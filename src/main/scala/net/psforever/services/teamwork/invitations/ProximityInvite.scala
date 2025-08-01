// Copyright (c) 2024 PSForever
package net.psforever.services.teamwork.invitations

import net.psforever.objects.Player
import net.psforever.objects.teamwork.{Member, SquadFeatures}
import net.psforever.services.teamwork.{SquadInvitationManager, SquadResponse}
import net.psforever.types.{PlanetSideGUID, SquadResponseType}

import scala.annotation.unused

/**
  * Utilized in conjunction with an external queuing data structure
  * to search for and submit requests to other players
  * for the purposes of fill out unoccupied squad roles.
  * This invitation is handled by the player who would be joining the squad.
  *
  * @param squadLeader squad leader
  * @param features squad
  * @param position index of a role
  */
final case class ProximityInvite(squadLeader: Member, features: SquadFeatures, position: Int)
    extends Invitation(squadLeader.CharId, squadLeader.Name) {
  def handleInvitation(indirectInviteFunc: (IndirectInvite, Player, Long, Long, String) => Boolean)(
    manager: SquadInvitationManager,
    invitedPlayer: Long,
    invitingPlayer: Long,
    otherName: String
  ): Unit = {
    manager.publish(
      invitedPlayer,
      SquadResponse.Membership(
        SquadResponseType.Invite,
        invitedPlayer,
        Some(squadLeader.CharId),
        squadLeader.Name,
        unk5 = false
      )
    )
  }

  def handleAcceptance(
                        manager: SquadInvitationManager,
                        player: Player,
                        invitedPlayer: Long,
                        invitedPlayerSquadOpt: Option[SquadFeatures]
                      ): Unit = {
    val leaderCharId = squadLeader.CharId
    //this cleanup activity always happens
    features.ProxyInvites = features.ProxyInvites.filterNot { _ == invitedPlayer }
    if (
      manager.notLimitedByEnrollmentInSquad(invitedPlayerSquadOpt, invitedPlayer) &&
        SquadInvitationManager.canEnrollInSquad(features, invitedPlayer) &&
        manager.joinSquad(player, features, position)
    ) {
      //join this squad
      //manager.acceptanceMessages(invitingPlayer, invitedPlayer, player.Name)
      val msg = SquadResponse.Membership(SquadResponseType.Accept, invitedPlayer, Some(leaderCharId), player.Name, unk5 = false)
      manager.publish(leaderCharId, msg)
      manager.publish(invitedPlayer, msg.copy(unk5 = true))
      //clean up invitations specifically for this squad and this position
      val cleanedUpQueuedInvites = manager.cleanUpQueuedInvitesForSquadAndPosition(features.Squad.GUID, position)
      if (features.Squad.Capacity == features.Squad.Size) {
        val cleanedUpActiveInvites = manager.cleanUpActiveInvitesForSquad(features.Squad.GUID)
        cleanedUpActiveInvites.collect { case (id, invites) =>
          invites.foreach(_.handleCancel(manager, player, id))
          manager.publish(
            id,
            SquadResponse.SquadRelatedComment(s"An invitation to join a squad has ended.")
          )
        }
        (manager.cleanUpQueuedInvitesForSquad(features.Squad.GUID) ++ cleanedUpActiveInvites ++ cleanedUpQueuedInvites).collectFirst { case _ =>
          manager.publish(
            leaderCharId,
            SquadResponse.SquadRelatedComment(s"You had invitations that were cancelled due to this action.")
          )
        }
      } else if (cleanedUpQueuedInvites.nonEmpty) {
        manager.publish(
          leaderCharId,
          SquadResponse.SquadRelatedComment(s"You had invitations that were cancelled due to this action.")
        )
      }
    } else {
      //if able to attempt to accept this proximity invite, recruitment is still ongoing
      manager.reloadProximityInvite(player.Zone.Players, invitedPlayer, features, position)
    }
  }

  def handleRejection(
                       manager: SquadInvitationManager,
                       player: Player,
                       rejectingPlayer: Long,
                       @unused squadsToLeaders: List[(PlanetSideGUID, SquadFeatures)]
                     ): Unit = {
    /*if SquadInvitationManager.notLeaderOfThisSquad(squadsToLeaders, features.Squad.GUID, rejectingPlayer)*/
    //rejectingPlayer is the would-be squad member; the squad leader sent the request and was rejected
    doRejection(manager, player, rejectingPlayer)
    manager.rejectionMessage(rejectingPlayer)
    manager.publish(
      rejectingPlayer,
      SquadResponse.SquadRelatedComment(s"Your request to join squad '${features.Squad.Task}' has been refused.")
    )
  }

  def doRejection(
                   manager: SquadInvitationManager,
                   player: Player,
                   rejectingPlayer: Long
                 ): Unit = {
    manager.reloadProximityInvite(player.Zone.Players, rejectingPlayer, features, position)
  }

  def handleCancel(
                    manager: SquadInvitationManager,
                    player: Player,
                    handlingPlayer: Long
                  ): Unit = {
    val actingPlayer = player.CharId
    val leaderCharId = squadLeader.CharId
    if (actingPlayer == handlingPlayer) {
      manager.publish(
        handlingPlayer,
        SquadResponse.SquadRelatedComment(s"You have declined an offer to join a squad.")
      )
    } else if (actingPlayer == leaderCharId) {
      manager.publish(
        handlingPlayer,
        SquadResponse.SquadRelatedComment(s"The offer to join a squad has been cancelled.")
      )
    } else {
      manager.publish(
        handlingPlayer,
        SquadResponse.SquadRelatedComment(s"The offer to join into a squad is no longer valid.")
      )
    }
  }

  def canBeAutoApproved: Boolean = false

  def getOptionalSquad: Option[SquadFeatures] = Some(features)

  def getPlayer: Player = null

  def appliesToPlayer(playerCharId: Long): Boolean = playerCharId == squadLeader.CharId

  def appliesToSquad(guid: PlanetSideGUID): Boolean = features.Squad.GUID == guid

  def appliesToSquadAndPosition(guid: PlanetSideGUID, squadPosition: Int): Boolean = appliesToSquad(guid) && position == squadPosition
}
