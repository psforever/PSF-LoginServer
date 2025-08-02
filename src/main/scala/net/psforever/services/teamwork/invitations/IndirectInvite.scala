// Copyright (c) 2024 PSForever
package net.psforever.services.teamwork.invitations

import net.psforever.objects.Player
import net.psforever.objects.teamwork.SquadFeatures
import net.psforever.services.teamwork.{SquadInvitationManager, SquadResponse}
import net.psforever.types.PlanetSideGUID

import scala.annotation.unused

/**
  * Utilized to redirect an (accepted) invitation request to the proper squad leader.
  * An anticipated result of clarifying permission to request invitation
  * to a squad belonging to some player who is not the squad leader.
  * No direct action causes this message.
  * This invitation is handled by the squad leader.
  * @param originalRequester player who would be joining the squad;
 *                          also the player who invited the player who will become the squad leader
  * @param features squad
  */
final case class IndirectInvite(originalRequester: Player, features: SquadFeatures)
    extends Invitation(originalRequester.CharId, originalRequester.Name) {
  def handleInvitation(indirectInviteFunc: (IndirectInvite, Player, Long, Long, String) => Boolean)(
    manager: SquadInvitationManager,
    invitedPlayer: Long,
    invitingPlayer: Long,
    otherName: String
  ): Unit = {
    indirectInviteFunc(this, originalRequester, invitedPlayer, invitingPlayer, otherName)
  }

  def handleAcceptance(
                        manager: SquadInvitationManager,
                        @unused player: Player,
                        invitedPlayer: Long,
                        @unused invitedPlayerSquadOpt: Option[SquadFeatures]
                      ): Unit = {
    //tplayer / invitedPlayer is actually the squad leader
    if (SquadInvitationManager.canEnrollInSquad(features, originalRequester.CharId)) {
      val leaderCharId = player.CharId
      val invitedPlayer = originalRequester.CharId
      manager
        .handleVacancyInvite(features, invitedPlayer, leaderCharId, originalRequester)
        .collect {
          case (_, position) if manager.joinSquad(originalRequester, features, position) =>
            manager.acceptanceMessages(leaderCharId, invitedPlayer, originalRequester.Name)
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
            features
        }
        .orElse {
          manager.publish(
            leaderCharId,
            SquadResponse.SquadRelatedComment(s"Your invitation to ${originalRequester.Name} was accepted, but failed.")
          )
          manager.publish(
            invitedPlayer,
            SquadResponse.SquadRelatedComment(s"You have failed to joined the squad '${features.Squad.Task}'.")
          )
          None
        }
    } else {

    }
  }

  def handleRejection(
                       manager: SquadInvitationManager,
                       player: Player,
                       rejectingPlayer: Long,
                       @unused squadsToLeaders: List[(PlanetSideGUID, SquadFeatures)]
                     ): Unit = {
    doRejection(manager, player, rejectingPlayer)
    manager.publish(
      originalRequester.CharId,
      SquadResponse.SquadRelatedComment(s"Your request to join the squad has been refused.")
    )
    manager.publish(
      rejectingPlayer,
      SquadResponse.SquadRelatedComment(s"You refused ${originalRequester.Name}'s request to join this squad.")
    )
  }

  def doRejection(
                   manager: SquadInvitationManager,
                   player: Player,
                   rejectingPlayer: Long
                 ): Unit = {
    features.DeniedPlayers(originalRequester.CharId)
  }

  def handleCancel(
                    manager: SquadInvitationManager,
                    player: Player,
                    handlingPlayer: Long
                  ): Unit = {
    val invitingPlayer = originalRequester.CharId
    val invitingPlayerName = originalRequester.Name
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

  def getPlayer: Player = originalRequester

  def appliesToPlayer(playerCharId: Long): Boolean = playerCharId == originalRequester.CharId

  def appliesToSquad(guid: PlanetSideGUID): Boolean = features.Squad.GUID == guid

  def appliesToSquadAndPosition(guid: PlanetSideGUID, squadPosition: Int): Boolean = false
}
