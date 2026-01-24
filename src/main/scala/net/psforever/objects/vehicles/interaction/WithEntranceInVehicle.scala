// Copyright (c) 2024 PSForever
package net.psforever.objects.vehicles.interaction

import net.psforever.objects.Vehicle
import net.psforever.objects.avatar.interaction.WithEntrance
import net.psforever.objects.serverobject.doors.InteriorDoorPassage
import net.psforever.objects.serverobject.environment.PieceOfEnvironment
import net.psforever.objects.zones.interaction.InteractsWithZone

class WithEntranceInVehicle
  extends WithEntrance() {
  private var warningLevel: Int = 0
  private var lastWarning: Long = 0L

  override def doInteractingWith(obj: InteractsWithZone, body: PieceOfEnvironment, data: Option[Any]): Unit = {
    super.doInteractingWith(obj, body, data)
    if (warningLevel == -1) {
      warnAboutProximity(obj, msg = "@InvalidTerrain_VehicleNowSafe")
      warningLevel = 0
    } else if (!body.asInstanceOf[InteriorDoorPassage].door.Definition.Name.contains("garage")) {
      val curr = System.currentTimeMillis()
      if (curr - lastWarning >= 5000L) {
        if (warningLevel > 3) {
          import scala.concurrent.duration._
          obj.Actor ! Vehicle.Deconstruct(Some(2.seconds))
        } else if (warningLevel > 0) {
          warnAboutProximity(obj, msg = "@InvalidTerrain_VehicleWillDeconstruct")
        }
        lastWarning = curr
        warningLevel += 1
      }
    }
  }

  override def stopInteractingWith(obj: InteractsWithZone, body: PieceOfEnvironment, data: Option[Any]): Unit = {
    super.stopInteractingWith(obj, body, data)
    warningLevel = -1
  }

  private def warnAboutProximity(obj: InteractsWithZone, msg: String): Unit = {
    import net.psforever.packet.game.ChatMsg
    import net.psforever.services.Service
    import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
    import net.psforever.types.ChatMessageType
    obj.Zone.AvatarEvents ! AvatarServiceMessage(
      obj.Actor.toString(),
      AvatarAction.SendResponse(ChatMsg(ChatMessageType.UNK_227, msg))
    )
  }
}
