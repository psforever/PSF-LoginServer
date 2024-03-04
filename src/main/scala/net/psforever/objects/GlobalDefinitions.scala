// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ballistics._
import net.psforever.objects.ce.DeployedItem
import net.psforever.objects.definition._
import net.psforever.objects.definition.converter._
import net.psforever.objects.equipment._
import net.psforever.objects.global.{GlobalDefinitionsAmmo, GlobalDefinitionsDeployable, GlobalDefinitionsExoSuit, GlobalDefinitionsKit, GlobalDefinitionsMiscellaneous, GlobalDefinitionsProjectile, GlobalDefinitionsTools, GlobalDefinitionsVehicle}
import net.psforever.objects.locker.LockerContainerDefinition
import net.psforever.objects.serverobject.doors.DoorDefinition
import net.psforever.objects.serverobject.generator.GeneratorDefinition
import net.psforever.objects.serverobject.locks.IFFLockDefinition
import net.psforever.objects.serverobject.mblocker.LockerDefinition
import net.psforever.objects.serverobject.pad.VehicleSpawnPadDefinition
import net.psforever.objects.serverobject.painbox.PainboxDefinition
import net.psforever.objects.serverobject.terminals._
import net.psforever.objects.serverobject.tube.SpawnTubeDefinition
import net.psforever.objects.serverobject.resourcesilo.ResourceSiloDefinition
import net.psforever.objects.serverobject.structures.{AmenityDefinition, BuildingDefinition, WarpGateDefinition}
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminalDefinition
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalMechDefinition
import net.psforever.objects.serverobject.turret.FacilityTurretDefinition
import net.psforever.objects.vehicles.InternalTelepadDefinition
import net.psforever.objects.vital.etc.{ShieldAgainstRadiation => _}
import net.psforever.objects.vital._
import net.psforever.types.{ExoSuitType, ImplantType, PlanetSideEmpire, Vector3}
import net.psforever.types._
import net.psforever.objects.serverobject.llu.{CaptureFlagDefinition, CaptureFlagSocketDefinition}

import scala.annotation.switch

object GlobalDefinitions {
  /*
  characters
   */
  val avatar = new AvatarDefinition(121)
  avatar.MaxHealth = 100
  avatar.Damageable = true
  avatar.DrownAtMaxDepth = true
  avatar.MaxDepth = 1.609375f //Male, standing, not MAX
  avatar.UnderwaterLifespan(suffocation = 60000L, recovery = 10000L)
  avatar.collision.xy = CollisionXYData(Array((0.1f, 0), (0.2f, 5), (0.50f, 15), (0.75f, 20), (1f, 30))) //not in the ADB
  avatar.collision.z = CollisionZData(Array((0.1f, 0), (5f, 1), (10f, 3), (20f, 5), (35f, 7), (50f, 10), (75f, 40), (100f, 100))) //not in the ADB
  avatar.maxForwardSpeed = 27f //not in the ADB; running speed
  /*
  exo-suits
   */
  val Standard = ExoSuitDefinition(ExoSuitType.Standard)

  val Agile = ExoSuitDefinition(ExoSuitType.Agile)

  val Reinforced = ExoSuitDefinition(ExoSuitType.Reinforced)

  val Infiltration = ExoSuitDefinition(ExoSuitType.Infiltration)

  val VSMAX = SpecialExoSuitDefinition(ExoSuitType.MAX)

  val TRMAX = SpecialExoSuitDefinition(ExoSuitType.MAX)

  val NCMAX = SpecialExoSuitDefinition(ExoSuitType.MAX)

  /*
  Implants
   */
  val advanced_regen = new ImplantDefinition(ImplantType.AdvancedRegen) {
    Name = "advanced_regen"
  }
  advanced_regen.InitializationDuration = 120
  advanced_regen.StaminaCost = 2
  advanced_regen.CostIntervalDefault = 500

  val targeting = new ImplantDefinition(ImplantType.Targeting) {
    Name = "targeting"
  }
  targeting.InitializationDuration = 60

  val audio_amplifier = new ImplantDefinition(ImplantType.AudioAmplifier) {
    Name = "audio_amplifier"
  }
  audio_amplifier.InitializationDuration = 60
  audio_amplifier.StaminaCost = 1
  audio_amplifier.CostIntervalDefault = 1000

  val darklight_vision = new ImplantDefinition(ImplantType.DarklightVision) {
    Name = "darklight_vision"
  }
  darklight_vision.InitializationDuration = 60
  darklight_vision.ActivationStaminaCost = 3
  darklight_vision.StaminaCost = 1
  darklight_vision.CostIntervalDefault = 500

  val melee_booster = new ImplantDefinition(ImplantType.MeleeBooster) {
    Name = "melee_booster"
  }
  melee_booster.InitializationDuration = 120
  melee_booster.StaminaCost = 10

  val personal_shield = new ImplantDefinition(ImplantType.PersonalShield) {
    Name = "personal_shield"
  }
  personal_shield.InitializationDuration = 120
  personal_shield.StaminaCost = 1
  personal_shield.CostIntervalDefault = 600

  val range_magnifier = new ImplantDefinition(ImplantType.RangeMagnifier) {
    Name = "range_magnifier"
  }
  range_magnifier.InitializationDuration = 60

  val second_wind = new ImplantDefinition(ImplantType.SecondWind) {
    Name = "second_wind"
  }
  second_wind.InitializationDuration = 180

  val silent_run = new ImplantDefinition(ImplantType.SilentRun) {
    Name = "silent_run"
  }
  silent_run.InitializationDuration = 90
  silent_run.StaminaCost = 1
  silent_run.CostIntervalDefault = 333
  silent_run.CostIntervalByExoSuitHashMap(ExoSuitType.Infiltration) = 1000

  val surge = new ImplantDefinition(ImplantType.Surge) {
    Name = "surge"
  }
  surge.InitializationDuration = 90
  surge.StaminaCost = 1
  surge.CostIntervalDefault = 1000
  surge.CostIntervalByExoSuitHashMap(ExoSuitType.Agile) = 500
  surge.CostIntervalByExoSuitHashMap(ExoSuitType.Reinforced) = 333

  /*
  Projectiles
   */
  val no_projectile = new ProjectileDefinition(0) //also called none in ADB

  val bullet_105mm_projectile = ProjectileDefinition(Projectiles.Types.bullet_105mm_projectile)

  val bullet_12mm_projectile = ProjectileDefinition(Projectiles.Types.bullet_12mm_projectile)

  val bullet_12mm_projectileb = ProjectileDefinition(Projectiles.Types.bullet_12mm_projectileb)

  val bullet_150mm_projectile = ProjectileDefinition(Projectiles.Types.bullet_150mm_projectile)

  val bullet_15mm_apc_projectile = ProjectileDefinition(Projectiles.Types.bullet_15mm_apc_projectile)

  val bullet_15mm_projectile = ProjectileDefinition(Projectiles.Types.bullet_15mm_projectile)

  val bullet_20mm_apc_projectile = ProjectileDefinition(Projectiles.Types.bullet_20mm_apc_projectile)

  val bullet_20mm_projectile = ProjectileDefinition(Projectiles.Types.bullet_20mm_projectile)

  val bullet_25mm_projectile = ProjectileDefinition(Projectiles.Types.bullet_25mm_projectile)

  val bullet_35mm_projectile = ProjectileDefinition(Projectiles.Types.bullet_35mm_projectile)

  val bullet_75mm_apc_projectile = ProjectileDefinition(Projectiles.Types.bullet_75mm_apc_projectile)

  val bullet_75mm_projectile = ProjectileDefinition(Projectiles.Types.bullet_75mm_projectile)

  val bullet_9mm_AP_projectile = ProjectileDefinition(Projectiles.Types.bullet_9mm_AP_projectile)

  val bullet_9mm_projectile = ProjectileDefinition(Projectiles.Types.bullet_9mm_projectile)

  val anniversary_projectilea = ProjectileDefinition(Projectiles.Types.anniversary_projectilea)

  val anniversary_projectileb = ProjectileDefinition(Projectiles.Types.anniversary_projectileb)

  val aphelion_immolation_cannon_projectile = ProjectileDefinition(Projectiles.Types.aphelion_immolation_cannon_projectile)

  val aphelion_laser_projectile = ProjectileDefinition(Projectiles.Types.aphelion_laser_projectile)

  val aphelion_plasma_cloud = ProjectileDefinition(Projectiles.Types.aphelion_plasma_cloud)

  val aphelion_plasma_rocket_projectile = ProjectileDefinition(Projectiles.Types.aphelion_plasma_rocket_projectile)

  val aphelion_ppa_projectile = ProjectileDefinition(Projectiles.Types.aphelion_ppa_projectile)

  val aphelion_starfire_projectile = ProjectileDefinition(Projectiles.Types.aphelion_starfire_projectile)

  val bolt_projectile = ProjectileDefinition(Projectiles.Types.bolt_projectile)

  val burster_projectile = ProjectileDefinition(Projectiles.Types.burster_projectile)

  val chainblade_projectile = ProjectileDefinition(Projectiles.Types.chainblade_projectile)

  val colossus_100mm_projectile = ProjectileDefinition(Projectiles.Types.colossus_100mm_projectile)

  val colossus_burster_projectile = ProjectileDefinition(Projectiles.Types.colossus_burster_projectile)

  val colossus_chaingun_projectile = ProjectileDefinition(Projectiles.Types.colossus_chaingun_projectile)

  val colossus_cluster_bomb_projectile = ProjectileDefinition(Projectiles.Types.colossus_cluster_bomb_projectile)

  val colossus_tank_cannon_projectile = ProjectileDefinition(Projectiles.Types.colossus_tank_cannon_projectile)

  val comet_projectile = ProjectileDefinition(Projectiles.Types.comet_projectile)

  val dualcycler_projectile = ProjectileDefinition(Projectiles.Types.dualcycler_projectile)

  val dynomite_projectile = ProjectileDefinition(Projectiles.Types.dynomite_projectile)

  val energy_cell_projectile = ProjectileDefinition(Projectiles.Types.energy_cell_projectile)

  val energy_gun_nc_projectile = ProjectileDefinition(Projectiles.Types.energy_gun_nc_projectile)

  val energy_gun_tr_projectile = ProjectileDefinition(Projectiles.Types.energy_gun_tr_projectile)

  val energy_gun_vs_projectile = ProjectileDefinition(Projectiles.Types.energy_gun_vs_projectile)

  val enhanced_energy_cell_projectile = ProjectileDefinition(Projectiles.Types.enhanced_energy_cell_projectile)

  val enhanced_quasar_projectile = ProjectileDefinition(Projectiles.Types.enhanced_quasar_projectile)

  val falcon_projectile = ProjectileDefinition(Projectiles.Types.falcon_projectile)

  val firebird_missile_projectile = ProjectileDefinition(Projectiles.Types.firebird_missile_projectile)

  val flail_projectile = ProjectileDefinition(Projectiles.Types.flail_projectile)

  val flamethrower_fire_cloud = ProjectileDefinition(Projectiles.Types.flamethrower_fire_cloud)

  val flamethrower_fireball = ProjectileDefinition(Projectiles.Types.flamethrower_fireball)

  val flamethrower_projectile = ProjectileDefinition(Projectiles.Types.flamethrower_projectile)

  val flux_cannon_apc_projectile = ProjectileDefinition(Projectiles.Types.flux_cannon_apc_projectile)

  val flux_cannon_thresher_projectile = ProjectileDefinition(Projectiles.Types.flux_cannon_thresher_projectile)

  val fluxpod_projectile = ProjectileDefinition(Projectiles.Types.fluxpod_projectile)

  val forceblade_projectile = ProjectileDefinition(Projectiles.Types.forceblade_projectile)

  val frag_cartridge_projectile = ProjectileDefinition(Projectiles.Types.frag_cartridge_projectile)

  val frag_cartridge_projectile_b = ProjectileDefinition(Projectiles.Types.frag_cartridge_projectile_b)

  val frag_grenade_projectile = ProjectileDefinition(Projectiles.Types.frag_grenade_projectile)

