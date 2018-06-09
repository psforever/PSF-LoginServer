// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.ActorRef
import net.psforever.objects.serverobject.terminals.{MatrixTerminalDefinition, Terminal}
import net.psforever.objects.{Avatar, GlobalDefinitions, Player, Vehicle}
import net.psforever.packet.game.{ItemTransactionMessage, PlanetSideGUID}
import net.psforever.types._
import org.specs2.mutable.Specification

class MatrixTerminalTest extends Specification {
  "MatrixTerminal" should {
    "define (a)" in {
      val a = new MatrixTerminalDefinition(517)
      a.ObjectId mustEqual 517
      a.Name mustEqual "matrix_terminala"
    }

    "define (b)" in {
      val b = new MatrixTerminalDefinition(518)
      b.ObjectId mustEqual 518
      b.Name mustEqual "matrix_terminalb"
    }

    "define (c)" in {
      val b = new MatrixTerminalDefinition(519)
      b.ObjectId mustEqual 519
      b.Name mustEqual "matrix_terminalc"
    }

    "define (d)" in {
      val b = new MatrixTerminalDefinition(812)
      b.ObjectId mustEqual 812
      b.Name mustEqual "spawn_terminal"
    }

    "define (invalid)" in {
      var id : Int = (math.random * Int.MaxValue).toInt
      if(id == 517) {
        id += 3
      }
      else if(id == 518) {
        id += 2
      }
      else if(id == 519 | id == 812) {
        id += 1
      }

      new MatrixTerminalDefinition(id) must throwA[IllegalArgumentException]
    }
  }

  "Matrix_Terminal" should {
    val terminal = Terminal(GlobalDefinitions.matrix_terminalc)
    terminal.Owner = Vehicle(GlobalDefinitions.quadstealth)
    terminal.Owner.Faction = PlanetSideEmpire.TR

    "construct" in {
      terminal.Actor mustEqual ActorRef.noSender
    }

    "player can not buy (anything)" in {
      val player = Player(Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 1, "lite_armor", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }
  }
}
