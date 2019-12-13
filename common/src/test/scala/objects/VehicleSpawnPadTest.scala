// Copyright (c) 2017 PSForever
package objects

import akka.actor.ActorRef
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.GlobalDefinitions
import org.specs2.mutable.Specification

class VehicleSpawnPadTest extends Specification {
  "VehicleSpawnPadDefinition" should {
    "define" in {
      GlobalDefinitions.mb_pad_creation.ObjectId mustEqual 525
    }
  }

  "VehicleSpawnPad" should {
    "construct" in {
      val obj = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
      obj.Actor mustEqual ActorRef.noSender
      obj.Definition mustEqual GlobalDefinitions.mb_pad_creation
    }
  }
}
