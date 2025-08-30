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
  private val ExpirationSeconds = 320

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
}

