// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._
/**
  * An `Enumeration` of the available certifications.<br>
  * <br>
  * As indicated, the following certifications are always enqueued on an avatar's permissions:
  * `StandardAssault`, `StandardExoSuit`, `AgileExoSuit`.
  * They must still be included in any formal lists of permitted equipment for a user.
  * The other noted certifications require all prerequisite certifications listed or they themselves will not be listed:
  * `ElectronicsExpert` and `AdvancedEngineering`.
  * No other certification requires its prerequisites explicitly listed to be listed itself.
  * Any certification that contains multiple other certifications overrides those individual certifications in the list.
  * There is no certification for the Advanced Nanite Transport.<br>
  * <br>
  * In terms of pricing, `StandardAssault`, `StandardExoSuit`, and `AgileExoSuit` are costless.
  * A certification that contains multiple other certifications acts as the overriding cost.
  * (Taking `UniMAX` while owning `AAMAX` will refund the `AAMAX` cost and replace it with the `UniMAX` cost.)
  */
object CertificationType extends Enumeration {
  type Type = Value
  val
  //0
  StandardAssault, //always listed
  MediumAssault,
  HeavyAssault,
  SpecialAssault,
  AntiVehicular,
  Sniping,
  EliteAssault,
  AirCalvaryScout,
  AirCalvaryInterceptor,
  AirCalvaryAssault,
  //10
  AirSupport,
  ATV,
  LightScout,
  AssaultBuggy,
  ArmoredAssault1,
  ArmoredAssault2,
  GroundTransport,
  GroundSupport,
  BattleFrameRobotics,
  Flail,
  //20
  Switchblade,
  Harasser,
  Phantasm,
  GalaxyGunship,
  BFRAntiAircraft,
  BFRAntiInfantry,
  StandardExoSuit, //always listed
  AgileExoSuit, //always listed
  ReinforcedExoSuit,
  InfiltrationSuit,
  //30
  AAMAX,
  AIMAX,
  AVMAX,
  UniMAX,
  Medical,
  AdvancedMedical,
  Hacking,
  AdvancedHacking,
  ExpertHacking,
  DataCorruption,
  //40
  ElectronicsExpert, //requires Hacking and AdvancedHacking
  Engineering,
  CombatEngineering,
  FortificationEngineering,
  AssaultEngineering,
  AdvancedEngineering //requires Engineering and CombatEngineering
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint8L)
}
