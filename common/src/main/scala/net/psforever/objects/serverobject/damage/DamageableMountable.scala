//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.damage

import net.psforever.objects.Player
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.structures.Amenity
import services.avatar.{AvatarAction, AvatarServiceMessage}

trait DamageableMountable extends DamageableEntity {
  def DamageableObject : Amenity with Mountable

  override protected def DamageAwareness(target : Damageable.Target, cause : ResolvedProjectile, amount : Int) : Unit = {
    super.DamageAwareness(target, cause, amount)
    DamageableMountable.DamageAwareness(DamageableObject, cause, amount)
  }

  override protected def DestructionAwareness(target : Damageable.Target, cause : ResolvedProjectile) : Unit = {
    super.DestructionAwareness(target, cause)
    DamageableMountable.DestructionAwareness(DamageableObject, cause)
  }
}

object DamageableMountable {
  def DamageAwareness(obj : Damageable.Target with Mountable, cause : ResolvedProjectile, amount : Int) : Unit = {
    //alert occupants to damage source
    val zone = obj.Zone
    val zoneId = zone.Id
    val events = zone.AvatarEvents
    val attribution = zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => obj.GUID
    }
    obj.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    }).foreach(seat => {
      val tplayer = seat.Occupant.get
      events ! AvatarServiceMessage(zoneId, AvatarAction.HitHint(attribution, tplayer.GUID))
    })
  }

  def DestructionAwareness(obj : Damageable.Target with Mountable, cause : ResolvedProjectile) : Unit = {
    //kill everyone (and hide the bodies)
    obj.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    }).foreach(seat => {
      val tplayer = seat.Occupant.get
      tplayer.History(cause)
      tplayer.Actor ! Player.Die()
    })
  }
}
