// Copyright (c) 2024 PSForever
package net.psforever.objects.avatar.interaction

import net.psforever.objects.serverobject.environment.interaction.{InteractionWith, RespondsToZoneEnvironment}
import net.psforever.objects.{Vehicle, Vehicles}
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentTrait, GantryDenialField, PieceOfEnvironment, interaction}
import net.psforever.objects.serverobject.shuttle.OrbitalShuttlePad
import net.psforever.objects.zones.InteractsWithZone
import net.psforever.packet.game.{PlayerStateShiftMessage, ShiftState}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.hart.ShuttleState

import scala.annotation.unused
import scala.concurrent.duration._

class WithGantry(val channel: String)
  extends InteractionWith {
  val attribute: EnvironmentTrait = EnvironmentAttribute.GantryDenialField

  def doInteractingWith(
                         obj: InteractsWithZone,
                         body: PieceOfEnvironment,
                         @unused data: Option[Any]
                       ): Unit = {
    val field = body.asInstanceOf[GantryDenialField]
    val zone = obj.Zone
    (zone.GUID(field.obbasemesh) match {
      case Some(pad : OrbitalShuttlePad) => zone.GUID(pad.shuttle)
      case _                             => None
    }) match {
      case Some(shuttle: Vehicle)
        if shuttle.Flying.contains(ShuttleState.State11.id) || shuttle.Faction != obj.Faction =>
        val (pos, ang) = Vehicles.dismountShuttle(shuttle, field.mountPoint)
        shuttle.Zone.AvatarEvents ! AvatarServiceMessage(
          channel,
          AvatarAction.SendResponse(
            Service.defaultPlayerGUID,
            PlayerStateShiftMessage(ShiftState(0, pos, ang, None)))
        )
      case Some(_: Vehicle) =>
        obj.Actor ! RespondsToZoneEnvironment.Timer(
          attribute,
          delay = 250 milliseconds,
          obj.Actor,
          interaction.InteractingWithEnvironment(body, None)
        )
      case _ => () //something configured incorrectly; no need to keep checking
    }
  }
}
