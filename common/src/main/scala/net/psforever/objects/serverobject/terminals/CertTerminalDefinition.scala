// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.types.CertificationType

/**
  * The definition for any `Terminal` that is of a type "cert_terminal" (certification terminal).
  * `Learn` and `Sell` `CertificationType` entries, gaining access to different `Equipment` and `Vehicles`.
  */
class CertTerminalDefinition extends TerminalDefinition(171) {
  Name = "cert_terminal"

  /**
    * The certifications available.
    * All entries are listed on page (tab) number 0.
    */
  private val certificationList : Map[String, (CertificationType.Value, Int)] = Map(
    "medium_assault" -> (CertificationType.MediumAssault, 2),
    "reinforced_armor" -> (CertificationType.ReinforcedExoSuit, 3),
    "quad_all" -> (CertificationType.ATV, 1),
    "switchblade" -> (CertificationType.Switchblade, 1),
    "harasser" -> (CertificationType.Harasser, 1),
    "anti_vehicular" -> (CertificationType.AntiVehicular, 3),
    "heavy_assault" -> (CertificationType.HeavyAssault, 4),
    "sniper" -> (CertificationType.Sniping, 3),
    "special_assault" -> (CertificationType.SpecialAssault, 3),
    "special_assault_2" -> (CertificationType.EliteAssault, 1),
    "infiltration_suit" -> (CertificationType.InfiltrationSuit, 2),
    "max_anti_personnel" -> (CertificationType.AIMAX, 3),
    "max_anti_vehicular" -> (CertificationType.AVMAX, 3),
    "max_anti_aircraft" -> (CertificationType.AAMAX, 2),
    "max_all" -> (CertificationType.UniMAX, 6),
    "air_cavalry_scout" -> (CertificationType.AirCavalryScout, 3),
    "air_cavalry_assault" -> (CertificationType.AirCavalryAssault, 2),
    "air_cavalry_interceptor" -> (CertificationType.AirCavalryInterceptor, 2),
    "air_support" -> (CertificationType.AirSupport, 3),
    "gunship" -> (CertificationType.GalaxyGunship, 2),
    "phantasm" -> (CertificationType.Phantasm, 3),
    "armored_assault1" -> (CertificationType.ArmoredAssault1, 2),
    "armored_assault2" -> (CertificationType.ArmoredAssault2, 1),
    "flail" -> (CertificationType.Flail, 1),
    "assault_buggy" -> (CertificationType.AssaultBuggy, 3),
    "ground_support" -> (CertificationType.GroundSupport, 2),
    "ground_transport" -> (CertificationType.GroundTransport, 2),
    "light_scout" -> (CertificationType.LightScout, 5),
    "Repair" -> (CertificationType.Engineering, 3),
    "combat_engineering" -> (CertificationType.CombatEngineering, 2),
    "ce_offense" -> (CertificationType.AssaultEngineering, 3),
    "ce_defense" -> (CertificationType.FortificationEngineering, 3),
    "ce_advanced" -> (CertificationType.AdvancedEngineering, 5),
    "Hacking" -> (CertificationType.Hacking, 3),
    "advanced_hacking" -> (CertificationType.AdvancedHacking, 2),
    "expert_hacking" -> (CertificationType.ExpertHacking, 2),
    "virus_hacking" -> (CertificationType.DataCorruption, 3),
    "electronics_expert" -> (CertificationType.ElectronicsExpert, 4),
    "Medical" -> (CertificationType.Medical, 3),
    "advanced_medical" -> (CertificationType.AdvancedMedical, 2)
    //TODO bfr certification entries
  )

  /**
    * Process a `TransactionType.Learn` action by the user.
    * @param player the player
    * @param msg the original packet carrying the request
    * @return an actionable message that explains how to process the request
    */
  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = { //Learn
    certificationList.get(msg.item_name) match {
      case Some((cert, cost)) =>
        Terminal.LearnCertification(cert, cost)
      case None =>
        Terminal.NoDeal()
    }
  }

  /**
    * Process a `TransactionType.Sell` action by the user.
    * @param player the player
    * @param msg the original packet carrying the request
    * @return an actionable message that explains how to process the request
    */
  def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    certificationList.get(msg.item_name) match {
      case Some((cert, cost)) =>
        Terminal.SellCertification(cert, cost)
      case None =>
        Terminal.NoDeal()
    }
  }

  /**
    * This action is not supported by this type of `Terminal`.
    * @param player the player
    * @param msg the original packet carrying the request
    * @return `Terminal.NoEvent` always
    */
  def Loadout(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
}
