// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.{ActorSystem, Props}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.{Avatar, GlobalDefinitions, Player}
import net.psforever.objects.serverobject.terminals._
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterGender, PlanetSideEmpire}
import objects.ActorTest

import scala.concurrent.duration.Duration

class ProximityTerminalControl1Test extends ActorTest() {
  "ProximityTerminalControl" should {
    "construct (medical terminal)" in {
      val terminal = ProximityTerminal(GlobalDefinitions.medical_terminal)
      terminal.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], terminal), "test-term")
    }
  }
}

class ProximityTerminalControl2Test extends ActorTest() {
  "ProximityTerminalControl can not process wrong messages" in {
    val (_, terminal) = TerminalControlTest.SetUpAgents(GlobalDefinitions.medical_terminal, PlanetSideEmpire.TR)

    terminal.Actor !"hello"
    expectNoMsg(Duration.create(500, "ms"))
  }
}

//terminal control is mostly a pass-through actor for Terminal.Exchange messages, wrapped in Terminal.TerminalMessage protocol
class MedicalTerminalControl1Test extends ActorTest() {
  "ProximityTerminalControl sends a message to the first new user only" in {
    val (player, terminal) = ProximityTerminalControlTest.SetUpAgents(GlobalDefinitions.medical_terminal, PlanetSideEmpire.TR)
    player.GUID = PlanetSideGUID(10)
    val player2 = Player(Avatar("someothertest", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
    player2.GUID = PlanetSideGUID(11)

    terminal.Actor ! CommonMessages.Use(player)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player)
    assert(reply2.msg == null)
    assert(reply2.response.isInstanceOf[Terminal.StartProximityEffect])
    assert(reply2.response.asInstanceOf[Terminal.StartProximityEffect].terminal == terminal)
    assert(terminal.NumberUsers == 1)

    terminal.Actor ! CommonMessages.Use(player2)
    expectNoMsg(Duration.create(500, "ms"))
    assert(terminal.NumberUsers == 2)
  }
}

class MedicalTerminalControl2Test extends ActorTest() {
  "ProximityTerminalControl sends a message to the last user only" in {
    val (player, terminal) : (Player, ProximityTerminal) = ProximityTerminalControlTest.SetUpAgents(GlobalDefinitions.medical_terminal, PlanetSideEmpire.TR)
    player.GUID = PlanetSideGUID(10)
    val player2 = Player(Avatar("someothertest", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
    player2.GUID = PlanetSideGUID(11)

    terminal.Actor ! CommonMessages.Use(player)
    receiveOne(Duration.create(500, "ms"))
    terminal.Actor ! CommonMessages.Use(player2)
    expectNoMsg(Duration.create(500, "ms"))
    assert(terminal.NumberUsers == 2)

    terminal.Actor ! CommonMessages.Unuse(player)
    expectNoMsg(Duration.create(500, "ms"))
    assert(terminal.NumberUsers == 1)

    terminal.Actor ! CommonMessages.Unuse(player2)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player2)
    assert(reply2.msg == null)
    assert(reply2.response.isInstanceOf[Terminal.StopProximityEffect])
    assert(reply2.response.asInstanceOf[Terminal.StopProximityEffect].terminal == terminal)
    assert(terminal.NumberUsers == 0)
  }
}

class MedicalTerminalControl3Test extends ActorTest() {
  "ProximityTerminalControl sends a message to the last user only (confirmation of test #2)" in {
    val (player, terminal) : (Player, ProximityTerminal) = ProximityTerminalControlTest.SetUpAgents(GlobalDefinitions.medical_terminal, PlanetSideEmpire.TR)
    player.GUID = PlanetSideGUID(10)
    val player2 = Player(Avatar("someothertest", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
    player2.GUID = PlanetSideGUID(11)

    terminal.Actor ! CommonMessages.Use(player)
    receiveOne(Duration.create(500, "ms"))
    terminal.Actor ! CommonMessages.Use(player2)
    expectNoMsg(Duration.create(500, "ms"))
    assert(terminal.NumberUsers == 2)

    terminal.Actor ! CommonMessages.Unuse(player2)
    expectNoMsg(Duration.create(500, "ms"))
    assert(terminal.NumberUsers == 1)

    terminal.Actor ! CommonMessages.Unuse(player)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player) //important!
    assert(reply2.msg == null)
    assert(reply2.response.isInstanceOf[Terminal.StopProximityEffect])
    assert(reply2.response.asInstanceOf[Terminal.StopProximityEffect].terminal == terminal)
    assert(terminal.NumberUsers == 0)
  }
}

object ProximityTerminalControlTest {
  def SetUpAgents(tdef : MedicalTerminalDefinition, faction : PlanetSideEmpire.Value)(implicit system : ActorSystem) : (Player, ProximityTerminal) = {
    val terminal = ProximityTerminal(tdef)
    terminal.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], terminal), "test-term")
    (Player(Avatar("test", faction, CharacterGender.Male, 0, 0)), terminal)
  }
}
