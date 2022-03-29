// Copyright (c) 2022 PSForever
package net.psforever.objects.avatar

import enumeratum.values.{StringEnum, StringEnumEntry}
import net.psforever.types.{MeritCommendation, PlanetSideEmpire}

/**
  * Awards organize merit commendations into an iterative series that have their own accomplishment statistics.
  * @see `MeritCommendation.Value`
  * @param commendation the merit commendation attached to this rank
  */
final case class CommendationRank(commendation: MeritCommendation.Value)

/**
  * A unique accomplishment that statuses positive player behavior and
  * assigns classification and advancement metrics to the accomplishment.
  * @param value the specific label that indicates this award
  * @param category a generalization of the award's accreditation
  * @param progression the iterative merit commendations associated with increasing ranks of this award
  */
sealed abstract class Award(
                             val value: String,
                             val category: AwardCategory.Value,
                             val progression: List[CommendationRank]
                           ) extends StringEnumEntry {
  assert(
    Merit.values.exists(_.toString().equals(value)),
    s"$value is not a merit found in the appropriate Enumeration"
  )
  assert(
    progression.nonEmpty,
    s"$value must define a base progression rank"
  )

  def alignment: PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
}

sealed abstract class TerranRepublicAward(
                                           override val value: String,
                                           override val category: AwardCategory.Value,
                                           override val progression: List[CommendationRank]
                                         ) extends Award(value, category, progression) {
  override def alignment: PlanetSideEmpire.Value = PlanetSideEmpire.TR
}

sealed abstract class NewConglomerateAward(
                                            override val value: String,
                                            override val category: AwardCategory.Value,
                                            override val progression: List[CommendationRank]
                                          ) extends Award(value, category, progression) {
  override def alignment: PlanetSideEmpire.Value = PlanetSideEmpire.NC
}

sealed abstract class VanuSovereigntyAward(
                                            override val value: String,
                                            override val category: AwardCategory.Value,
                                            override val progression: List[CommendationRank]
                                          ) extends Award(value, category, progression) {
  override def alignment: PlanetSideEmpire.Value = PlanetSideEmpire.VS
}

/**
  * A collection of all award labels.
  */
