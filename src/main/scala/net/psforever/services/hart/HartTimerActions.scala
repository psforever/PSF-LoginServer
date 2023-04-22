// Copyright (c) 2021 PSForever
package net.psforever.services.hart

import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.shuttle.OrbitalShuttlePad
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

object HartTimerActions {
  /**
    * Update the shuttle's mounted arrangement with the pad, setting the state.
    * @param pad the orbital shuttle pad
    * @param shuttle the orbital shuttle pad's shuttle
    * @param toChannel to whom these messages will be dispatched
    */
  def ShuttleDocked(pad: OrbitalShuttlePad, shuttle: Vehicle, toChannel: String): Unit = {
    val zone = pad.Zone
    if(toChannel.equals(zone.id)) {
      shuttle.MountedIn = pad.GUID
    }
    zone.LocalEvents ! LocalServiceMessage(
      toChannel,
      LocalAction.ShuttleDock(pad.GUID, shuttle.GUID, 3)
    )
  }

  /**
    * Update the shuttle's mounted arrangement with the pad, undoing any connection.
    * @param pad the orbital shuttle pad
    * @param shuttle the orbital shuttle pad's shuttle
    * @param toChannel to whom these messages will be dispatched
    */
  def ShuttleFreeFromDock(pad: OrbitalShuttlePad, shuttle: Vehicle, toChannel: String): Unit = {
    val zone = pad.Zone
    if(toChannel.equals(zone.id)) {
      shuttle.MountedIn = None
    }
    zone.LocalEvents ! LocalServiceMessage(
      toChannel,
      LocalAction.ShuttleUndock(pad.GUID, shuttle.GUID, shuttle.Position, shuttle.Orientation)
    )
  }

  /**
    * Update the shuttle's flight state.
    * @param pad the orbital shuttle pad
    * @param shuttle the orbital shuttle pad's shuttle
    * @param toChannel to whom these messages will be dispatched
    */
  def ShuttleStateUpdate(pad: OrbitalShuttlePad, shuttle: Vehicle, toChannel: String, state: Int): Unit = {
    val zone = pad.Zone
    if(toChannel.equals(zone.id)) {
      shuttle.Flying = state
    }
    zone.LocalEvents ! LocalServiceMessage(
      toChannel,
      LocalAction.ShuttleState(shuttle.GUID, shuttle.Position, shuttle.Orientation, state)
    )
  }
}
