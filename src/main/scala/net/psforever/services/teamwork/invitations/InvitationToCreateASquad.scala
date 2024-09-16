// Copyright (c) 2024 PSForever
package net.psforever.services.teamwork.invitations

import net.psforever.objects.Player
import net.psforever.objects.teamwork.SquadFeatures
import net.psforever.services.teamwork.SquadInvitationManager.FinishStartSquad
import net.psforever.services.teamwork.{SquadInvitationManager, SquadResponse}
import net.psforever.types.{PlanetSideGUID, SquadResponseType}

import scala.annotation.unused
import scala.util.Success

/**
  * Utilized when one player issues an invite for some other player for a squad that does not yet exist.
  * This invitation is handled by the player who would be joining the squad.
  *
  * @param futureSquadLeader player who wishes to become the leader of a squad
  */
final case class InvitationToCreateASquad(futureSquadLeader: Player)
    extends Invitation(futureSquadLeader.CharId, futureSquadLeader.Name) {
  def handleInvitation(indirectInviteFunc: (IndirectInvite, Player, Long, Long, String) => Boolean)(
    manager: SquadInvitationManager,
    invitedPlayer: Long,
    invitingPlayer: Long,
    otherName: String
  ): Unit = {
    manager.publish(
      invitedPlayer,
      SquadResponse.Membership(SquadResponseType.Invite, inviterCharId, Some(invitedPlayer), futureSquadLeader.Name, unk5 = false)
    )
    manager.publish(
      inviterCharId,
      SquadResponse.Membership(SquadResponseType.Invite, invitedPlayer, Some(inviterCharId), futureSquadLeader.Name, unk5 = true)
    )
  }

  def handleAcceptance(
                        manager: SquadInvitationManager,
                        player: Player,
                        invitedPlayer: Long,
                        invitedPlayerSquadOpt: Option[SquadFeatures]
                      ): Unit = {
    if (manager.notLimitedByEnrollmentInSquad(invitedPlayerSquadOpt, invitedPlayer)) {
      //accepted an invitation to join an existing squad
      import scala.concurrent.ExecutionContext.Implicits.global
      val leaderCharId = futureSquadLeader.CharId
      manager
        .askToCreateANewSquad(futureSquadLeader)
        .onComplete {
          case Success(FinishStartSquad(features)) =>
            manager.handleVacancyInvite(features, invitedPlayer, leaderCharId, player) match {
              case Some((_, line)) =>
                manager.publish(
                  invitedPlayer,
                  SquadResponse.Membership(SquadResponseType.Accept, invitedPlayer, Some(leaderCharId), "", unk5 = true)
                )
                manager.publish(
                  leaderCharId,
                  SquadResponse.Membership(SquadResponseType.Accept, leaderCharId, Some(invitedPlayer), player.Name, unk5 = false)
                )
                manager.joinSquad(player, features, line)
                manager.cleanUpQueuedInvites(invitedPlayer)
              case _ => ()
            }
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
    //rejectingPlayer is the would-be squad member; the would-be squad leader sent the request and was rejected
    val invitingPlayerCharId = futureSquadLeader.CharId
    doRejection(manager, player, rejectingPlayer)
    manager.publish(
      rejectingPlayer,
      SquadResponse.Membership(SquadResponseType.Reject, rejectingPlayer, Some(invitingPlayerCharId), "", unk5 = true)
    )
    manager.publish(
      invitingPlayerCharId,
      SquadResponse.Membership(SquadResponseType.Reject, invitingPlayerCharId, Some(rejectingPlayer), player.Name, unk5 = false)
    )
    manager.publish(
      rejectingPlayer,
      SquadResponse.SquadRelatedComment(s"Your request to form a squad has been refused.")
    )
  }

  def doRejection(
                   manager: SquadInvitationManager,
                   player: Player,
                   rejectingPlayer: Long
                 ): Unit = {
    manager.refused(rejectingPlayer, futureSquadLeader.CharId)
  }

  def canBeAutoApproved: Boolean = false

  def getOptionalSquad: Option[SquadFeatures] = None

  def getPlayer: Player = futureSquadLeader

  def appliesToPlayer(playerCharId: Long): Boolean = playerCharId == futureSquadLeader.CharId

  def appliesToSquad(guid: PlanetSideGUID): Boolean = false

  def appliesToSquadAndPosition(guid: PlanetSideGUID, squadPosition: Int): Boolean = false
}