object Merit extends Enumeration {
  val AdvancedMedic = Value("AdvancedMedic")
  val AdvancedMedicAssists = Value("AdvancedMedicAssists")
  val AirDefender = Value("AirDefender")
  val AMSSupport = Value("AMSSupport")
  val AntiVehicular = Value("AntiVehicular")
  val Avenger = Value("Avenger")
  val BendingMovieActor = Value("BendingMovieActor")
  val BFRAdvanced = Value("BFRAdvanced")
  val BFRBuster = Value("BFRBuster")
  val BlackOpsHunter = Value("BlackOpsHunter")
  val BlackOpsParticipant = Value("BlackOpsParticipant")
  val BlackOpsVictory = Value("BlackOpsVictory")
  val Bombadier = Value("Bombadier")
  val BomberAce = Value("BomberAce")
  val Boomer = Value("Boomer")
  val CalvaryDriver = Value("CalvaryDriver")
  val CalvaryPilot = Value("CalvaryPilot")
  val CMTopOutfit = Value("CMTopOutfit")
  val CombatMedic = Value("CombatMedic")
  val CombatRepair = Value("CombatRepair")
  val ContestFirstBR40 = Value("ContestFirstBR40")
  val ContestMovieMaker = Value("ContestMovieMaker")
  val ContestMovieMakerOutfit = Value("ContestMovieMakerOutfit")
  val ContestPlayerOfTheMonth = Value("ContestPlayerOfTheMonth")
  val ContestPlayerOfTheYear = Value("ContestPlayerOfTheYear")
  val CSAppreciation = Value("CSAppreciation")
  val DefenseNC = Value("DefenseNC")
  val DefenseTR = Value("DefenseTR")
  val DefenseVS = Value("DefenseVS")
  val DevilDogsMovie = Value("DevilDogsMovie")
  val DogFighter = Value("DogFighter")
  val DriverGunner = Value("DriverGunner")
  val EliteAssault = Value("EliteAssault")
  val EmeraldVeteran = Value("EmeraldVeteran")
  val Engineer = Value("Engineer")
  val EquipmentSupport = Value("EquipmentSupport")
  val EventNC = Value("EventNC")
  val EventNCCommander = Value("EventNCCommander")
  val EventNCElite = Value("EventNCElite")
  val EventNCSoldier = Value("EventNCSoldier")
  val EventTR = Value("EventTR")
  val EventTRCommander = Value("EventTRCommander")
  val EventTRElite = Value("EventTRElite")
  val EventTRSoldier = Value("EventTRSoldier")
  val EventVS = Value("EventVS")
  val EventVSCommander = Value("EventVSCommander")
  val EventVSElite = Value("EventVSElite")
  val EventVSSoldier = Value("EventVSSoldier")
  val Explorer = Value("Explorer")
  val FanFaire2005Commander = Value("FanFaire2005Commander")
  val FanFaire2005Soldier = Value("FanFaire2005Soldier")
  val FanFaire2006Atlanta = Value("FanFaire2006Atlanta")
  val FanFaire2007 = Value("FanFaire2007")
  val FanFaire2008 = Value("FanFaire2008")
  val FanFaire2009 = Value("FanFaire2009")
  val GalaxySupport = Value("GalaxySupport")
  val Grenade = Value("Grenade")
  val GroundGunner = Value("GroundGunner")
  val HackingSupport = Value("HackingSupport")
  val HalloweenMassacre2006NC = Value("HalloweenMassacre2006NC")
  val HalloweenMassacre2006TR = Value("HalloweenMassacre2006TR")
  val HalloweenMassacre2006VS = Value("HalloweenMassacre2006VS")
  val HeavyAssault = Value("HeavyAssault")
  val HeavyInfantry = Value("HeavyInfantry")
  val InfantryExpert = Value("InfantryExpert")
  val Jacking = Value("Jacking")
  val KnifeCombat = Value("KnifeCombat")
  val LightInfantry = Value("LightInfantry")
  val LockerCracker = Value("LockerCracker")
  val LodestarSupport = Value("LodestarSupport")
  val Loser = Value("Loser")
  val MarkovVeteran = Value("MarkovVeteran")
  val Max = Value("Max")
  val MaxBuster = Value("MaxBuster")
  val MediumAssault = Value("MediumAssault")
  val None = Value("None") //the no merit commendation award label
  val Orion = Value("Orion")
  val Osprey = Value("Osprey")
  val Phalanx = Value("Phalanx")
  val PSUMaAttendee = Value("PSUMaAttendee")
  val PSUMbAttendee = Value("PSUMbAttendee")
  val QAAppreciation = Value("QAAppreciation")
  val ReinforcementHackSpecialist = Value("ReinforcementHackSpecialist")
  val ReinforcementInfantrySpecialist = Value("ReinforcementInfantrySpecialist")
  val ReinforcementSpecialist = Value("ReinforcementSpecialist")
  val ReinforcementVehicleSpecialist = Value("ReinforcementVehicleSpecialist")
  val RouterSupport = Value("RouterSupport")
  val RouterTelepadDeploy = Value("RouterTelepadDeploy")
  val ScavengerNC = Value("ScavengerNC")
  val ScavengerTR = Value("ScavengerTR")
  val ScavengerVS = Value("ScavengerVS")
  val Sniper = Value("Sniper")
  val SpecialAssault = Value("SpecialAssault")
  val StandardAssault = Value("StandardAssault")
  val StracticsHistorian = Value("StracticsHistorian")
  val Supply = Value("Supply")
  val TankBuster = Value("TankBuster")
  val TermOfServiceNC = Value("TermOfServiceNC")
  val TermOfServiceTR = Value("TermOfServiceTR")
  val TermOfServiceVS = Value("TermOfServiceVS")
  val TinyRoboticSupport = Value("TinyRoboticSupport")
  val Transport = Value("Transport")
  val TransportationCitation = Value("TransportationCitation")
  val ValentineFemale = Value("ValentineFemale")
  val ValentineMale = Value("ValentineMale")
  val WernerVeteran = Value("WernerVeteran")
  val XmasGingerman = Value("XmasGingerman")
  val XmasSnowman = Value("XmasSnowman")
  val XmasSpirit = Value("XmasSpirit")
}

/**
  * A collection of all award categories.
  */
object AwardCategory extends Enumeration {
  val
  Activity,
  Defense,
  Exclusive,
  Support,
  Vehicular,
  Weaponry
  = Value
}

object Award extends StringEnum[Award] {
  import net.psforever.types.MeritCommendation._

  val values = findValues

