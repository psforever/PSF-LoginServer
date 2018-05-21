// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.Props
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.terminals.Terminal.TerminalMessage
import net.psforever.objects.serverobject.terminals.{ProximityTerminal, ProximityTerminalControl, ProximityUnit, Terminal}
import net.psforever.objects.{Avatar, GlobalDefinitions, Player}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterGender, PlanetSideEmpire}
import objects.ActorTest
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class ProximityTest extends Specification {
  "ProximityUnit" should {
    "construct (with a Terminal object)" in {
      val obj = new ProximityTest.SampleTerminal()
      obj.NumberUsers mustEqual 0
    }

    "keep track of users (add)" in {
      val obj = new ProximityTest.SampleTerminal()
      obj.NumberUsers mustEqual 0
      obj.AddUser(PlanetSideGUID(10)) mustEqual obj.NumberUsers
      obj.NumberUsers mustEqual 1
      obj.AddUser(PlanetSideGUID(20)) mustEqual obj.NumberUsers
      obj.NumberUsers mustEqual 2
    }

    "keep track of users (remove)" in {
      val obj = new ProximityTest.SampleTerminal()
      obj.AddUser(PlanetSideGUID(10))
      obj.AddUser(PlanetSideGUID(20))
      obj.NumberUsers mustEqual 2
      obj.RemoveUser(PlanetSideGUID(10)) mustEqual obj.NumberUsers
      obj.NumberUsers mustEqual 1
      obj.RemoveUser(PlanetSideGUID(20)) mustEqual obj.NumberUsers
      obj.NumberUsers mustEqual 0
    }

    "can not add a user twice" in {
      val obj = new ProximityTest.SampleTerminal()
      obj.AddUser(PlanetSideGUID(10))
      obj.NumberUsers mustEqual 1
      obj.AddUser(PlanetSideGUID(10))
      obj.NumberUsers mustEqual 1
    }

    "can not remove a user that was not added" in {
      val obj = new ProximityTest.SampleTerminal()
      obj.AddUser(PlanetSideGUID(10))
      obj.NumberUsers mustEqual 1
      obj.RemoveUser(PlanetSideGUID(20))
      obj.NumberUsers mustEqual 1
    }
  }

  "ProximityTerminal" should {
    "construct" in {
      ProximityTerminal(GlobalDefinitions.medical_terminal)
      ok
    }
  }
}

class ProximityTerminalControl1bTest extends ActorTest {
  "ProximityTerminalControl" should {
    "send out a start message" in {
      val obj = ProximityTerminal(GlobalDefinitions.medical_terminal)
      obj.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], obj), "prox-ctrl")
      val player = Player(Avatar("TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
      player.GUID = PlanetSideGUID(10)

      assert(obj.NumberUsers == 0)
      obj.Actor ! CommonMessages.Use(player)
      val msg = receiveOne(200 milliseconds)
      assert(obj.NumberUsers == 1)
      assert(msg.isInstanceOf[TerminalMessage])
      val msgout = msg.asInstanceOf[TerminalMessage]
      assert(msgout.player == player)
      assert(msgout.msg == null)
      assert(msgout.response.isInstanceOf[Terminal.StartProximityEffect])
    }
  }
}

class ProximityTerminalControl2bTest extends ActorTest {
  "ProximityTerminalControl" should {
    "will not send out one start message unless first user" in {
      val obj = ProximityTerminal(GlobalDefinitions.medical_terminal)
      obj.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], obj), "prox-ctrl")
      val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
      player1.GUID = PlanetSideGUID(10)
      val player2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
      player2.GUID = PlanetSideGUID(11)
      assert(obj.NumberUsers == 0)

      obj.Actor ! CommonMessages.Use(player1)
      val msg = receiveOne(200 milliseconds)
      assert(obj.NumberUsers == 1)
      assert(msg.isInstanceOf[TerminalMessage])
      assert(msg.asInstanceOf[TerminalMessage].response.isInstanceOf[Terminal.StartProximityEffect])
      obj.Actor ! CommonMessages.Use(player2)
      expectNoMsg(500 milliseconds)
      assert(obj.NumberUsers == 2)
    }
  }
}

class ProximityTerminalControl3bTest extends ActorTest {
  "ProximityTerminalControl" should {
    "send out a stop message" in {
      val obj = ProximityTerminal(GlobalDefinitions.medical_terminal)
      obj.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], obj), "prox-ctrl")
      val player = Player(Avatar("TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
      player.GUID = PlanetSideGUID(10)

      assert(obj.NumberUsers == 0)
      obj.Actor ! CommonMessages.Use(player)
      receiveOne(200 milliseconds)
      assert(obj.NumberUsers == 1)
      obj.Actor ! CommonMessages.Unuse(player)
      val msg = receiveOne(200 milliseconds)
      assert(obj.NumberUsers == 0)
      assert(msg.isInstanceOf[TerminalMessage])
      val msgout = msg.asInstanceOf[TerminalMessage]
      assert(msgout.player == player)
      assert(msgout.msg == null)
      assert(msgout.response.isInstanceOf[Terminal.StopProximityEffect])
    }
  }
}

class ProximityTerminalControl4bTest extends ActorTest {
  "ProximityTerminalControl" should {
    "will not send out one stop message until last user" in {
      val obj = ProximityTerminal(GlobalDefinitions.medical_terminal)
      obj.Actor = system.actorOf(Props(classOf[ProximityTerminalControl], obj), "prox-ctrl")
      val player1 = Player(Avatar("TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
      player1.GUID = PlanetSideGUID(10)
      val player2 = Player(Avatar("TestCharacter2", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0))
      player2.GUID = PlanetSideGUID(11)
      assert(obj.NumberUsers == 0)

      obj.Actor ! CommonMessages.Use(player1)
      receiveOne(200 milliseconds) //StartProximityEffect
      assert(obj.NumberUsers == 1)
      obj.Actor ! CommonMessages.Use(player2)
      expectNoMsg(500 milliseconds)
      assert(obj.NumberUsers == 2)
      obj.Actor ! CommonMessages.Unuse(player1)
      expectNoMsg(500 milliseconds)
      assert(obj.NumberUsers == 1)
      obj.Actor ! CommonMessages.Unuse(player2)
      val msg = receiveOne(200 milliseconds)
      assert(obj.NumberUsers == 0)
      assert(msg.isInstanceOf[TerminalMessage])
      val msgout = msg.asInstanceOf[TerminalMessage]
      assert(msgout.player == player2)
      assert(msgout.msg == null)
      assert(msgout.response.isInstanceOf[Terminal.StopProximityEffect])
    }
  }
}

object ProximityTest {
  class SampleTerminal extends Terminal(GlobalDefinitions.dropship_vehicle_terminal) with ProximityUnit
}
