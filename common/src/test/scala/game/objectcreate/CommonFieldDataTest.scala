// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable._

class CommonFieldDataTest extends Specification {
  "CommonFieldData" should {
    "construct" in {
      CommonFieldData(PlacementData(0f, 0f, 0f), PlanetSideEmpire.NC, true, 5) mustEqual
      CommonFieldData(PlacementData(0f, 0f, 0f), PlanetSideEmpire.NC, false, true, 5, false, PlanetSideGUID(0))
    }
  }
}
