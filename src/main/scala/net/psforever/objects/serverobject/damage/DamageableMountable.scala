//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import net.psforever.objects.Player
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.packet.game.DamageWithPositionMessage
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

/**
  * Functions to assist other damage-dealing code for objects that contain users.
  */
object DamageableMountable {

  /**
    * A damaged target alerts its occupants (as it is a `Mountable` object) of the source of the damage.
    *
    * @see `AvatarAction.HitHint`
    * @see `AvatarAction.SendResponse`
    * @see `AvatarServiceMessage`
    * @see `DamageWithPositionMessage`
    * @see `Mountable.Seats`
    * @see `Service.defaultPlayerGUID`
    * @see `Zone.AvatarEvents`
    * @see `Zone.LivePlayers`
    * @param target the entity being damaged
    * @param cause historical information about the damage
    * @param countableDamage the amount of damage being done, translating to the intensity of the damage indicator
    */
  def DamageAwareness(
                       target: Damageable.Target with Mountable,
                       cause: DamageResult,
                       countableDamage: Int
                     ): Unit = {
    val zone   = target.Zone
    val events = zone.AvatarEvents
    val occupants = target.Seats.values.toSeq.flatMap { seat => seat.occupants.filter(_.isAlive) }
    ((cause.adversarial match {
      case Some(adversarial) => Some(adversarial.attacker)
      case None              => None
    }) match {
      case Some(pSource: PlayerSource) => //player damage
        val name = pSource.Name
        (zone.LivePlayers.find(_.Name == name).orElse(zone.Corpses.find(_.Name == name)) match {
          case Some(player) =>
            AvatarAction.HitHint(player.GUID)
          case None =>
            AvatarAction.SendResponse(DamageWithPositionMessage(countableDamage, pSource.Position))
        }) match {
          case AvatarAction.HitHint(guid) =>
            occupants.map { tplayer => (tplayer.Name, guid, AvatarAction.HitHint(tplayer.GUID)) }
          case msg =>
            occupants.map { tplayer => (tplayer.Name, Service.defaultPlayerGUID, msg) }
        }
      case Some(source) => //object damage
        val msg = AvatarAction.SendResponse(DamageWithPositionMessage(countableDamage, source.Position))
        occupants.map { tplayer => (tplayer.Name, Service.defaultPlayerGUID, msg) }
      case None =>
        List.empty
    }).foreach {
      case (channel, filter, msg) =>
        events ! AvatarServiceMessage(channel, filter, msg)
    }
  }

  /**
    * When the target dies, so do all of its occupants.
    * @see `Mountable.Seats`
    * @see `Player.Die`
    * @see `VitalsHistory.History`
    * @param target the entity being destroyed
    * @param cause historical information about the damage
    */
  def DestructionAwareness(target: Damageable.Target with Mountable, cause: DamageResult): Unit = {
    val interaction = cause.interaction
    val targets = target.Seats.values.flatMap(_.occupant).filter(_.isAlive)
    targets.foreach { player =>
        //make llu visible to others in zone if passenger is carrying one
        player.Zone.AvatarEvents ! AvatarServiceMessage(player.Name, AvatarAction.DropSpecialItem())
        //player.LogActivity(cause)
        player.Actor ! Player.Die(
          DamageInteraction(interaction.resolution, SourceEntry(player), interaction.cause, interaction.hitPos)
        )
    }
  }
}
