// Copyright (c) 2023 PSForever
package net.psforever.types

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.types.StatisticalElement.{AMS, ANT, AgileExoSuit, ApcNc, ApcTr, ApcVs, Aphelion, AphelionFlight, AphelionGunner, Battlewagon, Colossus, ColossusFlight, ColossusGunner, Dropship, Flail, Fury, GalaxyGunship, InfiltrationExoSuit, Liberator, Lightgunship, Lightning, Lodestar, Magrider, MechanizedAssaultExoSuit, MediumTransport, Mosquito, Peregrine, PeregrineFlight, PeregrineGunner, PhalanxTurret, PortableMannedTurretNc, PortableMannedTurretTr, PortableMannedTurretVs, Prowler, QuadAssault, QuadStealth, Raider, ReinforcedExoSuit, Router, Skyguard, StandardExoSuit, Sunderer, Switchblade, ThreeManHeavyBuggy, Thunderer, TwoManAssaultBuggy, TwoManHeavyBuggy, TwoManHoverBuggy, VanSentryTurret, Vanguard, Vulture, Wasp}

sealed abstract class StatisticalCategory(val value: Int) extends IntEnumEntry

sealed abstract class StatisticalElement(val value: Int) extends IntEnumEntry

object StatisticalCategory extends IntEnum[StatisticalCategory] {
  val values: IndexedSeq[StatisticalCategory] = findValues

  final case object Destroyed extends StatisticalCategory(value = 1)
  final case object Unknown2 extends StatisticalCategory(value = 2)
  final case object Capture extends StatisticalCategory(value = 3)
  final case object ReviveAssist extends StatisticalCategory(value = 4) //number of allies who killed enemies after being revived
  final case object CavernCapture extends StatisticalCategory(value = 7)
  final case object Breach extends StatisticalCategory(value = 8)
  final case object Unknown9 extends StatisticalCategory(value = 9)
  final case object AmenityDestroyed extends StatisticalCategory(value = 10) //does not include turrets
  final case object DriverKilled extends StatisticalCategory(value = 12)
  final case object GunnerKilled extends StatisticalCategory(value = 13)
  final case object PassengerKilled extends StatisticalCategory(value = 14)
  final case object CargoDestroyed extends StatisticalCategory(value = 15)
  final case object BombadierKilled extends StatisticalCategory(value = 16)
  final case object Special extends StatisticalCategory(value = 17)
  final case object DriverAssist extends StatisticalCategory(value = 18)
  final case object Dogfighter extends StatisticalCategory(value = 19)
  final case object HealKillAssist extends StatisticalCategory(value = 20)
  final case object ReviveKillAssist extends StatisticalCategory(value = 21) //number of enemies killed by allies you have revived
  final case object RepairKillAssist extends StatisticalCategory(value = 22) //number of enemies killed by allies you have repaired
  final case object AmsRespawnKillAssist extends StatisticalCategory(value = 23)
  final case object HotDropKillAssist extends StatisticalCategory(value = 24)
  final case object HackKillAssist extends StatisticalCategory(value = 25)
  final case object LodestarRearmKillAssist extends StatisticalCategory(value = 26)
  final case object AmsResupplyKillAssist extends StatisticalCategory(value = 27)
  final case object RouterKillAssist extends StatisticalCategory(value = 28)
  final case object Unknown29 extends StatisticalCategory(value = 29)

  private val gunnerVehicles: Seq[StatisticalElement] = Seq(
    Sunderer, ApcTr, ApcNc, ApcVs, Aphelion, AphelionGunner, Battlewagon,
    Colossus, ColossusGunner, Dropship, GalaxyGunship, Liberator,
    Magrider, MediumTransport, Peregrine, PeregrineGunner, Prowler, Raider,
    Skyguard, ThreeManHeavyBuggy, Thunderer, TwoManAssaultBuggy,
    TwoManHeavyBuggy, TwoManHoverBuggy, Vanguard, Vulture
  )
  private val driverOnlyVehicles: Seq[StatisticalElement] = Seq(
    AMS, ANT, AphelionFlight, ColossusFlight, Flail, Fury,
    Lightgunship, Lightning, Lodestar, Mosquito, PeregrineFlight,
    QuadAssault, QuadStealth, Router, Switchblade, Wasp
  )
  private val exosuitElements: Seq[StatisticalElement] = Seq(
    MechanizedAssaultExoSuit, AgileExoSuit, ReinforcedExoSuit, StandardExoSuit, InfiltrationExoSuit
  )
  private val mannedTurretElements = Seq(
    PhalanxTurret, PortableMannedTurretTr, PortableMannedTurretNc, PortableMannedTurretVs, VanSentryTurret
  )