  val frag_grenade_projectile_enh = ProjectileDefinition(Projectiles.Types.frag_grenade_projectile_enh)

  val galaxy_gunship_gun_projectile = ProjectileDefinition(Projectiles.Types.galaxy_gunship_gun_projectile)

  val gauss_cannon_projectile = ProjectileDefinition(Projectiles.Types.gauss_cannon_projectile)

  val grenade_projectile = ProjectileDefinition(Projectiles.Types.grenade_projectile)

  val heavy_grenade_projectile = ProjectileDefinition(Projectiles.Types.heavy_grenade_projectile)

  val heavy_rail_beam_projectile = ProjectileDefinition(Projectiles.Types.heavy_rail_beam_projectile)

  val heavy_sniper_projectile = ProjectileDefinition(Projectiles.Types.heavy_sniper_projectile)

  val hellfire_projectile = ProjectileDefinition(Projectiles.Types.hellfire_projectile)

  val hunter_seeker_missile_dumbfire = ProjectileDefinition(Projectiles.Types.hunter_seeker_missile_dumbfire)

  val hunter_seeker_missile_projectile = ProjectileDefinition(Projectiles.Types.hunter_seeker_missile_projectile)

  val jammer_cartridge_projectile = ProjectileDefinition(Projectiles.Types.jammer_cartridge_projectile)

  val jammer_cartridge_projectile_b = ProjectileDefinition(Projectiles.Types.jammer_cartridge_projectile_b)

  val jammer_grenade_projectile = ProjectileDefinition(Projectiles.Types.jammer_grenade_projectile)

  val jammer_grenade_projectile_enh = ProjectileDefinition(Projectiles.Types.jammer_grenade_projectile_enh)

  val katana_projectile = ProjectileDefinition(Projectiles.Types.katana_projectile)

  val katana_projectileb = ProjectileDefinition(Projectiles.Types.katana_projectileb)

  val lancer_projectile = ProjectileDefinition(Projectiles.Types.lancer_projectile)

  val lasher_projectile = ProjectileDefinition(Projectiles.Types.lasher_projectile)

  val lasher_projectile_ap = ProjectileDefinition(Projectiles.Types.lasher_projectile_ap)

  val liberator_bomb_cluster_bomblet_projectile = ProjectileDefinition(
    Projectiles.Types.liberator_bomb_cluster_bomblet_projectile
  )

  val liberator_bomb_cluster_projectile = ProjectileDefinition(Projectiles.Types.liberator_bomb_cluster_projectile)

  val liberator_bomb_projectile = ProjectileDefinition(Projectiles.Types.liberator_bomb_projectile)

  val maelstrom_grenade_damager = ProjectileDefinition(Projectiles.Types.maelstrom_grenade_damager)

  val maelstrom_grenade_projectile = ProjectileDefinition(Projectiles.Types.maelstrom_grenade_projectile)

  val maelstrom_grenade_projectile_contact = ProjectileDefinition(Projectiles.Types.maelstrom_grenade_projectile_contact)

  val maelstrom_stream_projectile = ProjectileDefinition(Projectiles.Types.maelstrom_stream_projectile)

  val magcutter_projectile = ProjectileDefinition(Projectiles.Types.magcutter_projectile)

  val melee_ammo_projectile = ProjectileDefinition(Projectiles.Types.melee_ammo_projectile)

  val meteor_common = ProjectileDefinition(Projectiles.Types.meteor_common)

  val meteor_projectile_b_large = ProjectileDefinition(Projectiles.Types.meteor_projectile_b_large)

  val meteor_projectile_b_medium = ProjectileDefinition(Projectiles.Types.meteor_projectile_b_medium)

  val meteor_projectile_b_small = ProjectileDefinition(Projectiles.Types.meteor_projectile_b_small)

  val meteor_projectile_large = ProjectileDefinition(Projectiles.Types.meteor_projectile_large)

  val meteor_projectile_medium = ProjectileDefinition(Projectiles.Types.meteor_projectile_medium)

  val meteor_projectile_small = ProjectileDefinition(Projectiles.Types.meteor_projectile_small)

  val mine_projectile = ProjectileDefinition(Projectiles.Types.mine_projectile)

  val mine_sweeper_projectile = ProjectileDefinition(Projectiles.Types.mine_sweeper_projectile)

  val mine_sweeper_projectile_enh = ProjectileDefinition(Projectiles.Types.mine_sweeper_projectile_enh)

  val oicw_projectile = ProjectileDefinition(Projectiles.Types.oicw_projectile)

  val oicw_little_buddy = ProjectileDefinition(Projectiles.Types.oicw_little_buddy)

  val pellet_gun_projectile = ProjectileDefinition(Projectiles.Types.pellet_gun_projectile)

  val peregrine_dual_machine_gun_projectile = ProjectileDefinition(Projectiles.Types.peregrine_dual_machine_gun_projectile)

  val peregrine_mechhammer_projectile = ProjectileDefinition(Projectiles.Types.peregrine_mechhammer_projectile)

  val peregrine_particle_cannon_projectile = ProjectileDefinition(Projectiles.Types.peregrine_particle_cannon_projectile)

  val peregrine_particle_cannon_radiation_cloud = ProjectileDefinition(Projectiles.Types.peregrine_particle_cannon_radiation_cloud)

  val peregrine_rocket_pod_projectile = ProjectileDefinition(Projectiles.Types.peregrine_rocket_pod_projectile)

  val peregrine_sparrow_projectile = ProjectileDefinition(Projectiles.Types.peregrine_sparrow_projectile)

  val phalanx_av_projectile = ProjectileDefinition(Projectiles.Types.phalanx_av_projectile)

  val phalanx_flak_projectile = ProjectileDefinition(Projectiles.Types.phalanx_flak_projectile)

  val phalanx_projectile = ProjectileDefinition(Projectiles.Types.phalanx_projectile)

  val phoenix_missile_guided_projectile = ProjectileDefinition(Projectiles.Types.phoenix_missile_guided_projectile)

  val phoenix_missile_projectile = ProjectileDefinition(Projectiles.Types.phoenix_missile_projectile)

  val plasma_cartridge_projectile = ProjectileDefinition(Projectiles.Types.plasma_cartridge_projectile)

  val plasma_cartridge_projectile_b = ProjectileDefinition(Projectiles.Types.plasma_cartridge_projectile_b)

  val plasma_grenade_projectile = ProjectileDefinition(Projectiles.Types.plasma_grenade_projectile)

  val plasma_grenade_projectile_B = ProjectileDefinition(Projectiles.Types.plasma_grenade_projectile_B)

  val pounder_projectile = ProjectileDefinition(Projectiles.Types.pounder_projectile)

  val pounder_projectile_enh = ProjectileDefinition(Projectiles.Types.pounder_projectile_enh)

  val ppa_projectile = ProjectileDefinition(Projectiles.Types.ppa_projectile)

  val pulsar_ap_projectile = ProjectileDefinition(Projectiles.Types.pulsar_ap_projectile)

  val pulsar_projectile = ProjectileDefinition(Projectiles.Types.pulsar_projectile)

  val quasar_projectile = ProjectileDefinition(Projectiles.Types.quasar_projectile)

  val radiator_cloud = ProjectileDefinition(Projectiles.Types.radiator_cloud)

  val radiator_grenade_projectile = ProjectileDefinition(Projectiles.Types.radiator_grenade_projectile)

  val radiator_sticky_projectile = ProjectileDefinition(Projectiles.Types.radiator_sticky_projectile)

  val reaver_rocket_projectile = ProjectileDefinition(Projectiles.Types.reaver_rocket_projectile)

  val rocket_projectile = ProjectileDefinition(Projectiles.Types.rocket_projectile)

  val rocklet_flak_projectile = ProjectileDefinition(Projectiles.Types.rocklet_flak_projectile)

  val rocklet_jammer_projectile = ProjectileDefinition(Projectiles.Types.rocklet_jammer_projectile)

  val scattercannon_projectile = ProjectileDefinition(Projectiles.Types.scattercannon_projectile)

  val scythe_projectile = ProjectileDefinition(Projectiles.Types.scythe_projectile)

  val scythe_projectile_slave = ProjectileDefinition(Projectiles.Types.scythe_projectile_slave)

  val shotgun_shell_AP_projectile = ProjectileDefinition(Projectiles.Types.shotgun_shell_AP_projectile)

  val shotgun_shell_projectile = ProjectileDefinition(Projectiles.Types.shotgun_shell_projectile)

  val six_shooter_projectile = ProjectileDefinition(Projectiles.Types.six_shooter_projectile)

  val skyguard_flak_cannon_projectile = ProjectileDefinition(Projectiles.Types.skyguard_flak_cannon_projectile)

  val sparrow_projectile = ProjectileDefinition(Projectiles.Types.sparrow_projectile)

  val sparrow_secondary_projectile = ProjectileDefinition(Projectiles.Types.sparrow_secondary_projectile)

  val spiker_projectile = ProjectileDefinition(Projectiles.Types.spiker_projectile)

  val spitfire_aa_ammo_projectile = ProjectileDefinition(Projectiles.Types.spitfire_aa_ammo_projectile)

  val spitfire_ammo_projectile = ProjectileDefinition(Projectiles.Types.spitfire_ammo_projectile)

  val starfire_projectile = ProjectileDefinition(Projectiles.Types.starfire_projectile)

  val striker_missile_projectile = ProjectileDefinition(Projectiles.Types.striker_missile_projectile)

  val striker_missile_targeting_projectile = ProjectileDefinition(Projectiles.Types.striker_missile_targeting_projectile)

  val trek_projectile = ProjectileDefinition(Projectiles.Types.trek_projectile)

  val vanu_sentry_turret_projectile = ProjectileDefinition(Projectiles.Types.vanu_sentry_turret_projectile)

  val vulture_bomb_projectile = ProjectileDefinition(Projectiles.Types.vulture_bomb_projectile)

  val vulture_nose_bullet_projectile = ProjectileDefinition(Projectiles.Types.vulture_nose_bullet_projectile)

  val vulture_tail_bullet_projectile = ProjectileDefinition(Projectiles.Types.vulture_tail_bullet_projectile)

  val wasp_gun_projectile = ProjectileDefinition(Projectiles.Types.wasp_gun_projectile)

  val wasp_rocket_projectile = ProjectileDefinition(Projectiles.Types.wasp_rocket_projectile)

  val winchester_projectile = ProjectileDefinition(Projectiles.Types.winchester_projectile)

  val armor_siphon_projectile = ProjectileDefinition(Projectiles.Types.trek_projectile) //fake projectile for storing damage information

  val ntu_siphon_emp = ProjectileDefinition(Projectiles.Types.ntu_siphon_emp)

  /*
  Equipment (locker_container, kits, ammunition, weapons)
   */
  import net.psforever.packet.game.objectcreate.ObjectClass
  val locker_container = new LockerContainerDefinition()

  val medkit = KitDefinition(Kits.medkit)

  val super_medkit = KitDefinition(Kits.super_medkit)

  val super_armorkit = KitDefinition(Kits.super_armorkit)

  val super_staminakit = KitDefinition(Kits.super_staminakit) //super stimpak

  val melee_ammo = AmmoBoxDefinition(Ammo.melee_ammo)

  val frag_grenade_ammo = AmmoBoxDefinition(Ammo.frag_grenade_ammo)

  val plasma_grenade_ammo = AmmoBoxDefinition(Ammo.plasma_grenade_ammo)

  val jammer_grenade_ammo = AmmoBoxDefinition(Ammo.jammer_grenade_ammo)

  val bullet_9mm = AmmoBoxDefinition(Ammo.bullet_9mm)

  val bullet_9mm_AP = AmmoBoxDefinition(Ammo.bullet_9mm_AP)

  val shotgun_shell = AmmoBoxDefinition(Ammo.shotgun_shell)

  val shotgun_shell_AP = AmmoBoxDefinition(Ammo.shotgun_shell_AP)

  val energy_cell = AmmoBoxDefinition(Ammo.energy_cell)

