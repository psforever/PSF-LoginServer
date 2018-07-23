// Copyright (c) 2017 PSForever
package net.psforever.objects.avatar

import net.psforever.types.CertificationType

import scala.collection.mutable

object Certification {
  object Dependencies {
    /**
      * Find the certifications that are immediately dependent on the target certification.
      * (For `A`, find all `B` that are `B ⇒ A`.)
      * @param certification the target certification
      * @return all connected certifications
      */
    def From(certification : CertificationType.Value) : Set[CertificationType.Value] = dependencies(certification).toSet

    /**
      * Find all certifications that are dependent on the target certification.
      * (For `A`, find all `B...C` where `C ⇒ B` and `B ⇒ A`.)
      * @param certification the target certification
      * @return all connected certifications
      */
    def FromAll(certification : CertificationType.Value) : Set[CertificationType.Value] = {
      var available : List[CertificationType.Value] = List(certification)
      var allocated : mutable.ListBuffer[CertificationType.Value] = mutable.ListBuffer.empty[CertificationType.Value]
      do {
        available = available.flatMap(cert => dependencies(cert))
        allocated ++= available
      }
      while(available.nonEmpty)
      allocated.toSet
    }

    /**
      * Find the certifications that are immediate dependencies of the target certification.
      * (For `A`, find all `B` where `A ⇒ B`.)
      * @param certification the target certification
      * @return all connected certifications
      */
    def For(certification : CertificationType.Value) : Set[CertificationType.Value] = {
      (for {
        (cert, certs) <- dependencies
        if certs contains certification
      } yield cert).toSet
    }

    /**
      * Find all certifications that are dependencies of the target certification.
      * (For `A`, find all `B...C` where `A ⇒ B` and `B ⇒ C`.)
      * @param certification the target certification
      * @return all connected certifications
      */
    def ForAll(certification : CertificationType.Value) : Set[CertificationType.Value] = {
      var available : List[CertificationType.Value] = List(certification)
      var allocated : mutable.ListBuffer[CertificationType.Value] = mutable.ListBuffer.empty[CertificationType.Value]
      do {
        available = available.flatMap {
          For
        }
        allocated ++= available
      }
      while(available.nonEmpty)
      allocated.toSet
    }

    import CertificationType._

    /**
      * Find all certifications that are related but mutually exclusive with the target certification.
      * (For `A`, find all `B` that `B ⊃ A` but `A XOR B`.)
      * @param certification the target certification
      * @return all connected certifications
      */
    def Like(certification : CertificationType.Value) : Set[CertificationType.Value] = certification match {
      case AssaultBuggy =>
        Set(Harasser)
      case LightScout =>
        Set(AirCavalryScout, AssaultBuggy, Harasser)
      case UniMAX =>
        Set(AAMAX, AIMAX, AVMAX)
      case AdvancedEngineering =>
        Set(AssaultEngineering, FortificationEngineering)
      case ElectronicsExpert =>
        Set(DataCorruption, ExpertHacking)
      case _ =>
        Set.empty[CertificationType.Value]
    }

