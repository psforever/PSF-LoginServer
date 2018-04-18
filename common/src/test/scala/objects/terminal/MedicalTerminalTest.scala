// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.ActorRef
import net.psforever.objects.serverobject.terminals.{MedicalTerminalDefinition, ProximityTerminal, Terminal}
import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.packet.game.{ItemTransactionMessage, PlanetSideGUID}
import net.psforever.types.{CharacterGender, PlanetSideEmpire, TransactionType}
import org.specs2.mutable.Specification

class MedicalTerminalTest extends Specification {
  "MedicalTerminal" should {
    "define (a)" in {
      val a = new MedicalTerminalDefinition(38)
      a.ObjectId mustEqual 38
      a.Name mustEqual "adv_med_terminal"
    }

    "define (b)" in {
      val b = new MedicalTerminalDefinition(225)
      b.ObjectId mustEqual 225
      b.Name mustEqual "crystals_health_a"
    }

    "define (c)" in {
      val c = new MedicalTerminalDefinition(226)
      c.ObjectId mustEqual 226
      c.Name mustEqual "crystals_health_b"
    }

    "define (d)" in {
      val d = new MedicalTerminalDefinition(529)
      d.ObjectId mustEqual 529
      d.Name mustEqual "medical_terminal"
    }

    "define (e)" in {
      val e = new MedicalTerminalDefinition(689)
      e.ObjectId mustEqual 689
      e.Name mustEqual "portable_med_terminal"
    }

    "define (invalid)" in {
      var id : Int = (math.random * Int.MaxValue).toInt
      if(id == 224) {
        id += 2
      }
      else if(id == 37) {
        id += 1
      }
      else if(id == 528) {
        id += 1
      }
      else if(id == 688) {
        id += 1
      }

      new MedicalTerminalDefinition(id) must throwA[IllegalArgumentException]
    }
  }

  "Medical_Terminal" should {
    "construct" in {
      ProximityTerminal(GlobalDefinitions.medical_terminal).Actor mustEqual ActorRef.noSender
    }

    "can add a a player to a list of users" in {
      val terminal = ProximityTerminal(GlobalDefinitions.medical_terminal)
      terminal.NumberUsers mustEqual 0
      terminal.AddUser(PlanetSideGUID(10))
      terminal.NumberUsers mustEqual 1
    }

    "can remove a a player to a list of users" in {
      val terminal = ProximityTerminal(GlobalDefinitions.medical_terminal)
      terminal.AddUser(PlanetSideGUID(10))
      terminal.NumberUsers mustEqual 1
      terminal.RemoveUser(PlanetSideGUID(10))
      terminal.NumberUsers mustEqual 0
    }

    "player can not interact with the proximity terminal normally (buy)" in {
      val terminal = ProximityTerminal(GlobalDefinitions.medical_terminal)
      val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 1, "lite_armor", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }
  }
}