  val statElements: Seq[Seq[StatisticalElement]] = {
    import StatisticalElement._
    Seq(
      Nil,
      Seq(Phantasm, ImplantTerminalMech, Droppod, SpitfireAA, SpitfireCloaked, SpitfireTurret, TankTraps) ++
        driverOnlyVehicles ++ gunnerVehicles ++ mannedTurretElements ++ exosuitElements,
      Seq(
        //        Chaingun12mm, Chaingun15mm, Cannon20mm, Deliverer20mm, DropshipL20mm, Cannon75mm, Lightning75mm, AdvancedMissileLauncherT, AMS, AnniversaryGun, AnniversaryGunA, AnniversaryGunB, ANT, Sunderer, ApcBallGunL, ApcBallGunR, ApcTr, ApcNc, ApcVs, ApcWeaponSystemA, ApcWeaponSystemB, ApcWeaponSystemC, ApcWeaponSystemCNc, ApcWeaponSystemCTr, ApcWeaponSystemCVs, ApcWeaponSystemD, ApcWeaponSystemDNc, ApcWeaponSystemDTr, ApcWeaponSystemDVs, Aphelion, AphelionArmorSiphon, AphelionFlight, AphelionGunner, AphelionImmolationCannon, AphelionLaser, AphelionNtuSiphon, AphelionPlasmaCloud, AphelionPlasmaRocketPod,
        //        AphelionPpa, AphelionStarfire, AuroraWeaponSystemA, AuroraWeaponSystemB, Battlewagon, BattlewagonWeaponSystemA, BattlewagonWeaponSystemB, BattlewagonWeaponSystemC, BattlewagonWeaponSystemD, Infantry, Raider, Beamer, BoltDriver, Boomer, Chainblade, ChaingunP, Colossus, ColossusArmorSiphon, ColossusBurster, ColossusChaingun, ColossusClusterBombPod, ColossusDual100mmCannons, ColossusFlight,
        //        ColossusGunner, ColossusNtuSiphon, ColossusTankCannon, Cycler, CyclerV2, CyclerV3, CyclerV4, Dropship, DropshipRearTurret, Dynomite, EnergyGunNc, EnergyGunTr, EnergyGunVs, Flail, FlailWeapon, Flamethrower,
        //        Flechette, FluxCannonThresher, Fluxpod, Forceblade, FragGrenade, Fury, FragmentationGrenade, FuryWeaponSystemA, GalaxyGunship, GalaxyGunshipCannon, GalaxyGunshipGun, GalaxyGunshipTailgun, Gauss, GaussCannon, GrenadeLauncherMarauder, HeMine, HeavyRailBeamMagrider, HeavySniper, Hellfire,
        //        Hunterseeker, Ilc9, Isp, JammerGrenade, Katana, Knife, Lancer, Lasher, Liberator, Liberator25mmCannon, LiberatorBombBay, LiberatorWeaponSystem, Lightgunship, LightgunshipWeapon20mm, LightgunshipWeaponRocket, LightgunshipWeaponSystem, Lightning, LightningWeaponSystem, Lodestar, Maelstrom, Magcutter, Magrider, PhalanxTurret,
        //        MedicalApplicator, MediumTransport, MediumTransportWeaponSystemA, MediumTransportWeaponSystemB, MineSweeper, MiniChaingun, Mosquito, NchevFalcon, NchevScattercannon, NchevSparrow, Oicw,
        //        OrbitalStrikeBig, OrbitalStrikeSmall, ParticleBeamMagrider, PelletGun, Peregrine, PeregrineArmorSiphon, PeregrineDualMachineGun, PeregrineDualRocketPods, PeregrineFlight, PeregrineGunner, PeregrineMechhammer, PeregrineNtuSiphon, PeregrineParticleCannon, PeregrineSparrow, PhalanxAvcombo, PhalanxFlakcombo, PhalanxSglHevgatcan, Phantasm, Phantasm12mmMachinegun, Phoenix, PlasmaGrenade, Prowler, ProwlerWeaponSystemA,
        //        ProwlerWeaponSystemB, Pulsar, PulsedParticleAccelerator, Punisher, QuadAssault, QuadAssaultWeaponSystem, QuadStealth, RShotgun, Radiator, Repeater, Rocklet, RotaryChaingunMosquito, Router, RouterTelepadDeployable, Scythe, SixShooter, Skyguard, SkyguardWeaponSystem,
        //        Spiker, SpitfireAA, SpitfireCloaked, SpitfireTurret, Striker, Suppressor, Switchblade, ThreeManHeavyBuggy, Thumper, Thunderer, ThundererWeaponSystemA, ThundererWeaponSystemB, TrhevBurster, TrhevDualcycler, TrhevPounder, TwoManAssaultBuggy, TwoManHeavyBuggy,
        //        TwoManHoverBuggy, Vanguard, VanguardWeapon150mm, VanguardWeapon20mm, VanguardWeaponSystem, VanuModule, VanuSentryTurretWeapon, VanuModuleBeam, VshevComet, VshevQuasar, VshevStarfire, Vulture, VultureBombBay, VultureNoseWeaponSystem, VultureTailCannon, Wasp, WaspWeaponSystem, Winchester
      ),
      Seq(Facilities, Redoubt, Tower, VanuControlPoint, VanuVehicleStation),
      exosuitElements,
      Nil,
      Nil,
      Seq(Facilities, Redoubt, VanuControlPoint, VanuVehicleStation),
      Seq(BfrTerminal, Door, Terminal),
      Seq(Phantasm) ++ driverOnlyVehicles ++ gunnerVehicles,
      Seq(BfrTerminal, Generator, RespawnTube, Terminal),
      Nil,
      Seq(Phantasm) ++ driverOnlyVehicles ++ gunnerVehicles,
      mannedTurretElements ++ gunnerVehicles,
      Seq(Phantasm) ++ gunnerVehicles,
      Seq(Dropship, Lodestar),
      Seq(Liberator, Vulture),
      Seq(
        MonolithAmerish, MonolithCeryshen, MonolithCyssor, MonolithEsamir, MonolithForseral,
        MonolithHossin, MonolithIshundar, MonolithSearhus, MonolithSolsar,
        XmasCharlie1, XmasCharlie2, XmasCharlie3, XmasCharlie4, XmasCharlie5, XmasCharlie6, XmasCharlie7, XmasCharlie8, XmasCharlie9,
        XmasGingermanAtar, XmasGingermanDahaka, XmasGingermanHvar, XmasGingermanIzha, XmasGingermanJamshid,
        XmasGingermanMithra, XmasGingermanRashnu, XmasGingermanSraosha, XmasGingermanYazata, XmasGingermanZal,
        XmasSled1, XmasSled2, XmasSled3, XmasSled4, XmasSled5, XmasSled6, XmasSled7, XmasSled8, XmasSled9,
        XmasSnowmanAmerish, XmasSnowmanCeryshen, XmasSnowmanCyssor, XmasSnowmanEsamir, XmasSnowmanForseral,
        XmasSnowmanHossin, XmasSnowmanIshundar, XmasSnowmanSearhus, XmasSnowmanSolsar
      ),
      Seq(Phantasm, Flail) ++ mannedTurretElements ++ gunnerVehicles,
      Seq(AirToAir),
      Seq(Infantry),
      Seq(Infantry),
      Seq(Infantry, Vehicle, Lodestar, PhalanxTurret, SpitfireAA, SpitfireCloaked, SpitfireTurret),
      Seq(AMS),
      Seq(Dropship),
      Seq(BfrTerminal, Locker, MedicalTerminal, EquipmentTerminal, VehicleTerminal),
      Seq(Lodestar),
      Seq(AMS),
      Seq(Router, RouterTelepadDeployable),
      Seq(AgileExoSuit, ReinforcedExoSuit)
    )
  }
}