    private val dependencies : Map[CertificationType.Value, List[CertificationType.Value]] = Map(
      StandardAssault -> List(),
      AgileExoSuit -> List(),
      ReinforcedExoSuit -> List(),
      InfiltrationSuit -> List(Phantasm),
      AIMAX -> List(),
      AVMAX -> List(),
      AAMAX -> List(),
      UniMAX -> List(),

      StandardAssault -> List(),
      MediumAssault -> List(AntiVehicular, HeavyAssault, Sniping, SpecialAssault),
      AntiVehicular -> List(),
      HeavyAssault -> List(),
      Sniping -> List(),
      SpecialAssault -> List(EliteAssault),
      EliteAssault -> List(),

      ATV -> List(Switchblade),
      Switchblade -> List(),
      Harasser -> List(),
      AssaultBuggy -> List(),
      LightScout -> List(AirCavalryAssault),
      GroundSupport -> List(),
      GroundTransport -> List(),
      ArmoredAssault1 -> List(ArmoredAssault2),
      ArmoredAssault2 -> List(BattleFrameRobotics, Flail),
      Flail -> List(),

      AirCavalryScout -> List(AirCavalryAssault),
      AirCavalryAssault -> List(AirCavalryInterceptor),
      AirCavalryInterceptor -> List(),
      AirSupport -> List(GalaxyGunship),
      GalaxyGunship -> List(),
      Phantasm -> List(),

      BattleFrameRobotics -> List(BFRAntiInfantry, BFRAntiAircraft),
      BFRAntiInfantry -> List(),
      BFRAntiAircraft -> List(),

      Medical -> List(AdvancedMedical),
      AdvancedMedical -> List(),
      Engineering -> List(CombatEngineering),
      CombatEngineering -> List(AdvancedEngineering, AssaultEngineering, FortificationEngineering),
      AdvancedEngineering -> List(),
      AssaultEngineering -> List(),
      FortificationEngineering -> List(),
      Hacking -> List(AdvancedHacking),
      AdvancedHacking -> List(DataCorruption, ElectronicsExpert, ExpertHacking),
      DataCorruption -> List(),
      ElectronicsExpert -> List(),
      ExpertHacking -> List()
    )
  }

  object Cost {
    /**
      * For a certification, get its point cost.
      * @param certification the certification
      * @return the cost
      */
    def Of(certification : CertificationType.Value) : Int = points(certification)

    /**
      * For a list of certifications, find the point cost of all unique certifications.
      * @see `Of(Set)`
      * @param certifications the certification list
      * @return the total cost
      */
    def Of(certifications : List[CertificationType.Value]) : Int = Of(certifications.toSet)

    /**
      * For a set of certifications, find the point cost of all certifications.
      * @see `OfAll(List)`
      * @param certifications the certification list
      * @return the total cost
      */
    def Of(certifications : Set[CertificationType.Value]) : Int = OfAll(certifications.toList)

    /**
      * For a list of certifications, find the point cost of all certifications, counting any duplicates.
      * @param certifications the certification list
      * @return the total cost
      */
    def OfAll(certifications : List[CertificationType.Value]) : Int = {
      certifications map points sum
    }

    import CertificationType._
    private val points : Map[CertificationType.Value, Int] = Map(
      StandardExoSuit -> 0,
      AgileExoSuit -> 0,
      ReinforcedExoSuit -> 3,
      InfiltrationSuit -> 2,
      AAMAX -> 2,
      AIMAX -> 3,
      AVMAX -> 3,
      UniMAX -> 6,

      StandardAssault -> 0,
      MediumAssault -> 2,
      AntiVehicular -> 3,
      HeavyAssault -> 4,
      Sniping -> 3,
      SpecialAssault -> 3,
      EliteAssault -> 1,

      ATV -> 1,
      Switchblade -> 1,
      Harasser -> 1,
      AssaultBuggy -> 3,
      LightScout -> 5,
      GroundSupport -> 2,
      GroundTransport -> 2,
      ArmoredAssault1 -> 2,
      ArmoredAssault2 -> 3,
      Flail -> 1,

      AirCavalryScout -> 3,
      AirCavalryAssault -> 2,
      AirCavalryInterceptor -> 2,
      AirSupport -> 3,
      GalaxyGunship -> 2,
      Phantasm -> 3,

      BattleFrameRobotics -> 4,
      BFRAntiInfantry -> 1,
      BFRAntiAircraft -> 1,

      Medical -> 3,
      AdvancedMedical -> 2,
      Engineering -> 3,
      CombatEngineering -> 2,
      AdvancedEngineering -> 5,
      AssaultEngineering -> 3,
      FortificationEngineering -> 3,
      Hacking -> 3,
      AdvancedHacking -> 2,
      DataCorruption -> 3,
      ElectronicsExpert -> 4,
      ExpertHacking -> 2
    )
  }
}
