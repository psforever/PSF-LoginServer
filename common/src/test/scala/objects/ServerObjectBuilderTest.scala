// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, Props}
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.objects.serverobject.builders.ServerObjectBuilder

import scala.concurrent.duration.Duration

class DoorObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.doors.Door
  import net.psforever.objects.serverobject.builders.DoorObjectBuilder
  "DoorObjectBuilder" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], DoorObjectBuilder(GlobalDefinitions.door, 1), hub), "door")
      actor ! "!"

      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Door])
      assert(reply.asInstanceOf[Door].HasGUID)
      assert(reply.asInstanceOf[Door].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class IFFLockObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.locks.IFFLock
  import net.psforever.objects.serverobject.builders.IFFLockObjectBuilder
  "IFFLockObjectBuilder" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], IFFLockObjectBuilder(GlobalDefinitions.lock_external, 1), hub), "lock")
      actor ! "!"

      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[IFFLock])
      assert(reply.asInstanceOf[IFFLock].HasGUID)
      assert(reply.asInstanceOf[IFFLock].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class ImplantTerminalMechObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
  import net.psforever.objects.serverobject.builders.ImplantTerminalMechObjectBuilder
  "IFFLockObjectBuilder" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], ImplantTerminalMechObjectBuilder(GlobalDefinitions.implant_terminal_mech, 1), hub), "mech")
      actor ! "!"

      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[ImplantTerminalMech])
      assert(reply.asInstanceOf[ImplantTerminalMech].HasGUID)
      assert(reply.asInstanceOf[ImplantTerminalMech].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class TerminalObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.terminals.Terminal
  import net.psforever.objects.serverobject.builders.TerminalObjectBuilder
  "TerminalObjectBuilder" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], TerminalObjectBuilder(GlobalDefinitions.order_terminal, 1), hub), "term")
      actor ! "!"

      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[Terminal])
      assert(reply.asInstanceOf[Terminal].HasGUID)
      assert(reply.asInstanceOf[Terminal].GUID == PlanetSideGUID(1))
      assert(reply == hub(1).get)
    }
  }
}

class VehicleSpawnPadObjectBuilderTest extends ActorTest {
  import net.psforever.objects.serverobject.pad.VehicleSpawnPad
  import net.psforever.objects.serverobject.builders.VehicleSpawnPadObjectBuilder
  "TerminalObjectBuilder" should {
    "build" in {
      val hub = ServerObjectBuilderTest.NumberPoolHub
      val actor = system.actorOf(Props(classOf[ServerObjectBuilderTest.BuilderTestActor], VehicleSpawnPadObjectBuilder(GlobalDefinitions.spawn_pad, 1), hub), "pad")
      actor ! "!"

      val reply = receiveOne(Duration.create(100, "ms"))
      assert(reply.isInstanceOf[VehicleSpawnPad])
      assert(reply.asInstanceOf[VehicleSpawnPad].HasGUID)
      assert(reply.asInstanceOf[VehicleSpawnPad].GUID == PlanetSideGUID(1))
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

