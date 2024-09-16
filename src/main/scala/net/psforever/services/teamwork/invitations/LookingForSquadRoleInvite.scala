// Copyright (c) 2024 PSForever
package net.psforever.services.teamwork.invitations

import net.psforever.objects.teamwork.{Member, SquadFeatures}
import net.psforever.objects.{LivePlayerList, Player}
import net.psforever.services.teamwork.{SquadInvitationManager, SquadResponse}
import net.psforever.types.{PlanetSideGUID, SquadResponseType}

import scala.annotation.unused

/**
  * Utilized in conjunction with an external queuing data structure
  * to search for and submit requests to other players
  * for the purposes of fill out an unoccupied squad role.
  * This invitation is handled by the player who would be joining the squad.
  *
  * @param squadLeader squad leader
  * @param features squad with the role
  * @param position index of the role
  */
final case class LookingForSquadRoleInvite(squadLeader: Member, features: SquadFeatures, position: Int)
    extends Invitation(squadLeader.CharId, squadLeader.Name) {
  def handleInvitation(indirectInviteFunc: (IndirectInvite, Player, Long, Long, String) => Boolean)(
    manager: SquadInvitationManager,
    invitedPlayer: Long,
    invitingPlayer: Long,
    otherName: String
  ): Unit = {
    manager.publish(
      invitedPlayer,
      SquadResponse.Membership(SquadResponseType.Invite, invitedPlayer, Some(squadLeader.CharId), squadLeader.Name, unk5 = false)
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
      val invitingPlayer = squadLeader.CharId
      features.ProxyInvites = features.ProxyInvites.filterNot { _ == invitedPlayer }
      if (manager.joinSquad(player, features, position)) {
        //join this squad
        manager.acceptanceMessages(invitingPlayer, invitedPlayer, player.Name)
        manager.cleanUpQueuedInvites(player.CharId)
        manager.cleanUpInvitesForSquadAndPosition(features, position)
      }
    }
  }

  def handleRejection(
                       manager: SquadInvitationManager,
                       player: Player,
                       rejectingPlayer: Long,
                       @unused squadsToLeaders: List[(PlanetSideGUID, SquadFeatures)]
                     ): Unit = {
    val leaderCharId = squadLeader.CharId
    //rejectingPlayer is the would-be squad member; the squad leader sent the request and was rejected
    doRejection(manager, player, rejectingPlayer)
    manager.rejectionMessages(rejectingPlayer, leaderCharId, player.Name)
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
    manager.reloadSearchForRoleInvite(
      LivePlayerList.WorldPopulation { _ => true },
      rejectingPlayer,
      features,
      position
    )
  }

  def canBeAutoApproved: Boolean = false

  def getOptionalSquad: Option[SquadFeatures] = Some(features)

  def getPlayer: Player = null

  def appliesToPlayer(playerCharId: Long): Boolean = playerCharId == squadLeader.CharId

  def appliesToSquad(guid: PlanetSideGUID): Boolean = features.Squad.GUID == guid

  def appliesToSquadAndPosition(guid: PlanetSideGUID, squadPosition: Int): Boolean = appliesToSquad(guid) && position == squadPosition
}