  val anniversary_ammo = AmmoBoxDefinition(Ammo.anniversary_ammo) //10mm multi-phase

  val ancient_ammo_combo = AmmoBoxDefinition(Ammo.ancient_ammo_combo)

  val maelstrom_ammo = AmmoBoxDefinition(Ammo.maelstrom_ammo)

  val phoenix_missile = AmmoBoxDefinition(Ammo.phoenix_missile) //decimator missile

  val striker_missile_ammo = AmmoBoxDefinition(Ammo.striker_missile_ammo)

  val hunter_seeker_missile = AmmoBoxDefinition(Ammo.hunter_seeker_missile) //phoenix missile

  val lancer_cartridge = AmmoBoxDefinition(Ammo.lancer_cartridge)

  val rocket = AmmoBoxDefinition(Ammo.rocket)

  val frag_cartridge = AmmoBoxDefinition(Ammo.frag_cartridge)

  val plasma_cartridge = AmmoBoxDefinition(Ammo.plasma_cartridge)

  val jammer_cartridge = AmmoBoxDefinition(Ammo.jammer_cartridge)

  val bolt = AmmoBoxDefinition(Ammo.bolt)

  val oicw_ammo = AmmoBoxDefinition(Ammo.oicw_ammo) //scorpion missile

  val flamethrower_ammo = AmmoBoxDefinition(Ammo.flamethrower_ammo)

  val winchester_ammo = AmmoBoxDefinition(Ammo.winchester_ammo)

  val pellet_gun_ammo = AmmoBoxDefinition(Ammo.pellet_gun_ammo)

  val six_shooter_ammo = AmmoBoxDefinition(Ammo.six_shooter_ammo)

  val dualcycler_ammo = AmmoBoxDefinition(Ammo.dualcycler_ammo)

  val pounder_ammo = AmmoBoxDefinition(Ammo.pounder_ammo)

  val burster_ammo = AmmoBoxDefinition(Ammo.burster_ammo)

  val scattercannon_ammo = AmmoBoxDefinition(Ammo.scattercannon_ammo)

  val falcon_ammo = AmmoBoxDefinition(Ammo.falcon_ammo)

  val sparrow_ammo = AmmoBoxDefinition(Ammo.sparrow_ammo)

  val quasar_ammo = AmmoBoxDefinition(Ammo.quasar_ammo)

  val comet_ammo = AmmoBoxDefinition(Ammo.comet_ammo)

  val starfire_ammo = AmmoBoxDefinition(Ammo.starfire_ammo)

  val health_canister = AmmoBoxDefinition(Ammo.health_canister)

  val armor_canister = AmmoBoxDefinition(Ammo.armor_canister)

  val upgrade_canister = AmmoBoxDefinition(Ammo.upgrade_canister)

  val trek_ammo = AmmoBoxDefinition(Ammo.trek_ammo)

  val bullet_35mm = AmmoBoxDefinition(Ammo.bullet_35mm) //liberator nosegun

  val ancient_ammo_vehicle = AmmoBoxDefinition(Ammo.ancient_ammo_vehicle)

  val aphelion_laser_ammo = AmmoBoxDefinition(Ammo.aphelion_laser_ammo)

  val aphelion_immolation_cannon_ammo = AmmoBoxDefinition(Ammo.aphelion_immolation_cannon_ammo)

  val aphelion_plasma_rocket_ammo = AmmoBoxDefinition(Ammo.aphelion_plasma_rocket_ammo)

  val aphelion_ppa_ammo = AmmoBoxDefinition(Ammo.aphelion_ppa_ammo)

  val aphelion_starfire_ammo = AmmoBoxDefinition(Ammo.aphelion_starfire_ammo)

  val skyguard_flak_cannon_ammo = AmmoBoxDefinition(Ammo.skyguard_flak_cannon_ammo)

  val firebird_missile = AmmoBoxDefinition(ObjectClass.firebird_missile)

  val flux_cannon_thresher_battery = AmmoBoxDefinition(Ammo.flux_cannon_thresher_battery)

  val fluxpod_ammo = AmmoBoxDefinition(Ammo.fluxpod_ammo)

  val hellfire_ammo = AmmoBoxDefinition(Ammo.hellfire_ammo)

  val liberator_bomb = AmmoBoxDefinition(Ammo.liberator_bomb)

  val bullet_25mm = AmmoBoxDefinition(Ammo.bullet_25mm) //liberator tailgun

  val bullet_75mm = AmmoBoxDefinition(Ammo.bullet_75mm) //lightning shell

  val heavy_grenade_mortar = AmmoBoxDefinition(Ammo.heavy_grenade_mortar) //marauder and gal gunship

  val pulse_battery = AmmoBoxDefinition(Ammo.pulse_battery)

  val heavy_rail_beam_battery = AmmoBoxDefinition(Ammo.heavy_rail_beam_battery)

  val reaver_rocket = AmmoBoxDefinition(Ammo.reaver_rocket)

  val bullet_20mm = AmmoBoxDefinition(Ammo.bullet_20mm) //reaver nosegun

  val bullet_12mm = AmmoBoxDefinition(Ammo.bullet_12mm) //common

  val wasp_rocket_ammo = AmmoBoxDefinition(Ammo.wasp_rocket_ammo)

  val wasp_gun_ammo = AmmoBoxDefinition(Ammo.wasp_gun_ammo) //wasp nosegun

  val bullet_15mm = AmmoBoxDefinition(Ammo.bullet_15mm)

  val colossus_100mm_cannon_ammo = AmmoBoxDefinition(Ammo.colossus_100mm_cannon_ammo)

  val colossus_burster_ammo = AmmoBoxDefinition(Ammo.colossus_burster_ammo)

  val colossus_cluster_bomb_ammo = AmmoBoxDefinition(Ammo.colossus_cluster_bomb_ammo) //colossus mortar launcher shells

  val colossus_chaingun_ammo = AmmoBoxDefinition(Ammo.colossus_chaingun_ammo)

  val colossus_tank_cannon_ammo = AmmoBoxDefinition(Ammo.colossus_tank_cannon_ammo)

  val bullet_105mm = AmmoBoxDefinition(Ammo.bullet_105mm) //prowler 100mm cannon shell

  val gauss_cannon_ammo = AmmoBoxDefinition(Ammo.gauss_cannon_ammo)

  val peregrine_dual_machine_gun_ammo = AmmoBoxDefinition(Ammo.peregrine_dual_machine_gun_ammo)

  val peregrine_mechhammer_ammo = AmmoBoxDefinition(Ammo.peregrine_mechhammer_ammo)

  val peregrine_particle_cannon_ammo = AmmoBoxDefinition(Ammo.peregrine_particle_cannon_ammo)

  val peregrine_rocket_pod_ammo = AmmoBoxDefinition(Ammo.peregrine_rocket_pod_ammo)

  val peregrine_sparrow_ammo = AmmoBoxDefinition(Ammo.peregrine_sparrow_ammo)

  val bullet_150mm = AmmoBoxDefinition(Ammo.bullet_150mm)

  val phalanx_ammo = AmmoBoxDefinition(Ammo.phalanx_ammo)

  val spitfire_ammo = AmmoBoxDefinition(Ammo.spitfire_ammo)

  val spitfire_aa_ammo = AmmoBoxDefinition(Ammo.spitfire_aa_ammo)

  val energy_gun_ammo = AmmoBoxDefinition(Ammo.energy_gun_ammo)

  val armor_siphon_ammo = AmmoBoxDefinition(Ammo.armor_siphon_ammo)

  val ntu_siphon_ammo = AmmoBoxDefinition(Ammo.ntu_siphon_ammo)

  val chainblade = ToolDefinition(ObjectClass.chainblade)

  val magcutter = ToolDefinition(ObjectClass.magcutter)

  val forceblade = ToolDefinition(ObjectClass.forceblade)

  val katana = ToolDefinition(ObjectClass.katana)

  val frag_grenade = ToolDefinition(ObjectClass.frag_grenade)

  val plasma_grenade = ToolDefinition(ObjectClass.plasma_grenade)

  val jammer_grenade = ToolDefinition(ObjectClass.jammer_grenade)

  val repeater = ToolDefinition(ObjectClass.repeater)

  val isp = ToolDefinition(ObjectClass.isp) //mag-scatter

  val beamer = ToolDefinition(ObjectClass.beamer)

  val ilc9 = ToolDefinition(ObjectClass.ilc9) //amp

  val suppressor = ToolDefinition(ObjectClass.suppressor)

  val punisher = ToolDefinition(ObjectClass.punisher)

  val flechette = ToolDefinition(ObjectClass.flechette) //sweeper

  val cycler = ToolDefinition(ObjectClass.cycler)

  val gauss = ToolDefinition(ObjectClass.gauss)

  val pulsar = ToolDefinition(ObjectClass.pulsar)

  val anniversary_guna = ToolDefinition(ObjectClass.anniversary_guna) //tr stinger

  val anniversary_gun = ToolDefinition(ObjectClass.anniversary_gun) //nc spear

  val anniversary_gunb = ToolDefinition(ObjectClass.anniversary_gunb) //vs eraser

  val spiker = ToolDefinition(ObjectClass.spiker)

  val mini_chaingun = ToolDefinition(ObjectClass.mini_chaingun)

  val r_shotgun = ToolDefinition(ObjectClass.r_shotgun) //jackhammer

  val lasher = ToolDefinition(ObjectClass.lasher)

  val maelstrom = ToolDefinition(ObjectClass.maelstrom)

  val phoenix = ToolDefinition(ObjectClass.phoenix) //decimator

  val striker = ToolDefinition(ObjectClass.striker)

  val hunterseeker = ToolDefinition(ObjectClass.hunterseeker)

  val lancer = ToolDefinition(ObjectClass.lancer)

  val rocklet = ToolDefinition(ObjectClass.rocklet)

  val thumper = ToolDefinition(ObjectClass.thumper)

  val radiator = ToolDefinition(ObjectClass.radiator)

  val heavy_sniper = ToolDefinition(ObjectClass.heavy_sniper) //hsr

  val bolt_driver = ToolDefinition(ObjectClass.bolt_driver)

  val oicw = ToolDefinition(ObjectClass.oicw)

  val flamethrower = ToolDefinition(ObjectClass.flamethrower)

  val winchester = ToolDefinition(ObjectClass.winchester)

  val pellet_gun = ToolDefinition(ObjectClass.pellet_gun)

  val six_shooter = ToolDefinition(ObjectClass.six_shooter)

  val dynomite = ToolDefinition(ObjectClass.dynomite)

  val trhev_dualcycler = new ToolDefinition(ObjectClass.trhev_dualcycler) {
    Name = "trhev_dualcycler"

    override def NextFireModeIndex(index: Int): Int = index
  }

  val trhev_pounder = new ToolDefinition(ObjectClass.trhev_pounder) {
    Name = "trhev_pounder"

    override def NextFireModeIndex(index: Int): Int = {
      //TODO other modes
      if (index == 0 || index == 3) {
        if (index == 0) {
          3 //3-second fuse
        } else {
          0 //explode on contact
        }
      } else if (index == 1 || index == 4) {
        if (index == 1) {
          4 //3-second fuse, anchored
        } else {
          1 //explode on contact, anchored
        }
      } else {
        index
      }
    }
  }

  val trhev_burster = new ToolDefinition(ObjectClass.trhev_burster) {
    Name = "trhev_burster"

    override def NextFireModeIndex(index: Int): Int = index
  }

  val nchev_scattercannon = new ToolDefinition(ObjectClass.nchev_scattercannon) { Name = "nchev_scattercannon" }

  val nchev_falcon = new ToolDefinition(ObjectClass.nchev_falcon) { Name = "nchev_falcon" }

  val nchev_sparrow = new ToolDefinition(ObjectClass.nchev_sparrow) { Name = "nchev_sparrow" }

  val vshev_quasar = new ToolDefinition(ObjectClass.vshev_quasar) { Name = "vshev_quasar" }

