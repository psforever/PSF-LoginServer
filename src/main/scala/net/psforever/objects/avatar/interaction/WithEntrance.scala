// Copyright (c) 2024 PSForever
package net.psforever.objects.avatar.interaction

import net.psforever.objects.serverobject.doors.{Door, InteriorDoorPassage}
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentTrait, PieceOfEnvironment, interaction}
import net.psforever.objects.serverobject.environment.interaction.{InteractionWith, RespondsToZoneEnvironment}
import net.psforever.objects.serverobject.interior.{Sidedness, TraditionalInteriorAware}
import net.psforever.objects.zones.interaction.InteractsWithZone
import net.psforever.types.Vector3

import scala.annotation.unused
import scala.concurrent.duration._

class WithEntrance()
  extends InteractionWith
  with TraditionalInteriorAware {
  val attribute: EnvironmentTrait = EnvironmentAttribute.InteriorField

  private var stopTest: Boolean = false

  def doInteractingWith(
                         obj: InteractsWithZone,
                         body: PieceOfEnvironment,
                         data: Option[Any]
                       ): Unit = {
    if (stopTest && data.contains("bellybutton")) {
      stopTest = false
    } else {
      doorInteriorCheck(obj, body.asInstanceOf[InteriorDoorPassage].door)
      obj.Actor ! RespondsToZoneEnvironment.Timer(attribute, delay = 250.milliseconds, obj.Actor, interaction.InteractingWithEnvironment(body, Some("bellybutton")))
    }
  }

  override def stopInteractingWith(
                                    obj: InteractsWithZone,
                                    body: PieceOfEnvironment,
                                    data: Option[Any]
                                  ): Unit = {
    straightforwardInteriorCheck(obj, body.asInstanceOf[InteriorDoorPassage].door)
    stopTest = true
  }

  private def doorInteriorCheck(
                                 obj: InteractsWithZone,
                                 door: Door
                               ): Sidedness = {
    val strictly = interiorCheck(obj, door)
    val value = if (door.isOpen) {
      Sidedness.InBetweenSides(door, strictly)
    } else {
      strictly
    }
    WhichSide_=(value)
    value
  }

  private def straightforwardInteriorCheck(
                                            obj: InteractsWithZone,
                                            door: Door
                                          ): Sidedness = {
    WhichSide_=(interiorCheck(obj, door))
  }

  private def interiorCheck(
                             obj: InteractsWithZone,
                             door: Door
                           ): Sidedness = {
    //debugInteriorCheck(obj, door)
    strictInteriorCheck(obj, door)
  }

  @unused
  private def strictInteriorCheck(
                                   obj: InteractsWithZone,
                                   door: Door
                                 ): Sidedness = {
    if (door.Outwards == Vector3.Zero) {
      WhichSide
    } else if (Vector3.DotProduct(Vector3.Unit(obj.Position - door.Position), door.Outwards) > 0f) {
      Sidedness.OutsideOf
    } else {
      Sidedness.InsideOf
    }
  }

  @unused
  private def debugInteriorCheck(
                                  obj: InteractsWithZone,
                                  door: Door
                                ): Sidedness = {
    import net.psforever.objects.{Player, Vehicle}
    import net.psforever.packet.game.ChatMsg
    import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
    import net.psforever.types.{ChatMessageType, PlanetSideGUID}
    val channel = obj match {
      case p: Player => p.Name
      case v: Vehicle => v.Actor.toString()
      case _ => ""
    }
    if (door.Outwards == Vector3.Zero) {
      obj.Zone.AvatarEvents ! AvatarServiceMessage(
        channel,
        AvatarAction.SendResponse(ChatMsg(ChatMessageType.UNK_229, "Door not configured."))
      )
      WhichSide
    } else {
      val result = Vector3.DotProduct(Vector3.Unit(obj.Position - door.Position), door.Outwards) > 0f
      if (result && WhichSide != Sidedness.OutsideOf) {
        //outside
        obj.Zone.AvatarEvents ! AvatarServiceMessage(
          channel,
          AvatarAction.SendResponse(ChatMsg(ChatMessageType.UNK_229, "You are now outside"))
        )
        Sidedness.OutsideOf
      } else if (!result && WhichSide != Sidedness.InsideOf) {
        //inside
        obj.Zone.AvatarEvents ! AvatarServiceMessage(
          channel,
          AvatarAction.SendResponse(ChatMsg(ChatMessageType.UNK_229, "You are now inside"))
        )
        Sidedness.InsideOf
      } else {
        WhichSide
      }
    }
  }
}
