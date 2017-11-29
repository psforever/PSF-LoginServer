// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.Props
import net.psforever.objects.serverobject.terminals.{Terminal, TerminalControl}
import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.packet.game.{ItemTransactionMessage, PlanetSideGUID}
import net.psforever.types._
import objects.ActorTest

import scala.concurrent.duration.Duration

class TerminalControlTest extends ActorTest() {
  "TerminalControl" should {
    "construct (cert terminal)" in {
      val terminal = Terminal(GlobalDefinitions.cert_terminal)
      terminal.Actor = system.actorOf(Props(classOf[TerminalControl], terminal), "test-cert-term")
    }
  }
}

//terminal control is mostly a pass-through actor for Terminal.Exchange messages, wrapped in Terminal.TerminalMessage protocol
//test for Cert_Terminal messages (see CertTerminalTest)
class CertTerminalControl1Test extends ActorTest() {
  "TerminalControl can be used to learn a certification ('medium_assault')" in {
    val terminal = Terminal(GlobalDefinitions.cert_terminal)
    terminal.Actor = system.actorOf(Props(classOf[TerminalControl], terminal), "test-cert-term")
    val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
    val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Learn, 0, "medium_assault", 0, PlanetSideGUID(0))

    terminal.Actor ! Terminal.Request(player, msg)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player)
    assert(reply2.msg == msg)
    assert(reply2.response == Terminal.LearnCertification(CertificationType.MediumAssault, 2))
  }
}

class CertTerminalControl2Test extends ActorTest() {
  "TerminalControl can be used to warn about not learning a fake certification ('juggling')" in {
    val terminal = Terminal(GlobalDefinitions.cert_terminal)
    terminal.Actor = system.actorOf(Props(classOf[TerminalControl], terminal), "test-cert-term")
    val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
    val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Learn, 0, "juggling", 0, PlanetSideGUID(0))

    terminal.Actor ! Terminal.Request(player, msg)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player)
    assert(reply2.msg == msg)
    assert(reply2.response == Terminal.NoDeal())
  }
}

class CertTerminalControl3Test extends ActorTest() {
  "TerminalControl can be used to forget a certification ('medium_assault')" in {
    val terminal = Terminal(GlobalDefinitions.cert_terminal)
    terminal.Actor = system.actorOf(Props(classOf[TerminalControl], terminal), "test-cert-term")
    val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
    val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Sell, 0, "medium_assault", 0, PlanetSideGUID(0))

    terminal.Actor ! Terminal.Request(player, msg)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player)
    assert(reply2.msg == msg)
    assert(reply2.response == Terminal.SellCertification(CertificationType.MediumAssault, 2))
  }
}

class VehicleTerminalControl1Test extends ActorTest() {
  "TerminalControl can be used to buy a vehicle ('two_man_assault_buggy')" in {
    val terminal = Terminal(GlobalDefinitions.ground_vehicle_terminal)
    terminal.Actor = system.actorOf(Props(classOf[TerminalControl], terminal), "test-cert-term")
    val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
    val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "two_man_assault_buggy", 0, PlanetSideGUID(0))

    terminal.Actor ! Terminal.Request(player, msg)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player)
    assert(reply2.msg == msg)
    assert(reply2.response.isInstanceOf[Terminal.BuyVehicle])
    val reply3 = reply2.response.asInstanceOf[Terminal.BuyVehicle]
    assert(reply3.vehicle.Definition == GlobalDefinitions.two_man_assault_buggy)
    assert(reply3.loadout == Nil) //TODO
  }
}

class VehicleTerminalControl2Test extends ActorTest() {
  "TerminalControl can be used to warn about not buy a vehicle ('harasser')" in {
    val terminal = Terminal(GlobalDefinitions.ground_vehicle_terminal)
    terminal.Actor = system.actorOf(Props(classOf[TerminalControl], terminal), "test-cert-term")
    val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
    val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "harasser", 0, PlanetSideGUID(0))

    terminal.Actor ! Terminal.Request(player, msg)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player)
    assert(reply2.msg == msg)
    assert(reply2.response == Terminal.NoDeal())
  }
}