  val vshev_comet = new ToolDefinition(ObjectClass.vshev_comet) { Name = "vshev_comet" }

  val vshev_starfire = new ToolDefinition(ObjectClass.vshev_starfire) { Name = "vshev_starfire" }

  val medicalapplicator = ToolDefinition(ObjectClass.medicalapplicator)

  val nano_dispenser = ToolDefinition(ObjectClass.nano_dispenser)

  val bank = ToolDefinition(ObjectClass.bank)

  val boomer_trigger = SimpleItemDefinition(SItem.boomer_trigger)

  val remote_electronics_kit = SimpleItemDefinition(SItem.remote_electronics_kit)

  val trek = ToolDefinition(ObjectClass.trek)

  val flail_targeting_laser = SimpleItemDefinition(SItem.flail_targeting_laser)

  val command_detonater = SimpleItemDefinition(SItem.command_detonater)

  val ace = ConstructionItemDefinition(CItem.ace)

  val advanced_ace = ConstructionItemDefinition(CItem.advanced_ace)

  val router_telepad = ConstructionItemDefinition(CItem.router_telepad)

  val fury_weapon_systema = ToolDefinition(ObjectClass.fury_weapon_systema)

  val quadassault_weapon_system = ToolDefinition(ObjectClass.quadassault_weapon_system)

  val scythe = ToolDefinition(ObjectClass.scythe) //TODO resolve ammo slot/pool discrepancy

  val chaingun_p = ToolDefinition(ObjectClass.chaingun_p)

  val skyguard_weapon_system = ToolDefinition(ObjectClass.skyguard_weapon_system)

  val grenade_launcher_marauder = ToolDefinition(ObjectClass.grenade_launcher_marauder)

  val advanced_missile_launcher_t = ToolDefinition(ObjectClass.advanced_missile_launcher_t)

  val flux_cannon_thresher = ToolDefinition(ObjectClass.flux_cannon_thresher)

  val mediumtransport_weapon_systemA = ToolDefinition(ObjectClass.mediumtransport_weapon_systemA)

  val mediumtransport_weapon_systemB = ToolDefinition(ObjectClass.mediumtransport_weapon_systemB)

  val battlewagon_weapon_systema = ToolDefinition(ObjectClass.battlewagon_weapon_systema)

  val battlewagon_weapon_systemb = ToolDefinition(ObjectClass.battlewagon_weapon_systemb)

  val battlewagon_weapon_systemc = ToolDefinition(ObjectClass.battlewagon_weapon_systemc)

  val battlewagon_weapon_systemd = ToolDefinition(ObjectClass.battlewagon_weapon_systemd)

  val thunderer_weapon_systema = ToolDefinition(ObjectClass.thunderer_weapon_systema)

  val thunderer_weapon_systemb = ToolDefinition(ObjectClass.thunderer_weapon_systemb)

  val aurora_weapon_systema = ToolDefinition(ObjectClass.aurora_weapon_systema)

  val aurora_weapon_systemb = ToolDefinition(ObjectClass.aurora_weapon_systemb)

  val apc_weapon_systema = ToolDefinition(ObjectClass.apc_weapon_systema)

  val apc_weapon_systemb = ToolDefinition(ObjectClass.apc_weapon_systemb)

  val apc_ballgun_r = ToolDefinition(ObjectClass.apc_ballgun_r)

  val apc_ballgun_l = ToolDefinition(ObjectClass.apc_ballgun_l)

  val apc_weapon_systemc_tr = ToolDefinition(ObjectClass.apc_weapon_systemc_tr)

  val apc_weapon_systemd_tr = ToolDefinition(ObjectClass.apc_weapon_systemd_tr)

  val apc_weapon_systemc_nc = ToolDefinition(ObjectClass.apc_weapon_systemc_nc)

  val apc_weapon_systemd_nc = ToolDefinition(ObjectClass.apc_weapon_systemd_nc)

  val apc_weapon_systemc_vs = ToolDefinition(ObjectClass.apc_weapon_systemc_vs)

  val apc_weapon_systemd_vs = ToolDefinition(ObjectClass.apc_weapon_systemd_vs)

  val lightning_weapon_system = ToolDefinition(ObjectClass.lightning_weapon_system)

  val prowler_weapon_systemA = ToolDefinition(ObjectClass.prowler_weapon_systemA)

  val prowler_weapon_systemB = ToolDefinition(ObjectClass.prowler_weapon_systemB)

  val vanguard_weapon_system = ToolDefinition(ObjectClass.vanguard_weapon_system)

  val particle_beam_magrider = ToolDefinition(ObjectClass.particle_beam_magrider)

  val heavy_rail_beam_magrider = ToolDefinition(ObjectClass.heavy_rail_beam_magrider)

  val flail_weapon = ToolDefinition(ObjectClass.flail_weapon)

  val rotarychaingun_mosquito = ToolDefinition(ObjectClass.rotarychaingun_mosquito)

  val lightgunship_weapon_system = ToolDefinition(ObjectClass.lightgunship_weapon_system)

  val wasp_weapon_system = new ToolDefinition(ObjectClass.wasp_weapon_system)

  val liberator_weapon_system = ToolDefinition(ObjectClass.liberator_weapon_system)

  val liberator_bomb_bay = ToolDefinition(ObjectClass.liberator_bomb_bay)

  val liberator_25mm_cannon = ToolDefinition(ObjectClass.liberator_25mm_cannon)

  val vulture_nose_weapon_system = ToolDefinition(ObjectClass.vulture_nose_weapon_system)

  val vulture_bomb_bay = ToolDefinition(ObjectClass.vulture_bomb_bay)

  val vulture_tail_cannon = ToolDefinition(ObjectClass.vulture_tail_cannon)

  val cannon_dropship_20mm = ToolDefinition(ObjectClass.cannon_dropship_20mm)

  val dropship_rear_turret = ToolDefinition(ObjectClass.dropship_rear_turret)

  val galaxy_gunship_cannon = ToolDefinition(ObjectClass.galaxy_gunship_cannon)

  val galaxy_gunship_tailgun = ToolDefinition(ObjectClass.galaxy_gunship_tailgun)

  val galaxy_gunship_gun = ToolDefinition(ObjectClass.galaxy_gunship_gun)

  val phalanx_sgl_hevgatcan = ToolDefinition(ObjectClass.phalanx_sgl_hevgatcan)

  val phalanx_avcombo = ToolDefinition(ObjectClass.phalanx_avcombo)

  val phalanx_flakcombo = ToolDefinition(ObjectClass.phalanx_flakcombo)

  val vanu_sentry_turret_weapon = ToolDefinition(ObjectClass.vanu_sentry_turret_weapon)

  val spitfire_weapon = ToolDefinition(ObjectClass.spitfire_weapon)

  val spitfire_aa_weapon = ToolDefinition(ObjectClass.spitfire_aa_weapon)

  val energy_gun = ToolDefinition(ObjectClass.energy_gun)

  val energy_gun_nc = ToolDefinition(ObjectClass.energy_gun_nc)

  val energy_gun_tr = ToolDefinition(ObjectClass.energy_gun_tr)

  val energy_gun_vs = ToolDefinition(ObjectClass.energy_gun_vs)

  val aphelion_armor_siphon = ToolDefinition(ObjectClass.aphelion_armor_siphon)

  val aphelion_armor_siphon_left = ToolDefinition(ObjectClass.aphelion_armor_siphon_left)

  val aphelion_armor_siphon_right = ToolDefinition(ObjectClass.aphelion_armor_siphon_right)

  val aphelion_laser = ToolDefinition(ObjectClass.aphelion_laser)

  val aphelion_laser_left = ToolDefinition(ObjectClass.aphelion_laser_left)

  val aphelion_laser_right = ToolDefinition(ObjectClass.aphelion_laser_right)

  val aphelion_ntu_siphon = ToolDefinition(ObjectClass.aphelion_ntu_siphon)

  val aphelion_ntu_siphon_left = ToolDefinition(ObjectClass.aphelion_ntu_siphon_left)

  val aphelion_ntu_siphon_right = ToolDefinition(ObjectClass.aphelion_ntu_siphon_right)

  val aphelion_ppa = ToolDefinition(ObjectClass.aphelion_ppa)

  val aphelion_ppa_left = ToolDefinition(ObjectClass.aphelion_ppa_left)

  val aphelion_ppa_right = ToolDefinition(ObjectClass.aphelion_ppa_right)

  val aphelion_starfire = ToolDefinition(ObjectClass.aphelion_starfire)

  val aphelion_starfire_left = ToolDefinition(ObjectClass.aphelion_starfire_left)

  val aphelion_starfire_right = ToolDefinition(ObjectClass.aphelion_starfire_right)

  val aphelion_plasma_rocket_pod = ToolDefinition(ObjectClass.aphelion_plasma_rocket_pod)

  val aphelion_immolation_cannon = ToolDefinition(ObjectClass.aphelion_immolation_cannon)

  val colossus_armor_siphon = ToolDefinition(ObjectClass.colossus_armor_siphon)

  val colossus_armor_siphon_left = ToolDefinition(ObjectClass.colossus_armor_siphon_left)

  val colossus_armor_siphon_right = ToolDefinition(ObjectClass.colossus_armor_siphon_right)

  val colossus_burster = ToolDefinition(ObjectClass.colossus_burster)

  val colossus_burster_left = ToolDefinition(ObjectClass.colossus_burster_left)

  val colossus_burster_right = ToolDefinition(ObjectClass.colossus_burster_right)

  val colossus_chaingun = ToolDefinition(ObjectClass.colossus_chaingun)

  val colossus_chaingun_left = ToolDefinition(ObjectClass.colossus_chaingun_left)

  val colossus_chaingun_right = ToolDefinition(ObjectClass.colossus_chaingun_right)

  val colossus_ntu_siphon = ToolDefinition(ObjectClass.colossus_ntu_siphon)

  val colossus_ntu_siphon_left = ToolDefinition(ObjectClass.colossus_ntu_siphon_left)

  val colossus_ntu_siphon_right = ToolDefinition(ObjectClass.colossus_ntu_siphon_right)

  val colossus_tank_cannon = ToolDefinition(ObjectClass.colossus_tank_cannon)

  val colossus_tank_cannon_left = ToolDefinition(ObjectClass.colossus_tank_cannon_left)

  val colossus_tank_cannon_right = ToolDefinition(ObjectClass.colossus_tank_cannon_right)

  val colossus_dual_100mm_cannons = ToolDefinition(ObjectClass.colossus_dual_100mm_cannons)

  val colossus_cluster_bomb_pod = ToolDefinition(ObjectClass.colossus_cluster_bomb_pod)

  val peregrine_armor_siphon = ToolDefinition(ObjectClass.peregrine_armor_siphon)

  val peregrine_armor_siphon_left = ToolDefinition(ObjectClass.peregrine_armor_siphon_left)

  val peregrine_armor_siphon_right = ToolDefinition(ObjectClass.peregrine_armor_siphon_right)

  val peregrine_dual_machine_gun = ToolDefinition(ObjectClass.peregrine_dual_machine_gun)

  val peregrine_dual_machine_gun_left = ToolDefinition(ObjectClass.peregrine_dual_machine_gun_left)

  val peregrine_dual_machine_gun_right = ToolDefinition(ObjectClass.peregrine_dual_machine_gun_right)

  val peregrine_mechhammer = ToolDefinition(ObjectClass.peregrine_mechhammer)

  val peregrine_mechhammer_left = ToolDefinition(ObjectClass.peregrine_mechhammer_left)

  val peregrine_mechhammer_right = ToolDefinition(ObjectClass.peregrine_mechhammer_right)

  val peregrine_sparrow = ToolDefinition(ObjectClass.peregrine_sparrow)

  val peregrine_sparrow_left = ToolDefinition(ObjectClass.peregrine_sparrow_left)

  val peregrine_sparrow_right = ToolDefinition(ObjectClass.peregrine_sparrow_right)

  val peregrine_particle_cannon = ToolDefinition(ObjectClass.peregrine_particle_cannon)

