// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.ActorRef
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.packet.game.{ItemTransactionMessage, PlanetSideGUID}
import net.psforever.types._
import org.specs2.mutable.Specification

class CertTerminalTest extends Specification {
  "Cert_Terminal" should {
    val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)

    "construct" in {
      val terminal = Terminal(GlobalDefinitions.cert_terminal)
      terminal.Actor mustEqual ActorRef.noSender
    }

    "player can learn a certification ('medium_assault')" in {
      val terminal = Terminal(GlobalDefinitions.cert_terminal)
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Learn, 0, "medium_assault", 0, PlanetSideGUID(0))
      terminal.Request(player, msg) mustEqual Terminal.LearnCertification(CertificationType.MediumAssault, 2)
    }

    "player can not learn a fake certification ('juggling')" in {
      val terminal = Terminal(GlobalDefinitions.cert_terminal)
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Learn, 0, "juggling", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }

    "player can forget a certification ('medium_assault')" in {
      val terminal = Terminal(GlobalDefinitions.cert_terminal)
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Sell, 0, "medium_assault", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.SellCertification(CertificationType.MediumAssault, 2)
    }

    "player can not forget a fake certification ('juggling')" in {
      val terminal = Terminal(GlobalDefinitions.cert_terminal)
      val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Sell, 0, "juggling", 0, PlanetSideGUID(0))

      terminal.Request(player, msg) mustEqual Terminal.NoDeal()
    }
  }
}
