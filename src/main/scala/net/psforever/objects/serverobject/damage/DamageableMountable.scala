//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import net.psforever.objects.Player
import net.psforever.objects.ballistics.PlayerSource
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vital.test.DamageResult
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
    val occupants = target.Seats.values.collect {
      case seat if seat.isOccupied && seat.Occupant.get.isAlive =>
        seat.Occupant.get
    }
    ((cause.adversarial match {
      case Some(adversarial) => Some(adversarial.attacker)
      case None              => None
    }) match {
      case Some(pSource: PlayerSource) => //player damage
        val name = pSource.Name
        (zone.LivePlayers.find(_.Name == name).orElse(zone.Corpses.find(_.Name == name)) match {
          case Some(player) =>
            AvatarAction.HitHint(player.GUID, player.GUID)
          case None =>
            AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(countableDamage, pSource.Position))
        }) match {
          case AvatarAction.HitHint(_, guid) =>
            occupants.map { tplayer => (tplayer.Name, AvatarAction.HitHint(guid, tplayer.GUID)) }
          case msg =>
            occupants.map { tplayer => (tplayer.Name, msg) }
        }
      case Some(source) => //object damage
        val msg = AvatarAction.SendResponse(Service.defaultPlayerGUID, DamageWithPositionMessage(countableDamage, source.Position))
        occupants.map { tplayer => (tplayer.Name, msg) }
      case None =>
        List.empty
    }).foreach {
      case (channel, msg) =>
        events ! AvatarServiceMessage(channel, msg)
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
    target.Seats.values
      .filter(seat => {
        seat.isOccupied && seat.Occupant.get.isAlive
      })
      .foreach(seat => {
        val tplayer = seat.Occupant.get
        tplayer.History(cause)
        tplayer.Actor ! Player.Die()
      })
  }
}