  val peregrine_dual_rocket_pods = ToolDefinition(ObjectClass.peregrine_dual_rocket_pods)

  val peregrine_ntu_siphon = ToolDefinition(ObjectClass.peregrine_ntu_siphon)

  val peregrine_ntu_siphon_left = ToolDefinition(ObjectClass.peregrine_ntu_siphon_left)

  val peregrine_ntu_siphon_right = ToolDefinition(ObjectClass.peregrine_ntu_siphon_right)

  /*
  Vehicles
   */
  val fury = VehicleDefinition(ObjectClass.fury)

  val quadassault = VehicleDefinition(ObjectClass.quadassault)

  val quadstealth = VehicleDefinition(ObjectClass.quadstealth)

  val two_man_assault_buggy = VehicleDefinition(ObjectClass.two_man_assault_buggy)

  val skyguard = VehicleDefinition(ObjectClass.skyguard)

  val threemanheavybuggy = VehicleDefinition(ObjectClass.threemanheavybuggy)

  val twomanheavybuggy = VehicleDefinition(ObjectClass.twomanheavybuggy)

  val twomanhoverbuggy = VehicleDefinition(ObjectClass.twomanhoverbuggy)

  val mediumtransport = VehicleDefinition(ObjectClass.mediumtransport)

  val battlewagon = VehicleDefinition(ObjectClass.battlewagon)

  val thunderer = VehicleDefinition(ObjectClass.thunderer)

  val aurora = VehicleDefinition(ObjectClass.aurora)

  val apc_tr = VehicleDefinition.Apc(ObjectClass.apc_tr)

  val apc_nc = VehicleDefinition.Apc(ObjectClass.apc_nc)

  val apc_vs = VehicleDefinition.Apc(ObjectClass.apc_vs)

  val lightning = VehicleDefinition(ObjectClass.lightning)

  val prowler = VehicleDefinition(ObjectClass.prowler)

  val vanguard = VehicleDefinition(ObjectClass.vanguard)

  val magrider = VehicleDefinition(ObjectClass.magrider)

  val ant = VehicleDefinition.Ant(ObjectClass.ant)

  val ams = VehicleDefinition.Ams(ObjectClass.ams)

  val router = VehicleDefinition.Router(ObjectClass.router)

  val switchblade = VehicleDefinition.Deploying(ObjectClass.switchblade)

  val flail = VehicleDefinition.Deploying(ObjectClass.flail)

  val mosquito = VehicleDefinition(ObjectClass.mosquito)

  val lightgunship = VehicleDefinition(ObjectClass.lightgunship)

  val wasp = VehicleDefinition(ObjectClass.wasp)

  val liberator = VehicleDefinition(ObjectClass.liberator)

  val vulture = VehicleDefinition(ObjectClass.vulture)

  val dropship = VehicleDefinition.Carrier(ObjectClass.dropship)

  val galaxy_gunship = VehicleDefinition(ObjectClass.galaxy_gunship)

  val lodestar = VehicleDefinition.Carrier(ObjectClass.lodestar)

  val phantasm = VehicleDefinition(ObjectClass.phantasm)

  val aphelion_gunner = VehicleDefinition.Bfr(ObjectClass.aphelion_gunner)

  val colossus_gunner = VehicleDefinition.Bfr(ObjectClass.colossus_gunner)

  val peregrine_gunner = VehicleDefinition.Bfr(ObjectClass.peregrine_gunner)

  val aphelion_flight = VehicleDefinition.BfrFlight(ObjectClass.aphelion_flight) //Eclipse

  val colossus_flight = VehicleDefinition.BfrFlight(ObjectClass.colossus_flight) //Invader

  val peregrine_flight = VehicleDefinition.BfrFlight(ObjectClass.peregrine_flight) //Eagle

  val droppod = VehicleDefinition(ObjectClass.droppod)

  val orbital_shuttle = VehicleDefinition(ObjectClass.orbital_shuttle)

  /*
  combat engineering deployables
   */
  val boomer = BoomerDeployableDefinition(DeployedItem.boomer)

  val he_mine = ExplosiveDeployableDefinition(DeployedItem.he_mine)

  val jammer_mine = ExplosiveDeployableDefinition(DeployedItem.jammer_mine)

  val spitfire_turret = TurretDeployableDefinition(DeployedItem.spitfire_turret)

  val spitfire_cloaked = TurretDeployableDefinition(DeployedItem.spitfire_cloaked)

  val spitfire_aa = TurretDeployableDefinition(DeployedItem.spitfire_aa)

  val motionalarmsensor = SensorDeployableDefinition(DeployedItem.motionalarmsensor)

  val sensor_shield = SensorDeployableDefinition(DeployedItem.sensor_shield)

  val tank_traps = TrapDeployableDefinition(DeployedItem.tank_traps)

  val portable_manned_turret = TurretDeployableDefinition(DeployedItem.portable_manned_turret)

  val portable_manned_turret_nc = TurretDeployableDefinition(DeployedItem.portable_manned_turret_nc)

  val portable_manned_turret_tr = TurretDeployableDefinition(DeployedItem.portable_manned_turret_tr)

  val portable_manned_turret_vs = TurretDeployableDefinition(DeployedItem.portable_manned_turret_vs)

  val deployable_shield_generator = new ShieldGeneratorDefinition

  val router_telepad_deployable = TelepadDeployableDefinition(DeployedItem.router_telepad_deployable)

  //this is only treated like a deployable
  val internal_router_telepad_deployable = InternalTelepadDefinition() //objectId: 744

  /*
  Miscellaneous
   */
  val ams_respawn_tube = new SpawnTubeDefinition(49)

  val matrix_terminala = new MatrixTerminalDefinition(517)

  val matrix_terminalb = new MatrixTerminalDefinition(518)

  val matrix_terminalc = new MatrixTerminalDefinition(519)

  val spawn_terminal = new MatrixTerminalDefinition(812)

  val order_terminal = new OrderTerminalDefinition(612)

  val order_terminala = new OrderTerminalDefinition(613)

  val order_terminalb = new OrderTerminalDefinition(614)

  val vanu_equipment_term = new OrderTerminalDefinition(933)

  val cert_terminal = new OrderTerminalDefinition(171)

  val implant_terminal_mech = new ImplantTerminalMechDefinition

  val implant_terminal_interface = new OrderTerminalDefinition(409)

  val ground_vehicle_terminal = new OrderTerminalDefinition(386)

  val air_vehicle_terminal = new OrderTerminalDefinition(43)

  val dropship_vehicle_terminal = new OrderTerminalDefinition(263)

  val vehicle_terminal_combined = new OrderTerminalDefinition(952)

  val vanu_air_vehicle_term = new OrderTerminalDefinition(928)

  val vanu_vehicle_term = new OrderTerminalDefinition(949)

  val bfr_terminal = new OrderTerminalDefinition(143)

  val respawn_tube = new SpawnTubeDefinition(732)

  val respawn_tube_sanctuary = new SpawnTubeDefinition(732) //respawn_tube for sanctuary VT_building_* structures

  val respawn_tube_tower = new SpawnTubeDefinition(733)

  val teleportpad_terminal = new OrderTerminalDefinition(853)

  val adv_med_terminal = new MedicalTerminalDefinition(38)

  val crystals_health_a = new MedicalTerminalDefinition(225)

  val crystals_health_b = new MedicalTerminalDefinition(226)

  val crystals_repair_a = new MedicalTerminalDefinition(227)

  val crystals_repair_b = new MedicalTerminalDefinition(228)

  val crystals_energy = new WeaponRechargeTerminalDefinition(222)

  val crystals_energy_a = new WeaponRechargeTerminalDefinition(223)

  val crystals_energy_b = new WeaponRechargeTerminalDefinition(224)

  val crystals_vehicle_a = new MedicalTerminalDefinition(229)

  val crystals_vehicle_b = new MedicalTerminalDefinition(230)

  val crystals_damage_a = new MedicalTerminalDefinition(220)
  //todo: make these work
  val crystals_damage_b = new MedicalTerminalDefinition(221)

  val medical_terminal = new MedicalTerminalDefinition(529)

  val portable_med_terminal = new MedicalTerminalDefinition(689)

  val pad_landing_frame = new MedicalTerminalDefinition(618)

  val pad_landing_tower_frame = new MedicalTerminalDefinition(619)

  val repair_silo = new MedicalTerminalDefinition(729)

  val recharge_terminal = new WeaponRechargeTerminalDefinition(724)

  val recharge_terminal_weapon_module = new WeaponRechargeTerminalDefinition(725)

  val mb_pad_creation = new VehicleSpawnPadDefinition(525)

  val dropship_pad_doors = new VehicleSpawnPadDefinition(261)

  val vanu_vehicle_creation_pad = new VehicleSpawnPadDefinition(947)

  val bfr_door = new VehicleSpawnPadDefinition(141)

  val pad_create = new VehicleSpawnPadDefinition(615)

  val pad_creation = new VehicleSpawnPadDefinition(616)

  val spawnpoint_vehicle = new VehicleSpawnPadDefinition(816)

  val mb_locker = new LockerDefinition

  val lock_external = new IFFLockDefinition

  val amp_cap_door = new DoorDefinition(44)

  val ancient_door = new DoorDefinition(52)

  val ancient_garage_door = new DoorDefinition(53)

  val cryo_med_door = new DoorDefinition(216)

  val cryo_room_door = new DoorDefinition(216)

  val door = new DoorDefinition(242)

  val door_airlock = new DoorDefinition(243)

  val door_airlock_orb = new DoorDefinition(244)

  val door_dsp = new DoorDefinition(245)

  val door_garage = new DoorDefinition(246)

  val door_interior = new DoorDefinition(247)

  val door_mb = new DoorDefinition(248)

  val door_mb_garage = new DoorDefinition(249)

  val door_mb_main = new DoorDefinition(250)

  val door_mb_orb = new DoorDefinition(251)

  val door_mb_side = new DoorDefinition(252)

  val door_nc_garage = new DoorDefinition(253)

  val door_nc_rotating = new DoorDefinition(254)

  val door_ncside = new DoorDefinition(255)

  val door_orbspawn = new DoorDefinition(256)

  val door_spawn_mb = new DoorDefinition(257)

  val garage_door = new DoorDefinition(344)

  val gr_door_airlock = new DoorDefinition(358)

  val gr_door_ext = new DoorDefinition(359)

  val gr_door_garage_ext = new DoorDefinition(360)

  val gr_door_garage_int = new DoorDefinition(361)

  val gr_door_int = new DoorDefinition(362)

  val gr_door_main = new DoorDefinition(363)

  val gr_door_mb_ext = new DoorDefinition(364)

  val gr_door_mb_int = new DoorDefinition(365)

  val gr_door_mb_lrg = new DoorDefinition(366)

  val gr_door_mb_obsd = new DoorDefinition(367)

  val gr_door_mb_orb = new DoorDefinition(368)

  val gr_door_med = new DoorDefinition(369)

  val main_door = new DoorDefinition(472)

  val shield_door = new DoorDefinition(753)

  val spawn_tube_door = new DoorDefinition(813)

  val spawn_tube_door_coffin = new DoorDefinition(814)

  val resource_silo = new ResourceSiloDefinition

  val capture_terminal = new CaptureTerminalDefinition(158) // Base CC

  val secondary_capture = new CaptureTerminalDefinition(751) // Tower CC

  val vanu_control_console = new CaptureTerminalDefinition(930) // Cavern CC

  val llm_socket = new CaptureFlagSocketDefinition()

  val capture_flag = new CaptureFlagDefinition()
  capture_flag.Packet = new CaptureFlagConverter

  val lodestar_repair_terminal = new MedicalTerminalDefinition(461)

  val multivehicle_rearm_terminal = new OrderTerminalDefinition(576)

  val bfr_rearm_terminal = new OrderTerminalDefinition(142)

  val air_rearm_terminal = new OrderTerminalDefinition(42)

  val ground_rearm_terminal = new OrderTerminalDefinition(384)

  val manned_turret = new FacilityTurretDefinition(480)