  case object AdvancedMedic extends Award(
    value = "AdvancedMedic",
    AwardCategory.Support,
    List(
      CommendationRank(AdvancedMedic1),
      CommendationRank(AdvancedMedic2),
      CommendationRank(AdvancedMedic3),
      CommendationRank(AdvancedMedic4),
      CommendationRank(AdvancedMedic5),
      CommendationRank(AdvancedMedic6),
      CommendationRank(AdvancedMedic7)
    )
  )
  case object AdvancedMedicAssists extends Award(
    value = "AdvancedMedicAssists",
    AwardCategory.Support,
    List(
      CommendationRank(AdvancedMedicAssists1),
      CommendationRank(AdvancedMedicAssists2),
      CommendationRank(AdvancedMedicAssists3),
      CommendationRank(AdvancedMedicAssists4),
      CommendationRank(AdvancedMedicAssists5),
      CommendationRank(AdvancedMedicAssists6),
      CommendationRank(AdvancedMedicAssists7)
    )
  )
  case object AirDefender extends Award(
    value = "AirDefender",
    AwardCategory.Vehicular,
    List(
      CommendationRank(AirDefender1),
      CommendationRank(AirDefender2),
      CommendationRank(AirDefender3),
      CommendationRank(AirDefender4),
      CommendationRank(AirDefender5),
      CommendationRank(AirDefender6),
      CommendationRank(AirDefender7)
    )
  )
  case object AMSSupport extends Award(
    value = "AMSSupport",
    AwardCategory.Support,
    List(
      CommendationRank(AMSSupport1),
      CommendationRank(AMSSupport2),
      CommendationRank(AMSSupport3),
      CommendationRank(AMSSupport4),
      CommendationRank(AMSSupport5),
      CommendationRank(AMSSupport6),
      CommendationRank(AMSSupport7)
    )
  )
  case object AntiVehicular extends Award(
    value = "AntiVehicular",
    AwardCategory.Weaponry,
    List(
      CommendationRank(AntiVehicular1),
      CommendationRank(AntiVehicular2),
      CommendationRank(AntiVehicular3),
      CommendationRank(AntiVehicular4),
      CommendationRank(AntiVehicular5),
      CommendationRank(AntiVehicular6),
      CommendationRank(AntiVehicular7)
    )
  )
  case object Avenger extends TerranRepublicAward(
    value = "Avenger",
    AwardCategory.Weaponry,
    List(
      CommendationRank(Avenger1),
      CommendationRank(Avenger2),
      CommendationRank(Avenger3),
      CommendationRank(Avenger4),
      CommendationRank(Avenger5),
      CommendationRank(Avenger6),
      CommendationRank(Avenger7)
    )
  )
  case object BendingMovieActor extends Award(
    value = "BendingMovieActor",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.BendingMovieActor))
  )
  case object BFRAdvanced extends Award(
    value = "BFRAdvanced",
    AwardCategory.Vehicular,
    List(CommendationRank(MeritCommendation.BFRAdvanced))
  )
  case object BFRBuster extends Award(
    value = "BFRBuster",
    AwardCategory.Activity,
    List(
      CommendationRank(BFRBuster1),
      CommendationRank(BFRBuster2),
      CommendationRank(BFRBuster3),
      CommendationRank(BFRBuster4),
      CommendationRank(BFRBuster5),
      CommendationRank(BFRBuster6),
      CommendationRank(BFRBuster7)
    )
  )
  case object BlackOpsHunter extends Award(
    value = "BlackOpsHunter",
    AwardCategory.Activity,
    List(
      CommendationRank(MeritCommendation.BlackOpsHunter1),
      CommendationRank(MeritCommendation.BlackOpsHunter2),
      CommendationRank(MeritCommendation.BlackOpsHunter3),
      CommendationRank(MeritCommendation.BlackOpsHunter4),
      CommendationRank(MeritCommendation.BlackOpsHunter5)
    )
  )
  case object BlackOpsParticipant extends Award(
    value = "BlackOpsParticipant",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.BlackOpsParticipant))
  )
  case object BlackOpsVictory extends Award(
    value = "BlackOpsVictory",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.BlackOpsVictory))
  )
  case object Bombadier extends Award(
    value = "Bombadier",
    AwardCategory.Vehicular,
    List(
      CommendationRank(Bombadier1),
      CommendationRank(Bombadier2),
      CommendationRank(Bombadier3),
      CommendationRank(Bombadier4),
      CommendationRank(Bombadier5),
      CommendationRank(Bombadier6),
      CommendationRank(Bombadier7)
    )
  )
  case object BomberAce extends Award(
    value = "BomberAce",
    AwardCategory.Vehicular,
    List(
      CommendationRank(BomberAce1),
      CommendationRank(BomberAce2),
      CommendationRank(BomberAce3),
      CommendationRank(BomberAce4),
      CommendationRank(BomberAce5),
      CommendationRank(BomberAce6),
      CommendationRank(BomberAce7)
    )
  )
  case object Boomer extends Award(
    value = "Boomer",
    AwardCategory.Weaponry,
    List(
      CommendationRank(Boomer1),
      CommendationRank(Boomer2),
      CommendationRank(Boomer3),
      CommendationRank(Boomer4),
      CommendationRank(Boomer5),
      CommendationRank(Boomer6),
      CommendationRank(Boomer7)
    )
  )
  case object CalvaryDriver extends Award(
    value = "CalvaryDriver",
    AwardCategory.Vehicular,
    List(
      CommendationRank(CalvaryDriver1),
      CommendationRank(CalvaryDriver2),
      CommendationRank(CalvaryDriver3),
      CommendationRank(CalvaryDriver4),
      CommendationRank(CalvaryDriver5),
      CommendationRank(CalvaryDriver6),
      CommendationRank(CalvaryDriver7)
    )
  )
  case object CalvaryPilot extends Award(
    value = "CalvaryPilot",
    AwardCategory.Vehicular,
    List(
      CommendationRank(MeritCommendation.CalvaryPilot),
      CommendationRank(CalvaryPilot2),
      CommendationRank(CalvaryPilot3),
      CommendationRank(CalvaryPilot4),
      CommendationRank(CalvaryPilot5),
      CommendationRank(CalvaryPilot6),
      CommendationRank(CalvaryPilot7)
    )
  )
  case object CMTopOutfit extends Award(
    value = "CMTopOutfit",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.CMTopOutfit))
  )
  case object CombatMedic extends Award(
    value = "CombatMedic",
    AwardCategory.Support,
    List(
      CommendationRank(MeritCommendation.CombatMedic),
      CommendationRank(CombatMedic2),
      CommendationRank(CombatMedic3),
      CommendationRank(CombatMedic4),
      CommendationRank(CombatMedic5),
      CommendationRank(CombatMedic6),
      CommendationRank(CombatMedic7)
    )
  )
  case object CombatRepair extends Award(
    value = "CombatRepair",
    AwardCategory.Support,
    List(
      CommendationRank(CombatRepair1),
      CommendationRank(CombatRepair2),
      CommendationRank(CombatRepair3),
      CommendationRank(CombatRepair4),
      CommendationRank(CombatRepair5),
      CommendationRank(CombatRepair6),
      CommendationRank(CombatRepair7)
    )
  )
  case object ContestFirstBR40 extends Award(
    value = "ContestFirstBR40",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.ContestFirstBR40))
  )
  case object ContestMovieMaker extends Award(
    value = "ContestMovieMaker",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.ContestMovieMaker))
  )
  case object ContestMovieMakerOutfit extends Award(
    value = "ContestMovieMakerOutfit",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.ContestMovieMakerOutfit))
  )
  case object ContestPlayerOfTheMonth extends Award(
    value = "ContestPlayerOfTheMonth",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.ContestPlayerOfTheMonth))
  )
  case object ContestPlayerOfTheYear extends Award(
    value = "ContestPlayerOfTheYear",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.ContestPlayerOfTheYear))
  )
  case object CSAppreciation extends Award(
    value = "CSAppreciation",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.CSAppreciation))
  )
  case object DefenseNC extends NewConglomerateAward(
    value = "DefenseNC",
    AwardCategory.Defense,
    List(
      CommendationRank(DefenseNC1),
      CommendationRank(DefenseNC2),
      CommendationRank(DefenseNC3),
      CommendationRank(DefenseNC4),
      CommendationRank(DefenseNC5),
      CommendationRank(DefenseNC6),
      CommendationRank(DefenseNC7)
    )
  )
  case object DefenseTR extends TerranRepublicAward(
    value = "DefenseTR",
    AwardCategory.Defense,
    List(
      CommendationRank(DefenseTR1),
      CommendationRank(DefenseTR2),
      CommendationRank(DefenseTR3),
      CommendationRank(DefenseTR4),
      CommendationRank(DefenseTR5),
      CommendationRank(DefenseTR6),
      CommendationRank(DefenseTR7)
    )
  )
  case object DefenseVS extends VanuSovereigntyAward(
    value = "DefenseVS",
    AwardCategory.Defense,
    List(
      CommendationRank(DefenseVS1),
      CommendationRank(DefenseVS2),
      CommendationRank(DefenseVS3),
      CommendationRank(DefenseVS4),
      CommendationRank(DefenseVS5),
      CommendationRank(DefenseVS6),
      CommendationRank(DefenseVS7)
    )
  )
  case object DevilDogsMovie extends Award(
    value = "DevilDogsMovie",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.DevilDogsMovie))
  )
  case object DogFighter extends Award(
    value = "DogFighter",
    AwardCategory.Vehicular,
    List(
      CommendationRank(DogFighter1),
      CommendationRank(DogFighter2),
      CommendationRank(DogFighter3),
      CommendationRank(DogFighter4),
      CommendationRank(DogFighter5),
      CommendationRank(DogFighter6),
      CommendationRank(DogFighter7)
    )
  )
  case object DriverGunner extends Award(
    value = "DriverGunner",
    AwardCategory.Vehicular,
    List(
      CommendationRank(DriverGunner1),
      CommendationRank(DriverGunner2),
      CommendationRank(DriverGunner3),
      CommendationRank(DriverGunner4),
      CommendationRank(DriverGunner5),
      CommendationRank(DriverGunner6),
      CommendationRank(DriverGunner7)
    )
  )
  case object EliteAssault extends Award(
    value = "EliteAssault",
    AwardCategory.Weaponry,
    List(
      CommendationRank(EliteAssault1),
      CommendationRank(EliteAssault2),
      CommendationRank(EliteAssault3),
      CommendationRank(EliteAssault4),
      CommendationRank(EliteAssault5),
      CommendationRank(EliteAssault6),
      CommendationRank(EliteAssault7)
    )
  )
  case object EmeraldVeteran extends Award(
    value = "EmeraldVeteran",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.EmeraldVeteran))
  )
  case object Engineer extends Award(
    value = "Engineer",
    AwardCategory.Support,
    List(
      CommendationRank(Engineer1),
      CommendationRank(Engineer2),
      CommendationRank(Engineer3),
      CommendationRank(Engineer4),
      CommendationRank(Engineer5),
      CommendationRank(Engineer6)
    )
  )
  case object EquipmentSupport extends Award(
    value = "EquipmentSupport",
    AwardCategory.Support,
    List(
      CommendationRank(EquipmentSupport1),
      CommendationRank(EquipmentSupport2),
      CommendationRank(EquipmentSupport3),
      CommendationRank(EquipmentSupport4),
      CommendationRank(EquipmentSupport5),
      CommendationRank(EquipmentSupport6),
      CommendationRank(EquipmentSupport7)
    )
  )
  case object EventNCCommander extends NewConglomerateAward(
    value = "EventNCCommander",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.EventNCCommander))
  )
  case object EventNCElite extends NewConglomerateAward(
    value = "EventNCElite",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.EventNCElite))
  )
  case object EventNCSoldier extends NewConglomerateAward(
    value = "EventNCSoldier",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.EventNCSoldier))
  )
  case object EventTRCommander extends TerranRepublicAward(
    value = "EventTRCommander",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.EventTRCommander))
  )
  case object EventTRElite extends TerranRepublicAward(
    value = "EventTRElite",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.EventTRElite))
  )
  case object EventTRSoldier extends TerranRepublicAward(
    value = "EventTRSoldier",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.EventTRSoldier))
  )
  case object EventVSCommander extends VanuSovereigntyAward(
    value = "EventVSCommander",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.EventVSCommander))
  )
  case object EventVSElite extends VanuSovereigntyAward(
    value = "EventVSElite",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.EventVSElite))
  )
  case object EventVSSoldier extends VanuSovereigntyAward(
    value = "EventVSSoldier",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.EventVSSoldier))
  )
