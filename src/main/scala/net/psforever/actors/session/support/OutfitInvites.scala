package net.psforever.actors.session.support

import net.psforever.objects.Player
import scala.collection.mutable

case class OutfitInvite(
                   sentTo: Player,
                   sentFrom: Player,
                   timestamp: Long = System.currentTimeMillis() / 1000
                 )

object OutfitInviteManager {
  private val invites = mutable.Map[Long, OutfitInvite]()

  /** how long an outfit invitation stays valid after it was extended */
  val ExpirationSeconds = 320

  /**
    * The manager is shared between every session actor and the expiry sweep's executor thread,
    * and its operations are compound (registering an invitation, claiming one, withdrawing an
    * expired one only if unchanged). Every method is synchronized so those can not interleave:
    * an acceptance claiming its invitation and the expiry sweep withdrawing the same one are
    * decided atomically, with the loser finding nothing and acting on nothing.
    */
  def addOutfitInvite(invite: OutfitInvite): Boolean = synchronized {
    invites.get(invite.sentTo.CharId) match {
      case Some(existing) if (System.currentTimeMillis() / 1000 - existing.timestamp) < ExpirationSeconds =>
        false // Reject new invite (previous one is still valid)
      case _ =>
        invites(invite.sentTo.CharId) = invite
        true
    }
  }

  /**
    * Atomically claim the invitation stored for an invited player: it is removed and handed
    * over, so neither a later expiry sweep nor any concurrent action can act on it anymore.
    * @param sentToId the invited player's character id
    * @return the claimed invitation, or `None` if none is stored for the player
    */
  def withdrawInvite(sentToId: Long): Option[OutfitInvite] = synchronized {
    invites.remove(sentToId)
  }

  /**
    * Atomically claim the invitation that a given inviter extended to a given invited player,
    * matched by the invited player's character id and falling back to their name.
    * @param sentFromId the inviting player's character id; only their invitation may be claimed
    * @param sentToId the invited player's character id
    * @param sentToName the invited player's name, for a claim whose id did not match
    * @return the claimed invitation, or `None` if this inviter holds no such invitation
    */
  def withdrawInvitationOf(sentFromId: Long, sentToId: Long, sentToName: String): Option[OutfitInvite] = synchronized {
    val invitations = invites.values.filter(_.sentFrom.CharId == sentFromId)
    invitations
      .find(_.sentTo.CharId == sentToId)
      .orElse(invitations.find(invite => invite.sentTo.Name.equalsIgnoreCase(sentToName)))
      .map { invite =>
        invites.remove(invite.sentTo.CharId)
        invite
      }
  }

  /**
    * Remove the invitation stored for its invitee, but only if it is still the given one.
    * The invitee's slot may have been replaced by a newer invitation in the meantime, and that
    * invitation schedules its own expiry; a replacement can also only occur once the previous
    * one lapsed, so the caller learns here that its invitation truly expired unlodged.
    * @param invite the invitation as it was originally created
    * @return the removed invitation, or `None` if the invitee's slot now holds something else or nothing
    */
  def withdrawIfUnchanged(invite: OutfitInvite): Option[OutfitInvite] = synchronized {
    invites.get(invite.sentTo.CharId) match {
      case Some(current) if current eq invite =>
        invites.remove(invite.sentTo.CharId)
        Some(current)
      case _ =>
        None
    }
  }
}
