// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.TestProbe
import akka.actor.typed.scaladsl.adapter._
import base.ActorTest
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.Player
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.mount._
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.types.{CharacterSex, CharacterVoice, PlanetSideEmpire, PlanetSideGUID}

import scala.concurrent.duration.Duration

class MountableControl1Test extends ActorTest {
  "MountableControl" should {
    "construct" in {
      val obj = new MountableTest.MountableTestObject
      obj.Actor = system.actorOf(Props(classOf[MountableTest.MountableTestControl], obj), "mech")
      assert(obj.Actor != ActorRef.noSender)
    }
  }
}

class MountableControl2Test extends ActorTest {
  "MountableControl" should {
    "let a player mount" in {
      val player = Player(Avatar(0, "test", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      val obj    = new MountableTest.MountableTestObject
      obj.Zone = new Zone("test", new ZoneMap("test"), 0) {
        override def SetupNumberPools() = {}
        this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
      }
      obj.Actor = system.actorOf(Props(classOf[MountableTest.MountableTestControl], obj), "mountable")
      val msg = Mountable.TryMount(player, 0)

      obj.Actor ! msg
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Mountable.MountMessages])
      val reply2 = reply.asInstanceOf[Mountable.MountMessages]
      assert(reply2.player == player)
      assert(reply2.response.isInstanceOf[Mountable.CanMount])
      val reply3 = reply2.response.asInstanceOf[Mountable.CanMount]
      assert(reply3.obj == obj)
      assert(reply3.seat_number == 0)
    }
  }
}

class MountableControl3Test extends ActorTest {
  "MountableControl" should {
    "block a player from mounting" in {
      val player1 = Player(Avatar(0, "test1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      val player2 = Player(Avatar(1, "test2", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      val obj     = new MountableTest.MountableTestObject
      obj.Actor = system.actorOf(Props(classOf[MountableTest.MountableTestControl], obj), "mountable")
      obj.Actor ! Mountable.TryMount(player1, 0)
      receiveOne(Duration.create(100, "ms")) //consume reply

      obj.Actor ! Mountable.TryMount(player2, 0)
      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Mountable.MountMessages])
      val reply2 = reply.asInstanceOf[Mountable.MountMessages]
      assert(reply2.player == player2)
      assert(reply2.response.isInstanceOf[Mountable.CanNotMount])
      val reply3 = reply2.response.asInstanceOf[Mountable.CanNotMount]
      assert(reply3.obj == obj)
      assert(reply3.mount_point == 0)
    }
  }
}

object MountableTest {
  class MountableTestObject extends PlanetSideServerObject with Mountable {
    seats += 0 -> new Seat(new SeatDefinition())
    GUID = PlanetSideGUID(1) //eh whatever
    def Faction = PlanetSideEmpire.TR
    def Definition = new ObjectDefinition(1) with MountableDefinition {
      MountPoints += 0 -> MountInfo(0)
    }
  }

  class MountableTestControl(obj: PlanetSideServerObject with Mountable)
      extends Actor
      with MountableBehavior {
    override def MountableObject = obj

    def receive: Receive = mountBehavior.orElse(dismountBehavior)
  }
}
