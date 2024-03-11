// Copyright (c) 2024 PSForever
package net.psforever.objects.avatar.interaction

import net.psforever.objects.serverobject.doors.{Door, InteriorDoorPassage}
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentTrait, PieceOfEnvironment, interaction}
import net.psforever.objects.serverobject.environment.interaction.{InteractionWith, RespondsToZoneEnvironment}
import net.psforever.objects.serverobject.interior.{Sidedness, TraditionalInteriorAware}
import net.psforever.objects.zones.InteractsWithZone
import net.psforever.types.Vector3

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
      val door = body.asInstanceOf[InteriorDoorPassage].door
      val strictly = performInteriorCheck(obj, door)
      val value = if (door.isOpen) {
        Sidedness.InBetweenSides(door, strictly)
      } else {
        strictly
      }
      WhichSide_=(value)
      obj.Actor ! RespondsToZoneEnvironment.Timer(attribute, delay = 250.milliseconds, obj.Actor, interaction.InteractingWithEnvironment(body, Some("bellybutton")))
    }
  }

  override def stopInteractingWith(
                                    obj: InteractsWithZone,
                                    body: PieceOfEnvironment,
                                    data: Option[Any]
                                  ): Unit = {
    WhichSide_=(performInteriorCheck(obj, body.asInstanceOf[InteriorDoorPassage].door))
    stopTest = true
  }

  private def performInteriorCheck(
                                    obj: InteractsWithZone,
                                    door: Door
                                  ): Sidedness = {
    debugInteriorCheck(obj, door)
//    if (Vector3.DotProduct(Vector3.Unit(obj.Position - door.Position), door.Outwards) > 0f) {
//      Sidedness.OutsideOf
//    } else {
//      Sidedness.InsideOf
//    }
  }

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
    val result = Vector3.DotProduct(Vector3.Unit(obj.Position - door.Position), door.Outwards) > 0f
    if (result && WhichSide != Sidedness.OutsideOf) {
      //outside
      obj.Zone.AvatarEvents ! AvatarServiceMessage(
        channel,
        AvatarAction.SendResponse(PlanetSideGUID(0), ChatMsg(ChatMessageType.UNK_229, "You are now outside"))
      )
      Sidedness.OutsideOf
    } else if (!result && WhichSide != Sidedness.InsideOf) {
      //inside
      obj.Zone.AvatarEvents ! AvatarServiceMessage(
        channel,
        AvatarAction.SendResponse(PlanetSideGUID(0), ChatMsg(ChatMessageType.UNK_229, "You are now inside"))
      )
      Sidedness.InsideOf
    } else {
      WhichSide
    }
  }
}
