package net.psforever.objects.serverobject.terminals.capture

import akka.actor.Actor.Receive
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

/**
  * The behaviours corresponding to an Amenity that is marked as being CaptureTerminalAware
  * @see CaptureTerminalAware
  */
trait CaptureTerminalAwareBehavior {
  def CaptureTerminalAwareObject : Amenity with CaptureTerminalAware

  val captureTerminalAwareBehaviour: Receive = {
    case CaptureTerminalAwareBehavior.TerminalStatusChanged(terminal, isResecured) =>
      isResecured match {
        case true => ; // CC is resecured
        case false => // CC is hacked
          // Remove seated occupants for mountables
          CaptureTerminalAwareObject match {
            case mountable: Mountable =>

              val guid = mountable.GUID
              val zone = mountable.Zone
              val zoneId = zone.id
              val events = zone.VehicleEvents

              mountable.Seats.values.zipWithIndex.foreach {
                case (seat, seat_num) =>
                  seat.occupant match {
                    case Some(player) =>
                      seat.unmount(player)
                      player.VehicleSeated = None
                      if (player.HasGUID) {
                        events ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(player.GUID, seat_num, true, guid))
                      }
                    case None => ;
                  }
              }
            case _ =>
          }
      }
  }
}

object CaptureTerminalAwareBehavior {
  final case class TerminalStatusChanged(terminal: CaptureTerminal, isResecured: Boolean)
}
