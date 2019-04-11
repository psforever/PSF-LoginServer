// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.types.CertificationType

object CertTerminalDefinition {
  /**
    * The certifications available.
    * All entries are listed on page (tab) number 0.
    */
  val certs : Map[String, CertificationType.Value] = Map(
    "medium_assault" -> CertificationType.MediumAssault,
    "reinforced_armor" -> CertificationType.ReinforcedExoSuit,
    "quad_all" -> CertificationType.ATV,
    "switchblade" -> CertificationType.Switchblade,
    "harasser" -> CertificationType.Harasser,
    "anti_vehicular" -> CertificationType.AntiVehicular,
    "heavy_assault" -> CertificationType.HeavyAssault,
    "sniper" -> CertificationType.Sniping,
    "special_assault" -> CertificationType.SpecialAssault,
    "special_assault_2" -> CertificationType.EliteAssault,
    "infiltration_suit" -> CertificationType.InfiltrationSuit,
    "max_anti_personnel" -> CertificationType.AIMAX,
    "max_anti_vehicular" -> CertificationType.AVMAX,
    "max_anti_aircraft" -> CertificationType.AAMAX,
    "max_all" -> CertificationType.UniMAX,
    "air_cavalry_scout" -> CertificationType.AirCavalryScout,
    "air_cavalry_assault" -> CertificationType.AirCavalryAssault,
    "air_cavalry_interceptor" -> CertificationType.AirCavalryInterceptor,
    "air_support" -> CertificationType.AirSupport,
    "gunship" -> CertificationType.GalaxyGunship,
    "phantasm" -> CertificationType.Phantasm,
    "armored_assault1" -> CertificationType.ArmoredAssault1,
    "armored_assault2" -> CertificationType.ArmoredAssault2,
    "flail" -> CertificationType.Flail,
    "assault_buggy" -> CertificationType.AssaultBuggy,
    "ground_support" -> CertificationType.GroundSupport,
    "ground_transport" -> CertificationType.GroundTransport,
    "light_scout" -> CertificationType.LightScout,
    "Repair" -> CertificationType.Engineering,
    "combat_engineering" -> CertificationType.CombatEngineering,
    "ce_offense" -> CertificationType.AssaultEngineering,
    "ce_defense" -> CertificationType.FortificationEngineering,
    "ce_advanced" -> CertificationType.AdvancedEngineering,
    "Hacking" -> CertificationType.Hacking,
    "advanced_hacking" -> CertificationType.AdvancedHacking,
    "expert_hacking" -> CertificationType.ExpertHacking,
    "virus_hacking" -> CertificationType.DataCorruption,
    "electronics_expert" -> CertificationType.ElectronicsExpert,
    "Medical" -> CertificationType.Medical,
    "advanced_medical" -> CertificationType.AdvancedMedical
    //TODO bfr certification entries
  )
}
