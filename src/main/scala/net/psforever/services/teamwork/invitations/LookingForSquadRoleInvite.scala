// Copyright (c) 2024 PSForever
package net.psforever.services.teamwork.invitations

import net.psforever.objects.teamwork.{Member, SquadFeatures}
import net.psforever.objects.Player
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
    val leaderCharId = squadLeader.CharId
    if (
      manager.notLimitedByEnrollmentInSquad(invitedPlayerSquadOpt, invitedPlayer) &&
        SquadInvitationManager.canEnrollInSquad(features, invitedPlayer) &&
        manager.joinSquad(player, features, position)
    ) {
      //join this squad
      //manager.acceptanceMessages(invitedPlayer, requestee.CharId, requestee.Name)
      val msg = SquadResponse.Membership(SquadResponseType.Accept, invitedPlayer, Some(leaderCharId), player.Name, unk5 = false)
      manager.publish(leaderCharId, msg)
      manager.publish(invitedPlayer, msg.copy(unk5 = true))
//      manager.publish(
//        invitedPlayer,
//        SquadResponse.SquadRelatedComment(s"You have accepted ${squadLeader.Name}'s request to join a squad.")
//      )
//      manager.publish(
//        leaderCharId,
//        SquadResponse.SquadRelatedComment(s"${player.Name} has agreed to joined your squad.")
//      )
      //clean up invitations specifically for this squad and this position
      val cleanedUpActiveInvitesForSquadAndPosition = manager.cleanUpActiveInvitesForSquadAndPosition(features.Squad.GUID, position)
      cleanedUpActiveInvitesForSquadAndPosition.collect { case (id, _) =>
        manager.publish(
          id,
          SquadResponse.SquadRelatedComment(s"An invitation to join a squad has ended.")
        )
      }
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
      manager.publish(
        invitedPlayer,
        SquadResponse.SquadRelatedComment(s"Your accepted an invitation to squad '${features.Squad.Task}', but it failed.")
      )
      manager.publish(
        leaderCharId,
        SquadResponse.SquadRelatedComment(s"An accepted request to join your squad has failed.")
      )
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
   val faction = player.Faction
    manager.reloadSearchForRoleInvite(
      player.Zone.Players.filter(_.faction == faction),
      rejectingPlayer,
      features,
      position
    )
  }

  def handleCancel(
                    manager: SquadInvitationManager,
                    player: Player,
                    handlingPlayer: Long
                  ): Unit = {
    val actingPlayer = player.CharId
    val leaderCharId = features.Squad.Leader.CharId
    val leaderName = features.Squad.Leader.Name
    if (actingPlayer == handlingPlayer) {
      manager.publish(
        leaderCharId,
        SquadResponse.SquadRelatedComment(s"${player.Name} has declined to join the squad.")
      )
    } else if (actingPlayer == leaderCharId) {
      manager.publish(
        handlingPlayer,
        SquadResponse.SquadRelatedComment(s"$leaderName has rescinded the offer to join the squad.")
      )
    } else {
      manager.publish(
        leaderCharId,
        SquadResponse.SquadRelatedComment(s"The offer to ${player.Name} to join the squad is no longer valid.")
      )
      manager.publish(
        handlingPlayer,
        SquadResponse.SquadRelatedComment(s"The offer from $leaderName to join the squad is no longer valid.")
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
