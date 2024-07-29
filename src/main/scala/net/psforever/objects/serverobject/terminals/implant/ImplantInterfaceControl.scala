// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.terminals.implant

import akka.actor.ActorRef
import net.psforever.objects.Player
import net.psforever.objects.serverobject.hackable.{GenericHackables, Hackable}
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.serverobject.terminals.{Terminal, TerminalControl}
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideGUID

object ImplantInterfaceControl {
  private def FindPairedTerminalMech(
                                      zone: Zone,
                                      interfaceGuid: PlanetSideGUID
                                    ): Option[Amenity with Hackable] = {
    zone
      .map
      .terminalToInterface
      .find { case (_, guid) => guid == interfaceGuid.guid }
      .flatMap { case (mechGuid, _) => zone.GUID(mechGuid) }
      .collect { case mech: ImplantTerminalMech if !mech.Destroyed && mech.HackedBy.isEmpty => mech }
  }
}

class ImplantInterfaceControl(private val terminal: Terminal)
  extends TerminalControl(terminal) {

  override def performHack(player: Player, data: Option[Any], replyTo: ActorRef): Unit = {
    HackableObject.HackedBy
      .orElse {
        super.performHack(player, data, replyTo)
        ImplantInterfaceControl
          .FindPairedTerminalMech(terminal.Zone, terminal.GUID)
          .foreach(GenericHackables.FinishHacking(_, player, unk = 3212836864L)())
        None
      }
  }
}
