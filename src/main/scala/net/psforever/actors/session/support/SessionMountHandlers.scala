// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, typed}
import net.psforever.objects.Tool
import net.psforever.objects.vehicles.MountableWeapons
import net.psforever.packet.game.{DismountVehicleCargoMsg, InventoryStateMessage, MountVehicleCargoMsg, MountVehicleMsg}
//
import net.psforever.actors.session.AvatarActor
import net.psforever.objects.Player
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.packet.game.DismountVehicleMsg

trait MountHandlerFunctions extends CommonSessionInterfacingFunctionality {
  val ops: SessionMountHandlers

  def handleMountVehicle(pkt: MountVehicleMsg): Unit

  def handleDismountVehicle(pkt: DismountVehicleMsg): Unit

  def handleMountVehicleCargo(pkt: MountVehicleCargoMsg): Unit

  def handleDismountVehicleCargo(pkt: DismountVehicleCargoMsg): Unit

  def handle(tplayer: Player, reply: Mountable.Exchange): Unit
}

class SessionMountHandlers(
                            val sessionLogic: SessionData,
                            val avatarActor: typed.ActorRef[AvatarActor.Command],
                            implicit val context: ActorContext
                          ) extends CommonSessionInterfacingFunctionality {
  /**
   * From a mount, find the weapon controlled from it, and update the ammunition counts for that weapon's magazines.
   * @param objWithSeat the object that owns seats (and weaponry)
   * @param seatNum the mount
   */
  def updateWeaponAtSeatPosition(objWithSeat: MountableWeapons, seatNum: Int): Unit = {
    objWithSeat.WeaponControlledFromSeat(seatNum) foreach {
      case weapon: Tool =>
        //update mounted weapon belonging to mount
        weapon.AmmoSlots.foreach(slot => {
          //update the magazine(s) in the weapon, specifically
          val magazine = slot.Box
          sendResponse(InventoryStateMessage(magazine.GUID, weapon.GUID, magazine.Capacity.toLong))
        })
      case _ => () //no weapons to update
    }
  }
}
