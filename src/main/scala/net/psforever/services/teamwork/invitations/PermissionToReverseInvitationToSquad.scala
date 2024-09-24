// Copyright (c) 2024 PSForever
package net.psforever.services.teamwork.invitations

import net.psforever.objects.Player
import net.psforever.objects.teamwork.SquadFeatures
import net.psforever.services.teamwork.{SquadInvitationManager, SquadResponse}
import net.psforever.types.PlanetSideGUID

/**
 * When requesting to that some other player join a newly-formed squad,
 * but that player is actually the member of a squad already,
 * this offer is extended to convert the invitation request into a different invitation request.
 * The "different invitation" will be asking the leader of the other player's squad if our player can join it.
 * Only technically an "invitation" in that sense, just for the purposes of handling it.
 * This "invitation" is handled by the player who tried to initiate the original invitation to the other player.
 * @param initialRequest player who would be joining the squad
 * @param invitedPlayer player who would be joining the squad (unique character id)
 * @param invitedPlayerSquad squad
 */
case class PermissionToReverseInvitationToSquad(initialRequest: Player, invitedPlayer: Long, invitedPlayerSquad: SquadFeatures)
  extends Invitation(initialRequest.CharId, initialRequest.Name) {

  def handleInvitation(indirectInviteFunc: (IndirectInvite, Player, Long, Long, String) => Boolean)(
    manager: SquadInvitationManager,
    invitedPlayer: Long,
    invitingPlayer: Long,
    otherName: String
  ): Unit = {
    manager.publish(
      invitingPlayer,
      SquadResponse.SquadRelatedComment(s"\\#6 The player you tried to invite already belongs to a squad.")
    )
    manager.publish(
      invitingPlayer,
      SquadResponse.SquadRelatedComment(s"\\#6Would you like to try join that squad? (respond with \\#3/accept\\#6 or \\#3/cancel\\#6)")
    )
  }

  def handleAcceptance(
                        manager: SquadInvitationManager,
                        player: Player,
                        invitedPlayer: Long,
                        invitedPlayerSquadOpt: Option[SquadFeatures]
                      ): Unit = {
    manager.createIndirectInvite(player, invitedPlayer, invitedPlayerSquad) //should put it at the front of the list
  }

  def handleRejection(
                       manager: SquadInvitationManager,
                       player: Player,
                       rejectingPlayer: Long,
                       squadsToLeaders: List[(PlanetSideGUID, SquadFeatures)]
                     ): Unit = {
    /* wordless rejection */
  }

  def doRejection(
                   manager: SquadInvitationManager,
                   player: Player,
                   rejectingPlayer: Long
                 ): Unit = {
    /* wordless rejection */
  }

  def handleCancel(
                    manager: SquadInvitationManager,
                    player: Player,
                    handlingPlayer: Long
                  ): Unit = {
    val actingPlayer = player.CharId
    if (actingPlayer != handlingPlayer) {
      manager.publish(
        handlingPlayer,
        SquadResponse.SquadRelatedComment(s"A question regarding squad invitations no longer matters.")
      )
    }
  }

  def canBeAutoApproved: Boolean = false

  def getOptionalSquad: Option[SquadFeatures] = Some(invitedPlayerSquad)

  def getPlayer: Player = initialRequest

   def appliesToPlayer(playerCharId: Long): Boolean = invitedPlayer == playerCharId

  def appliesToSquad(guid: PlanetSideGUID): Boolean = invitedPlayerSquad.Squad.GUID == guid

  def appliesToSquadAndPosition(guid: PlanetSideGUID, squadPosition: Int): Boolean = false
}
