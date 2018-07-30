// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.avatar.Certification
import net.psforever.types.CertificationType._
import org.specs2.mutable.Specification

class CertificationTest extends Specification {
  "Dependencies" should {
    //From
    "find any certifications immediately dependent on a given certification (nothing)" in {
      Certification.Dependencies.From(StandardAssault) mustEqual Set()
    }

    "find any certifications immediately dependent on a given certification (one)" in {
      Certification.Dependencies.From(Engineering) mustEqual Set(CombatEngineering)
    }

    "find any certifications immediately dependent on a given certification (multiple)" in {
      Certification.Dependencies.From(MediumAssault) mustEqual Set(AntiVehicular, HeavyAssault, Sniping, SpecialAssault)
    }

    "find any certifications immediately dependent on a given certification (intermediate)" in {
      Certification.Dependencies.From(ArmoredAssault2) mustEqual Set(BattleFrameRobotics, Flail)
    }
    //FromAll
    "find all certifications dependent on a given certification (nothing)" in {
      Certification.Dependencies.FromAll(StandardAssault) mustEqual Set()
    }

    "find all certifications dependent on a given certification (one)" in {
      Certification.Dependencies.FromAll(ATV) mustEqual Set(Switchblade)
    }

    "find all certifications dependent on a given certification (multiple)" in {
      Certification.Dependencies.FromAll(MediumAssault) mustEqual Set(AntiVehicular, HeavyAssault, Sniping, SpecialAssault, EliteAssault)
    }

    "find all certifications dependent on a given certification (intermediate)" in {
      Certification.Dependencies.FromAll(ArmoredAssault2) mustEqual Set(BattleFrameRobotics, Flail, BFRAntiInfantry, BFRAntiAircraft)
    }
    //For
    "find any certifications that are immediate dependencies for a given certification (nothing)" in {
      Certification.Dependencies.For(StandardAssault) mustEqual Set()
    }

    "find any certifications that are immediate dependencies for a given certification (one)" in {
      Certification.Dependencies.For(CombatEngineering) mustEqual Set(Engineering)
    }

    "find any certifications that are immediate dependencies for a given certification (multiple)" in {
      Certification.Dependencies.For(AirCavalryAssault) mustEqual Set(AirCavalryScout, LightScout)
    }

    "find any certifications that are immediate dependencies for a given certification (intermediate)" in {
      Certification.Dependencies.For(BattleFrameRobotics) mustEqual Set(ArmoredAssault2)
    }
    //ForAll
    "find all certifications that are dependencies for a given certification (nothing)" in {
      Certification.Dependencies.ForAll(StandardAssault) mustEqual Set()
    }

    "find all certifications that are dependencies for a given certification (one)" in {
      Certification.Dependencies.ForAll(CombatEngineering) mustEqual Set(Engineering)
    }

    "find all certifications that are dependencies for a given certification (multiple)" in {
      Certification.Dependencies.ForAll(AirCavalryAssault) mustEqual Set(AirCavalryScout, LightScout)
    }

    "find all certifications that are dependencies for a given certification (intermediate)" in {
      Certification.Dependencies.ForAll(BattleFrameRobotics) mustEqual Set(ArmoredAssault1, ArmoredAssault2)
    }
    //Like
    "find related certifications" in {
      Certification.Dependencies.Like(AssaultBuggy) mustEqual Set(Harasser)
      Certification.Dependencies.Like(LightScout) mustEqual Set(AirCavalryScout, AssaultBuggy, Harasser)
      Certification.Dependencies.Like(UniMAX) mustEqual Set(AIMAX, AVMAX, AAMAX)
      Certification.Dependencies.Like(StandardAssault) mustEqual Set()
    }
  }

  "Cost" should {
    "calculate the point-value of any certification (no value)" in {
      Certification.Cost.Of(StandardAssault) mustEqual 0
    }

    "calculate the point-value of any certification (value)" in {
      Certification.Cost.Of(MediumAssault) mustEqual 2
    }

    "calculate the sum-point-value of all certifications (no value)" in {
      Certification.Cost.Of(Set(StandardAssault)) mustEqual 0
    }

    "calculate the sum-point-value of all certifications (value)" in {
      Certification.Cost.Of(Set(MediumAssault)) mustEqual 2
    }

    "calculate the sum-point-value of all certifications (add)" in {
      Certification.Cost.Of(Set(StandardAssault, MediumAssault)) mustEqual 2
      Certification.Cost.Of(Set(HeavyAssault, MediumAssault)) mustEqual 6
    }

    "calculate the sum-point-value of all certifications (large collection)" in {
      Certification.Cost.Of(Set(StandardAssault, MediumAssault, StandardExoSuit, AgileExoSuit, ReinforcedExoSuit, ATV, Harasser)) mustEqual 7
    }

    "calculate the sum-point-value of all unique certifications (no value)" in {
      Certification.Cost.Of(List(StandardAssault, StandardAssault)) mustEqual 0
    }

    "calculate the sum-point-value of all unique certifications (value)" in {
      Certification.Cost.Of(List(MediumAssault, MediumAssault)) mustEqual 2
    }

    "calculate the sum-point-value of all unique certifications (add)" in {
      Certification.Cost.Of(List(StandardAssault, MediumAssault, MediumAssault)) mustEqual 2
      Certification.Cost.Of(List(HeavyAssault, MediumAssault, HeavyAssault)) mustEqual 6
    }

    "calculate the sum-point-value of all unique certifications (large collection)" in {
      Certification.Cost.Of(
        List(
          StandardAssault, MediumAssault, StandardExoSuit, AgileExoSuit, ReinforcedExoSuit, ATV, Harasser,
          MediumAssault, StandardExoSuit, ReinforcedExoSuit, ATV
        )
      ) mustEqual 7
    }

    "calculate the sum-point-value of all certifications (count duplicates)" in {
      Certification.Cost.OfAll(
        List(
          StandardAssault, MediumAssault, StandardExoSuit, AgileExoSuit, ReinforcedExoSuit, ATV, Harasser,
          MediumAssault, StandardExoSuit, ReinforcedExoSuit, ATV
        )
      ) mustEqual 13
    }
  }
}
