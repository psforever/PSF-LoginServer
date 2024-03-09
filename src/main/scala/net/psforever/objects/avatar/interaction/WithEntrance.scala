// Copyright (c) 2024 PSForever
package net.psforever.objects.avatar.interaction

import net.psforever.objects.serverobject.doors.{Door, InteriorDoorPassage}
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentTrait, PieceOfEnvironment, interaction}
import net.psforever.objects.serverobject.environment.interaction.{InteractionWith, RespondsToZoneEnvironment}
import net.psforever.objects.serverobject.interior.Sidedness
import net.psforever.objects.zones.InteractsWithZone
import net.psforever.types.Vector3

import scala.concurrent.duration._

class WithEntrance(val channel: String)
  extends InteractionWith {
  val attribute: EnvironmentTrait = EnvironmentAttribute.InteriorField

  private var stopTest: Boolean = false
  private var sideAware: Sidedness = Sidedness.InBetweenSides

  def doInteractingWith(
                         obj: InteractsWithZone,
                         body: PieceOfEnvironment,
                         data: Option[Any]
                       ): Unit = {
    if (stopTest && data.contains("bellybutton")) {
      stopTest = false
    } else {
      val door = body.asInstanceOf[InteriorDoorPassage].door
      if (door.isOpen) {
        sideAware = Sidedness.InBetweenSides
      } else {
        performInteriorCheck(obj, door)
      }
      obj.Actor ! RespondsToZoneEnvironment.Timer(attribute, delay = 250 milliseconds, obj.Actor, interaction.InteractingWithEnvironment(body, Some("bellybutton")))
    }
  }

  override def stopInteractingWith(
                                    obj: InteractsWithZone,
                                    body: PieceOfEnvironment,
                                    data: Option[Any]
                                  ): Unit = {
    performInteriorCheck(obj, body.asInstanceOf[InteriorDoorPassage].door)
    stopTest = true
  }

  private def performInteriorCheck(
                                    obj: InteractsWithZone,
                                    door: Door
                                  ): Sidedness = {
    import net.psforever.packet.game.ChatMsg
    import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
    import net.psforever.types.{ChatMessageType, PlanetSideGUID}
    val result = Vector3.DotProduct(Vector3.Unit(obj.Position - door.Position), door.Outwards) > 0f
    if (result && sideAware != Sidedness.OutsideOf) {
      //outside
      sideAware = Sidedness.OutsideOf
      obj.Zone.AvatarEvents ! AvatarServiceMessage(
        channel,
        AvatarAction.SendResponse(PlanetSideGUID(0), ChatMsg(ChatMessageType.UNK_229, "You are now outside"))
      )
    } else if (!result && sideAware != Sidedness.InsideOf) {
      //inside
      sideAware = Sidedness.InsideOf
      obj.Zone.AvatarEvents ! AvatarServiceMessage(
        channel,
        AvatarAction.SendResponse(PlanetSideGUID(0), ChatMsg(ChatMessageType.UNK_229, "You are now inside"))
      )
    }
    sideAware
  }

  def ThisSide: Sidedness = sideAware

  def ThisSide_=(thisSide: Sidedness): Unit = {
    sideAware = thisSide
    ThisSide
  }
}
