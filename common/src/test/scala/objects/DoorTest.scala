// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorRef, Props}
import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.objects.serverobject.doors.{Door, DoorControl}
import net.psforever.packet.game.{PlanetSideGUID, UseItemMessage}
import net.psforever.types.{CharacterGender, PlanetSideEmpire, Vector3}
import org.specs2.mutable.Specification

import scala.concurrent.duration.Duration

class DoorTest extends Specification {
  "Door" should {
    "construct" in {
      Door(GlobalDefinitions.door)
      ok
    }

    "starts as closed (false)" in {
      val door = Door(GlobalDefinitions.door)
      door.Open mustEqual false
    }

    "can be opened and closed (1; manual)" in {
      val door = Door(GlobalDefinitions.door)
      door.Open mustEqual false
      door.Open = true
      door.Open mustEqual true
      door.Open = false
      door.Open mustEqual false
    }

    "can beopened and closed (2; toggle)" in {
      val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val msg = UseItemMessage(PlanetSideGUID(6585), 0, PlanetSideGUID(372), 4294967295L, false, Vector3(5.0f,0.0f,0.0f), Vector3(0.0f,0.0f,0.0f), 11, 25, 0, 364)
      val door = Door(GlobalDefinitions.door)
      door.Open mustEqual false
      door.Use(player, msg)
      door.Open mustEqual true
      door.Use(player, msg)
      door.Open mustEqual false
    }
  }
}

class DoorControl1Test extends ActorTest() {
  "DoorControl" should {
    "construct" in {
      val door = Door(GlobalDefinitions.door)
      door.Actor = system.actorOf(Props(classOf[DoorControl], door), "door")
      assert(door.Actor != ActorRef.noSender)
    }
  }
}

class DoorControl2Test extends ActorTest() {
  "DoorControl" should {
    "open on use" in {
      val door = Door(GlobalDefinitions.door)
      door.Actor = system.actorOf(Props(classOf[DoorControl], door), "door")
      val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
      val msg = UseItemMessage(PlanetSideGUID(1), 0, PlanetSideGUID(2), 0L, false, Vector3(0f,0f,0f),Vector3(0f,0f,0f),0,0,0,0L) //faked
      assert(!door.Open)

      door.Actor ! Door.Use(player, msg)
      val reply = receiveOne(Duration.create(500, "ms"))
      assert(reply.isInstanceOf[Door.DoorMessage])
      val reply2 = reply.asInstanceOf[Door.DoorMessage]
      assert(reply2.player == player)
      assert(reply2.msg == msg)
      assert(reply2.response == Door.OpenEvent())
      assert(door.Open)
    }
  }
}

class DoorControl3Test extends ActorTest() {
  "DoorControl" should {
    "do nothing if given garbage" in {
      val door = Door(GlobalDefinitions.door)
      door.Actor = system.actorOf(Props(classOf[DoorControl], door), "door")
      assert(!door.Open)

      door.Actor ! "trash"
      val reply = receiveOne(Duration.create(500, "ms"))
      assert(reply.isInstanceOf[Door.NoEvent])
      assert(!door.Open)
    }
  }
}
