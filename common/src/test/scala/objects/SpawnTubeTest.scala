// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorRef, Props}
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.tube.{SpawnTube, SpawnTubeControl, SpawnTubeDefinition}
import org.specs2.mutable.Specification

class SpawnTubeTest extends Specification {
  "SpawnTubeDefinition" should {
    "define (ams_respawn_tube)" in {
      val obj = new SpawnTubeDefinition(49)
      obj.ObjectId mustEqual 49
      obj.Name mustEqual "ams_respawn_tube"
    }

    "define (respawn_tube)" in {
      val obj = new SpawnTubeDefinition(732)
      obj.ObjectId mustEqual 732
      obj.Name mustEqual "respawn_tube"
    }

    "define (respawn_tube_tower)" in {
      val obj = new SpawnTubeDefinition(733)
      obj.ObjectId mustEqual 733
      obj.Name mustEqual "respawn_tube_tower"
    }

    "define (invalid)" in {
      var id : Int = (math.random * Int.MaxValue).toInt
      if(id == 49 || id == 733) {
        id += 1
      }
      else if(id == 732) {
        id += 2
      }

      new SpawnTubeDefinition(id) must throwA[IllegalArgumentException]
    }
  }

  "SpawnTube" should {
    "construct" in {
      val obj = SpawnTube(GlobalDefinitions.ams_respawn_tube)
      obj.Actor mustEqual ActorRef.noSender
      obj.Definition mustEqual GlobalDefinitions.ams_respawn_tube
    }
  }
}

class SpawnTubeControlTest extends ActorTest() {
  "SpawnTubeControl" should {
    "construct" in {
      val obj = SpawnTube(GlobalDefinitions.ams_respawn_tube)
      obj.Actor = system.actorOf(Props(classOf[SpawnTubeControl], obj), "spawn-tube")
      assert(obj.Actor != ActorRef.noSender)
    }
  }
}
