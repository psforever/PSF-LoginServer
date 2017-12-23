// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.game.objectcreate._
import net.psforever.types.MeritCommendation
import org.specs2.mutable._

class RibbonBarsTest extends Specification {
  "RibbonBars" should {
    "construct" in {
      RibbonBars()
      ok
    }

    "construct (custom)" in {
      RibbonBars(
        MeritCommendation.MarkovVeteran,
        MeritCommendation.HeavyInfantry4,
        MeritCommendation.TankBuster7,
        MeritCommendation.SixYearTR
      )
      ok
    }

    "size" in {
      RibbonBars().bitsize mustEqual 128L

      RibbonBars(
        MeritCommendation.MarkovVeteran,
        MeritCommendation.HeavyInfantry4,
        MeritCommendation.TankBuster7,
        MeritCommendation.SixYearTR
      ).bitsize mustEqual 128L
    }
  }
}