object StatisticalElement extends IntEnum[StatisticalElement] {
  val values: IndexedSeq[StatisticalElement] = findValues

  final case object Chaingun12mm extends StatisticalElement(value = 2)
  final case object Chaingun15mm extends StatisticalElement(value = 8)
  final case object Cannon20mm extends StatisticalElement(value = 12)
  final case object Deliverer20mm extends StatisticalElement(value = 13)
  final case object Dropship20mm extends StatisticalElement(value = 14)
  final case object DropshipL20mm extends StatisticalElement(value = 15)
  final case object Cannon75mm extends StatisticalElement(value = 23)
  final case object Lightning75mm extends StatisticalElement(value = 24)
  final case object AdvancedMissileLauncherT extends StatisticalElement(value = 40)
  final case object AnniversaryGun extends StatisticalElement(value = 55)
  final case object AnniversaryGunA extends StatisticalElement(value = 56)
  final case object AnniversaryGunB extends StatisticalElement(value = 57)
  final case object ApcBallGunL extends StatisticalElement(value = 63)
  final case object ApcBallGunR extends StatisticalElement(value = 64)
  final case object ApcWeaponSystemA extends StatisticalElement(value = 69)
  final case object ApcWeaponSystemB extends StatisticalElement(value = 70)
  final case object ApcWeaponSystemC extends StatisticalElement(value = 71)
  final case object ApcWeaponSystemCNc extends StatisticalElement(value = 72)
  final case object ApcWeaponSystemCTr extends StatisticalElement(value = 73)
  final case object ApcWeaponSystemCVs extends StatisticalElement(value = 74)
  final case object ApcWeaponSystemD extends StatisticalElement(value = 75)
  final case object ApcWeaponSystemDNc extends StatisticalElement(value = 76)
  final case object ApcWeaponSystemDTr extends StatisticalElement(value = 77)
  final case object ApcWeaponSystemDVs extends StatisticalElement(value = 78)
  final case object AphelionArmorSiphon extends StatisticalElement(value = 80)
  final case object AphelionImmolationCannon extends StatisticalElement(value = 85)
  final case object AphelionLaser extends StatisticalElement(value = 88)
  final case object AphelionNtuSiphon extends StatisticalElement(value = 93)
  final case object AphelionPlasmaRocketPod extends StatisticalElement(value = 98)
  final case object AphelionPpa extends StatisticalElement(value = 100)
  final case object AphelionStarfire extends StatisticalElement(value = 105)
  final case object AuroraWeaponSystemA extends StatisticalElement(value = 119)
  final case object AuroraWeaponSystemB extends StatisticalElement(value = 120)
  final case object BattlewagonWeaponSystemA extends StatisticalElement(value = 136)
  final case object BattlewagonWeaponSystemB extends StatisticalElement(value = 137)
  final case object BattlewagonWeaponSystemC extends StatisticalElement(value = 138)
  final case object BattlewagonWeaponSystemD extends StatisticalElement(value = 139)
  final case object Beamer extends StatisticalElement(value = 140)
  final case object BoltDriver extends StatisticalElement(value = 146)
  final case object Chainblade extends StatisticalElement(value = 175)
  final case object ChaingunP extends StatisticalElement(value = 177)
  final case object ColossusArmorSiphon extends StatisticalElement(value = 182)
  final case object ColossusBurster extends StatisticalElement(value = 185)
  final case object ColossusChaingun extends StatisticalElement(value = 190)
  final case object ColossusClusterBombPod extends StatisticalElement(value = 196)
  final case object ColossusDual100mmCannons extends StatisticalElement(value = 198)
  final case object ColossusNtuSiphon extends StatisticalElement(value = 201)
  final case object ColossusTankCannon extends StatisticalElement(value = 204)
  final case object Cycler extends StatisticalElement(value = 233)
  final case object CyclerV2 extends StatisticalElement(value = 234)
  final case object CyclerV3 extends StatisticalElement(value = 235)
  final case object CyclerV4 extends StatisticalElement(value = 236)
  final case object DropshipRearTurret extends StatisticalElement(value = 262)
  final case object Dynomite extends StatisticalElement(value = 267)
  final case object EnergyGun extends StatisticalElement(value = 274)
  final case object EnergyGunNc extends StatisticalElement(value = 276)
  final case object EnergyGunTr extends StatisticalElement(value = 278)
  final case object EnergyGunVs extends StatisticalElement(value = 280)
  final case object FlailWeapon extends StatisticalElement(value = 298)
  final case object Flamethrower extends StatisticalElement(value = 299)
  final case object Flechette extends StatisticalElement(value = 304)
  final case object FluxCannonThresher extends StatisticalElement(value = 306)
  final case object Fluxpod extends StatisticalElement(value = 309)
  final case object Forceblade extends StatisticalElement(value = 324)
  final case object FragGrenade extends StatisticalElement(value = 330)
  final case object FragmentationGrenade extends StatisticalElement(value = 334)
  final case object FuryWeaponSystemA extends StatisticalElement(value = 336)
  final case object GalaxyGunshipCannon extends StatisticalElement(value = 339)
  final case object GalaxyGunshipGun extends StatisticalElement(value = 340)
  final case object GalaxyGunshipTailgun extends StatisticalElement(value = 342)
  final case object Gauss extends StatisticalElement(value = 345)
  final case object GaussCannon extends StatisticalElement(value = 346)
  final case object GrenadeLauncherMarauder extends StatisticalElement(value = 371)
  final case object HeavyRailBeamMagrider extends StatisticalElement(value = 394)
  final case object HeavySniper extends StatisticalElement(value = 396)
  final case object Hellfire extends StatisticalElement(value = 398)
  final case object Hunterseeker extends StatisticalElement(value = 406)
  final case object Ilc9 extends StatisticalElement(value = 407)
  final case object Isp extends StatisticalElement(value = 411)
  final case object JammerGrenade extends StatisticalElement(value = 416)
  final case object Katana extends StatisticalElement(value = 421)
  final case object Lancer extends StatisticalElement(value = 425)
  final case object Lasher extends StatisticalElement(value = 429)
  final case object Liberator25mmCannon extends StatisticalElement(value = 433)
  final case object LiberatorBombBay extends StatisticalElement(value = 435)
  final case object LiberatorWeaponSystem extends StatisticalElement(value = 440)
  final case object LightgunshipWeaponSystem extends StatisticalElement(value = 445)
  final case object LightningWeaponSystem extends StatisticalElement(value = 448)
  final case object Maelstrom extends StatisticalElement(value = 462)
  final case object Magcutter extends StatisticalElement(value = 468)
  final case object MediumTransportWeaponSystemA extends StatisticalElement(value = 534)
  final case object MediumTransportWeaponSystemB extends StatisticalElement(value = 535)
  final case object MineSweeper extends StatisticalElement(value = 552)
  final case object MiniChaingun extends StatisticalElement(value = 556)
  final case object NchevFalcon extends StatisticalElement(value = 587)
  final case object NchevScattercannon extends StatisticalElement(value = 588)
  final case object NchevSparrow extends StatisticalElement(value = 589)
  final case object Oicw extends StatisticalElement(value = 599)
  final case object ParticleBeamMagrider extends StatisticalElement(value = 628)
  final case object PelletGun extends StatisticalElement(value = 629)
  final case object PeregrineArmorSiphon extends StatisticalElement(value = 633)
  final case object PeregrineDualMachineGun extends StatisticalElement(value = 636)
  final case object PeregrineDualRocketPods extends StatisticalElement(value = 641)
  final case object PeregrineMechhammer extends StatisticalElement(value = 644)
  final case object PeregrineNtuSiphon extends StatisticalElement(value = 649)
  final case object PeregrineParticleCannon extends StatisticalElement(value = 652)
  final case object PeregrineSparrow extends StatisticalElement(value = 658)
  final case object PhalanxAvcombo extends StatisticalElement(value = 666)
  final case object PhalanxFlakcombo extends StatisticalElement(value = 668)
  final case object PhalanxSglHevgatcan extends StatisticalElement(value = 670)
  final case object Phantasm12mmMachinegun extends StatisticalElement(value = 672)
  final case object Phoenix extends StatisticalElement(value = 673)
  final case object PlasmaGrenade extends StatisticalElement(value = 680)
  final case object ProwlerWeaponSystemA extends StatisticalElement(value = 699)
  final case object ProwlerWeaponSystemB extends StatisticalElement(value = 700)
  final case object Pulsar extends StatisticalElement(value = 701)
  final case object PulsedParticleAccelerator extends StatisticalElement(value = 705)
  final case object Punisher extends StatisticalElement(value = 706)
  final case object QuadAssaultWeaponSystem extends StatisticalElement(value = 709)
  final case object RShotgun extends StatisticalElement(value = 714)
  final case object Radiator extends StatisticalElement(value = 716)
  final case object Repeater extends StatisticalElement(value = 730)
  final case object Rocklet extends StatisticalElement(value = 737)
  final case object RotaryChaingunMosquito extends StatisticalElement(value = 740)
  final case object Scythe extends StatisticalElement(value = 747)
  final case object SixShooter extends StatisticalElement(value = 761)
  final case object SkyguardWeaponSystem extends StatisticalElement(value = 788)
  final case object Spiker extends StatisticalElement(value = 817)
  final case object Striker extends StatisticalElement(value = 838)
  final case object Suppressor extends StatisticalElement(value = 845)
  final case object Thumper extends StatisticalElement(value = 864)
  final case object ThundererWeaponSystemA extends StatisticalElement(value = 866)
  final case object ThundererWeaponSystemB extends StatisticalElement(value = 867)
  final case object TrhevBurster extends StatisticalElement(value = 888)
  final case object TrhevDualcycler extends StatisticalElement(value = 889)
  final case object TrhevPounder extends StatisticalElement(value = 890)
  final case object VanguardWeapon150mm extends StatisticalElement(value = 925)
  final case object VanguardWeapon20mm extends StatisticalElement(value = 926)
  final case object VanguardWeaponSystem extends StatisticalElement(value = 927)
  final case object VanuSentryTurretWeapon extends StatisticalElement(value = 945)
  final case object VshevComet extends StatisticalElement(value = 968)
  final case object VshevQuasar extends StatisticalElement(value = 969)
  final case object VshevStarfire extends StatisticalElement(value = 970)
  final case object VultureBombBay extends StatisticalElement(value = 987)
  final case object VultureNoseWeaponSystem extends StatisticalElement(value = 990)
  final case object VultureTailCannon extends StatisticalElement(value = 992)
  final case object WaspWeaponSystem extends StatisticalElement(value = 1002)
  final case object Winchester extends StatisticalElement(value = 1003)

