// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, Props}
import net.psforever.objects.GlobalDefinitions.order_terminal
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.objects.serverobject.ServerObjectBuilder
import net.psforever.types.Vector3

import scala.concurrent.duration.Duration

class DoorObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.doors.Door
  "DoorObjectBuilder" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1, Door.Constructor), hub), "door")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[Door])
      assert(reply.asInstanceOf[Door].HasGUID)
      assert(reply.asInstanceOf[Door].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class IFFLockObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.locks.IFFLock
  "IFFLockObjectBuilder" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1, IFFLock.Constructor), hub), "lock")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[IFFLock])
      assert(reply.asInstanceOf[IFFLock].HasGUID)
      assert(reply.asInstanceOf[IFFLock].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class ImplantTerminalMechObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
  "IFFLockObjectBuilder" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1, ImplantTerminalMech.Constructor), hub), "mech")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[ImplantTerminalMech])
      assert(reply.asInstanceOf[ImplantTerminalMech].HasGUID)
      assert(reply.asInstanceOf[ImplantTerminalMech].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class TerminalObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.terminals.Terminal
  "TerminalObjectBuilder" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1, Terminal.Constructor(order_terminal)), hub), "term")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[Terminal])
      assert(reply.asInstanceOf[Terminal].HasGUID)
      assert(reply.asInstanceOf[Terminal].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class VehicleSpawnPadObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.pad.VehicleSpawnPad
  "TerminalObjectBuilder" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ServerObjectBuilder(1,
        VehicleSpawnPad.Constructor(Vector3(1.1f, 2.2f, 3.3f), Vector3(4.4f, 5.5f, 6.6f))
      ), hub), "pad")
      actor ! "!"

      val reply = receiveOne(Duration.create(1000, "ms"))
      assert(reply.isInstanceOf[VehicleSpawnPad])
      assert(reply.asInstanceOf[VehicleSpawnPad].HasGUID)
      assert(reply.asInstanceOf[VehicleSpawnPad].GUID == PlanetSideGUID(1))
      assert(reply.asInstanceOf[VehicleSpawnPad].Position == Vector3(1.1f, 2.2f, 3.3f))
      assert(reply.asInstanceOf[VehicleSpawnPad].Orientation == Vector3(4.4f, 5.5f, 6.6f))
      assert(reply == hub(1).get)
    }
  }
}

object ServerObjectBuilderTest {
  import net.psforever.objects.guid.source.LimitedNumberSource
  def NumberPoolHub : NumberPoolHub = {
    val obj = new NumberPoolHub(new LimitedNumberSource(2))
    obj
  }

  class BuilderTestActor(builder : ServerObjectBuilder[_], hub : NumberPoolHub) extends Actor {
    def receive : Receive = {
      case _ =>
        sender ! builder.Build(context, hub)
    }
  }
}

