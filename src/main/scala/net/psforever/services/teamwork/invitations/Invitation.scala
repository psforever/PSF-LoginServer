// Copyright (c) 2024 PSForever
package net.psforever.services.teamwork.invitations

import net.psforever.objects.Player
import net.psforever.objects.teamwork.SquadFeatures
import net.psforever.services.teamwork.SquadInvitationManager
import net.psforever.types.PlanetSideGUID

/**
 * The base of all objects that exist for the purpose of communicating invitation from one player to the next.
 * @param charId inviting player's unique identifier number
 * @param name inviting player's name
 */
abstract class Invitation(charId: Long, name: String) {
  def inviterCharId: Long = charId
  def inviterName: String = name

  /**
   * A branched response for processing (new) invitation objects that have been submitted to the system.<br>
   * <br>
   * A comparison is performed between the original invitation object and an invitation object
   * that represents the potential modification or redirection of the current active invitation obect.
   * Any further action is only performed when an "is equal" comparison is `true`.
   * When passing, the system publishes up to two messages
   * to users that would anticipate being informed of squad join activity.
   * @param indirectInviteFunc the method that cans the responding behavior should an `IndirectInvite` object being consumed
   * @param invitedPlayer the unique character identifier for the player being invited;
   *                      in actuality, represents the player who will address the invitation object
   * @param invitingPlayer the unique character identifier for the player who invited the former
   * @param otherName a name to be used in message composition
   */
  def handleInvitation(indirectInviteFunc: (IndirectInvite, Player, Long, Long, String) => Boolean)(
    manager: SquadInvitationManager,
    invitedPlayer: Long,
    invitingPlayer: Long,
    otherName: String
  ): Unit

  def handleAcceptance(
                        manager: SquadInvitationManager,
                        player: Player,
                        invitedPlayer: Long,
                        invitedPlayerSquadOpt: Option[SquadFeatures]
                      ): Unit

  def handleRejection(
                       manager: SquadInvitationManager,
                       player: Player,
                       rejectingPlayer: Long,
                       squadsToLeaders: List[(PlanetSideGUID, SquadFeatures)]
                     ): Unit

  def doRejection(
                   manager: SquadInvitationManager,
                   player: Player,
                   rejectingPlayer: Long
                 ): Unit

  def canBeAutoApproved: Boolean

  def getOptionalSquad: Option[SquadFeatures]

  /**
   * na
   * @return active player entity associated with this invite;
   *         can be `null` as some invitations do not retain such character data
   */
  def getPlayer: Player

  def appliesToPlayer(playerCharId: Long): Boolean

  def appliesToSquad(guid: PlanetSideGUID): Boolean

  def appliesToSquadAndPosition(guid: PlanetSideGUID, squadPosition: Int): Boolean
}