  final case object AphelionPlasmaCloud extends StatisticalElement(value = 96)
  final case object Boomer extends StatisticalElement(value = 148)
  final case object HeMine extends StatisticalElement(value = 388)
  final case object Knife extends StatisticalElement(value = 424)
  final case object LightgunshipWeapon20mm extends StatisticalElement(value = 443)
  final case object LightgunshipWeaponRocket extends StatisticalElement(value = 444)
  final case object MedicalApplicator extends StatisticalElement(value = 531)
  final case object OrbitalStrikeBig extends StatisticalElement(value = 609)
  final case object OrbitalStrikeSmall extends StatisticalElement(value = 610)
  final case object VanuModule extends StatisticalElement(value = 934)
  final case object VanuModuleBeam extends StatisticalElement(value = 950)

  final case object AMS extends StatisticalElement(value = 46)
  final case object ANT extends StatisticalElement(value = 60)
  final case object Sunderer extends StatisticalElement(value = 62)
  final case object ApcTr extends StatisticalElement(value = 66)
  final case object ApcNc extends StatisticalElement(value = 67)
  final case object ApcVs extends StatisticalElement(value = 68)
  final case object Aphelion extends StatisticalElement(value = 79)
  final case object AphelionFlight extends StatisticalElement(value = 83)
  final case object AphelionGunner extends StatisticalElement(value = 84)
  final case object Battlewagon extends StatisticalElement(value = 118) //aurora
  final case object Infantry extends StatisticalElement(value = 121)
  final case object Raider extends StatisticalElement(value = 135)
  final case object BfrTerminal extends StatisticalElement(value = 143)
  final case object Colossus extends StatisticalElement(value = 179)
  final case object ColossusFlight extends StatisticalElement(value = 199)
  final case object ColossusGunner extends StatisticalElement(value = 200)
  final case object Door extends StatisticalElement(value = 242)
  final case object Droppod extends StatisticalElement(value = 258)
  final case object Dropship extends StatisticalElement(value = 259)
  final case object Facilities extends StatisticalElement(value = 284)
  final case object Flail extends StatisticalElement(value = 294)
  final case object Fury extends StatisticalElement(value = 335)
  final case object GalaxyGunship extends StatisticalElement(value = 338)
  final case object Generator extends StatisticalElement(value = 351)
  final case object Vehicle extends StatisticalElement(value = 356)
  final case object MechanizedAssaultExoSuit extends StatisticalElement(value = 390)
  final case object ImplantTerminalMech extends StatisticalElement(value = 410)
  final case object Liberator extends StatisticalElement(value = 432)
  final case object Lightgunship extends StatisticalElement(value = 441)
  final case object Locker extends StatisticalElement(value = 456)
  final case object Lightning extends StatisticalElement(value = 446)
  final case object AgileExoSuit extends StatisticalElement(value = 449)
  final case object Lodestar extends StatisticalElement(value = 459)
  final case object Magrider extends StatisticalElement(value = 470)
  final case object PhalanxTurret extends StatisticalElement(value = 480)
  final case object ReinforcedExoSuit extends StatisticalElement(value = 528)
  final case object MedicalTerminal extends StatisticalElement(value = 529)
  final case object MediumTransport extends StatisticalElement(value = 532)
  final case object MonolithAmerish extends StatisticalElement(value = 560)
  final case object MonolithCeryshen extends StatisticalElement(value = 562)
  final case object MonolithCyssor extends StatisticalElement(value = 563)
  final case object MonolithEsamir extends StatisticalElement(value = 564)
  final case object MonolithForseral extends StatisticalElement(value = 566)
  final case object MonolithHossin extends StatisticalElement(value = 567)
  final case object MonolithIshundar extends StatisticalElement(value = 568)
  final case object MonolithSearhus extends StatisticalElement(value = 569)
  final case object MonolithSolsar extends StatisticalElement(value = 570)
  final case object Mosquito extends StatisticalElement(value = 572)
  final case object EquipmentTerminal extends StatisticalElement(value = 612)
  final case object Peregrine extends StatisticalElement(value = 632)
  final case object PeregrineFlight extends StatisticalElement(value = 642)
  final case object PeregrineGunner extends StatisticalElement(value = 643)
  final case object Phantasm extends StatisticalElement(value = 671)
  final case object PortableMannedTurretTr extends StatisticalElement(value = 686)
  final case object PortableMannedTurretNc extends StatisticalElement(value = 687)
  final case object PortableMannedTurretVs extends StatisticalElement(value = 688)
  final case object Prowler extends StatisticalElement(value = 697)
  final case object QuadAssault extends StatisticalElement(value = 707)
  final case object QuadStealth extends StatisticalElement(value = 710)
  final case object Redoubt extends StatisticalElement(value = 726)
  final case object RespawnTube extends StatisticalElement(value = 732)
  final case object Router extends StatisticalElement(value = 741)
  final case object RouterTelepadDeployable extends StatisticalElement(value = 744)
  final case object Skyguard extends StatisticalElement(value = 784)
  final case object SpitfireAA extends StatisticalElement(value = 819)
  final case object SpitfireCloaked extends StatisticalElement(value = 825)
  final case object SpitfireTurret extends StatisticalElement(value = 826)
  final case object StandardExoSuit extends StatisticalElement(value = 829)
  final case object AirToAir extends StatisticalElement(value = 832)
  final case object InfiltrationExoSuit extends StatisticalElement(value = 837)
  final case object Switchblade extends StatisticalElement(value = 847)
  final case object TankTraps extends StatisticalElement(value = 849)
  final case object Terminal extends StatisticalElement(value = 854)
  final case object ThreeManHeavyBuggy extends StatisticalElement(value = 862)
  final case object Thunderer extends StatisticalElement(value = 865)
  final case object Tower extends StatisticalElement(value = 868)
  final case object TwoManAssaultBuggy extends StatisticalElement(value = 896)
  final case object TwoManHeavyBuggy extends StatisticalElement(value = 898)
  final case object TwoManHoverBuggy extends StatisticalElement(value = 900)
  final case object Vanguard extends StatisticalElement(value = 923)
  final case object VanuControlPoint extends StatisticalElement(value = 931)
  final case object VanSentryTurret extends StatisticalElement(value = 943)
  final case object VanuVehicleStation extends StatisticalElement(value = 948)
  final case object VehicleTerminal extends StatisticalElement(value = 953)
  final case object Vulture extends StatisticalElement(value = 986)
  final case object Wasp extends StatisticalElement(value = 997)
  final case object XmasCharlie1 extends StatisticalElement(value = 1007)
  final case object XmasCharlie2 extends StatisticalElement(value = 1008)
  final case object XmasCharlie3 extends StatisticalElement(value = 1009)
  final case object XmasCharlie4 extends StatisticalElement(value = 1010)
  final case object XmasCharlie5 extends StatisticalElement(value = 1011)
  final case object XmasCharlie6 extends StatisticalElement(value = 1012)
  final case object XmasCharlie7 extends StatisticalElement(value = 1013)
  final case object XmasCharlie8 extends StatisticalElement(value = 1014)
  final case object XmasCharlie9 extends StatisticalElement(value = 1015)
  final case object XmasGingermanAtar extends StatisticalElement(value = 1017)
  final case object XmasGingermanDahaka extends StatisticalElement(value = 1018)
  final case object XmasGingermanHvar extends StatisticalElement(value = 1019)
  final case object XmasGingermanIzha extends StatisticalElement(value = 1020)
  final case object XmasGingermanJamshid extends StatisticalElement(value = 1021)
  final case object XmasGingermanMithra extends StatisticalElement(value = 1022)
  final case object XmasGingermanRashnu extends StatisticalElement(value = 1023)
  final case object XmasGingermanSraosha extends StatisticalElement(value = 1024)
  final case object XmasGingermanYazata extends StatisticalElement(value = 1025)
  final case object XmasGingermanZal extends StatisticalElement(value = 1026)
  final case object XmasSled1 extends StatisticalElement(value = 1028)
  final case object XmasSled2 extends StatisticalElement(value = 1029)
  final case object XmasSled3 extends StatisticalElement(value = 1030)
  final case object XmasSled4 extends StatisticalElement(value = 1031)
  final case object XmasSled5 extends StatisticalElement(value = 1032)
  final case object XmasSled6 extends StatisticalElement(value = 1033)
  final case object XmasSled7 extends StatisticalElement(value = 1034)
  final case object XmasSled8 extends StatisticalElement(value = 1035)
  final case object XmasSled9 extends StatisticalElement(value = 1036)
  final case object XmasSnowmanAmerish extends StatisticalElement(value = 1038)
  final case object XmasSnowmanCeryshen extends StatisticalElement(value = 1039)
  final case object XmasSnowmanCyssor extends StatisticalElement(value = 1040)
  final case object XmasSnowmanEsamir extends StatisticalElement(value = 1041)
  final case object XmasSnowmanForseral extends StatisticalElement(value = 1042)
  final case object XmasSnowmanHossin extends StatisticalElement(value = 1043)
  final case object XmasSnowmanIshundar extends StatisticalElement(value = 1044)
  final case object XmasSnowmanSearhus extends StatisticalElement(value = 1045)
  final case object XmasSnowmanSolsar extends StatisticalElement(value = 1046)
}
