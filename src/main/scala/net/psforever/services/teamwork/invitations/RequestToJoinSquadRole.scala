// Copyright (c) 2024 PSForever
package net.psforever.services.teamwork.invitations

import net.psforever.objects.Player
import net.psforever.objects.teamwork.SquadFeatures
import net.psforever.services.teamwork.{SquadInvitationManager, SquadResponse}
import net.psforever.types.PlanetSideGUID

import scala.annotation.unused

/**
  * Utilized when one player attempts to join an existing squad in a specific role.
  * Accessed by the joining player from the squad detail window.
  * This invitation is handled by the squad leader.
  *
  * @param requestee player who requested the role
  * @param features squad with the role
  * @param position index of the role
  */
final case class RequestToJoinSquadRole(requestee: Player, features: SquadFeatures, position: Int)
    extends Invitation(requestee.CharId, requestee.Name) {
  def handleInvitation(indirectInviteFunc: (IndirectInvite, Player, Long, Long, String) => Boolean)(
    manager: SquadInvitationManager,
    invitedPlayer: Long,
    invitingPlayer: Long,
    otherName: String
  ): Unit = {
    SquadInvitationManager.handleRequestRole(manager, requestee, bid = this)
  }

  def handleAcceptance(
                        manager: SquadInvitationManager,
                        @unused player: Player,
                        invitedPlayer: Long,
                        @unused invitedPlayerSquadOpt: Option[SquadFeatures]
                      ): Unit = {
    //player requested to join a squad's specific position
    //invitedPlayer is actually the squad leader; petitioner is the actual "invitedPlayer"
    val leaderCharId = player.CharId
    val requestingPlayer = requestee.CharId
    if (
      SquadInvitationManager.canEnrollInSquad(features, requestee.CharId) &&
      manager.joinSquad(requestee, features, position)
    ) {
      //manager.acceptanceMessages(invitedPlayer, requestee.CharId, requestee.Name)
      manager.publish(
        leaderCharId,
        SquadResponse.SquadRelatedComment(s"You have accepted ${requestee.Name}'s request to join your squad.")
      )
      manager.publish(
        requestingPlayer,
        SquadResponse.SquadRelatedComment(s"You have joined the squad '${features.Squad.Task}'.")
      )
      //clean up invitations specifically for this squad and this position
      val cleanedUpActiveInvitesForSquadAndPosition = manager.cleanUpActiveInvitesForSquadAndPosition(features.Squad.GUID, position)
      cleanedUpActiveInvitesForSquadAndPosition.collect { case (id, invites) =>
        invites.foreach(_.handleCancel(manager, player, id))
        manager.publish(
          id,
          SquadResponse.SquadRelatedComment(s"An invitation to join a squad has ended.")
        )
      }
      val cleanedUpQueuedInvites = manager.cleanUpQueuedInvitesForSquadAndPosition(features.Squad.GUID, position)
      if (features.Squad.Capacity == features.Squad.Size) {
        val cleanedUpActiveInvites = manager.cleanUpActiveInvitesForSquad(features.Squad.GUID)
        cleanedUpActiveInvites.collect { case (id, _) =>
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
      manager.publish(
        requestingPlayer,
        SquadResponse.SquadRelatedComment(s"Your invitation to squad '${features.Squad.Task}' was accepted, but failed.")
      )
      manager.publish(
        leaderCharId,
        SquadResponse.SquadRelatedComment(s"An accepted request to join your squad has failed.")
      )
    }
  }

  def handleRejection(
                       manager: SquadInvitationManager,
                       @unused player: Player,
                       rejectingPlayer: Long,
                       @unused squadsToLeaders: List[(PlanetSideGUID, SquadFeatures)]
                     ): Unit = {
    if (SquadInvitationManager.notLeaderOfThisSquad(squadsToLeaders, features.Squad.GUID, requestee.CharId)) {
      //rejected is the would-be squad member; rejectingPlayer is the squad leader who rejected the request
      doRejection(manager, player, rejectingPlayer)
      manager.rejectionMessage(rejectingPlayer)
      manager.publish(
        rejectingPlayer,
        SquadResponse.SquadRelatedComment(s"Your request to join squad '${features.Squad.Task}' has been refused.")
      )
    }
  }

  def doRejection(
                   manager: SquadInvitationManager,
                   player: Player,
                   rejectingPlayer: Long
                 ): Unit = {
    features.DeniedPlayers(requestee.CharId)
  }

  def handleCancel(
                    manager: SquadInvitationManager,
                    player: Player,
                    handlingPlayer: Long
                  ): Unit = {
    val invitingPlayer = requestee.CharId
    val invitingPlayerName = requestee.Name
    val actingPlayer = player.CharId
    val leaderCharId = features.Squad.Leader.CharId
    val leaderName = features.Squad.Leader.Name
    if (actingPlayer == handlingPlayer) {
      manager.publish(
        invitingPlayer,
        SquadResponse.SquadRelatedComment(s"You were declined admission to a squad.")
      )
    } else if (actingPlayer == invitingPlayer) {
      manager.publish(
        leaderCharId,
        SquadResponse.SquadRelatedComment(s"$invitingPlayerName has rescinded the offer to join the squad.")
      )
    } else {
      manager.publish(
        leaderCharId,
        SquadResponse.SquadRelatedComment(s"The request from $invitingPlayerName to join the squad is no longer valid.")
      )
      manager.publish(
        invitingPlayer,
        SquadResponse.SquadRelatedComment(s"The offer to $leaderName to join the squad is no longer valid.")
      )
    }
  }

  def canBeAutoApproved: Boolean = true

  def getOptionalSquad: Option[SquadFeatures] = Some(features)

  def getPlayer: Player = requestee

  def appliesToPlayer(playerCharId: Long): Boolean = playerCharId == requestee.CharId

  def appliesToSquad(guid: PlanetSideGUID): Boolean = features.Squad.GUID == guid

  def appliesToSquadAndPosition(guid: PlanetSideGUID, squadPosition: Int): Boolean = appliesToSquad(guid) && position == squadPosition
}