  val vanu_sentry_turret = new FacilityTurretDefinition(943)

  val painbox = new PainboxDefinition(622)

  val painbox_continuous = new PainboxDefinition(623)

  val painbox_door_radius = new PainboxDefinition(624)

  val painbox_door_radius_continuous = new PainboxDefinition(625)

  val painbox_radius = new PainboxDefinition(626)

  val painbox_radius_continuous = new PainboxDefinition(627)

  val gen_control = new GeneratorTerminalDefinition(349)

  val generator = new GeneratorDefinition(351)

  val obbasemesh = new AmenityDefinition(598) {}

  val targeting_laser_dispenser = new OrderTerminalDefinition(851)

  /*
  Buildings
   */
  val amp_station = new BuildingDefinition(45) {
    Name = "amp_station"
    SOIRadius = 300
    LatticeLinkBenefit = LatticeBenefit.AmpStation
  }
  val comm_station = new BuildingDefinition(211) {
    Name = "comm_station"
    SOIRadius = 300
    LatticeLinkBenefit = LatticeBenefit.InterlinkFacility
  }
  val comm_station_dsp = new BuildingDefinition(212) {
    Name = "comm_station_dsp"
    SOIRadius = 300
    LatticeLinkBenefit = LatticeBenefit.DropshipCenter
  }
  val cryo_facility = new BuildingDefinition(215) {
    Name = "cryo_facility"
    SOIRadius = 300
    LatticeLinkBenefit = LatticeBenefit.BioLaboratory
  }
  val tech_plant = new BuildingDefinition(852) {
    Name = "tech_plant"
    SOIRadius = 300
    LatticeLinkBenefit = LatticeBenefit.TechnologyPlant
  }

  val building = new BuildingDefinition(474) { Name = "building" } //borrows object id of entity mainbase1

  val vanu_core = new BuildingDefinition(932) { Name = "vanu_core" }

  val ground_bldg_a = new BuildingDefinition(474) { Name = "ground_bldg_a" } //borrows object id of entity mainbase1
  val ground_bldg_b = new BuildingDefinition(474) { Name = "ground_bldg_b" } //borrows object id of entity mainbase1
  val ground_bldg_c = new BuildingDefinition(474) { Name = "ground_bldg_c" } //borrows object id of entity mainbase1
  val ground_bldg_d = new BuildingDefinition(474) { Name = "ground_bldg_d" } //borrows object id of entity mainbase1
  val ground_bldg_e = new BuildingDefinition(474) { Name = "ground_bldg_e" } //borrows object id of entity mainbase1
  val ground_bldg_f = new BuildingDefinition(474) { Name = "ground_bldg_f" } //borrows object id of entity mainbase1
  val ground_bldg_g = new BuildingDefinition(474) { Name = "ground_bldg_g" } //borrows object id of entity mainbase1
  val ground_bldg_h = new BuildingDefinition(474) { Name = "ground_bldg_h" } //borrows object id of entity mainbase1
  val ground_bldg_i = new BuildingDefinition(474) { Name = "ground_bldg_i" } //borrows object id of entity mainbase1
  val ground_bldg_j = new BuildingDefinition(474) { Name = "ground_bldg_j" } //borrows object id of entity mainbase1
  val ground_bldg_z = new BuildingDefinition(474) { Name = "ground_bldg_z" } //borrows object id of entity mainbase1

  val ceiling_bldg_a = new BuildingDefinition(474) { Name = "ceiling_bldg_a" } //borrows object id of entity mainbase1
  val ceiling_bldg_b = new BuildingDefinition(474) { Name = "ceiling_bldg_b" } //borrows object id of entity mainbase1
  val ceiling_bldg_c = new BuildingDefinition(474) { Name = "ceiling_bldg_c" } //borrows object id of entity mainbase1
  val ceiling_bldg_d = new BuildingDefinition(474) { Name = "ceiling_bldg_d" } //borrows object id of entity mainbase1
  val ceiling_bldg_e = new BuildingDefinition(474) { Name = "ceiling_bldg_e" } //borrows object id of entity mainbase1
  val ceiling_bldg_f = new BuildingDefinition(474) { Name = "ceiling_bldg_f" } //borrows object id of entity mainbase1
  val ceiling_bldg_g = new BuildingDefinition(474) { Name = "ceiling_bldg_g" } //borrows object id of entity mainbase1
  val ceiling_bldg_h = new BuildingDefinition(474) { Name = "ceiling_bldg_h" } //borrows object id of entity mainbase1
  val ceiling_bldg_i = new BuildingDefinition(474) { Name = "ceiling_bldg_i" } //borrows object id of entity mainbase1
  val ceiling_bldg_j = new BuildingDefinition(474) { Name = "ceiling_bldg_j" } //borrows object id of entity mainbase1
  val ceiling_bldg_z = new BuildingDefinition(474) { Name = "ceiling_bldg_z" } //borrows object id of entity mainbase1

  val mainbase1            = new BuildingDefinition(474) { Name = "mainbase1" }
  val mainbase2            = new BuildingDefinition(475) { Name = "mainbase2" }
  val mainbase3            = new BuildingDefinition(476) { Name = "mainbase3" }
  val meeting_center_nc    = new BuildingDefinition(537) { Name = "meeting_center_nc" }
  val meeting_center_tr    = new BuildingDefinition(538) { Name = "meeting_center_tr" }
  val meeting_center_vs    = new BuildingDefinition(539) { Name = "meeting_center_vs" }
  val minibase1            = new BuildingDefinition(557) { Name = "minibase1" }
  val minibase2            = new BuildingDefinition(558) { Name = "minibase2" }
  val minibase3            = new BuildingDefinition(559) { Name = "minibase3" }
  val redoubt              = new BuildingDefinition(726) { Name = "redoubt"; SOIRadius = 187 }
  val tower_a              = new BuildingDefinition(869) { Name = "tower_a"; SOIRadius = 50 }
  val tower_b              = new BuildingDefinition(870) { Name = "tower_b"; SOIRadius = 50 }
  val tower_c              = new BuildingDefinition(871) { Name = "tower_c"; SOIRadius = 50 }
  val vanu_control_point   = new BuildingDefinition(931) { Name = "vanu_control_point"; SOIRadius = 187 }
  val vanu_vehicle_station = new BuildingDefinition(948) { Name = "vanu_vehicle_station"; SOIRadius = 187 }

  val hst = new WarpGateDefinition(402)
  hst.Name = "hst"
  hst.UseRadius = 44.96882005f
  hst.SOIRadius = 82
  hst.VehicleAllowance = true
  hst.NoWarp += dropship
  hst.NoWarp += galaxy_gunship
  hst.NoWarp += lodestar
  hst.NoWarp += aphelion_gunner
  hst.NoWarp += aphelion_flight
  hst.NoWarp += colossus_gunner
  hst.NoWarp += colossus_flight
  hst.NoWarp += peregrine_gunner
  hst.NoWarp += peregrine_flight
  hst.SpecificPointFunc = SpawnPoint.CavernGate(innerRadius = 6f)

  val warpgate = new WarpGateDefinition(993)
  warpgate.Name = "warpgate"
  warpgate.UseRadius = 67.81070029f
  warpgate.SOIRadius = 302 //301.8713f
  warpgate.VehicleAllowance = true
  warpgate.SpecificPointFunc = SpawnPoint.Gate

  val warpgate_cavern = new WarpGateDefinition(994)
  warpgate_cavern.Name = "warpgate_cavern"
  warpgate_cavern.UseRadius = 19.72639434f
  warpgate_cavern.SOIRadius = 41
  warpgate_cavern.VehicleAllowance = true
  warpgate_cavern.SpecificPointFunc = SpawnPoint.CavernGate(innerRadius = 4.5f)

  val warpgate_small = new WarpGateDefinition(995)
  warpgate_small.Name = "warpgate_small"
  warpgate_small.UseRadius = 69.03687655f
  warpgate_small.SOIRadius = 103
  warpgate_small.VehicleAllowance = true
  warpgate_small.SpecificPointFunc = SpawnPoint.SmallGate(innerRadius = 27.60654127f, flightlessZOffset = 0.5f)

  val bunker_gauntlet = new BuildingDefinition(150) { Name = "bunker_gauntlet" }
  val bunker_lg       = new BuildingDefinition(151) { Name = "bunker_lg" }
  val bunker_sm       = new BuildingDefinition(152) { Name = "bunker_sm" }

  val orbital_building_nc = new BuildingDefinition(605) { Name = "orbital_building_nc" }
  val orbital_building_tr = new BuildingDefinition(606) { Name = "orbital_building_tr" }
  val orbital_building_vs = new BuildingDefinition(607) { Name = "orbital_building_vs" }
  val VT_building_nc      = new BuildingDefinition(978) { Name = "VT_building_nc" }
  val VT_building_tr      = new BuildingDefinition(979) { Name = "VT_building_tr" }
  val VT_building_vs      = new BuildingDefinition(980) { Name = "VT_building_vs" }
  val vt_dropship         = new BuildingDefinition(981) { Name = "vt_dropship" }
  val vt_spawn            = new BuildingDefinition(984) { Name = "vt_spawn" }
  val vt_vehicle          = new BuildingDefinition(985) { Name = "vt_vehicle" }

