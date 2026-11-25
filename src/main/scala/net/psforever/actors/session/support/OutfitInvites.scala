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

  /** how long an outfit invitation stays valid after it was created */
  val ExpirationSeconds = 320

  def addOutfitInvite(invite: OutfitInvite): Boolean = {
    invites.get(invite.sentTo.CharId) match {
      case Some(existing) if (System.currentTimeMillis() / 1000 - existing.timestamp) < ExpirationSeconds =>
        false // Reject new invite (previous one is still valid)
      case _ =>
        invites(invite.sentTo.CharId) = invite
        true
    }
  }

  def removeOutfitInvite(sentToId: Long): Unit = {
    invites.remove(sentToId)
  }

  def getOutfitInvite(sentToId: Long): Option[OutfitInvite] = invites.get(sentToId)

  def getAllOutfitInvites: List[OutfitInvite] = invites.values.toList

  /**
    * Remove the invitation stored for its invitee, but only if it is still the given one.
    * The invitee's slot may have been replaced by a newer invitation in the meantime, and that
    * invitation schedules its own expiry; a replacement can also only occur once the previous
    * one lapsed, so the caller learns here that its invitation truly expired unlodged.
    * @param invite the invitation as it was originally created
    * @return the removed invitation, or `None` if the invitee's slot now holds something else or nothing
    */
  def withdrawIfUnchanged(invite: OutfitInvite): Option[OutfitInvite] = {
    invites.get(invite.sentTo.CharId) match {
      case Some(current) if current eq invite =>
        invites.remove(invite.sentTo.CharId)
        Some(current)
      case _ =>
        None
    }
  }
}

