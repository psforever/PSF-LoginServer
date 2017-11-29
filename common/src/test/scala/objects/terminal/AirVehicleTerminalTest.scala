// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.ActorRef
import net.psforever.objects.{GlobalDefinitions, Player, Tool}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.{ItemTransactionMessage, PlanetSideGUID}
import net.psforever.types.{CharacterGender, PlanetSideEmpire, TransactionType}
import org.specs2.mutable.Specification

class AirVehicleTerminalTest extends Specification {
  "Air_Vehicle_Terminal" should {
    val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)

    "construct" in {
      val terminal = Terminal(GlobalDefinitions.air_vehicle_terminal)
      terminal.Actor mustEqual ActorRef.noSender
    }

    "player can buy a reaver ('lightgunship')" in {
      val terminal = Terminal(GlobalDefinitions.air_vehicle_terminal)
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "lightgunship", 0, PlanetSideGUID(0))
      val reply = terminal.Request(player, msg)
      reply.isInstanceOf[Terminal.BuyVehicle] mustEqual true
      val reply2 = reply.asInstanceOf[Terminal.BuyVehicle]
      reply2.vehicle.Definition mustEqual GlobalDefinitions.lightgunship
      reply2.loadout mustEqual Nil //TODO
    }

    "player can not buy a fake vehicle ('reaver')" in {
      val terminal = Terminal(GlobalDefinitions.ground_vehicle_terminal)
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "reaver", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }
  }
}
