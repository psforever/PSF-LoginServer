// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.{Default, GlobalDefinitions}
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
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
      obj.Actor mustEqual Default.Actor
      obj.Definition mustEqual GlobalDefinitions.mb_pad_creation
    }
  }
}
