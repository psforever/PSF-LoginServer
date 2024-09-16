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
    if (
      SquadInvitationManager.canEnrollInSquad(features, requestee.CharId) &&
      manager.joinSquad(requestee, features, position)
    ) {
      manager.acceptanceMessages(invitedPlayer, requestee.CharId, requestee.Name)
      manager.cleanUpInvitesForSquadAndPosition(features, position)
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

  def canBeAutoApproved: Boolean = true

  def getOptionalSquad: Option[SquadFeatures] = Some(features)

  def getPlayer: Player = requestee

  def appliesToPlayer(playerCharId: Long): Boolean = playerCharId == requestee.CharId

  def appliesToSquad(guid: PlanetSideGUID): Boolean = features.Squad.GUID == guid

  def appliesToSquadAndPosition(guid: PlanetSideGUID, squadPosition: Int): Boolean = appliesToSquad(guid) && position == squadPosition
}
