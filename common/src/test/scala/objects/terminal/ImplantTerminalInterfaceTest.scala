// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.ActorRef
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.{Avatar, GlobalDefinitions, Player}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{ItemTransactionMessage, PlanetSideGUID}
import net.psforever.types.{CharacterGender, PlanetSideEmpire, TransactionType}
import org.specs2.mutable.Specification

class ImplantTerminalInterfaceTest extends Specification {
  "Implant_Terminal_Interface" should {
    val player = Player(Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
    val terminal = Terminal(GlobalDefinitions.implant_terminal_interface)
    terminal.Owner = new Building(0, Zone.Nowhere)
    terminal.Owner.Faction = PlanetSideEmpire.TR

    "construct" in {
      val terminal = Terminal(GlobalDefinitions.implant_terminal_interface)
      terminal.Actor mustEqual ActorRef.noSender
    }

    "player can learn an implant ('darklight_vision')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "darklight_vision", 0, PlanetSideGUID(0))

      val reply = terminal.Request(player, msg)
      reply.isInstanceOf[Terminal.LearnImplant] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.LearnImplant]
      reply2.implant mustEqual GlobalDefinitions.darklight_vision
    }

    "player can not learn a fake implant ('aimbot')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "aimbot", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }

    "player can surrender an implant ('darklight_vision')" in {
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Sell, 0, "darklight_vision", 0, PlanetSideGUID(0))

      val reply = terminal.Request(player, msg)
      reply.isInstanceOf[Terminal.SellImplant] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.SellImplant]
      reply2.implant mustEqual GlobalDefinitions.darklight_vision
    }

    "player can not surrender a fake implant ('aimbot')" in {
      val terminal = Terminal(GlobalDefinitions.implant_terminal_interface)
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Sell, 0, "aimbot", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }
  }
}
