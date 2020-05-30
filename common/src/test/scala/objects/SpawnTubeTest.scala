// Copyright (c) 2017 PSForever
package objects

import akka.actor.Props
import base.ActorTest
import net.psforever.objects.{Default, GlobalDefinitions}
import net.psforever.objects.serverobject.tube.{SpawnTube, SpawnTubeControl, SpawnTubeDefinition}
import org.specs2.mutable.Specification

class SpawnTubeTest extends Specification {
  "SpawnTubeDefinition" should {
    "define" in {
      val obj = new SpawnTubeDefinition(49)
      obj.ObjectId mustEqual 49
    }
  }

  "SpawnTube" should {
    "construct" in {
      val obj = SpawnTube(GlobalDefinitions.ams_respawn_tube)
      obj.Actor mustEqual Default.Actor
      obj.Definition mustEqual GlobalDefinitions.ams_respawn_tube
    }
  }
}

class SpawnTubeControlTest extends ActorTest {
  "SpawnTubeControl" should {
    "construct" in {
      val obj = SpawnTube(GlobalDefinitions.ams_respawn_tube)
      obj.Actor = system.actorOf(Props(classOf[SpawnTubeControl], obj), "spawn-tube")
      assert(obj.Actor != Default.Actor)
    }
  }
}
