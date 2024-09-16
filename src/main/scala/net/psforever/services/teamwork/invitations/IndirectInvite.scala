// Copyright (c) 2024 PSForever
package net.psforever.services.teamwork.invitations

import net.psforever.objects.Player
import net.psforever.objects.teamwork.SquadFeatures
import net.psforever.services.teamwork.SquadInvitationManager
import net.psforever.types.PlanetSideGUID

import scala.annotation.unused

/**
  * Utilized to redirect an (accepted) invitation request to the proper squad leader.
  * No direct action causes this message.
  * Depending on the situation, either the squad leader or the player who would join the squad handle this invitation.
  *
  * @param recruitOrOwner player who would be joining the squad;
  *                       may or may not have actually requested it in the first place
  * @param features squad
  */
final case class IndirectInvite(recruitOrOwner: Player, features: SquadFeatures)
    extends Invitation(recruitOrOwner.CharId, recruitOrOwner.Name) {
  def handleInvitation(indirectInviteFunc: (IndirectInvite, Player, Long, Long, String) => Boolean)(
    manager: SquadInvitationManager,
    invitedPlayer: Long,
    invitingPlayer: Long,
    otherName: String
  ): Unit = {
    indirectInviteFunc(this, recruitOrOwner, invitedPlayer, invitingPlayer, otherName)
  }

  def handleAcceptance(
                        manager: SquadInvitationManager,
                        @unused player: Player,
                        invitedPlayer: Long,
                        @unused invitedPlayerSquadOpt: Option[SquadFeatures]
                      ): Unit = {
    //tplayer / invitedPlayer is actually the squad leader
    if (SquadInvitationManager.canEnrollInSquad(features, recruitOrOwner.CharId)) {
      val recruitCharId = recruitOrOwner.CharId
      manager.handleVacancyInvite(features, recruitCharId, invitedPlayer, recruitOrOwner) match {
        case Some((_, line)) =>
          manager.acceptanceMessages(invitedPlayer, recruitCharId, recruitOrOwner.Name)
          manager.joinSquad(recruitOrOwner, features, line)
          manager.cleanUpAllInvitesWithPlayer(recruitCharId)
          manager.cleanUpInvitesForSquadAndPosition(features, line)
        //TODO since we are the squad leader, we do not want to brush off our queued squad invite tasks
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
    //todo how to do this?
  }

  def doRejection(
                   manager: SquadInvitationManager,
                   player: Player,
                   rejectingPlayer: Long
                 ): Unit = {
    //todo how to do this?
  }

  def canBeAutoApproved: Boolean = true

  def getOptionalSquad: Option[SquadFeatures] = Some(features)

  def getPlayer: Player = recruitOrOwner

  def appliesToPlayer(playerCharId: Long): Boolean = playerCharId == recruitOrOwner.CharId

  def appliesToSquad(guid: PlanetSideGUID): Boolean = features.Squad.GUID == guid

  def appliesToSquadAndPosition(guid: PlanetSideGUID, squadPosition: Int): Boolean = false
}
