// Copyright (c) 2017 PSForever
package objects.terminal

import net.psforever.objects.serverobject.terminals.{MatrixTerminalDefinition, Terminal}
import net.psforever.objects.{Avatar, GlobalDefinitions, Player, Vehicle}
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.types._
import org.specs2.mutable.Specification

class MatrixTerminalTest extends Specification {
  "MatrixTerminal" should {
    "define" in {
      val a = new MatrixTerminalDefinition(517)
      a.ObjectId mustEqual 517
    }

    "creation" in {
      Terminal(new MatrixTerminalDefinition(518))
      ok
    }

    "invalid message" in {
      val player = Player(Avatar("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 1, "lite_armor", 0, PlanetSideGUID(0))
      val terminal = Terminal(new MatrixTerminalDefinition(519))
      terminal.Owner = Vehicle(GlobalDefinitions.quadstealth)
      terminal.Owner.Faction = PlanetSideEmpire.TR
      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }
  }
}