//  case object EventNC extends NewConglomerateAward(
//    value = "EventNC",
//    AwardCategory.Exclusive,
//    List(
//      CommendationRank(MeritCommendation.EventNCSoldier),
//      CommendationRank(MeritCommendation.EventNCElite),
//      CommendationRank(MeritCommendation.EventNCCommander)
//    )
//  )
//  case object EventTR extends TerranRepublicAward(
//    value = "EventTR",
//    AwardCategory.Exclusive,
//    List(
//      CommendationRank(MeritCommendation.EventTRSoldier),
//      CommendationRank(MeritCommendation.EventTRElite),
//      CommendationRank(MeritCommendation.EventTRCommander)
//    )
//  )
//  case object EventVS extends VanuSovereigntyAward(
//    value = "EventVS",
//    AwardCategory.Exclusive,
//    List(
//      CommendationRank(MeritCommendation.EventVSSoldier),
//      CommendationRank(MeritCommendation.EventVSElite),
//      CommendationRank(MeritCommendation.EventVSCommander)
//    )
//  )
  case object Explorer extends Award(
    value = "Explorer",
    AwardCategory.Activity,
    List(CommendationRank(MeritCommendation.Explorer1))
  )
  case object FanFaire2005Commander extends Award(
    value = "FanFaire2005Commander",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.FanFaire2005Commander))
  )
  case object FanFaire2005Soldier extends Award(
    value = "FanFaire2005Soldier",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.FanFaire2005Soldier))
  )
  case object FanFaire2006Atlanta extends Award(
    value = "FanFaire2006Atlanta",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.FanFaire2006Atlanta))
  )
  case object FanFaire2007 extends Award(
    value = "FanFaire2007",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.FanFaire2007))
  )
  case object FanFaire2008 extends Award(
    value = "FanFaire2008",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.FanFaire2008))
  )
  case object FanFaire2009 extends Award(
    value = "FanFaire2009",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.FanFaire2009))
  )
  case object GalaxySupport extends Award(
    value = "GalaxySupport",
    AwardCategory.Support,
    List(
      CommendationRank(GalaxySupport1),
      CommendationRank(GalaxySupport2),
      CommendationRank(GalaxySupport3),
      CommendationRank(GalaxySupport4),
      CommendationRank(GalaxySupport5),
      CommendationRank(GalaxySupport6),
      CommendationRank(GalaxySupport7)
    )
  )
  case object Grenade extends Award(
    value = "Grenade",
    AwardCategory.Weaponry,
    List(
      CommendationRank(Grenade1),
      CommendationRank(Grenade2),
      CommendationRank(Grenade3),
      CommendationRank(Grenade4),
      CommendationRank(Grenade5),
      CommendationRank(Grenade6),
      CommendationRank(Grenade7)
    )
  )
  case object GroundGunner extends Award(
    value = "GroundGunner",
    AwardCategory.Vehicular,
    List(
      CommendationRank(GroundGunner1),
      CommendationRank(GroundGunner2),
      CommendationRank(GroundGunner3),
      CommendationRank(GroundGunner4),
      CommendationRank(GroundGunner5),
      CommendationRank(GroundGunner6),
      CommendationRank(GroundGunner7)
    )
  )
  case object HackingSupport extends Award(
    value = "HackingSupport",
    AwardCategory.Support,
    List(
      CommendationRank(HackingSupport1),
      CommendationRank(HackingSupport2),
      CommendationRank(HackingSupport3),
      CommendationRank(HackingSupport4),
      CommendationRank(HackingSupport5),
      CommendationRank(HackingSupport6),
      CommendationRank(HackingSupport7)
    )
  )
  case object HalloweenMassacre2006NC extends NewConglomerateAward(
    value = "HalloweenMassacre2006NC",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.HalloweenMassacre2006NC))
  )
  case object HalloweenMassacre2006TR extends TerranRepublicAward(
    value = "HalloweenMassacre2006TR",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.HalloweenMassacre2006TR))
  )
  case object HalloweenMassacre2006VS extends VanuSovereigntyAward(
    value = "HalloweenMassacre2006VS",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.HalloweenMassacre2006VS))
  )
  case object HeavyAssault extends Award(
    value = "HeavyAssault",
    AwardCategory.Weaponry,
    List(
      CommendationRank(HeavyAssault1),
      CommendationRank(HeavyAssault2),
      CommendationRank(HeavyAssault3),
      CommendationRank(HeavyAssault4),
      CommendationRank(HeavyAssault5),
      CommendationRank(HeavyAssault6),
      CommendationRank(HeavyAssault7)
    )
  )
  case object HeavyInfantry extends Award(
    value = "HeavyInfantry",
    AwardCategory.Weaponry,
    List(
      CommendationRank(MeritCommendation.HeavyInfantry),
      CommendationRank(HeavyInfantry2),
      CommendationRank(HeavyInfantry3),
      CommendationRank(HeavyInfantry4)
    )
  )
  case object InfantryExpert extends Award(
    value = "InfantryExpert",
    AwardCategory.Weaponry,
    List(
      CommendationRank(InfantryExpert1),
      CommendationRank(InfantryExpert2),
      CommendationRank(InfantryExpert3)
    )
  )
  case object Jacking extends Award(
    value = "Jacking",
    AwardCategory.Activity,
    List(
      CommendationRank(MeritCommendation.Jacking),
      CommendationRank(Jacking2),
      CommendationRank(Jacking3),
      CommendationRank(Jacking4),
      CommendationRank(Jacking5),
      CommendationRank(Jacking6),
      CommendationRank(Jacking7)
    )
  )
  case object KnifeCombat extends Award(
    value = "KnifeCombat",
    AwardCategory.Weaponry,
    List(
      CommendationRank(KnifeCombat1),
      CommendationRank(KnifeCombat2),
      CommendationRank(KnifeCombat3),
      CommendationRank(KnifeCombat4),
      CommendationRank(KnifeCombat5),
      CommendationRank(KnifeCombat6),
      CommendationRank(KnifeCombat7)
    )
  )
  case object LightInfantry extends Award(
    value = "LightInfantry",
    AwardCategory.Weaponry,
    List(CommendationRank(MeritCommendation.LightInfantry))
  )
  case object LockerCracker extends Award(
    value = "LockerCracker",
    AwardCategory.Support,
    List(
      CommendationRank(LockerCracker1),
      CommendationRank(LockerCracker2),
      CommendationRank(LockerCracker3),
      CommendationRank(LockerCracker4),
      CommendationRank(LockerCracker5),
      CommendationRank(LockerCracker6),
      CommendationRank(LockerCracker7)
    )
  )
  case object LodestarSupport extends Award(
    value = "LodestarSupport",
    AwardCategory.Support,
    List(
      CommendationRank(LodestarSupport1),
      CommendationRank(LodestarSupport2),
      CommendationRank(LodestarSupport3),
      CommendationRank(LodestarSupport4),
      CommendationRank(LodestarSupport5),
      CommendationRank(LodestarSupport6),
      CommendationRank(LodestarSupport7)
    )
  )
  case object Loser extends Award(
    value = "Loser",
    AwardCategory.Exclusive,
    List(
      CommendationRank(MeritCommendation.Loser),
      CommendationRank(MeritCommendation.Loser2),
      CommendationRank(MeritCommendation.Loser3),
      CommendationRank(MeritCommendation.Loser4)
    )
  )
  case object MarkovVeteran extends Award(
    value = "MarkovVeteran",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.MarkovVeteran))
  )
  case object Max extends Award(
    value = "Max",
    AwardCategory.Vehicular,
    List(
      CommendationRank(Max1),
      CommendationRank(Max2),
      CommendationRank(Max3),
      CommendationRank(Max4),
      CommendationRank(Max5),
      CommendationRank(Max6)
    )
  )
  case object MaxBuster extends Award(
    value = "MaxBuster",
    AwardCategory.Activity,
    List(
      CommendationRank(MaxBuster1),
      CommendationRank(MaxBuster2),
      CommendationRank(MaxBuster3),
      CommendationRank(MaxBuster4),
      CommendationRank(MaxBuster5),
      CommendationRank(MaxBuster6)
    )
  )
  case object MediumAssault extends Award(
    value = "MediumAssault",
    AwardCategory.Weaponry,
    List(
      CommendationRank(MediumAssault1),
      CommendationRank(MediumAssault2),
      CommendationRank(MediumAssault3),
      CommendationRank(MediumAssault4),
      CommendationRank(MediumAssault5),
      CommendationRank(MediumAssault6),
      CommendationRank(MediumAssault7)
    )
  )
  case object None extends Award(
    value = "None",
    AwardCategory.Exclusive,
    List(
      CommendationRank(MeritCommendation.None)
    )
  )
  case object Orion extends VanuSovereigntyAward(
    value = "Orion",
    AwardCategory.Vehicular,
    List(
      CommendationRank(Orion1),
      CommendationRank(Orion2),
      CommendationRank(Orion3),
      CommendationRank(Orion4),
      CommendationRank(Orion5),
      CommendationRank(Orion6),
      CommendationRank(Orion7)
    )
  )
  case object Osprey extends NewConglomerateAward(
    value = "Osprey",
    AwardCategory.Vehicular,
    List(
      CommendationRank(Osprey1),
      CommendationRank(Osprey2),
      CommendationRank(Osprey3),
      CommendationRank(Osprey4),
      CommendationRank(Osprey5),
      CommendationRank(Osprey6),
      CommendationRank(Osprey7)
    )
  )
  case object Phalanx extends Award(
    value = "Phalanx",
    AwardCategory.Weaponry,
    List(
      CommendationRank(Phalanx1),
      CommendationRank(Phalanx2),
      CommendationRank(Phalanx3),
      CommendationRank(Phalanx4),
      CommendationRank(Phalanx5),
      CommendationRank(Phalanx6),
      CommendationRank(Phalanx7)
    )
  )
  case object PSUMaAttendee extends Award(
    value = "PSUMaAttendee",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.PSUMaAttendee))
  )
  case object PSUMbAttendee extends Award(
    value = "PSUMbAttendee",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.PSUMbAttendee))
  )
  case object QAAppreciation extends Award(
    value = "QAAppreciation",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.QAAppreciation))
  )
  case object ReinforcementHackSpecialist extends Award(
    value = "ReinforcementHackSpecialist",
    AwardCategory.Support,
    List(CommendationRank(MeritCommendation.ReinforcementHackSpecialist))
  )
  case object ReinforcementInfantrySpecialist extends Award(
    value = "ReinforcementInfantrySpecialist",
    AwardCategory.Support,
    List(CommendationRank(MeritCommendation.ReinforcementInfantrySpecialist))
  )
  case object ReinforcementSpecialist extends Award(
    value = "ReinforcementSpecialist",
    AwardCategory.Support,
    List(CommendationRank(MeritCommendation.ReinforcementSpecialist))
  )
  case object ReinforcementVehicleSpecialist extends Award(
    value = "ReinforcementVehicleSpecialist",
    AwardCategory.Support,
    List(CommendationRank(MeritCommendation.ReinforcementVehicleSpecialist))
  )
  case object RouterSupport extends Award(
    value = "RouterSupport",
    AwardCategory.Support,
    List(
      CommendationRank(RouterSupport1),
      CommendationRank(RouterSupport2),
      CommendationRank(RouterSupport3),
      CommendationRank(RouterSupport4),
      CommendationRank(RouterSupport5),
      CommendationRank(RouterSupport6),
      CommendationRank(RouterSupport7)
    )
  )
  case object RouterTelepadDeploy extends Award(
    value = "RouterTelepadDeploy",
    AwardCategory.Support,
    List(
      CommendationRank(RouterTelepadDeploy1),
      CommendationRank(RouterTelepadDeploy2),
      CommendationRank(RouterTelepadDeploy3),
      CommendationRank(RouterTelepadDeploy4),
      CommendationRank(RouterTelepadDeploy5),
      CommendationRank(RouterTelepadDeploy6),
      CommendationRank(RouterTelepadDeploy7)
    )
  )
  case object ScavengerNC extends NewConglomerateAward(
    value = "ScavengerNC",
    AwardCategory.Weaponry,
    List(
      CommendationRank(ScavengerNC1),
      CommendationRank(ScavengerNC2),
      CommendationRank(ScavengerNC3),
      CommendationRank(ScavengerNC4),
      CommendationRank(ScavengerNC5),
      CommendationRank(ScavengerNC6)
    )
  )
  case object ScavengerTR extends TerranRepublicAward(
    value = "ScavengerTR",
    AwardCategory.Weaponry,
    List(
      CommendationRank(ScavengerTR1),
      CommendationRank(ScavengerTR2),
      CommendationRank(ScavengerTR3),
      CommendationRank(ScavengerTR4),
      CommendationRank(ScavengerTR5),
      CommendationRank(ScavengerTR6)
    )
  )
  case object ScavengerVS extends VanuSovereigntyAward(
    value = "ScavengerVS",
    AwardCategory.Weaponry,
    List(
      CommendationRank(ScavengerVS1),
      CommendationRank(ScavengerVS2),
      CommendationRank(ScavengerVS3),
      CommendationRank(ScavengerVS4),
      CommendationRank(ScavengerVS5),
      CommendationRank(ScavengerVS6)
    )
  )
  case object Sniper extends Award(
    value = "Sniper",
    AwardCategory.Weaponry,
    List(
      CommendationRank(Sniper1),
      CommendationRank(Sniper2),
      CommendationRank(Sniper3),
      CommendationRank(Sniper4),
      CommendationRank(Sniper5),
      CommendationRank(Sniper6),
      CommendationRank(Sniper7)
    )
  )
  case object SpecialAssault extends Award(
    value = "SpecialAssault",
    AwardCategory.Weaponry,
    List(
      CommendationRank(SpecialAssault1),
      CommendationRank(SpecialAssault2),
      CommendationRank(SpecialAssault3),
      CommendationRank(SpecialAssault4),
      CommendationRank(SpecialAssault5),
      CommendationRank(SpecialAssault6),
      CommendationRank(SpecialAssault7)
    )
  )
  case object StandardAssault extends Award(
    value = "StandardAssault",
    AwardCategory.Weaponry,
    List(
      CommendationRank(StandardAssault1),
      CommendationRank(StandardAssault2),
      CommendationRank(StandardAssault3),
      CommendationRank(StandardAssault4),
      CommendationRank(StandardAssault5),
      CommendationRank(StandardAssault6),
      CommendationRank(StandardAssault7)
    )
  )
  case object StracticsHistorian extends Award(
    value = "StracticsHistorian",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.StracticsHistorian))
  )
  case object Supply extends Award(
    value = "Supply",
    AwardCategory.Activity,
    List(
      CommendationRank(Supply1),
      CommendationRank(Supply2),
      CommendationRank(Supply3),
      CommendationRank(Supply4),
      CommendationRank(Supply5),
      CommendationRank(Supply6),
      CommendationRank(Supply7)
    )
  )
  case object TankBuster extends Award(
    value = "TankBuster",
    AwardCategory.Activity,
    List(
      CommendationRank(TankBuster1),
      CommendationRank(TankBuster2),
      CommendationRank(TankBuster3),
      CommendationRank(TankBuster4),
      CommendationRank(TankBuster5),
      CommendationRank(TankBuster6),
      CommendationRank(TankBuster7)
    )
  )
  case object TermOfServiceNC extends NewConglomerateAward(
    value = "TermOfServiceNC",
    AwardCategory.Exclusive, //Activity,
    List(
      CommendationRank(OneYearNC),
      CommendationRank(TwoYearNC),
      CommendationRank(ThreeYearNC),
      CommendationRank(FourYearNC),
      CommendationRank(FiveYearNC),
      CommendationRank(SixYearNC)
    )
  )
  case object TermOfServiceTR extends TerranRepublicAward(
    value = "TermOfServiceTR",
    AwardCategory.Exclusive, //Activity,
    List(
      CommendationRank(OneYearTR),
      CommendationRank(TwoYearTR),
      CommendationRank(ThreeYearTR),
      CommendationRank(FourYearTR),
      CommendationRank(FiveYearTR),
      CommendationRank(SixYearTR)
    )
  )
  case object TermOfServiceVS extends VanuSovereigntyAward(
    value = "TermOfServiceVS",
    AwardCategory.Exclusive, //Activity,
    List(
      CommendationRank(OneYearVS),
      CommendationRank(TwoYearVS),
      CommendationRank(ThreeYearVS),
      CommendationRank(FourYearVS),
      CommendationRank(FiveYearVS),
      CommendationRank(SixYearVS)
    )
  )
  case object TinyRoboticSupport extends Award(
    value = "TinyRoboticSupport",
    AwardCategory.Support,
    List(
      CommendationRank(TinyRoboticSupport1),
      CommendationRank(TinyRoboticSupport2),
      CommendationRank(TinyRoboticSupport3),
      CommendationRank(TinyRoboticSupport4),
      CommendationRank(TinyRoboticSupport5),
      CommendationRank(TinyRoboticSupport6),
      CommendationRank(TinyRoboticSupport7)
    )
  )
  case object Transport extends Award(
    value = "Transport",
    AwardCategory.Activity,
    List(
      CommendationRank(Transport1),
      CommendationRank(Transport2),
      CommendationRank(Transport3),
      CommendationRank(Transport4),
      CommendationRank(Transport5),
      CommendationRank(Transport6),
      CommendationRank(Transport7)
    )
  )
  case object TransportationCitation extends Award(
    value = "TransportationCitation",
    AwardCategory.Activity,
    List(
      CommendationRank(TransportationCitation1),
      CommendationRank(TransportationCitation2),
      CommendationRank(TransportationCitation3),
      CommendationRank(TransportationCitation4),
      CommendationRank(TransportationCitation5)
    )
  )
  case object ValentineFemale extends Award(
    value = "ValentineFemale",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.ValentineFemale))
  )
  case object ValentineMale extends Award(
    value = "ValentineMale",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.ValentineMale))
  )
  case object WernerVeteran extends Award(
    value = "WernerVeteran",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.WernerVeteran))
  )
  case object XmasGingerman extends Award(
    value = "XmasGingerman",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.XmasGingerman))
  )
  case object XmasSnowman extends Award(
    value = "XmasSnowman",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.XmasSnowman))
  )
  case object XmasSpirit extends Award(
    value = "XmasSpirit",
    AwardCategory.Exclusive,
    List(CommendationRank(MeritCommendation.XmasSpirit))
  )

  final val common: List[Award] = List(
    AdvancedMedic, AdvancedMedicAssists, AirDefender, AMSSupport, AntiVehicular,
    BFRAdvanced, BFRBuster, Bombadier, BomberAce, Boomer,
    TankBuster, TinyRoboticSupport, Transport, TransportationCitation
  )
}
