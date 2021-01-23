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
          if (CaptureTerminalAwareObject.isInstanceOf[Mountable]) {
            CaptureTerminalAwareObject.asInstanceOf[Mountable].Seats.filter(x => x._2.isOccupied).foreach(x => {
              val (seat_num, seat) = x
              CaptureTerminalAwareObject.Zone.VehicleEvents ! VehicleServiceMessage(
                CaptureTerminalAwareObject.Zone.id,
                VehicleAction.KickPassenger(seat.Occupant.get.GUID, seat_num, true, CaptureTerminalAwareObject.GUID))

              seat.Occupant = None
            })
          }
      }
  }
}

object CaptureTerminalAwareBehavior {
  final case class TerminalStatusChanged(terminal: CaptureTerminal, isResecured: Boolean)
}
