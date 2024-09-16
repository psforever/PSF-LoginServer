// Copyright (c) 2024 PSForever
package net.psforever.services.teamwork.invitations

import net.psforever.objects.Player
import net.psforever.objects.teamwork.SquadFeatures
import net.psforever.services.teamwork.{SquadInvitationManager, SquadResponse}
import net.psforever.types.{PlanetSideGUID, SquadResponseType}

import scala.annotation.unused

/**
  * Utilized when one squad member issues an invite for some other player.
  * Accessed by an existing squad member using the "Invite" menu option on another player.
  * This invitation is handled by the player who would join the squad.
  *
  * @param charId unique character identifier of the player who sent the invite
  * @param name name the player who sent the invite
  * @param features the squad
  */
final case class InvitationToJoinSquad(charId: Long, name: String, features: SquadFeatures)
    extends Invitation(charId, name) {
  def handleInvitation(indirectInviteFunc: (IndirectInvite, Player, Long, Long, String) => Boolean)(
    manager: SquadInvitationManager,
    invitedPlayer: Long,
    invitingPlayer: Long,
    otherName: String
  ): Unit = {
    manager.publish(
      invitedPlayer,
      SquadResponse.Membership(SquadResponseType.Invite, charId, Some(invitedPlayer), name, unk5 = false)
    )
    manager.publish(
      charId,
      SquadResponse.Membership(SquadResponseType.Invite, invitedPlayer, Some(charId), name, unk5 = true)
    )
  }

  def handleAcceptance(
                        manager: SquadInvitationManager,
                        player: Player,
                        invitedPlayer: Long,
                        @unused invitedPlayerSquadOpt: Option[SquadFeatures]
                      ): Unit = {
    if (
      manager.notLimitedByEnrollmentInSquad(invitedPlayerSquadOpt, invitedPlayer) &&
      SquadInvitationManager.canEnrollInSquad(features, invitedPlayer)
    ) {
      //accepted an invitation to join an existing squad
      manager.handleVacancyInvite(features, invitedPlayer, charId, player) match {
        case Some((_, line)) =>
          manager.acceptanceMessages(charId, invitedPlayer, player.Name)
          manager.joinSquad(player, features, line)
          manager.cleanUpQueuedInvites(invitedPlayer)
          manager.cleanUpInvitesForSquadAndPosition(features, line)
        case _ => ()
      }
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
    manager.rejectionMessages(rejectingPlayer, charId, player.Name)
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
    manager.refused(rejectingPlayer, charId)
  }

  def canBeAutoApproved: Boolean = false

  def getOptionalSquad: Option[SquadFeatures] = Some(features)

  def getPlayer: Player = null

  def appliesToPlayer(playerCharId: Long): Boolean = playerCharId == charId

  def appliesToSquad(guid: PlanetSideGUID): Boolean = features.Squad.GUID == guid

  def appliesToSquadAndPosition(guid: PlanetSideGUID, squadPosition: Int): Boolean = false
}
