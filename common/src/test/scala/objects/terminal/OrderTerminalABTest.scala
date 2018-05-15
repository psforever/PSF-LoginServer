// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.ActorRef
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.serverobject.terminals.{OrderTerminalABDefinition, Terminal}
import net.psforever.objects.zones.Zone
import net.psforever.objects.{Avatar, GlobalDefinitions, Player}
import net.psforever.packet.game.{ItemTransactionMessage, PlanetSideGUID}
import net.psforever.types._
import org.specs2.mutable.Specification

class OrderTerminalABTest extends Specification {
  "OrderTerminalAB" should {
    "define (a)" in {
      val a = new OrderTerminalABDefinition(613)
      a.ObjectId mustEqual 613
      a.Name mustEqual "order_terminala"
    }

    "define (b)" in {
      val b = new OrderTerminalABDefinition(614)
      b.ObjectId mustEqual 614
      b.Name mustEqual "order_terminalb"
    }

    "define (invalid)" in {
      var id : Int = (math.random * Int.MaxValue).toInt
      if(id == 613) {
        id += 2
      }
      else if(id == 614) {
        id += 1
      }

      new OrderTerminalABDefinition(id) must throwA[IllegalArgumentException]
    }
  }

  "Order_Terminal" should {
    val terminal = Terminal(GlobalDefinitions.order_terminala)
    terminal.Owner = new Building(0, Zone.Nowhere, StructureType.Building)
    terminal.Owner.Faction = PlanetSideEmpire.TR

    "construct" in {
      terminal.Actor mustEqual ActorRef.noSender
    }

    "player can buy different armor ('lite_armor')" in {
      val player = Player(Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 1, "lite_armor", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.BuyExosuit(ExoSuitType.Agile)
    }

    "player can buy max armor ('trhev_antiaircraft')" in {
      val player = Player(Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 1, "trhev_antiaircraft", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }
    //TODO loudout tests

    "player can not load max loadout" in {
      val avatar = Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val player = Player(avatar)
      avatar.SaveLoadout(player, "test1", 0)
      player.ExoSuit = ExoSuitType.MAX
      avatar.SaveLoadout(player, "test2", 1)

      val msg1 = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Loadout, 4, "", 0, PlanetSideGUID(0))
      terminal.Request(player, msg1) mustEqual Terminal.InfantryLoadout(ExoSuitType.Standard, 0, Nil, Nil)

      val msg2 = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Loadout, 4, "", 1, PlanetSideGUID(0))
      terminal.Request(player, msg2) mustEqual Terminal.NoDeal()
    }
  }
}