  /**
    * Given a faction, provide the standard assault melee weapon.
    * @param faction the faction
    * @return the `ToolDefinition` for the melee weapon
    */
  def StandardMelee(faction: PlanetSideEmpire.Value): ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => chainblade
      case PlanetSideEmpire.NC      => magcutter
      case PlanetSideEmpire.VS      => forceblade
      case PlanetSideEmpire.NEUTRAL => chainblade //do NOT hand out the katana
    }
  }

  /**
    * Given a faction, provide the satndard assault pistol.
    * @param faction the faction
    * @return the `ToolDefinition` for the pistol
    */
  def StandardPistol(faction: PlanetSideEmpire.Value): ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => repeater
      case PlanetSideEmpire.NC      => isp
      case PlanetSideEmpire.VS      => beamer
      case PlanetSideEmpire.NEUTRAL => ilc9
    }
  }

  /**
    * For a given faction, provide the ammunition for the standard assault pistol.
    * The ammunition value here must work with the result of obtaining the pistol using the faction.
    * @param faction the faction
    * @return thr `AmmoBoxDefinition` for the pistol's ammo
    * @see `GlobalDefinitions.StandardPistol`
    */
  def StandardPistolAmmo(faction: PlanetSideEmpire.Value): AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => bullet_9mm
      case PlanetSideEmpire.NC      => shotgun_shell
      case PlanetSideEmpire.VS      => energy_cell
      case PlanetSideEmpire.NEUTRAL => bullet_9mm
    }
  }

  /**
    * For a given faction, provide the medium assault pistol.
    * The medium assault pistols all use the same ammunition so there is no point for a separate selection function.
    * @param faction the faction
    * @return the `ToolDefinition` for the pistol
    */
  def MediumPistol(faction: PlanetSideEmpire.Value): ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => anniversary_guna
      case PlanetSideEmpire.NC      => anniversary_gun
      case PlanetSideEmpire.VS      => anniversary_gunb
      case PlanetSideEmpire.NEUTRAL => ilc9 //do not hand out the spiker
    }
  }

  /**
    * For a given faction, provide the medium assault rifle.
    * For `Neutral` or `Black Ops`, just return a Suppressor.
    * @param faction the faction
    * @return the `ToolDefinition` for the rifle
    */
  def MediumRifle(faction: PlanetSideEmpire.Value): ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => cycler
      case PlanetSideEmpire.NC      => gauss
      case PlanetSideEmpire.VS      => pulsar
      case PlanetSideEmpire.NEUTRAL => suppressor //the Punisher would be messy to have to code for
    }
  }

  /**
    * For a given faction, provide the ammunition for the medium assault rifle.
    * The ammunition value here must work with the result of obtaining the rifle using the faction.
    * @param faction the faction
    * @return thr `AmmoBoxDefinition` for the rifle's ammo
    * @see `GlobalDefinitions.MediumRifle`
    */
  def MediumRifleAmmo(faction: PlanetSideEmpire.Value): AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => bullet_9mm
      case PlanetSideEmpire.NC      => bullet_9mm
      case PlanetSideEmpire.VS      => energy_cell
      case PlanetSideEmpire.NEUTRAL => bullet_9mm
    }
  }

  /**
    * For a given faction, provide the AP ammunition for the medium assault rifle.
    * The ammunition value here must work with the result of obtaining the rifle using the faction.
    * @param faction the faction
    * @return thr `AmmoBoxDefinition` for the rifle's ammo
    * @see `GlobalDefinitions.MediumRifle`
    */
  def MediumRifleAPAmmo(faction: PlanetSideEmpire.Value): AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => bullet_9mm_AP
      case PlanetSideEmpire.NC      => bullet_9mm_AP
      case PlanetSideEmpire.VS      => energy_cell
      case PlanetSideEmpire.NEUTRAL => bullet_9mm_AP
    }
  }

  /**
    * For a given faction, provide the heavy assault rifle.
    * For `Neutral` or `Black Ops`, just return a Suppressor.
    * @param faction the faction
    * @return the `ToolDefinition` for the rifle
    */
  def HeavyRifle(faction: PlanetSideEmpire.Value): ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => mini_chaingun
      case PlanetSideEmpire.NC      => r_shotgun
      case PlanetSideEmpire.VS      => lasher
      case PlanetSideEmpire.NEUTRAL => suppressor //do not hand out the maelstrom
    }
  }

  /**
    * For a given faction, provide the ammunition for the heavy assault rifle.
    * The ammunition value here must work with the result of obtaining the rifle using the faction.
    * @param faction the faction
    * @return thr `AmmoBoxDefinition` for the rifle's ammo
    * @see `GlobalDefinitions.HeavyRifle`
    */
  def HeavyRifleAmmo(faction: PlanetSideEmpire.Value): AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => bullet_9mm
      case PlanetSideEmpire.NC      => shotgun_shell
      case PlanetSideEmpire.VS      => energy_cell
      case PlanetSideEmpire.NEUTRAL => bullet_9mm
    }
  }

  /**
    * For a given faction, provide the AP ammunition for the heavy assault rifle.
    * The ammunition value here must work with the result of obtaining the rifle using the faction.
    * @param faction the faction
    * @return thr `AmmoBoxDefinition` for the rifle's ammo
    * @see `GlobalDefinitions.HeavyRifle`
    */
  def HeavyRifleAPAmmo(faction: PlanetSideEmpire.Value): AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => bullet_9mm_AP
      case PlanetSideEmpire.NC      => shotgun_shell_AP
      case PlanetSideEmpire.VS      => energy_cell
      case PlanetSideEmpire.NEUTRAL => bullet_9mm_AP
    }
  }

  /**
    * For a given faction, provide the anti-vehicular launcher.
    * @param faction the faction
    * @return the `ToolDefinition` for the launcher
    */
  def AntiVehicularLauncher(faction: PlanetSideEmpire.Value): ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => striker
      case PlanetSideEmpire.NC      => hunterseeker
      case PlanetSideEmpire.VS      => lancer
      case PlanetSideEmpire.NEUTRAL => phoenix
    }
  }

  /**
    * For a given faction, provide the ammunition for the anti-vehicular launcher.
    * The ammunition value here must work with the result of obtaining the anti-vehicular launcher using the faction.
    * @param faction the faction
    * @return thr `AmmoBoxDefinition` for the launcher's ammo
    * @see `GlobalDefinitions.AntiVehicular`
    */
  def AntiVehicularAmmo(faction: PlanetSideEmpire.Value): AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => striker_missile_ammo
      case PlanetSideEmpire.NC      => hunter_seeker_missile
      case PlanetSideEmpire.VS      => lancer_cartridge
      case PlanetSideEmpire.NEUTRAL => phoenix_missile //careful - does not exist as an AmmoBox normally
    }
  }

  def MAXArms(subtype: Int, faction: PlanetSideEmpire.Value): ToolDefinition = {
    if (subtype == 1) {
      AA_MAX(faction)
    } else if (subtype == 2) {
      AI_MAX(faction)
    } else if (subtype == 3) {
      AV_MAX(faction)
    } else {
      suppressor // there are no common pool MAX arms
    }
  }

  def isMaxArms(tdef: ToolDefinition): Boolean = {
    tdef match {
      case `trhev_dualcycler` | `nchev_scattercannon` | `vshev_quasar` | `trhev_pounder` | `nchev_falcon` |
           `vshev_comet` | `trhev_burster` | `nchev_sparrow` | `vshev_starfire` =>
        true
      case _ =>
        false
    }
  }

  def AI_MAX(faction: PlanetSideEmpire.Value): ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => trhev_dualcycler
      case PlanetSideEmpire.NC      => nchev_scattercannon
      case PlanetSideEmpire.VS      => vshev_quasar
      case PlanetSideEmpire.NEUTRAL => suppressor //there are no common pool MAX arms
    }
  }

  def AI_MAXAmmo(faction: PlanetSideEmpire.Value): AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => dualcycler_ammo
      case PlanetSideEmpire.NC      => scattercannon_ammo
      case PlanetSideEmpire.VS      => quasar_ammo
      case PlanetSideEmpire.NEUTRAL => bullet_9mm //there are no common pool MAX arms
    }
  }

  def AV_MAX(faction: PlanetSideEmpire.Value): ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => trhev_pounder
      case PlanetSideEmpire.NC      => nchev_falcon
      case PlanetSideEmpire.VS      => vshev_comet
      case PlanetSideEmpire.NEUTRAL => suppressor //there are no common pool MAX arms
    }
  }

  def AV_MAXAmmo(faction: PlanetSideEmpire.Value): AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => pounder_ammo
      case PlanetSideEmpire.NC      => falcon_ammo
      case PlanetSideEmpire.VS      => comet_ammo
      case PlanetSideEmpire.NEUTRAL => bullet_9mm //there are no common pool MAX arms
    }
  }

  def AA_MAX(faction: PlanetSideEmpire.Value): ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => trhev_burster
      case PlanetSideEmpire.NC      => nchev_sparrow
      case PlanetSideEmpire.VS      => vshev_starfire
      case PlanetSideEmpire.NEUTRAL => suppressor //there are no common pool MAX arms
    }
  }

  def AA_MAXAmmo(faction: PlanetSideEmpire.Value): AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => burster_ammo
      case PlanetSideEmpire.NC      => sparrow_ammo
      case PlanetSideEmpire.VS      => starfire_ammo
      case PlanetSideEmpire.NEUTRAL => bullet_9mm //there are no common pool MAX arms
    }
  }

  def PortableMannedTurret(faction: PlanetSideEmpire.Value): TurretDeployableDefinition = {
    faction match {
      case PlanetSideEmpire.TR      => portable_manned_turret_tr
      case PlanetSideEmpire.NC      => portable_manned_turret_nc
      case PlanetSideEmpire.VS      => portable_manned_turret_vs
      case PlanetSideEmpire.NEUTRAL => portable_manned_turret
    }
  }

  /**
    * Using the definition for a piece of `Equipment` determine if it is a grenade-type weapon.
    * Only the normal grenades count; the grenade packs are excluded.
    * @param edef the `EquipmentDefinition` of the item
    * @return `true`, if it is a grenade-type weapon; `false`, otherwise
    */
  def isGrenade(edef: EquipmentDefinition): Boolean = {
    edef match {
      case `frag_grenade` | `jammer_grenade` | `plasma_grenade` | `dynomite` =>
        true
      case _ =>
        false
    }
  }

  /**
    * Using the definition for a piece of `Equipment` determine if it is a grenade-type weapon.
    * Only the grenade packs count; the normal grenades are excluded.
    * @param edef the `EquipmentDefinition` of the item
    * @return `true`, if it is a grenade-type weapon; `false`, otherwise
    */
  def isGrenadePack(edef: EquipmentDefinition): Boolean = {
    edef match {
      case `frag_cartridge` | `jammer_cartridge` | `plasma_cartridge` =>
        true
      case _ =>
        false
    }
  }

  /**
    * Using the definition for a piece of `Equipment` determine with which faction it aligns if it is a weapon.
    * Only checks `Tool` objects.
    * Useful for determining if some item has to be dropped during an activity like `Loadout` switching.
    * @param edef the `EquipmentDefinition` of the item
    * @return the faction alignment, or `Neutral`
    */
  def isFactionWeapon(edef: EquipmentDefinition): PlanetSideEmpire.Value = {
    edef match {
      case `chainblade` | `repeater` | `anniversary_guna` | `cycler` | `mini_chaingun` | `striker` |
           `trhev_dualcycler` | `trhev_pounder` | `trhev_burster` =>
        PlanetSideEmpire.TR
      case `magcutter` | `isp` | `anniversary_gun` | `gauss` | `r_shotgun` | `hunterseeker` | `nchev_scattercannon` |
           `nchev_falcon` | `nchev_sparrow` =>
        PlanetSideEmpire.NC
      case `forceblade` | `beamer` | `anniversary_gunb` | `pulsar` | `lasher` | `lancer` | `vshev_quasar` |
           `vshev_comet` | `vshev_starfire` =>
        PlanetSideEmpire.VS
      case _ =>
        PlanetSideEmpire.NEUTRAL
    }
  }

  /**
    * Using the definition for a piece of `Equipment` determine with which faction it aligns.
    * Checks both `Tool` objects and unique `AmmoBox` objects.
    * @param edef the `EquipmentDefinition` of the item
    * @return the faction alignment, or `Neutral`
    */
  def isFactionEquipment(edef: EquipmentDefinition): PlanetSideEmpire.Value = {
    edef match {
      case `chainblade` | `repeater` | `anniversary_guna` | `cycler` | `mini_chaingun` | `striker` |
           `striker_missile_ammo` | `trhev_dualcycler` | `trhev_pounder` | `trhev_burster` | `dualcycler_ammo` |
           `pounder_ammo` | `burster_ammo` =>
        PlanetSideEmpire.TR
      case `magcutter` | `isp` | `anniversary_gun` | `gauss` | `r_shotgun` | `hunterseeker` | `hunter_seeker_missile` |
           `nchev_scattercannon` | `nchev_falcon` | `nchev_sparrow` | `scattercannon_ammo` | `falcon_ammo` |
           `sparrow_ammo` =>
        PlanetSideEmpire.NC
      case `forceblade` | `beamer` | `anniversary_gunb` | `pulsar` | `lasher` | `lancer` | `energy_cell` |
           `lancer_cartridge` | `vshev_quasar` | `vshev_comet` | `vshev_starfire` | `quasar_ammo` | `comet_ammo` |
           `starfire_ammo` =>
        PlanetSideEmpire.VS
      case _ =>
        PlanetSideEmpire.NEUTRAL
    }
  }

  /**
    * Using the definition for a piece of `Equipment` determine whether it is a "cavern weapon."
    * Useful for determining if some item has to be dropped during an activity like `Loadout` switching.
    * @param edef the `EquipmentDefinition` of the item
    * @return `true`, if it is; otherwise, `false`
    */
  def isCavernWeapon(edef: EquipmentDefinition): Boolean = {
    edef match {
      case `spiker` | `maelstrom` | `radiator` => true
      case _                                   => false
    }
  }

  /**
    * Using the definition for a piece of `Equipment` determine whether it is "cavern equipment."
    * @param edef the `EquipmentDefinition` of the item
    * @return `true`, if it is; otherwise, `false`
    */
  def isCavernEquipment(edef: EquipmentDefinition): Boolean = {
    edef match {
      case `spiker` | `maelstrom` | `radiator` | `ancient_ammo_combo` | `maelstrom_ammo` => true
      case _                                                                             => false
    }
  }

  /**
    * Using the definition for a `Vehicle` determine whether it is a "cavern Vehicle."
    * @param vdef the `VehicleDefinition` of the item
    * @return `true`, if it is; otherwise, `false`
    */
  def isCavernVehicle(vdef: VehicleDefinition): Boolean = {
    vdef match {
      case `router` | `switchblade` | `flail` => true
      case _                                  => false
    }
  }

  /**
    *  Using the definition for a piece of `Equipment` determine whether it is "special."
    * "Special equipment" is any non-standard `Equipment` that, while it can be obtained from a `Terminal`, has artificial prerequisites.
    * For example, the Kits are unlocked as rewards for holiday events and require possessing a specific `MeritCommendation`.
    * @param edef the `EquipmentDefinition` of the item
    * @return `true`, if it is; otherwise, `false`
    */
  def isSpecialEquipment(edef: EquipmentDefinition): Boolean = {
    edef match {
      case `super_medkit` | `super_armorkit` | `super_staminakit` | `katana` =>
        true
      case _ =>
        false
    }
  }

  /**
    * Given the the definition of a piece of equipment,
    * determine whether it is a weapon to be installed on battle frame robotics units.
    * @param tdef the `EquipmentDefinition` of the alleged weapon
    * @return `true`, if the definition represents a battle frame robotics weapon;
    *         `false`, otherwise
    */
  def isBattleFrameWeapon(tdef : EquipmentDefinition) : Boolean = {
    isBattleFrameWeaponForVS(tdef) || isBattleFrameWeaponForTR(tdef) || isBattleFrameWeaponForNC(tdef)
  }

  /**
    * Given the the definition of a battle frame robotics weapon, determine whether it is used by the specific faction.
    * @param tdef the `EquipmentDefinition` of the alleged weapon
    * @param faction the suggested alignment of the weapon
    * @return `true`, if a battle frame robotics weapon and associated with the given faction;
    *         `false`, otherwise
    */
  def isBattleFrameWeapon(tdef : EquipmentDefinition, faction : PlanetSideEmpire.Value) : Boolean = {
    faction match {
      case PlanetSideEmpire.VS =>
        isBattleFrameWeaponForVS(tdef)
      case PlanetSideEmpire.TR =>
        isBattleFrameWeaponForTR(tdef)
      case PlanetSideEmpire.NC =>
        isBattleFrameWeaponForNC(tdef)
      case _ =>
        false
    }
  }

  def isBattleFrameArmorSiphon(edef : EquipmentDefinition) : Boolean = {
    edef match {
      case `aphelion_armor_siphon` | `aphelion_armor_siphon_left` | `aphelion_armor_siphon_right` |
           `colossus_armor_siphon` | `colossus_armor_siphon_left` | `colossus_armor_siphon_right` |
           `peregrine_armor_siphon` | `peregrine_armor_siphon_left` | `peregrine_armor_siphon_right` =>
        true
      case _ =>
        false
    }
  }

  def isBattleFrameNTUSiphon(edef : EquipmentDefinition) : Boolean = {
    edef match {
      case `aphelion_ntu_siphon` | `aphelion_ntu_siphon_left` | `aphelion_ntu_siphon_right` |
           `colossus_ntu_siphon` | `colossus_ntu_siphon_left` | `colossus_ntu_siphon_right` |
           `peregrine_ntu_siphon` | `peregrine_ntu_siphon_left` | `peregrine_ntu_siphon_right` =>
        true
      case _ =>
        false
    }
  }

  /**
    * Given the the definition of a battle frame robotics weapon, determine whether it is used by the Vanu Sovereignty.
    * @param tdef the `EquipmentDefinition` of the alleged weapon
    * @return `true`, if a battle frame robotics weapon and associated with the given faction;
    *         `false`, otherwise
    */
  def isBattleFrameWeaponForVS(tdef : EquipmentDefinition) : Boolean = {
    tdef match {
      case `aphelion_armor_siphon` | `aphelion_armor_siphon_left` | `aphelion_armor_siphon_right` |
           `aphelion_laser` | `aphelion_laser_left` | `aphelion_laser_right` |
           `aphelion_ntu_siphon` | `aphelion_ntu_siphon_left` | `aphelion_ntu_siphon_right` |
           `aphelion_ppa` | `aphelion_ppa_left` | `aphelion_ppa_right` |
           `aphelion_starfire` | `aphelion_starfire_left` | `aphelion_starfire_right` |
           `aphelion_immolation_cannon` | `aphelion_plasma_rocket_pod` =>
        true
      case _ =>
        false
    }
  }

  /**
    * Given the the definition of a battle frame robotics weapon, determine whether it is used by the Terran Republic.
    * @param tdef the `EquipmentDefinition` of the alleged weapon
    * @return `true`, if a battle frame robotics weapon and associated with the given faction;
    *         `false`, otherwise
    */
  def isBattleFrameWeaponForTR(tdef : EquipmentDefinition) : Boolean = {
    tdef match {
      case `colossus_armor_siphon` | `colossus_armor_siphon_left` | `colossus_armor_siphon_right` |
           `colossus_burster` | `colossus_burster_left` | `colossus_burster_right` |
           `colossus_chaingun` | `colossus_chaingun_left` | `colossus_chaingun_right` |
           `colossus_ntu_siphon` | `colossus_ntu_siphon_left` | `colossus_ntu_siphon_right` |
           `colossus_tank_cannon` | `colossus_tank_cannon_left` | `colossus_tank_cannon_right` |
           `colossus_cluster_bomb_pod` | `colossus_dual_100mm_cannons` =>
        true
      case _ =>
        false
    }
  }

  /**
    * Given the the definition of a battle frame robotics weapon, determine whether it is used by the New Conglomerate.
    * @param tdef the `EquipmentDefinition` of the alleged weapon
    * @return `true`, if a battle frame robotics weapon and associated with the given faction;
    *         `false`, otherwise
    */
  def isBattleFrameWeaponForNC(tdef : EquipmentDefinition) : Boolean = {
    tdef match {
      case `peregrine_armor_siphon` | `peregrine_armor_siphon_left` | `peregrine_armor_siphon_right` |
           `peregrine_dual_machine_gun` | `peregrine_dual_machine_gun_left` | `peregrine_dual_machine_gun_right` |
           `peregrine_mechhammer` | `peregrine_mechhammer_left` | `peregrine_mechhammer_right` |
           `peregrine_ntu_siphon` | `colossus_ntu_siphon_left` | `peregrine_ntu_siphon_right` |
           `peregrine_sparrow` | `peregrine_sparrow_left` | `peregrine_sparrow_right` |
           `peregrine_particle_cannon` | `peregrine_dual_rocket_pods` =>
        true
      case _ =>
        false
    }
  }

  /**
   * Using the definition for a `Vehicle` determine whether it is an all-terrain vehicle type.
   * @param vdef the `VehicleDefinition` of the vehicle
   * @return `true`, if it is; `false`, otherwise
   */
  def isAtvVehicle(vdef: VehicleDefinition): Boolean = {
    vdef match {
      case `quadassault` | `fury` | `quadstealth` =>
        true
      case _ =>
        false
    }
  }

  /**
    * Using the definition for a `Vehicle` determine whether it can fly.
    * Does not count the flying battleframe robotics vehicles.
    * @see `isBattleFrameFlightVehicle`
    * @param vdef the `VehicleDefinition` of the vehicle
    * @return `true`, if it is; `false`, otherwise
    */
  def isFlightVehicle(vdef: VehicleDefinition): Boolean = {
    vdef match {
      case `mosquito` | `lightgunship` | `wasp` | `liberator` | `vulture` | `phantasm` | `lodestar` | `dropship` |
           `galaxy_gunship` =>
        true
      case _ =>
        false
    }
  }

  /**
    * Using the definition for a `Vehicle` determine whether it hovers.
    * @param vdef the `VehicleDefinition` of the vehicle
    * @return `true`, if it can; `false`, otherwise
    */
  def isHoverVehicle(vdef: VehicleDefinition): Boolean = {
    vdef match {
      case `twomanhoverbuggy` | `magrider` | `router` | `flail` =>
        true
      case _ =>
        false
    }
  }

  /**
    * Using the definition for a `Vehicle` determine whether it is a frame vehicle.
    * @param vdef the `VehicleDefinition` of the vehicle
    * @return `true`, if it is; `false`, otherwise
    */
  def isBattleFrameVehicle(vdef : VehicleDefinition) : Boolean = {
    isBattleFrameGunnerVehicle(vdef) || isBattleFrameFlightVehicle(vdef)
  }

  /**
    * Using the definition for a `Vehicle` determine whether it is a frame vehicle,
    * primarily a gunner-variant battleframe vehicle.
    * @param vdef the `VehicleDefinition` of the vehicle
    * @return `true`, if it is; `false`, otherwise
    */
  def isBattleFrameGunnerVehicle(vdef: VehicleDefinition): Boolean = {
    vdef match {
      case `colossus_gunner` | `peregrine_gunner` | `aphelion_gunner` =>
        true
      case _ =>
        false
    }
  }

  /**
    * Using the definition for a `Vehicle` determine whether it is a frame vehicle,
    * primarily a flight-variant battleframe vehicle.
    * @see `isFlightVehicle`
    * @param vdef the `VehicleDefinition` of the vehicle
    * @return `true`, if it is; `false`, otherwise
    */
  def isBattleFrameFlightVehicle(vdef: VehicleDefinition): Boolean = {
    vdef match {
      case `colossus_flight` | `peregrine_flight` | `aphelion_flight` =>
        true
      case _ =>
        false
    }
  }

  /**
    * Using the definition for a `Vehicle` determine whether it can rotate its body without forward acceleration.
    * @param vdef the `VehicleDefinition` of the vehicle
    * @return `true`, if it is; `false`, otherwise
    */
  def canStationaryRotate(vdef: VehicleDefinition): Boolean = {
    if (isFlightVehicle(vdef) || isHoverVehicle(vdef)) {
      true
    } else {
      vdef match {
        case `lightning` | `prowler` | `vanguard` =>
          true
        case _ =>
          false
      }
    }
  }

  def MaxDepth(obj: PlanetSideGameObject): Float = {
    obj match {
      case p: Player =>
        if (p.Crouching) {
          1.093750f // same regardless of gender
        } else if (p.ExoSuit == ExoSuitType.MAX) {
          1.906250f // VS female MAX
        } else if (p.Sex == CharacterSex.Male) {
          obj.Definition.MaxDepth // male
        } else {
          1.546875f // female
        }
      case _ =>
        obj.Definition.MaxDepth
    }
  }

  /**
    * Return projectiles that are the damage proxies of another projectile,
    * if such a damage proxy is defined in the appropriate field by its unique object identifier.
    * @see `ProjectileDefinition.DamageProxy`
    * @param projectile the original projectile
    * @return the damage proxy projectiles, if they can be produced
    */
  def getDamageProxy(projectile: Projectile, hitPosition: Vector3): List[Projectile] = {
    projectile
      .Definition
      .DamageProxy
      .flatMap { uoid =>
        ((uoid: @switch) match {
          case 96 =>  Some(aphelion_plasma_cloud)
          case 301 => Some(projectile.profile) //'flamethrower_fire_cloud' can not be made into a packet
          case 464 => Some(projectile.profile) //'maelstrom_grenade_damager' can not be made into a packet
          case 601 => Some(oicw_little_buddy)
          case 655 => Some(peregrine_particle_cannon_radiation_cloud)
          case 717 => Some(radiator_cloud)
          case _   => None
        }) match {
          case Some(proxy)
            if proxy eq projectile.profile =>
            List(projectile)
          case Some(proxy) =>
            List(Projectile(
              proxy,
              projectile.tool_def,
              projectile.fire_mode,
              projectile.owner,
              projectile.attribute_to,
              hitPosition,
              Vector3.Zero
            ))
          case None =>
            Nil
        }
      }
  }

  GlobalDefinitionsExoSuit.init()
  GlobalDefinitionsKit.init()
  GlobalDefinitionsAmmo.init()
  GlobalDefinitionsProjectile.init()
  GlobalDefinitionsMiscellaneous.init()
  GlobalDefinitionsTools.init()
  GlobalDefinitionsVehicle.init()
  GlobalDefinitionsDeployable.init()
}
