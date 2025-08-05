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
      val leaderCharId = charId
      manager
        .handleVacancyInvite(features, invitedPlayer, charId, player)
        .collect {
          case (_, line) if manager.joinSquad(player, features, line) =>
            //manager.acceptanceMessages(charId, invitedPlayer, player.Name)
            manager.publish(
              leaderCharId,
              SquadResponse.SquadRelatedComment(s"Your invitation to ${player.Name} was accepted.")
            )
            manager.publish(
              invitedPlayer,
              SquadResponse.SquadRelatedComment(s"You have joined the squad '${features.Squad.Task}'.")
            )
            //all invitations involving the invited person must be cancelled due to the nature of this acceptance
            manager.cleanUpQueuedInvitesForPlayer(invitedPlayer).collect { case (id, _) =>
              manager.publish(
                id,
                SquadResponse.SquadRelatedComment(s"An invitation involving ${player.Name} has ended.")
              )
            }
            if (features.Squad.Capacity == features.Squad.Size) {
              val cleanedUpActiveInvites = manager.cleanUpActiveInvitesForSquad(features.Squad.GUID)
              cleanedUpActiveInvites.collect { case (id, invites) =>
                invites.foreach(_.handleCancel(manager, player, id))
                manager.publish(
                  id,
                  SquadResponse.SquadRelatedComment(s"An invitation to join a squad has ended.")
                )
              }
              (manager.cleanUpQueuedInvitesForSquad(features.Squad.GUID) ++ cleanedUpActiveInvites).collectFirst { case _ =>
                manager.publish(
                  leaderCharId,
                  SquadResponse.SquadRelatedComment(s"You had invitations that were cancelled due to this action.")
                )
              }
            }
            features
        }
        .orElse {
          manager.publish(
            leaderCharId,
            SquadResponse.SquadRelatedComment(s"Your invitation to ${player.Name} was accepted, but failed.")
          )
          manager.publish(
            invitedPlayer,
            SquadResponse.SquadRelatedComment(s"You have failed to joined the squad '${features.Squad.Task}'.")
          )
          None
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
    /*manager.publish(
      rejectingPlayer,
      SquadResponse.SquadRelatedComment(s"Your request to join squad '${features.Squad.Task}' has been refused.")
    )*/
  }

  def doRejection(
                   manager: SquadInvitationManager,
                   player: Player,
                   rejectingPlayer: Long
                 ): Unit = {
    manager.refused(rejectingPlayer, charId)
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

  def appliesToPlayer(playerCharId: Long): Boolean = playerCharId == charId

  def appliesToSquad(guid: PlanetSideGUID): Boolean = features.Squad.GUID == guid

  def appliesToSquadAndPosition(guid: PlanetSideGUID, squadPosition: Int): Boolean = false
}
