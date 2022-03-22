// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.avatar.Certification
import net.psforever.objects.ballistics._
import net.psforever.objects.ce.{DeployableCategory, DeployedItem}
import net.psforever.objects.definition._
import net.psforever.objects.definition.converter._
import net.psforever.objects.equipment._
import net.psforever.objects.geometry.GeometryForm
import net.psforever.objects.inventory.InventoryTile
import net.psforever.objects.locker.LockerContainerDefinition
import net.psforever.objects.serverobject.aura.Aura
import net.psforever.objects.serverobject.doors.DoorDefinition
import net.psforever.objects.serverobject.generator.GeneratorDefinition
import net.psforever.objects.serverobject.locks.IFFLockDefinition
import net.psforever.objects.serverobject.mblocker.LockerDefinition
import net.psforever.objects.serverobject.mount._
import net.psforever.objects.serverobject.pad.VehicleSpawnPadDefinition
import net.psforever.objects.serverobject.painbox.PainboxDefinition
import net.psforever.objects.serverobject.terminals._
import net.psforever.objects.serverobject.tube.SpawnTubeDefinition
import net.psforever.objects.serverobject.resourcesilo.ResourceSiloDefinition
import net.psforever.objects.serverobject.structures.{AmenityDefinition, AutoRepairStats, BuildingDefinition, WarpGateDefinition}
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminalDefinition
import net.psforever.objects.serverobject.terminals.implant.{ImplantTerminalDefinition, ImplantTerminalMechDefinition}
import net.psforever.objects.serverobject.turret.{FacilityTurretDefinition, TurretUpgrade}
import net.psforever.objects.vehicles.{DestroyedVehicle, InternalTelepadDefinition, UtilityType, VehicleSubsystemEntry}
import net.psforever.objects.vital.base.DamageType
import net.psforever.objects.vital.damage._
import net.psforever.objects.vital.etc.{ShieldAgainstRadiation => _, _}
import net.psforever.objects.vital.projectile._
import net.psforever.objects.vital.prop.DamageWithPosition
import net.psforever.objects.vital._
import net.psforever.types.{ExoSuitType, ImplantType, PlanetSideEmpire, Vector3}
import net.psforever.types._
import net.psforever.objects.serverobject.llu.{CaptureFlagDefinition, CaptureFlagSocketDefinition}
import net.psforever.objects.vital.collision.TrapCollisionDamageMultiplier

import scala.annotation.switch
import scala.collection.mutable
import scala.concurrent.duration._

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
  init_exosuit()

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
  init_projectile()

  /*
  Equipment (locker_container, kits, ammunition, weapons)
   */
  import net.psforever.packet.game.objectcreate.ObjectClass
  val locker_container = new LockerContainerDefinition()

  val medkit = KitDefinition(Kits.medkit)

  val super_medkit = KitDefinition(Kits.super_medkit)

  val super_armorkit = KitDefinition(Kits.super_armorkit)

  val super_staminakit = KitDefinition(Kits.super_staminakit) //super stimpak
  init_kit()

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
  init_ammo()

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
  init_tools()

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
  init_vehicles()

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
  init_deployables()

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

  val door = new DoorDefinition(242)

  val door_spawn_mb = new DoorDefinition(257)

  val gr_door_mb_orb = new DoorDefinition(368)

  val spawn_tube_door = new DoorDefinition(813)

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
  initMiscellaneous()

  /*
  Buildings
   */
  val building         = new BuildingDefinition(474) { Name = "building" } //borrows object id of entity mainbase1
  val amp_station      = new BuildingDefinition(45) { Name = "amp_station"; SOIRadius = 300 }
  val comm_station     = new BuildingDefinition(211) { Name = "comm_station"; SOIRadius = 300 }
  val comm_station_dsp = new BuildingDefinition(212) { Name = "comm_station_dsp"; SOIRadius = 300 }
  val cryo_facility    = new BuildingDefinition(215) { Name = "cryo_facility"; SOIRadius = 300 }

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

  val hst = new WarpGateDefinition(402)
  hst.Name = "hst"
  hst.UseRadius = 20.4810f
  hst.SOIRadius = 21
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
  hst.SpecificPointFunc = SpawnPoint.Gate

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
  val tech_plant           = new BuildingDefinition(852) { Name = "tech_plant"; SOIRadius = 300 }
  val tower_a              = new BuildingDefinition(869) { Name = "tower_a"; SOIRadius = 50 }
  val tower_b              = new BuildingDefinition(870) { Name = "tower_b"; SOIRadius = 50 }
  val tower_c              = new BuildingDefinition(871) { Name = "tower_c"; SOIRadius = 50 }
  val vanu_control_point   = new BuildingDefinition(931) { Name = "vanu_control_point"; SOIRadius = 187 }
  val vanu_vehicle_station = new BuildingDefinition(948) { Name = "vanu_vehicle_station"; SOIRadius = 187 }

  val warpgate = new WarpGateDefinition(993)
  warpgate.Name = "warpgate"
  warpgate.UseRadius = 301.8713f
  warpgate.SOIRadius = 302
  warpgate.VehicleAllowance = true
  warpgate.SpecificPointFunc = SpawnPoint.Gate

  val warpgate_cavern = new WarpGateDefinition(994)
  warpgate_cavern.Name = "warpgate_cavern"
  warpgate_cavern.UseRadius = 51.0522f
  warpgate_cavern.SOIRadius = 52
  warpgate_cavern.VehicleAllowance = true
  warpgate_cavern.SpecificPointFunc = SpawnPoint.Gate

  val warpgate_small = new WarpGateDefinition(995)
  warpgate_small.Name = "warpgate_small"
  warpgate_small.UseRadius = 103f
  warpgate_small.SOIRadius = 103
  warpgate_small.VehicleAllowance = true
  warpgate_small.SpecificPointFunc = SpawnPoint.Gate

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
    * Using the definition for a `Vehicle` determine whether it can fly.
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

  /**
    * Initialize `KitDefinition` globals.
    */
  private def init_kit(): Unit = {
    medkit.Name = "medkit"

    super_medkit.Name = "super_medkit"

    super_armorkit.Name = "super_armorkit"

    super_staminakit.Name = "super_staminakit"
  }

  /**
    * Initialize `ExoSuitType` globals.
    */
  private def init_exosuit(): Unit = {
    Standard.Name = "standard"
    Standard.MaxArmor = 50
    Standard.InventoryScale = InventoryTile.Tile96
    Standard.InventoryOffset = 6
    Standard.Holster(0, EquipmentSize.Pistol)
    Standard.Holster(2, EquipmentSize.Rifle)
    Standard.Holster(4, EquipmentSize.Melee)
    Standard.ResistanceDirectHit = 4
    Standard.ResistanceSplash = 15
    Standard.ResistanceAggravated = 8
    Standard.collision.forceFactor = 1.5f
    Standard.collision.massFactor = 2f

    Agile.Name = "lite_armor"
    Agile.Descriptor = "agile"
    Agile.MaxArmor = 100
    Agile.InventoryScale = InventoryTile.Tile99
    Agile.InventoryOffset = 6
    Agile.Holster(0, EquipmentSize.Pistol)
    Agile.Holster(1, EquipmentSize.Pistol)
    Agile.Holster(2, EquipmentSize.Rifle)
    Agile.Holster(4, EquipmentSize.Melee)
    Agile.ResistanceDirectHit = 6
    Agile.ResistanceSplash = 25
    Agile.ResistanceAggravated = 10
    Agile.collision.forceFactor = 1.5f
    Agile.collision.massFactor = 2f

    Reinforced.Name = "med_armor"
    Reinforced.Descriptor = "reinforced"
    Reinforced.Permissions = List(Certification.ReinforcedExoSuit)
    Reinforced.MaxArmor = 200
    Reinforced.InventoryScale = InventoryTile.Tile1209
    Reinforced.InventoryOffset = 6
    Reinforced.Holster(0, EquipmentSize.Pistol)
    Reinforced.Holster(1, EquipmentSize.Pistol)
    Reinforced.Holster(2, EquipmentSize.Rifle)
    Reinforced.Holster(3, EquipmentSize.Rifle)
    Reinforced.Holster(4, EquipmentSize.Melee)
    Reinforced.ResistanceDirectHit = 10
    Reinforced.ResistanceSplash = 35
    Reinforced.ResistanceAggravated = 12
    Reinforced.collision.forceFactor = 2f
    Reinforced.collision.massFactor = 3f

    Infiltration.Name = "infiltration_suit"
    Infiltration.Permissions = List(Certification.InfiltrationSuit)
    Infiltration.MaxArmor = 0
    Infiltration.InventoryScale = InventoryTile.Tile66
    Infiltration.InventoryOffset = 6
    Infiltration.Holster(0, EquipmentSize.Pistol)
    Infiltration.Holster(4, EquipmentSize.Melee)

    def CommonMaxConfig(max: SpecialExoSuitDefinition): Unit = {
      max.Permissions = List(Certification.AIMAX, Certification.AVMAX, Certification.AAMAX, Certification.UniMAX)
      max.MaxArmor = 650
      max.InventoryScale = InventoryTile.Tile1612
      max.InventoryOffset = 6
      max.Holster(0, EquipmentSize.Max)
      max.Holster(4, EquipmentSize.Melee)
      max.Subtract.Damage1 = 2
      max.ResistanceDirectHit = 6
      max.ResistanceSplash = 35
      max.ResistanceAggravated = 10
      max.RadiationShielding = 0.5f
      max.collision.forceFactor = 4f
      max.collision.massFactor = 10f
      max.DamageUsing = DamageCalculations.AgainstMaxSuit
      max.Model = MaxResolutions.calculate
    }

    CommonMaxConfig(VSMAX)
    VSMAX.Name = "vshev"
    VSMAX.MaxCapacitor = 50
    VSMAX.CapacitorRechargeDelayMillis = 5000
    VSMAX.CapacitorRechargePerSecond = 3
    VSMAX.CapacitorDrainPerSecond = 20

    CommonMaxConfig(TRMAX)
    TRMAX.Name = "trhev"
    TRMAX.MaxCapacitor = 300
    TRMAX.CapacitorRechargeDelayMillis = 10000
    TRMAX.CapacitorRechargePerSecond = 10
    TRMAX.CapacitorDrainPerSecond = 30

    CommonMaxConfig(NCMAX)
    NCMAX.Name = "nchev"
    NCMAX.MaxCapacitor = 400
    NCMAX.CapacitorRechargeDelayMillis = 10000
    NCMAX.CapacitorRechargePerSecond = 4
    NCMAX.CapacitorDrainPerSecond = 4

  }

  /**
    * Initialize `AmmoBoxDefinition` globals.
    */
  private def init_ammo(): Unit = {
    melee_ammo.Name = "melee_ammo"
    melee_ammo.Size = EquipmentSize.Blocked

    frag_grenade_ammo.Name = "frag_grenade_ammo"
    frag_grenade_ammo.Size = EquipmentSize.Blocked

    jammer_grenade_ammo.Name = "jammer_grenade_ammo"
    jammer_grenade_ammo.Size = EquipmentSize.Blocked

    plasma_grenade_ammo.Name = "plasma_grenade_ammo"
    plasma_grenade_ammo.Size = EquipmentSize.Blocked

    bullet_9mm.Name = "9mmbullet"
    bullet_9mm.Capacity = 50
    bullet_9mm.Tile = InventoryTile.Tile33

    bullet_9mm_AP.Name = "9mmbullet_AP"
    bullet_9mm_AP.Capacity = 50
    bullet_9mm_AP.Tile = InventoryTile.Tile33

    shotgun_shell.Name = "shotgun_shell"
    shotgun_shell.Capacity = 16
    shotgun_shell.Tile = InventoryTile.Tile33

    shotgun_shell_AP.Name = "shotgun_shell_AP"
    shotgun_shell_AP.Capacity = 16
    shotgun_shell_AP.Tile = InventoryTile.Tile33

    energy_cell.Name = "energy_cell"
    energy_cell.Capacity = 50
    energy_cell.Tile = InventoryTile.Tile33

    anniversary_ammo.Name = "anniversary_ammo"
    anniversary_ammo.Capacity = 30
    anniversary_ammo.Tile = InventoryTile.Tile33

    ancient_ammo_combo.Name = "ancient_ammo_combo"
    ancient_ammo_combo.Capacity = 30
    ancient_ammo_combo.Tile = InventoryTile.Tile33

    maelstrom_ammo.Name = "maelstrom_ammo"
    maelstrom_ammo.Capacity = 50
    maelstrom_ammo.Tile = InventoryTile.Tile33

    phoenix_missile.Name = "phoenix_missile"
    phoenix_missile.Size = EquipmentSize.Blocked

    striker_missile_ammo.Name = "striker_missile_ammo"
    striker_missile_ammo.Capacity = 15
    striker_missile_ammo.Tile = InventoryTile.Tile44

    hunter_seeker_missile.Name = "hunter_seeker_missile"
    hunter_seeker_missile.Capacity = 9
    hunter_seeker_missile.Tile = InventoryTile.Tile44

    lancer_cartridge.Name = "lancer_cartridge"
    lancer_cartridge.Capacity = 18
    lancer_cartridge.Tile = InventoryTile.Tile44

    rocket.Name = "rocket"
    rocket.Capacity = 15
    rocket.Tile = InventoryTile.Tile33

    frag_cartridge.Name = "frag_cartridge"
    frag_cartridge.Capacity = 12
    frag_cartridge.Tile = InventoryTile.Tile33

    plasma_cartridge.Name = "plasma_cartridge"
    plasma_cartridge.Capacity = 12
    plasma_cartridge.Tile = InventoryTile.Tile33

    jammer_cartridge.Name = "jammer_cartridge"
    jammer_cartridge.Capacity = 12
    jammer_cartridge.Tile = InventoryTile.Tile33

    bolt.Name = "bolt"
    bolt.Capacity = 10
    bolt.Tile = InventoryTile.Tile33

    oicw_ammo.Name = "oicw_ammo"
    oicw_ammo.Capacity = 10
    oicw_ammo.Tile = InventoryTile.Tile44

    flamethrower_ammo.Name = "flamethrower_ammo"
    flamethrower_ammo.Capacity = 100
    flamethrower_ammo.Tile = InventoryTile.Tile44

    winchester_ammo.Name = "winchester_ammo"
    winchester_ammo.Capacity = 10
    winchester_ammo.Tile = InventoryTile.Tile33

    pellet_gun_ammo.Name = "pellet_gun_ammo"
    pellet_gun_ammo.Capacity = 8
    pellet_gun_ammo.Tile = InventoryTile.Tile33

    six_shooter_ammo.Name = "six_shooter_ammo"
    six_shooter_ammo.Capacity = 12
    six_shooter_ammo.Tile = InventoryTile.Tile33

    dualcycler_ammo.Name = "dualcycler_ammo"
    dualcycler_ammo.Capacity = 100
    dualcycler_ammo.Tile = InventoryTile.Tile44

    pounder_ammo.Name = "pounder_ammo"
    pounder_ammo.Capacity = 50
    pounder_ammo.Tile = InventoryTile.Tile44

    burster_ammo.Name = "burster_ammo"
    burster_ammo.Capacity = 100
    burster_ammo.Tile = InventoryTile.Tile44

    scattercannon_ammo.Name = "scattercannon_ammo"
    scattercannon_ammo.Capacity = 50
    scattercannon_ammo.Tile = InventoryTile.Tile44

    falcon_ammo.Name = "falcon_ammo"
    falcon_ammo.Capacity = 50
    falcon_ammo.Tile = InventoryTile.Tile44

    sparrow_ammo.Name = "sparrow_ammo"
    sparrow_ammo.Capacity = 50
    sparrow_ammo.Tile = InventoryTile.Tile44

    quasar_ammo.Name = "quasar_ammo"
    quasar_ammo.Capacity = 60
    quasar_ammo.Tile = InventoryTile.Tile44

    comet_ammo.Name = "comet_ammo"
    comet_ammo.Capacity = 50
    comet_ammo.Tile = InventoryTile.Tile44

    starfire_ammo.Name = "starfire_ammo"
    starfire_ammo.Capacity = 50
    starfire_ammo.Tile = InventoryTile.Tile44

    health_canister.Name = "health_canister"
    health_canister.Capacity = 100
    health_canister.Tile = InventoryTile.Tile23

    armor_canister.Name = "armor_canister"
    armor_canister.Capacity = 100
    armor_canister.repairAmount = 12f //ADB says 12.5, but 12 is better for the math
    armor_canister.Tile = InventoryTile.Tile23

    upgrade_canister.Name = "upgrade_canister"
    upgrade_canister.Capacity = 1
    upgrade_canister.Tile = InventoryTile.Tile23

    trek_ammo.Name = "trek_ammo"
    trek_ammo.Size = EquipmentSize.Blocked

    bullet_35mm.Name = "35mmbullet"
    bullet_35mm.Capacity = 100
    bullet_35mm.Tile = InventoryTile.Tile44

    aphelion_laser_ammo.Name = "aphelion_laser_ammo"
    aphelion_laser_ammo.Capacity = 165
    aphelion_laser_ammo.Tile = InventoryTile.Tile44

    aphelion_immolation_cannon_ammo.Name = "aphelion_immolation_cannon_ammo"
    aphelion_immolation_cannon_ammo.Capacity = 100
    aphelion_immolation_cannon_ammo.Tile = InventoryTile.Tile55

    aphelion_plasma_rocket_ammo.Name = "aphelion_plasma_rocket_ammo"
    aphelion_plasma_rocket_ammo.Capacity = 195
    aphelion_plasma_rocket_ammo.Tile = InventoryTile.Tile55

    aphelion_ppa_ammo.Name = "aphelion_ppa_ammo"
    aphelion_ppa_ammo.Capacity = 110
    aphelion_ppa_ammo.Tile = InventoryTile.Tile44

    aphelion_starfire_ammo.Name = "aphelion_starfire_ammo"
    aphelion_starfire_ammo.Capacity = 132
    aphelion_starfire_ammo.Tile = InventoryTile.Tile44

    skyguard_flak_cannon_ammo.Name = "skyguard_flak_cannon_ammo"
    skyguard_flak_cannon_ammo.Capacity = 200
    skyguard_flak_cannon_ammo.Tile = InventoryTile.Tile44

    firebird_missile.Name = "firebird_missile"
    firebird_missile.Capacity = 50
    firebird_missile.Tile = InventoryTile.Tile44

    flux_cannon_thresher_battery.Name = "flux_cannon_thresher_battery"
    flux_cannon_thresher_battery.Capacity = 150
    flux_cannon_thresher_battery.Tile = InventoryTile.Tile44

    fluxpod_ammo.Name = "fluxpod_ammo"
    fluxpod_ammo.Capacity = 80
    fluxpod_ammo.Tile = InventoryTile.Tile44

    hellfire_ammo.Name = "hellfire_ammo"
    hellfire_ammo.Capacity = 24
    hellfire_ammo.Tile = InventoryTile.Tile44

    liberator_bomb.Name = "liberator_bomb"
    liberator_bomb.Capacity = 20
    liberator_bomb.Tile = InventoryTile.Tile44

    bullet_25mm.Name = "25mmbullet"
    bullet_25mm.Capacity = 150
    bullet_25mm.Tile = InventoryTile.Tile44

    bullet_75mm.Name = "75mmbullet"
    bullet_75mm.Capacity = 100
    bullet_75mm.Tile = InventoryTile.Tile44

    heavy_grenade_mortar.Name = "heavy_grenade_mortar"
    heavy_grenade_mortar.Capacity = 100
    heavy_grenade_mortar.Tile = InventoryTile.Tile44

    pulse_battery.Name = "pulse_battery"
    pulse_battery.Capacity = 100
    pulse_battery.Tile = InventoryTile.Tile44

    heavy_rail_beam_battery.Name = "heavy_rail_beam_battery"
    heavy_rail_beam_battery.Capacity = 100
    heavy_rail_beam_battery.Tile = InventoryTile.Tile44

    reaver_rocket.Name = "reaver_rocket"
    reaver_rocket.Capacity = 12
    reaver_rocket.Tile = InventoryTile.Tile44

    bullet_20mm.Name = "20mmbullet"
    bullet_20mm.Capacity = 200
    bullet_20mm.Tile = InventoryTile.Tile44

    bullet_12mm.Name = "12mmbullet"
    bullet_12mm.Capacity = 300
    bullet_12mm.Tile = InventoryTile.Tile44

    wasp_rocket_ammo.Name = "wasp_rocket_ammo"
    wasp_rocket_ammo.Capacity = 6
    wasp_rocket_ammo.Tile = InventoryTile.Tile44

    wasp_gun_ammo.Name = "wasp_gun_ammo"
    wasp_gun_ammo.Capacity = 150
    wasp_gun_ammo.Tile = InventoryTile.Tile44

    bullet_15mm.Name = "15mmbullet"
    bullet_15mm.Capacity = 360
    bullet_15mm.Tile = InventoryTile.Tile44

    colossus_100mm_cannon_ammo.Name = "colossus_100mm_cannon_ammo"
    colossus_100mm_cannon_ammo.Capacity = 90
    colossus_100mm_cannon_ammo.Tile = InventoryTile.Tile55

    colossus_burster_ammo.Name = "colossus_burster_ammo"
    colossus_burster_ammo.Capacity = 235
    colossus_burster_ammo.Tile = InventoryTile.Tile44

    colossus_cluster_bomb_ammo.Name = "colossus_cluster_bomb_ammo"
    colossus_cluster_bomb_ammo.Capacity = 150
    colossus_cluster_bomb_ammo.Tile = InventoryTile.Tile55

    colossus_chaingun_ammo.Name = "colossus_chaingun_ammo"
    colossus_chaingun_ammo.Capacity = 600
    colossus_chaingun_ammo.Tile = InventoryTile.Tile44

    colossus_tank_cannon_ammo.Name = "colossus_tank_cannon_ammo"
    colossus_tank_cannon_ammo.Capacity = 110
    colossus_tank_cannon_ammo.Tile = InventoryTile.Tile44

    bullet_105mm.Name = "105mmbullet"
    bullet_105mm.Capacity = 100
    bullet_105mm.Tile = InventoryTile.Tile44

    gauss_cannon_ammo.Name = "gauss_cannon_ammo"
    gauss_cannon_ammo.Capacity = 15
    gauss_cannon_ammo.Tile = InventoryTile.Tile44

    peregrine_dual_machine_gun_ammo.Name = "peregrine_dual_machine_gun_ammo"
    peregrine_dual_machine_gun_ammo.Capacity = 240
    peregrine_dual_machine_gun_ammo.Tile = InventoryTile.Tile44

    peregrine_mechhammer_ammo.Name = "peregrine_mechhammer_ammo"
    peregrine_mechhammer_ammo.Capacity = 30
    peregrine_mechhammer_ammo.Tile = InventoryTile.Tile44

    peregrine_particle_cannon_ammo.Name = "peregrine_particle_cannon_ammo"
    peregrine_particle_cannon_ammo.Capacity = 40
    peregrine_particle_cannon_ammo.Tile = InventoryTile.Tile55

    peregrine_rocket_pod_ammo.Name = "peregrine_rocket_pod_ammo"
    peregrine_rocket_pod_ammo.Capacity = 275
    peregrine_rocket_pod_ammo.Tile = InventoryTile.Tile55

    peregrine_sparrow_ammo.Name = "peregrine_sparrow_ammo"
    peregrine_sparrow_ammo.Capacity = 150
    peregrine_sparrow_ammo.Tile = InventoryTile.Tile44

    bullet_150mm.Name = "150mmbullet"
    bullet_150mm.Capacity = 50
    bullet_150mm.Tile = InventoryTile.Tile44

    phalanx_ammo.Name = "phalanx_ammo"
    phalanx_ammo.Size = EquipmentSize.Inventory

    spitfire_ammo.Name = "spitfire_ammo"
    spitfire_ammo.Size = EquipmentSize.Inventory

    spitfire_aa_ammo.Name = "spitfire_aa_ammo"
    spitfire_aa_ammo.Size = EquipmentSize.Inventory

    energy_gun_ammo.Name = "energy_gun_ammo"
    energy_gun_ammo.Size = EquipmentSize.Inventory

    armor_siphon_ammo.Name = "armor_siphon_ammo"
    armor_siphon_ammo.Capacity = 0
    armor_siphon_ammo.Size = EquipmentSize.Blocked

    ntu_siphon_ammo.Name = "ntu_siphon_ammo"
    ntu_siphon_ammo.Capacity = 0
    ntu_siphon_ammo.Size = EquipmentSize.Blocked
  }

  /**
    * Initialize `ProjectileDefinition` globals.
    */
  private def init_projectile(): Unit = {
    init_standard_projectile()
    init_bfr_projectile()
  }

  /**
    * Initialize `ProjectileDefinition` globals for most projectiles.
    */
  private def init_standard_projectile(): Unit = {
    val projectileConverter: ProjectileConverter = new ProjectileConverter
    val radCloudConverter: RadiationCloudConverter = new RadiationCloudConverter

    no_projectile.Name = "no_projectile"
    no_projectile.DamageRadiusMin = 0f
    ProjectileDefinition.CalculateDerivedFields(no_projectile)
    no_projectile.Modifiers = Nil

    bullet_105mm_projectile.Name = "105mmbullet_projectile"
    bullet_105mm_projectile.Damage0 = 150
    bullet_105mm_projectile.Damage1 = 300
    bullet_105mm_projectile.Damage2 = 300
    bullet_105mm_projectile.Damage3 = 300
    bullet_105mm_projectile.Damage4 = 180
    bullet_105mm_projectile.DamageAtEdge = 0.1f
    bullet_105mm_projectile.DamageRadius = 7f
    bullet_105mm_projectile.ProjectileDamageType = DamageType.Splash
    bullet_105mm_projectile.InitialVelocity = 100
    bullet_105mm_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(bullet_105mm_projectile)

    bullet_12mm_projectile.Name = "12mmbullet_projectile"
    bullet_12mm_projectile.Damage0 = 25
    bullet_12mm_projectile.Damage1 = 10
    bullet_12mm_projectile.Damage2 = 25
    bullet_12mm_projectile.Damage3 = 10
    bullet_12mm_projectile.Damage4 = 7
    bullet_12mm_projectile.ProjectileDamageType = DamageType.Direct
    bullet_12mm_projectile.DegradeDelay = .015f
    bullet_12mm_projectile.DegradeMultiplier = 0.5f
    bullet_12mm_projectile.InitialVelocity = 500
    bullet_12mm_projectile.Lifespan = 0.5f
    bullet_12mm_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(bullet_12mm_projectile)

    bullet_12mm_projectileb.Name = "12mmbullet_projectileb"
    // TODO for later, maybe : set_resource_parent 12mmbullet_projectileb game_objects 12mmbullet_projectile
    bullet_12mm_projectileb.Damage0 = 25
    bullet_12mm_projectileb.Damage1 = 10
    bullet_12mm_projectileb.Damage2 = 25
    bullet_12mm_projectileb.Damage3 = 10
    bullet_12mm_projectileb.Damage4 = 7
    bullet_12mm_projectileb.ProjectileDamageType = DamageType.Direct
    bullet_12mm_projectileb.DegradeDelay = .015f
    bullet_12mm_projectileb.DegradeMultiplier = 0.5f
    bullet_12mm_projectileb.InitialVelocity = 500
    bullet_12mm_projectileb.Lifespan = 0.5f
    bullet_12mm_projectileb.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(bullet_12mm_projectileb)

    bullet_150mm_projectile.Name = "150mmbullet_projectile"
    bullet_150mm_projectile.Damage0 = 150
    bullet_150mm_projectile.Damage1 = 450
    bullet_150mm_projectile.Damage2 = 450
    bullet_150mm_projectile.Damage3 = 450
    bullet_150mm_projectile.Damage4 = 400
    bullet_150mm_projectile.DamageAtEdge = 0.10f
    bullet_150mm_projectile.DamageRadius = 8f
    bullet_150mm_projectile.ProjectileDamageType = DamageType.Splash
    bullet_150mm_projectile.InitialVelocity = 100
    bullet_150mm_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(bullet_150mm_projectile)
    bullet_150mm_projectile.Modifiers = RadialDegrade

    bullet_15mm_apc_projectile.Name = "15mmbullet_apc_projectile"
    // TODO for later, maybe : set_resource_parent 15mmbullet_apc_projectile game_objects 15mmbullet_projectile
    bullet_15mm_apc_projectile.Damage0 = 12
    bullet_15mm_apc_projectile.Damage1 = 20
    bullet_15mm_apc_projectile.Damage2 = 30
    bullet_15mm_apc_projectile.Damage3 = 20
    bullet_15mm_apc_projectile.Damage4 = 16
    bullet_15mm_apc_projectile.ProjectileDamageType = DamageType.Direct
    bullet_15mm_apc_projectile.DegradeDelay = .015f
    bullet_15mm_apc_projectile.DegradeMultiplier = 0.5f
    bullet_15mm_apc_projectile.InitialVelocity = 500
    bullet_15mm_apc_projectile.Lifespan = 0.5f
    ProjectileDefinition.CalculateDerivedFields(bullet_15mm_apc_projectile)

    bullet_15mm_projectile.Name = "15mmbullet_projectile"
    bullet_15mm_projectile.Damage0 = 21
    bullet_15mm_projectile.Damage1 = 18
    bullet_15mm_projectile.Damage2 = 25
    bullet_15mm_projectile.Damage3 = 18
    bullet_15mm_projectile.Damage4 = 11
    bullet_15mm_projectile.ProjectileDamageType = DamageType.Direct
    bullet_15mm_projectile.DegradeDelay = .015f
    bullet_15mm_projectile.DegradeMultiplier = 0.5f
    bullet_15mm_projectile.InitialVelocity = 500
    bullet_15mm_projectile.Lifespan = 0.5f
    ProjectileDefinition.CalculateDerivedFields(bullet_15mm_projectile)

    bullet_20mm_apc_projectile.Name = "20mmbullet_apc_projectile"
    // TODO for later, maybe : set_resource_parent 20mmbullet_apc_projectile game_objects 20mmbullet_projectile
    bullet_20mm_apc_projectile.Damage0 = 24
    bullet_20mm_apc_projectile.Damage1 = 40
    bullet_20mm_apc_projectile.Damage2 = 60
    bullet_20mm_apc_projectile.Damage3 = 40
    bullet_20mm_apc_projectile.Damage4 = 32
    bullet_20mm_apc_projectile.ProjectileDamageType = DamageType.Direct
    bullet_20mm_apc_projectile.DegradeDelay = .015f
    bullet_20mm_apc_projectile.DegradeMultiplier = 0.5f
    bullet_20mm_apc_projectile.InitialVelocity = 500
    bullet_20mm_apc_projectile.Lifespan = 0.5f
    ProjectileDefinition.CalculateDerivedFields(bullet_20mm_apc_projectile)

    bullet_20mm_projectile.Name = "20mmbullet_projectile"
    bullet_20mm_projectile.Damage0 = 20
    bullet_20mm_projectile.Damage1 = 20
    bullet_20mm_projectile.Damage2 = 40
    bullet_20mm_projectile.Damage3 = 20
    bullet_20mm_projectile.Damage4 = 16
    bullet_20mm_projectile.ProjectileDamageType = DamageType.Direct
    bullet_20mm_projectile.DegradeDelay = .015f
    bullet_20mm_projectile.DegradeMultiplier = 0.5f
    bullet_20mm_projectile.InitialVelocity = 500
    bullet_20mm_projectile.Lifespan = 0.5f
    ProjectileDefinition.CalculateDerivedFields(bullet_20mm_projectile)

    bullet_25mm_projectile.Name = "25mmbullet_projectile"
    bullet_25mm_projectile.Damage0 = 25
    bullet_25mm_projectile.Damage1 = 35
    bullet_25mm_projectile.Damage2 = 50
    bullet_25mm_projectile.ProjectileDamageType = DamageType.Direct
    bullet_25mm_projectile.DegradeDelay = .02f
    bullet_25mm_projectile.DegradeMultiplier = 0.5f
    bullet_25mm_projectile.InitialVelocity = 500
    bullet_25mm_projectile.Lifespan = 0.6f
    ProjectileDefinition.CalculateDerivedFields(bullet_25mm_projectile)

    bullet_35mm_projectile.Name = "35mmbullet_projectile"
    bullet_35mm_projectile.Damage0 = 40
    bullet_35mm_projectile.Damage1 = 50
    bullet_35mm_projectile.Damage2 = 60
    bullet_35mm_projectile.ProjectileDamageType = DamageType.Direct
    bullet_35mm_projectile.DegradeDelay = .015f
    bullet_35mm_projectile.DegradeMultiplier = 0.5f
    bullet_35mm_projectile.InitialVelocity = 200
    bullet_35mm_projectile.Lifespan = 1.5f
    ProjectileDefinition.CalculateDerivedFields(bullet_35mm_projectile)

    bullet_75mm_apc_projectile.Name = "75mmbullet_apc_projectile"
    // TODO for later, maybe : set_resource_parent 75mmbullet_apc_projectile game_objects 75mmbullet_projectile
    bullet_75mm_apc_projectile.Damage0 = 85
    bullet_75mm_apc_projectile.Damage1 = 155
    bullet_75mm_apc_projectile.DamageAtEdge = 0.1f
    bullet_75mm_apc_projectile.DamageRadius = 5f
    bullet_75mm_apc_projectile.ProjectileDamageType = DamageType.Splash
    bullet_75mm_apc_projectile.InitialVelocity = 100
    bullet_75mm_apc_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(bullet_75mm_apc_projectile)
    bullet_75mm_apc_projectile.Modifiers = RadialDegrade

    bullet_75mm_projectile.Name = "75mmbullet_projectile"
    bullet_75mm_projectile.Damage0 = 75
    bullet_75mm_projectile.Damage1 = 125
    bullet_75mm_projectile.DamageAtEdge = 0.1f
    bullet_75mm_projectile.DamageRadius = 5f
    bullet_75mm_projectile.ProjectileDamageType = DamageType.Splash
    bullet_75mm_projectile.InitialVelocity = 100
    bullet_75mm_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(bullet_75mm_projectile)
    bullet_75mm_projectile.Modifiers = RadialDegrade

    bullet_9mm_AP_projectile.Name = "9mmbullet_AP_projectile"
    // TODO for later, maybe : set_resource_parent 9mmbullet_AP_projectile game_objects 9mmbullet_projectile
    bullet_9mm_AP_projectile.Damage0 = 10
    bullet_9mm_AP_projectile.Damage1 = 15
    bullet_9mm_AP_projectile.ProjectileDamageType = DamageType.Direct
    bullet_9mm_AP_projectile.DegradeDelay = 0.15f
    bullet_9mm_AP_projectile.DegradeMultiplier = 0.25f
    bullet_9mm_AP_projectile.InitialVelocity = 500
    bullet_9mm_AP_projectile.Lifespan = 0.4f
    bullet_9mm_AP_projectile.UseDamage1Subtract = true
    bullet_9mm_AP_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(bullet_9mm_AP_projectile)

    bullet_9mm_projectile.Name = "9mmbullet_projectile"
    bullet_9mm_projectile.Damage0 = 18
    bullet_9mm_projectile.Damage1 = 10
    bullet_9mm_projectile.ProjectileDamageType = DamageType.Direct
    bullet_9mm_projectile.DegradeDelay = 0.15f
    bullet_9mm_projectile.DegradeMultiplier = 0.25f
    bullet_9mm_projectile.InitialVelocity = 500
    bullet_9mm_projectile.Lifespan = 0.4f
    bullet_9mm_projectile.UseDamage1Subtract = true
    bullet_9mm_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(bullet_9mm_projectile)

    anniversary_projectilea.Name = "anniversary_projectilea"
    anniversary_projectilea.Damage0 = 30
    anniversary_projectilea.Damage1 = 15
    anniversary_projectilea.Damage2 = 15
    anniversary_projectilea.Damage3 = 45
    anniversary_projectilea.Damage4 = 15
    anniversary_projectilea.ProjectileDamageType = DamageType.Direct
    anniversary_projectilea.DegradeDelay = 0.04f
    anniversary_projectilea.DegradeMultiplier = 0.2f
    anniversary_projectilea.InitialVelocity = 500
    anniversary_projectilea.Lifespan = 0.5f
    anniversary_projectilea.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(anniversary_projectilea)

    anniversary_projectileb.Name = "anniversary_projectileb"
    // TODO for later, maybe : set_resource_parent anniversary_projectileb game_objects anniversary_projectilea
    anniversary_projectileb.Damage0 = 30
    anniversary_projectileb.Damage1 = 15
    anniversary_projectileb.Damage2 = 15
    anniversary_projectileb.Damage3 = 45
    anniversary_projectileb.Damage4 = 15
    anniversary_projectileb.ProjectileDamageType = DamageType.Direct
    anniversary_projectileb.DegradeDelay = 0.04f
    anniversary_projectileb.DegradeMultiplier = 0.2f
    anniversary_projectileb.InitialVelocity = 500
    anniversary_projectileb.Lifespan = 0.5f
    anniversary_projectileb.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(anniversary_projectileb)

    bolt_projectile.Name = "bolt_projectile"
    bolt_projectile.Damage0 = 100
    bolt_projectile.Damage1 = 50
    bolt_projectile.Damage2 = 50
    bolt_projectile.Damage3 = 50
    bolt_projectile.Damage4 = 75
    bolt_projectile.ProjectileDamageType = DamageType.Splash
    bolt_projectile.InitialVelocity = 500
    bolt_projectile.Lifespan = 1.0f
    bolt_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(bolt_projectile)
    //TODO bolt_projectile.Modifiers = DistanceDegrade?

    burster_projectile.Name = "burster_projectile"
    burster_projectile.Damage0 = 18
    burster_projectile.Damage1 = 25
    burster_projectile.Damage2 = 50
    burster_projectile.DamageAtEdge = 0.25f
    burster_projectile.DamageRadius = 10f
    burster_projectile.ProjectileDamageType = DamageType.Direct
    burster_projectile.ProjectileDamageTypeSecondary = DamageType.Splash
    burster_projectile.InitialVelocity = 125
    burster_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(burster_projectile)
    burster_projectile.Modifiers = List(
      //FlakHit,
      FlakBurst,
      MaxDistanceCutoff
    )

    chainblade_projectile.Name = "chainblade_projectile"
    // TODO for later, maybe : set_resource_parent chainblade_projectile game_objects melee_ammo_projectile
    chainblade_projectile.Damage0 = 50
    chainblade_projectile.Damage1 = 0
    chainblade_projectile.ProjectileDamageType = DamageType.Direct
    chainblade_projectile.InitialVelocity = 100
    chainblade_projectile.Lifespan = .02f
    ProjectileDefinition.CalculateDerivedFields(chainblade_projectile)
    chainblade_projectile.Modifiers = List(MeleeBoosted, MaxDistanceCutoff)

    comet_projectile.Name = "comet_projectile"
    comet_projectile.Damage0 = 15
    comet_projectile.Damage1 = 60
    comet_projectile.Damage2 = 60
    comet_projectile.Damage3 = 38
    comet_projectile.Damage4 = 64
    comet_projectile.Acceleration = 10
    comet_projectile.AccelerationUntil = 2f
    comet_projectile.DamageAtEdge = 0.45f
    comet_projectile.DamageRadius = 1.0f
    comet_projectile.ProjectileDamageType = DamageType.Aggravated
    comet_projectile.Aggravated = AggravatedDamage(
      AggravatedInfo(DamageType.Direct, 0.25f, 500), //originally, .2
      Aura.Comet,
      AggravatedTiming(2000, 3),
      10f,
      List(
        TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player),
        TargetValidation(EffectTarget.Category.Vehicle, EffectTarget.Validation.Vehicle),
        TargetValidation(EffectTarget.Category.Turret, EffectTarget.Validation.Turret)
      )
    )
    comet_projectile.InitialVelocity = 80
    comet_projectile.Lifespan = 3.1f
    ProjectileDefinition.CalculateDerivedFields(comet_projectile)
    comet_projectile.Modifiers = List(
      CometAggravated,
      CometAggravatedBurn
    )

    dualcycler_projectile.Name = "dualcycler_projectile"
    dualcycler_projectile.Damage0 = 18
    dualcycler_projectile.Damage1 = 10
    dualcycler_projectile.ProjectileDamageType = DamageType.Direct
    dualcycler_projectile.DegradeDelay = .025f
    dualcycler_projectile.DegradeMultiplier = .5f
    dualcycler_projectile.InitialVelocity = 500
    dualcycler_projectile.Lifespan = 0.5f
    dualcycler_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(dualcycler_projectile)

    dynomite_projectile.Name = "dynomite_projectile"
    // TODO for later, maybe : set_resource_parent dynomite_projectile game_objects frag_grenade_projectile_enh
    dynomite_projectile.Damage0 = 75
    dynomite_projectile.Damage1 = 175
    dynomite_projectile.DamageAtEdge = 0.1f
    dynomite_projectile.DamageRadius = 10f
    dynomite_projectile.GrenadeProjectile = true
    dynomite_projectile.ProjectileDamageType = DamageType.Splash
    dynomite_projectile.InitialVelocity = 30
    dynomite_projectile.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(dynomite_projectile)
    dynomite_projectile.Modifiers = RadialDegrade

    energy_cell_projectile.Name = "energy_cell_projectile"
    energy_cell_projectile.Damage0 = 18
    energy_cell_projectile.Damage1 = 10
    energy_cell_projectile.ProjectileDamageType = DamageType.Direct
    energy_cell_projectile.DegradeDelay = 0.05f
    energy_cell_projectile.DegradeMultiplier = 0.4f
    energy_cell_projectile.InitialVelocity = 500
    energy_cell_projectile.Lifespan = .4f
    energy_cell_projectile.UseDamage1Subtract = true
    energy_cell_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(energy_cell_projectile)

    energy_gun_nc_projectile.Name = "energy_gun_nc_projectile"
    energy_gun_nc_projectile.Damage0 = 10
    energy_gun_nc_projectile.Damage1 = 13
    energy_gun_nc_projectile.ProjectileDamageType = DamageType.Direct
    energy_gun_nc_projectile.InitialVelocity = 500
    energy_gun_nc_projectile.Lifespan = 0.5f
    energy_gun_nc_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(energy_gun_nc_projectile)

    energy_gun_tr_projectile.Name = "energy_gun_tr_projectile"
    energy_gun_tr_projectile.Damage0 = 14
    energy_gun_tr_projectile.Damage1 = 18
    energy_gun_tr_projectile.ProjectileDamageType = DamageType.Direct
    energy_gun_tr_projectile.DegradeDelay = .025f
    energy_gun_tr_projectile.DegradeMultiplier = .5f
    energy_gun_tr_projectile.InitialVelocity = 500
    energy_gun_tr_projectile.Lifespan = 0.5f
    energy_gun_tr_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(energy_gun_tr_projectile)

    energy_gun_vs_projectile.Name = "energy_gun_vs_projectile"
    energy_gun_vs_projectile.Damage0 = 25
    energy_gun_vs_projectile.Damage1 = 35
    energy_gun_vs_projectile.ProjectileDamageType = DamageType.Direct
    energy_gun_vs_projectile.DegradeDelay = 0.045f
    energy_gun_vs_projectile.DegradeMultiplier = 0.5f
    energy_gun_vs_projectile.InitialVelocity = 500
    energy_gun_vs_projectile.Lifespan = .5f
    energy_gun_vs_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(energy_gun_vs_projectile)

    enhanced_energy_cell_projectile.Name = "enhanced_energy_cell_projectile"
    // TODO for later, maybe : set_resource_parent enhanced_energy_cell_projectile game_objects energy_cell_projectile
    enhanced_energy_cell_projectile.Damage0 = 7
    enhanced_energy_cell_projectile.Damage1 = 15
    enhanced_energy_cell_projectile.ProjectileDamageType = DamageType.Direct
    enhanced_energy_cell_projectile.DegradeDelay = 0.05f
    enhanced_energy_cell_projectile.DegradeMultiplier = 0.4f
    enhanced_energy_cell_projectile.InitialVelocity = 500
    enhanced_energy_cell_projectile.Lifespan = .4f
    enhanced_energy_cell_projectile.UseDamage1Subtract = true
    enhanced_energy_cell_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(enhanced_energy_cell_projectile)

    enhanced_quasar_projectile.Name = "enhanced_quasar_projectile"
    // TODO for later, maybe : set_resource_parent enhanced_quasar_projectile game_objects quasar_projectile
    enhanced_quasar_projectile.Damage1 = 12
    enhanced_quasar_projectile.Damage0 = 10
    enhanced_quasar_projectile.ProjectileDamageType = DamageType.Direct
    enhanced_quasar_projectile.DegradeDelay = 0.045f
    enhanced_quasar_projectile.DegradeMultiplier = 0.5f
    enhanced_quasar_projectile.InitialVelocity = 500
    enhanced_quasar_projectile.Lifespan = .4f
    enhanced_quasar_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(enhanced_quasar_projectile)

    falcon_projectile.Name = "falcon_projectile"
    falcon_projectile.Damage0 = 35
    falcon_projectile.Damage1 = 132
    falcon_projectile.Damage2 = 132
    falcon_projectile.Damage3 = 83
    falcon_projectile.Damage4 = 144
    falcon_projectile.Acceleration = 10
    falcon_projectile.AccelerationUntil = 2f
    falcon_projectile.DamageAtEdge = 0.2f
    falcon_projectile.DamageRadius = 1f
    falcon_projectile.ProjectileDamageType = DamageType.Splash
    falcon_projectile.InitialVelocity = 120
    falcon_projectile.Lifespan = 2.1f
    ProjectileDefinition.CalculateDerivedFields(falcon_projectile)
    falcon_projectile.Modifiers = RadialDegrade

    firebird_missile_projectile.Name = "firebird_missile_projectile"
    firebird_missile_projectile.Damage0 = 125
    firebird_missile_projectile.Damage1 = 220
    firebird_missile_projectile.Damage2 = 220
    firebird_missile_projectile.Damage3 = 200
    firebird_missile_projectile.Damage4 = 181
    firebird_missile_projectile.Acceleration = 20
    firebird_missile_projectile.AccelerationUntil = 2f
    firebird_missile_projectile.DamageAtEdge = .1f
    firebird_missile_projectile.DamageRadius = 5f
    firebird_missile_projectile.ProjectileDamageType = DamageType.Splash
    firebird_missile_projectile.InitialVelocity = 75
    firebird_missile_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(firebird_missile_projectile)
    firebird_missile_projectile.Modifiers = RadialDegrade

    flail_projectile.Name = "flail_projectile"
    flail_projectile.Damage0 = 75
    flail_projectile.Damage1 = 200
    flail_projectile.Damage2 = 200
    flail_projectile.Damage3 = 200
    flail_projectile.Damage4 = 300
    flail_projectile.DamageAtEdge = 0.1f
    flail_projectile.DamageRadius = 15f
    flail_projectile.ProjectileDamageType = DamageType.Splash
    flail_projectile.DegradeDelay = 1.5f
    //a DegradeDelay of 1.5s equals a DistanceNoDegrade of 112.5m
    flail_projectile.DegradeMultiplier = 5f
    flail_projectile.InitialVelocity = 75
    flail_projectile.Lifespan = 40f
    ProjectileDefinition.CalculateDerivedFields(flail_projectile)
    flail_projectile.Modifiers = List(
      FlailDistanceDamageBoost,
      RadialDegrade
    )

    flamethrower_fire_cloud.Name = "flamethrower_fire_cloud"
    flamethrower_fire_cloud.Damage0 = 2
    flamethrower_fire_cloud.Damage1 = 0
    flamethrower_fire_cloud.Damage2 = 0
    flamethrower_fire_cloud.Damage3 = 1
    flamethrower_fire_cloud.Damage4 = 0
    flamethrower_fire_cloud.DamageAtEdge = 0.1f
    flamethrower_fire_cloud.DamageRadius = 5f
    flamethrower_fire_cloud.ProjectileDamageType = DamageType.Aggravated
    flamethrower_fire_cloud.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, -1.5f, 500), AggravatedInfo(DamageType.Splash, -4.0f, 500)),
      Aura.Fire,
      AggravatedTiming(5000, 10),
      2.5f,
      false,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    flamethrower_fire_cloud.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(flamethrower_fire_cloud)
    flamethrower_fire_cloud.Modifiers = List(
      InfantryAggravatedDirect,
      InfantryAggravatedSplash,
      RadialDegrade,
      FireballAggravatedBurn
    )

    flamethrower_fireball.Name = "flamethrower_fireball"
    flamethrower_fireball.Damage0 = 30
    flamethrower_fireball.Damage1 = 0
    flamethrower_fireball.Damage2 = 0
    flamethrower_fireball.Damage3 = 20
    flamethrower_fireball.Damage4 = 0
    flamethrower_fireball.DamageToHealthOnly = true
    flamethrower_fireball.DamageAtEdge = 0.15f
    flamethrower_fireball.DamageRadius = 5f
    flamethrower_fireball.ProjectileDamageType = DamageType.Aggravated
    flamethrower_fireball.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, 0.9f, 500), AggravatedInfo(DamageType.Splash, 0.9f, 500)),
      Aura.Fire,
      AggravatedTiming(5000, 10),
      0.1f,
      false,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    flamethrower_fireball.InitialVelocity = 15
    flamethrower_fireball.Lifespan = 1.2f
    ProjectileDefinition.CalculateDerivedFields(flamethrower_fireball)
    flamethrower_fireball.Modifiers = List(
      InfantryAggravatedDirect,
      InfantryAggravatedSplash,
      RadialDegrade,
      FireballAggravatedBurn
    )

    flamethrower_projectile.Name = "flamethrower_projectile"
    flamethrower_projectile.Damage0 = 10
    flamethrower_projectile.Damage1 = 0
    flamethrower_projectile.Damage2 = 0
    flamethrower_projectile.Damage3 = 4
    flamethrower_projectile.Damage4 = 0
    flamethrower_projectile.DamageToHealthOnly = true
    flamethrower_projectile.Acceleration = -5
    flamethrower_projectile.AccelerationUntil = 2f
    flamethrower_projectile.ProjectileDamageType = DamageType.Aggravated
    flamethrower_projectile.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, 0.5f, 500)),
      Aura.Fire,
      AggravatedTiming(5000, 10),
      0.5f,
      false,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    flamethrower_projectile.DegradeDelay = 1.0f
    flamethrower_projectile.DegradeMultiplier = 0.5f
    flamethrower_projectile.InitialVelocity = 10
    flamethrower_projectile.Lifespan = 2.0f
    ProjectileDefinition.CalculateDerivedFields(flamethrower_projectile)
    flamethrower_projectile.Modifiers = List(
      InfantryAggravatedDirect,
      FireballAggravatedBurn,
      MaxDistanceCutoff
    )

    flux_cannon_apc_projectile.Name = "flux_cannon_apc_projectile"
    // TODO for later, maybe : set_resource_parent flux_cannon_apc_projectile game_objects flux_cannon_thresher_projectile
    flux_cannon_apc_projectile.Damage0 = 14
    flux_cannon_apc_projectile.Damage1 = 23
    flux_cannon_apc_projectile.Damage2 = 35
    flux_cannon_apc_projectile.Damage3 = 23
    flux_cannon_apc_projectile.Damage4 = 18
    flux_cannon_apc_projectile.DamageAtEdge = .5f
    flux_cannon_apc_projectile.DamageRadius = 4f
    flux_cannon_apc_projectile.ProjectileDamageType = DamageType.Direct
    flux_cannon_apc_projectile.InitialVelocity = 300
    flux_cannon_apc_projectile.Lifespan = 1f
    ProjectileDefinition.CalculateDerivedFields(flux_cannon_apc_projectile)

    flux_cannon_thresher_projectile.Name = "flux_cannon_thresher_projectile"
    flux_cannon_thresher_projectile.Damage0 = 30
    flux_cannon_thresher_projectile.Damage1 = 44
    flux_cannon_thresher_projectile.Damage2 = 44
    flux_cannon_thresher_projectile.Damage3 = 40
    flux_cannon_thresher_projectile.Damage4 = 37
    flux_cannon_thresher_projectile.DamageAtEdge = .5f
    flux_cannon_thresher_projectile.DamageRadius = 4f
    flux_cannon_thresher_projectile.ProjectileDamageType = DamageType.Splash
    flux_cannon_thresher_projectile.InitialVelocity = 75
    flux_cannon_thresher_projectile.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(flux_cannon_thresher_projectile)
    flux_cannon_thresher_projectile.Modifiers = RadialDegrade

    fluxpod_projectile.Name = "fluxpod_projectile"
    fluxpod_projectile.Damage0 = 110
    fluxpod_projectile.Damage1 = 80
    fluxpod_projectile.Damage2 = 125
    fluxpod_projectile.Damage3 = 80
    fluxpod_projectile.Damage4 = 52
    fluxpod_projectile.DamageAtEdge = .3f
    fluxpod_projectile.DamageRadius = 3f
    fluxpod_projectile.ProjectileDamageType = DamageType.Splash
    fluxpod_projectile.InitialVelocity = 80
    fluxpod_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(fluxpod_projectile)
    fluxpod_projectile.Modifiers = RadialDegrade

    forceblade_projectile.Name = "forceblade_projectile"
    // TODO for later, maybe : set_resource_parent forceblade_projectile game_objects melee_ammo_projectile
    forceblade_projectile.Damage0 = 50
    forceblade_projectile.Damage1 = 0
    forceblade_projectile.ProjectileDamageType = DamageType.Direct
    forceblade_projectile.InitialVelocity = 100
    forceblade_projectile.Lifespan = .02f
    ProjectileDefinition.CalculateDerivedFields(forceblade_projectile)
    forceblade_projectile.Modifiers = List(MeleeBoosted, MaxDistanceCutoff)

    frag_cartridge_projectile.Name = "frag_cartridge_projectile"
    // TODO for later, maybe : set_resource_parent frag_cartridge_projectile game_objects frag_grenade_projectile
    frag_cartridge_projectile.Damage0 = 75
    frag_cartridge_projectile.Damage1 = 100
    frag_cartridge_projectile.DamageAtEdge = 0.1f
    frag_cartridge_projectile.DamageRadius = 7f
    frag_cartridge_projectile.GrenadeProjectile = true
    frag_cartridge_projectile.ProjectileDamageType = DamageType.Splash
    frag_cartridge_projectile.InitialVelocity = 30
    frag_cartridge_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(frag_cartridge_projectile)
    frag_cartridge_projectile.Modifiers = RadialDegrade

    frag_cartridge_projectile_b.Name = "frag_cartridge_projectile_b"
    // TODO for later, maybe : set_resource_parent frag_cartridge_projectile_b game_objects frag_grenade_projectile_enh
    frag_cartridge_projectile_b.Damage0 = 75
    frag_cartridge_projectile_b.Damage1 = 100
    frag_cartridge_projectile_b.DamageAtEdge = 0.1f
    frag_cartridge_projectile_b.DamageRadius = 5f
    frag_cartridge_projectile_b.GrenadeProjectile = true
    frag_cartridge_projectile_b.ProjectileDamageType = DamageType.Splash
    frag_cartridge_projectile_b.InitialVelocity = 30
    frag_cartridge_projectile_b.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(frag_cartridge_projectile_b)
    frag_cartridge_projectile_b.Modifiers = RadialDegrade

    frag_grenade_projectile.Name = "frag_grenade_projectile"
    frag_grenade_projectile.Damage0 = 75
    frag_grenade_projectile.Damage1 = 100
    frag_grenade_projectile.DamageAtEdge = 0.1f
    frag_grenade_projectile.DamageRadius = 7f
    frag_grenade_projectile.GrenadeProjectile = true
    frag_grenade_projectile.ProjectileDamageType = DamageType.Splash
    frag_grenade_projectile.InitialVelocity = 30
    frag_grenade_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(frag_grenade_projectile)
    frag_grenade_projectile.Modifiers = RadialDegrade

    frag_grenade_projectile_enh.Name = "frag_grenade_projectile_enh"
    // TODO for later, maybe : set_resource_parent frag_grenade_projectile_enh game_objects frag_grenade_projectile
    frag_grenade_projectile_enh.Damage0 = 75
    frag_grenade_projectile_enh.Damage1 = 100
    frag_grenade_projectile_enh.DamageAtEdge = 0.1f
    frag_grenade_projectile_enh.DamageRadius = 7f
    frag_grenade_projectile_enh.GrenadeProjectile = true
    frag_grenade_projectile_enh.ProjectileDamageType = DamageType.Splash
    frag_grenade_projectile_enh.InitialVelocity = 30
    frag_grenade_projectile_enh.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(frag_grenade_projectile_enh)
    frag_grenade_projectile_enh.Modifiers = RadialDegrade

    galaxy_gunship_gun_projectile.Name = "galaxy_gunship_gun_projectile"
    // TODO for later, maybe : set_resource_parent galaxy_gunship_gun_projectile game_objects 35mmbullet_projectile
    galaxy_gunship_gun_projectile.Damage0 = 40
    galaxy_gunship_gun_projectile.Damage1 = 50
    galaxy_gunship_gun_projectile.Damage2 = 80
    galaxy_gunship_gun_projectile.ProjectileDamageType = DamageType.Direct
    galaxy_gunship_gun_projectile.DegradeDelay = 0.4f
    galaxy_gunship_gun_projectile.DegradeMultiplier = 0.6f
    galaxy_gunship_gun_projectile.InitialVelocity = 400
    galaxy_gunship_gun_projectile.Lifespan = 0.8f
    ProjectileDefinition.CalculateDerivedFields(galaxy_gunship_gun_projectile)

    gauss_cannon_projectile.Name = "gauss_cannon_projectile"
    gauss_cannon_projectile.Damage0 = 190
    gauss_cannon_projectile.Damage1 = 370
    gauss_cannon_projectile.Damage2 = 370
    gauss_cannon_projectile.Damage3 = 370
    gauss_cannon_projectile.Damage4 = 240
    gauss_cannon_projectile.DamageAtEdge = 0.3f
    gauss_cannon_projectile.DamageRadius = 1.5f
    gauss_cannon_projectile.ProjectileDamageType = DamageType.Splash
    gauss_cannon_projectile.InitialVelocity = 150
    gauss_cannon_projectile.Lifespan = 2.67f
    ProjectileDefinition.CalculateDerivedFields(gauss_cannon_projectile)
    gauss_cannon_projectile.Modifiers = RadialDegrade

    grenade_projectile.Name = "grenade_projectile"
    grenade_projectile.Damage0 = 50
    grenade_projectile.DamageAtEdge = 0.2f
    grenade_projectile.DamageRadius = 100f
    grenade_projectile.ProjectileDamageType = DamageType.Splash
    grenade_projectile.InitialVelocity = 15
    grenade_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(grenade_projectile)
    grenade_projectile.Modifiers = RadialDegrade

    heavy_grenade_projectile.Name = "heavy_grenade_projectile"
    heavy_grenade_projectile.Damage0 = 50
    heavy_grenade_projectile.Damage1 = 82
    heavy_grenade_projectile.Damage2 = 82
    heavy_grenade_projectile.Damage3 = 75
    heavy_grenade_projectile.Damage4 = 66
    heavy_grenade_projectile.DamageAtEdge = 0.1f
    heavy_grenade_projectile.DamageRadius = 5f
    heavy_grenade_projectile.GrenadeProjectile = true
    heavy_grenade_projectile.ProjectileDamageType = DamageType.Splash
    heavy_grenade_projectile.InitialVelocity = 75
    heavy_grenade_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(heavy_grenade_projectile)
    heavy_grenade_projectile.Modifiers = RadialDegrade

    heavy_rail_beam_projectile.Name = "heavy_rail_beam_projectile"
    heavy_rail_beam_projectile.Damage0 = 75
    heavy_rail_beam_projectile.Damage1 = 215
    heavy_rail_beam_projectile.Damage2 = 215
    heavy_rail_beam_projectile.Damage3 = 215
    heavy_rail_beam_projectile.Damage4 = 120
    heavy_rail_beam_projectile.DamageAtEdge = 0.30f
    heavy_rail_beam_projectile.DamageRadius = 5f
    heavy_rail_beam_projectile.ProjectileDamageType = DamageType.Splash
    heavy_rail_beam_projectile.InitialVelocity = 600
    heavy_rail_beam_projectile.Lifespan = .5f
    ProjectileDefinition.CalculateDerivedFields(heavy_rail_beam_projectile)
    heavy_rail_beam_projectile.Modifiers = RadialDegrade

    heavy_sniper_projectile.Name = "heavy_sniper_projectile"
    heavy_sniper_projectile.Damage0 = 55
    heavy_sniper_projectile.Damage1 = 28
    heavy_sniper_projectile.Damage2 = 28
    heavy_sniper_projectile.Damage3 = 28
    heavy_sniper_projectile.Damage4 = 42
    heavy_sniper_projectile.ProjectileDamageType = DamageType.Splash
    heavy_sniper_projectile.InitialVelocity = 500
    heavy_sniper_projectile.Lifespan = 1.0f
    heavy_sniper_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(heavy_sniper_projectile)
    heavy_sniper_projectile.Modifiers = RadialDegrade

    hellfire_projectile.Name = "hellfire_projectile"
    hellfire_projectile.Damage0 = 50
    hellfire_projectile.Damage1 = 250
    hellfire_projectile.Damage2 = 250
    hellfire_projectile.Damage3 = 125
    hellfire_projectile.Damage4 = 250
    hellfire_projectile.Acceleration = 10
    hellfire_projectile.AccelerationUntil = 2f
    hellfire_projectile.DamageAtEdge = .25f
    hellfire_projectile.DamageRadius = 3f
    hellfire_projectile.ProjectileDamageType = DamageType.Splash
    hellfire_projectile.InitialVelocity = 125
    hellfire_projectile.Lifespan = 1.5f
    ProjectileDefinition.CalculateDerivedFields(hellfire_projectile)
    hellfire_projectile.Modifiers = RadialDegrade

    hunter_seeker_missile_dumbfire.Name = "hunter_seeker_missile_dumbfire"
    hunter_seeker_missile_dumbfire.Damage0 = 50
    hunter_seeker_missile_dumbfire.Damage1 = 350
    hunter_seeker_missile_dumbfire.Damage2 = 250
    hunter_seeker_missile_dumbfire.Damage3 = 250
    hunter_seeker_missile_dumbfire.Damage4 = 525
    hunter_seeker_missile_dumbfire.DamageAtEdge = 0.1f
    hunter_seeker_missile_dumbfire.DamageRadius = 1.5f
    hunter_seeker_missile_dumbfire.ProjectileDamageType = DamageType.Splash
    hunter_seeker_missile_dumbfire.InitialVelocity = 40
    hunter_seeker_missile_dumbfire.Lifespan = 6.3f
    ProjectileDefinition.CalculateDerivedFields(hunter_seeker_missile_dumbfire)
    hunter_seeker_missile_dumbfire.Modifiers = RadialDegrade

    hunter_seeker_missile_projectile.Name = "hunter_seeker_missile_projectile"
    hunter_seeker_missile_projectile.Damage0 = 50
    hunter_seeker_missile_projectile.Damage1 = 350
    hunter_seeker_missile_projectile.Damage2 = 250
    hunter_seeker_missile_projectile.Damage3 = 250
    hunter_seeker_missile_projectile.Damage4 = 525
    hunter_seeker_missile_projectile.DamageAtEdge = 0.1f
    hunter_seeker_missile_projectile.DamageRadius = 1.5f
    hunter_seeker_missile_projectile.ProjectileDamageType = DamageType.Splash
    hunter_seeker_missile_projectile.InitialVelocity = 40
    hunter_seeker_missile_projectile.Lifespan = 6.3f
    hunter_seeker_missile_projectile.registerAs = "rc-projectiles"
    hunter_seeker_missile_projectile.ExistsOnRemoteClients = true
    hunter_seeker_missile_projectile.RemoteClientData = (39577, 201)
    hunter_seeker_missile_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(hunter_seeker_missile_projectile)
    hunter_seeker_missile_projectile.Modifiers = RadialDegrade

    jammer_cartridge_projectile.Name = "jammer_cartridge_projectile"
    // TODO for later, maybe : set_resource_parent jammer_cartridge_projectile game_objects jammer_grenade_projectile
    jammer_cartridge_projectile.Damage0 = 0
    jammer_cartridge_projectile.Damage1 = 0
    jammer_cartridge_projectile.DamageAtEdge = 1.0f
    jammer_cartridge_projectile.DamageRadius = 10f
    jammer_cartridge_projectile.GrenadeProjectile = true
    jammer_cartridge_projectile.ProjectileDamageType = DamageType.Splash
    jammer_cartridge_projectile.InitialVelocity = 30
    jammer_cartridge_projectile.Lifespan = 15f
    jammer_cartridge_projectile.AdditionalEffect = true
    jammer_cartridge_projectile.JammerProjectile = true
    jammer_cartridge_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Player,
      EffectTarget.Validation.Player
    ) -> 1000
    jammer_cartridge_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.AMS
    ) -> 5000
    jammer_cartridge_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.MotionSensor
    ) -> 30000
    jammer_cartridge_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.Spitfire
    ) -> 30000
    jammer_cartridge_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Turret,
      EffectTarget.Validation.Turret
    ) -> 30000
    jammer_cartridge_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.VehicleNotAMS
    ) -> 10000
    ProjectileDefinition.CalculateDerivedFields(jammer_cartridge_projectile)
    jammer_cartridge_projectile.Modifiers = MaxDistanceCutoff

    jammer_cartridge_projectile_b.Name = "jammer_cartridge_projectile_b"
    // TODO for later, maybe : set_resource_parent jammer_cartridge_projectile_b game_objects jammer_grenade_projectile_enh
    jammer_cartridge_projectile_b.Damage0 = 0
    jammer_cartridge_projectile_b.Damage1 = 0
    jammer_cartridge_projectile_b.DamageAtEdge = 1.0f
    jammer_cartridge_projectile_b.DamageRadius = 10f
    jammer_cartridge_projectile_b.GrenadeProjectile = true
    jammer_cartridge_projectile_b.ProjectileDamageType = DamageType.Splash
    jammer_cartridge_projectile_b.InitialVelocity = 30
    jammer_cartridge_projectile_b.Lifespan = 2f
    jammer_cartridge_projectile_b.AdditionalEffect = true
    jammer_cartridge_projectile_b.JammerProjectile = true
    jammer_cartridge_projectile_b.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Player,
      EffectTarget.Validation.Player
    ) -> 1000
    jammer_cartridge_projectile_b.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.AMS
    ) -> 5000
    jammer_cartridge_projectile_b.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.MotionSensor
    ) -> 30000
    jammer_cartridge_projectile_b.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.Spitfire
    ) -> 30000
    jammer_cartridge_projectile_b.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Turret,
      EffectTarget.Validation.Turret
    ) -> 30000
    jammer_cartridge_projectile_b.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.VehicleNotAMS
    ) -> 10000
    ProjectileDefinition.CalculateDerivedFields(jammer_cartridge_projectile_b)
    jammer_cartridge_projectile_b.Modifiers = MaxDistanceCutoff

    jammer_grenade_projectile.Name = "jammer_grenade_projectile"
    jammer_grenade_projectile.Damage0 = 0
    jammer_grenade_projectile.Damage1 = 0
    jammer_grenade_projectile.DamageAtEdge = 1.0f
    jammer_grenade_projectile.DamageRadius = 10f
    jammer_grenade_projectile.GrenadeProjectile = true
    jammer_grenade_projectile.ProjectileDamageType = DamageType.Splash
    jammer_grenade_projectile.InitialVelocity = 30
    jammer_grenade_projectile.Lifespan = 15f
    jammer_grenade_projectile.AdditionalEffect = true
    jammer_grenade_projectile.JammerProjectile = true
    jammer_grenade_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Player,
      EffectTarget.Validation.Player
    ) -> 1000
    jammer_grenade_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.AMS
    ) -> 5000
    jammer_grenade_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.MotionSensor
    ) -> 30000
    jammer_grenade_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.Spitfire
    ) -> 30000
    jammer_grenade_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Turret,
      EffectTarget.Validation.Turret
    ) -> 30000
    jammer_grenade_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.VehicleNotAMS
    ) -> 10000
    ProjectileDefinition.CalculateDerivedFields(jammer_grenade_projectile)
    jammer_grenade_projectile.Modifiers = MaxDistanceCutoff

    jammer_grenade_projectile_enh.Name = "jammer_grenade_projectile_enh"
    // TODO for later, maybe : set_resource_parent jammer_grenade_projectile_enh game_objects jammer_grenade_projectile
    jammer_grenade_projectile_enh.Damage0 = 0
    jammer_grenade_projectile_enh.Damage1 = 0
    jammer_grenade_projectile_enh.DamageAtEdge = 1.0f
    jammer_grenade_projectile_enh.DamageRadius = 10f
    jammer_grenade_projectile_enh.GrenadeProjectile = true
    jammer_grenade_projectile_enh.ProjectileDamageType = DamageType.Splash
    jammer_grenade_projectile_enh.InitialVelocity = 30
    jammer_grenade_projectile_enh.Lifespan = 3f
    jammer_grenade_projectile_enh.AdditionalEffect = true
    jammer_grenade_projectile_enh.JammerProjectile = true
    jammer_grenade_projectile_enh.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Player,
      EffectTarget.Validation.Player
    ) -> 1000
    jammer_grenade_projectile_enh.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.AMS
    ) -> 5000
    jammer_grenade_projectile_enh.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.MotionSensor
    ) -> 30000
    jammer_grenade_projectile_enh.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.Spitfire
    ) -> 30000
    jammer_grenade_projectile_enh.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Turret,
      EffectTarget.Validation.Turret
    ) -> 30000
    jammer_grenade_projectile_enh.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.VehicleNotAMS
    ) -> 10000
    ProjectileDefinition.CalculateDerivedFields(jammer_grenade_projectile_enh)
    jammer_grenade_projectile_enh.Modifiers = MaxDistanceCutoff

    katana_projectile.Name = "katana_projectile"
    katana_projectile.Damage0 = 25
    katana_projectile.Damage1 = 0
    katana_projectile.ProjectileDamageType = DamageType.Direct
    katana_projectile.InitialVelocity = 100
    katana_projectile.Lifespan = .03f
    ProjectileDefinition.CalculateDerivedFields(katana_projectile)

    katana_projectileb.Name = "katana_projectileb"
    // TODO for later, maybe : set_resource_parent katana_projectileb game_objects katana_projectile
    katana_projectileb.Damage0 = 25
    katana_projectileb.Damage1 = 0
    katana_projectileb.ProjectileDamageType = DamageType.Direct
    katana_projectileb.InitialVelocity = 100
    katana_projectileb.Lifespan = .03f
    ProjectileDefinition.CalculateDerivedFields(katana_projectileb)

    lancer_projectile.Name = "lancer_projectile"
    lancer_projectile.Damage0 = 25
    lancer_projectile.Damage1 = 175
    lancer_projectile.Damage2 = 125
    lancer_projectile.Damage3 = 125
    lancer_projectile.Damage4 = 263
    lancer_projectile.ProjectileDamageType = DamageType.Direct
    lancer_projectile.InitialVelocity = 500
    lancer_projectile.Lifespan = 0.6f
    ProjectileDefinition.CalculateDerivedFields(lancer_projectile)

    lasher_projectile.Name = "lasher_projectile"
    lasher_projectile.Damage0 = 30
    lasher_projectile.Damage1 = 15
    lasher_projectile.Damage2 = 15
    lasher_projectile.Damage3 = 12
    lasher_projectile.Damage4 = 12
    lasher_projectile.ProjectileDamageType = DamageType.Direct
    lasher_projectile.DegradeDelay = 0.012f
    lasher_projectile.DegradeMultiplier = 0.3f
    lasher_projectile.InitialVelocity = 120
    lasher_projectile.LashRadius = 2.5f
    lasher_projectile.Lifespan = 0.75f
    lasher_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(lasher_projectile)
    lasher_projectile.Modifiers = List(
      DistanceDegrade,
      Lash
    )

    lasher_projectile_ap.Name = "lasher_projectile_ap"
    lasher_projectile_ap.Damage0 = 12
    lasher_projectile_ap.Damage1 = 25
    lasher_projectile_ap.Damage2 = 25
    lasher_projectile_ap.Damage3 = 28
    lasher_projectile_ap.Damage4 = 28
    lasher_projectile_ap.ProjectileDamageType = DamageType.Direct
    lasher_projectile_ap.DegradeDelay = 0.012f
    lasher_projectile_ap.DegradeMultiplier = 0.3f
    lasher_projectile_ap.InitialVelocity = 120
    lasher_projectile_ap.LashRadius = 2.5f
    lasher_projectile_ap.Lifespan = 0.75f
    lasher_projectile_ap.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(lasher_projectile_ap)
    lasher_projectile_ap.Modifiers = List(
      DistanceDegrade,
      Lash
    )

    liberator_bomb_cluster_bomblet_projectile.Name = "liberator_bomb_cluster_bomblet_projectile"
    liberator_bomb_cluster_bomblet_projectile.Damage0 = 75
    liberator_bomb_cluster_bomblet_projectile.Damage1 = 100
    liberator_bomb_cluster_bomblet_projectile.DamageAtEdge = 0.25f
    liberator_bomb_cluster_bomblet_projectile.DamageRadius = 3f
    liberator_bomb_cluster_bomblet_projectile.ProjectileDamageType = DamageType.Splash
    liberator_bomb_cluster_bomblet_projectile.InitialVelocity = 0
    liberator_bomb_cluster_bomblet_projectile.Lifespan = 30f
    ProjectileDefinition.CalculateDerivedFields(liberator_bomb_cluster_bomblet_projectile)
    liberator_bomb_cluster_bomblet_projectile.Modifiers = RadialDegrade

    liberator_bomb_cluster_projectile.Name = "liberator_bomb_cluster_projectile"
    liberator_bomb_cluster_projectile.Damage0 = 75
    liberator_bomb_cluster_projectile.Damage1 = 100
    liberator_bomb_cluster_projectile.DamageAtEdge = 0.25f
    liberator_bomb_cluster_projectile.DamageRadius = 3f
    liberator_bomb_cluster_projectile.ProjectileDamageType = DamageType.Direct
    liberator_bomb_cluster_projectile.InitialVelocity = 0
    liberator_bomb_cluster_projectile.Lifespan = 30f
    ProjectileDefinition.CalculateDerivedFields(liberator_bomb_cluster_projectile)

    liberator_bomb_projectile.Name = "liberator_bomb_projectile"
    liberator_bomb_projectile.Damage0 = 250
    liberator_bomb_projectile.Damage1 = 1000
    liberator_bomb_projectile.Damage2 = 1000
    liberator_bomb_projectile.Damage3 = 1000
    liberator_bomb_projectile.Damage4 = 600
    liberator_bomb_projectile.DamageAtEdge = 0.1f
    liberator_bomb_projectile.DamageRadius = 10f
    liberator_bomb_projectile.ProjectileDamageType = DamageType.Splash
    liberator_bomb_projectile.InitialVelocity = 0
    liberator_bomb_projectile.Lifespan = 30f
    ProjectileDefinition.CalculateDerivedFields(liberator_bomb_projectile)
    liberator_bomb_projectile.Modifiers = RadialDegrade

    maelstrom_grenade_damager.Name = "maelstrom_grenade_damager"
    maelstrom_grenade_damager.ProjectileDamageType = DamageType.Direct
    //todo the maelstrom_grenade_damage is something of a broken entity atm

    maelstrom_grenade_projectile.Name = "maelstrom_grenade_projectile"
    maelstrom_grenade_projectile.Damage0 = 32
    maelstrom_grenade_projectile.Damage1 = 60
    maelstrom_grenade_projectile.DamageRadius = 20f
    maelstrom_grenade_projectile.LashRadius = 5f
    maelstrom_grenade_projectile.GrenadeProjectile = true
    maelstrom_grenade_projectile.ProjectileDamageType = DamageType.Direct
    maelstrom_grenade_projectile.InitialVelocity = 30
    maelstrom_grenade_projectile.Lifespan = 2f
    maelstrom_grenade_projectile.DamageProxy = 464 //maelstrom_grenade_damager
    ProjectileDefinition.CalculateDerivedFields(maelstrom_grenade_projectile)
    maelstrom_grenade_projectile.Modifiers = RadialDegrade

    maelstrom_grenade_projectile_contact.Name = "maelstrom_grenade_projectile_contact"
    // TODO for later, maybe : set_resource_parent maelstrom_grenade_projectile_contact game_objects maelstrom_grenade_projectile
    maelstrom_grenade_projectile_contact.Damage0 = 32
    maelstrom_grenade_projectile_contact.Damage1 = 60
    maelstrom_grenade_projectile_contact.DamageRadius = 20f
    maelstrom_grenade_projectile_contact.LashRadius = 5f
    maelstrom_grenade_projectile_contact.GrenadeProjectile = true
    maelstrom_grenade_projectile_contact.ProjectileDamageType = DamageType.Direct
    maelstrom_grenade_projectile_contact.InitialVelocity = 30
    maelstrom_grenade_projectile_contact.Lifespan = 15f
    maelstrom_grenade_projectile_contact.DamageProxy = 464 //maelstrom_grenade_damager
    ProjectileDefinition.CalculateDerivedFields(maelstrom_grenade_projectile_contact)
    maelstrom_grenade_projectile_contact.Modifiers = RadialDegrade

    maelstrom_stream_projectile.Name = "maelstrom_stream_projectile"
    maelstrom_stream_projectile.Damage0 = 15
    maelstrom_stream_projectile.Damage1 = 6
    maelstrom_stream_projectile.ProjectileDamageType = DamageType.Direct
    maelstrom_stream_projectile.DegradeDelay = .075f
    maelstrom_stream_projectile.DegradeMultiplier = 0.5f
    maelstrom_stream_projectile.InitialVelocity = 200
    maelstrom_stream_projectile.Lifespan = 0.2f
    ProjectileDefinition.CalculateDerivedFields(maelstrom_stream_projectile)
    maelstrom_stream_projectile.Modifiers = MaxDistanceCutoff

    magcutter_projectile.Name = "magcutter_projectile"
    // TODO for later, maybe : set_resource_parent magcutter_projectile game_objects melee_ammo_projectile
    magcutter_projectile.Damage0 = 50
    magcutter_projectile.Damage1 = 0
    magcutter_projectile.ProjectileDamageType = DamageType.Direct
    magcutter_projectile.InitialVelocity = 100
    magcutter_projectile.Lifespan = .02f
    ProjectileDefinition.CalculateDerivedFields(magcutter_projectile)
    magcutter_projectile.Modifiers = List(MeleeBoosted, MaxDistanceCutoff)

    melee_ammo_projectile.Name = "melee_ammo_projectile"
    melee_ammo_projectile.Damage0 = 25
    melee_ammo_projectile.Damage1 = 0
    melee_ammo_projectile.ProjectileDamageType = DamageType.Direct
    melee_ammo_projectile.InitialVelocity = 100
    melee_ammo_projectile.Lifespan = .02f
    ProjectileDefinition.CalculateDerivedFields(melee_ammo_projectile)
    melee_ammo_projectile.Modifiers = List(MeleeBoosted, MaxDistanceCutoff)

    meteor_common.Name = "meteor_common"
    meteor_common.DamageAtEdge = .1f
    meteor_common.ProjectileDamageType = DamageType.Splash
    meteor_common.InitialVelocity = 0
    meteor_common.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_common)
    meteor_common.Modifiers = RadialDegrade

    meteor_projectile_b_large.Name = "meteor_projectile_b_large"
    // TODO for later, maybe : set_resource_parent meteor_projectile_b_large game_objects meteor_common
    meteor_projectile_b_large.Damage0 = 2500
    meteor_projectile_b_large.Damage1 = 5000
    meteor_projectile_b_large.DamageRadius = 15f
    meteor_projectile_b_large.DamageAtEdge = .1f
    meteor_projectile_b_large.ProjectileDamageType = DamageType.Splash
    meteor_projectile_b_large.InitialVelocity = 0
    meteor_projectile_b_large.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_projectile_b_large)
    meteor_projectile_b_large.Modifiers = RadialDegrade

    meteor_projectile_b_medium.Name = "meteor_projectile_b_medium"
    // TODO for later, maybe : set_resource_parent meteor_projectile_b_medium game_objects meteor_common
    meteor_projectile_b_medium.Damage0 = 1250
    meteor_projectile_b_medium.Damage1 = 2500
    meteor_projectile_b_medium.DamageRadius = 10f
    meteor_projectile_b_medium.DamageAtEdge = .1f
    meteor_projectile_b_medium.ProjectileDamageType = DamageType.Splash
    meteor_projectile_b_medium.InitialVelocity = 0
    meteor_projectile_b_medium.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_projectile_b_medium)
    meteor_projectile_b_medium.Modifiers = RadialDegrade

    meteor_projectile_b_small.Name = "meteor_projectile_b_small"
    // TODO for later, maybe : set_resource_parent meteor_projectile_b_small game_objects meteor_common
    meteor_projectile_b_small.Damage0 = 625
    meteor_projectile_b_small.Damage1 = 1250
    meteor_projectile_b_small.DamageRadius = 5f
    meteor_projectile_b_small.DamageAtEdge = .1f
    meteor_projectile_b_small.ProjectileDamageType = DamageType.Splash
    meteor_projectile_b_small.InitialVelocity = 0
    meteor_projectile_b_small.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_projectile_b_small)
    meteor_projectile_b_small.Modifiers = RadialDegrade

    meteor_projectile_large.Name = "meteor_projectile_large"
    // TODO for later, maybe : set_resource_parent meteor_projectile_large game_objects meteor_common
    meteor_projectile_large.Damage0 = 2500
    meteor_projectile_large.Damage1 = 5000
    meteor_projectile_large.DamageRadius = 15f
    meteor_projectile_large.DamageAtEdge = .1f
    meteor_projectile_large.ProjectileDamageType = DamageType.Splash
    meteor_projectile_large.InitialVelocity = 0
    meteor_projectile_large.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_projectile_large)
    meteor_projectile_large.Modifiers = RadialDegrade

    meteor_projectile_medium.Name = "meteor_projectile_medium"
    // TODO for later, maybe : set_resource_parent meteor_projectile_medium game_objects meteor_common
    meteor_projectile_medium.Damage0 = 1250
    meteor_projectile_medium.Damage1 = 2500
    meteor_projectile_medium.DamageRadius = 10f
    meteor_projectile_medium.DamageAtEdge = .1f
    meteor_projectile_medium.ProjectileDamageType = DamageType.Splash
    meteor_projectile_medium.InitialVelocity = 0
    meteor_projectile_medium.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_projectile_medium)
    meteor_projectile_medium.Modifiers = RadialDegrade

    meteor_projectile_small.Name = "meteor_projectile_small"
    // TODO for later, maybe : set_resource_parent meteor_projectile_small game_objects meteor_common
    meteor_projectile_small.Damage0 = 625
    meteor_projectile_small.Damage1 = 1250
    meteor_projectile_small.DamageRadius = 5f
    meteor_projectile_small.DamageAtEdge = .1f
    meteor_projectile_small.ProjectileDamageType = DamageType.Splash
    meteor_projectile_small.InitialVelocity = 0
    meteor_projectile_small.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_projectile_small)
    meteor_projectile_small.Modifiers = RadialDegrade

    mine_projectile.Name = "mine_projectile"
    mine_projectile.Lifespan = 0.01f
    mine_projectile.InitialVelocity = 300
    ProjectileDefinition.CalculateDerivedFields(mine_projectile)

    mine_sweeper_projectile.Name = "mine_sweeper_projectile"
    mine_sweeper_projectile.Damage0 = 0
    mine_sweeper_projectile.Damage1 = 0
    mine_sweeper_projectile.DamageAtEdge = .33f
    mine_sweeper_projectile.DamageRadius = 25f
    mine_sweeper_projectile.GrenadeProjectile = true
    mine_sweeper_projectile.ProjectileDamageType = DamageType.Splash
    mine_sweeper_projectile.InitialVelocity = 30
    mine_sweeper_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(mine_sweeper_projectile)
    mine_sweeper_projectile.Modifiers = RadialDegrade

    mine_sweeper_projectile_enh.Name = "mine_sweeper_projectile_enh"
    mine_sweeper_projectile_enh.Damage0 = 0
    mine_sweeper_projectile_enh.Damage1 = 0
    mine_sweeper_projectile_enh.DamageAtEdge = 0.33f
    mine_sweeper_projectile_enh.DamageRadius = 25f
    mine_sweeper_projectile_enh.GrenadeProjectile = true
    mine_sweeper_projectile_enh.ProjectileDamageType = DamageType.Splash
    mine_sweeper_projectile_enh.InitialVelocity = 30
    mine_sweeper_projectile_enh.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(mine_sweeper_projectile_enh)
    mine_sweeper_projectile_enh.Modifiers = RadialDegrade

    oicw_projectile.Name = "oicw_projectile"
    oicw_projectile.Damage0 = 50
    oicw_projectile.Damage1 = 50
    oicw_projectile.Acceleration = 15
    oicw_projectile.AccelerationUntil = 5f
    oicw_projectile.DamageAtEdge = 0.1f
    oicw_projectile.DamageRadius = 10f
    oicw_projectile.ProjectileDamageType = DamageType.Splash
    oicw_projectile.InitialVelocity = 5
    oicw_projectile.Lifespan = 6.1f
    oicw_projectile.DamageProxy = List(601, 601, 601, 601, 601) //5 x oicw_little_buddy
    oicw_projectile.registerAs = "rc-projectiles"
    oicw_projectile.ExistsOnRemoteClients = true
    oicw_projectile.RemoteClientData = (13107, 195)
    oicw_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(oicw_projectile)
    oicw_projectile.Modifiers = List(
      //ExplodingRadialDegrade,
      RadialDegrade
    )

    oicw_little_buddy.Name = "oicw_little_buddy"
    oicw_little_buddy.Damage0 = 75
    oicw_little_buddy.Damage1 = 75
    oicw_little_buddy.DamageAtEdge = 0.1f
    oicw_little_buddy.DamageRadius = 7.5f
    oicw_little_buddy.ProjectileDamageType = DamageType.Splash
    oicw_little_buddy.InitialVelocity = 40
    oicw_little_buddy.Lifespan = 0.5f
    oicw_little_buddy.registerAs = "rc-projectiles"
    oicw_little_buddy.ExistsOnRemoteClients = true //does not use RemoteClientData
    oicw_little_buddy.Packet = new LittleBuddyProjectileConverter
    //add_property oicw_little_buddy multi_stage_spawn_server_side true ...
    ProjectileDefinition.CalculateDerivedFields(oicw_little_buddy)
    oicw_little_buddy.Modifiers = List(
      ExplosionDamagesOnlyAbove
    )

    pellet_gun_projectile.Name = "pellet_gun_projectile"
    // TODO for later, maybe : set_resource_parent pellet_gun_projectile game_objects shotgun_shell_projectile
    pellet_gun_projectile.Damage0 = 12
    pellet_gun_projectile.Damage1 = 8
    pellet_gun_projectile.ProjectileDamageType = DamageType.Direct
    pellet_gun_projectile.InitialVelocity = 400
    pellet_gun_projectile.Lifespan = 0.1875f
    pellet_gun_projectile.UseDamage1Subtract = false
    ProjectileDefinition.CalculateDerivedFields(pellet_gun_projectile)

    phalanx_av_projectile.Name = "phalanx_av_projectile"
    phalanx_av_projectile.Damage0 = 60
    phalanx_av_projectile.Damage1 = 140
    phalanx_av_projectile.DamageAtEdge = 0.1f
    phalanx_av_projectile.DamageRadius = 5f
    phalanx_av_projectile.ProjectileDamageType = DamageType.Splash
    phalanx_av_projectile.InitialVelocity = 100
    phalanx_av_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(phalanx_av_projectile)
    phalanx_av_projectile.Modifiers = RadialDegrade

    phalanx_flak_projectile.Name = "phalanx_flak_projectile"
    phalanx_flak_projectile.Damage0 = 15
    phalanx_flak_projectile.Damage1 = 25
    phalanx_flak_projectile.Damage2 = 70
    phalanx_flak_projectile.DamageAtEdge = 1f
    phalanx_flak_projectile.DamageRadius = 10f
    phalanx_flak_projectile.ProjectileDamageType = DamageType.Direct
    phalanx_flak_projectile.ProjectileDamageTypeSecondary = DamageType.Splash
    phalanx_flak_projectile.InitialVelocity = 100
    phalanx_flak_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(phalanx_flak_projectile)
    phalanx_flak_projectile.Modifiers = List(
      //FlakHit,
      FlakBurst,
      MaxDistanceCutoff
    )

    phalanx_projectile.Name = "phalanx_projectile"
    phalanx_projectile.Damage0 = 20
    phalanx_projectile.Damage1 = 30
    phalanx_projectile.Damage2 = 30
    phalanx_projectile.Damage3 = 30
    phalanx_projectile.Damage4 = 18
    phalanx_projectile.ProjectileDamageType = DamageType.Direct
    phalanx_projectile.DegradeDelay = 0f
    phalanx_projectile.DegradeMultiplier = 0.25f
    phalanx_projectile.InitialVelocity = 400
    phalanx_projectile.Lifespan = 1f
    phalanx_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(phalanx_projectile)

    phoenix_missile_guided_projectile.Name = "phoenix_missile_guided_projectile"
    // TODO for later, maybe : set_resource_parent phoenix_missile_guided_projectile game_objects phoenix_missile_projectile
    phoenix_missile_guided_projectile.Damage0 = 80
    phoenix_missile_guided_projectile.Damage1 = 400
    phoenix_missile_guided_projectile.Damage2 = 400
    phoenix_missile_guided_projectile.Damage3 = 300
    phoenix_missile_guided_projectile.Damage4 = 600
    phoenix_missile_guided_projectile.Acceleration = 60
    phoenix_missile_guided_projectile.AccelerationUntil = 2.5f
    phoenix_missile_guided_projectile.DamageAtEdge = 0.3f
    phoenix_missile_guided_projectile.DamageRadius = 1.5f
    phoenix_missile_guided_projectile.ProjectileDamageType = DamageType.Splash
    phoenix_missile_guided_projectile.InitialVelocity = 0
    phoenix_missile_guided_projectile.Lifespan = 3f
    //not naturally a remote projectile, but being governed as one for convenience
    phoenix_missile_guided_projectile.registerAs = "rc-projectiles"
    phoenix_missile_guided_projectile.ExistsOnRemoteClients = true
    phoenix_missile_guided_projectile.RemoteClientData = (0, 63)
    phoenix_missile_guided_projectile.Packet = projectileConverter
    //
    ProjectileDefinition.CalculateDerivedFields(phoenix_missile_guided_projectile)
    phoenix_missile_guided_projectile.Modifiers = RadialDegrade

    phoenix_missile_projectile.Name = "phoenix_missile_projectile"
    phoenix_missile_projectile.Damage0 = 80
    phoenix_missile_projectile.Damage1 = 400
    phoenix_missile_projectile.Damage2 = 400
    phoenix_missile_projectile.Damage3 = 300
    phoenix_missile_projectile.Damage4 = 600
    phoenix_missile_projectile.Acceleration = 60
    phoenix_missile_projectile.AccelerationUntil = 2.5f
    phoenix_missile_projectile.DamageAtEdge = 0.3f
    phoenix_missile_projectile.DamageRadius = 1.5f
    phoenix_missile_projectile.ProjectileDamageType = DamageType.Splash
    phoenix_missile_projectile.InitialVelocity = 0
    phoenix_missile_projectile.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(phoenix_missile_projectile)
    phoenix_missile_projectile.Modifiers = RadialDegrade

    plasma_cartridge_projectile.Name = "plasma_cartridge_projectile"
    // TODO for later, maybe : set_resource_parent plasma_cartridge_projectile game_objects plasma_grenade_projectile
    plasma_cartridge_projectile.Damage0 = 20
    plasma_cartridge_projectile.Damage1 = 15
    plasma_cartridge_projectile.DamageAtEdge = 0.2f
    plasma_cartridge_projectile.DamageRadius = 7f
    plasma_cartridge_projectile.GrenadeProjectile = true
    plasma_cartridge_projectile.ProjectileDamageType = DamageType.Aggravated
    plasma_cartridge_projectile.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, 0.25f, 750), AggravatedInfo(DamageType.Splash, 0.25f, 1000)),
      Aura.Plasma,
      AggravatedTiming(3000),
      1.5f,
      true,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    plasma_cartridge_projectile.InitialVelocity = 30
    plasma_cartridge_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(plasma_cartridge_projectile)
    plasma_cartridge_projectile.Modifiers = List(
      InfantryAggravatedDirect,
      InfantryAggravatedDirectBurn,
      InfantryAggravatedSplash,
      InfantryAggravatedSplashBurn,
      RadialDegrade
    )

    plasma_cartridge_projectile_b.Name = "plasma_cartridge_projectile_b"
    // TODO for later, maybe : set_resource_parent plasma_cartridge_projectile_b game_objects plasma_grenade_projectile_B
    plasma_cartridge_projectile_b.Damage0 = 20
    plasma_cartridge_projectile_b.Damage1 = 15
    plasma_cartridge_projectile_b.DamageAtEdge = 0.2f
    plasma_cartridge_projectile_b.DamageRadius = 7f
    plasma_cartridge_projectile_b.GrenadeProjectile = true
    plasma_cartridge_projectile_b.ProjectileDamageType = DamageType.Aggravated
    plasma_cartridge_projectile_b.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, 0.25f, 750), AggravatedInfo(DamageType.Splash, 0.25f, 1000)),
      Aura.Plasma,
      AggravatedTiming(3000),
      1.5f,
      true,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    plasma_cartridge_projectile_b.InitialVelocity = 30
    plasma_cartridge_projectile_b.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(plasma_cartridge_projectile_b)
    plasma_cartridge_projectile_b.Modifiers = List(
      InfantryAggravatedDirect,
      InfantryAggravatedDirectBurn,
      InfantryAggravatedSplash,
      InfantryAggravatedSplashBurn,
      RadialDegrade
    )

    plasma_grenade_projectile.Name = "plasma_grenade_projectile"
    plasma_grenade_projectile.Damage0 = 40
    plasma_grenade_projectile.Damage1 = 30
    plasma_grenade_projectile.DamageAtEdge = 0.1f
    plasma_grenade_projectile.DamageRadius = 7f
    plasma_grenade_projectile.GrenadeProjectile = true
    plasma_grenade_projectile.ProjectileDamageType = DamageType.Aggravated
    plasma_grenade_projectile.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, 0.25f, 750), AggravatedInfo(DamageType.Splash, 0.25f, 1000)),
      Aura.Plasma,
      AggravatedTiming(3000),
      1.5f,
      true,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    plasma_grenade_projectile.InitialVelocity = 30
    plasma_grenade_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(plasma_grenade_projectile)
    plasma_grenade_projectile.Modifiers = List(
      InfantryAggravatedDirect,
      InfantryAggravatedDirectBurn,
      InfantryAggravatedSplash,
      InfantryAggravatedSplashBurn,
      RadialDegrade
    )

    plasma_grenade_projectile_B.Name = "plasma_grenade_projectile_B"
    // TODO for later, maybe : set_resource_parent plasma_grenade_projectile_B game_objects plasma_grenade_projectile
    plasma_grenade_projectile_B.Damage0 = 40
    plasma_grenade_projectile_B.Damage1 = 30
    plasma_grenade_projectile_B.DamageAtEdge = 0.1f
    plasma_grenade_projectile_B.DamageRadius = 7f
    plasma_grenade_projectile_B.GrenadeProjectile = true
    plasma_grenade_projectile_B.ProjectileDamageType = DamageType.Aggravated
    plasma_grenade_projectile_B.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, 0.25f, 750), AggravatedInfo(DamageType.Splash, 0.25f, 1000)),
      Aura.Plasma,
      AggravatedTiming(3000),
      1.5f,
      true,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    plasma_grenade_projectile_B.InitialVelocity = 30
    plasma_grenade_projectile_B.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(plasma_grenade_projectile_B)
    plasma_grenade_projectile_B.Modifiers = List(
      InfantryAggravatedDirect,
      InfantryAggravatedDirectBurn,
      InfantryAggravatedSplash,
      InfantryAggravatedSplashBurn,
      RadialDegrade
    )

    pounder_projectile.Name = "pounder_projectile"
    pounder_projectile.Damage0 = 31
    pounder_projectile.Damage1 = 120
    pounder_projectile.Damage2 = 120
    pounder_projectile.Damage3 = 75
    pounder_projectile.Damage4 = 132
    pounder_projectile.DamageAtEdge = 0.1f
    pounder_projectile.DamageRadius = 1f
    pounder_projectile.GrenadeProjectile = true
    pounder_projectile.ProjectileDamageType = DamageType.Splash
    pounder_projectile.InitialVelocity = 120
    pounder_projectile.Lifespan = 2.5f
    ProjectileDefinition.CalculateDerivedFields(pounder_projectile)
    pounder_projectile.Modifiers = RadialDegrade

    pounder_projectile_enh.Name = "pounder_projectile_enh"
    // TODO for later, maybe : set_resource_parent pounder_projectile_enh game_objects pounder_projectile
    pounder_projectile_enh.Damage0 = 31
    pounder_projectile_enh.Damage1 = 120
    pounder_projectile_enh.Damage2 = 120
    pounder_projectile_enh.Damage3 = 75
    pounder_projectile_enh.Damage4 = 132
    pounder_projectile_enh.DamageAtEdge = 0.1f
    pounder_projectile_enh.DamageRadius = 1f
    pounder_projectile_enh.GrenadeProjectile = true
    pounder_projectile_enh.ProjectileDamageType = DamageType.Splash
    pounder_projectile_enh.InitialVelocity = 120
    pounder_projectile_enh.Lifespan = 3.2f
    ProjectileDefinition.CalculateDerivedFields(pounder_projectile_enh)
    pounder_projectile_enh.Modifiers = RadialDegrade

    ppa_projectile.Name = "ppa_projectile"
    ppa_projectile.Damage0 = 20
    ppa_projectile.Damage1 = 20
    ppa_projectile.Damage2 = 40
    ppa_projectile.Damage3 = 20
    ppa_projectile.Damage4 = 13
    ppa_projectile.ProjectileDamageType = DamageType.Direct
    ppa_projectile.InitialVelocity = 400
    ppa_projectile.Lifespan = .5f
    ProjectileDefinition.CalculateDerivedFields(ppa_projectile)

    pulsar_ap_projectile.Name = "pulsar_ap_projectile"
    // TODO for later, maybe : set_resource_parent pulsar_ap_projectile game_objects pulsar_projectile
    pulsar_ap_projectile.Damage0 = 7
    pulsar_ap_projectile.Damage1 = 15
    pulsar_ap_projectile.ProjectileDamageType = DamageType.Direct
    pulsar_ap_projectile.DegradeDelay = 0.1f
    pulsar_ap_projectile.DegradeMultiplier = 0.5f
    pulsar_ap_projectile.InitialVelocity = 500
    pulsar_ap_projectile.Lifespan = .4f
    pulsar_ap_projectile.UseDamage1Subtract = true
    pulsar_ap_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(pulsar_ap_projectile)

    pulsar_projectile.Name = "pulsar_projectile"
    pulsar_projectile.Damage0 = 20
    pulsar_projectile.Damage1 = 10
    pulsar_projectile.ProjectileDamageType = DamageType.Direct
    pulsar_projectile.DegradeDelay = 0.1f
    pulsar_projectile.DegradeMultiplier = 0.4f
    pulsar_projectile.InitialVelocity = 500
    pulsar_projectile.Lifespan = .4f
    pulsar_projectile.UseDamage1Subtract = true
    pulsar_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(pulsar_projectile)

    quasar_projectile.Name = "quasar_projectile"
    quasar_projectile.Damage0 = 18
    quasar_projectile.Damage1 = 8
    quasar_projectile.ProjectileDamageType = DamageType.Direct
    quasar_projectile.DegradeDelay = 0.045f
    quasar_projectile.DegradeMultiplier = 0.5f
    quasar_projectile.InitialVelocity = 500
    quasar_projectile.Lifespan = .4f
    quasar_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(quasar_projectile)

    radiator_cloud.Name = "radiator_cloud"
    radiator_cloud.Damage0 = 2
    radiator_cloud.DamageAtEdge = 1.0f
    radiator_cloud.DamageRadius = 5f
    radiator_cloud.DamageToHealthOnly = true
    radiator_cloud.radiation_cloud = true
    radiator_cloud.ProjectileDamageType = DamageType.Radiation
    //custom aggravated information
    radiator_cloud.ProjectileDamageTypeSecondary = DamageType.Aggravated
    radiator_cloud.Aggravated = AggravatedDamage(
      AggravatedInfo(DamageType.Splash, 1f, 80),
      Aura.None,
      AggravatedTiming(250, 2),
      0f,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    radiator_cloud.Lifespan = 10.0f
    ProjectileDefinition.CalculateDerivedFields(radiator_cloud)
    radiator_cloud.registerAs = "rc-projectiles"
    radiator_cloud.ExistsOnRemoteClients = true
    radiator_cloud.Packet = radCloudConverter
    //radiator_cloud.Geometry = GeometryForm.representProjectileBySphere()
    radiator_cloud.Modifiers = List(
      MaxDistanceCutoff,
      InfantryAggravatedRadiation,
      InfantryAggravatedRadiationBurn,
      ShieldAgainstRadiation
    )

    radiator_grenade_projectile.Name = "radiator_grenade_projectile" // Todo : Radiator damages ?
    radiator_grenade_projectile.GrenadeProjectile = true             //not really, but technically yes
    radiator_grenade_projectile.ProjectileDamageType = DamageType.Direct
    radiator_grenade_projectile.InitialVelocity = 30
    radiator_grenade_projectile.Lifespan = 3f
    radiator_grenade_projectile.DamageProxy = 717 //radiator_cloud
    ProjectileDefinition.CalculateDerivedFields(radiator_grenade_projectile)

    radiator_sticky_projectile.Name = "radiator_sticky_projectile"
    // TODO for later, maybe : set_resource_parent radiator_sticky_projectile game_objects radiator_grenade_projectile
    radiator_sticky_projectile.GrenadeProjectile = true //not really, but technically yes
    radiator_sticky_projectile.ProjectileDamageType = DamageType.Direct
    radiator_sticky_projectile.InitialVelocity = 30
    radiator_sticky_projectile.Lifespan = 4f
    radiator_sticky_projectile.DamageProxy = 717 //radiator_cloud
    ProjectileDefinition.CalculateDerivedFields(radiator_sticky_projectile)

    reaver_rocket_projectile.Name = "reaver_rocket_projectile"
    reaver_rocket_projectile.Damage0 = 25
    reaver_rocket_projectile.Damage1 = 88
    reaver_rocket_projectile.Damage2 = 75
    reaver_rocket_projectile.Damage3 = 75
    reaver_rocket_projectile.Damage4 = 88
    reaver_rocket_projectile.Acceleration = 50
    reaver_rocket_projectile.AccelerationUntil = 1f
    reaver_rocket_projectile.DamageAtEdge = 0.1f
    reaver_rocket_projectile.DamageRadius = 3f
    reaver_rocket_projectile.ProjectileDamageType = DamageType.Splash
    reaver_rocket_projectile.InitialVelocity = 100
    reaver_rocket_projectile.Lifespan = 2.1f
    ProjectileDefinition.CalculateDerivedFields(reaver_rocket_projectile)
    reaver_rocket_projectile.Modifiers = RadialDegrade

    rocket_projectile.Name = "rocket_projectile"
    rocket_projectile.Damage0 = 50
    rocket_projectile.Damage1 = 105
    rocket_projectile.Damage2 = 75
    rocket_projectile.Damage3 = 75
    rocket_projectile.Damage4 = 75
    rocket_projectile.Acceleration = 10
    rocket_projectile.AccelerationUntil = 2f
    rocket_projectile.DamageAtEdge = .5f
    rocket_projectile.DamageRadius = 3f
    rocket_projectile.ProjectileDamageType = DamageType.Splash
    rocket_projectile.InitialVelocity = 50
    rocket_projectile.Lifespan = 8f
    ProjectileDefinition.CalculateDerivedFields(rocket_projectile)
    rocket_projectile.Modifiers = RadialDegrade

    rocklet_flak_projectile.Name = "rocklet_flak_projectile"
    rocklet_flak_projectile.Damage0 = 20
    rocklet_flak_projectile.Damage1 = 30
    rocklet_flak_projectile.Damage2 = 57
    rocklet_flak_projectile.Damage3 = 30
    rocklet_flak_projectile.Damage4 = 50
    rocklet_flak_projectile.DamageAtEdge = 0.25f
    rocklet_flak_projectile.DamageRadius = 8f
    rocklet_flak_projectile.ProjectileDamageType = DamageType.Direct
    rocklet_flak_projectile.ProjectileDamageTypeSecondary = DamageType.Splash
    rocklet_flak_projectile.InitialVelocity = 60
    rocklet_flak_projectile.Lifespan = 3.2f
    ProjectileDefinition.CalculateDerivedFields(rocklet_flak_projectile)
    rocklet_flak_projectile.Modifiers = List(
      //FlakHit,
      FlakBurst,
      MaxDistanceCutoff
    )

    rocklet_jammer_projectile.Name = "rocklet_jammer_projectile"
    rocklet_jammer_projectile.Damage0 = 0
    rocklet_jammer_projectile.Acceleration = 10
    rocklet_jammer_projectile.AccelerationUntil = 2f
    rocklet_jammer_projectile.DamageAtEdge = 1.0f
    rocklet_jammer_projectile.DamageRadius = 10f
    rocklet_jammer_projectile.ProjectileDamageType = DamageType.Splash
    rocklet_jammer_projectile.InitialVelocity = 50
    rocklet_jammer_projectile.Lifespan = 8f
    ProjectileDefinition.CalculateDerivedFields(rocklet_jammer_projectile)
    //TODO rocklet_jammer_projectile.Modifiers = RadialDegrade?

    scattercannon_projectile.Name = "scattercannon_projectile"
    scattercannon_projectile.Damage0 = 11
    scattercannon_projectile.Damage1 = 5
    scattercannon_projectile.ProjectileDamageType = DamageType.Direct
    scattercannon_projectile.InitialVelocity = 400
    scattercannon_projectile.Lifespan = 0.25f
    scattercannon_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(scattercannon_projectile)

    scythe_projectile.Name = "scythe_projectile"
    scythe_projectile.Damage0 = 30
    scythe_projectile.Damage1 = 20
    scythe_projectile.ProjectileDamageType = DamageType.Direct
    scythe_projectile.DegradeDelay = .015f
    scythe_projectile.DegradeMultiplier = 0.35f
    scythe_projectile.InitialVelocity = 60
    scythe_projectile.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(scythe_projectile)

    scythe_projectile_slave.Name = "scythe_projectile_slave" // Todo how does it work ?
    scythe_projectile_slave.InitialVelocity = 30
    scythe_projectile_slave.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(scythe_projectile_slave)

    shotgun_shell_AP_projectile.Name = "shotgun_shell_AP_projectile"
    // TODO for later, maybe : set_resource_parent shotgun_shell_AP_projectile game_objects shotgun_shell_projectile
    shotgun_shell_AP_projectile.Damage0 = 5
    shotgun_shell_AP_projectile.Damage1 = 10
    shotgun_shell_AP_projectile.ProjectileDamageType = DamageType.Direct
    shotgun_shell_AP_projectile.InitialVelocity = 400
    shotgun_shell_AP_projectile.Lifespan = 0.25f
    shotgun_shell_AP_projectile.UseDamage1Subtract = true
    shotgun_shell_AP_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(shotgun_shell_AP_projectile)

    shotgun_shell_projectile.Name = "shotgun_shell_projectile"
    shotgun_shell_projectile.Damage0 = 12
    shotgun_shell_projectile.Damage1 = 5
    shotgun_shell_projectile.ProjectileDamageType = DamageType.Direct
    shotgun_shell_projectile.InitialVelocity = 400
    shotgun_shell_projectile.Lifespan = 0.25f
    shotgun_shell_projectile.UseDamage1Subtract = true
    shotgun_shell_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(shotgun_shell_projectile)

    six_shooter_projectile.Name = "six_shooter_projectile"
    // TODO for later, maybe : set_resource_parent six_shooter_projectile game_objects 9mmbullet_projectile
    six_shooter_projectile.Damage0 = 22
    six_shooter_projectile.Damage1 = 20
    six_shooter_projectile.ProjectileDamageType = DamageType.Direct
    six_shooter_projectile.DegradeDelay = 0.15f
    six_shooter_projectile.DegradeMultiplier = 0.25f
    six_shooter_projectile.InitialVelocity = 500
    six_shooter_projectile.Lifespan = 0.4f
    six_shooter_projectile.UseDamage1Subtract = false
    ProjectileDefinition.CalculateDerivedFields(six_shooter_projectile)

    skyguard_flak_cannon_projectile.Name = "skyguard_flak_cannon_projectile"
    skyguard_flak_cannon_projectile.Damage0 = 15
    skyguard_flak_cannon_projectile.Damage1 = 25
    skyguard_flak_cannon_projectile.Damage2 = 50
    skyguard_flak_cannon_projectile.DamageAtEdge = 1f
    skyguard_flak_cannon_projectile.DamageRadius = 10f
    skyguard_flak_cannon_projectile.ProjectileDamageType = DamageType.Direct
    skyguard_flak_cannon_projectile.ProjectileDamageTypeSecondary = DamageType.Splash
    skyguard_flak_cannon_projectile.InitialVelocity = 100
    skyguard_flak_cannon_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(skyguard_flak_cannon_projectile)
    skyguard_flak_cannon_projectile.Modifiers = List(
      //FlakHit,
      FlakBurst,
      MaxDistanceCutoff
    )

    sparrow_projectile.Name = "sparrow_projectile"
    sparrow_projectile.Damage0 = 35
    sparrow_projectile.Damage1 = 50
    sparrow_projectile.Damage2 = 125
    sparrow_projectile.Acceleration = 12
    sparrow_projectile.AccelerationUntil = 5f
    sparrow_projectile.DamageAtEdge = 0.1f
    sparrow_projectile.DamageRadius = 3f
    sparrow_projectile.ProjectileDamageType = DamageType.Splash
    sparrow_projectile.InitialVelocity = 60
    sparrow_projectile.Lifespan = 5.85f
    sparrow_projectile.registerAs = "rc-projectiles"
    sparrow_projectile.ExistsOnRemoteClients = true
    sparrow_projectile.RemoteClientData = (13107, 187)
    sparrow_projectile.AutoLock = true
    sparrow_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(sparrow_projectile)
    sparrow_projectile.Modifiers = RadialDegrade

    sparrow_secondary_projectile.Name = "sparrow_secondary_projectile"
    // TODO for later, maybe : set_resource_parent sparrow_secondary_projectile game_objects sparrow_projectile
    sparrow_secondary_projectile.Damage0 = 35
    sparrow_secondary_projectile.Damage1 = 50
    sparrow_secondary_projectile.Damage2 = 125
    sparrow_secondary_projectile.Acceleration = 12
    sparrow_secondary_projectile.AccelerationUntil = 5f
    sparrow_secondary_projectile.DamageAtEdge = 0.1f
    sparrow_secondary_projectile.DamageRadius = 3f
    sparrow_secondary_projectile.ProjectileDamageType = DamageType.Splash
    sparrow_secondary_projectile.InitialVelocity = 60
    sparrow_secondary_projectile.Lifespan = 5.85f
    sparrow_secondary_projectile.registerAs = "rc-projectiles"
    sparrow_secondary_projectile.ExistsOnRemoteClients = true
    sparrow_secondary_projectile.RemoteClientData = (13107, 187)
    sparrow_secondary_projectile.AutoLock = true
    sparrow_secondary_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(sparrow_secondary_projectile)
    sparrow_secondary_projectile.Modifiers = RadialDegrade

    spiker_projectile.Name = "spiker_projectile"
    spiker_projectile.Charging = ChargeDamage(4, StandardDamageProfile(damage0 = Some(20), damage1 = Some(20)))
    spiker_projectile.Damage0 = 75
    spiker_projectile.Damage1 = 75
    spiker_projectile.DamageAtEdge = 0.1f
    spiker_projectile.DamageRadius = 5f
    spiker_projectile.DamageRadiusMin = 1f
    spiker_projectile.ProjectileDamageType = DamageType.Splash
    spiker_projectile.InitialVelocity = 40
    spiker_projectile.Lifespan = 5f
    spiker_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(spiker_projectile)
    spiker_projectile.Modifiers = List(
      SpikerChargeDamage,
      RadialDegrade
    )

    spitfire_aa_ammo_projectile.Name = "spitfire_aa_ammo_projectile"
    spitfire_aa_ammo_projectile.Damage0 = 5
    spitfire_aa_ammo_projectile.Damage1 = 15
    spitfire_aa_ammo_projectile.Damage2 = 12
    spitfire_aa_ammo_projectile.Damage3 = 5
    spitfire_aa_ammo_projectile.Damage4 = 15
    spitfire_aa_ammo_projectile.DamageAtEdge = 1f
    spitfire_aa_ammo_projectile.DamageRadius = 10f
    spitfire_aa_ammo_projectile.ProjectileDamageType = DamageType.Direct
    spitfire_aa_ammo_projectile.ProjectileDamageTypeSecondary = DamageType.Splash
    spitfire_aa_ammo_projectile.InitialVelocity = 100
    spitfire_aa_ammo_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(spitfire_aa_ammo_projectile)
    spitfire_aa_ammo_projectile.Modifiers = List(
      //FlakHit,
      FlakBurst,
      MaxDistanceCutoff
    )

    spitfire_ammo_projectile.Name = "spitfire_ammo_projectile"
    spitfire_ammo_projectile.Damage0 = 15
    spitfire_ammo_projectile.Damage1 = 10
    spitfire_ammo_projectile.ProjectileDamageType = DamageType.Direct
    spitfire_ammo_projectile.DegradeDelay = .01f
    spitfire_ammo_projectile.DegradeMultiplier = 0.5f
    spitfire_ammo_projectile.InitialVelocity = 100
    spitfire_ammo_projectile.Lifespan = .5f
    spitfire_ammo_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(spitfire_ammo_projectile)

    starfire_projectile.Name = "starfire_projectile"
    starfire_projectile.Damage0 = 16
    starfire_projectile.Damage1 = 20
    starfire_projectile.Damage2 = 58
    starfire_projectile.Acceleration = 12
    starfire_projectile.AccelerationUntil = 5f
    starfire_projectile.ProjectileDamageType = DamageType.Aggravated
    starfire_projectile.Aggravated = AggravatedDamage(
      AggravatedInfo(DamageType.Direct, 0.25f, 250),
      Aura.Comet,
      2000,
      0f,
      true,
      List(
        TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player),
        TargetValidation(EffectTarget.Category.Vehicle, EffectTarget.Validation.Vehicle),
        TargetValidation(EffectTarget.Category.Turret, EffectTarget.Validation.Turret)
      )
    )
    starfire_projectile.InitialVelocity = 45
    starfire_projectile.Lifespan = 7.8f
    starfire_projectile.registerAs = "rc-projectiles"
    starfire_projectile.ExistsOnRemoteClients = true
    starfire_projectile.RemoteClientData = (39577, 249)
    starfire_projectile.AutoLock = true
    starfire_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(starfire_projectile)
    starfire_projectile.Modifiers = List(
      StarfireAggravatedBurn,
      RadialDegrade
    )

    striker_missile_projectile.Name = "striker_missile_projectile"
    striker_missile_projectile.Damage0 = 35
    striker_missile_projectile.Damage1 = 175
    striker_missile_projectile.Damage2 = 125
    striker_missile_projectile.Damage3 = 125
    striker_missile_projectile.Damage4 = 263
    striker_missile_projectile.Acceleration = 20
    striker_missile_projectile.AccelerationUntil = 2f
    striker_missile_projectile.DamageAtEdge = 0.1f
    striker_missile_projectile.DamageRadius = 1.5f
    striker_missile_projectile.ProjectileDamageType = DamageType.Splash
    striker_missile_projectile.InitialVelocity = 30
    striker_missile_projectile.Lifespan = 4.2f
    ProjectileDefinition.CalculateDerivedFields(striker_missile_projectile)
    striker_missile_projectile.Modifiers = RadialDegrade

    striker_missile_targeting_projectile.Name = "striker_missile_targeting_projectile"
    // TODO for later, maybe : set_resource_parent striker_missile_targeting_projectile game_objects striker_missile_projectile
    striker_missile_targeting_projectile.Damage0 = 35
    striker_missile_targeting_projectile.Damage1 = 175
    striker_missile_targeting_projectile.Damage2 = 125
    striker_missile_targeting_projectile.Damage3 = 125
    striker_missile_targeting_projectile.Damage4 = 263
    striker_missile_targeting_projectile.Acceleration = 20
    striker_missile_targeting_projectile.AccelerationUntil = 2f
    striker_missile_targeting_projectile.DamageAtEdge = 0.1f
    striker_missile_targeting_projectile.DamageRadius = 1.5f
    striker_missile_targeting_projectile.ProjectileDamageType = DamageType.Splash
    striker_missile_targeting_projectile.InitialVelocity = 30
    striker_missile_targeting_projectile.Lifespan = 4.2f
    striker_missile_targeting_projectile.registerAs = "rc-projectiles"
    striker_missile_targeting_projectile.ExistsOnRemoteClients = true
    striker_missile_targeting_projectile.RemoteClientData = (26214, 134)
    striker_missile_targeting_projectile.AutoLock = true
    striker_missile_targeting_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(striker_missile_targeting_projectile)
    striker_missile_targeting_projectile.Modifiers = RadialDegrade

    trek_projectile.Name = "trek_projectile"
    trek_projectile.Damage0 = 0
    trek_projectile.Damage1 = 0
    trek_projectile.Damage2 = 0
    trek_projectile.Damage3 = 0
    trek_projectile.Damage4 = 0
    trek_projectile.Acceleration = -20
    trek_projectile.AccelerationUntil = 1f
    trek_projectile.ProjectileDamageType = DamageType.Direct
    trek_projectile.InitialVelocity = 40
    trek_projectile.Lifespan = 7f
    trek_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(trek_projectile)
    trek_projectile.Modifiers = MaxDistanceCutoff

    vanu_sentry_turret_projectile.Name = "vanu_sentry_turret_projectile"
    vanu_sentry_turret_projectile.Damage0 = 25
    vanu_sentry_turret_projectile.Damage1 = 35
    vanu_sentry_turret_projectile.Damage2 = 100
    vanu_sentry_turret_projectile.DamageAtEdge = 0.1f
    vanu_sentry_turret_projectile.DamageRadius = 3f
    vanu_sentry_turret_projectile.ProjectileDamageType = DamageType.Splash
    vanu_sentry_turret_projectile.InitialVelocity = 240
    vanu_sentry_turret_projectile.Lifespan = 1.3f
    ProjectileDefinition.CalculateDerivedFields(vanu_sentry_turret_projectile)
    vanu_sentry_turret_projectile.Modifiers = RadialDegrade

    vulture_bomb_projectile.Name = "vulture_bomb_projectile"
    vulture_bomb_projectile.Damage0 = 175
    vulture_bomb_projectile.Damage1 = 1750
    vulture_bomb_projectile.Damage2 = 1000
    vulture_bomb_projectile.Damage3 = 400
    vulture_bomb_projectile.Damage4 = 1500
    vulture_bomb_projectile.DamageAtEdge = 0.1f
    vulture_bomb_projectile.DamageRadius = 10f
    vulture_bomb_projectile.ProjectileDamageType = DamageType.Splash
    vulture_bomb_projectile.InitialVelocity = 0
    vulture_bomb_projectile.Lifespan = 30f
    ProjectileDefinition.CalculateDerivedFields(vulture_bomb_projectile)
    vulture_bomb_projectile.Modifiers = RadialDegrade

    vulture_nose_bullet_projectile.Name = "vulture_nose_bullet_projectile"
    vulture_nose_bullet_projectile.Damage0 = 12
    vulture_nose_bullet_projectile.Damage1 = 15
    vulture_nose_bullet_projectile.Damage2 = 10
    vulture_nose_bullet_projectile.Damage3 = 10
    vulture_nose_bullet_projectile.Damage4 = 15
    vulture_nose_bullet_projectile.ProjectileDamageType = DamageType.Direct
    vulture_nose_bullet_projectile.DegradeDelay = .4f
    vulture_nose_bullet_projectile.DegradeMultiplier = 0.7f
    vulture_nose_bullet_projectile.InitialVelocity = 500
    vulture_nose_bullet_projectile.Lifespan = 0.46f
    ProjectileDefinition.CalculateDerivedFields(vulture_nose_bullet_projectile)

    vulture_tail_bullet_projectile.Name = "vulture_tail_bullet_projectile"
    vulture_tail_bullet_projectile.Damage0 = 25
    vulture_tail_bullet_projectile.Damage1 = 35
    vulture_tail_bullet_projectile.Damage2 = 50
    vulture_tail_bullet_projectile.ProjectileDamageType = DamageType.Direct
    vulture_tail_bullet_projectile.DegradeDelay = .02f
    vulture_tail_bullet_projectile.DegradeMultiplier = 0.5f
    vulture_tail_bullet_projectile.InitialVelocity = 500
    vulture_tail_bullet_projectile.Lifespan = 0.6f
    ProjectileDefinition.CalculateDerivedFields(vulture_tail_bullet_projectile)

    wasp_gun_projectile.Name = "wasp_gun_projectile"
    wasp_gun_projectile.Damage0 = 10
    wasp_gun_projectile.Damage1 = 15
    wasp_gun_projectile.Damage2 = 25
    wasp_gun_projectile.Damage3 = 17
    wasp_gun_projectile.Damage4 = 7
    wasp_gun_projectile.ProjectileDamageType = DamageType.Direct
    wasp_gun_projectile.DegradeDelay = .015f
    wasp_gun_projectile.DegradeMultiplier = 0.5f
    wasp_gun_projectile.InitialVelocity = 500
    wasp_gun_projectile.Lifespan = 0.5f
    wasp_gun_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(wasp_gun_projectile)

    wasp_rocket_projectile.Name = "wasp_rocket_projectile"
    wasp_rocket_projectile.Damage0 = 35
    wasp_rocket_projectile.Damage1 = 50
    wasp_rocket_projectile.Damage2 = 300
    wasp_rocket_projectile.Acceleration = 10
    wasp_rocket_projectile.AccelerationUntil = 5f
    wasp_rocket_projectile.DamageAtEdge = 0.1f
    wasp_rocket_projectile.DamageRadius = 3f
    wasp_rocket_projectile.ProjectileDamageType = DamageType.Splash
    wasp_rocket_projectile.InitialVelocity = 60
    wasp_rocket_projectile.Lifespan = 6.5f
    wasp_rocket_projectile.registerAs = "rc-projectiles"
    wasp_rocket_projectile.ExistsOnRemoteClients = true
    wasp_rocket_projectile.RemoteClientData = (0, 208)
    wasp_rocket_projectile.AutoLock = true
    wasp_rocket_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(wasp_rocket_projectile)
    wasp_rocket_projectile.Modifiers = RadialDegrade

    winchester_projectile.Name = "winchester_projectile"
    // TODO for later, maybe : set_resource_parent winchester_projectile game_objects bolt_projectile
    winchester_projectile.Damage0 = 80
    winchester_projectile.Damage1 = 40
    winchester_projectile.Damage2 = 50
    winchester_projectile.Damage3 = 50
    winchester_projectile.Damage4 = 75
    winchester_projectile.ProjectileDamageType = DamageType.Direct
    winchester_projectile.InitialVelocity = 500
    winchester_projectile.Lifespan = 0.6f
    ProjectileDefinition.CalculateDerivedFields(winchester_projectile)
  }

  /**
    * Initialize `ProjectileDefinition` globals for projectiles utilized by battleframe robotics.
    */
  private def init_bfr_projectile(): Unit = {
    val projectileConverter: ProjectileConverter = new ProjectileConverter
    val radCloudConverter: RadiationCloudConverter = new RadiationCloudConverter

    aphelion_immolation_cannon_projectile.Name = "aphelion_immolation_cannon_projectile"
    aphelion_immolation_cannon_projectile.Damage0 = 55
    aphelion_immolation_cannon_projectile.Damage1 = 225
    aphelion_immolation_cannon_projectile.Damage2 = 210
    aphelion_immolation_cannon_projectile.Damage3 = 135
    aphelion_immolation_cannon_projectile.Damage4 = 140
    aphelion_immolation_cannon_projectile.DamageAtEdge = 0.1f
    aphelion_immolation_cannon_projectile.DamageRadius = 2.0f
    aphelion_immolation_cannon_projectile.ProjectileDamageType = DamageType.Splash
    aphelion_immolation_cannon_projectile.InitialVelocity = 250
    aphelion_immolation_cannon_projectile.Lifespan = 1.4f
    ProjectileDefinition.CalculateDerivedFields(aphelion_immolation_cannon_projectile)
    aphelion_immolation_cannon_projectile.Modifiers = RadialDegrade

    aphelion_laser_projectile.Name = "aphelion_laser_projectile"
    aphelion_laser_projectile.Damage0 = 3
    aphelion_laser_projectile.Damage1 = 5
    aphelion_laser_projectile.Damage2 = 5
    aphelion_laser_projectile.Damage3 = 4
    aphelion_laser_projectile.Damage4 = 5
    aphelion_laser_projectile.ProjectileDamageType = DamageType.Direct
    aphelion_laser_projectile.DegradeDelay = .05f
    aphelion_laser_projectile.DegradeMultiplier = 0.5f
    aphelion_laser_projectile.InitialVelocity = 500
    aphelion_laser_projectile.Lifespan = 0.35f
    ProjectileDefinition.CalculateDerivedFields(aphelion_laser_projectile)

    aphelion_plasma_cloud.Name = "aphelion_plasma_cloud"
    aphelion_plasma_cloud.Damage0 = 3
    aphelion_plasma_cloud.DamageAtEdge = 1.0f
    aphelion_plasma_cloud.DamageRadius = 3f
    aphelion_plasma_cloud.radiation_cloud = true
    aphelion_plasma_cloud.ProjectileDamageType = DamageType.Aggravated
    aphelion_plasma_cloud.Aggravated = AggravatedDamage(
      AggravatedInfo(DamageType.Splash, 0.5f, 1000),
      Aura.Napalm,
      AggravatedTiming(10000, 2), //10000
      10f, //aphelion_plasma_rocket_projectile.aggravated_damage_max_factor
      true,
      List(
        TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player)
      )
    )
    aphelion_plasma_cloud.Lifespan = 10.0f
    ProjectileDefinition.CalculateDerivedFields(aphelion_plasma_cloud)
    aphelion_plasma_cloud.registerAs = "rc-projectiles"
    aphelion_plasma_cloud.ExistsOnRemoteClients = true
    aphelion_plasma_cloud.Packet = radCloudConverter
    //aphelion_plasma_cloud.Geometry = GeometryForm.representProjectileBySphere()
    aphelion_plasma_cloud.Modifiers = List( //TODO placeholder values
      MaxDistanceCutoff,
      InfantryAggravatedRadiation,
      InfantryAggravatedRadiationBurn,
      ShieldAgainstRadiation
    )

    aphelion_plasma_rocket_projectile.Name = "aphelion_plasma_rocket_projectile"
    //has property aggravated_damage_max_factor, but it's the aphelion_plasma_cloud that performs aggravated damage
    aphelion_plasma_rocket_projectile.Damage0 = 38
    aphelion_plasma_rocket_projectile.Damage1 = 70
    aphelion_plasma_rocket_projectile.Damage2 = 95
    aphelion_plasma_rocket_projectile.Damage3 = 55
    aphelion_plasma_rocket_projectile.Damage4 = 60
    aphelion_plasma_rocket_projectile.Acceleration = 20
    aphelion_plasma_rocket_projectile.AccelerationUntil = 2f
    aphelion_plasma_rocket_projectile.DamageAtEdge = .1f
    aphelion_plasma_rocket_projectile.DamageRadius = 3f
    aphelion_plasma_rocket_projectile.ProjectileDamageType = DamageType.Splash
    aphelion_plasma_rocket_projectile.DamageProxy = 96 //aphelion_plama_cloud
    aphelion_plasma_rocket_projectile.InitialVelocity = 75
    aphelion_plasma_rocket_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(aphelion_plasma_rocket_projectile)
    aphelion_plasma_rocket_projectile.Modifiers = RadialDegrade

    aphelion_ppa_projectile.Name = "aphelion_ppa_projectile"
    // TODO for later, maybe : set_resource_parent aphelion_ppa_projectile game_objects ppa_projectile
    aphelion_ppa_projectile.Damage0 = 31
    aphelion_ppa_projectile.Damage1 = 84
    aphelion_ppa_projectile.Damage2 = 58
    aphelion_ppa_projectile.Damage3 = 57
    aphelion_ppa_projectile.Damage4 = 60
    aphelion_ppa_projectile.DamageAtEdge = 0.10f
    aphelion_ppa_projectile.DamageRadius = 1f
    aphelion_ppa_projectile.ProjectileDamageType = DamageType.Splash
    aphelion_ppa_projectile.DegradeDelay = .5f
    aphelion_ppa_projectile.DegradeMultiplier = 0.55f
    aphelion_ppa_projectile.InitialVelocity = 350
    aphelion_ppa_projectile.Lifespan = .7f
    ProjectileDefinition.CalculateDerivedFields(aphelion_ppa_projectile)
    aphelion_ppa_projectile.Modifiers = RadialDegrade

    aphelion_starfire_projectile.Name = "aphelion_starfire_projectile"
    // TODO for later, maybe : set_resource_parent aphelion_starfire_projectile game_objects starfire_projectile
    aphelion_starfire_projectile.Damage0 = 12
    aphelion_starfire_projectile.Damage1 = 20
    aphelion_starfire_projectile.Damage2 = 15
    aphelion_starfire_projectile.Damage3 = 19
    aphelion_starfire_projectile.Damage4 = 17
    aphelion_starfire_projectile.Acceleration = 11
    aphelion_starfire_projectile.AccelerationUntil = 5f
    aphelion_starfire_projectile.InitialVelocity = 45
    aphelion_starfire_projectile.Lifespan = 7f
    aphelion_starfire_projectile.ProjectileDamageType = DamageType.Aggravated
    aphelion_starfire_projectile.Aggravated = AggravatedDamage(
      AggravatedInfo(DamageType.Direct, 0.25f, 250),
      Aura.None,
      2000,
      0f,
      true,
      List(TargetValidation(EffectTarget.Category.Aircraft, EffectTarget.Validation.Aircraft))
    )
    aphelion_starfire_projectile.registerAs = "rc-projectiles"
    aphelion_starfire_projectile.ExistsOnRemoteClients = true
    aphelion_starfire_projectile.RemoteClientData = (39577, 249) //starfire_projectile data
    aphelion_starfire_projectile.AutoLock = true
    aphelion_starfire_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(aphelion_starfire_projectile)
    aphelion_starfire_projectile.Modifiers = List(
      StarfireAggravated,
      StarfireAggravatedBurn
    )

    colossus_100mm_projectile.Name = "colossus_100mm_projectile"
    colossus_100mm_projectile.Damage0 = 58
    colossus_100mm_projectile.Damage1 = 330
    colossus_100mm_projectile.Damage2 = 300
    colossus_100mm_projectile.Damage3 = 165
    colossus_100mm_projectile.Damage4 = 190
    colossus_100mm_projectile.DamageAtEdge = 0.1f
    colossus_100mm_projectile.DamageRadius = 5f
    colossus_100mm_projectile.ProjectileDamageType = DamageType.Splash
    colossus_100mm_projectile.InitialVelocity = 100
    colossus_100mm_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(colossus_100mm_projectile)
    colossus_100mm_projectile.Modifiers = RadialDegrade

    colossus_burster_projectile.Name = "colossus_burster_projectile"
    // TODO for later, maybe : set_resource_parent colossus_burster_projectile game_objects burster_projectile
    colossus_burster_projectile.Damage0 = 18
    colossus_burster_projectile.Damage1 = 26
    colossus_burster_projectile.Damage2 = 18
    colossus_burster_projectile.Damage3 = 22
    colossus_burster_projectile.Damage4 = 20
    colossus_burster_projectile.DamageAtEdge = 0.1f
    colossus_burster_projectile.DamageRadius = 7f
    colossus_burster_projectile.ProjectileDamageType = DamageType.Direct
    colossus_burster_projectile.ProjectileDamageTypeSecondary = DamageType.Splash
    colossus_burster_projectile.InitialVelocity = 175
    colossus_burster_projectile.Lifespan = 2.5f
    ProjectileDefinition.CalculateDerivedFields(colossus_burster_projectile)
    colossus_burster_projectile.Modifiers = List(
      //FlakHit,
      FlakBurst,
      MaxDistanceCutoff
    )

    colossus_chaingun_projectile.Name = "colossus_chaingun_projectile"
    // TODO for later, maybe : set_resource_parent colossus_chaingun_projectile game_objects 35mmbullet_projectile
    colossus_chaingun_projectile.Damage0 = 15
    colossus_chaingun_projectile.Damage1 = 14
    colossus_chaingun_projectile.Damage2 = 15
    colossus_chaingun_projectile.Damage3 = 13
    colossus_chaingun_projectile.Damage4 = 11
    colossus_chaingun_projectile.ProjectileDamageType = DamageType.Direct
    colossus_chaingun_projectile.DegradeDelay = .100f
    colossus_chaingun_projectile.DegradeMultiplier = 0.44f
    colossus_chaingun_projectile.InitialVelocity = 500
    colossus_chaingun_projectile.Lifespan = .50f
    ProjectileDefinition.CalculateDerivedFields(colossus_chaingun_projectile)

    colossus_cluster_bomb_projectile.Name = "colossus_cluster_bomb_projectile"
    colossus_cluster_bomb_projectile.Damage0 = 40
    colossus_cluster_bomb_projectile.Damage1 = 88
    colossus_cluster_bomb_projectile.Damage2 = 100
    colossus_cluster_bomb_projectile.Damage3 = 83
    colossus_cluster_bomb_projectile.Damage4 = 88
    colossus_cluster_bomb_projectile.DamageAtEdge = 0.1f
    colossus_cluster_bomb_projectile.DamageRadius = 8f
    colossus_cluster_bomb_projectile.ProjectileDamageType = DamageType.Splash
    colossus_cluster_bomb_projectile.InitialVelocity = 75
    colossus_cluster_bomb_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(colossus_cluster_bomb_projectile)
    colossus_cluster_bomb_projectile.Modifiers = RadialDegrade

    colossus_tank_cannon_projectile.Name = "colossus_tank_cannon_projectile"
    // TODO for later, maybe : set_resource_parent colossus_tank_cannon_projectile game_objects 75mmbullet_projectile
    colossus_tank_cannon_projectile.Damage0 = 33
    colossus_tank_cannon_projectile.Damage1 = 90
    colossus_tank_cannon_projectile.Damage2 = 95
    colossus_tank_cannon_projectile.Damage3 = 71
    colossus_tank_cannon_projectile.Damage4 = 66
    colossus_tank_cannon_projectile.DamageAtEdge = 0.1f
    colossus_tank_cannon_projectile.DamageRadius = 2f
    colossus_tank_cannon_projectile.ProjectileDamageType = DamageType.Splash
    colossus_tank_cannon_projectile.InitialVelocity = 165
    colossus_tank_cannon_projectile.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(colossus_tank_cannon_projectile)
    colossus_tank_cannon_projectile.Modifiers = RadialDegrade

    peregrine_dual_machine_gun_projectile.Name = "peregrine_dual_machine_gun_projectile"
    // TODO for later, maybe : set_resource_parent peregrine_dual_machine_gun_projectile game_objects 35mmbullet_projectile
    peregrine_dual_machine_gun_projectile.Damage0 = 16
    peregrine_dual_machine_gun_projectile.Damage1 = 44
    peregrine_dual_machine_gun_projectile.Damage2 = 30
    peregrine_dual_machine_gun_projectile.Damage3 = 27
    peregrine_dual_machine_gun_projectile.Damage4 = 32
    peregrine_dual_machine_gun_projectile.ProjectileDamageType = DamageType.Direct
    peregrine_dual_machine_gun_projectile.DegradeDelay = .25f
    peregrine_dual_machine_gun_projectile.DegradeMultiplier = 0.65f
    peregrine_dual_machine_gun_projectile.InitialVelocity = 250
    peregrine_dual_machine_gun_projectile.Lifespan = 1.1f
    ProjectileDefinition.CalculateDerivedFields(peregrine_dual_machine_gun_projectile)

    peregrine_mechhammer_projectile.Name = "peregrine_mechhammer_projectile"
    peregrine_mechhammer_projectile.Damage0 = 5
    peregrine_mechhammer_projectile.Damage1 = 4
    peregrine_mechhammer_projectile.Damage2 = 4
    peregrine_mechhammer_projectile.Damage3 = 5
    peregrine_mechhammer_projectile.Damage4 = 3
    peregrine_mechhammer_projectile.ProjectileDamageType = DamageType.Direct
    peregrine_mechhammer_projectile.InitialVelocity = 500
    peregrine_mechhammer_projectile.Lifespan = 0.4f
    ProjectileDefinition.CalculateDerivedFields(peregrine_mechhammer_projectile)

    peregrine_particle_cannon_projectile.Name = "peregrine_particle_cannon_projectile"
    peregrine_particle_cannon_projectile.Damage0 = 70
    peregrine_particle_cannon_projectile.Damage1 = 525
    peregrine_particle_cannon_projectile.Damage2 = 350
    peregrine_particle_cannon_projectile.Damage3 = 318
    peregrine_particle_cannon_projectile.Damage4 = 310
    peregrine_particle_cannon_projectile.DamageAtEdge = 0.1f
    peregrine_particle_cannon_projectile.DamageRadius = 3f
    peregrine_particle_cannon_projectile.ProjectileDamageType = DamageType.Splash
    peregrine_particle_cannon_projectile.DamageProxy = 655 //peregrine_particle_cannon_radiation_cloud
    peregrine_particle_cannon_projectile.InitialVelocity = 500
    peregrine_particle_cannon_projectile.Lifespan = .6f
    ProjectileDefinition.CalculateDerivedFields(peregrine_particle_cannon_projectile)
    peregrine_particle_cannon_projectile.Modifiers = RadialDegrade

    peregrine_particle_cannon_radiation_cloud.Name = "peregrine_particle_cannon_radiation_cloud"
    peregrine_particle_cannon_radiation_cloud.Damage0 = 1
    peregrine_particle_cannon_radiation_cloud.DamageAtEdge = 1.0f
    peregrine_particle_cannon_radiation_cloud.DamageRadius = 3f
    peregrine_particle_cannon_radiation_cloud.radiation_cloud = true
    peregrine_particle_cannon_radiation_cloud.ProjectileDamageType = DamageType.Radiation
    peregrine_particle_cannon_radiation_cloud.Lifespan = 5.0f
    ProjectileDefinition.CalculateDerivedFields(peregrine_particle_cannon_radiation_cloud)
    peregrine_particle_cannon_radiation_cloud.registerAs = "rc-projectiles"
    peregrine_particle_cannon_radiation_cloud.ExistsOnRemoteClients = true
    peregrine_particle_cannon_radiation_cloud.Packet = radCloudConverter
    //peregrine_particle_cannon_radiation_cloud.Geometry = GeometryForm.representProjectileBySphere()
    peregrine_particle_cannon_radiation_cloud.Modifiers = List(
      MaxDistanceCutoff,
      ShieldAgainstRadiation
    )

    peregrine_rocket_pod_projectile.Name = "peregrine_rocket_pod_projectile"
    peregrine_rocket_pod_projectile.Damage0 = 30
    peregrine_rocket_pod_projectile.Damage1 = 50
    peregrine_rocket_pod_projectile.Damage2 = 50
    peregrine_rocket_pod_projectile.Damage3 = 45
    peregrine_rocket_pod_projectile.Damage4 = 40
    peregrine_rocket_pod_projectile.Acceleration = 10
    peregrine_rocket_pod_projectile.AccelerationUntil = 2f
    peregrine_rocket_pod_projectile.DamageAtEdge = 0.1f
    peregrine_rocket_pod_projectile.DamageRadius = 3f
    peregrine_rocket_pod_projectile.ProjectileDamageType = DamageType.Splash
    peregrine_rocket_pod_projectile.InitialVelocity = 200
    peregrine_rocket_pod_projectile.Lifespan = 1.85f
    ProjectileDefinition.CalculateDerivedFields(peregrine_rocket_pod_projectile)
    peregrine_rocket_pod_projectile.Modifiers = RadialDegrade

    peregrine_sparrow_projectile.Name = "peregrine_sparrow_projectile"
    // TODO for later, maybe : set_resource_parent peregrine_sparrow_projectile game_objects sparrow_projectile
    peregrine_sparrow_projectile.Damage0 = 20
    peregrine_sparrow_projectile.Damage1 = 40
    peregrine_sparrow_projectile.Damage2 = 30
    peregrine_sparrow_projectile.Damage3 = 30
    peregrine_sparrow_projectile.Damage4 = 31
    peregrine_sparrow_projectile.Acceleration = 12
    peregrine_sparrow_projectile.AccelerationUntil = 5f
    peregrine_sparrow_projectile.DamageAtEdge = 0.1f
    peregrine_sparrow_projectile.DamageRadius = 2f
    peregrine_sparrow_projectile.ProjectileDamageType = DamageType.Splash
    peregrine_sparrow_projectile.InitialVelocity = 45
    peregrine_sparrow_projectile.Lifespan = 7.5f
    peregrine_sparrow_projectile.registerAs = "rc-projectiles"
    peregrine_sparrow_projectile.ExistsOnRemoteClients = true
    peregrine_sparrow_projectile.RemoteClientData = (13107, 187) //sparrow_projectile data
    peregrine_sparrow_projectile.AutoLock = true
    peregrine_sparrow_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(peregrine_sparrow_projectile)
    peregrine_sparrow_projectile.Modifiers = RadialDegrade

    armor_siphon_projectile.Name = "armor_siphon_projectile"
    armor_siphon_projectile.Damage0 = 0
    armor_siphon_projectile.Damage1 = 20 //ground vehicles, siphon drain
    armor_siphon_projectile.Damage2 = 20 //aircraft, siphon drain
    armor_siphon_projectile.Damage3 = 0
    armor_siphon_projectile.Damage4 = 20 //bfr's, siphon drain
    armor_siphon_projectile.DamageToVehicleOnly = true
    armor_siphon_projectile.DamageToBattleframeOnly = true
    armor_siphon_projectile.DamageRadius = 35f
    armor_siphon_projectile.ProjectileDamageType = DamageType.Siphon
    ProjectileDefinition.CalculateDerivedFields(armor_siphon_projectile)
    armor_siphon_projectile.Modifiers = List(
      ArmorSiphonMaxDistanceCutoff,
      ArmorSiphonRepairHost
    )

    ntu_siphon_emp.Name = "ntu_siphon_emp"
    ntu_siphon_emp.Damage0 = 1
    ntu_siphon_emp.Damage1 = 1
    ntu_siphon_emp.DamageAtEdge = 0.1f
    ntu_siphon_emp.DamageRadius = 50f
    ntu_siphon_emp.ProjectileDamageType = DamageType.Splash
    ntu_siphon_emp.AdditionalEffect = true
    ntu_siphon_emp.SympatheticExplosion = true
    ntu_siphon_emp.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Player,
      EffectTarget.Validation.Player
    ) -> 1000
    ntu_siphon_emp.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.AMS
    ) -> 5000
    ntu_siphon_emp.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.MotionSensor
    ) -> 30000
    ntu_siphon_emp.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.Spitfire
    ) -> 30000
    ntu_siphon_emp.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Turret,
      EffectTarget.Validation.Turret
    ) -> 30000
    ntu_siphon_emp.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.VehicleNotAMS
    ) -> 10000
    ProjectileDefinition.CalculateDerivedFields(ntu_siphon_emp)
    ntu_siphon_emp.Modifiers = MaxDistanceCutoff
  }

  /**
    * Initialize `ToolDefinition` globals.
    */
  private def init_tools(): Unit = {
    init_infantry_tools()
    init_vehicle_tools()
  }

  /**
    * Initialize `ToolDefinition` globals.
    */
  private def init_infantry_tools(): Unit = {
    chainblade.Name = "chainblade"
    chainblade.Size = EquipmentSize.Melee
    chainblade.AmmoTypes += melee_ammo
    chainblade.ProjectileTypes += melee_ammo_projectile
    chainblade.ProjectileTypes += chainblade_projectile
    chainblade.FireModes += new InfiniteFireModeDefinition
    chainblade.FireModes.head.AmmoTypeIndices += 0
    chainblade.FireModes.head.AmmoSlotIndex = 0
    chainblade.FireModes.head.Magazine = 1
    chainblade.FireModes += new InfiniteFireModeDefinition
    chainblade.FireModes(1).AmmoTypeIndices += 0
    chainblade.FireModes(1).ProjectileTypeIndices += 1
    chainblade.FireModes(1).AmmoSlotIndex = 0
    chainblade.FireModes(1).Magazine = 1

    magcutter.Name = "magcutter"
    magcutter.Size = EquipmentSize.Melee
    magcutter.AmmoTypes += melee_ammo
    magcutter.ProjectileTypes += melee_ammo_projectile
    magcutter.ProjectileTypes += magcutter_projectile
    magcutter.FireModes += new InfiniteFireModeDefinition
    magcutter.FireModes.head.AmmoTypeIndices += 0
    magcutter.FireModes.head.AmmoSlotIndex = 0
    magcutter.FireModes.head.Magazine = 1
    magcutter.FireModes += new InfiniteFireModeDefinition
    magcutter.FireModes(1).AmmoTypeIndices += 0
    magcutter.FireModes(1).ProjectileTypeIndices += 1
    magcutter.FireModes(1).AmmoSlotIndex = 0
    magcutter.FireModes(1).Magazine = 1

    forceblade.Name = "forceblade"
    forceblade.Size = EquipmentSize.Melee
    forceblade.AmmoTypes += melee_ammo
    forceblade.ProjectileTypes += melee_ammo_projectile
    forceblade.ProjectileTypes += forceblade_projectile
    forceblade.FireModes += new InfiniteFireModeDefinition
    forceblade.FireModes.head.AmmoTypeIndices += 0
    forceblade.FireModes.head.AmmoSlotIndex = 0
    forceblade.FireModes.head.Magazine = 1
    forceblade.FireModes += new InfiniteFireModeDefinition
    forceblade.FireModes(1).AmmoTypeIndices += 0
    forceblade.FireModes(1).ProjectileTypeIndices += 1
    forceblade.FireModes(1).AmmoSlotIndex = 0
    forceblade.FireModes(1).Magazine = 1

    katana.Name = "katana"
    katana.Size = EquipmentSize.Melee
    katana.AmmoTypes += melee_ammo
    katana.ProjectileTypes += katana_projectile
    katana.ProjectileTypes += katana_projectileb
    katana.FireModes += new InfiniteFireModeDefinition
    katana.FireModes.head.AmmoTypeIndices += 0
    katana.FireModes.head.AmmoSlotIndex = 0
    katana.FireModes.head.Magazine = 1
    katana.FireModes += new InfiniteFireModeDefinition
    katana.FireModes(1).AmmoTypeIndices += 0
    katana.FireModes(1).ProjectileTypeIndices += 1
    katana.FireModes(1).AmmoSlotIndex = 0
    katana.FireModes(1).Magazine = 1

    frag_grenade.Name = "frag_grenade"
    frag_grenade.Size = EquipmentSize.Pistol
    frag_grenade.AmmoTypes += frag_grenade_ammo
    frag_grenade.ProjectileTypes += frag_grenade_projectile
    frag_grenade.FireModes += new FireModeDefinition
    frag_grenade.FireModes.head.AmmoTypeIndices += 0
    frag_grenade.FireModes.head.AmmoSlotIndex = 0
    frag_grenade.FireModes.head.Magazine = 3
    frag_grenade.FireModes += new FireModeDefinition
    frag_grenade.FireModes(1).AmmoTypeIndices += 0
    frag_grenade.FireModes(1).AmmoSlotIndex = 0
    frag_grenade.FireModes(1).Magazine = 3
    frag_grenade.Tile = InventoryTile.Tile22

    plasma_grenade.Name = "plasma_grenade"
    plasma_grenade.Size = EquipmentSize.Pistol
    plasma_grenade.AmmoTypes += plasma_grenade_ammo
    plasma_grenade.ProjectileTypes += plasma_grenade_projectile
    plasma_grenade.FireModes += new FireModeDefinition
    plasma_grenade.FireModes.head.AmmoTypeIndices += 0
    plasma_grenade.FireModes.head.AmmoSlotIndex = 0
    plasma_grenade.FireModes.head.Magazine = 3
    plasma_grenade.FireModes += new FireModeDefinition
    plasma_grenade.FireModes(1).AmmoTypeIndices += 0
    plasma_grenade.FireModes(1).AmmoSlotIndex = 0
    plasma_grenade.FireModes(1).Magazine = 3
    plasma_grenade.Tile = InventoryTile.Tile22

    jammer_grenade.Name = "jammer_grenade"
    jammer_grenade.Size = EquipmentSize.Pistol
    jammer_grenade.AmmoTypes += jammer_grenade_ammo
    jammer_grenade.ProjectileTypes += jammer_grenade_projectile
    jammer_grenade.FireModes += new FireModeDefinition
    jammer_grenade.FireModes.head.AmmoTypeIndices += 0
    jammer_grenade.FireModes.head.AmmoSlotIndex = 0
    jammer_grenade.FireModes.head.Magazine = 3
    jammer_grenade.FireModes += new FireModeDefinition
    jammer_grenade.FireModes(1).AmmoTypeIndices += 0
    jammer_grenade.FireModes(1).AmmoSlotIndex = 0
    jammer_grenade.FireModes(1).Magazine = 3
    jammer_grenade.Tile = InventoryTile.Tile22

    repeater.Name = "repeater"
    repeater.Size = EquipmentSize.Pistol
    repeater.AmmoTypes += bullet_9mm
    repeater.AmmoTypes += bullet_9mm_AP
    repeater.ProjectileTypes += bullet_9mm_projectile
    repeater.ProjectileTypes += bullet_9mm_AP_projectile
    repeater.FireModes += new FireModeDefinition
    repeater.FireModes.head.AmmoTypeIndices += 0
    repeater.FireModes.head.AmmoTypeIndices += 1
    repeater.FireModes.head.AmmoSlotIndex = 0
    repeater.FireModes.head.Magazine = 20
    repeater.FireModes.head.Add.Damage0 = 2
    repeater.FireModes.head.Add.Damage1 = -3
    repeater.FireModes.head.Add.Damage2 = -3
    repeater.FireModes.head.Add.Damage3 = -3
    repeater.FireModes.head.Add.Damage4 = -3
    repeater.Tile = InventoryTile.Tile33

    isp.Name = "isp"
    isp.Size = EquipmentSize.Pistol
    isp.AmmoTypes += shotgun_shell
    isp.AmmoTypes += shotgun_shell_AP
    isp.ProjectileTypes += shotgun_shell_projectile
    isp.ProjectileTypes += shotgun_shell_AP_projectile
    isp.FireModes += new PelletFireModeDefinition
    isp.FireModes.head.AmmoTypeIndices += 0
    isp.FireModes.head.AmmoTypeIndices += 1
    isp.FireModes.head.AmmoSlotIndex = 0
    isp.FireModes.head.Chamber = 6 //8 shells x 6 pellets = 36
    isp.FireModes.head.Magazine = 8
    isp.FireModes.head.Add.Damage0 = 1
    isp.FireModes.head.Add.Damage2 = 1
    isp.FireModes.head.Add.Damage3 = 1
    isp.Tile = InventoryTile.Tile33

    beamer.Name = "beamer"
    beamer.Size = EquipmentSize.Pistol
    beamer.AmmoTypes += energy_cell
    beamer.ProjectileTypes += energy_cell_projectile
    beamer.ProjectileTypes += enhanced_energy_cell_projectile
    beamer.FireModes += new FireModeDefinition
    beamer.FireModes.head.AmmoTypeIndices += 0
    beamer.FireModes.head.AmmoSlotIndex = 0
    beamer.FireModes.head.Magazine = 16
    beamer.FireModes.head.Add.Damage0 = 4
    beamer.FireModes.head.Add.Damage1 = -1
    beamer.FireModes.head.Add.Damage2 = -1
    beamer.FireModes.head.Add.Damage3 = -1
    beamer.FireModes.head.Add.Damage4 = -1
    beamer.FireModes += new FireModeDefinition
    beamer.FireModes(1).AmmoTypeIndices += 0
    beamer.FireModes(1).ProjectileTypeIndices += 1
    beamer.FireModes(1).AmmoSlotIndex = 0
    beamer.FireModes(1).Magazine = 16
    beamer.FireModes(1).Add.Damage0 = -3
    beamer.FireModes(1).Add.Damage1 = -3
    beamer.FireModes(1).Add.Damage2 = -3
    beamer.FireModes(1).Add.Damage3 = -3
    beamer.FireModes(1).Add.Damage4 = -3
    beamer.Tile = InventoryTile.Tile33

    ilc9.Name = "ilc9"
    ilc9.Size = EquipmentSize.Pistol
    ilc9.AmmoTypes += bullet_9mm
    ilc9.AmmoTypes += bullet_9mm_AP
    ilc9.ProjectileTypes += bullet_9mm_projectile
    ilc9.ProjectileTypes += bullet_9mm_AP_projectile
    ilc9.FireModes += new FireModeDefinition
    ilc9.FireModes.head.AmmoTypeIndices += 0
    ilc9.FireModes.head.AmmoTypeIndices += 1
    ilc9.FireModes.head.AmmoSlotIndex = 0
    ilc9.FireModes.head.Magazine = 30
    ilc9.FireModes.head.Add.Damage1 = -3
    ilc9.FireModes.head.Add.Damage4 = -3
    ilc9.Tile = InventoryTile.Tile33

    suppressor.Name = "suppressor"
    suppressor.Size = EquipmentSize.Rifle
    suppressor.AmmoTypes += bullet_9mm
    suppressor.AmmoTypes += bullet_9mm_AP
    suppressor.ProjectileTypes += bullet_9mm_projectile
    suppressor.ProjectileTypes += bullet_9mm_AP_projectile
    suppressor.FireModes += new FireModeDefinition
    suppressor.FireModes.head.AmmoTypeIndices += 0
    suppressor.FireModes.head.AmmoTypeIndices += 1
    suppressor.FireModes.head.AmmoSlotIndex = 0
    suppressor.FireModes.head.Magazine = 25
    suppressor.FireModes.head.Add.Damage0 = -1
    suppressor.FireModes.head.Add.Damage1 = -1
    suppressor.Tile = InventoryTile.Tile63

    punisher.Name = "punisher"
    punisher.Size = EquipmentSize.Rifle
    punisher.AmmoTypes += bullet_9mm
    punisher.AmmoTypes += bullet_9mm_AP
    punisher.AmmoTypes += rocket
    punisher.AmmoTypes += frag_cartridge
    punisher.AmmoTypes += jammer_cartridge
    punisher.AmmoTypes += plasma_cartridge
    punisher.ProjectileTypes += bullet_9mm_projectile
    punisher.ProjectileTypes += bullet_9mm_AP_projectile
    punisher.ProjectileTypes += rocket_projectile
    punisher.ProjectileTypes += frag_cartridge_projectile
    punisher.ProjectileTypes += jammer_cartridge_projectile
    punisher.ProjectileTypes += plasma_cartridge_projectile
    punisher.FireModes += new FireModeDefinition
    punisher.FireModes.head.AmmoTypeIndices += 0
    punisher.FireModes.head.AmmoTypeIndices += 1
    punisher.FireModes.head.AmmoSlotIndex = 0
    punisher.FireModes.head.Magazine = 30
    punisher.FireModes.head.Add.Damage0 = 1
    punisher.FireModes.head.Add.Damage3 = 1
    punisher.FireModes += new FireModeDefinition
    punisher.FireModes(1).AmmoTypeIndices += 2
    punisher.FireModes(1).AmmoTypeIndices += 3
    punisher.FireModes(1).AmmoTypeIndices += 4
    punisher.FireModes(1).AmmoTypeIndices += 5
    punisher.FireModes(1).AmmoSlotIndex = 1
    punisher.FireModes(1).Magazine = 1
    punisher.Tile = InventoryTile.Tile63

    flechette.Name = "flechette"
    flechette.Size = EquipmentSize.Rifle
    flechette.AmmoTypes += shotgun_shell
    flechette.AmmoTypes += shotgun_shell_AP
    flechette.ProjectileTypes += shotgun_shell_projectile
    flechette.ProjectileTypes += shotgun_shell_AP_projectile
    flechette.FireModes += new PelletFireModeDefinition
    flechette.FireModes.head.AmmoTypeIndices += 0
    flechette.FireModes.head.AmmoTypeIndices += 1
    flechette.FireModes.head.AmmoSlotIndex = 0
    flechette.FireModes.head.Magazine = 12
    flechette.FireModes.head.Chamber = 8 //12 shells * 8 pellets = 96
    flechette.Tile = InventoryTile.Tile63

    cycler.Name = "cycler"
    cycler.Size = EquipmentSize.Rifle
    cycler.AmmoTypes += bullet_9mm
    cycler.AmmoTypes += bullet_9mm_AP
    cycler.ProjectileTypes += bullet_9mm_projectile
    cycler.ProjectileTypes += bullet_9mm_AP_projectile
    cycler.FireModes += new FireModeDefinition
    cycler.FireModes.head.AmmoTypeIndices += 0
    cycler.FireModes.head.AmmoTypeIndices += 1
    cycler.FireModes.head.AmmoSlotIndex = 0
    cycler.FireModes.head.Magazine = 50
    cycler.Tile = InventoryTile.Tile63

    gauss.Name = "gauss"
    gauss.Size = EquipmentSize.Rifle
    gauss.AmmoTypes += bullet_9mm
    gauss.AmmoTypes += bullet_9mm_AP
    gauss.ProjectileTypes += bullet_9mm_projectile
    gauss.ProjectileTypes += bullet_9mm_AP_projectile
    gauss.FireModes += new FireModeDefinition
    gauss.FireModes.head.AmmoTypeIndices += 0
    gauss.FireModes.head.AmmoTypeIndices += 1
    gauss.FireModes.head.AmmoSlotIndex = 0
    gauss.FireModes.head.Magazine = 30
    gauss.FireModes.head.Add.Damage0 = 2
    gauss.FireModes.head.Add.Damage3 = 2
    gauss.Tile = InventoryTile.Tile63

    pulsar.Name = "pulsar"
    pulsar.Size = EquipmentSize.Rifle
    pulsar.AmmoTypes += energy_cell
    pulsar.ProjectileTypes += pulsar_projectile
    pulsar.ProjectileTypes += pulsar_ap_projectile
    pulsar.FireModes += new FireModeDefinition
    pulsar.FireModes.head.AmmoTypeIndices += 0
    pulsar.FireModes.head.AmmoSlotIndex = 0
    pulsar.FireModes.head.Magazine = 40
    pulsar.FireModes += new FireModeDefinition
    pulsar.FireModes(1).AmmoTypeIndices += 0
    pulsar.FireModes(1).ProjectileTypeIndices += 1
    pulsar.FireModes(1).AmmoSlotIndex = 0
    pulsar.FireModes(1).Magazine = 40
    pulsar.Tile = InventoryTile.Tile63

    anniversary_guna.Name = "anniversary_guna"
    anniversary_guna.Size = EquipmentSize.Pistol
    anniversary_guna.AmmoTypes += anniversary_ammo
    anniversary_guna.ProjectileTypes += anniversary_projectilea
    anniversary_guna.ProjectileTypes += anniversary_projectileb
    anniversary_guna.FireModes += new FireModeDefinition
    anniversary_guna.FireModes.head.AmmoTypeIndices += 0
    anniversary_guna.FireModes.head.AmmoSlotIndex = 0
    anniversary_guna.FireModes.head.Magazine = 6
    anniversary_guna.FireModes += new FireModeDefinition
    anniversary_guna.FireModes(1).AmmoTypeIndices += 0
    anniversary_guna.FireModes(1).ProjectileTypeIndices += 1
    anniversary_guna.FireModes(1).AmmoSlotIndex = 0
    anniversary_guna.FireModes(1).Magazine = 6
    anniversary_guna.Tile = InventoryTile.Tile33

    anniversary_gun.Name = "anniversary_gun"
    anniversary_gun.Size = EquipmentSize.Pistol
    anniversary_gun.AmmoTypes += anniversary_ammo
    anniversary_gun.ProjectileTypes += anniversary_projectilea
    anniversary_gun.ProjectileTypes += anniversary_projectileb
    anniversary_gun.FireModes += new FireModeDefinition
    anniversary_gun.FireModes.head.AmmoTypeIndices += 0
    anniversary_gun.FireModes.head.AmmoSlotIndex = 0
    anniversary_gun.FireModes.head.Magazine = 6
    anniversary_gun.FireModes += new FireModeDefinition
    anniversary_gun.FireModes(1).AmmoTypeIndices += 0
    anniversary_gun.FireModes(1).ProjectileTypeIndices += 1
    anniversary_gun.FireModes(1).AmmoSlotIndex = 0
    anniversary_gun.FireModes(1).Magazine = 6
    anniversary_gun.Tile = InventoryTile.Tile33

    anniversary_gunb.Name = "anniversary_gunb"
    anniversary_gunb.Size = EquipmentSize.Pistol
    anniversary_gunb.AmmoTypes += anniversary_ammo
    anniversary_gunb.ProjectileTypes += anniversary_projectilea
    anniversary_gunb.ProjectileTypes += anniversary_projectileb
    anniversary_gunb.FireModes += new FireModeDefinition
    anniversary_gunb.FireModes.head.AmmoTypeIndices += 0
    anniversary_gunb.FireModes.head.AmmoSlotIndex = 0
    anniversary_gunb.FireModes.head.Magazine = 6
    anniversary_gunb.FireModes += new FireModeDefinition
    anniversary_gunb.FireModes(1).AmmoTypeIndices += 0
    anniversary_gunb.FireModes(1).ProjectileTypeIndices += 1
    anniversary_gunb.FireModes(1).AmmoSlotIndex = 0
    anniversary_gunb.FireModes(1).Magazine = 6
    anniversary_gunb.Tile = InventoryTile.Tile33

    spiker.Name = "spiker"
    spiker.Size = EquipmentSize.Pistol
    spiker.AmmoTypes += ancient_ammo_combo
    spiker.ProjectileTypes += spiker_projectile
    spiker.FireModes += new ChargeFireModeDefinition(time = 1000, drainInterval = 500)
    spiker.FireModes.head.AmmoTypeIndices += 0
    spiker.FireModes.head.AmmoSlotIndex = 0
    spiker.FireModes.head.Magazine = 25
    spiker.Tile = InventoryTile.Tile33
    //TODO the spiker is weird

    mini_chaingun.Name = "mini_chaingun"
    mini_chaingun.Size = EquipmentSize.Rifle
    mini_chaingun.AmmoTypes += bullet_9mm
    mini_chaingun.AmmoTypes += bullet_9mm_AP
    mini_chaingun.ProjectileTypes += bullet_9mm_projectile
    mini_chaingun.ProjectileTypes += bullet_9mm_AP_projectile
    mini_chaingun.FireModes += new FireModeDefinition
    mini_chaingun.FireModes.head.AmmoTypeIndices += 0
    mini_chaingun.FireModes.head.AmmoTypeIndices += 1
    mini_chaingun.FireModes.head.AmmoSlotIndex = 0
    mini_chaingun.FireModes.head.Magazine = 100
    mini_chaingun.Tile = InventoryTile.Tile93

    r_shotgun.Name = "r_shotgun"
    r_shotgun.Size = EquipmentSize.Rifle
    r_shotgun.AmmoTypes += shotgun_shell
    r_shotgun.AmmoTypes += shotgun_shell_AP
    r_shotgun.ProjectileTypes += shotgun_shell_projectile
    r_shotgun.ProjectileTypes += shotgun_shell_AP_projectile
    r_shotgun.FireModes += new PelletFireModeDefinition
    r_shotgun.FireModes.head.AmmoTypeIndices += 0
    r_shotgun.FireModes.head.AmmoTypeIndices += 1
    r_shotgun.FireModes.head.AmmoSlotIndex = 0
    r_shotgun.FireModes.head.Magazine = 16
    r_shotgun.FireModes.head.Chamber = 8 //16 shells * 8 pellets = 128
    r_shotgun.FireModes.head.Add.Damage0 = 1
    r_shotgun.FireModes += new PelletFireModeDefinition
    r_shotgun.FireModes(1).AmmoTypeIndices += 0
    r_shotgun.FireModes(1).AmmoTypeIndices += 1
    r_shotgun.FireModes(1).AmmoSlotIndex = 0
    r_shotgun.FireModes(1).Magazine = 16
    r_shotgun.FireModes(1).Chamber = 8 //16 shells * 8 pellets = 128
    r_shotgun.FireModes(1).Add.Damage0 = -3
    r_shotgun.Tile = InventoryTile.Tile93

    lasher.Name = "lasher"
    lasher.Size = EquipmentSize.Rifle
    lasher.AmmoTypes += energy_cell
    lasher.ProjectileTypes += lasher_projectile
    lasher.ProjectileTypes += lasher_projectile_ap
    lasher.FireModes += new FireModeDefinition
    lasher.FireModes.head.AmmoTypeIndices += 0
    lasher.FireModes.head.AmmoSlotIndex = 0
    lasher.FireModes.head.Magazine = 35
    lasher.FireModes += new FireModeDefinition
    lasher.FireModes(1).AmmoTypeIndices += 0
    lasher.FireModes(1).ProjectileTypeIndices += 1
    lasher.FireModes(1).AmmoSlotIndex = 0
    lasher.FireModes(1).Magazine = 35
    lasher.Tile = InventoryTile.Tile93

    maelstrom.Name = "maelstrom"
    maelstrom.Size = EquipmentSize.Rifle
    maelstrom.AmmoTypes += maelstrom_ammo
    maelstrom.ProjectileTypes += maelstrom_stream_projectile
    maelstrom.ProjectileTypes += maelstrom_grenade_projectile_contact
    maelstrom.ProjectileTypes += maelstrom_grenade_projectile
    maelstrom.FireModes += new FireModeDefinition
    maelstrom.FireModes.head.AmmoTypeIndices += 0
    maelstrom.FireModes.head.AmmoSlotIndex = 0
    maelstrom.FireModes.head.Magazine = 150
    maelstrom.FireModes += new FireModeDefinition
    maelstrom.FireModes(1).AmmoTypeIndices += 0
    maelstrom.FireModes(1).ProjectileTypeIndices += 1
    maelstrom.FireModes(1).AmmoSlotIndex = 0
    maelstrom.FireModes(1).Magazine = 150
    maelstrom.FireModes(1).RoundsPerShot = 10
    maelstrom.FireModes += new FireModeDefinition
    maelstrom.FireModes(2).AmmoTypeIndices += 0
    maelstrom.FireModes(2).ProjectileTypeIndices += 2
    maelstrom.FireModes(2).AmmoSlotIndex = 0
    maelstrom.FireModes(2).Magazine = 150
    maelstrom.FireModes(2).RoundsPerShot = 10
    maelstrom.Tile = InventoryTile.Tile93
    //TODO the maelstrom is weird

    phoenix.Name = "phoenix"
    phoenix.Size = EquipmentSize.Rifle
    phoenix.AmmoTypes += phoenix_missile
    phoenix.ProjectileTypes += phoenix_missile_projectile
    phoenix.ProjectileTypes += phoenix_missile_guided_projectile
    phoenix.FireModes += new FireModeDefinition
    phoenix.FireModes.head.AmmoTypeIndices += 0
    phoenix.FireModes.head.AmmoSlotIndex = 0
    phoenix.FireModes.head.Magazine = 3
    phoenix.FireModes += new FireModeDefinition
    phoenix.FireModes(1).AmmoTypeIndices += 0
    phoenix.FireModes(1).ProjectileTypeIndices += 1
    phoenix.FireModes(1).AmmoSlotIndex = 0
    phoenix.FireModes(1).Magazine = 3
    phoenix.Tile = InventoryTile.Tile93

    striker.Name = "striker"
    striker.Size = EquipmentSize.Rifle
    striker.AmmoTypes += striker_missile_ammo
    striker.ProjectileTypes += striker_missile_targeting_projectile
    striker.ProjectileTypes += striker_missile_projectile
    striker.FireModes += new FireModeDefinition
    striker.FireModes.head.AmmoTypeIndices += 0
    striker.FireModes.head.AmmoSlotIndex = 0
    striker.FireModes.head.Magazine = 5
    striker.FireModes += new FireModeDefinition
    striker.FireModes(1).AmmoTypeIndices += 0
    striker.FireModes(1).ProjectileTypeIndices += 1
    striker.FireModes(1).AmmoSlotIndex = 0
    striker.FireModes(1).Magazine = 5
    striker.Tile = InventoryTile.Tile93

    hunterseeker.Name = "hunterseeker"
    hunterseeker.Size = EquipmentSize.Rifle
    hunterseeker.AmmoTypes += hunter_seeker_missile
    hunterseeker.ProjectileTypes += hunter_seeker_missile_projectile
    hunterseeker.ProjectileTypes += hunter_seeker_missile_dumbfire
    hunterseeker.FireModes += new FireModeDefinition
    hunterseeker.FireModes.head.AmmoTypeIndices += 0
    hunterseeker.FireModes.head.AmmoSlotIndex = 0
    hunterseeker.FireModes.head.Magazine = 1
    hunterseeker.FireModes += new FireModeDefinition
    hunterseeker.FireModes(1).AmmoTypeIndices += 0
    hunterseeker.FireModes(1).ProjectileTypeIndices += 1
    hunterseeker.FireModes(1).AmmoSlotIndex = 0
    hunterseeker.FireModes(1).Magazine = 1
    hunterseeker.Tile = InventoryTile.Tile93

    lancer.Name = "lancer"
    lancer.Size = EquipmentSize.Rifle
    lancer.AmmoTypes += lancer_cartridge
    lancer.ProjectileTypes += lancer_projectile
    lancer.FireModes += new FireModeDefinition
    lancer.FireModes.head.AmmoTypeIndices += 0
    lancer.FireModes.head.AmmoSlotIndex = 0
    lancer.FireModes.head.Magazine = 6
    lancer.Tile = InventoryTile.Tile93

    rocklet.Name = "rocklet"
    rocklet.Size = EquipmentSize.Rifle
    rocklet.AmmoTypes += rocket
    rocklet.AmmoTypes += frag_cartridge
    rocklet.ProjectileTypes += rocket_projectile
    rocklet.ProjectileTypes += rocklet_flak_projectile
    rocklet.FireModes += new FireModeDefinition
    rocklet.FireModes.head.AmmoTypeIndices += 0
    rocklet.FireModes.head.AmmoTypeIndices += 1
    rocklet.FireModes.head.AmmoSlotIndex = 0
    rocklet.FireModes.head.Magazine = 6
    rocklet.FireModes += new FireModeDefinition
    rocklet.FireModes(1).AmmoTypeIndices += 0
    rocklet.FireModes(1).AmmoTypeIndices += 1
    rocklet.FireModes(1).AmmoSlotIndex = 0
    rocklet.FireModes(1).Magazine = 6
    rocklet.Tile = InventoryTile.Tile63

    thumper.Name = "thumper"
    thumper.Size = EquipmentSize.Rifle
    thumper.AmmoTypes += frag_cartridge
    thumper.AmmoTypes += plasma_cartridge
    thumper.AmmoTypes += jammer_cartridge
    thumper.ProjectileTypes += frag_cartridge_projectile_b
    thumper.ProjectileTypes += plasma_cartridge_projectile_b
    thumper.ProjectileTypes += jammer_cartridge_projectile_b
    thumper.FireModes += new FireModeDefinition
    thumper.FireModes.head.AmmoTypeIndices += 0
    thumper.FireModes.head.AmmoTypeIndices += 1
    thumper.FireModes.head.AmmoTypeIndices += 2
    thumper.FireModes.head.AmmoSlotIndex = 0
    thumper.FireModes.head.Magazine = 6
    thumper.FireModes += new FireModeDefinition
    thumper.FireModes(1).AmmoTypeIndices += 0
    thumper.FireModes(1).AmmoTypeIndices += 1
    thumper.FireModes(1).AmmoTypeIndices += 2
    thumper.FireModes(1).AmmoSlotIndex = 0
    thumper.FireModes(1).Magazine = 6
    thumper.Tile = InventoryTile.Tile63

    radiator.Name = "radiator"
    radiator.Size = EquipmentSize.Rifle
    radiator.AmmoTypes += ancient_ammo_combo
    radiator.ProjectileTypes += radiator_grenade_projectile
    radiator.ProjectileTypes += radiator_sticky_projectile
    radiator.FireModes += new FireModeDefinition
    radiator.FireModes.head.AmmoTypeIndices += 0
    radiator.FireModes.head.AmmoSlotIndex = 0
    radiator.FireModes.head.Magazine = 25
    radiator.FireModes += new FireModeDefinition
    radiator.FireModes(1).AmmoTypeIndices += 0
    radiator.FireModes(1).ProjectileTypeIndices += 1
    radiator.FireModes(1).AmmoSlotIndex = 0
    radiator.FireModes(1).Magazine = 25
    radiator.Tile = InventoryTile.Tile63

    heavy_sniper.Name = "heavy_sniper"
    heavy_sniper.Size = EquipmentSize.Rifle
    heavy_sniper.AmmoTypes += bolt
    heavy_sniper.ProjectileTypes += heavy_sniper_projectile
    heavy_sniper.FireModes += new FireModeDefinition
    heavy_sniper.FireModes.head.AmmoTypeIndices += 0
    heavy_sniper.FireModes.head.AmmoSlotIndex = 0
    heavy_sniper.FireModes.head.Magazine = 10
    heavy_sniper.Tile = InventoryTile.Tile93

    bolt_driver.Name = "bolt_driver"
    bolt_driver.Size = EquipmentSize.Rifle
    bolt_driver.AmmoTypes += bolt
    bolt_driver.ProjectileTypes += bolt_projectile
    bolt_driver.FireModes += new FireModeDefinition
    bolt_driver.FireModes.head.AmmoTypeIndices += 0
    bolt_driver.FireModes.head.AmmoSlotIndex = 0
    bolt_driver.FireModes.head.Magazine = 1
    bolt_driver.Tile = InventoryTile.Tile93

    oicw.Name = "oicw"
    oicw.Size = EquipmentSize.Rifle
    oicw.AmmoTypes += oicw_ammo
    oicw.ProjectileTypes += oicw_projectile
    oicw.FireModes += new FireModeDefinition
    oicw.FireModes.head.AmmoTypeIndices += 0
    oicw.FireModes.head.AmmoSlotIndex = 0
    oicw.FireModes.head.Magazine = 1
    oicw.FireModes += new FireModeDefinition
    oicw.FireModes(1).AmmoTypeIndices += 0
    oicw.FireModes(1).AmmoSlotIndex = 0
    oicw.FireModes(1).Magazine = 1
    oicw.Tile = InventoryTile.Tile93

    flamethrower.Name = "flamethrower"
    flamethrower.Size = EquipmentSize.Rifle
    flamethrower.AmmoTypes += flamethrower_ammo
    flamethrower.ProjectileTypes += flamethrower_projectile
    flamethrower.ProjectileTypes += flamethrower_fireball
    flamethrower.FireModes += new FireModeDefinition
    flamethrower.FireModes.head.AmmoTypeIndices += 0
    flamethrower.FireModes.head.AmmoSlotIndex = 0
    flamethrower.FireModes.head.Magazine = 100
    flamethrower.FireModes += new FireModeDefinition
    flamethrower.FireModes(1).AmmoTypeIndices += 0
    flamethrower.FireModes(1).ProjectileTypeIndices += 1
    flamethrower.FireModes(1).AmmoSlotIndex = 0
    flamethrower.FireModes(1).Magazine = 100
    flamethrower.FireModes(1).RoundsPerShot = 50
    flamethrower.Tile = InventoryTile.Tile93

    winchester.Name = "winchester"
    winchester.Size = EquipmentSize.Rifle
    winchester.AmmoTypes += winchester_ammo
    winchester.ProjectileTypes += winchester_projectile
    winchester.FireModes += new FireModeDefinition
    winchester.FireModes.head.AmmoTypeIndices += 0
    winchester.FireModes.head.AmmoSlotIndex = 0
    winchester.FireModes.head.Magazine = 1
    winchester.Tile = InventoryTile.Tile93

    pellet_gun.Name = "pellet_gun"
    pellet_gun.Size = EquipmentSize.Rifle
    pellet_gun.AmmoTypes += pellet_gun_ammo
    pellet_gun.ProjectileTypes += pellet_gun_projectile
    pellet_gun.FireModes += new PelletFireModeDefinition
    pellet_gun.FireModes.head.AmmoTypeIndices += 0
    pellet_gun.FireModes.head.AmmoSlotIndex = 0
    pellet_gun.FireModes.head.Magazine = 1 //what is this?
    pellet_gun.FireModes.head.Chamber = 8  //1 shell * 8 pellets = 8
    pellet_gun.Tile = InventoryTile.Tile63

    six_shooter.Name = "six_shooter"
    six_shooter.Size = EquipmentSize.Pistol
    six_shooter.AmmoTypes += six_shooter_ammo
    six_shooter.ProjectileTypes += six_shooter_projectile
    six_shooter.FireModes += new FireModeDefinition
    six_shooter.FireModes.head.AmmoTypeIndices += 0
    six_shooter.FireModes.head.AmmoSlotIndex = 0
    six_shooter.FireModes.head.Magazine = 6
    six_shooter.Tile = InventoryTile.Tile33

    dynomite.Name = "dynomite"
    dynomite.Size = EquipmentSize.Pistol
    dynomite.AmmoTypes += frag_grenade_ammo
    dynomite.ProjectileTypes += dynomite_projectile
    dynomite.FireModes += new FireModeDefinition
    dynomite.FireModes.head.AmmoTypeIndices += 0
    dynomite.FireModes.head.AmmoSlotIndex = 0
    dynomite.FireModes.head.Magazine = 1
    dynomite.Tile = InventoryTile.Tile22

    trhev_dualcycler.Name = "trhev_dualcycler"
    trhev_dualcycler.Size = EquipmentSize.Max
    trhev_dualcycler.AmmoTypes += dualcycler_ammo
    trhev_dualcycler.ProjectileTypes += dualcycler_projectile
    trhev_dualcycler.FireModes += new FireModeDefinition
    trhev_dualcycler.FireModes.head.AmmoTypeIndices += 0
    trhev_dualcycler.FireModes.head.AmmoSlotIndex = 0
    trhev_dualcycler.FireModes.head.Magazine = 200
    trhev_dualcycler.FireModes += new FireModeDefinition //anchored
    trhev_dualcycler.FireModes(1).AmmoTypeIndices += 0
    trhev_dualcycler.FireModes(1).AmmoSlotIndex = 0
    trhev_dualcycler.FireModes(1).Magazine = 200
    trhev_dualcycler.FireModes += new FireModeDefinition //overdrive?
    trhev_dualcycler.FireModes(2).AmmoTypeIndices += 0
    trhev_dualcycler.FireModes(2).AmmoSlotIndex = 0
    trhev_dualcycler.FireModes(2).Magazine = 200

    trhev_pounder.Name = "trhev_pounder"
    trhev_pounder.Size = EquipmentSize.Max
    trhev_pounder.AmmoTypes += pounder_ammo
    trhev_pounder.ProjectileTypes += pounder_projectile
    trhev_pounder.ProjectileTypes += pounder_projectile_enh
    trhev_pounder.FireModes += new FireModeDefinition
    trhev_pounder.FireModes.head.AmmoTypeIndices += 0 //explode on contact
    trhev_pounder.FireModes.head.AmmoSlotIndex = 0
    trhev_pounder.FireModes.head.Magazine = 30
    trhev_pounder.FireModes += new FireModeDefinition //explode on contact, anchored
    trhev_pounder.FireModes(1).AmmoTypeIndices += 0
    trhev_pounder.FireModes(1).AmmoSlotIndex = 0
    trhev_pounder.FireModes(1).Magazine = 30
    trhev_pounder.FireModes += new FireModeDefinition //explode on contact, overdrive?
    trhev_pounder.FireModes(2).AmmoTypeIndices += 0
    trhev_pounder.FireModes(2).AmmoSlotIndex = 0
    trhev_pounder.FireModes(2).Magazine = 30
    trhev_pounder.FireModes += new FireModeDefinition //3-second fuse
    trhev_pounder.FireModes(3).AmmoTypeIndices += 0
    trhev_pounder.FireModes(3).ProjectileTypeIndices += 1
    trhev_pounder.FireModes(3).AmmoSlotIndex = 0
    trhev_pounder.FireModes(3).Magazine = 30
    trhev_pounder.FireModes += new FireModeDefinition //3-second fuse, anchored
    trhev_pounder.FireModes(4).AmmoTypeIndices += 0
    trhev_pounder.FireModes(4).ProjectileTypeIndices += 1
    trhev_pounder.FireModes(4).AmmoSlotIndex = 0
    trhev_pounder.FireModes(4).Magazine = 30
    trhev_pounder.FireModes += new FireModeDefinition //3-second fuse, overdrive?
    trhev_pounder.FireModes(5).AmmoTypeIndices += 0
    trhev_pounder.FireModes(5).ProjectileTypeIndices += 1
    trhev_pounder.FireModes(5).AmmoSlotIndex = 0
    trhev_pounder.FireModes(5).Magazine = 30

    trhev_burster.Name = "trhev_burster"
    trhev_burster.Size = EquipmentSize.Max
    trhev_burster.AmmoTypes += burster_ammo
    trhev_burster.ProjectileTypes += burster_projectile
    trhev_burster.FireModes += new FireModeDefinition
    trhev_burster.FireModes.head.AmmoTypeIndices += 0
    trhev_burster.FireModes.head.AmmoSlotIndex = 0
    trhev_burster.FireModes.head.Magazine = 40

    nchev_scattercannon.Name = "nchev_scattercannon"
    nchev_scattercannon.Size = EquipmentSize.Max
    nchev_scattercannon.AmmoTypes += scattercannon_ammo
    nchev_scattercannon.ProjectileTypes += scattercannon_projectile
    nchev_scattercannon.FireModes += new PelletFireModeDefinition
    nchev_scattercannon.FireModes.head.AmmoTypeIndices += 0
    nchev_scattercannon.FireModes.head.AmmoSlotIndex = 0
    nchev_scattercannon.FireModes.head.Magazine = 40
    nchev_scattercannon.FireModes.head.Chamber = 10 //40 shells * 10 pellets = 400
    nchev_scattercannon.FireModes += new PelletFireModeDefinition
    nchev_scattercannon.FireModes(1).AmmoTypeIndices += 0
    nchev_scattercannon.FireModes(1).AmmoSlotIndex = 0
    nchev_scattercannon.FireModes(1).Magazine = 40
    nchev_scattercannon.FireModes(1).Chamber = 10 //40 shells * 10 pellets = 400
    nchev_scattercannon.FireModes += new PelletFireModeDefinition
    nchev_scattercannon.FireModes(2).AmmoTypeIndices += 0
    nchev_scattercannon.FireModes(2).AmmoSlotIndex = 0
    nchev_scattercannon.FireModes(2).Magazine = 40
    nchev_scattercannon.FireModes(2).Chamber = 10 //40 shells * 10 pellets = 400

    nchev_falcon.Name = "nchev_falcon"
    nchev_falcon.Size = EquipmentSize.Max
    nchev_falcon.AmmoTypes += falcon_ammo
    nchev_falcon.ProjectileTypes += falcon_projectile
    nchev_falcon.FireModes += new FireModeDefinition
    nchev_falcon.FireModes.head.AmmoTypeIndices += 0
    nchev_falcon.FireModes.head.AmmoSlotIndex = 0
    nchev_falcon.FireModes.head.Magazine = 20

    nchev_sparrow.Name = "nchev_sparrow"
    nchev_sparrow.Size = EquipmentSize.Max
    nchev_sparrow.AmmoTypes += sparrow_ammo
    nchev_sparrow.ProjectileTypes += sparrow_projectile
    nchev_sparrow.FireModes += new FireModeDefinition
    nchev_sparrow.FireModes.head.AmmoTypeIndices += 0
    nchev_sparrow.FireModes.head.AmmoSlotIndex = 0
    nchev_sparrow.FireModes.head.Magazine = 12

    vshev_quasar.Name = "vshev_quasar"
    vshev_quasar.Size = EquipmentSize.Max
    vshev_quasar.AmmoTypes += quasar_ammo
    vshev_quasar.ProjectileTypes += quasar_projectile
    vshev_quasar.ProjectileTypes += enhanced_quasar_projectile
    vshev_quasar.FireModes += new FireModeDefinition
    vshev_quasar.FireModes.head.AmmoTypeIndices += 0
    vshev_quasar.FireModes.head.AmmoSlotIndex = 0
    vshev_quasar.FireModes.head.Magazine = 120
    vshev_quasar.FireModes += new FireModeDefinition
    vshev_quasar.FireModes(1).AmmoTypeIndices += 0
    vshev_quasar.FireModes(1).ProjectileTypeIndices += 1
    vshev_quasar.FireModes(1).AmmoSlotIndex = 0
    vshev_quasar.FireModes(1).Magazine = 120

    vshev_comet.Name = "vshev_comet"
    vshev_comet.Size = EquipmentSize.Max
    vshev_comet.AmmoTypes += comet_ammo
    vshev_comet.ProjectileTypes += comet_projectile
    vshev_comet.FireModes += new FireModeDefinition
    vshev_comet.FireModes.head.AmmoTypeIndices += 0
    vshev_comet.FireModes.head.AmmoSlotIndex = 0
    vshev_comet.FireModes.head.Magazine = 10

    vshev_starfire.Name = "vshev_starfire"
    vshev_starfire.Size = EquipmentSize.Max
    vshev_starfire.AmmoTypes += starfire_ammo
    vshev_starfire.ProjectileTypes += starfire_projectile
    vshev_starfire.FireModes += new FireModeDefinition
    vshev_starfire.FireModes.head.AmmoTypeIndices += 0
    vshev_starfire.FireModes.head.AmmoSlotIndex = 0
    vshev_starfire.FireModes.head.Magazine = 8

    medicalapplicator.Name = "medicalapplicator"
    medicalapplicator.Size = EquipmentSize.Pistol
    medicalapplicator.AmmoTypes += health_canister
    medicalapplicator.ProjectileTypes += no_projectile
    medicalapplicator.FireModes += new FireModeDefinition
    medicalapplicator.FireModes.head.AmmoTypeIndices += 0
    medicalapplicator.FireModes.head.AmmoSlotIndex = 0
    medicalapplicator.FireModes.head.Magazine = 100
    medicalapplicator.FireModes += new FireModeDefinition
    medicalapplicator.FireModes(1).AmmoTypeIndices += 0
    medicalapplicator.FireModes(1).AmmoSlotIndex = 0
    medicalapplicator.FireModes(1).Magazine = 100
    medicalapplicator.Tile = InventoryTile.Tile33

    nano_dispenser.Name = "nano_dispenser"
    nano_dispenser.Size = EquipmentSize.Rifle
    nano_dispenser.AmmoTypes += armor_canister
    nano_dispenser.AmmoTypes += upgrade_canister
    nano_dispenser.ProjectileTypes += no_projectile
    nano_dispenser.FireModes += new FireModeDefinition
    nano_dispenser.FireModes.head.AmmoTypeIndices += 0
    nano_dispenser.FireModes.head.AmmoTypeIndices += 1
    nano_dispenser.FireModes.head.ProjectileTypeIndices += 0 //armor_canister
    nano_dispenser.FireModes.head.ProjectileTypeIndices += 0 //upgrade_canister
    nano_dispenser.FireModes.head.AmmoSlotIndex = 0
    nano_dispenser.FireModes.head.Magazine = 100
    nano_dispenser.FireModes.head.CustomMagazine = Ammo.upgrade_canister -> 1
    nano_dispenser.FireModes.head.Add.Damage0 = 0
    nano_dispenser.FireModes.head.Add.Damage1 = 20
    nano_dispenser.FireModes.head.Add.Damage2 = 0
    nano_dispenser.FireModes.head.Add.Damage3 = 0
    nano_dispenser.FireModes.head.Add.Damage4 = 20
    nano_dispenser.AddRepairMultiplier(level = 3, value = 2.0f)
    nano_dispenser.AddRepairMultiplier(level = 2, value = 1.5f)
    nano_dispenser.AddRepairMultiplier(level = 1, value = 1.0f)
    nano_dispenser.Tile = InventoryTile.Tile63

    bank.Name = "bank"
    bank.Size = EquipmentSize.Pistol
    bank.AmmoTypes += armor_canister
    bank.ProjectileTypes += no_projectile
    bank.FireModes += new FireModeDefinition
    bank.FireModes.head.AmmoTypeIndices += 0
    bank.FireModes.head.AmmoSlotIndex = 0
    bank.FireModes.head.Magazine = 100
    bank.FireModes += new FireModeDefinition
    bank.FireModes(1).AmmoTypeIndices += 0
    bank.FireModes(1).AmmoSlotIndex = 0
    bank.FireModes(1).Magazine = 100
    bank.AddRepairMultiplier(level = 3, value = 1.5f)
    bank.AddRepairMultiplier(level = 2, value = 1.2f)
    bank.AddRepairMultiplier(level = 1, value = 1.0f)
    bank.Tile = InventoryTile.Tile33

    remote_electronics_kit.Name = "remote_electronics_kit"
    remote_electronics_kit.Packet = new REKConverter
    remote_electronics_kit.Tile = InventoryTile.Tile33

    boomer_trigger.Name = "boomer_trigger"
    boomer_trigger.Packet = new BoomerTriggerConverter
    boomer_trigger.Tile = InventoryTile.Tile22

    trek.Name = "trek"
    trek.Size = EquipmentSize.Pistol
    trek.AmmoTypes += trek_ammo
    trek.ProjectileTypes += trek_projectile
    trek.FireModes += new FireModeDefinition
    trek.FireModes.head.AmmoTypeIndices += 0
    trek.FireModes.head.AmmoSlotIndex = 0
    trek.FireModes.head.Magazine = 4
    trek.FireModes += new InfiniteFireModeDefinition
    trek.FireModes(1).AmmoTypeIndices += 0
    trek.FireModes(1).AmmoSlotIndex = 0
    trek.FireModes(1).Magazine = 1
    trek.Tile = InventoryTile.Tile33

    flail_targeting_laser.Name = "flail_targeting_laser"
    flail_targeting_laser.Packet = new CommandDetonaterConverter
    flail_targeting_laser.Tile = InventoryTile.Tile33

    command_detonater.Name = "command_detonater"
    command_detonater.Packet = new CommandDetonaterConverter
    command_detonater.Tile = InventoryTile.Tile33

    ace.Name = "ace"
    ace.Size = EquipmentSize.Pistol
    ace.Modes += new ConstructionFireMode {
      Item(DeployedItem.boomer, Set(Certification.CombatEngineering))
    }
    ace.Modes += new ConstructionFireMode {
      Item(DeployedItem.he_mine, Set(Certification.CombatEngineering))
      Item(DeployedItem.jammer_mine, Set(Certification.AssaultEngineering))
    }
    ace.Modes += new ConstructionFireMode {
      Item(DeployedItem.spitfire_turret, Set(Certification.CombatEngineering))
      Item(DeployedItem.spitfire_cloaked, Set(Certification.FortificationEngineering))
      Item(DeployedItem.spitfire_aa, Set(Certification.FortificationEngineering))
    }
    ace.Modes += new ConstructionFireMode {
      Item(DeployedItem.motionalarmsensor, Set(Certification.CombatEngineering))
      Item(DeployedItem.sensor_shield, Set(Certification.AdvancedHacking, Certification.CombatEngineering))
    }
    ace.Tile = InventoryTile.Tile33

    advanced_ace.Name = "advanced_ace"
    advanced_ace.Size = EquipmentSize.Rifle
    advanced_ace.Modes += new ConstructionFireMode {
      Item(DeployedItem.portable_manned_turret, Set(Certification.AssaultEngineering))
    }
    advanced_ace.Modes += new ConstructionFireMode {
      Item(DeployedItem.tank_traps, Set(Certification.FortificationEngineering))
    }
    advanced_ace.Modes += new ConstructionFireMode {
      Item(DeployedItem.deployable_shield_generator, Set(Certification.AssaultEngineering))
    }
    advanced_ace.Tile = InventoryTile.Tile93

    router_telepad.Name = "router_telepad"
    router_telepad.Size = EquipmentSize.Pistol
    router_telepad.Modes += new ConstructionFireMode
    router_telepad.Modes.head.Item(DeployedItem.router_telepad_deployable, Set(Certification.GroundSupport))
    router_telepad.Tile = InventoryTile.Tile33
    router_telepad.Packet = new TelepadConverter
  }

  /**
    * Initialize `ToolDefinition` globals.
    */
  private def init_vehicle_tools(): Unit = {
    fury_weapon_systema.Name = "fury_weapon_systema"
    fury_weapon_systema.Size = EquipmentSize.VehicleWeapon
    fury_weapon_systema.AmmoTypes += hellfire_ammo
    fury_weapon_systema.ProjectileTypes += hellfire_projectile
    fury_weapon_systema.FireModes += new FireModeDefinition
    fury_weapon_systema.FireModes.head.AmmoTypeIndices += 0
    fury_weapon_systema.FireModes.head.AmmoSlotIndex = 0
    fury_weapon_systema.FireModes.head.Magazine = 2

    quadassault_weapon_system.Name = "quadassault_weapon_system"
    quadassault_weapon_system.Size = EquipmentSize.VehicleWeapon
    quadassault_weapon_system.AmmoTypes += bullet_12mm
    quadassault_weapon_system.ProjectileTypes += bullet_12mm_projectile
    quadassault_weapon_system.FireModes += new FireModeDefinition
    quadassault_weapon_system.FireModes.head.AmmoTypeIndices += 0
    quadassault_weapon_system.FireModes.head.AmmoSlotIndex = 0
    quadassault_weapon_system.FireModes.head.Magazine = 150

    scythe.Name = "scythe"
    scythe.Size = EquipmentSize.VehicleWeapon
    scythe.AmmoTypes += ancient_ammo_vehicle
    scythe.AmmoTypes += ancient_ammo_vehicle
    scythe.ProjectileTypes += scythe_projectile
    scythe.FireModes += new FireModeDefinition
    scythe.FireModes.head.AmmoTypeIndices += 0
    scythe.FireModes.head.AmmoSlotIndex = 0
    scythe.FireModes.head.Magazine = 250
    scythe.FireModes += new FireModeDefinition
    scythe.FireModes(1).AmmoTypeIndices += 0
    scythe.FireModes(1).ProjectileTypeIndices += 0
    scythe.FireModes(1).AmmoSlotIndex =
      1 //note: the scythe has two magazines using a single pool; however, it can not ammo-switch or mode-switch
    scythe.FireModes(1).Magazine = 250

    chaingun_p.Name = "chaingun_p"
    chaingun_p.Size = EquipmentSize.VehicleWeapon
    chaingun_p.AmmoTypes += bullet_12mm
    chaingun_p.ProjectileTypes += bullet_12mm_projectile
    chaingun_p.FireModes += new FireModeDefinition
    chaingun_p.FireModes.head.AmmoTypeIndices += 0
    chaingun_p.FireModes.head.AmmoSlotIndex = 0
    chaingun_p.FireModes.head.Magazine = 150

    skyguard_weapon_system.Name = "skyguard_weapon_system"
    skyguard_weapon_system.Size = EquipmentSize.VehicleWeapon
    skyguard_weapon_system.AmmoTypes += skyguard_flak_cannon_ammo
    skyguard_weapon_system.AmmoTypes += bullet_12mm
    skyguard_weapon_system.ProjectileTypes += skyguard_flak_cannon_projectile
    skyguard_weapon_system.ProjectileTypes += bullet_12mm_projectile
    skyguard_weapon_system.FireModes += new FireModeDefinition
    skyguard_weapon_system.FireModes.head.AmmoTypeIndices += 0
    skyguard_weapon_system.FireModes.head.AmmoSlotIndex = 0
    skyguard_weapon_system.FireModes.head.Magazine = 40
    skyguard_weapon_system.FireModes += new FireModeDefinition
    skyguard_weapon_system.FireModes(1).AmmoTypeIndices += 1
    skyguard_weapon_system.FireModes(1).AmmoSlotIndex = 1
    skyguard_weapon_system.FireModes(1).Magazine = 250

    grenade_launcher_marauder.Name = "grenade_launcher_marauder"
    grenade_launcher_marauder.Size = EquipmentSize.VehicleWeapon
    grenade_launcher_marauder.AmmoTypes += heavy_grenade_mortar
    grenade_launcher_marauder.ProjectileTypes += heavy_grenade_projectile
    grenade_launcher_marauder.FireModes += new FireModeDefinition
    grenade_launcher_marauder.FireModes.head.AmmoTypeIndices += 0
    grenade_launcher_marauder.FireModes.head.AmmoSlotIndex = 0
    grenade_launcher_marauder.FireModes.head.Magazine = 50

    advanced_missile_launcher_t.Name = "advanced_missile_launcher_t"
    advanced_missile_launcher_t.Size = EquipmentSize.VehicleWeapon
    advanced_missile_launcher_t.AmmoTypes += firebird_missile
    advanced_missile_launcher_t.ProjectileTypes += firebird_missile_projectile
    advanced_missile_launcher_t.FireModes += new FireModeDefinition
    advanced_missile_launcher_t.FireModes.head.AmmoTypeIndices += 0
    advanced_missile_launcher_t.FireModes.head.AmmoSlotIndex = 0
    advanced_missile_launcher_t.FireModes.head.Magazine = 40

    flux_cannon_thresher.Name = "flux_cannon_thresher"
    flux_cannon_thresher.Size = EquipmentSize.VehicleWeapon
    flux_cannon_thresher.AmmoTypes += flux_cannon_thresher_battery
    flux_cannon_thresher.ProjectileTypes += flux_cannon_thresher_projectile
    flux_cannon_thresher.FireModes += new FireModeDefinition
    flux_cannon_thresher.FireModes.head.AmmoTypeIndices += 0
    flux_cannon_thresher.FireModes.head.AmmoSlotIndex = 0
    flux_cannon_thresher.FireModes.head.Magazine = 100

    mediumtransport_weapon_systemA.Name = "mediumtransport_weapon_systemA"
    mediumtransport_weapon_systemA.Size = EquipmentSize.VehicleWeapon
    mediumtransport_weapon_systemA.AmmoTypes += bullet_20mm
    mediumtransport_weapon_systemA.ProjectileTypes += bullet_20mm_projectile
    mediumtransport_weapon_systemA.FireModes += new FireModeDefinition
    mediumtransport_weapon_systemA.FireModes.head.AmmoTypeIndices += 0
    mediumtransport_weapon_systemA.FireModes.head.AmmoSlotIndex = 0
    mediumtransport_weapon_systemA.FireModes.head.Magazine = 150

    mediumtransport_weapon_systemB.Name = "mediumtransport_weapon_systemB"
    mediumtransport_weapon_systemB.Size = EquipmentSize.VehicleWeapon
    mediumtransport_weapon_systemB.AmmoTypes += bullet_20mm
    mediumtransport_weapon_systemB.ProjectileTypes += bullet_20mm_projectile
    mediumtransport_weapon_systemB.FireModes += new FireModeDefinition
    mediumtransport_weapon_systemB.FireModes.head.AmmoTypeIndices += 0
    mediumtransport_weapon_systemB.FireModes.head.AmmoSlotIndex = 0
    mediumtransport_weapon_systemB.FireModes.head.Magazine = 150

    battlewagon_weapon_systema.Name = "battlewagon_weapon_systema"
    battlewagon_weapon_systema.Size = EquipmentSize.VehicleWeapon
    battlewagon_weapon_systema.AmmoTypes += bullet_15mm
    battlewagon_weapon_systema.ProjectileTypes += bullet_15mm_projectile
    battlewagon_weapon_systema.FireModes += new FireModeDefinition
    battlewagon_weapon_systema.FireModes.head.AmmoTypeIndices += 0
    battlewagon_weapon_systema.FireModes.head.AmmoSlotIndex = 0
    battlewagon_weapon_systema.FireModes.head.Magazine = 240

    battlewagon_weapon_systemb.Name = "battlewagon_weapon_systemb"
    battlewagon_weapon_systemb.Size = EquipmentSize.VehicleWeapon
    battlewagon_weapon_systemb.AmmoTypes += bullet_15mm
    battlewagon_weapon_systemb.ProjectileTypes += bullet_15mm_projectile
    battlewagon_weapon_systemb.FireModes += new FireModeDefinition
    battlewagon_weapon_systemb.FireModes.head.AmmoTypeIndices += 0
    battlewagon_weapon_systemb.FireModes.head.AmmoSlotIndex = 0
    battlewagon_weapon_systemb.FireModes.head.Magazine = 240

    battlewagon_weapon_systemc.Name = "battlewagon_weapon_systemc"
    battlewagon_weapon_systemc.Size = EquipmentSize.VehicleWeapon
    battlewagon_weapon_systemc.AmmoTypes += bullet_15mm
    battlewagon_weapon_systemc.ProjectileTypes += bullet_15mm_projectile
    battlewagon_weapon_systemc.FireModes += new FireModeDefinition
    battlewagon_weapon_systemc.FireModes.head.AmmoTypeIndices += 0
    battlewagon_weapon_systemc.FireModes.head.AmmoSlotIndex = 0
    battlewagon_weapon_systemc.FireModes.head.Magazine = 240

    battlewagon_weapon_systemd.Name = "battlewagon_weapon_systemd"
    battlewagon_weapon_systemd.Size = EquipmentSize.VehicleWeapon
    battlewagon_weapon_systemd.AmmoTypes += bullet_15mm
    battlewagon_weapon_systemd.ProjectileTypes += bullet_15mm_projectile
    battlewagon_weapon_systemd.FireModes += new FireModeDefinition
    battlewagon_weapon_systemd.FireModes.head.AmmoTypeIndices += 0
    battlewagon_weapon_systemd.FireModes.head.AmmoSlotIndex = 0
    battlewagon_weapon_systemd.FireModes.head.Magazine = 240

    thunderer_weapon_systema.Name = "thunderer_weapon_systema"
    thunderer_weapon_systema.Size = EquipmentSize.VehicleWeapon
    thunderer_weapon_systema.AmmoTypes += gauss_cannon_ammo
    thunderer_weapon_systema.ProjectileTypes += gauss_cannon_projectile
    thunderer_weapon_systema.FireModes += new FireModeDefinition
    thunderer_weapon_systema.FireModes.head.AmmoTypeIndices += 0
    thunderer_weapon_systema.FireModes.head.AmmoSlotIndex = 0
    thunderer_weapon_systema.FireModes.head.Magazine = 15

    thunderer_weapon_systemb.Name = "thunderer_weapon_systemb"
    thunderer_weapon_systemb.Size = EquipmentSize.VehicleWeapon
    thunderer_weapon_systemb.AmmoTypes += gauss_cannon_ammo
    thunderer_weapon_systemb.ProjectileTypes += gauss_cannon_projectile
    thunderer_weapon_systemb.FireModes += new FireModeDefinition
    thunderer_weapon_systemb.FireModes.head.AmmoTypeIndices += 0
    thunderer_weapon_systemb.FireModes.head.AmmoSlotIndex = 0
    thunderer_weapon_systemb.FireModes.head.Magazine = 15

    aurora_weapon_systema.Name = "aurora_weapon_systema"
    aurora_weapon_systema.Size = EquipmentSize.VehicleWeapon
    aurora_weapon_systema.AmmoTypes += fluxpod_ammo
    aurora_weapon_systema.ProjectileTypes += fluxpod_projectile
    aurora_weapon_systema.FireModes += new FireModeDefinition
    aurora_weapon_systema.FireModes.head.AmmoTypeIndices += 0
    aurora_weapon_systema.FireModes.head.AmmoSlotIndex = 0
    aurora_weapon_systema.FireModes.head.Magazine = 12
    aurora_weapon_systema.FireModes += new FireModeDefinition
    aurora_weapon_systema.FireModes(1).AmmoTypeIndices += 0
    aurora_weapon_systema.FireModes(1).AmmoSlotIndex = 0
    aurora_weapon_systema.FireModes(1).Magazine = 12

    aurora_weapon_systemb.Name = "aurora_weapon_systemb"
    aurora_weapon_systemb.Size = EquipmentSize.VehicleWeapon
    aurora_weapon_systemb.AmmoTypes += fluxpod_ammo
    aurora_weapon_systemb.ProjectileTypes += fluxpod_projectile
    aurora_weapon_systemb.FireModes += new FireModeDefinition
    aurora_weapon_systemb.FireModes.head.AmmoTypeIndices += 0
    aurora_weapon_systemb.FireModes.head.AmmoSlotIndex = 0
    aurora_weapon_systemb.FireModes.head.Magazine = 12
    aurora_weapon_systemb.FireModes += new FireModeDefinition
    aurora_weapon_systemb.FireModes(1).AmmoTypeIndices += 0
    aurora_weapon_systemb.FireModes(1).AmmoSlotIndex = 0
    aurora_weapon_systemb.FireModes(1).Magazine = 12

    apc_weapon_systema.Name = "apc_weapon_systema"
    apc_weapon_systema.Size = EquipmentSize.VehicleWeapon
    apc_weapon_systema.AmmoTypes += bullet_75mm
    apc_weapon_systema.ProjectileTypes += bullet_75mm_apc_projectile
    apc_weapon_systema.FireModes += new FireModeDefinition
    apc_weapon_systema.FireModes.head.AmmoTypeIndices += 0
    apc_weapon_systema.FireModes.head.AmmoSlotIndex = 0
    apc_weapon_systema.FireModes.head.Magazine = 50

    apc_weapon_systemb.Name = "apc_weapon_systemb"
    apc_weapon_systemb.Size = EquipmentSize.VehicleWeapon
    apc_weapon_systemb.AmmoTypes += bullet_75mm
    apc_weapon_systemb.ProjectileTypes += bullet_75mm_apc_projectile
    apc_weapon_systemb.FireModes += new FireModeDefinition
    apc_weapon_systemb.FireModes.head.AmmoTypeIndices += 0
    apc_weapon_systemb.FireModes.head.AmmoSlotIndex = 0
    apc_weapon_systemb.FireModes.head.Magazine = 50

    apc_ballgun_r.Name = "apc_ballgun_r"
    apc_ballgun_r.Size = EquipmentSize.VehicleWeapon
    apc_ballgun_r.AmmoTypes += bullet_12mm
    apc_ballgun_r.ProjectileTypes += bullet_12mm_projectile
    apc_ballgun_r.FireModes += new FireModeDefinition
    apc_ballgun_r.FireModes.head.AmmoTypeIndices += 0
    apc_ballgun_r.FireModes.head.AmmoSlotIndex = 0
    apc_ballgun_r.FireModes.head.Magazine = 150

    apc_ballgun_l.Name = "apc_ballgun_l"
    apc_ballgun_l.Size = EquipmentSize.VehicleWeapon
    apc_ballgun_l.AmmoTypes += bullet_12mm
    apc_ballgun_l.ProjectileTypes += bullet_12mm_projectile
    apc_ballgun_l.FireModes += new FireModeDefinition
    apc_ballgun_l.FireModes.head.AmmoTypeIndices += 0
    apc_ballgun_l.FireModes.head.AmmoSlotIndex = 0
    apc_ballgun_l.FireModes.head.Magazine = 150

    apc_weapon_systemc_tr.Name = "apc_weapon_systemc_tr"
    apc_weapon_systemc_tr.Size = EquipmentSize.VehicleWeapon
    apc_weapon_systemc_tr.AmmoTypes += bullet_15mm
    apc_weapon_systemc_tr.ProjectileTypes += bullet_15mm_apc_projectile
    apc_weapon_systemc_tr.FireModes += new FireModeDefinition
    apc_weapon_systemc_tr.FireModes.head.AmmoTypeIndices += 0
    apc_weapon_systemc_tr.FireModes.head.AmmoSlotIndex = 0
    apc_weapon_systemc_tr.FireModes.head.Magazine = 150

    apc_weapon_systemd_tr.Name = "apc_weapon_systemd_tr"
    apc_weapon_systemd_tr.Size = EquipmentSize.VehicleWeapon
    apc_weapon_systemd_tr.AmmoTypes += bullet_15mm
    apc_weapon_systemd_tr.ProjectileTypes += bullet_15mm_apc_projectile
    apc_weapon_systemd_tr.FireModes += new FireModeDefinition
    apc_weapon_systemd_tr.FireModes.head.AmmoTypeIndices += 0
    apc_weapon_systemd_tr.FireModes.head.AmmoSlotIndex = 0
    apc_weapon_systemd_tr.FireModes.head.Magazine = 150

    apc_weapon_systemc_nc.Name = "apc_weapon_systemc_nc"
    apc_weapon_systemc_nc.Size = EquipmentSize.VehicleWeapon
    apc_weapon_systemc_nc.AmmoTypes += bullet_20mm
    apc_weapon_systemc_nc.ProjectileTypes += bullet_20mm_apc_projectile
    apc_weapon_systemc_nc.FireModes += new FireModeDefinition
    apc_weapon_systemc_nc.FireModes.head.AmmoTypeIndices += 0
    apc_weapon_systemc_nc.FireModes.head.AmmoSlotIndex = 0
    apc_weapon_systemc_nc.FireModes.head.Magazine = 150

    apc_weapon_systemd_nc.Name = "apc_weapon_systemd_nc"
    apc_weapon_systemd_nc.Size = EquipmentSize.VehicleWeapon
    apc_weapon_systemd_nc.AmmoTypes += bullet_20mm
    apc_weapon_systemd_nc.ProjectileTypes += bullet_20mm_apc_projectile
    apc_weapon_systemd_nc.FireModes += new FireModeDefinition
    apc_weapon_systemd_nc.FireModes.head.AmmoTypeIndices += 0
    apc_weapon_systemd_nc.FireModes.head.AmmoSlotIndex = 0
    apc_weapon_systemd_nc.FireModes.head.Magazine = 150

    apc_weapon_systemc_vs.Name = "apc_weapon_systemc_vs"
    apc_weapon_systemc_vs.Size = EquipmentSize.VehicleWeapon
    apc_weapon_systemc_vs.AmmoTypes += flux_cannon_thresher_battery
    apc_weapon_systemc_vs.ProjectileTypes += flux_cannon_apc_projectile
    apc_weapon_systemc_vs.FireModes += new FireModeDefinition
    apc_weapon_systemc_vs.FireModes.head.AmmoTypeIndices += 0
    apc_weapon_systemc_vs.FireModes.head.AmmoSlotIndex = 0
    apc_weapon_systemc_vs.FireModes.head.Magazine = 100

    apc_weapon_systemd_vs.Name = "apc_weapon_systemd_vs"
    apc_weapon_systemd_vs.Size = EquipmentSize.VehicleWeapon
    apc_weapon_systemd_vs.AmmoTypes += flux_cannon_thresher_battery
    apc_weapon_systemd_vs.ProjectileTypes += flux_cannon_apc_projectile
    apc_weapon_systemd_vs.FireModes += new FireModeDefinition
    apc_weapon_systemd_vs.FireModes.head.AmmoTypeIndices += 0
    apc_weapon_systemd_vs.FireModes.head.AmmoSlotIndex = 0
    apc_weapon_systemd_vs.FireModes.head.Magazine = 100

    lightning_weapon_system.Name = "lightning_weapon_system"
    lightning_weapon_system.Size = EquipmentSize.VehicleWeapon
    lightning_weapon_system.AmmoTypes += bullet_75mm
    lightning_weapon_system.AmmoTypes += bullet_12mm
    lightning_weapon_system.ProjectileTypes += bullet_75mm_projectile
    lightning_weapon_system.ProjectileTypes += bullet_12mm_projectile
    lightning_weapon_system.FireModes += new FireModeDefinition
    lightning_weapon_system.FireModes.head.AmmoTypeIndices += 0
    lightning_weapon_system.FireModes.head.AmmoSlotIndex = 0
    lightning_weapon_system.FireModes.head.Magazine = 20
    lightning_weapon_system.FireModes += new FireModeDefinition
    lightning_weapon_system.FireModes(1).AmmoTypeIndices += 1
    lightning_weapon_system.FireModes(1).AmmoSlotIndex = 1
    lightning_weapon_system.FireModes(1).Magazine = 150

    prowler_weapon_systemA.Name = "prowler_weapon_systemA"
    prowler_weapon_systemA.Size = EquipmentSize.VehicleWeapon
    prowler_weapon_systemA.AmmoTypes += bullet_105mm
    prowler_weapon_systemA.ProjectileTypes += bullet_105mm_projectile
    prowler_weapon_systemA.FireModes += new FireModeDefinition
    prowler_weapon_systemA.FireModes.head.AmmoTypeIndices += 0
    prowler_weapon_systemA.FireModes.head.AmmoSlotIndex = 0
    prowler_weapon_systemA.FireModes.head.Magazine = 20

    prowler_weapon_systemB.Name = "prowler_weapon_systemB"
    prowler_weapon_systemB.Size = EquipmentSize.VehicleWeapon
    prowler_weapon_systemB.AmmoTypes += bullet_15mm
    prowler_weapon_systemB.ProjectileTypes += bullet_15mm_projectile
    prowler_weapon_systemB.FireModes += new FireModeDefinition
    prowler_weapon_systemB.FireModes.head.AmmoTypeIndices += 0
    prowler_weapon_systemB.FireModes.head.AmmoSlotIndex = 0
    prowler_weapon_systemB.FireModes.head.Magazine = 240

    vanguard_weapon_system.Name = "vanguard_weapon_system"
    vanguard_weapon_system.Size = EquipmentSize.VehicleWeapon
    vanguard_weapon_system.AmmoTypes += bullet_150mm
    vanguard_weapon_system.AmmoTypes += bullet_20mm
    vanguard_weapon_system.ProjectileTypes += bullet_150mm_projectile
    vanguard_weapon_system.ProjectileTypes += bullet_20mm_projectile
    vanguard_weapon_system.FireModes += new FireModeDefinition
    vanguard_weapon_system.FireModes.head.AmmoTypeIndices += 0
    vanguard_weapon_system.FireModes.head.AmmoSlotIndex = 0
    vanguard_weapon_system.FireModes.head.Magazine = 10
    vanguard_weapon_system.FireModes += new FireModeDefinition
    vanguard_weapon_system.FireModes(1).AmmoTypeIndices += 1
    vanguard_weapon_system.FireModes(1).AmmoSlotIndex = 1
    vanguard_weapon_system.FireModes(1).Magazine = 200

    particle_beam_magrider.Name = "particle_beam_magrider"
    particle_beam_magrider.Size = EquipmentSize.VehicleWeapon
    particle_beam_magrider.AmmoTypes += pulse_battery
    particle_beam_magrider.ProjectileTypes += ppa_projectile
    particle_beam_magrider.FireModes += new FireModeDefinition
    particle_beam_magrider.FireModes.head.AmmoTypeIndices += 0
    particle_beam_magrider.FireModes.head.AmmoSlotIndex = 0
    particle_beam_magrider.FireModes.head.Magazine = 150

    heavy_rail_beam_magrider.Name = "heavy_rail_beam_magrider"
    heavy_rail_beam_magrider.Size = EquipmentSize.VehicleWeapon
    heavy_rail_beam_magrider.AmmoTypes += heavy_rail_beam_battery
    heavy_rail_beam_magrider.ProjectileTypes += heavy_rail_beam_projectile
    heavy_rail_beam_magrider.FireModes += new FireModeDefinition
    heavy_rail_beam_magrider.FireModes.head.AmmoTypeIndices += 0
    heavy_rail_beam_magrider.FireModes.head.AmmoSlotIndex = 0
    heavy_rail_beam_magrider.FireModes.head.Magazine = 25

    flail_weapon.Name = "flail_weapon"
    flail_weapon.Size = EquipmentSize.VehicleWeapon
    flail_weapon.AmmoTypes += ancient_ammo_vehicle
    flail_weapon.ProjectileTypes += flail_projectile
    flail_weapon.FireModes += new FireModeDefinition
    flail_weapon.FireModes.head.AmmoTypeIndices += 0
    flail_weapon.FireModes.head.AmmoSlotIndex = 0
    flail_weapon.FireModes.head.Magazine = 100

    rotarychaingun_mosquito.Name = "rotarychaingun_mosquito"
    rotarychaingun_mosquito.Size = EquipmentSize.VehicleWeapon
    rotarychaingun_mosquito.AmmoTypes += bullet_12mm
    rotarychaingun_mosquito.ProjectileTypes += bullet_12mm_projectile
    rotarychaingun_mosquito.FireModes += new FireModeDefinition
    rotarychaingun_mosquito.FireModes.head.AmmoTypeIndices += 0
    rotarychaingun_mosquito.FireModes.head.AmmoSlotIndex = 0
    rotarychaingun_mosquito.FireModes.head.Magazine = 150

    lightgunship_weapon_system.Name = "lightgunship_weapon_system"
    lightgunship_weapon_system.Size = EquipmentSize.VehicleWeapon
    lightgunship_weapon_system.AmmoTypes += bullet_20mm
    lightgunship_weapon_system.AmmoTypes += reaver_rocket
    lightgunship_weapon_system.ProjectileTypes += bullet_20mm_projectile
    lightgunship_weapon_system.ProjectileTypes += reaver_rocket_projectile
    lightgunship_weapon_system.FireModes += new FireModeDefinition
    lightgunship_weapon_system.FireModes.head.AmmoTypeIndices += 0
    lightgunship_weapon_system.FireModes.head.AmmoSlotIndex = 0
    lightgunship_weapon_system.FireModes.head.Magazine = 245
    lightgunship_weapon_system.FireModes += new FireModeDefinition
    lightgunship_weapon_system.FireModes(1).AmmoTypeIndices += 1
    lightgunship_weapon_system.FireModes(1).AmmoSlotIndex = 1
    lightgunship_weapon_system.FireModes(1).Magazine = 16

    wasp_weapon_system.Name = "wasp_weapon_system"
    wasp_weapon_system.Size = EquipmentSize.VehicleWeapon
    wasp_weapon_system.AmmoTypes += wasp_gun_ammo
    wasp_weapon_system.AmmoTypes += wasp_rocket_ammo
    wasp_weapon_system.ProjectileTypes += wasp_gun_projectile
    wasp_weapon_system.ProjectileTypes += wasp_rocket_projectile
    wasp_weapon_system.FireModes += new FireModeDefinition
    wasp_weapon_system.FireModes.head.AmmoTypeIndices += 0
    wasp_weapon_system.FireModes.head.AmmoSlotIndex = 0
    wasp_weapon_system.FireModes.head.Magazine = 30
    wasp_weapon_system.FireModes += new FireModeDefinition
    wasp_weapon_system.FireModes(1).AmmoTypeIndices += 1
    wasp_weapon_system.FireModes(1).AmmoSlotIndex = 1
    wasp_weapon_system.FireModes(1).Magazine = 2

    liberator_weapon_system.Name = "liberator_weapon_system"
    liberator_weapon_system.Size = EquipmentSize.VehicleWeapon
    liberator_weapon_system.AmmoTypes += bullet_35mm
    liberator_weapon_system.ProjectileTypes += bullet_35mm_projectile
    liberator_weapon_system.FireModes += new FireModeDefinition
    liberator_weapon_system.FireModes.head.AmmoTypeIndices += 0
    liberator_weapon_system.FireModes.head.AmmoSlotIndex = 0
    liberator_weapon_system.FireModes.head.Magazine = 100

    liberator_bomb_bay.Name = "liberator_bomb_bay"
    liberator_bomb_bay.Size = EquipmentSize.VehicleWeapon
    liberator_bomb_bay.AmmoTypes += liberator_bomb
    liberator_bomb_bay.ProjectileTypes += liberator_bomb_projectile
    liberator_bomb_bay.ProjectileTypes += liberator_bomb_cluster_projectile
    liberator_bomb_bay.FireModes += new FireModeDefinition
    liberator_bomb_bay.FireModes.head.AmmoTypeIndices += 0
    liberator_bomb_bay.FireModes.head.AmmoSlotIndex = 0
    liberator_bomb_bay.FireModes.head.Magazine = 10
    liberator_bomb_bay.FireModes += new FireModeDefinition
    liberator_bomb_bay.FireModes(1).AmmoTypeIndices += 0
    liberator_bomb_bay.FireModes(1).ProjectileTypeIndices += 1
    liberator_bomb_bay.FireModes(1).AmmoSlotIndex = 0
    liberator_bomb_bay.FireModes(1).Magazine = 10

    liberator_25mm_cannon.Name = "liberator_25mm_cannon"
    liberator_25mm_cannon.Size = EquipmentSize.VehicleWeapon
    liberator_25mm_cannon.AmmoTypes += bullet_25mm
    liberator_25mm_cannon.ProjectileTypes += bullet_25mm_projectile
    liberator_25mm_cannon.FireModes += new FireModeDefinition
    liberator_25mm_cannon.FireModes.head.AmmoTypeIndices += 0
    liberator_25mm_cannon.FireModes.head.AmmoSlotIndex = 0
    liberator_25mm_cannon.FireModes.head.Magazine = 150

    vulture_nose_weapon_system.Name = "vulture_nose_weapon_system"
    vulture_nose_weapon_system.Size = EquipmentSize.VehicleWeapon
    vulture_nose_weapon_system.AmmoTypes += bullet_35mm
    vulture_nose_weapon_system.ProjectileTypes += vulture_nose_bullet_projectile
    vulture_nose_weapon_system.FireModes += new FireModeDefinition
    vulture_nose_weapon_system.FireModes.head.AmmoTypeIndices += 0
    vulture_nose_weapon_system.FireModes.head.AmmoSlotIndex = 0
    vulture_nose_weapon_system.FireModes.head.Magazine = 75

    vulture_bomb_bay.Name = "vulture_bomb_bay"
    vulture_bomb_bay.Size = EquipmentSize.VehicleWeapon
    vulture_bomb_bay.AmmoTypes += liberator_bomb
    vulture_bomb_bay.ProjectileTypes += vulture_bomb_projectile
    vulture_bomb_bay.FireModes += new FireModeDefinition
    vulture_bomb_bay.FireModes.head.AmmoTypeIndices += 0
    vulture_bomb_bay.FireModes.head.AmmoSlotIndex = 0
    vulture_bomb_bay.FireModes.head.Magazine = 10

    vulture_tail_cannon.Name = "vulture_tail_cannon"
    vulture_tail_cannon.Size = EquipmentSize.VehicleWeapon
    vulture_tail_cannon.AmmoTypes += bullet_25mm
    vulture_tail_cannon.ProjectileTypes += vulture_tail_bullet_projectile
    vulture_tail_cannon.FireModes += new FireModeDefinition
    vulture_tail_cannon.FireModes.head.AmmoTypeIndices += 0
    vulture_tail_cannon.FireModes.head.AmmoSlotIndex = 0
    vulture_tail_cannon.FireModes.head.Magazine = 100

    cannon_dropship_20mm.Name = "cannon_dropship_20mm"
    cannon_dropship_20mm.Size = EquipmentSize.VehicleWeapon
    cannon_dropship_20mm.AmmoTypes += bullet_20mm
    cannon_dropship_20mm.ProjectileTypes += bullet_20mm_projectile
    cannon_dropship_20mm.FireModes += new FireModeDefinition
    cannon_dropship_20mm.FireModes.head.AmmoTypeIndices += 0
    cannon_dropship_20mm.FireModes.head.AmmoSlotIndex = 0
    cannon_dropship_20mm.FireModes.head.Magazine = 250

    dropship_rear_turret.Name = "dropship_rear_turret"
    dropship_rear_turret.Size = EquipmentSize.VehicleWeapon
    dropship_rear_turret.AmmoTypes += bullet_20mm
    dropship_rear_turret.ProjectileTypes += bullet_20mm_projectile
    dropship_rear_turret.FireModes += new FireModeDefinition
    dropship_rear_turret.FireModes.head.AmmoTypeIndices += 0
    dropship_rear_turret.FireModes.head.AmmoSlotIndex = 0
    dropship_rear_turret.FireModes.head.Magazine = 250

    galaxy_gunship_cannon.Name = "galaxy_gunship_cannon"
    galaxy_gunship_cannon.Size = EquipmentSize.VehicleWeapon
    galaxy_gunship_cannon.AmmoTypes += heavy_grenade_mortar
    galaxy_gunship_cannon.ProjectileTypes += heavy_grenade_projectile
    galaxy_gunship_cannon.FireModes += new FireModeDefinition
    galaxy_gunship_cannon.FireModes.head.AmmoTypeIndices += 0
    galaxy_gunship_cannon.FireModes.head.AmmoSlotIndex = 0
    galaxy_gunship_cannon.FireModes.head.Magazine = 50
    galaxy_gunship_cannon.FireModes.head.Add.Damage1 = 50
    galaxy_gunship_cannon.FireModes.head.Add.Damage2 = 50
    galaxy_gunship_cannon.FireModes.head.Add.Damage3 = 10
    galaxy_gunship_cannon.FireModes.head.Add.Damage4 = 50

    galaxy_gunship_tailgun.Name = "galaxy_gunship_tailgun"
    galaxy_gunship_tailgun.Size = EquipmentSize.VehicleWeapon
    galaxy_gunship_tailgun.AmmoTypes += bullet_35mm
    galaxy_gunship_tailgun.ProjectileTypes += galaxy_gunship_gun_projectile
    galaxy_gunship_tailgun.FireModes += new FireModeDefinition
    galaxy_gunship_tailgun.FireModes.head.AmmoTypeIndices += 0
    galaxy_gunship_tailgun.FireModes.head.AmmoSlotIndex = 0
    galaxy_gunship_tailgun.FireModes.head.Magazine = 200

    galaxy_gunship_gun.Name = "galaxy_gunship_gun"
    galaxy_gunship_gun.Size = EquipmentSize.VehicleWeapon
    galaxy_gunship_gun.AmmoTypes += bullet_35mm
    galaxy_gunship_gun.ProjectileTypes += galaxy_gunship_gun_projectile
    galaxy_gunship_gun.FireModes += new FireModeDefinition
    galaxy_gunship_gun.FireModes.head.AmmoTypeIndices += 0
    galaxy_gunship_gun.FireModes.head.AmmoSlotIndex = 0
    galaxy_gunship_gun.FireModes.head.Magazine = 200

    phalanx_sgl_hevgatcan.Name = "phalanx_sgl_hevgatcan"
    phalanx_sgl_hevgatcan.Size = EquipmentSize.BaseTurretWeapon
    phalanx_sgl_hevgatcan.AmmoTypes += phalanx_ammo
    phalanx_sgl_hevgatcan.ProjectileTypes += phalanx_projectile
    phalanx_sgl_hevgatcan.FireModes += new InfiniteFireModeDefinition
    phalanx_sgl_hevgatcan.FireModes.head.AmmoTypeIndices += 0
    phalanx_sgl_hevgatcan.FireModes.head.AmmoSlotIndex = 0
    phalanx_sgl_hevgatcan.FireModes.head.Magazine = 4000

    phalanx_avcombo.Name = "phalanx_avcombo"
    phalanx_avcombo.Size = EquipmentSize.BaseTurretWeapon
    phalanx_avcombo.AmmoTypes += phalanx_ammo
    phalanx_avcombo.ProjectileTypes += phalanx_projectile
    phalanx_avcombo.ProjectileTypes += phalanx_av_projectile
    phalanx_avcombo.FireModes += new InfiniteFireModeDefinition
    phalanx_avcombo.FireModes.head.AmmoTypeIndices += 0
    phalanx_avcombo.FireModes.head.AmmoSlotIndex = 0
    phalanx_avcombo.FireModes.head.Magazine = 4000
    phalanx_avcombo.FireModes += new InfiniteFireModeDefinition
    phalanx_avcombo.FireModes(1).AmmoTypeIndices += 0
    phalanx_avcombo.FireModes(1).ProjectileTypeIndices += 1
    phalanx_avcombo.FireModes(1).AmmoSlotIndex = 0
    phalanx_avcombo.FireModes(1).Magazine = 4000

    phalanx_flakcombo.Name = "phalanx_flakcombo"
    phalanx_flakcombo.Size = EquipmentSize.BaseTurretWeapon
    phalanx_flakcombo.AmmoTypes += phalanx_ammo
    phalanx_flakcombo.ProjectileTypes += phalanx_projectile
    phalanx_flakcombo.ProjectileTypes += phalanx_flak_projectile
    phalanx_flakcombo.FireModes += new InfiniteFireModeDefinition
    phalanx_flakcombo.FireModes.head.AmmoTypeIndices += 0
    phalanx_flakcombo.FireModes.head.AmmoSlotIndex = 0
    phalanx_flakcombo.FireModes.head.Magazine = 4000
    phalanx_flakcombo.FireModes += new InfiniteFireModeDefinition
    phalanx_flakcombo.FireModes(1).AmmoTypeIndices += 0
    phalanx_flakcombo.FireModes(1).ProjectileTypeIndices += 1
    phalanx_flakcombo.FireModes(1).AmmoSlotIndex = 0
    phalanx_flakcombo.FireModes(1).Magazine = 4000

    vanu_sentry_turret_weapon.Name = "vanu_sentry_turret_weapon"
    vanu_sentry_turret_weapon.Size = EquipmentSize.BaseTurretWeapon
    vanu_sentry_turret_weapon.AmmoTypes += ancient_ammo_vehicle
    vanu_sentry_turret_weapon.ProjectileTypes += vanu_sentry_turret_projectile
    vanu_sentry_turret_weapon.FireModes += new FireModeDefinition
    vanu_sentry_turret_weapon.FireModes.head.AmmoTypeIndices += 0
    vanu_sentry_turret_weapon.FireModes.head.AmmoSlotIndex = 0
    vanu_sentry_turret_weapon.FireModes.head.Magazine = 100

    spitfire_weapon.Name = "spitfire_weapon"
    spitfire_weapon.Size = EquipmentSize.BaseTurretWeapon
    spitfire_weapon.AmmoTypes += spitfire_ammo
    spitfire_weapon.ProjectileTypes += spitfire_ammo_projectile
    spitfire_weapon.FireModes += new InfiniteFireModeDefinition
    spitfire_weapon.FireModes.head.AmmoTypeIndices += 0
    spitfire_weapon.FireModes.head.AmmoSlotIndex = 0
    spitfire_weapon.FireModes.head.Magazine = 4000

    spitfire_aa_weapon.Name = "spitfire_aa_weapon"
    spitfire_aa_weapon.Size = EquipmentSize.BaseTurretWeapon
    spitfire_aa_weapon.AmmoTypes += spitfire_aa_ammo
    spitfire_aa_weapon.ProjectileTypes += spitfire_aa_ammo_projectile
    spitfire_aa_weapon.FireModes += new InfiniteFireModeDefinition
    spitfire_aa_weapon.FireModes.head.AmmoTypeIndices += 0
    spitfire_aa_weapon.FireModes.head.AmmoSlotIndex = 0
    spitfire_aa_weapon.FireModes.head.Magazine = 4000

    energy_gun.Name = "energy_gun"
    energy_gun.Size = EquipmentSize.BaseTurretWeapon
    energy_gun.AmmoTypes += energy_gun_ammo
    energy_gun.ProjectileTypes += bullet_9mm_projectile //fallback
    energy_gun.FireModes += new FireModeDefinition
    energy_gun.FireModes.head.AmmoTypeIndices += 0
    energy_gun.FireModes.head.AmmoSlotIndex = 0
    energy_gun.FireModes.head.Magazine = 4000

    energy_gun_nc.Name = "energy_gun_nc"
    energy_gun_nc.Size = EquipmentSize.BaseTurretWeapon
    energy_gun_nc.AmmoTypes += energy_gun_ammo
    energy_gun_nc.ProjectileTypes += energy_gun_nc_projectile
    energy_gun_nc.FireModes += new PelletFireModeDefinition
    energy_gun_nc.FireModes.head.AmmoTypeIndices += 0
    energy_gun_nc.FireModes.head.AmmoSlotIndex = 0
    energy_gun_nc.FireModes.head.Magazine = 35
    energy_gun_nc.FireModes.head.Chamber = 8 //35 shots * 8 pellets = 280

    energy_gun_tr.Name = "energy_gun_tr"
    energy_gun_tr.Size = EquipmentSize.BaseTurretWeapon
    energy_gun_tr.AmmoTypes += energy_gun_ammo
    energy_gun_tr.ProjectileTypes += energy_gun_tr_projectile
    energy_gun_tr.FireModes += new FireModeDefinition
    energy_gun_tr.FireModes.head.AmmoTypeIndices += 0
    energy_gun_tr.FireModes.head.AmmoSlotIndex = 0
    energy_gun_tr.FireModes.head.Magazine = 200

    energy_gun_vs.Name = "energy_gun_vs"
    energy_gun_vs.Size = EquipmentSize.BaseTurretWeapon
    energy_gun_vs.AmmoTypes += energy_gun_ammo
    energy_gun_vs.ProjectileTypes += energy_gun_tr_projectile
    energy_gun_vs.FireModes += new FireModeDefinition
    energy_gun_vs.FireModes.head.AmmoTypeIndices += 0
    energy_gun_vs.FireModes.head.AmmoSlotIndex = 0
    energy_gun_vs.FireModes.head.Magazine = 100

    val battleFrameToolConverter = new BattleFrameToolConverter
    aphelion_armor_siphon.Name = "aphelion_armor_siphon"
    aphelion_armor_siphon.Size = EquipmentSize.BFRArmWeapon
    aphelion_armor_siphon.AmmoTypes += armor_siphon_ammo
    aphelion_armor_siphon.ProjectileTypes += armor_siphon_projectile
    aphelion_armor_siphon.FireModes += new FireModeDefinition
    aphelion_armor_siphon.FireModes.head.AmmoTypeIndices += 0
    aphelion_armor_siphon.FireModes.head.AmmoSlotIndex = 0
    aphelion_armor_siphon.FireModes.head.Magazine = 100
    aphelion_armor_siphon.Packet = battleFrameToolConverter
    aphelion_armor_siphon.Tile = InventoryTile.Tile84

    aphelion_armor_siphon_left.Name = "aphelion_armor_siphon_left"
    aphelion_armor_siphon_left.Size = EquipmentSize.BFRArmWeapon
    aphelion_armor_siphon_left.AmmoTypes += armor_siphon_ammo
    aphelion_armor_siphon_left.ProjectileTypes += armor_siphon_projectile
    aphelion_armor_siphon_left.FireModes += new FireModeDefinition
    aphelion_armor_siphon_left.FireModes.head.AmmoTypeIndices += 0
    aphelion_armor_siphon_left.FireModes.head.AmmoSlotIndex = 0
    aphelion_armor_siphon_left.FireModes.head.Magazine = 100
    aphelion_armor_siphon_left.Packet = battleFrameToolConverter
    aphelion_armor_siphon_left.Tile = InventoryTile.Tile84

    aphelion_armor_siphon_right.Name = "aphelion_armor_siphon_right"
    aphelion_armor_siphon_right.Size = EquipmentSize.BFRArmWeapon
    aphelion_armor_siphon_right.AmmoTypes += armor_siphon_ammo
    aphelion_armor_siphon_right.ProjectileTypes += armor_siphon_projectile
    aphelion_armor_siphon_right.FireModes += new FireModeDefinition
    aphelion_armor_siphon_right.FireModes.head.AmmoTypeIndices += 0
    aphelion_armor_siphon_right.FireModes.head.AmmoSlotIndex = 0
    aphelion_armor_siphon_right.FireModes.head.Magazine = 100
    aphelion_armor_siphon_right.Packet = battleFrameToolConverter
    aphelion_armor_siphon_right.Tile = InventoryTile.Tile84

    aphelion_laser.Name = "aphelion_laser"
    aphelion_laser.Size = EquipmentSize.BFRArmWeapon
    aphelion_laser.AmmoTypes += aphelion_laser_ammo
    aphelion_laser.ProjectileTypes += aphelion_laser_projectile
    aphelion_laser.FireModes += new FireModeDefinition
    aphelion_laser.FireModes.head.AmmoTypeIndices += 0
    aphelion_laser.FireModes.head.AmmoSlotIndex = 0
    aphelion_laser.FireModes.head.Magazine = 350
    aphelion_laser.Packet = battleFrameToolConverter
    aphelion_laser.Tile = InventoryTile.Tile84

    aphelion_laser_left.Name = "aphelion_laser_left"
    aphelion_laser_left.Size = EquipmentSize.BFRArmWeapon
    aphelion_laser_left.AmmoTypes += aphelion_laser_ammo
    aphelion_laser_left.ProjectileTypes += aphelion_laser_projectile
    aphelion_laser_left.FireModes += new FireModeDefinition
    aphelion_laser_left.FireModes.head.AmmoTypeIndices += 0
    aphelion_laser_left.FireModes.head.AmmoSlotIndex = 0
    aphelion_laser_left.FireModes.head.Magazine = 350
    aphelion_laser_left.Packet = battleFrameToolConverter
    aphelion_laser_left.Tile = InventoryTile.Tile84

    aphelion_laser_right.Name = "aphelion_laser_right"
    aphelion_laser_right.Size = EquipmentSize.BFRArmWeapon
    aphelion_laser_right.AmmoTypes += aphelion_laser_ammo
    aphelion_laser_right.ProjectileTypes += aphelion_laser_projectile
    aphelion_laser_right.FireModes += new FireModeDefinition
    aphelion_laser_right.FireModes.head.AmmoTypeIndices += 0
    aphelion_laser_right.FireModes.head.AmmoSlotIndex = 0
    aphelion_laser_right.FireModes.head.Magazine = 350
    aphelion_laser_right.Packet = battleFrameToolConverter
    aphelion_laser_right.Tile = InventoryTile.Tile84

    aphelion_ntu_siphon.Name = "aphelion_ntu_siphon"
    aphelion_ntu_siphon.Size = EquipmentSize.BFRArmWeapon
    aphelion_ntu_siphon.AmmoTypes += ntu_siphon_ammo
    aphelion_ntu_siphon.ProjectileTypes += no_projectile
    aphelion_ntu_siphon.ProjectileTypes += ntu_siphon_emp
    aphelion_ntu_siphon.FireModes += new FireModeDefinition
    aphelion_ntu_siphon.FireModes.head.AmmoTypeIndices += 0
    aphelion_ntu_siphon.FireModes.head.AmmoSlotIndex = 0
    aphelion_ntu_siphon.FireModes.head.RoundsPerShot = 5
    aphelion_ntu_siphon.FireModes.head.Magazine = 150
    aphelion_ntu_siphon.FireModes.head.DefaultMagazine = 0
    aphelion_ntu_siphon.FireModes += new FireModeDefinition
    aphelion_ntu_siphon.FireModes(1).AmmoTypeIndices += 0
    aphelion_ntu_siphon.FireModes(1).AmmoSlotIndex = 0
    aphelion_ntu_siphon.FireModes(1).ProjectileTypeIndices += 1
    aphelion_ntu_siphon.FireModes(1).RoundsPerShot = 30
    aphelion_ntu_siphon.FireModes(1).Magazine = 150
    aphelion_ntu_siphon.FireModes(1).DefaultMagazine = 0
    aphelion_ntu_siphon.Packet = battleFrameToolConverter
    aphelion_ntu_siphon.Tile = InventoryTile.Tile84

    aphelion_ntu_siphon_left.Name = "aphelion_ntu_siphon_left"
    aphelion_ntu_siphon_left.Size = EquipmentSize.BFRArmWeapon
    aphelion_ntu_siphon_left.AmmoTypes += ntu_siphon_ammo
    aphelion_ntu_siphon_left.ProjectileTypes += no_projectile
    aphelion_ntu_siphon_left.ProjectileTypes += ntu_siphon_emp
    aphelion_ntu_siphon_left.FireModes += new FireModeDefinition
    aphelion_ntu_siphon_left.FireModes.head.AmmoTypeIndices += 0
    aphelion_ntu_siphon_left.FireModes.head.AmmoSlotIndex = 0
    aphelion_ntu_siphon_left.FireModes.head.RoundsPerShot = 5
    aphelion_ntu_siphon_left.FireModes.head.Magazine = 150
    aphelion_ntu_siphon_left.FireModes.head.DefaultMagazine = 0
    aphelion_ntu_siphon_left.FireModes += new FireModeDefinition
    aphelion_ntu_siphon_left.FireModes(1).AmmoTypeIndices += 0
    aphelion_ntu_siphon_left.FireModes(1).AmmoSlotIndex = 0
    aphelion_ntu_siphon_left.FireModes(1).ProjectileTypeIndices += 1
    aphelion_ntu_siphon_left.FireModes(1).RoundsPerShot = 30
    aphelion_ntu_siphon_left.FireModes(1).Magazine = 150
    aphelion_ntu_siphon_left.FireModes(1).DefaultMagazine = 0
    aphelion_ntu_siphon_left.Packet = battleFrameToolConverter
    aphelion_ntu_siphon_left.Tile = InventoryTile.Tile84

    aphelion_ntu_siphon_right.Name = "aphelion_ntu_siphon_right"
    aphelion_ntu_siphon_right.Size = EquipmentSize.BFRArmWeapon
    aphelion_ntu_siphon_right.AmmoTypes += ntu_siphon_ammo
    aphelion_ntu_siphon_right.ProjectileTypes += no_projectile
    aphelion_ntu_siphon_right.ProjectileTypes += ntu_siphon_emp
    aphelion_ntu_siphon_right.FireModes += new FireModeDefinition
    aphelion_ntu_siphon_right.FireModes.head.AmmoTypeIndices += 0
    aphelion_ntu_siphon_right.FireModes.head.AmmoSlotIndex = 0
    aphelion_ntu_siphon_right.FireModes.head.RoundsPerShot = 5
    aphelion_ntu_siphon_right.FireModes.head.Magazine = 150
    aphelion_ntu_siphon_right.FireModes.head.DefaultMagazine = 0
    aphelion_ntu_siphon_right.FireModes += new FireModeDefinition
    aphelion_ntu_siphon_right.FireModes(1).AmmoTypeIndices += 0
    aphelion_ntu_siphon_right.FireModes(1).AmmoSlotIndex = 0
    aphelion_ntu_siphon_right.FireModes(1).ProjectileTypeIndices += 1
    aphelion_ntu_siphon_right.FireModes(1).RoundsPerShot = 30
    aphelion_ntu_siphon_right.FireModes(1).Magazine = 150
    aphelion_ntu_siphon_right.FireModes(1).DefaultMagazine = 0
    aphelion_ntu_siphon_right.Packet = battleFrameToolConverter
    aphelion_ntu_siphon_right.Tile = InventoryTile.Tile84

    aphelion_ppa.Name = "aphelion_ppa"
    aphelion_ppa.Size = EquipmentSize.BFRArmWeapon
    aphelion_ppa.AmmoTypes += aphelion_ppa_ammo
    aphelion_ppa.ProjectileTypes += aphelion_ppa_projectile
    aphelion_ppa.FireModes += new FireModeDefinition
    aphelion_ppa.FireModes.head.AmmoTypeIndices += 0
    aphelion_ppa.FireModes.head.AmmoSlotIndex = 0
    aphelion_ppa.FireModes.head.Magazine = 25
    aphelion_ppa.Packet = battleFrameToolConverter
    aphelion_ppa.Tile = InventoryTile.Tile84

    aphelion_ppa_left.Name = "aphelion_ppa_left"
    aphelion_ppa_left.Size = EquipmentSize.BFRArmWeapon
    aphelion_ppa_left.AmmoTypes += aphelion_ppa_ammo
    aphelion_ppa_left.ProjectileTypes += aphelion_ppa_projectile
    aphelion_ppa_left.FireModes += new FireModeDefinition
    aphelion_ppa_left.FireModes.head.AmmoTypeIndices += 0
    aphelion_ppa_left.FireModes.head.AmmoSlotIndex = 0
    aphelion_ppa_left.FireModes.head.Magazine = 25
    aphelion_ppa_left.Packet = battleFrameToolConverter
    aphelion_ppa_left.Tile = InventoryTile.Tile84

    aphelion_ppa_right.Name = "aphelion_ppa_right"
    aphelion_ppa_right.Size = EquipmentSize.BFRArmWeapon
    aphelion_ppa_right.AmmoTypes += aphelion_ppa_ammo
    aphelion_ppa_right.ProjectileTypes += aphelion_ppa_projectile
    aphelion_ppa_right.FireModes += new FireModeDefinition
    aphelion_ppa_right.FireModes.head.AmmoTypeIndices += 0
    aphelion_ppa_right.FireModes.head.AmmoSlotIndex = 0
    aphelion_ppa_right.FireModes.head.Magazine = 25
    aphelion_ppa_right.Packet = battleFrameToolConverter
    aphelion_ppa_right.Tile = InventoryTile.Tile84

    aphelion_starfire.Name = "aphelion_starfire"
    aphelion_starfire.Size = EquipmentSize.BFRArmWeapon
    aphelion_starfire.AmmoTypes += aphelion_starfire_ammo
    aphelion_starfire.ProjectileTypes += aphelion_starfire_projectile
    aphelion_starfire.FireModes += new FireModeDefinition
    aphelion_starfire.FireModes.head.AmmoTypeIndices += 0
    aphelion_starfire.FireModes.head.AmmoSlotIndex = 0
    aphelion_starfire.FireModes.head.Magazine = 20
    aphelion_starfire.Packet = battleFrameToolConverter
    aphelion_starfire.Tile = InventoryTile.Tile84

    aphelion_starfire_left.Name = "aphelion_starfire_left"
    aphelion_starfire_left.Size = EquipmentSize.BFRArmWeapon
    aphelion_starfire_left.AmmoTypes += aphelion_starfire_ammo
    aphelion_starfire_left.ProjectileTypes += aphelion_starfire_projectile
    aphelion_starfire_left.FireModes += new FireModeDefinition
    aphelion_starfire_left.FireModes.head.AmmoTypeIndices += 0
    aphelion_starfire_left.FireModes.head.AmmoSlotIndex = 0
    aphelion_starfire_left.FireModes.head.Magazine = 20
    aphelion_starfire_left.Packet = battleFrameToolConverter
    aphelion_starfire_left.Tile = InventoryTile.Tile84

    aphelion_starfire_right.Name = "aphelion_starfire_right"
    aphelion_starfire_right.Size = EquipmentSize.BFRArmWeapon
    aphelion_starfire_right.AmmoTypes += aphelion_starfire_ammo
    aphelion_starfire_right.ProjectileTypes += aphelion_starfire_projectile
    aphelion_starfire_right.FireModes += new FireModeDefinition
    aphelion_starfire_right.FireModes.head.AmmoTypeIndices += 0
    aphelion_starfire_right.FireModes.head.AmmoSlotIndex = 0
    aphelion_starfire_right.FireModes.head.Magazine = 20
    aphelion_starfire_right.Packet = battleFrameToolConverter
    aphelion_starfire_right.Tile = InventoryTile.Tile84

    aphelion_plasma_rocket_pod.Name = "aphelion_plasma_rocket_pod"
    aphelion_plasma_rocket_pod.Size = EquipmentSize.BFRGunnerWeapon
    aphelion_plasma_rocket_pod.AmmoTypes += aphelion_plasma_rocket_ammo
    aphelion_plasma_rocket_pod.ProjectileTypes += aphelion_plasma_rocket_projectile
    aphelion_plasma_rocket_pod.FireModes += new FireModeDefinition
    aphelion_plasma_rocket_pod.FireModes.head.AmmoTypeIndices += 0
    aphelion_plasma_rocket_pod.FireModes.head.AmmoSlotIndex = 0
    aphelion_plasma_rocket_pod.FireModes.head.Magazine = 40
    aphelion_plasma_rocket_pod.Packet = battleFrameToolConverter
    aphelion_plasma_rocket_pod.Tile = InventoryTile.Tile1004

    aphelion_immolation_cannon.Name = "aphelion_immolation_cannon"
    aphelion_immolation_cannon.Size = EquipmentSize.BFRGunnerWeapon
    aphelion_immolation_cannon.AmmoTypes += aphelion_immolation_cannon_ammo
    aphelion_immolation_cannon.ProjectileTypes += aphelion_immolation_cannon_projectile
    aphelion_immolation_cannon.FireModes += new FireModeDefinition
    aphelion_immolation_cannon.FireModes.head.AmmoTypeIndices += 0
    aphelion_immolation_cannon.FireModes.head.AmmoSlotIndex = 0
    aphelion_immolation_cannon.FireModes.head.Magazine = 25
    aphelion_immolation_cannon.Packet = battleFrameToolConverter
    aphelion_immolation_cannon.Tile = InventoryTile.Tile1004

    colossus_armor_siphon.Name = "colossus_armor_siphon"
    colossus_armor_siphon.Size = EquipmentSize.BFRArmWeapon
    colossus_armor_siphon.AmmoTypes += armor_siphon_ammo
    colossus_armor_siphon.ProjectileTypes += armor_siphon_projectile
    colossus_armor_siphon.FireModes += new FireModeDefinition
    colossus_armor_siphon.FireModes.head.AmmoTypeIndices += 0
    colossus_armor_siphon.FireModes.head.AmmoSlotIndex = 0
    colossus_armor_siphon.FireModes.head.Magazine = 100
    colossus_armor_siphon.Packet = battleFrameToolConverter
    colossus_armor_siphon.Tile = InventoryTile.Tile84

    colossus_armor_siphon_left.Name = "colossus_armor_siphon_left"
    colossus_armor_siphon_left.Size = EquipmentSize.BFRArmWeapon
    colossus_armor_siphon_left.AmmoTypes += armor_siphon_ammo
    colossus_armor_siphon_left.ProjectileTypes += armor_siphon_projectile
    colossus_armor_siphon_left.FireModes += new FireModeDefinition
    colossus_armor_siphon_left.FireModes.head.AmmoTypeIndices += 0
    colossus_armor_siphon_left.FireModes.head.AmmoSlotIndex = 0
    colossus_armor_siphon_left.FireModes.head.Magazine = 100
    colossus_armor_siphon_left.Packet = battleFrameToolConverter
    colossus_armor_siphon_left.Tile = InventoryTile.Tile84

    colossus_armor_siphon_right.Name = "colossus_armor_siphon_right"
    colossus_armor_siphon_right.Size = EquipmentSize.BFRArmWeapon
    colossus_armor_siphon_right.AmmoTypes += armor_siphon_ammo
    colossus_armor_siphon_right.ProjectileTypes += armor_siphon_projectile
    colossus_armor_siphon_right.FireModes += new FireModeDefinition
    colossus_armor_siphon_right.FireModes.head.AmmoTypeIndices += 0
    colossus_armor_siphon_right.FireModes.head.AmmoSlotIndex = 0
    colossus_armor_siphon_right.FireModes.head.Magazine = 100
    colossus_armor_siphon_right.Packet = battleFrameToolConverter
    colossus_armor_siphon_right.Tile = InventoryTile.Tile84

    colossus_burster.Name = "colossus_burster"
    colossus_burster.Size = EquipmentSize.BFRArmWeapon
    colossus_burster.AmmoTypes += colossus_burster_ammo
    colossus_burster.ProjectileTypes += colossus_burster_projectile
    colossus_burster.FireModes += new FireModeDefinition
    colossus_burster.FireModes.head.AmmoTypeIndices += 0
    colossus_burster.FireModes.head.AmmoSlotIndex = 0
    colossus_burster.FireModes.head.Magazine = 25
    colossus_burster.Packet = battleFrameToolConverter
    colossus_burster.Tile = InventoryTile.Tile84

    colossus_burster_left.Name = "colossus_burster_left"
    colossus_burster_left.Size = EquipmentSize.BFRArmWeapon
    colossus_burster_left.AmmoTypes += colossus_burster_ammo
    colossus_burster_left.ProjectileTypes += colossus_burster_projectile
    colossus_burster_left.FireModes += new FireModeDefinition
    colossus_burster_left.FireModes.head.AmmoTypeIndices += 0
    colossus_burster_left.FireModes.head.AmmoSlotIndex = 0
    colossus_burster_left.FireModes.head.Magazine = 25
    colossus_burster_left.Packet = battleFrameToolConverter
    colossus_burster_left.Tile = InventoryTile.Tile84

    colossus_burster_right.Name = "colossus_burster_right"
    colossus_burster_right.Size = EquipmentSize.BFRArmWeapon
    colossus_burster_right.AmmoTypes += colossus_burster_ammo
    colossus_burster_right.ProjectileTypes += colossus_burster_projectile
    colossus_burster_right.FireModes += new FireModeDefinition
    colossus_burster_right.FireModes.head.AmmoTypeIndices += 0
    colossus_burster_right.FireModes.head.AmmoSlotIndex = 0
    colossus_burster_right.FireModes.head.Magazine = 25
    colossus_burster_right.Packet = battleFrameToolConverter
    colossus_burster_right.Tile = InventoryTile.Tile84

    colossus_chaingun.Name = "colossus_chaingun"
    colossus_chaingun.Size = EquipmentSize.BFRArmWeapon
    colossus_chaingun.AmmoTypes += colossus_chaingun_ammo
    colossus_chaingun.ProjectileTypes += colossus_chaingun_projectile
    colossus_chaingun.FireModes += new FireModeDefinition
    colossus_chaingun.FireModes.head.AmmoTypeIndices += 0
    colossus_chaingun.FireModes.head.AmmoSlotIndex = 0
    colossus_chaingun.FireModes.head.Magazine = 125
    colossus_chaingun.Packet = battleFrameToolConverter
    colossus_chaingun.Tile = InventoryTile.Tile84

    colossus_chaingun_left.Name = "colossus_chaingun_left"
    colossus_chaingun_left.Size = EquipmentSize.BFRArmWeapon
    colossus_chaingun_left.AmmoTypes += colossus_chaingun_ammo
    colossus_chaingun_left.ProjectileTypes += colossus_chaingun_projectile
    colossus_chaingun_left.FireModes += new FireModeDefinition
    colossus_chaingun_left.FireModes.head.AmmoTypeIndices += 0
    colossus_chaingun_left.FireModes.head.AmmoSlotIndex = 0
    colossus_chaingun_left.FireModes.head.Magazine = 125
    colossus_chaingun_left.Packet = battleFrameToolConverter
    colossus_chaingun_left.Tile = InventoryTile.Tile84

    colossus_chaingun_right.Name = "colossus_chaingun_right"
    colossus_chaingun_right.Size = EquipmentSize.BFRArmWeapon
    colossus_chaingun_right.AmmoTypes += colossus_chaingun_ammo
    colossus_chaingun_right.ProjectileTypes += colossus_chaingun_projectile
    colossus_chaingun_right.FireModes += new FireModeDefinition
    colossus_chaingun_right.FireModes.head.AmmoTypeIndices += 0
    colossus_chaingun_right.FireModes.head.AmmoSlotIndex = 0
    colossus_chaingun_right.FireModes.head.Magazine = 125
    colossus_chaingun_right.Packet = battleFrameToolConverter
    colossus_chaingun_right.Tile = InventoryTile.Tile84

    colossus_ntu_siphon.Name = "colossus_ntu_siphon"
    colossus_ntu_siphon.Size = EquipmentSize.BFRArmWeapon
    colossus_ntu_siphon.AmmoTypes += ntu_siphon_ammo
    colossus_ntu_siphon.ProjectileTypes += no_projectile
    colossus_ntu_siphon.ProjectileTypes += ntu_siphon_emp
    colossus_ntu_siphon.FireModes += new FireModeDefinition
    colossus_ntu_siphon.FireModes.head.AmmoTypeIndices += 0
    colossus_ntu_siphon.FireModes.head.AmmoSlotIndex = 0
    colossus_ntu_siphon.FireModes.head.RoundsPerShot = 5
    colossus_ntu_siphon.FireModes.head.Magazine = 150
    colossus_ntu_siphon.FireModes.head.DefaultMagazine = 0
    colossus_ntu_siphon.FireModes += new FireModeDefinition
    colossus_ntu_siphon.FireModes(1).AmmoTypeIndices += 0
    colossus_ntu_siphon.FireModes(1).AmmoSlotIndex = 0
    colossus_ntu_siphon.FireModes(1).ProjectileTypeIndices += 1
    colossus_ntu_siphon.FireModes(1).RoundsPerShot = 30
    colossus_ntu_siphon.FireModes(1).Magazine = 150
    colossus_ntu_siphon.FireModes(1).DefaultMagazine = 0
    colossus_ntu_siphon.FireModes(1).AmmoSlotIndex = 0
    colossus_ntu_siphon.Packet = battleFrameToolConverter
    colossus_ntu_siphon.Tile = InventoryTile.Tile84

    colossus_ntu_siphon_left.Name = "colossus_ntu_siphon_left"
    colossus_ntu_siphon_left.Size = EquipmentSize.BFRArmWeapon
    colossus_ntu_siphon_left.AmmoTypes += ntu_siphon_ammo
    colossus_ntu_siphon_left.ProjectileTypes += no_projectile
    colossus_ntu_siphon_left.ProjectileTypes += ntu_siphon_emp
    colossus_ntu_siphon_left.FireModes += new FireModeDefinition
    colossus_ntu_siphon_left.FireModes.head.AmmoTypeIndices += 0
    colossus_ntu_siphon_left.FireModes.head.AmmoSlotIndex = 0
    colossus_ntu_siphon_left.FireModes.head.RoundsPerShot = 5
    colossus_ntu_siphon_left.FireModes.head.Magazine = 150
    colossus_ntu_siphon_left.FireModes.head.DefaultMagazine = 0
    colossus_ntu_siphon_left.FireModes += new FireModeDefinition
    colossus_ntu_siphon_left.FireModes(1).AmmoTypeIndices += 0
    colossus_ntu_siphon_left.FireModes(1).AmmoSlotIndex = 0
    colossus_ntu_siphon_left.FireModes(1).ProjectileTypeIndices += 1
    colossus_ntu_siphon_left.FireModes(1).RoundsPerShot = 30
    colossus_ntu_siphon_left.FireModes(1).Magazine = 150
    colossus_ntu_siphon_left.FireModes(1).DefaultMagazine = 0
    colossus_ntu_siphon_left.FireModes(1).AmmoSlotIndex = 0
    colossus_ntu_siphon_left.Packet = battleFrameToolConverter
    colossus_ntu_siphon_left.Tile = InventoryTile.Tile84

    colossus_ntu_siphon_right.Name = "colossus_ntu_siphon_right"
    colossus_ntu_siphon_right.Size = EquipmentSize.BFRArmWeapon
    colossus_ntu_siphon_right.AmmoTypes += ntu_siphon_ammo
    colossus_ntu_siphon_right.ProjectileTypes += no_projectile
    colossus_ntu_siphon_right.ProjectileTypes += ntu_siphon_emp
    colossus_ntu_siphon_right.FireModes += new FireModeDefinition
    colossus_ntu_siphon_right.FireModes.head.AmmoTypeIndices += 0
    colossus_ntu_siphon_right.FireModes.head.AmmoSlotIndex = 0
    colossus_ntu_siphon_right.FireModes.head.RoundsPerShot = 5
    colossus_ntu_siphon_right.FireModes.head.Magazine = 150
    colossus_ntu_siphon_right.FireModes.head.DefaultMagazine = 0
    colossus_ntu_siphon_right.FireModes += new FireModeDefinition
    colossus_ntu_siphon_right.FireModes(1).AmmoTypeIndices += 0
    colossus_ntu_siphon_right.FireModes(1).AmmoSlotIndex = 0
    colossus_ntu_siphon_right.FireModes(1).ProjectileTypeIndices += 1
    colossus_ntu_siphon_right.FireModes(1).RoundsPerShot = 30
    colossus_ntu_siphon_right.FireModes(1).Magazine = 150
    colossus_ntu_siphon_right.FireModes(1).DefaultMagazine = 0
    colossus_ntu_siphon_right.FireModes(1).AmmoSlotIndex = 0
    colossus_ntu_siphon_right.Packet = battleFrameToolConverter
    colossus_ntu_siphon_right.Tile = InventoryTile.Tile84

    colossus_tank_cannon.Name = "colossus_tank_cannon"
    colossus_tank_cannon.Size = EquipmentSize.BFRArmWeapon
    colossus_tank_cannon.AmmoTypes += colossus_tank_cannon_ammo
    colossus_tank_cannon.ProjectileTypes += colossus_tank_cannon_projectile
    colossus_tank_cannon.FireModes += new FireModeDefinition
    colossus_tank_cannon.FireModes.head.AmmoTypeIndices += 0
    colossus_tank_cannon.FireModes.head.AmmoSlotIndex = 0
    colossus_tank_cannon.FireModes.head.Magazine = 25
    colossus_tank_cannon.Packet = battleFrameToolConverter
    colossus_tank_cannon.Tile = InventoryTile.Tile84

    colossus_tank_cannon_left.Name = "colossus_tank_cannon_left"
    colossus_tank_cannon_left.Size = EquipmentSize.BFRArmWeapon
    colossus_tank_cannon_left.AmmoTypes += colossus_tank_cannon_ammo
    colossus_tank_cannon_left.ProjectileTypes += colossus_tank_cannon_projectile
    colossus_tank_cannon_left.FireModes += new FireModeDefinition
    colossus_tank_cannon_left.FireModes.head.AmmoTypeIndices += 0
    colossus_tank_cannon_left.FireModes.head.AmmoSlotIndex = 0
    colossus_tank_cannon_left.FireModes.head.Magazine = 25
    colossus_tank_cannon_left.Packet = battleFrameToolConverter
    colossus_tank_cannon_left.Tile = InventoryTile.Tile84

    colossus_tank_cannon_right.Name = "colossus_tank_cannon_right"
    colossus_tank_cannon_right.Size = EquipmentSize.BFRArmWeapon
    colossus_tank_cannon_right.AmmoTypes += colossus_tank_cannon_ammo
    colossus_tank_cannon_right.ProjectileTypes += colossus_tank_cannon_projectile
    colossus_tank_cannon_right.FireModes += new FireModeDefinition
    colossus_tank_cannon_right.FireModes.head.AmmoTypeIndices += 0
    colossus_tank_cannon_right.FireModes.head.AmmoSlotIndex = 0
    colossus_tank_cannon_right.FireModes.head.Magazine = 25
    colossus_tank_cannon_right.Packet = battleFrameToolConverter
    colossus_tank_cannon_right.Tile = InventoryTile.Tile84

    colossus_dual_100mm_cannons.Name = "colossus_dual_100mm_cannons"
    colossus_dual_100mm_cannons.Size = EquipmentSize.BFRGunnerWeapon
    colossus_dual_100mm_cannons.AmmoTypes += colossus_100mm_cannon_ammo
    colossus_dual_100mm_cannons.ProjectileTypes += colossus_100mm_projectile
    colossus_dual_100mm_cannons.FireModes += new FireModeDefinition
    colossus_dual_100mm_cannons.FireModes.head.AmmoTypeIndices += 0
    colossus_dual_100mm_cannons.FireModes.head.AmmoSlotIndex = 0
    colossus_dual_100mm_cannons.FireModes.head.Magazine = 22
    colossus_dual_100mm_cannons.Packet = battleFrameToolConverter
    colossus_dual_100mm_cannons.Tile = InventoryTile.Tile1004

    colossus_cluster_bomb_pod.Name = "colossus_cluster_bomb_pod"
    colossus_cluster_bomb_pod.Size = EquipmentSize.BFRGunnerWeapon
    colossus_cluster_bomb_pod.AmmoTypes += colossus_cluster_bomb_ammo
    colossus_cluster_bomb_pod.ProjectileTypes += colossus_cluster_bomb_projectile
    colossus_cluster_bomb_pod.FireModes += new FireModeDefinition
    colossus_cluster_bomb_pod.FireModes.head.AmmoTypeIndices += 0
    colossus_cluster_bomb_pod.FireModes.head.AmmoSlotIndex = 0
    colossus_cluster_bomb_pod.FireModes.head.Magazine = 125
    colossus_cluster_bomb_pod.FireModes += new FireModeDefinition
    colossus_cluster_bomb_pod.FireModes(1).AmmoTypeIndices += 0
    colossus_cluster_bomb_pod.FireModes(1).AmmoSlotIndex = 0
    colossus_cluster_bomb_pod.FireModes(1).Magazine = 125
    colossus_cluster_bomb_pod.Packet = battleFrameToolConverter
    colossus_cluster_bomb_pod.Tile = InventoryTile.Tile1004

    peregrine_armor_siphon.Name = "peregrine_armor_siphon"
    peregrine_armor_siphon.Size = EquipmentSize.BFRArmWeapon
    peregrine_armor_siphon.AmmoTypes += armor_siphon_ammo
    peregrine_armor_siphon.ProjectileTypes += armor_siphon_projectile
    peregrine_armor_siphon.FireModes += new FireModeDefinition
    peregrine_armor_siphon.FireModes.head.AmmoTypeIndices += 0
    peregrine_armor_siphon.FireModes.head.AmmoSlotIndex = 0
    peregrine_armor_siphon.FireModes.head.Magazine = 100
    peregrine_armor_siphon.Packet = battleFrameToolConverter
    peregrine_armor_siphon.Tile = InventoryTile.Tile84

    peregrine_armor_siphon_left.Name = "peregrine_armor_siphon_left"
    peregrine_armor_siphon_left.Size = EquipmentSize.BFRArmWeapon
    peregrine_armor_siphon_left.AmmoTypes += armor_siphon_ammo
    peregrine_armor_siphon_left.ProjectileTypes += armor_siphon_projectile
    peregrine_armor_siphon_left.FireModes += new FireModeDefinition
    peregrine_armor_siphon_left.FireModes.head.AmmoTypeIndices += 0
    peregrine_armor_siphon_left.FireModes.head.AmmoSlotIndex = 0
    peregrine_armor_siphon_left.FireModes.head.Magazine = 100
    peregrine_armor_siphon_left.Packet = battleFrameToolConverter
    peregrine_armor_siphon_left.Tile = InventoryTile.Tile84

    peregrine_armor_siphon_right.Name = "peregrine_armor_siphon_right"
    peregrine_armor_siphon_right.Size = EquipmentSize.BFRArmWeapon
    peregrine_armor_siphon_right.AmmoTypes += armor_siphon_ammo
    peregrine_armor_siphon_right.ProjectileTypes += armor_siphon_projectile
    peregrine_armor_siphon_right.FireModes += new FireModeDefinition
    peregrine_armor_siphon_right.FireModes.head.AmmoTypeIndices += 0
    peregrine_armor_siphon_right.FireModes.head.AmmoSlotIndex = 0
    peregrine_armor_siphon_right.FireModes.head.Magazine = 100
    peregrine_armor_siphon_right.Packet = battleFrameToolConverter
    peregrine_armor_siphon_right.Tile = InventoryTile.Tile84

    peregrine_dual_machine_gun.Name = "peregrine_dual_machine_gun"
    peregrine_dual_machine_gun.Size = EquipmentSize.BFRArmWeapon
    peregrine_dual_machine_gun.AmmoTypes += peregrine_dual_machine_gun_ammo
    peregrine_dual_machine_gun.ProjectileTypes += peregrine_dual_machine_gun_projectile
    peregrine_dual_machine_gun.FireModes += new FireModeDefinition
    peregrine_dual_machine_gun.FireModes.head.AmmoTypeIndices += 0
    peregrine_dual_machine_gun.FireModes.head.AmmoSlotIndex = 0
    peregrine_dual_machine_gun.FireModes.head.Magazine = 55
    peregrine_dual_machine_gun.Packet = battleFrameToolConverter
    peregrine_dual_machine_gun.Tile = InventoryTile.Tile84

    peregrine_dual_machine_gun_left.Name = "peregrine_dual_machine_gun_left"
    peregrine_dual_machine_gun_left.Size = EquipmentSize.BFRArmWeapon
    peregrine_dual_machine_gun_left.AmmoTypes += peregrine_dual_machine_gun_ammo
    peregrine_dual_machine_gun_left.ProjectileTypes += peregrine_dual_machine_gun_projectile
    peregrine_dual_machine_gun_left.FireModes += new FireModeDefinition
    peregrine_dual_machine_gun_left.FireModes.head.AmmoTypeIndices += 0
    peregrine_dual_machine_gun_left.FireModes.head.AmmoSlotIndex = 0
    peregrine_dual_machine_gun_left.FireModes.head.Magazine = 55
    peregrine_dual_machine_gun_left.Packet = battleFrameToolConverter
    peregrine_dual_machine_gun_left.Tile = InventoryTile.Tile84

    peregrine_dual_machine_gun_right.Name = "peregrine_dual_machine_gun_right"
    peregrine_dual_machine_gun_right.Size = EquipmentSize.BFRArmWeapon
    peregrine_dual_machine_gun_right.AmmoTypes += peregrine_dual_machine_gun_ammo
    peregrine_dual_machine_gun_right.ProjectileTypes += peregrine_dual_machine_gun_projectile
    peregrine_dual_machine_gun_right.FireModes += new FireModeDefinition
    peregrine_dual_machine_gun_right.FireModes.head.AmmoTypeIndices += 0
    peregrine_dual_machine_gun_right.FireModes.head.AmmoSlotIndex = 0
    peregrine_dual_machine_gun_right.FireModes.head.Magazine = 55
    peregrine_dual_machine_gun_right.Packet = battleFrameToolConverter
    peregrine_dual_machine_gun_right.Tile = InventoryTile.Tile84

    peregrine_mechhammer.Name = "peregrine_mechhammer"
    peregrine_mechhammer.Size = EquipmentSize.BFRArmWeapon
    peregrine_mechhammer.AmmoTypes += peregrine_mechhammer_ammo
    peregrine_mechhammer.ProjectileTypes += peregrine_mechhammer_projectile
    peregrine_mechhammer.FireModes += new PelletFireModeDefinition
    peregrine_mechhammer.FireModes.head.AmmoTypeIndices += 0
    peregrine_mechhammer.FireModes.head.AmmoSlotIndex = 0
    peregrine_mechhammer.FireModes.head.Magazine = 30
    peregrine_mechhammer.FireModes.head.Chamber = 16 //30 shells * 12 pellets = 480
    peregrine_mechhammer.FireModes += new PelletFireModeDefinition
    peregrine_mechhammer.FireModes(1).AmmoTypeIndices += 0
    peregrine_mechhammer.FireModes(1).AmmoSlotIndex = 0
    peregrine_mechhammer.FireModes(1).Magazine = 30
    peregrine_mechhammer.FireModes(1).Chamber = 12 //30 shells * 12 pellets = 360
    peregrine_mechhammer.Packet = battleFrameToolConverter
    peregrine_mechhammer.Tile = InventoryTile.Tile84

    peregrine_mechhammer_left.Name = "peregrine_mechhammer_left"
    peregrine_mechhammer_left.Size = EquipmentSize.BFRArmWeapon
    peregrine_mechhammer_left.AmmoTypes += peregrine_mechhammer_ammo
    peregrine_mechhammer_left.ProjectileTypes += peregrine_mechhammer_projectile
    peregrine_mechhammer_left.FireModes += new PelletFireModeDefinition
    peregrine_mechhammer_left.FireModes.head.AmmoTypeIndices += 0
    peregrine_mechhammer_left.FireModes.head.AmmoSlotIndex = 0
    peregrine_mechhammer_left.FireModes.head.Magazine = 30
    peregrine_mechhammer_left.FireModes.head.Chamber = 16 //30 shells * 12 pellets = 480
    peregrine_mechhammer_left.FireModes += new PelletFireModeDefinition
    peregrine_mechhammer_left.FireModes(1).AmmoTypeIndices += 0
    peregrine_mechhammer_left.FireModes(1).AmmoSlotIndex = 0
    peregrine_mechhammer_left.FireModes(1).Magazine = 30
    peregrine_mechhammer_left.FireModes(1).Chamber = 12 //30 shells * 12 pellets = 360
    peregrine_mechhammer_left.Packet = battleFrameToolConverter
    peregrine_mechhammer_left.Tile = InventoryTile.Tile84

    peregrine_mechhammer_right.Name = "peregrine_mechhammer_right"
    peregrine_mechhammer_right.Size = EquipmentSize.BFRArmWeapon
    peregrine_mechhammer_right.AmmoTypes += peregrine_mechhammer_ammo
    peregrine_mechhammer_right.ProjectileTypes += peregrine_mechhammer_projectile
    peregrine_mechhammer_right.FireModes += new PelletFireModeDefinition
    peregrine_mechhammer_right.FireModes.head.AmmoTypeIndices += 0
    peregrine_mechhammer_right.FireModes.head.AmmoSlotIndex = 0
    peregrine_mechhammer_right.FireModes.head.Magazine = 30
    peregrine_mechhammer_right.FireModes.head.Chamber = 16 //30 shells * 12 pellets = 480
    peregrine_mechhammer_right.FireModes += new PelletFireModeDefinition
    peregrine_mechhammer_right.FireModes(1).AmmoTypeIndices += 0
    peregrine_mechhammer_right.FireModes(1).AmmoSlotIndex = 0
    peregrine_mechhammer_right.FireModes(1).Magazine = 30
    peregrine_mechhammer_right.FireModes(1).Chamber = 12 //30 shells * 12 pellets = 360
    peregrine_mechhammer_right.Packet = battleFrameToolConverter
    peregrine_mechhammer_right.Tile = InventoryTile.Tile84

    peregrine_ntu_siphon.Name = "peregrine_ntu_siphon"
    peregrine_ntu_siphon.Size = EquipmentSize.BFRArmWeapon
    peregrine_ntu_siphon.AmmoTypes += ntu_siphon_ammo
    peregrine_ntu_siphon.ProjectileTypes += no_projectile
    peregrine_ntu_siphon.ProjectileTypes += ntu_siphon_emp
    peregrine_ntu_siphon.FireModes += new FireModeDefinition
    peregrine_ntu_siphon.FireModes.head.AmmoTypeIndices += 0
    peregrine_ntu_siphon.FireModes.head.AmmoSlotIndex = 0
    peregrine_ntu_siphon.FireModes.head.RoundsPerShot = 5
    peregrine_ntu_siphon.FireModes.head.Magazine = 150
    peregrine_ntu_siphon.FireModes.head.DefaultMagazine = 0
    peregrine_ntu_siphon.FireModes += new FireModeDefinition
    peregrine_ntu_siphon.FireModes(1).AmmoTypeIndices += 0
    peregrine_ntu_siphon.FireModes(1).AmmoSlotIndex = 0
    peregrine_ntu_siphon.FireModes(1).ProjectileTypeIndices += 1
    peregrine_ntu_siphon.FireModes(1).RoundsPerShot = 30
    peregrine_ntu_siphon.FireModes(1).Magazine = 150
    peregrine_ntu_siphon.FireModes(1).DefaultMagazine = 0
    peregrine_ntu_siphon.FireModes(1).AmmoSlotIndex = 0
    peregrine_ntu_siphon.Packet = battleFrameToolConverter
    peregrine_ntu_siphon.Tile = InventoryTile.Tile84

    peregrine_ntu_siphon_left.Name = "peregrine_ntu_siphon_left"
    peregrine_ntu_siphon_left.Size = EquipmentSize.BFRArmWeapon
    peregrine_ntu_siphon_left.AmmoTypes += ntu_siphon_ammo
    peregrine_ntu_siphon_left.ProjectileTypes += no_projectile
    peregrine_ntu_siphon_left.ProjectileTypes += ntu_siphon_emp
    peregrine_ntu_siphon_left.FireModes += new FireModeDefinition
    peregrine_ntu_siphon_left.FireModes.head.AmmoTypeIndices += 0
    peregrine_ntu_siphon_left.FireModes.head.AmmoSlotIndex = 0
    peregrine_ntu_siphon_left.FireModes.head.RoundsPerShot = 5
    peregrine_ntu_siphon_left.FireModes.head.Magazine = 150
    peregrine_ntu_siphon_left.FireModes.head.DefaultMagazine = 0
    peregrine_ntu_siphon_left.FireModes += new FireModeDefinition
    peregrine_ntu_siphon_left.FireModes(1).AmmoTypeIndices += 0
    peregrine_ntu_siphon_left.FireModes(1).AmmoSlotIndex = 0
    peregrine_ntu_siphon_left.FireModes(1).ProjectileTypeIndices += 1
    peregrine_ntu_siphon_left.FireModes(1).RoundsPerShot = 30
    peregrine_ntu_siphon_left.FireModes(1).Magazine = 150
    peregrine_ntu_siphon_left.FireModes(1).DefaultMagazine = 0
    peregrine_ntu_siphon_left.FireModes(1).AmmoSlotIndex = 0
    peregrine_ntu_siphon_left.Packet = battleFrameToolConverter
    peregrine_ntu_siphon_left.Tile = InventoryTile.Tile84

    peregrine_ntu_siphon_right.Name = "peregrine_ntu_siphon_right"
    peregrine_ntu_siphon_right.Size = EquipmentSize.BFRArmWeapon
    peregrine_ntu_siphon_right.AmmoTypes += ntu_siphon_ammo
    peregrine_ntu_siphon_right.ProjectileTypes += no_projectile
    peregrine_ntu_siphon_right.ProjectileTypes += ntu_siphon_emp
    peregrine_ntu_siphon_right.FireModes += new FireModeDefinition
    peregrine_ntu_siphon_right.FireModes.head.AmmoTypeIndices += 0
    peregrine_ntu_siphon_right.FireModes.head.AmmoSlotIndex = 0
    peregrine_ntu_siphon_right.FireModes.head.RoundsPerShot = 5
    peregrine_ntu_siphon_right.FireModes.head.Magazine = 150
    peregrine_ntu_siphon_right.FireModes.head.DefaultMagazine = 0
    peregrine_ntu_siphon_right.FireModes += new FireModeDefinition
    peregrine_ntu_siphon_right.FireModes(1).AmmoTypeIndices += 0
    peregrine_ntu_siphon_right.FireModes(1).AmmoSlotIndex = 0
    peregrine_ntu_siphon_right.FireModes(1).ProjectileTypeIndices += 1
    peregrine_ntu_siphon_right.FireModes(1).RoundsPerShot = 30
    peregrine_ntu_siphon_right.FireModes(1).Magazine = 150
    peregrine_ntu_siphon_right.FireModes(1).DefaultMagazine = 0
    peregrine_ntu_siphon_right.FireModes(1).AmmoSlotIndex = 0
    peregrine_ntu_siphon_right.Packet = battleFrameToolConverter
    peregrine_ntu_siphon_right.Tile = InventoryTile.Tile84

    peregrine_sparrow.Name = "peregrine_sparrow"
    peregrine_sparrow.Size = EquipmentSize.BFRArmWeapon
    peregrine_sparrow.AmmoTypes += peregrine_sparrow_ammo
    peregrine_sparrow.ProjectileTypes += peregrine_sparrow_projectile
    peregrine_sparrow.FireModes += new FireModeDefinition
    peregrine_sparrow.FireModes.head.AmmoTypeIndices += 0
    peregrine_sparrow.FireModes.head.AmmoSlotIndex = 0
    peregrine_sparrow.FireModes.head.Magazine = 12
    peregrine_sparrow.Packet = battleFrameToolConverter
    peregrine_sparrow.Tile = InventoryTile.Tile84

    peregrine_sparrow_left.Name = "peregrine_sparrow_left"
    peregrine_sparrow_left.Size = EquipmentSize.BFRArmWeapon
    peregrine_sparrow_left.AmmoTypes += peregrine_sparrow_ammo
    peregrine_sparrow_left.ProjectileTypes += peregrine_sparrow_projectile
    peregrine_sparrow_left.FireModes += new FireModeDefinition
    peregrine_sparrow_left.FireModes.head.AmmoTypeIndices += 0
    peregrine_sparrow_left.FireModes.head.AmmoSlotIndex = 0
    peregrine_sparrow_left.FireModes.head.Magazine = 12
    peregrine_sparrow_left.Packet = battleFrameToolConverter
    peregrine_sparrow_left.Tile = InventoryTile.Tile84

    peregrine_sparrow_right.Name = "peregrine_sparrow_right"
    peregrine_sparrow_right.Size = EquipmentSize.BFRArmWeapon
    peregrine_sparrow_right.AmmoTypes += peregrine_sparrow_ammo
    peregrine_sparrow_right.ProjectileTypes += peregrine_sparrow_projectile
    peregrine_sparrow_right.FireModes += new FireModeDefinition
    peregrine_sparrow_right.FireModes.head.AmmoTypeIndices += 0
    peregrine_sparrow_right.FireModes.head.AmmoSlotIndex = 0
    peregrine_sparrow_right.FireModes.head.Magazine = 12
    peregrine_sparrow_right.Packet = battleFrameToolConverter
    peregrine_sparrow_right.Tile = InventoryTile.Tile84

    peregrine_particle_cannon.Name = "peregrine_particle_cannon"
    peregrine_particle_cannon.Size = EquipmentSize.BFRGunnerWeapon
    peregrine_particle_cannon.AmmoTypes += peregrine_particle_cannon_ammo
    peregrine_particle_cannon.ProjectileTypes += peregrine_particle_cannon_projectile
    peregrine_particle_cannon.FireModes += new FireModeDefinition
    peregrine_particle_cannon.FireModes.head.AmmoTypeIndices += 0
    peregrine_particle_cannon.FireModes.head.AmmoSlotIndex = 0
    peregrine_particle_cannon.FireModes.head.Magazine = 10
    peregrine_particle_cannon.Packet = battleFrameToolConverter
    peregrine_particle_cannon.Tile = InventoryTile.Tile1004

    peregrine_dual_rocket_pods.Name = "peregrine_dual_rocket_pods"
    peregrine_dual_rocket_pods.Size = EquipmentSize.BFRGunnerWeapon
    peregrine_dual_rocket_pods.AmmoTypes += peregrine_rocket_pod_ammo
    peregrine_dual_rocket_pods.ProjectileTypes += peregrine_rocket_pod_projectile
    peregrine_dual_rocket_pods.FireModes += new FireModeDefinition
    peregrine_dual_rocket_pods.FireModes.head.AmmoTypeIndices += 0
    peregrine_dual_rocket_pods.FireModes.head.AmmoSlotIndex = 0
    peregrine_dual_rocket_pods.FireModes.head.Magazine = 24
    peregrine_dual_rocket_pods.FireModes += new FireModeDefinition
    peregrine_dual_rocket_pods.FireModes(1).AmmoTypeIndices += 0
    peregrine_dual_rocket_pods.FireModes(1).AmmoSlotIndex = 0
    peregrine_dual_rocket_pods.FireModes(1).Magazine = 24
    peregrine_dual_rocket_pods.Packet = battleFrameToolConverter
    peregrine_dual_rocket_pods.Tile = InventoryTile.Tile1004
  }

  /**
    * Initialize `VehicleDefinition` globals.
    */
  private def init_vehicles(): Unit = {
    init_ground_vehicles()
    init_flight_vehicles()
    init_bfr_vehicles()
  }

  /**
    * Initialize land-based `VehicleDefinition` globals.
    */
  private def init_ground_vehicles(): Unit = {
    val atvForm       = GeometryForm.representByCylinder(radius = 1.1797f, height = 1.1875f) _
    val delivererForm = GeometryForm.representByCylinder(radius = 2.46095f, height = 2.40626f) _ //TODO hexahedron
    val apcForm       = GeometryForm.representByCylinder(radius = 4.6211f, height = 3.90626f) _  //TODO hexahedron

    val driverSeat = new SeatDefinition() {
      restriction = NoReinforcedOrMax
    }
    val normalSeat = new SeatDefinition()
    val bailableSeat = new SeatDefinition() {
      bailable = true
    }
    val maxOnlySeat = new SeatDefinition() {
      restriction = MaxOnly
    }

    val controlSubsystem = List(VehicleSubsystemEntry.Controls)

    fury.Name = "fury"
    fury.MaxHealth = 650
    fury.Damageable = true
    fury.Repairable = true
    fury.RepairIfDestroyed = false
    fury.MaxShields = 130
    fury.Seats += 0             -> bailableSeat
    fury.controlledWeapons(seat = 0, weapon = 1)
    fury.Weapons += 1           -> fury_weapon_systema
    fury.MountPoints += 1       -> MountInfo(0)
    fury.MountPoints += 2       -> MountInfo(0)
    fury.subsystems = controlSubsystem
    fury.TrunkSize = InventoryTile.Tile1111
    fury.TrunkOffset = 30
    fury.TrunkLocation = Vector3(-1.71f, 0f, 0f)
    fury.AutoPilotSpeeds = (24, 10)
    fury.DestroyedModel = Some(DestroyedVehicle.QuadAssault)
    fury.JackingDuration = Array(0, 10, 3, 2)
    fury.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 225
      DamageRadius = 5
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    fury.DrownAtMaxDepth = true
    fury.MaxDepth = 1.3f
    fury.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    fury.Geometry = atvForm
    fury.collision.avatarCollisionDamageMax = 35
    fury.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 5), (0.5f, 20), (0.75f, 40), (1f, 60)))
    fury.collision.z = CollisionZData(Array((8f, 1), (24f, 35), (40f, 100), (48f, 175), (52f, 350)))
    fury.maxForwardSpeed = 90f
    fury.mass = 32.1f

    quadassault.Name = "quadassault" // Basilisk
    quadassault.MaxHealth = 650
    quadassault.Damageable = true
    quadassault.Repairable = true
    quadassault.RepairIfDestroyed = false
    quadassault.MaxShields = 130
    quadassault.Seats += 0             -> bailableSeat
    quadassault.controlledWeapons(seat = 0, weapon = 1)
    quadassault.Weapons += 1           -> quadassault_weapon_system
    quadassault.MountPoints += 1       -> MountInfo(0)
    quadassault.MountPoints += 2       -> MountInfo(0)
    quadassault.subsystems = controlSubsystem
    quadassault.TrunkSize = InventoryTile.Tile1111
    quadassault.TrunkOffset = 30
    quadassault.TrunkLocation = Vector3(-1.71f, 0f, 0f)
    quadassault.AutoPilotSpeeds = (24, 10)
    quadassault.DestroyedModel = Some(DestroyedVehicle.QuadAssault)
    quadassault.JackingDuration = Array(0, 10, 3, 2)
    quadassault.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 225
      DamageRadius = 5
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    quadassault.DrownAtMaxDepth = true
    quadassault.MaxDepth = 1.3f
    quadassault.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    quadassault.Geometry = atvForm
    quadassault.collision.avatarCollisionDamageMax = 35
    quadassault.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 5), (0.5f, 20), (0.75f, 40), (1f, 60)))
    quadassault.collision.z = CollisionZData(Array((8f, 1), (24f, 35), (40f, 100), (48f, 175), (52f, 350)))
    quadassault.maxForwardSpeed = 90f
    quadassault.mass = 32.1f

    quadstealth.Name = "quadstealth" // Wraith
    quadstealth.MaxHealth = 650
    quadstealth.Damageable = true
    quadstealth.Repairable = true
    quadstealth.RepairIfDestroyed = false
    quadstealth.MaxShields = 130
    quadstealth.CanCloak = true
    quadstealth.Seats += 0 -> bailableSeat
    quadstealth.CanCloak = true
    quadstealth.MountPoints += 1 -> MountInfo(0)
    quadstealth.MountPoints += 2 -> MountInfo(0)
    quadstealth.subsystems = controlSubsystem
    quadstealth.TrunkSize = InventoryTile.Tile1111
    quadstealth.TrunkOffset = 30
    quadstealth.TrunkLocation = Vector3(-1.71f, 0f, 0f)
    quadstealth.AutoPilotSpeeds = (24, 10)
    quadstealth.DestroyedModel = Some(DestroyedVehicle.QuadStealth)
    quadstealth.JackingDuration = Array(0, 10, 3, 2)
    quadstealth.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 225
      DamageRadius = 5
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    quadstealth.DrownAtMaxDepth = true
    quadstealth.MaxDepth = 1.25f
    quadstealth.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    quadstealth.Geometry = atvForm
    quadstealth.collision.avatarCollisionDamageMax = 35
    quadstealth.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 5), (0.5f, 20), (0.75f, 40), (1f, 60)))
    quadstealth.collision.z = CollisionZData(Array((8f, 1), (24f, 35), (40f, 100), (48f, 175), (52f, 350)))
    quadstealth.maxForwardSpeed = 90f
    quadstealth.mass = 32.1f

    two_man_assault_buggy.Name = "two_man_assault_buggy" // Harasser
    two_man_assault_buggy.MaxHealth = 1250
    two_man_assault_buggy.Damageable = true
    two_man_assault_buggy.Repairable = true
    two_man_assault_buggy.RepairIfDestroyed = false
    two_man_assault_buggy.MaxShields = 250
    two_man_assault_buggy.Seats += 0             -> bailableSeat
    two_man_assault_buggy.Seats += 1             -> bailableSeat
    two_man_assault_buggy.controlledWeapons(seat = 1, weapon = 2)
    two_man_assault_buggy.Weapons += 2           -> chaingun_p
    two_man_assault_buggy.MountPoints += 1       -> MountInfo(0)
    two_man_assault_buggy.MountPoints += 2       -> MountInfo(1)
    two_man_assault_buggy.subsystems = controlSubsystem
    two_man_assault_buggy.TrunkSize = InventoryTile.Tile1511
    two_man_assault_buggy.TrunkOffset = 30
    two_man_assault_buggy.TrunkLocation = Vector3(-2.5f, 0f, 0f)
    two_man_assault_buggy.AutoPilotSpeeds = (22, 8)
    two_man_assault_buggy.DestroyedModel = Some(DestroyedVehicle.TwoManAssaultBuggy)
    two_man_assault_buggy.RadiationShielding = 0.5f
    two_man_assault_buggy.JackingDuration = Array(0, 15, 5, 3)
    two_man_assault_buggy.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    two_man_assault_buggy.DrownAtMaxDepth = true
    two_man_assault_buggy.MaxDepth = 1.5f
    two_man_assault_buggy.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    two_man_assault_buggy.Geometry = GeometryForm.representByCylinder(radius = 2.10545f, height = 1.59376f)
    two_man_assault_buggy.collision.avatarCollisionDamageMax = 75
    two_man_assault_buggy.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 5), (0.5f, 20), (0.75f, 40), (1f, 60)))
    two_man_assault_buggy.collision.z = CollisionZData(Array((7f, 1), (21f, 50), (35f, 150), (42f, 300), (45.5f, 600)))
    two_man_assault_buggy.maxForwardSpeed = 85f
    two_man_assault_buggy.mass = 52.4f

    skyguard.Name = "skyguard"
    skyguard.MaxHealth = 1000
    skyguard.Damageable = true
    skyguard.Repairable = true
    skyguard.RepairIfDestroyed = false
    skyguard.MaxShields = 200
    skyguard.Seats += 0             -> bailableSeat
    skyguard.Seats += 1             -> bailableSeat
    skyguard.controlledWeapons(seat = 1, weapon = 2)
    skyguard.Weapons += 2           -> skyguard_weapon_system
    skyguard.MountPoints += 1       -> MountInfo(0)
    skyguard.MountPoints += 2       -> MountInfo(0)
    skyguard.MountPoints += 3       -> MountInfo(1)
    skyguard.subsystems = controlSubsystem
    skyguard.TrunkSize = InventoryTile.Tile1511
    skyguard.TrunkOffset = 30
    skyguard.TrunkLocation = Vector3(2.5f, 0f, 0f)
    skyguard.AutoPilotSpeeds = (22, 8)
    skyguard.DestroyedModel = Some(DestroyedVehicle.Skyguard)
    skyguard.JackingDuration = Array(0, 15, 5, 3)
    skyguard.RadiationShielding = 0.5f
    skyguard.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    skyguard.DrownAtMaxDepth = true
    skyguard.MaxDepth = 1.5f
    skyguard.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    skyguard.Geometry = GeometryForm.representByCylinder(radius = 1.8867f, height = 1.4375f)
    skyguard.collision.avatarCollisionDamageMax = 100
    skyguard.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 5), (0.5f, 20), (0.75f, 40), (1f, 60)))
    skyguard.collision.z = CollisionZData(Array((7f, 1), (21f, 50), (35f, 150), (42f, 300), (45.4f, 600)))
    skyguard.maxForwardSpeed = 90f
    skyguard.mass = 78.9f

    threemanheavybuggy.Name = "threemanheavybuggy" // Marauder
    threemanheavybuggy.MaxHealth = 1700
    threemanheavybuggy.Damageable = true
    threemanheavybuggy.Repairable = true
    threemanheavybuggy.RepairIfDestroyed = false
    threemanheavybuggy.MaxShields = 340
    threemanheavybuggy.Seats += 0             -> bailableSeat
    threemanheavybuggy.Seats += 1             -> bailableSeat
    threemanheavybuggy.Seats += 2             -> bailableSeat
    threemanheavybuggy.controlledWeapons(seat = 1, weapon = 3)
    threemanheavybuggy.controlledWeapons(seat = 2, weapon = 4)
    threemanheavybuggy.Weapons += 3           -> chaingun_p
    threemanheavybuggy.Weapons += 4           -> grenade_launcher_marauder
    threemanheavybuggy.MountPoints += 1       -> MountInfo(0)
    threemanheavybuggy.MountPoints += 2       -> MountInfo(1)
    threemanheavybuggy.MountPoints += 3       -> MountInfo(2)
    threemanheavybuggy.subsystems = controlSubsystem
    threemanheavybuggy.TrunkSize = InventoryTile.Tile1511
    threemanheavybuggy.TrunkOffset = 30
    threemanheavybuggy.TrunkLocation = Vector3(3.01f, 0f, 0f)
    threemanheavybuggy.AutoPilotSpeeds = (22, 8)
    threemanheavybuggy.DestroyedModel = Some(DestroyedVehicle.ThreeManHeavyBuggy)
    threemanheavybuggy.Subtract.Damage1 = 5
    threemanheavybuggy.JackingDuration = Array(0, 20, 7, 5)
    threemanheavybuggy.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    threemanheavybuggy.DrownAtMaxDepth = true
    threemanheavybuggy.MaxDepth = 1.83f
    threemanheavybuggy.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    threemanheavybuggy.Geometry = GeometryForm.representByCylinder(radius = 2.1953f, height = 2.03125f)
    threemanheavybuggy.collision.avatarCollisionDamageMax = 100
    threemanheavybuggy.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 15), (0.5f, 30), (0.75f, 60), (1f, 80)))
    threemanheavybuggy.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 300), (39f, 900)))
    threemanheavybuggy.maxForwardSpeed = 80f
    threemanheavybuggy.mass = 96.3f

    twomanheavybuggy.Name = "twomanheavybuggy" // Enforcer
    twomanheavybuggy.MaxHealth = 1800
    twomanheavybuggy.Damageable = true
    twomanheavybuggy.Repairable = true
    twomanheavybuggy.RepairIfDestroyed = false
    twomanheavybuggy.MaxShields = 360
    twomanheavybuggy.Seats += 0             -> bailableSeat
    twomanheavybuggy.Seats += 1             -> bailableSeat
    twomanheavybuggy.controlledWeapons(seat = 1, weapon = 2)
    twomanheavybuggy.Weapons += 2           -> advanced_missile_launcher_t
    twomanheavybuggy.MountPoints += 1       -> MountInfo(0)
    twomanheavybuggy.MountPoints += 2       -> MountInfo(1)
    twomanheavybuggy.subsystems = controlSubsystem
    twomanheavybuggy.TrunkSize = InventoryTile.Tile1511
    twomanheavybuggy.TrunkOffset = 30
    twomanheavybuggy.TrunkLocation = Vector3(-0.23f, -2.05f, 0f)
    twomanheavybuggy.AutoPilotSpeeds = (22, 8)
    twomanheavybuggy.DestroyedModel = Some(DestroyedVehicle.TwoManHeavyBuggy)
    twomanheavybuggy.RadiationShielding = 0.5f
    twomanheavybuggy.Subtract.Damage1 = 5
    twomanheavybuggy.JackingDuration = Array(0, 20, 7, 5)
    twomanheavybuggy.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    twomanheavybuggy.DrownAtMaxDepth = true
    twomanheavybuggy.MaxDepth = 1.95f
    twomanheavybuggy.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    twomanheavybuggy.Geometry = GeometryForm.representByCylinder(radius = 2.60935f, height = 1.79688f)
    twomanheavybuggy.collision.avatarCollisionDamageMax = 100
    twomanheavybuggy.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 12), (0.5f, 30), (0.75f, 55), (1f, 80)))
    twomanheavybuggy.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 300), (39f, 900)))
    twomanheavybuggy.maxForwardSpeed = 80f
    twomanheavybuggy.mass = 83.2f

    twomanhoverbuggy.Name = "twomanhoverbuggy" // Thresher
    twomanhoverbuggy.MaxHealth = 1600
    twomanhoverbuggy.Damageable = true
    twomanhoverbuggy.Repairable = true
    twomanhoverbuggy.RepairIfDestroyed = false
    twomanhoverbuggy.MaxShields = 320
    twomanhoverbuggy.Seats += 0             -> bailableSeat
    twomanhoverbuggy.Seats += 1             -> bailableSeat
    twomanhoverbuggy.controlledWeapons(seat = 1, weapon = 2)
    twomanhoverbuggy.Weapons += 2           -> flux_cannon_thresher
    twomanhoverbuggy.MountPoints += 1       -> MountInfo(0)
    twomanhoverbuggy.MountPoints += 2       -> MountInfo(1)
    twomanhoverbuggy.subsystems = controlSubsystem
    twomanhoverbuggy.TrunkSize = InventoryTile.Tile1511
    twomanhoverbuggy.TrunkOffset = 30
    twomanhoverbuggy.TrunkLocation = Vector3(-3.39f, 0f, 0f)
    twomanhoverbuggy.AutoPilotSpeeds = (22, 10)
    twomanhoverbuggy.DestroyedModel = Some(DestroyedVehicle.TwoManHoverBuggy)
    twomanhoverbuggy.RadiationShielding = 0.5f
    twomanhoverbuggy.Subtract.Damage1 = 5
    twomanhoverbuggy.JackingDuration = Array(0, 20, 7, 5)
    twomanhoverbuggy.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    twomanhoverbuggy.DrownAtMaxDepth = true
    twomanhoverbuggy.UnderwaterLifespan(
      suffocation = 45000L,
      recovery = 5000L
    ) //but the thresher hovers over water, so ...?
    twomanhoverbuggy.Geometry = GeometryForm.representByCylinder(radius = 2.1875f, height = 2.01563f)
    twomanhoverbuggy.collision.avatarCollisionDamageMax = 125
    twomanhoverbuggy.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 13), (0.5f, 35), (0.75f, 65), (1f, 90)))
    twomanhoverbuggy.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 300), (39f, 900)))
    twomanhoverbuggy.maxForwardSpeed = 85f
    twomanhoverbuggy.mass = 55.5f

    mediumtransport.Name = "mediumtransport" // Deliverer
    mediumtransport.MaxHealth = 2500
    mediumtransport.Damageable = true
    mediumtransport.Repairable = true
    mediumtransport.RepairIfDestroyed = false
    mediumtransport.MaxShields = 500
    mediumtransport.Seats += 0             -> driverSeat
    mediumtransport.Seats += 1             -> normalSeat
    mediumtransport.Seats += 2             -> normalSeat
    mediumtransport.Seats += 3             -> normalSeat
    mediumtransport.Seats += 4             -> normalSeat
    mediumtransport.controlledWeapons(seat = 1, weapon = 5)
    mediumtransport.controlledWeapons(seat = 2, weapon = 6)
    mediumtransport.Weapons += 5           -> mediumtransport_weapon_systemA
    mediumtransport.Weapons += 6           -> mediumtransport_weapon_systemB
    mediumtransport.MountPoints += 1       -> MountInfo(0)
    mediumtransport.MountPoints += 2       -> MountInfo(1)
    mediumtransport.MountPoints += 3       -> MountInfo(2)
    mediumtransport.MountPoints += 4       -> MountInfo(3)
    mediumtransport.MountPoints += 5       -> MountInfo(4)
    mediumtransport.subsystems = controlSubsystem
    mediumtransport.TrunkSize = InventoryTile.Tile1515
    mediumtransport.TrunkOffset = 30
    mediumtransport.TrunkLocation = Vector3(-3.46f, 0f, 0f)
    mediumtransport.AutoPilotSpeeds = (18, 6)
    mediumtransport.DestroyedModel = Some(DestroyedVehicle.MediumTransport)
    mediumtransport.Subtract.Damage1 = 7
    mediumtransport.JackingDuration = Array(0, 25, 8, 5)
    mediumtransport.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    mediumtransport.DrownAtMaxDepth = false
    mediumtransport.MaxDepth = 1.2f
    mediumtransport.UnderwaterLifespan(suffocation = -1, recovery = -1)
    mediumtransport.Geometry = delivererForm
    mediumtransport.collision.avatarCollisionDamageMax = 120
    mediumtransport.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 35), (0.5f, 60), (0.75f, 110), (1f, 175)))
    mediumtransport.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 200), (30f, 750), (32.5f, 2000)))
    mediumtransport.maxForwardSpeed = 70f
    mediumtransport.mass = 108.5f

    battlewagon.Name = "battlewagon" // Raider
    battlewagon.MaxHealth = 2500
    battlewagon.Damageable = true
    battlewagon.Repairable = true
    battlewagon.RepairIfDestroyed = false
    battlewagon.MaxShields = 500
    battlewagon.Seats += 0             -> driverSeat
    battlewagon.Seats += 1             -> normalSeat
    battlewagon.Seats += 2             -> normalSeat
    battlewagon.Seats += 3             -> normalSeat
    battlewagon.Seats += 4             -> normalSeat
    battlewagon.controlledWeapons(seat = 1, weapon = 5)
    battlewagon.controlledWeapons(seat = 2, weapon = 6)
    battlewagon.controlledWeapons(seat = 3, weapon = 7)
    battlewagon.controlledWeapons(seat = 4, weapon = 8)
    battlewagon.Weapons += 5           -> battlewagon_weapon_systema
    battlewagon.Weapons += 6           -> battlewagon_weapon_systemb
    battlewagon.Weapons += 7           -> battlewagon_weapon_systemc
    battlewagon.Weapons += 8           -> battlewagon_weapon_systemd
    battlewagon.MountPoints += 1       -> MountInfo(0)
    battlewagon.MountPoints += 2       -> MountInfo(1)
    battlewagon.MountPoints += 3       -> MountInfo(2)
    battlewagon.MountPoints += 4       -> MountInfo(3)
    battlewagon.MountPoints += 5       -> MountInfo(4)
    battlewagon.subsystems = controlSubsystem
    battlewagon.TrunkSize = InventoryTile.Tile1515
    battlewagon.TrunkOffset = 30
    battlewagon.TrunkLocation = Vector3(-3.46f, 0f, 0f)
    battlewagon.AutoPilotSpeeds = (18, 6)
    battlewagon.DestroyedModel = Some(DestroyedVehicle.MediumTransport)
    battlewagon.JackingDuration = Array(0, 25, 8, 5)
    battlewagon.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    battlewagon.DrownAtMaxDepth = true
    battlewagon.MaxDepth = 1.2f
    battlewagon.UnderwaterLifespan(suffocation = -1, recovery = -1)
    battlewagon.Geometry = delivererForm
    battlewagon.collision.avatarCollisionDamageMax = 120
    battlewagon.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 35), (0.5f, 60), (0.75f, 110), (1f, 175))) //inherited from mediumtransport
    battlewagon.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 200), (30f, 750), (32.5f, 2000))) //inherited from mediumtransport
    battlewagon.maxForwardSpeed = 65f
    battlewagon.mass = 108.5f

    thunderer.Name = "thunderer"
    thunderer.MaxHealth = 2500
    thunderer.Damageable = true
    thunderer.Repairable = true
    thunderer.RepairIfDestroyed = false
    thunderer.MaxShields = 500
    thunderer.Seats += 0             -> driverSeat
    thunderer.Seats += 1             -> normalSeat
    thunderer.Seats += 2             -> normalSeat
    thunderer.Seats += 3             -> normalSeat
    thunderer.Seats += 4             -> normalSeat
    thunderer.Weapons += 5           -> thunderer_weapon_systema
    thunderer.Weapons += 6           -> thunderer_weapon_systemb
    thunderer.controlledWeapons(seat = 1, weapon = 5)
    thunderer.controlledWeapons(seat = 2, weapon = 6)
    thunderer.MountPoints += 1       -> MountInfo(0)
    thunderer.MountPoints += 2       -> MountInfo(1)
    thunderer.MountPoints += 3       -> MountInfo(2)
    thunderer.MountPoints += 4       -> MountInfo(3)
    thunderer.MountPoints += 5       -> MountInfo(4)
    thunderer.subsystems = controlSubsystem
    thunderer.TrunkSize = InventoryTile.Tile1515
    thunderer.TrunkOffset = 30
    thunderer.TrunkLocation = Vector3(-3.46f, 0f, 0f)
    thunderer.AutoPilotSpeeds = (18, 6)
    thunderer.DestroyedModel = Some(DestroyedVehicle.MediumTransport)
    thunderer.Subtract.Damage1 = 7
    thunderer.JackingDuration = Array(0, 25, 8, 5)
    thunderer.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    thunderer.DrownAtMaxDepth = true
    thunderer.MaxDepth = 1.2f
    thunderer.UnderwaterLifespan(suffocation = -1, recovery = -1)
    thunderer.Geometry = delivererForm
    thunderer.collision.avatarCollisionDamageMax = 120
    thunderer.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 35), (0.5f, 60), (0.75f, 110), (1f, 175)))
    thunderer.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 200), (30f, 750), (32.5f, 2000)))
    thunderer.maxForwardSpeed = 65f
    thunderer.mass = 108.5f

    aurora.Name = "aurora"
    aurora.MaxHealth = 2500
    aurora.Damageable = true
    aurora.Repairable = true
    aurora.RepairIfDestroyed = false
    aurora.MaxShields = 500
    aurora.Seats += 0             -> driverSeat
    aurora.Seats += 1             -> normalSeat
    aurora.Seats += 2             -> normalSeat
    aurora.Seats += 3             -> normalSeat
    aurora.Seats += 4             -> normalSeat
    aurora.controlledWeapons(seat = 1, weapon = 5)
    aurora.controlledWeapons(seat = 2, weapon = 6)
    aurora.Weapons += 5           -> aurora_weapon_systema
    aurora.Weapons += 6           -> aurora_weapon_systemb
    aurora.MountPoints += 1       -> MountInfo(0)
    aurora.MountPoints += 2       -> MountInfo(1)
    aurora.MountPoints += 3       -> MountInfo(2)
    aurora.MountPoints += 4       -> MountInfo(3)
    aurora.MountPoints += 5       -> MountInfo(4)
    aurora.subsystems = controlSubsystem
    aurora.TrunkSize = InventoryTile.Tile1515
    aurora.TrunkOffset = 30
    aurora.TrunkLocation = Vector3(-3.46f, 0f, 0f)
    aurora.AutoPilotSpeeds = (18, 6)
    aurora.DestroyedModel = Some(DestroyedVehicle.MediumTransport)
    aurora.Subtract.Damage1 = 7
    aurora.JackingDuration = Array(0, 25, 8, 5)
    aurora.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    aurora.DrownAtMaxDepth = true
    aurora.MaxDepth = 1.2f
    aurora.UnderwaterLifespan(suffocation = -1, recovery = -1)
    aurora.Geometry = delivererForm
    aurora.collision.avatarCollisionDamageMax = 120
    aurora.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 35), (0.5f, 60), (0.75f, 110), (1f, 175)))
    aurora.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 200), (30f, 750), (32.5f, 2000)))
    aurora.maxForwardSpeed = 65f
    aurora.mass = 108.5f

    apc_tr.Name = "apc_tr" // Juggernaut
    apc_tr.MaxHealth = 6000
    apc_tr.Damageable = true
    apc_tr.Repairable = true
    apc_tr.RepairIfDestroyed = false
    apc_tr.MaxShields = 1200
    apc_tr.Seats += 0             -> normalSeat
    apc_tr.Seats += 1             -> normalSeat
    apc_tr.Seats += 2             -> normalSeat
    apc_tr.Seats += 3             -> normalSeat
    apc_tr.Seats += 4             -> normalSeat
    apc_tr.Seats += 5             -> normalSeat
    apc_tr.Seats += 6             -> normalSeat
    apc_tr.Seats += 7             -> normalSeat
    apc_tr.Seats += 8             -> normalSeat
    apc_tr.Seats += 9             -> maxOnlySeat
    apc_tr.Seats += 10            -> maxOnlySeat
    apc_tr.controlledWeapons(seat = 1, weapon = 11)
    apc_tr.controlledWeapons(seat = 2, weapon = 12)
    apc_tr.controlledWeapons(seat = 5, weapon = 15)
    apc_tr.controlledWeapons(seat = 6, weapon = 16)
    apc_tr.controlledWeapons(seat = 7, weapon = 13)
    apc_tr.controlledWeapons(seat = 8, weapon = 14)
    apc_tr.Weapons += 11          -> apc_weapon_systemc_tr
    apc_tr.Weapons += 12          -> apc_weapon_systemb
    apc_tr.Weapons += 13          -> apc_weapon_systema
    apc_tr.Weapons += 14          -> apc_weapon_systemd_tr
    apc_tr.Weapons += 15          -> apc_ballgun_r
    apc_tr.Weapons += 16          -> apc_ballgun_l
    apc_tr.MountPoints += 1       -> MountInfo(0)
    apc_tr.MountPoints += 2       -> MountInfo(0)
    apc_tr.MountPoints += 3       -> MountInfo(1)
    apc_tr.MountPoints += 4       -> MountInfo(2)
    apc_tr.MountPoints += 5       -> MountInfo(3)
    apc_tr.MountPoints += 6       -> MountInfo(4)
    apc_tr.MountPoints += 7       -> MountInfo(5)
    apc_tr.MountPoints += 8       -> MountInfo(6)
    apc_tr.MountPoints += 9       -> MountInfo(7)
    apc_tr.MountPoints += 10      -> MountInfo(8)
    apc_tr.MountPoints += 11      -> MountInfo(9)
    apc_tr.MountPoints += 12      -> MountInfo(10)
    apc_tr.subsystems = controlSubsystem
    apc_tr.TrunkSize = InventoryTile.Tile2016
    apc_tr.TrunkOffset = 30
    apc_tr.TrunkLocation = Vector3(-5.82f, 0f, 0f)
    apc_tr.AutoPilotSpeeds = (16, 6)
    apc_tr.DestroyedModel = Some(DestroyedVehicle.Apc)
    apc_tr.JackingDuration = Array(0, 45, 15, 10)
    apc_tr.RadiationShielding = 0.5f
    apc_tr.Subtract.Damage1 = 10
    apc_tr.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 15
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    apc_tr.DrownAtMaxDepth = true
    apc_tr.MaxDepth = 3
    apc_tr.UnderwaterLifespan(suffocation = 15000L, recovery = 7500L)
    apc_tr.Geometry = apcForm
    apc_tr.MaxCapacitor = 300
    apc_tr.CapacitorRecharge = 10
    apc_tr.collision.avatarCollisionDamageMax = 300
    apc_tr.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 10), (0.5f, 40), (0.75f, 70), (1f, 110)))
    apc_tr.collision.z = CollisionZData(Array((2f, 1), (6f, 50), (10f, 300), (12f, 1000), (13f, 3000)))
    apc_tr.maxForwardSpeed = 60f
    apc_tr.mass = 128.4f

    apc_nc.Name = "apc_nc" // Vindicator
    apc_nc.MaxHealth = 6000
    apc_nc.Damageable = true
    apc_nc.Repairable = true
    apc_nc.RepairIfDestroyed = false
    apc_nc.MaxShields = 1200
    apc_nc.Seats += 0             -> normalSeat
    apc_nc.Seats += 1             -> normalSeat
    apc_nc.Seats += 2             -> normalSeat
    apc_nc.Seats += 3             -> normalSeat
    apc_nc.Seats += 4             -> normalSeat
    apc_nc.Seats += 5             -> normalSeat
    apc_nc.Seats += 6             -> normalSeat
    apc_nc.Seats += 7             -> normalSeat
    apc_nc.Seats += 8             -> normalSeat
    apc_nc.Seats += 9             -> maxOnlySeat
    apc_nc.Seats += 10            -> maxOnlySeat
    apc_nc.controlledWeapons(seat = 1, weapon = 11)
    apc_nc.controlledWeapons(seat = 2, weapon = 12)
    apc_nc.controlledWeapons(seat = 5, weapon = 15)
    apc_nc.controlledWeapons(seat = 6, weapon = 16)
    apc_nc.controlledWeapons(seat = 7, weapon = 13)
    apc_nc.controlledWeapons(seat = 8, weapon = 14)
    apc_nc.Weapons += 11          -> apc_weapon_systemc_nc
    apc_nc.Weapons += 12          -> apc_weapon_systemb
    apc_nc.Weapons += 13          -> apc_weapon_systema
    apc_nc.Weapons += 14          -> apc_weapon_systemd_nc
    apc_nc.Weapons += 15          -> apc_ballgun_r
    apc_nc.Weapons += 16          -> apc_ballgun_l
    apc_nc.MountPoints += 1       -> MountInfo(0)
    apc_nc.MountPoints += 2       -> MountInfo(0)
    apc_nc.MountPoints += 3       -> MountInfo(1)
    apc_nc.MountPoints += 4       -> MountInfo(2)
    apc_nc.MountPoints += 5       -> MountInfo(3)
    apc_nc.MountPoints += 6       -> MountInfo(4)
    apc_nc.MountPoints += 7       -> MountInfo(5)
    apc_nc.MountPoints += 8       -> MountInfo(6)
    apc_nc.MountPoints += 9       -> MountInfo(7)
    apc_nc.MountPoints += 10      -> MountInfo(8)
    apc_nc.MountPoints += 11      -> MountInfo(9)
    apc_nc.MountPoints += 12      -> MountInfo(10)
    apc_nc.subsystems = controlSubsystem
    apc_nc.TrunkSize = InventoryTile.Tile2016
    apc_nc.TrunkOffset = 30
    apc_nc.TrunkLocation = Vector3(-5.82f, 0f, 0f)
    apc_nc.AutoPilotSpeeds = (16, 6)
    apc_nc.DestroyedModel = Some(DestroyedVehicle.Apc)
    apc_nc.JackingDuration = Array(0, 45, 15, 10)
    apc_nc.RadiationShielding = 0.5f
    apc_nc.Subtract.Damage1 = 10
    apc_nc.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 15
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    apc_nc.DrownAtMaxDepth = true
    apc_nc.MaxDepth = 3
    apc_nc.UnderwaterLifespan(suffocation = 15000L, recovery = 7500L)
    apc_nc.Geometry = apcForm
    apc_nc.MaxCapacitor = 300
    apc_nc.CapacitorRecharge = 10
    apc_nc.collision.avatarCollisionDamageMax = 300
    apc_nc.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 10), (0.5f, 40), (0.75f, 70), (1f, 110)))
    apc_nc.collision.z = CollisionZData(Array((2f, 1), (6f, 50), (10f, 300), (12f, 1000), (13f, 3000)))
    apc_nc.maxForwardSpeed = 60f
    apc_nc.mass = 128.4f

    apc_vs.Name = "apc_vs" // Leviathan
    apc_vs.MaxHealth = 6000
    apc_vs.Damageable = true
    apc_vs.Repairable = true
    apc_vs.RepairIfDestroyed = false
    apc_vs.MaxShields = 1200
    apc_vs.Seats += 0             -> normalSeat
    apc_vs.Seats += 1             -> normalSeat
    apc_vs.Seats += 2             -> normalSeat
    apc_vs.Seats += 3             -> normalSeat
    apc_vs.Seats += 4             -> normalSeat
    apc_vs.Seats += 5             -> normalSeat
    apc_vs.Seats += 6             -> normalSeat
    apc_vs.Seats += 7             -> normalSeat
    apc_vs.Seats += 8             -> normalSeat
    apc_vs.Seats += 9             -> maxOnlySeat
    apc_vs.Seats += 10            -> maxOnlySeat
    apc_vs.controlledWeapons(seat = 1, weapon = 11)
    apc_vs.controlledWeapons(seat = 2, weapon = 12)
    apc_vs.controlledWeapons(seat = 5, weapon = 15)
    apc_vs.controlledWeapons(seat = 6, weapon = 16)
    apc_vs.controlledWeapons(seat = 7, weapon = 13)
    apc_vs.controlledWeapons(seat = 8, weapon = 14)
    apc_vs.Weapons += 11          -> apc_weapon_systemc_vs
    apc_vs.Weapons += 12          -> apc_weapon_systemb
    apc_vs.Weapons += 13          -> apc_weapon_systema
    apc_vs.Weapons += 14          -> apc_weapon_systemd_vs
    apc_vs.Weapons += 15          -> apc_ballgun_r
    apc_vs.Weapons += 16          -> apc_ballgun_l
    apc_vs.MountPoints += 1       -> MountInfo(0)
    apc_vs.MountPoints += 2       -> MountInfo(0)
    apc_vs.MountPoints += 3       -> MountInfo(1)
    apc_vs.MountPoints += 4       -> MountInfo(2)
    apc_vs.MountPoints += 5       -> MountInfo(3)
    apc_vs.MountPoints += 6       -> MountInfo(4)
    apc_vs.MountPoints += 7       -> MountInfo(5)
    apc_vs.MountPoints += 8       -> MountInfo(6)
    apc_vs.MountPoints += 9       -> MountInfo(7)
    apc_vs.MountPoints += 10      -> MountInfo(8)
    apc_vs.MountPoints += 11      -> MountInfo(9)
    apc_vs.MountPoints += 12      -> MountInfo(10)
    apc_vs.subsystems = controlSubsystem
    apc_vs.TrunkSize = InventoryTile.Tile2016
    apc_vs.TrunkOffset = 30
    apc_vs.TrunkLocation = Vector3(-5.82f, 0f, 0f)
    apc_vs.AutoPilotSpeeds = (16, 6)
    apc_vs.DestroyedModel = Some(DestroyedVehicle.Apc)
    apc_vs.JackingDuration = Array(0, 45, 15, 10)
    apc_vs.RadiationShielding = 0.5f
    apc_vs.Subtract.Damage1 = 10
    apc_vs.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 15
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    apc_vs.DrownAtMaxDepth = true
    apc_vs.MaxDepth = 3
    apc_vs.UnderwaterLifespan(suffocation = 15000L, recovery = 7500L)
    apc_vs.Geometry = apcForm
    apc_vs.MaxCapacitor = 300
    apc_vs.CapacitorRecharge = 10
    apc_vs.collision.avatarCollisionDamageMax = 300
    apc_vs.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 10), (0.5f, 40), (0.75f, 70), (1f, 110)))
    apc_vs.collision.z = CollisionZData(Array((2f, 1), (6f, 50), (10f, 300), (12f, 1000), (13f, 3000)))
    apc_vs.maxForwardSpeed = 60f
    apc_vs.mass = 128.4f

    lightning.Name = "lightning"
    lightning.MaxHealth = 2000
    lightning.Damageable = true
    lightning.Repairable = true
    lightning.RepairIfDestroyed = false
    lightning.MaxShields = 400
    lightning.Seats += 0             -> driverSeat
    lightning.controlledWeapons(seat = 0, weapon = 1)
    lightning.Weapons += 1           -> lightning_weapon_system
    lightning.MountPoints += 1       -> MountInfo(0)
    lightning.MountPoints += 2       -> MountInfo(0)
    lightning.subsystems = controlSubsystem
    lightning.TrunkSize = InventoryTile.Tile1511
    lightning.TrunkOffset = 30
    lightning.TrunkLocation = Vector3(-3f, 0f, 0f)
    lightning.AutoPilotSpeeds = (20, 8)
    lightning.DestroyedModel = Some(DestroyedVehicle.Lightning)
    lightning.RadiationShielding = 0.5f
    lightning.Subtract.Damage1 = 7
    lightning.JackingDuration = Array(0, 20, 7, 5)
    lightning.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    lightning.DrownAtMaxDepth = true
    lightning.MaxDepth = 1.38f
    lightning.UnderwaterLifespan(suffocation = 12000L, recovery = 6000L)
    lightning.Geometry = GeometryForm.representByCylinder(radius = 2.5078f, height = 1.79688f)
    lightning.collision.avatarCollisionDamageMax = 150
    lightning.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 10), (0.5f, 25), (0.75f, 50), (1f, 80)))
    lightning.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 300), (39f, 750)))
    lightning.maxForwardSpeed = 74f
    lightning.mass = 100.2f

    prowler.Name = "prowler"
    prowler.MaxHealth = 4800
    prowler.Damageable = true
    prowler.Repairable = true
    prowler.RepairIfDestroyed = false
    prowler.MaxShields = 960
    prowler.Seats += 0             -> driverSeat
    prowler.Seats += 1             -> normalSeat
    prowler.Seats += 2             -> normalSeat
    prowler.controlledWeapons(seat = 1, weapon = 3)
    prowler.controlledWeapons(seat = 2, weapon = 4)
    prowler.Weapons += 3           -> prowler_weapon_systemA
    prowler.Weapons += 4           -> prowler_weapon_systemB
    prowler.MountPoints += 1       -> MountInfo(0)
    prowler.MountPoints += 2       -> MountInfo(1)
    prowler.MountPoints += 3       -> MountInfo(2)
    prowler.subsystems = controlSubsystem
    prowler.TrunkSize = InventoryTile.Tile1511
    prowler.TrunkOffset = 30
    prowler.TrunkLocation = Vector3(-4.71f, 0f, 0f)
    prowler.AutoPilotSpeeds = (14, 6)
    prowler.DestroyedModel = Some(DestroyedVehicle.Prowler)
    prowler.RadiationShielding = 0.5f
    prowler.Subtract.Damage1 = 9
    prowler.JackingDuration = Array(0, 30, 10, 5)
    prowler.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    prowler.DrownAtMaxDepth = true
    prowler.MaxDepth = 3
    prowler.UnderwaterLifespan(suffocation = 12000L, recovery = 6000L)
    prowler.Geometry = GeometryForm.representByCylinder(radius = 3.461f, height = 3.48438f)
    prowler.collision.avatarCollisionDamageMax = 300
    prowler.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 15), (0.5f, 40), (0.75f, 75), (1f, 100)))
    prowler.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 250), (30f, 600), (32.5f, 1500)))
    prowler.maxForwardSpeed = 57f
    prowler.mass = 510.5f

    vanguard.Name = "vanguard"
    vanguard.MaxHealth = 5400
    vanguard.Damageable = true
    vanguard.Repairable = true
    vanguard.RepairIfDestroyed = false
    vanguard.MaxShields = 1080
    vanguard.Seats += 0             -> driverSeat
    vanguard.Seats += 1             -> normalSeat
    vanguard.controlledWeapons(seat = 1, weapon = 2)
    vanguard.Weapons += 2           -> vanguard_weapon_system
    vanguard.MountPoints += 1       -> MountInfo(0)
    vanguard.MountPoints += 2       -> MountInfo(1)
    vanguard.subsystems = controlSubsystem
    vanguard.TrunkSize = InventoryTile.Tile1511
    vanguard.TrunkOffset = 30
    vanguard.TrunkLocation = Vector3(-4.84f, 0f, 0f)
    vanguard.AutoPilotSpeeds = (16, 6)
    vanguard.DestroyedModel = Some(DestroyedVehicle.Vanguard)
    vanguard.RadiationShielding = 0.5f
    vanguard.Subtract.Damage1 = 9
    vanguard.JackingDuration = Array(0, 30, 10, 5)
    vanguard.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    vanguard.DrownAtMaxDepth = true
    vanguard.MaxDepth = 2.7f
    vanguard.UnderwaterLifespan(suffocation = 12000L, recovery = 6000L)
    vanguard.Geometry = GeometryForm.representByCylinder(radius = 3.8554f, height = 2.60938f)
    vanguard.collision.avatarCollisionDamageMax = 300
    vanguard.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 5), (0.5f, 20), (0.75f, 40), (1f, 60)))
    vanguard.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 100), (30f, 250), (32.5f, 600)))
    vanguard.maxForwardSpeed = 60f
    vanguard.mass = 460.4f

    magrider.Name = "magrider"
    magrider.MaxHealth = 4200
    magrider.Damageable = true
    magrider.Repairable = true
    magrider.RepairIfDestroyed = false
    magrider.MaxShields = 840
    magrider.Seats += 0             -> driverSeat
    magrider.Seats += 1             -> normalSeat
    magrider.controlledWeapons(seat = 0, weapon = 2)
    magrider.controlledWeapons(seat = 1, weapon = 3)
    magrider.Weapons += 2           -> particle_beam_magrider
    magrider.Weapons += 3           -> heavy_rail_beam_magrider
    magrider.MountPoints += 1       -> MountInfo(0)
    magrider.MountPoints += 2       -> MountInfo(1)
    magrider.subsystems = controlSubsystem
    magrider.TrunkSize = InventoryTile.Tile1511
    magrider.TrunkOffset = 30
    magrider.TrunkLocation = Vector3(5.06f, 0f, 0f)
    magrider.AutoPilotSpeeds = (18, 6)
    magrider.DestroyedModel = Some(DestroyedVehicle.Magrider)
    magrider.RadiationShielding = 0.5f
    magrider.Subtract.Damage1 = 9
    magrider.JackingDuration = Array(0, 30, 10, 5)
    magrider.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    magrider.DrownAtMaxDepth = true
    magrider.MaxDepth = 2
    magrider.UnderwaterLifespan(suffocation = 45000L, recovery = 5000L) //but the magrider hovers over water, so ...?
    magrider.Geometry = GeometryForm.representByCylinder(radius = 3.3008f, height = 3.26562f)
    magrider.collision.avatarCollisionDamageMax = 225
    magrider.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 35), (0.5f, 70), (0.75f, 90), (1f, 120)))
    magrider.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 250), (30f, 600), (32.5f, 1500)))
    magrider.maxForwardSpeed = 65f
    magrider.mass = 75.3f

    val utilityConverter = new UtilityVehicleConverter
    ant.Name = "ant"
    ant.MaxHealth = 2000
    ant.Damageable = true
    ant.Repairable = true
    ant.RepairIfDestroyed = false
    ant.MaxShields = 400
    ant.Seats += 0       -> driverSeat
    ant.MountPoints += 1 -> MountInfo(0)
    ant.MountPoints += 2 -> MountInfo(0)
    ant.subsystems = controlSubsystem
    ant.Deployment = true
    ant.DeployTime = 1500
    ant.UndeployTime = 1500
    ant.AutoPilotSpeeds = (18, 6)
    ant.MaxNtuCapacitor = 1500
    ant.Packet = utilityConverter
    ant.DestroyedModel = Some(DestroyedVehicle.Ant)
    ant.RadiationShielding = 0.5f
    ant.Subtract.Damage1 = 5
    ant.JackingDuration = Array(0, 60, 20, 15)
    ant.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    ant.DrownAtMaxDepth = true
    ant.MaxDepth = 2
    ant.UnderwaterLifespan(suffocation = 12000L, recovery = 6000L)
    ant.Geometry = GeometryForm.representByCylinder(radius = 2.16795f, height = 2.09376f) //TODO hexahedron
    ant.collision.avatarCollisionDamageMax = 50
    ant.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 10), (0.5f, 30), (0.75f, 50), (1f, 70)))
    ant.collision.z = CollisionZData(Array((2f, 1), (6f, 50), (10f, 250), (12f, 500), (13f, 750)))
    ant.maxForwardSpeed = 65f
    ant.mass = 80.5f

    ams.Name = "ams"
    ams.MaxHealth = 3000
    ams.Damageable = true
    ams.Repairable = true
    ams.RepairIfDestroyed = false
    ams.MaxShields = 600 + 1
    ams.Seats += 0       -> driverSeat
    ams.MountPoints += 1 -> MountInfo(0)
    ams.MountPoints += 2 -> MountInfo(0)
    ams.Utilities += 1   -> UtilityType.matrix_terminalc
    ams.Utilities += 2   -> UtilityType.ams_respawn_tube
    ams.Utilities += 3   -> UtilityType.order_terminala
    ams.Utilities += 4   -> UtilityType.order_terminalb
    ams.subsystems = controlSubsystem
    ams.Deployment = true
    ams.DeployTime = 2000
    ams.UndeployTime = 2000
    ams.DeconstructionTime = Some(20 minutes)
    ams.AutoPilotSpeeds = (18, 6)
    ams.Packet = utilityConverter
    ams.DestroyedModel = Some(DestroyedVehicle.Ams)
    ams.RadiationShielding = 0.5f
    ams.Subtract.Damage1 = 10
    ams.JackingDuration = Array(0, 60, 20, 15)
    ams.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.Splash
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 15
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    ams.DrownAtMaxDepth = true
    ams.MaxDepth = 3
    ams.UnderwaterLifespan(suffocation = 5000L, recovery = 5000L)
    ams.Geometry = GeometryForm.representByCylinder(radius = 3.0117f, height = 3.39062f) //TODO hexahedron
    ams.collision.avatarCollisionDamageMax = 250
    ams.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 10), (0.5f, 40), (0.75f, 60), (1f, 100)))
    ams.collision.z = CollisionZData(Array((2f, 1), (6f, 50), (10f, 250), (12f, 805), (13f, 3000)))
    ams.maxForwardSpeed = 70f
    ams.mass = 136.8f

    val variantConverter = new VariantVehicleConverter
    router.Name = "router"
    router.MaxHealth = 4000
    router.Damageable = true
    router.Repairable = true
    router.RepairIfDestroyed = false
    router.MaxShields = 800
    router.Seats += 0       -> normalSeat
    router.MountPoints += 1 -> MountInfo(0)
    router.Utilities += 1   -> UtilityType.teleportpad_terminal
    router.Utilities += 2   -> UtilityType.internal_router_telepad_deployable
    router.subsystems = controlSubsystem
    router.TrunkSize = InventoryTile.Tile1511
    router.TrunkOffset = 30
    router.TrunkLocation = Vector3(0f, 3.4f, 0f)
    router.Deployment = true
    router.DeployTime = 2000
    router.UndeployTime = 2000
    router.DeconstructionTime = Duration(20, "minutes")
    router.AutoPilotSpeeds = (16, 6)
    router.Packet = variantConverter
    router.DestroyedModel = Some(DestroyedVehicle.Router)
    router.RadiationShielding = 0.5f
    router.Subtract.Damage1 = 5
    router.JackingDuration = Array(0, 20, 7, 5)
    router.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    router.DrownAtMaxDepth = true
    router.MaxDepth = 2
    router.UnderwaterLifespan(suffocation = 45000L, recovery = 5000L) //but the router hovers over water, so ...?
    router.Geometry = GeometryForm.representByCylinder(radius = 3.64845f, height = 3.51563f) //TODO hexahedron
    router.collision.avatarCollisionDamageMax = 150 //it has to bonk you on the head when it falls?
    router.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 13), (0.5f, 35), (0.75f, 65), (1f, 90)))
    router.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 350), (39f, 900)))
    router.maxForwardSpeed = 60f
    router.mass = 60f

    switchblade.Name = "switchblade"
    switchblade.MaxHealth = 1750
    switchblade.Damageable = true
    switchblade.Repairable = true
    switchblade.RepairIfDestroyed = false
    switchblade.MaxShields = 350
    switchblade.Seats += 0             -> normalSeat
    switchblade.controlledWeapons(seat = 0, weapon = 1)
    switchblade.Weapons += 1           -> scythe
    switchblade.MountPoints += 1       -> MountInfo(0)
    switchblade.MountPoints += 2       -> MountInfo(0)
    switchblade.subsystems = controlSubsystem
    switchblade.TrunkSize = InventoryTile.Tile1511
    switchblade.TrunkOffset = 30
    switchblade.TrunkLocation = Vector3(-2.5f, 0f, 0f)
    switchblade.Deployment = true
    switchblade.DeployTime = 2000
    switchblade.UndeployTime = 2000
    switchblade.AutoPilotSpeeds = (22, 8)
    switchblade.Packet = variantConverter
    switchblade.DestroyedModel = Some(DestroyedVehicle.Switchblade)
    switchblade.RadiationShielding = 0.5f
    switchblade.Subtract.Damage0 = 5
    switchblade.Subtract.Damage1 = 5
    switchblade.JackingDuration = Array(0, 20, 7, 5)
    switchblade.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    switchblade.DrownAtMaxDepth = true
    switchblade.MaxDepth = 2
    switchblade.UnderwaterLifespan(
      suffocation = 45000L,
      recovery = 5000L
    ) //but the switchblade hovers over water, so ...?
    switchblade.Geometry = GeometryForm.representByCylinder(radius = 2.4335f, height = 2.73438f)
    switchblade.collision.avatarCollisionDamageMax = 35
    switchblade.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 13), (0.5f, 35), (0.75f, 65), (1f, 90)))
    switchblade.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 350), (39f, 800)))
    switchblade.maxForwardSpeed = 80f
    switchblade.mass = 63.9f

    flail.Name = "flail"
    flail.MaxHealth = 2400
    flail.Damageable = true
    flail.Repairable = true
    flail.RepairIfDestroyed = false
    flail.MaxShields = 480
    flail.Seats += 0             -> normalSeat
    flail.controlledWeapons(seat = 0, weapon = 1)
    flail.Weapons += 1           -> flail_weapon
    flail.Utilities += 2         -> UtilityType.targeting_laser_dispenser
    flail.MountPoints += 1       -> MountInfo(0)
    flail.subsystems = controlSubsystem
    flail.TrunkSize = InventoryTile.Tile1511
    flail.TrunkOffset = 30
    flail.TrunkLocation = Vector3(-3.75f, 0f, 0f)
    flail.Deployment = true
    flail.DeployTime = 5500
    flail.UndeployTime = 5500
    flail.AutoPilotSpeeds = (14, 6)
    flail.Packet = variantConverter
    flail.DestroyedModel = Some(DestroyedVehicle.Flail)
    flail.RadiationShielding = 0.5f
    flail.Subtract.Damage1 = 7
    flail.JackingDuration = Array(0, 20, 7, 5)
    flail.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    flail.DrownAtMaxDepth = true
    flail.MaxDepth = 2
    flail.UnderwaterLifespan(suffocation = 45000L, recovery = 5000L) //but the flail hovers over water, so ...?
    flail.Geometry = GeometryForm.representByCylinder(radius = 2.1875f, height = 2.21875f)
    flail.collision.avatarCollisionDamageMax = 175
    flail.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 12), (0.5f, 35), (0.75f, 65), (1f, 90)))
    flail.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 350), (39f, 900)))
    flail.maxForwardSpeed = 55f
    flail.mass = 73.5f
  }

  /**
    * Initialize flight `VehicleDefinition` globals.
    */
  private def init_flight_vehicles(): Unit = {
    val liberatorForm = GeometryForm.representByCylinder(radius = 3.74615f, height = 2.51563f) _
    val bailableSeat = new SeatDefinition() {
      bailable = true
    }

    val flightSubsystems = List(VehicleSubsystemEntry.Controls, VehicleSubsystemEntry.Ejection)

    val variantConverter = new VariantVehicleConverter
    mosquito.Name = "mosquito"
    mosquito.MaxHealth = 665
    mosquito.Damageable = true
    mosquito.Repairable = true
    mosquito.RepairIfDestroyed = false
    mosquito.MaxShields = 133
    mosquito.CanFly = true
    mosquito.Seats += 0             -> bailableSeat
    mosquito.controlledWeapons(seat = 0, weapon = 1)
    mosquito.Weapons += 1           -> rotarychaingun_mosquito
    mosquito.MountPoints += 1       -> MountInfo(0)
    mosquito.MountPoints += 2       -> MountInfo(0)
    mosquito.subsystems = flightSubsystems :+ VehicleSubsystemEntry.MosquitoRadar
    mosquito.TrunkSize = InventoryTile.Tile1111
    mosquito.TrunkOffset = 30
    mosquito.TrunkLocation = Vector3(-4.6f, 0f, 0f)
    mosquito.AutoPilotSpeeds = (0, 6)
    mosquito.Packet = variantConverter
    mosquito.DestroyedModel = Some(DestroyedVehicle.Mosquito)
    mosquito.JackingDuration = Array(0, 20, 7, 5)
    mosquito.RadiationShielding = 0.5f
    mosquito.DamageUsing = DamageCalculations.AgainstAircraft
    mosquito.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    mosquito.DrownAtMaxDepth = true
    mosquito.MaxDepth = 2 //flying vehicles will automatically disable
    mosquito.Geometry = GeometryForm.representByCylinder(radius = 2.72108f, height = 2.5f)
    mosquito.collision.avatarCollisionDamageMax = 50
    mosquito.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 50), (0.5f, 100), (0.75f, 150), (1f, 200)))
    mosquito.collision.z = CollisionZData(Array((3f, 1), (9f, 25), (15f, 50), (18f, 75), (19.5f, 100)))
    mosquito.maxForwardSpeed = 120f
    mosquito.mass = 53.6f

    lightgunship.Name = "lightgunship" // Reaver
    lightgunship.MaxHealth = 855 // Temporary - Correct Reaver Health from pre-"Coder Madness 2" Event
    lightgunship.Damageable = true
    lightgunship.Repairable = true
    lightgunship.RepairIfDestroyed = false
    lightgunship.MaxShields = 171 // Temporary - Correct Reaver Shields from pre-"Coder Madness 2" Event
    lightgunship.CanFly = true
    lightgunship.Seats += 0             -> bailableSeat
    lightgunship.controlledWeapons(seat = 0, weapon = 1)
    lightgunship.Weapons += 1           -> lightgunship_weapon_system
    lightgunship.MountPoints += 1       -> MountInfo(0)
    lightgunship.MountPoints += 2       -> MountInfo(0)
    lightgunship.subsystems = flightSubsystems
    lightgunship.TrunkSize = InventoryTile.Tile1511
    lightgunship.TrunkOffset = 30
    lightgunship.TrunkLocation = Vector3(-5.61f, 0f, 0f)
    lightgunship.AutoPilotSpeeds = (0, 4)
    lightgunship.Packet = variantConverter
    lightgunship.DestroyedModel = Some(DestroyedVehicle.LightGunship)
    lightgunship.RadiationShielding = 0.5f
    lightgunship.Subtract.Damage1 = 3
    lightgunship.JackingDuration = Array(0, 30, 10, 5)
    lightgunship.DamageUsing = DamageCalculations.AgainstAircraft
    lightgunship.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    lightgunship.DrownAtMaxDepth = true
    lightgunship.MaxDepth = 2 //flying vehicles will automatically disable
    lightgunship.Geometry = GeometryForm.representByCylinder(radius = 2.375f, height = 1.98438f)
    lightgunship.collision.avatarCollisionDamageMax = 750
    lightgunship.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 60), (0.5f, 120), (0.75f, 180), (1f, 250)))
    lightgunship.collision.z = CollisionZData(Array((3f, 1), (9f, 30), (15f, 60), (18f, 90), (19.5f, 125)))
    lightgunship.maxForwardSpeed = 104f
    lightgunship.mass = 51.1f

    wasp.Name = "wasp"
    wasp.MaxHealth = 515
    wasp.Damageable = true
    wasp.Repairable = true
    wasp.RepairIfDestroyed = false
    wasp.MaxShields = 103
    wasp.CanFly = true
    wasp.Seats += 0             -> bailableSeat
    wasp.controlledWeapons(seat = 0, weapon = 1)
    wasp.Weapons += 1           -> wasp_weapon_system
    wasp.MountPoints += 1       -> MountInfo(0)
    wasp.MountPoints += 2       -> MountInfo(0)
    wasp.subsystems = flightSubsystems
    wasp.TrunkSize = InventoryTile.Tile1111
    wasp.TrunkOffset = 30
    wasp.TrunkLocation = Vector3(-4.6f, 0f, 0f)
    wasp.AutoPilotSpeeds = (0, 6)
    wasp.Packet = variantConverter
    wasp.DestroyedModel = Some(DestroyedVehicle.Mosquito) //set_resource_parent wasp game_objects mosquito
    wasp.JackingDuration = Array(0, 20, 7, 5)
    wasp.DamageUsing = DamageCalculations.AgainstAircraft
    wasp.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    wasp.DrownAtMaxDepth = true
    wasp.MaxDepth = 2 //flying vehicles will automatically disable
    wasp.Geometry = GeometryForm.representByCylinder(radius = 2.88675f, height = 2.5f)
    wasp.collision.avatarCollisionDamageMax = 50 //mosquito numbers
    wasp.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 50), (0.5f, 100), (0.75f, 150), (1f, 200))) //mosquito numbers
    wasp.collision.z = CollisionZData(Array((3f, 1), (9f, 25), (15f, 50), (18f, 75), (19.5f, 100))) //mosquito numbers
    wasp.maxForwardSpeed = 120f
    wasp.mass = 53.6f

    liberator.Name = "liberator"
    liberator.MaxHealth = 2500
    liberator.Damageable = true
    liberator.Repairable = true
    liberator.RepairIfDestroyed = false
    liberator.MaxShields = 500
    liberator.CanFly = true
    liberator.Seats += 0             -> bailableSeat //new SeatDefinition()
    liberator.Seats += 1             -> bailableSeat
    liberator.Seats += 2             -> bailableSeat
    liberator.controlledWeapons(seat = 0, weapon = 3)
    liberator.controlledWeapons(seat = 1, weapon = 4)
    liberator.controlledWeapons(seat = 2, weapon = 5)
    liberator.Weapons += 3           -> liberator_weapon_system
    liberator.Weapons += 4           -> liberator_bomb_bay
    liberator.Weapons += 5           -> liberator_25mm_cannon
    liberator.MountPoints += 1       -> MountInfo(0)
    liberator.MountPoints += 2       -> MountInfo(1)
    liberator.MountPoints += 3       -> MountInfo(1)
    liberator.MountPoints += 4       -> MountInfo(2)
    liberator.subsystems = flightSubsystems
    liberator.TrunkSize = InventoryTile.Tile1515
    liberator.TrunkOffset = 30
    liberator.TrunkLocation = Vector3(-0.76f, -1.88f, 0f)
    liberator.AutoPilotSpeeds = (0, 4)
    liberator.Packet = variantConverter
    liberator.DestroyedModel = Some(DestroyedVehicle.Liberator)
    liberator.RadiationShielding = 0.5f
    liberator.Subtract.Damage1 = 5
    liberator.JackingDuration = Array(0, 30, 10, 5)
    liberator.DamageUsing = DamageCalculations.AgainstAircraft
    liberator.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    liberator.DrownAtMaxDepth = true
    liberator.MaxDepth = 2 //flying vehicles will automatically disable
    liberator.Geometry = liberatorForm
    liberator.collision.avatarCollisionDamageMax = 100
    liberator.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 60), (0.5f, 120), (0.75f, 180), (1f, 250)))
    liberator.collision.z = CollisionZData(Array((3f, 1), (9f, 30), (15f, 60), (18f, 90), (19.5f, 125)))
    liberator.maxForwardSpeed = 90f
    liberator.mass = 82f

    vulture.Name = "vulture"
    vulture.MaxHealth = 2500
    vulture.Damageable = true
    vulture.Repairable = true
    vulture.RepairIfDestroyed = false
    vulture.MaxShields = 500
    vulture.CanFly = true
    vulture.Seats += 0             -> bailableSeat //new SeatDefinition()
    vulture.Seats += 1             -> bailableSeat
    vulture.Seats += 2             -> bailableSeat
    vulture.controlledWeapons(seat = 0, weapon = 3)
    vulture.controlledWeapons(seat = 1, weapon = 4)
    vulture.controlledWeapons(seat = 2, weapon = 5)
    vulture.Weapons += 3           -> vulture_nose_weapon_system
    vulture.Weapons += 4           -> vulture_bomb_bay
    vulture.Weapons += 5           -> vulture_tail_cannon
    vulture.MountPoints += 1       -> MountInfo(0)
    vulture.MountPoints += 2       -> MountInfo(1)
    vulture.MountPoints += 3       -> MountInfo(1)
    vulture.MountPoints += 4       -> MountInfo(2)
    vulture.subsystems = flightSubsystems
    vulture.TrunkSize = InventoryTile.Tile1611
    vulture.TrunkOffset = 30
    vulture.TrunkLocation = Vector3(-0.76f, -1.88f, 0f)
    vulture.AutoPilotSpeeds = (0, 4)
    vulture.Packet = variantConverter
    vulture.DestroyedModel =
      Some(DestroyedVehicle.Liberator) //add_property vulture destroyedphysics liberator_destroyed
    vulture.RadiationShielding = 0.5f
    vulture.Subtract.Damage1 = 5
    vulture.JackingDuration = Array(0, 30, 10, 5)
    vulture.DamageUsing = DamageCalculations.AgainstAircraft
    vulture.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    vulture.DrownAtMaxDepth = true
    vulture.MaxDepth = 2 //flying vehicles will automatically disable
    vulture.Geometry = liberatorForm
    vulture.collision.avatarCollisionDamageMax = 100
    vulture.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 60), (0.5f, 120), (0.75f, 180), (1f, 250)))
    vulture.collision.z = CollisionZData(Array((3f, 1), (9f, 30), (15f, 60), (18f, 90), (19.5f, 125)))
    vulture.maxForwardSpeed = 97f
    vulture.mass = 82f

    dropship.Name = "dropship" // Galaxy
    dropship.MaxHealth = 5000
    dropship.Damageable = true
    dropship.Repairable = true
    dropship.RepairDistance = 20
    dropship.RepairIfDestroyed = false
    dropship.MaxShields = 1000
    dropship.CanFly = true
    dropship.Seats += 0 -> bailableSeat //new SeatDefinition()
    dropship.Seats += 1 -> bailableSeat
    dropship.Seats += 2 -> bailableSeat
    dropship.Seats += 3 -> bailableSeat
    dropship.Seats += 4 -> bailableSeat
    dropship.Seats += 5 -> bailableSeat
    dropship.Seats += 6 -> bailableSeat
    dropship.Seats += 7 -> bailableSeat
    dropship.Seats += 8 -> bailableSeat
    dropship.Seats += 9 -> new SeatDefinition() {
      bailable = true
      restriction = MaxOnly
    }
    dropship.Seats += 10 -> new SeatDefinition() {
      bailable = true
      restriction = MaxOnly
    }
    dropship.Seats += 11             -> bailableSeat
    dropship.controlledWeapons(seat = 1, weapon = 12)
    dropship.controlledWeapons(seat = 2, weapon = 13)
    dropship.controlledWeapons(seat = 11, weapon = 14)
    dropship.Weapons += 12           -> cannon_dropship_20mm
    dropship.Weapons += 13           -> cannon_dropship_20mm
    dropship.Weapons += 14           -> dropship_rear_turret
    dropship.Cargo += 15 -> new CargoDefinition() {
      restriction = SmallCargo
    }
    dropship.MountPoints += 1  -> MountInfo(0)
    dropship.MountPoints += 2  -> MountInfo(11)
    dropship.MountPoints += 3  -> MountInfo(1)
    dropship.MountPoints += 4  -> MountInfo(2)
    dropship.MountPoints += 5  -> MountInfo(3)
    dropship.MountPoints += 6  -> MountInfo(4)
    dropship.MountPoints += 7  -> MountInfo(5)
    dropship.MountPoints += 8  -> MountInfo(6)
    dropship.MountPoints += 9  -> MountInfo(7)
    dropship.MountPoints += 10 -> MountInfo(8)
    dropship.MountPoints += 11 -> MountInfo(9)
    dropship.MountPoints += 12 -> MountInfo(10)
    dropship.MountPoints += 13 -> MountInfo(15)
    dropship.subsystems = flightSubsystems
    dropship.TrunkSize = InventoryTile.Tile1612
    dropship.TrunkOffset = 30
    dropship.TrunkLocation = Vector3(-7.39f, -4.96f, 0f)
    dropship.AutoPilotSpeeds = (0, 4)
    dropship.Packet = variantConverter
    dropship.DestroyedModel = Some(DestroyedVehicle.Dropship)
    dropship.RadiationShielding = 0.5f
    dropship.Subtract.Damage1 = 7
    dropship.JackingDuration = Array(0, 60, 20, 10)
    dropship.DamageUsing = DamageCalculations.AgainstAircraft
    dropship.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 30
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    dropship.DrownAtMaxDepth = true
    dropship.MaxDepth = 2
    dropship.Geometry = GeometryForm.representByCylinder(radius = 10.52202f, height = 6.23438f)
    dropship.collision.avatarCollisionDamageMax = 300
    dropship.collision.xy = CollisionXYData(Array((0.1f, 5), (0.25f, 125), (0.5f, 250), (0.75f, 500), (1f, 1000)))
    dropship.collision.z = CollisionZData(Array((3f, 5), (9f, 125), (15f, 250), (18f, 500), (19.5f, 1000)))
    dropship.maxForwardSpeed = 80f
    dropship.mass = 133f

    galaxy_gunship.Name = "galaxy_gunship"
    galaxy_gunship.MaxHealth = 6000
    galaxy_gunship.Damageable = true
    galaxy_gunship.Repairable = true
    galaxy_gunship.RepairDistance = 20
    galaxy_gunship.RepairIfDestroyed = false
    galaxy_gunship.MaxShields = 1200
    galaxy_gunship.CanFly = true
    galaxy_gunship.Seats += 0             -> bailableSeat //new SeatDefinition()
    galaxy_gunship.Seats += 1             -> bailableSeat
    galaxy_gunship.Seats += 2             -> bailableSeat
    galaxy_gunship.Seats += 3             -> bailableSeat
    galaxy_gunship.Seats += 4             -> bailableSeat
    galaxy_gunship.Seats += 5             -> bailableSeat
    galaxy_gunship.controlledWeapons(seat = 1, weapon = 6)
    galaxy_gunship.controlledWeapons(seat = 2, weapon = 7)
    galaxy_gunship.controlledWeapons(seat = 3, weapon = 8)
    galaxy_gunship.controlledWeapons(seat = 4, weapon = 9)
    galaxy_gunship.controlledWeapons(seat = 5, weapon = 10)
    galaxy_gunship.Weapons += 6           -> galaxy_gunship_cannon
    galaxy_gunship.Weapons += 7           -> galaxy_gunship_cannon
    galaxy_gunship.Weapons += 8           -> galaxy_gunship_tailgun
    galaxy_gunship.Weapons += 9           -> galaxy_gunship_gun
    galaxy_gunship.Weapons += 10          -> galaxy_gunship_gun
    galaxy_gunship.MountPoints += 1       -> MountInfo(0)
    galaxy_gunship.MountPoints += 2       -> MountInfo(3)
    galaxy_gunship.MountPoints += 3       -> MountInfo(1)
    galaxy_gunship.MountPoints += 4       -> MountInfo(2)
    galaxy_gunship.MountPoints += 5       -> MountInfo(4)
    galaxy_gunship.MountPoints += 6       -> MountInfo(5)
    galaxy_gunship.subsystems = flightSubsystems
    galaxy_gunship.TrunkSize = InventoryTile.Tile1816
    galaxy_gunship.TrunkOffset = 30
    galaxy_gunship.TrunkLocation = Vector3(-9.85f, 0f, 0f)
    galaxy_gunship.AutoPilotSpeeds = (0, 4)
    galaxy_gunship.Packet = variantConverter
    galaxy_gunship.DestroyedModel =
      Some(DestroyedVehicle.Dropship) //the adb calls out a galaxy_gunship_destroyed but no such asset exists
    galaxy_gunship.RadiationShielding = 0.5f
    galaxy_gunship.Subtract.Damage1 = 7
    galaxy_gunship.JackingDuration = Array(0, 60, 20, 10)
    galaxy_gunship.DamageUsing = DamageCalculations.AgainstAircraft
    galaxy_gunship.Modifiers = GalaxyGunshipReduction(0.63f)
    galaxy_gunship.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 30
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    galaxy_gunship.DrownAtMaxDepth = true
    galaxy_gunship.MaxDepth = 2
    galaxy_gunship.Geometry = GeometryForm.representByCylinder(radius = 9.2382f, height = 5.01562f)
    galaxy_gunship.collision.avatarCollisionDamageMax = 300
    galaxy_gunship.collision.xy = CollisionXYData(Array((0.1f, 5), (0.25f, 125), (0.5f, 250), (0.75f, 500), (1f, 1000)))
    galaxy_gunship.collision.z = CollisionZData(Array((3f, 5), (9f, 125), (15f, 250), (18f, 500), (19.5f, 1000)))
    galaxy_gunship.maxForwardSpeed = 85f
    galaxy_gunship.mass = 133f

    lodestar.Name = "lodestar"
    lodestar.MaxHealth = 5000
    lodestar.Damageable = true
    lodestar.Repairable = true
    lodestar.RepairDistance = 20
    lodestar.RepairIfDestroyed = false
    lodestar.MaxShields = 1000
    lodestar.CanFly = true
    lodestar.Seats += 0         -> bailableSeat
    lodestar.MountPoints += 1   -> MountInfo(0)
    lodestar.MountPoints += 2   -> MountInfo(1)
    lodestar.Cargo += 1         -> new CargoDefinition()
    lodestar.Utilities += 2     -> UtilityType.lodestar_repair_terminal
    lodestar.UtilityOffset += 2 -> Vector3(0, 20, 0)
    lodestar.Utilities += 3     -> UtilityType.lodestar_repair_terminal
    lodestar.UtilityOffset += 3 -> Vector3(0, -20, 0)
    lodestar.Utilities += 4     -> UtilityType.multivehicle_rearm_terminal
    lodestar.UtilityOffset += 4 -> Vector3(0, 20, 0)
    lodestar.Utilities += 5     -> UtilityType.multivehicle_rearm_terminal
    lodestar.UtilityOffset += 5 -> Vector3(0, -20, 0)
    lodestar.Utilities += 6     -> UtilityType.bfr_rearm_terminal
    lodestar.UtilityOffset += 6 -> Vector3(0, 20, 0)
    lodestar.Utilities += 7     -> UtilityType.bfr_rearm_terminal
    lodestar.UtilityOffset += 7 -> Vector3(0, -20, 0)
    lodestar.subsystems = flightSubsystems
    lodestar.TrunkSize = InventoryTile.Tile1612
    lodestar.TrunkOffset = 30
    lodestar.TrunkLocation = Vector3(6.85f, -6.8f, 0f)
    lodestar.AutoPilotSpeeds = (0, 4)
    lodestar.Packet = variantConverter
    lodestar.DestroyedModel = Some(DestroyedVehicle.Lodestar)
    lodestar.RadiationShielding = 0.5f
    lodestar.Subtract.Damage1 = 7
    lodestar.JackingDuration = Array(0, 60, 20, 10)
    lodestar.DamageUsing = DamageCalculations.AgainstAircraft
    lodestar.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 30
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    lodestar.DrownAtMaxDepth = true
    lodestar.MaxDepth = 2
    lodestar.Geometry = GeometryForm.representByCylinder(radius = 7.8671f, height = 6.79688f) //TODO hexahedron
    lodestar.collision.avatarCollisionDamageMax = 300
    lodestar.collision.xy = CollisionXYData(Array((0.1f, 5), (0.25f, 125), (0.5f, 250), (0.75f, 500), (1f, 1000)))
    lodestar.collision.z = CollisionZData(Array((3f, 5), (9f, 125), (15f, 250), (18f, 500), (19.5f, 1000)))
    lodestar.maxForwardSpeed = 80f
    lodestar.mass = 128.2f

    phantasm.Name = "phantasm"
    phantasm.MaxHealth = 2500
    phantasm.Damageable = true
    phantasm.Repairable = true
    phantasm.RepairIfDestroyed = false
    phantasm.MaxShields = 500
    phantasm.CanCloak = true
    phantasm.CanFly = true
    phantasm.Seats += 0       -> bailableSeat
    phantasm.Seats += 1       -> bailableSeat
    phantasm.Seats += 2       -> bailableSeat
    phantasm.Seats += 3       -> bailableSeat
    phantasm.Seats += 4       -> bailableSeat
    phantasm.MountPoints += 1 -> MountInfo(0)
    phantasm.MountPoints += 2 -> MountInfo(1)
    phantasm.MountPoints += 3 -> MountInfo(2)
    phantasm.MountPoints += 4 -> MountInfo(3)
    phantasm.MountPoints += 5 -> MountInfo(4)
    phantasm.subsystems = flightSubsystems
    phantasm.TrunkSize = InventoryTile.Tile1107
    phantasm.TrunkOffset = 30
    phantasm.TrunkLocation = Vector3(-6.16f, 0f, 0f)
    phantasm.AutoPilotSpeeds = (0, 6)
    phantasm.Packet = variantConverter
    phantasm.DestroyedModel = None //the adb calls out a phantasm_destroyed but no such asset exists
    phantasm.JackingDuration = Array(0, 60, 20, 10)
    phantasm.RadiationShielding = 0.5f
    phantasm.DamageUsing = DamageCalculations.AgainstAircraft
    phantasm.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 100
      Damage1 = 150
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    phantasm.DrownAtMaxDepth = true
    phantasm.MaxDepth = 2
    phantasm.Geometry = GeometryForm.representByCylinder(radius = 5.2618f, height = 3f)
    phantasm.collision.avatarCollisionDamageMax = 100
    phantasm.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 60), (0.5f, 120), (0.75f, 180), (1f, 250)))
    phantasm.collision.z = CollisionZData(Array((3f, 1), (9f, 30), (15f, 60), (18f, 90), (19.5f, 125)))
    phantasm.maxForwardSpeed = 140f
    phantasm.mass = 100f

    droppod.Name = "droppod"
    droppod.MaxHealth = 20000
    droppod.Damageable = false
    droppod.Repairable = false
    droppod.CanFly = true
    droppod.Seats += 0 -> new SeatDefinition {
      restriction = Unrestricted
    }
    droppod.MountPoints += 1 -> MountInfo(0)
    droppod.TrunkSize = InventoryTile.None
    droppod.Packet = new DroppodConverter()
    droppod.DeconstructionTime = Some(5 seconds)
    droppod.DestroyedModel = None //the adb calls out a droppod; the cyclic nature of this confounds me
    droppod.DamageUsing = DamageCalculations.AgainstAircraft
    droppod.DrownAtMaxDepth = false
    droppod.mass = 2500f

    orbital_shuttle.Name = "orbital_shuttle"
    orbital_shuttle.MaxHealth = 20000
    orbital_shuttle.Damageable = false
    orbital_shuttle.Repairable = false
    orbital_shuttle.CanFly = true
    orbital_shuttle.CanBeOwned = None
    orbital_shuttle.undergoesDecay = false
    orbital_shuttle.Seats += 0 -> new SeatDefinition {
      occupancy = 300
      restriction = Unrestricted
    }
    /*
    these are close to the mount point offsets in the ADB;
    physically, they correlate to positions in the HART building rather than with the shuttle model by itself;
    set the shuttle pad based on the zonemap extraction values then position the shuttle relative to that pad;
    rotation based on the shuttle should place these offsets in the HART lobby whose gantry hall corresponds to that mount index
     */
    orbital_shuttle.MountPoints += 1 -> MountInfo(0, Vector3(-62, 4, -28.2f))
    orbital_shuttle.MountPoints += 2 -> MountInfo(0, Vector3(-62, 28, -28.2f))
    orbital_shuttle.MountPoints += 3 -> MountInfo(0, Vector3(-62, 4, -18.2f))
    orbital_shuttle.MountPoints += 4 -> MountInfo(0, Vector3(-62, 28, -18.2f))
    orbital_shuttle.MountPoints += 5 -> MountInfo(0, Vector3(62, 4, -28.2f))
    orbital_shuttle.MountPoints += 6 -> MountInfo(0, Vector3(62, 28, -28.2f))
    orbital_shuttle.MountPoints += 7 -> MountInfo(0, Vector3(62, 4, -18.2f))
    orbital_shuttle.MountPoints += 8 -> MountInfo(0, Vector3(62, 28, -18.2f))
    orbital_shuttle.TrunkSize = InventoryTile.None
    orbital_shuttle.Packet = new OrbitalShuttleConverter
    orbital_shuttle.DeconstructionTime = None
    orbital_shuttle.DestroyedModel = None
    orbital_shuttle.DamageUsing = DamageCalculations.AgainstNothing
    orbital_shuttle.DrownAtMaxDepth = false
    orbital_shuttle.mass = 25000f
  }

  private def init_bfr_vehicles(): Unit = {
    val driverSeat = new SeatDefinition() {
      restriction = NoReinforcedOrMax
    }
    val bailableSeat = new SeatDefinition() {
      restriction = NoReinforcedOrMax
      bailable = true
    }
    val normalSeat = new SeatDefinition()
    val bfrSubsystems = List(
      VehicleSubsystemEntry.BattleframeMovementServos,
      VehicleSubsystemEntry.BattleframeSensorArray,
      VehicleSubsystemEntry.BattleframeShieldGenerator,
      VehicleSubsystemEntry.BattleframeTrunk
    )
    val bfrGunnerSubsystems = List(
      VehicleSubsystemEntry.BattleframeLeftArm,
      VehicleSubsystemEntry.BattleframeRightArm,
      VehicleSubsystemEntry.BattleframeLeftWeapon,
      VehicleSubsystemEntry.BattleframeRightWeapon,
      VehicleSubsystemEntry.BattleframeGunnerWeapon
    ) ++ bfrSubsystems
    val bfrFlightSubsystems = List(
      VehicleSubsystemEntry.BattleframeFlightLeftArm,
      VehicleSubsystemEntry.BattleframeFlightRightArm,
      VehicleSubsystemEntry.BattleframeFlightLeftWeapon,
      VehicleSubsystemEntry.BattleframeFlightRightWeapon
    ) ++ bfrSubsystems ++ List(
      VehicleSubsystemEntry.BattleframeFlightPod
    )

    val battleFrameConverter = new BattleFrameRoboticsConverter
    aphelion_gunner.Name = "aphelion_gunner"
    aphelion_gunner.MaxHealth = 4500
    aphelion_gunner.Damageable = true
    aphelion_gunner.Repairable = true
    aphelion_gunner.RepairIfDestroyed = false
    aphelion_gunner.shieldUiAttribute = 79
    aphelion_gunner.MaxShields = 3000
    aphelion_gunner.ShieldPeriodicDelay = 500
    aphelion_gunner.ShieldDamageDelay = 3500
    aphelion_gunner.ShieldAutoRecharge = 45
    aphelion_gunner.ShieldAutoRechargeSpecial = 85
    aphelion_gunner.DefaultShields = aphelion_gunner.MaxShields
    aphelion_gunner.Seats += 0       -> driverSeat
    aphelion_gunner.Seats += 1       -> normalSeat
    aphelion_gunner.controlledWeapons(seat = 0, weapons = Set(2, 3))
    aphelion_gunner.controlledWeapons(seat = 1, weapon = 4)
    aphelion_gunner.Weapons += 2     -> aphelion_ppa_left
    aphelion_gunner.Weapons += 3     -> aphelion_ppa_right
    aphelion_gunner.Weapons += 4     -> aphelion_plasma_rocket_pod
    aphelion_gunner.MountPoints += 1 -> MountInfo(0)
    aphelion_gunner.MountPoints += 2 -> MountInfo(1)
    aphelion_gunner.subsystems = bfrGunnerSubsystems
    aphelion_gunner.TrunkSize = InventoryTile.Tile1518
    aphelion_gunner.TrunkOffset = 30
    aphelion_gunner.TrunkLocation = Vector3(0f, -2f, 0f)
    aphelion_gunner.AutoPilotSpeeds = (5, 1)
    aphelion_gunner.Packet = battleFrameConverter
    aphelion_gunner.DestroyedModel = None
    aphelion_gunner.destructionDelay = Some(4000L)
    aphelion_gunner.JackingDuration = Array(0, 62, 60, 30)
    aphelion_gunner.RadiationShielding = 0.5f
    aphelion_gunner.DamageUsing = DamageCalculations.AgainstBfr
    aphelion_gunner.Model = BfrResolutions.calculate
    aphelion_gunner.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 400
      Damage1 = 500
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    aphelion_gunner.DrownAtMaxDepth = true
    aphelion_gunner.MaxDepth = 5.09375f
    aphelion_gunner.UnderwaterLifespan(suffocation = 6000L, recovery = 6000L)
    aphelion_gunner.Geometry = GeometryForm.representByCylinder(radius = 1.2618f, height = 6.01562f)
    aphelion_gunner.collision.avatarCollisionDamageMax = 300
    aphelion_gunner.collision.xy = CollisionXYData(Array((0.2f, 1), (0.35f, 5), (0.55f, 20), (0.75f, 40), (1f, 60)))
    aphelion_gunner.collision.z = CollisionZData(Array((25f, 2), (40f, 4), (60f, 8), (85f, 16), (115f, 32)))
    aphelion_gunner.maxForwardSpeed = 17
    aphelion_gunner.mass = 615.1f

    colossus_gunner.Name = "colossus_gunner"
    colossus_gunner.MaxHealth = 4500
    colossus_gunner.Damageable = true
    colossus_gunner.Repairable = true
    colossus_gunner.RepairIfDestroyed = false
    colossus_gunner.shieldUiAttribute = 79
    colossus_gunner.MaxShields = 3000
    colossus_gunner.ShieldPeriodicDelay = 500
    colossus_gunner.ShieldDamageDelay = 3500
    colossus_gunner.ShieldAutoRecharge = 45
    colossus_gunner.ShieldAutoRechargeSpecial = 85
    colossus_gunner.DefaultShields = colossus_gunner.MaxShields
    colossus_gunner.Seats += 0       -> driverSeat
    colossus_gunner.Seats += 1       -> normalSeat
    colossus_gunner.controlledWeapons(seat = 0, weapons = Set(2, 3))
    colossus_gunner.controlledWeapons(seat = 1, weapon = 4)
    colossus_gunner.Weapons += 2     -> colossus_tank_cannon_left
    colossus_gunner.Weapons += 3     -> colossus_tank_cannon_right
    colossus_gunner.Weapons += 4     -> colossus_dual_100mm_cannons
    colossus_gunner.MountPoints += 1 -> MountInfo(0)
    colossus_gunner.MountPoints += 2 -> MountInfo(1)
    colossus_gunner.subsystems = bfrGunnerSubsystems
    colossus_gunner.TrunkSize = InventoryTile.Tile1518
    colossus_gunner.TrunkOffset = 30
    colossus_gunner.TrunkLocation = Vector3(0f, -5f, 0f)
    colossus_gunner.AutoPilotSpeeds = (5, 1)
    colossus_gunner.Packet = battleFrameConverter
    colossus_gunner.DestroyedModel = None
    colossus_gunner.destructionDelay = Some(4000L)
    colossus_gunner.JackingDuration = Array(0, 62, 60, 30)
    colossus_gunner.RadiationShielding = 0.5f
    colossus_gunner.DamageUsing = DamageCalculations.AgainstBfr
    colossus_gunner.Model = BfrResolutions.calculate
    colossus_gunner.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 400
      Damage1 = 500
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    colossus_gunner.DrownAtMaxDepth = true
    colossus_gunner.MaxDepth = 5.515625f
    colossus_gunner.UnderwaterLifespan(suffocation = 6000L, recovery = 6000L)
    colossus_gunner.Geometry = GeometryForm.representByCylinder(radius = 3.60935f, height = 5.984375f)
    colossus_gunner.collision.avatarCollisionDamageMax = 300
    colossus_gunner.collision.xy = CollisionXYData(Array((0.2f, 1), (0.35f, 5), (0.55f, 20), (0.75f, 40), (1f, 60)))
    colossus_gunner.collision.z = CollisionZData(Array((25f, 2), (40f, 4), (60f, 8), (85f, 16), (115f, 32)))
    colossus_gunner.maxForwardSpeed = 17
    colossus_gunner.mass = 709.7f

    peregrine_gunner.Name = "peregrine_gunner"
    peregrine_gunner.MaxHealth = 4500
    peregrine_gunner.Damageable = true
    peregrine_gunner.Repairable = true
    peregrine_gunner.RepairIfDestroyed = false
    peregrine_gunner.shieldUiAttribute = 79
    peregrine_gunner.MaxShields = 3000
    peregrine_gunner.ShieldPeriodicDelay = 500
    peregrine_gunner.ShieldDamageDelay = 3500
    peregrine_gunner.ShieldAutoRecharge = 45
    peregrine_gunner.ShieldAutoRechargeSpecial = 85
    peregrine_gunner.DefaultShields = peregrine_gunner.MaxShields
    peregrine_gunner.Seats += 0       -> driverSeat
    peregrine_gunner.Seats += 1       -> normalSeat
    peregrine_gunner.controlledWeapons(seat = 0, weapons = Set(2, 3))
    peregrine_gunner.controlledWeapons(seat = 1, weapon = 4)
    peregrine_gunner.Weapons += 2     -> peregrine_dual_machine_gun_left
    peregrine_gunner.Weapons += 3     -> peregrine_dual_machine_gun_right
    peregrine_gunner.Weapons += 4     -> peregrine_particle_cannon
    peregrine_gunner.MountPoints += 1 -> MountInfo(0)
    peregrine_gunner.MountPoints += 2 -> MountInfo(1)
    peregrine_gunner.subsystems = bfrGunnerSubsystems
    peregrine_gunner.TrunkSize = InventoryTile.Tile1518
    peregrine_gunner.TrunkOffset = 30
    peregrine_gunner.TrunkLocation = Vector3(0f, -5f, 0f)
    peregrine_gunner.AutoPilotSpeeds = (5, 1)
    peregrine_gunner.Packet = battleFrameConverter
    peregrine_gunner.DestroyedModel = None
    peregrine_gunner.destructionDelay = Some(4000L)
    peregrine_gunner.JackingDuration = Array(0, 62, 60, 30)
    peregrine_gunner.RadiationShielding = 0.5f
    peregrine_gunner.DamageUsing = DamageCalculations.AgainstBfr
    peregrine_gunner.Model = BfrResolutions.calculate
    peregrine_gunner.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 400
      Damage1 = 500
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    peregrine_gunner.DrownAtMaxDepth = true
    peregrine_gunner.MaxDepth = 6.03125f
    peregrine_gunner.UnderwaterLifespan(suffocation = 6000L, recovery = 6000L)
    peregrine_gunner.Geometry = GeometryForm.representByCylinder(radius = 3.60935f, height = 6.421875f)
    peregrine_gunner.collision.avatarCollisionDamageMax = 300
    peregrine_gunner.collision.xy = CollisionXYData(Array((0.2f, 1), (0.35f, 5), (0.55f, 20), (0.75f, 40), (1f, 60)))
    peregrine_gunner.collision.z = CollisionZData(Array((25f, 2), (40f, 4), (60f, 8), (85f, 16), (115f, 32)))
    peregrine_gunner.maxForwardSpeed = 17
    peregrine_gunner.mass = 713f

    val battleFrameFlightConverter = new BattleFrameFlightConverter
    aphelion_flight.Name = "aphelion_flight"
    aphelion_flight.MaxHealth = 3500
    aphelion_flight.Damageable = true
    aphelion_flight.Repairable = true
    aphelion_flight.RepairIfDestroyed = false
    aphelion_flight.CanFly = true
    aphelion_flight.shieldUiAttribute = 79
    aphelion_flight.MaxShields = 2500
    aphelion_flight.ShieldPeriodicDelay = 500
    aphelion_flight.ShieldDamageDelay = 3500
    aphelion_flight.ShieldAutoRecharge = 12 //12.5
    aphelion_flight.ShieldAutoRechargeSpecial = 25
    aphelion_flight.ShieldDrain = 30
    aphelion_flight.DefaultShields = aphelion_flight.MaxShields
    aphelion_flight.Seats += 0       -> bailableSeat
    aphelion_flight.controlledWeapons(seat = 0, weapons = Set(1, 2))
    aphelion_flight.Weapons += 1     -> aphelion_ppa_left
    aphelion_flight.Weapons += 2     -> aphelion_ppa_right
    aphelion_flight.MountPoints += 1 -> MountInfo(0)
    aphelion_flight.subsystems = bfrFlightSubsystems
    aphelion_flight.TrunkSize = InventoryTile.Tile1511
    aphelion_flight.TrunkOffset = 30
    aphelion_flight.TrunkLocation = Vector3(0f, -2f, 0f)
    aphelion_flight.AutoPilotSpeeds = (5, 1)
    aphelion_flight.Packet = battleFrameFlightConverter
    aphelion_flight.DestroyedModel = None
    aphelion_flight.destructionDelay = Some(4000L)
    aphelion_flight.JackingDuration = Array(0, 62, 60, 30)
    aphelion_flight.RadiationShielding = 0.5f
    aphelion_flight.DamageUsing = DamageCalculations.AgainstBfr
    aphelion_flight.Model = BfrResolutions.calculate
    aphelion_flight.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 400
      Damage1 = 500
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    aphelion_flight.DrownAtMaxDepth = true
    aphelion_flight.MaxDepth = 5.09375f
    aphelion_flight.UnderwaterLifespan(suffocation = 6000L, recovery = 6000L)
    aphelion_flight.Geometry = GeometryForm.representByCylinder(radius = 1.98045f, height = 6.03125f)
    aphelion_flight.MaxCapacitor = 156
    aphelion_flight.DefaultCapacitor = aphelion_flight.MaxCapacitor
    aphelion_flight.CapacitorDrain = 16
    aphelion_flight.CapacitorDrainSpecial = 3
    aphelion_flight.CapacitorRecharge = 42
    aphelion_flight.collision.avatarCollisionDamageMax = 300
    aphelion_flight.collision.xy = CollisionXYData(Array((0.2f, 1), (0.35f, 5), (0.55f, 20), (0.75f, 40), (1f, 60)))
    aphelion_flight.collision.z = CollisionZData(Array((25f, 2), (40f, 4), (60f, 8), (85f, 16), (115f, 32)))
    aphelion_flight.maxForwardSpeed = 35
    aphelion_flight.mass = 615.1f

    colossus_flight.Name = "colossus_flight"
    colossus_flight.MaxHealth = 3500
    colossus_flight.Damageable = true
    colossus_flight.Repairable = true
    colossus_flight.RepairIfDestroyed = false
    colossus_flight.CanFly = true
    colossus_flight.shieldUiAttribute = 79
    colossus_flight.MaxShields = 2500
    colossus_flight.ShieldPeriodicDelay = 500
    colossus_flight.ShieldDamageDelay = 3500
    colossus_flight.ShieldAutoRecharge = 12 //12.5
    colossus_flight.ShieldAutoRechargeSpecial = 25
    colossus_flight.ShieldDrain = 30
    colossus_flight.DefaultShields = colossus_flight.MaxShields
    colossus_flight.Seats += 0       -> bailableSeat
    colossus_flight.controlledWeapons(seat = 0, weapons = Set(1, 2))
    colossus_flight.Weapons += 1     -> colossus_tank_cannon_left
    colossus_flight.Weapons += 2     -> colossus_tank_cannon_right
    colossus_flight.MountPoints += 1 -> MountInfo(0)
    colossus_flight.subsystems = bfrFlightSubsystems
    colossus_flight.TrunkSize = InventoryTile.Tile1511
    colossus_flight.TrunkOffset = 30
    colossus_flight.TrunkLocation = Vector3(0f, -5f, 0f)
    colossus_flight.AutoPilotSpeeds = (5, 1)
    colossus_flight.Packet = battleFrameFlightConverter
    colossus_flight.DestroyedModel = None
    colossus_flight.destructionDelay = Some(4000L)
    colossus_flight.JackingDuration = Array(0, 62, 60, 30)
    colossus_flight.RadiationShielding = 0.5f
    colossus_flight.DamageUsing = DamageCalculations.AgainstBfr
    colossus_flight.Model = BfrResolutions.calculate
    colossus_flight.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 400
      Damage1 = 500
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    colossus_flight.DrownAtMaxDepth = true
    colossus_flight.MaxDepth = 5.515625f
    colossus_flight.UnderwaterLifespan(suffocation = 6000L, recovery = 6000L)
    colossus_flight.Geometry = GeometryForm.representByCylinder(radius = 3.60935f, height = 5.984375f)
    colossus_flight.MaxCapacitor = 156
    colossus_flight.DefaultCapacitor = aphelion_flight.MaxCapacitor
    colossus_flight.CapacitorDrain = 16
    colossus_flight.CapacitorDrainSpecial = 3
    colossus_flight.CapacitorRecharge = 42
    colossus_flight.collision.avatarCollisionDamageMax = 300
    colossus_flight.collision.xy = CollisionXYData(Array((0.2f, 1), (0.35f, 5), (0.55f, 20), (0.75f, 40), (1f, 60)))
    colossus_flight.collision.z = CollisionZData(Array((25f, 2), (40f, 4), (60f, 8), (85f, 16), (115f, 32)))
    colossus_flight.maxForwardSpeed = 34
    colossus_flight.mass = 709.7f

    peregrine_flight.Name = "peregrine_flight"
    peregrine_flight.MaxHealth = 3500
    peregrine_flight.Damageable = true
    peregrine_flight.Repairable = true
    peregrine_flight.RepairIfDestroyed = false
    peregrine_flight.CanFly = true
    peregrine_flight.shieldUiAttribute = 79
    peregrine_flight.MaxShields = 2500
    peregrine_flight.ShieldPeriodicDelay = 500
    peregrine_flight.ShieldDamageDelay = 3500
    peregrine_flight.ShieldAutoRecharge = 12 //12.5
    peregrine_flight.ShieldAutoRechargeSpecial = 25
    peregrine_flight.ShieldDrain = 30
    peregrine_flight.DefaultShields = peregrine_flight.MaxShields
    peregrine_flight.Seats += 0       -> bailableSeat
    peregrine_flight.controlledWeapons(seat = 0, weapons = Set(1, 2))
    peregrine_flight.Weapons += 1     -> peregrine_dual_machine_gun_left
    peregrine_flight.Weapons += 2     -> peregrine_dual_machine_gun_right
    peregrine_flight.MountPoints += 1 -> MountInfo(0)
    peregrine_flight.subsystems = bfrFlightSubsystems
    peregrine_flight.TrunkSize = InventoryTile.Tile1511
    peregrine_flight.TrunkOffset = 30
    peregrine_flight.TrunkLocation = Vector3(0f, -5f, 0f)
    peregrine_flight.AutoPilotSpeeds = (5, 1)
    peregrine_flight.Packet = battleFrameFlightConverter
    peregrine_flight.DestroyedModel = None
    peregrine_flight.destructionDelay = Some(4000L)
    peregrine_flight.JackingDuration = Array(0, 62, 60, 30)
    peregrine_flight.RadiationShielding = 0.5f
    peregrine_flight.DamageUsing = DamageCalculations.AgainstBfr
    peregrine_flight.Model = BfrResolutions.calculate
    peregrine_flight.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 400
      Damage1 = 500
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    peregrine_flight.DrownAtMaxDepth = true
    peregrine_flight.MaxDepth = 6.03125f
    peregrine_flight.UnderwaterLifespan(suffocation = 6000L, recovery = 6000L)
    peregrine_flight.Geometry = GeometryForm.representByCylinder(radius = 3.60935f, height = 6.421875f)
    peregrine_flight.MaxCapacitor = 156
    peregrine_flight.DefaultCapacitor = aphelion_flight.MaxCapacitor
    peregrine_flight.CapacitorDrain = 16
    peregrine_flight.CapacitorDrainSpecial = 3
    peregrine_flight.CapacitorRecharge = 42
    peregrine_flight.collision.avatarCollisionDamageMax = 300
    peregrine_flight.collision.xy = CollisionXYData(Array((0.2f, 1), (0.35f, 5), (0.55f, 20), (0.75f, 40), (1f, 60)))
    peregrine_flight.collision.z = CollisionZData(Array((25f, 2), (40f, 4), (60f, 8), (85f, 16), (115f, 32)))
    peregrine_flight.maxForwardSpeed = 35
    peregrine_flight.mass = 713f
  }

  /**
    * Initialize `Deployable` globals.
    */
  private def init_deployables(): Unit = {
    val mine = GeometryForm.representByCylinder(radius = 0.1914f, height = 0.0957f) _
    val smallTurret = GeometryForm.representByCylinder(radius = 0.48435f, height = 1.23438f) _
    val sensor = GeometryForm.representByCylinder(radius = 0.1914f, height = 1.21875f) _
    val largeTurret = GeometryForm.representByCylinder(radius = 0.8437f, height = 2.29687f) _

    boomer.Name = "boomer"
    boomer.Descriptor = "Boomers"
    boomer.MaxHealth = 100
    boomer.Damageable = true
    boomer.DamageableByFriendlyFire = false
    boomer.Repairable = false
    boomer.DeployCategory = DeployableCategory.Boomers
    boomer.DeployTime = Duration.create(1000, "ms")
    boomer.deployAnimation = DeployAnimation.Standard
    boomer.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.Splash
      SympatheticExplosion = true
      Damage0 = 250
      Damage1 = 750
      Damage2 = 400
      Damage3 = 400
      Damage4 = 1850
      DamageRadius = 5.1f
      DamageAtEdge = 0.1f
      Modifiers = ExplodingRadialDegrade
    }
    boomer.Geometry = mine

    he_mine.Name = "he_mine"
    he_mine.Descriptor = "Mines"
    he_mine.MaxHealth = 100
    he_mine.Damageable = true
    he_mine.DamageableByFriendlyFire = false
    he_mine.Repairable = false
    he_mine.DeployTime = Duration.create(1000, "ms")
    he_mine.deployAnimation = DeployAnimation.Standard
    he_mine.triggerRadius = 3f
    he_mine.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.Splash
      SympatheticExplosion = true
      Damage0 = 100
      Damage1 = 750
      Damage2 = 400
      Damage3 = 565
      Damage4 = 1600
      DamageRadius = 6.6f
      DamageAtEdge = 0.25f
      Modifiers = ExplodingRadialDegrade
    }
    he_mine.Geometry = mine

    jammer_mine.Name = "jammer_mine"
    jammer_mine.Descriptor = "JammerMines"
    jammer_mine.MaxHealth = 100
    jammer_mine.Damageable = true
    jammer_mine.DamageableByFriendlyFire = false
    jammer_mine.Repairable = false
    jammer_mine.DeployTime = Duration.create(1000, "ms")
    jammer_mine.deployAnimation = DeployAnimation.Standard
    jammer_mine.DetonateOnJamming = false
    jammer_mine.triggerRadius = 3f
    jammer_mine.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.Splash
      Damage0 = 0
      DamageRadius = 10f
      DamageAtEdge = 1.0f
      AdditionalEffect = true
      JammedEffectDuration += TargetValidation(
        EffectTarget.Category.Player,
        EffectTarget.Validation.Player
      ) -> 1000
      JammedEffectDuration += TargetValidation(
        EffectTarget.Category.Vehicle,
        EffectTarget.Validation.AMS
      ) -> 5000
      JammedEffectDuration += TargetValidation(
        EffectTarget.Category.Deployable,
        EffectTarget.Validation.MotionSensor
      ) -> 30000
      JammedEffectDuration += TargetValidation(
        EffectTarget.Category.Deployable,
        EffectTarget.Validation.Spitfire
      ) -> 30000
      JammedEffectDuration += TargetValidation(
        EffectTarget.Category.Turret,
        EffectTarget.Validation.Turret
      ) -> 30000
      JammedEffectDuration += TargetValidation(
        EffectTarget.Category.Vehicle,
        EffectTarget.Validation.VehicleNotAMS
      ) -> 10000
    }
    jammer_mine.Geometry = mine

    spitfire_turret.Name = "spitfire_turret"
    spitfire_turret.Descriptor = "Spitfires"
    spitfire_turret.MaxHealth = 100
    spitfire_turret.Damageable = true
    spitfire_turret.Repairable = true
    spitfire_turret.RepairIfDestroyed = false
    spitfire_turret.WeaponPaths += 1 -> new mutable.HashMap()
    spitfire_turret.WeaponPaths(1) += TurretUpgrade.None -> spitfire_weapon
    spitfire_turret.ReserveAmmunition = false
    spitfire_turret.DeployCategory = DeployableCategory.SmallTurrets
    spitfire_turret.DeployTime = Duration.create(5000, "ms")
    spitfire_turret.Model = ComplexDeployableResolutions.calculate
    spitfire_turret.deployAnimation = DeployAnimation.Standard
    spitfire_turret.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    spitfire_turret.Geometry = smallTurret
    spitfire_turret.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    spitfire_turret.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    spitfire_turret.mass = 5f

    spitfire_cloaked.Name = "spitfire_cloaked"
    spitfire_cloaked.Descriptor = "CloakingSpitfires"
    spitfire_cloaked.MaxHealth = 100
    spitfire_cloaked.Damageable = true
    spitfire_cloaked.Repairable = true
    spitfire_cloaked.RepairIfDestroyed = false
    spitfire_cloaked.WeaponPaths += 1 -> new mutable.HashMap()
    spitfire_cloaked.WeaponPaths(1) += TurretUpgrade.None -> spitfire_weapon
    spitfire_cloaked.ReserveAmmunition = false
    spitfire_cloaked.DeployCategory = DeployableCategory.SmallTurrets
    spitfire_cloaked.DeployTime = Duration.create(5000, "ms")
    spitfire_cloaked.deployAnimation = DeployAnimation.Standard
    spitfire_cloaked.Model = ComplexDeployableResolutions.calculate
    spitfire_cloaked.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 50
      Damage1 = 75
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    spitfire_cloaked.Geometry = smallTurret
    spitfire_cloaked.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    spitfire_cloaked.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    spitfire_cloaked.mass = 5f

    spitfire_aa.Name = "spitfire_aa"
    spitfire_aa.Descriptor = "FlakSpitfires"
    spitfire_aa.MaxHealth = 100
    spitfire_aa.Damageable = true
    spitfire_aa.Repairable = true
    spitfire_aa.RepairIfDestroyed = false
    spitfire_aa.WeaponPaths += 1 -> new mutable.HashMap()
    spitfire_aa.WeaponPaths(1) += TurretUpgrade.None -> spitfire_aa_weapon
    spitfire_aa.ReserveAmmunition = false
    spitfire_aa.DeployCategory = DeployableCategory.SmallTurrets
    spitfire_aa.DeployTime = Duration.create(5000, "ms")
    spitfire_aa.deployAnimation = DeployAnimation.Standard
    spitfire_aa.Model = ComplexDeployableResolutions.calculate
    spitfire_aa.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    spitfire_aa.Geometry = smallTurret
    spitfire_aa.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    spitfire_aa.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    spitfire_aa.mass = 5f

    motionalarmsensor.Name = "motionalarmsensor"
    motionalarmsensor.Descriptor = "MotionSensors"
    motionalarmsensor.MaxHealth = 100
    motionalarmsensor.Damageable = true
    motionalarmsensor.Repairable = true
    motionalarmsensor.RepairIfDestroyed = false
    motionalarmsensor.DeployTime = Duration.create(1000, "ms")
    motionalarmsensor.deployAnimation = DeployAnimation.Standard
    motionalarmsensor.Geometry = sensor

    sensor_shield.Name = "sensor_shield"
    sensor_shield.Descriptor = "SensorShields"
    sensor_shield.MaxHealth = 100
    sensor_shield.Damageable = true
    sensor_shield.Repairable = true
    sensor_shield.RepairIfDestroyed = false
    sensor_shield.DeployTime = Duration.create(5000, "ms")
    sensor_shield.deployAnimation = DeployAnimation.Standard
    sensor_shield.Geometry = sensor

    tank_traps.Name = "tank_traps"
    tank_traps.Descriptor = "TankTraps"
    tank_traps.MaxHealth = 5000
    tank_traps.Damageable = true
    tank_traps.Repairable = true
    tank_traps.RepairIfDestroyed = false
    tank_traps.DeployCategory = DeployableCategory.TankTraps
    tank_traps.DeployTime = Duration.create(6000, "ms")
    tank_traps.deployAnimation = DeployAnimation.Fdu
    //tank_traps do not explode
    tank_traps.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 10
      Damage1 = 10
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    tank_traps.Geometry = GeometryForm.representByCylinder(radius = 2.89680997f, height = 3.57812f)
    tank_traps.collision.xy = CollisionXYData(Array((0.01f, 5), (0.02f, 10), (0.03f, 15), (0.04f, 20), (0.05f, 25)))
    tank_traps.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    tank_traps.Modifiers = TrapCollisionDamageMultiplier(5f) //10f
    tank_traps.mass = 600f

    val fieldTurretConverter = new FieldTurretConverter
    portable_manned_turret.Name = "portable_manned_turret"
    portable_manned_turret.Descriptor = "FieldTurrets"
    portable_manned_turret.MaxHealth = 1000
    portable_manned_turret.Damageable = true
    portable_manned_turret.Repairable = true
    portable_manned_turret.RepairIfDestroyed = false
    portable_manned_turret.controlledWeapons(seat = 0, weapon = 1)
    portable_manned_turret.WeaponPaths += 1 -> new mutable.HashMap()
    portable_manned_turret.WeaponPaths(1) += TurretUpgrade.None -> energy_gun
    portable_manned_turret.MountPoints += 1 -> MountInfo(0)
    portable_manned_turret.MountPoints += 2 -> MountInfo(0)
    portable_manned_turret.ReserveAmmunition = true
    portable_manned_turret.FactionLocked = true
    portable_manned_turret.Packet = fieldTurretConverter
    portable_manned_turret.DeployCategory = DeployableCategory.FieldTurrets
    portable_manned_turret.DeployTime = Duration.create(6000, "ms")
    portable_manned_turret.deployAnimation = DeployAnimation.Fdu
    portable_manned_turret.Model = ComplexDeployableResolutions.calculate
    portable_manned_turret.RadiationShielding = 0.5f
    portable_manned_turret.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.1f
      Modifiers = ExplodingRadialDegrade
    }
    portable_manned_turret.Geometry = largeTurret
    portable_manned_turret.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    portable_manned_turret.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    portable_manned_turret.mass = 100f

    portable_manned_turret_nc.Name = "portable_manned_turret_nc"
    portable_manned_turret_nc.Descriptor = "FieldTurrets"
    portable_manned_turret_nc.MaxHealth = 1000
    portable_manned_turret_nc.Damageable = true
    portable_manned_turret_nc.Repairable = true
    portable_manned_turret_nc.RepairIfDestroyed = false
    portable_manned_turret_nc.WeaponPaths += 1 -> new mutable.HashMap()
    portable_manned_turret_nc.WeaponPaths(1) += TurretUpgrade.None -> energy_gun_nc
    portable_manned_turret_nc.controlledWeapons(seat = 0, weapon = 1)
    portable_manned_turret_nc.MountPoints += 1 -> MountInfo(0)
    portable_manned_turret_nc.MountPoints += 2 -> MountInfo(0)
    portable_manned_turret_nc.ReserveAmmunition = true
    portable_manned_turret_nc.FactionLocked = true
    portable_manned_turret_nc.Packet = fieldTurretConverter
    portable_manned_turret_nc.DeployCategory = DeployableCategory.FieldTurrets
    portable_manned_turret_nc.DeployTime = Duration.create(6000, "ms")
    portable_manned_turret_nc.deployAnimation = DeployAnimation.Fdu
    portable_manned_turret_nc.Model = ComplexDeployableResolutions.calculate
    portable_manned_turret_nc.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.1f
      Modifiers = ExplodingRadialDegrade
    }
    portable_manned_turret_nc.Geometry = largeTurret
    portable_manned_turret_nc.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    portable_manned_turret_nc.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    portable_manned_turret_nc.mass = 100f

    portable_manned_turret_tr.Name = "portable_manned_turret_tr"
    portable_manned_turret_tr.Descriptor = "FieldTurrets"
    portable_manned_turret_tr.MaxHealth = 1000
    portable_manned_turret_tr.Damageable = true
    portable_manned_turret_tr.Repairable = true
    portable_manned_turret_tr.RepairIfDestroyed = false
    portable_manned_turret_tr.WeaponPaths += 1 -> new mutable.HashMap()
    portable_manned_turret_tr.WeaponPaths(1) += TurretUpgrade.None -> energy_gun_tr
    portable_manned_turret_tr.controlledWeapons(seat = 0, weapon = 1)
    portable_manned_turret_tr.MountPoints += 1 -> MountInfo(0)
    portable_manned_turret_tr.MountPoints += 2 -> MountInfo(0)
    portable_manned_turret_tr.ReserveAmmunition = true
    portable_manned_turret_tr.FactionLocked = true
    portable_manned_turret_tr.Packet = fieldTurretConverter
    portable_manned_turret_tr.DeployCategory = DeployableCategory.FieldTurrets
    portable_manned_turret_tr.DeployTime = Duration.create(6000, "ms")
    portable_manned_turret_tr.deployAnimation = DeployAnimation.Fdu
    portable_manned_turret_tr.Model = ComplexDeployableResolutions.calculate
    portable_manned_turret_tr.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.1f
      Modifiers = ExplodingRadialDegrade
    }
    portable_manned_turret_tr.Geometry = largeTurret
    portable_manned_turret_tr.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    portable_manned_turret_tr.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    portable_manned_turret_tr.mass = 100f

    portable_manned_turret_vs.Name = "portable_manned_turret_vs"
    portable_manned_turret_vs.Descriptor = "FieldTurrets"
    portable_manned_turret_vs.MaxHealth = 1000
    portable_manned_turret_vs.Damageable = true
    portable_manned_turret_vs.Repairable = true
    portable_manned_turret_vs.RepairIfDestroyed = false
    portable_manned_turret_vs.WeaponPaths += 1 -> new mutable.HashMap()
    portable_manned_turret_vs.WeaponPaths(1) += TurretUpgrade.None -> energy_gun_vs
    portable_manned_turret_vs.controlledWeapons(seat = 0, weapon = 1)
    portable_manned_turret_vs.MountPoints += 1 -> MountInfo(0)
    portable_manned_turret_vs.MountPoints += 2 -> MountInfo(0)
    portable_manned_turret_vs.ReserveAmmunition = true
    portable_manned_turret_vs.FactionLocked = true
    portable_manned_turret_vs.Packet = fieldTurretConverter
    portable_manned_turret_vs.DeployCategory = DeployableCategory.FieldTurrets
    portable_manned_turret_vs.DeployTime = Duration.create(6000, "ms")
    portable_manned_turret_vs.deployAnimation = DeployAnimation.Fdu
    portable_manned_turret_vs.Model = ComplexDeployableResolutions.calculate
    portable_manned_turret_vs.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.1f
      Modifiers = ExplodingRadialDegrade
    }
    portable_manned_turret_vs.Geometry = largeTurret
    portable_manned_turret_vs.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    portable_manned_turret_vs.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    portable_manned_turret_vs.mass = 100f

    deployable_shield_generator.Name = "deployable_shield_generator"
    deployable_shield_generator.Descriptor = "ShieldGenerators"
    deployable_shield_generator.MaxHealth = 1700
    deployable_shield_generator.Damageable = true
    deployable_shield_generator.Repairable = true
    deployable_shield_generator.RepairIfDestroyed = false
    deployable_shield_generator.DeployTime = Duration.create(6000, "ms")
    deployable_shield_generator.deployAnimation = DeployAnimation.Fdu
    deployable_shield_generator.Model = ComplexDeployableResolutions.calculate
    deployable_shield_generator.Geometry = GeometryForm.representByCylinder(radius = 0.6562f, height = 2.17188f)

    router_telepad_deployable.Name = "router_telepad_deployable"
    router_telepad_deployable.MaxHealth = 100
    router_telepad_deployable.Damageable = true
    router_telepad_deployable.Repairable = false
    router_telepad_deployable.DeployTime = Duration.create(1, "ms")
    router_telepad_deployable.DeployCategory = DeployableCategory.Telepads
    router_telepad_deployable.Packet = new TelepadDeployableConverter
    router_telepad_deployable.Model = SimpleResolutions.calculate
    router_telepad_deployable.Geometry = GeometryForm.representByRaisedSphere(radius = 1.2344f)

    internal_router_telepad_deployable.Name = "router_telepad_deployable"
    internal_router_telepad_deployable.MaxHealth = 1
    internal_router_telepad_deployable.Damageable = false
    internal_router_telepad_deployable.Repairable = false
    internal_router_telepad_deployable.DeployTime = Duration.create(1, "ms")
    internal_router_telepad_deployable.DeployCategory = DeployableCategory.Telepads
    internal_router_telepad_deployable.Packet = new InternalTelepadDeployableConverter
  }

  /**
    * Initialize `Miscellaneous` globals.
    */
  private def initMiscellaneous(): Unit = {
    val vterm = GeometryForm.representByCylinder(radius = 1.03515f, height = 1.09374f) _

    ams_respawn_tube.Name = "ams_respawn_tube"
    ams_respawn_tube.Delay = 10 // Temporary -- Default value is 15
    ams_respawn_tube.SpecificPointFunc = SpawnPoint.AMS
    ams_respawn_tube.Damageable = false
    ams_respawn_tube.Repairable = false

    matrix_terminala.Name = "matrix_terminala"
    matrix_terminala.Damageable = false
    matrix_terminala.Repairable = false

    matrix_terminalb.Name = "matrix_terminalb"
    matrix_terminalb.Damageable = false
    matrix_terminalb.Repairable = false

    matrix_terminalc.Name = "matrix_terminalc"
    matrix_terminalc.Damageable = false
    matrix_terminalc.Repairable = false

    spawn_terminal.Name = "spawn_terminal"
    spawn_terminal.Damageable = false
    spawn_terminal.Repairable = false
    spawn_terminal.autoRepair = AutoRepairStats(1, 5000, 200, 1) //TODO amount and drain are default? undefined?

    order_terminal.Name = "order_terminal"
    order_terminal.Tab += 0 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.infantryAmmunition ++ EquipmentTerminalDefinition.infantryWeapons
    )
    order_terminal.Tab += 1 -> OrderTerminalDefinition.ArmorWithAmmoPage(
      EquipmentTerminalDefinition.suits ++ EquipmentTerminalDefinition.maxSuits,
      EquipmentTerminalDefinition.maxAmmo
    )
    order_terminal.Tab += 2 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.supportAmmunition ++ EquipmentTerminalDefinition.supportWeapons
    )
    order_terminal.Tab += 3 -> OrderTerminalDefinition.EquipmentPage(EquipmentTerminalDefinition.vehicleAmmunition)
    order_terminal.Tab += 4 -> OrderTerminalDefinition.InfantryLoadoutPage()
    order_terminal.SellEquipmentByDefault = true
    order_terminal.MaxHealth = 500
    order_terminal.Damageable = true
    order_terminal.Repairable = true
    order_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.5f)
    order_terminal.RepairIfDestroyed = true
    order_terminal.Subtract.Damage1 = 8
    order_terminal.Geometry = GeometryForm.representByCylinder(radius = 0.8438f, height = 1.3f)

    order_terminala.Name = "order_terminala"
    order_terminala.Tab += 0 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.infantryAmmunition ++ EquipmentTerminalDefinition.infantryWeapons
    )
    order_terminala.Tab += 1 -> OrderTerminalDefinition.ArmorWithAmmoPage(
      EquipmentTerminalDefinition.suits,
      EquipmentTerminalDefinition.maxAmmo
    )
    order_terminala.Tab += 2 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.supportAmmunition ++ EquipmentTerminalDefinition.supportWeapons
    )
    order_terminala.Tab += 3 -> OrderTerminalDefinition.EquipmentPage(EquipmentTerminalDefinition.vehicleAmmunition)
    order_terminala.Tab += 4 -> OrderTerminalDefinition.InfantryLoadoutPage()
    order_terminala.Tab(4).asInstanceOf[OrderTerminalDefinition.InfantryLoadoutPage].Exclude = ExoSuitType.MAX
    order_terminala.SellEquipmentByDefault = true
    order_terminala.Damageable = false
    order_terminala.Repairable = false

    order_terminalb.Name = "order_terminalb"
    order_terminalb.Tab += 0 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.infantryAmmunition ++ EquipmentTerminalDefinition.infantryWeapons
    )
    order_terminalb.Tab += 1 -> OrderTerminalDefinition.ArmorWithAmmoPage(
      EquipmentTerminalDefinition.suits,
      EquipmentTerminalDefinition.maxAmmo
    )
    order_terminalb.Tab += 2 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.supportAmmunition ++ EquipmentTerminalDefinition.supportWeapons
    )
    order_terminalb.Tab += 3 -> OrderTerminalDefinition.EquipmentPage(EquipmentTerminalDefinition.vehicleAmmunition)
    order_terminalb.Tab += 4 -> OrderTerminalDefinition.InfantryLoadoutPage()
    order_terminalb.Tab(4).asInstanceOf[OrderTerminalDefinition.InfantryLoadoutPage].Exclude = ExoSuitType.MAX
    order_terminalb.SellEquipmentByDefault = true
    order_terminalb.Damageable = false
    order_terminalb.Repairable = false

    vanu_equipment_term.Name = "vanu_equipment_term"
    vanu_equipment_term.Tab += 0 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.infantryAmmunition ++ EquipmentTerminalDefinition.infantryWeapons
    )
    vanu_equipment_term.Tab += 1 -> OrderTerminalDefinition.ArmorWithAmmoPage(
      EquipmentTerminalDefinition.suits ++ EquipmentTerminalDefinition.maxSuits,
      EquipmentTerminalDefinition.maxAmmo
    )
    vanu_equipment_term.Tab += 2 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.supportAmmunition ++ EquipmentTerminalDefinition.supportWeapons
    )
    vanu_equipment_term.Tab += 3 -> OrderTerminalDefinition.EquipmentPage(EquipmentTerminalDefinition.vehicleAmmunition)
    vanu_equipment_term.Tab += 4 -> OrderTerminalDefinition.InfantryLoadoutPage()
    vanu_equipment_term.SellEquipmentByDefault = true
    vanu_equipment_term.Damageable = false
    vanu_equipment_term.Repairable = false

    cert_terminal.Name = "cert_terminal"
    val certs = Certification.values.filter(_.cost != 0)
    val page  = OrderTerminalDefinition.CertificationPage(certs)
    cert_terminal.Tab += 0 -> page
    cert_terminal.MaxHealth = 500
    cert_terminal.Damageable = true
    cert_terminal.Repairable = true
    cert_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.5f)
    cert_terminal.RepairIfDestroyed = true
    cert_terminal.Subtract.Damage1 = 8
    cert_terminal.Geometry = GeometryForm.representByCylinder(radius = 0.66405f, height = 1.09374f)

    implant_terminal_mech.Name = "implant_terminal_mech"
    implant_terminal_mech.MaxHealth = 1500 //TODO 1000; right now, 1000 (mech) + 500 (interface)
    implant_terminal_mech.Damageable = true
    implant_terminal_mech.Repairable = true
    implant_terminal_mech.autoRepair = AutoRepairStats(1.6f, 5000, 2400, 0.5f)
    implant_terminal_mech.RepairIfDestroyed = true
    implant_terminal_mech.RadiationShielding = 0.5f
    implant_terminal_mech.Geometry = GeometryForm.representByCylinder(radius = 2.7813f, height = 6.4375f)

    implant_terminal_interface.Name = "implant_terminal_interface"
    implant_terminal_interface.Tab += 0 -> OrderTerminalDefinition.ImplantPage(ImplantTerminalDefinition.implants)
    implant_terminal_interface.MaxHealth = 500
    implant_terminal_interface.Damageable = false //TODO true
    implant_terminal_interface.Repairable = true
    implant_terminal_interface.autoRepair =
      AutoRepairStats(1, 5000, 200, 1) //TODO amount and drain are default? undefined?
    implant_terminal_interface.RepairIfDestroyed = true
    //TODO will need geometry when Damageable = true

    ground_vehicle_terminal.Name = "ground_vehicle_terminal"
    ground_vehicle_terminal.Tab += 46769 -> OrderTerminalDefinition.VehiclePage(
      VehicleTerminalDefinition.groundVehicles,
      VehicleTerminalDefinition.trunk
    )
    ground_vehicle_terminal.Tab += 4 -> OrderTerminalDefinition.VehicleLoadoutPage(10)
    ground_vehicle_terminal.MaxHealth = 500
    ground_vehicle_terminal.Damageable = true
    ground_vehicle_terminal.Repairable = true
    ground_vehicle_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.5f)
    ground_vehicle_terminal.RepairIfDestroyed = true
    ground_vehicle_terminal.Subtract.Damage1 = 8
    ground_vehicle_terminal.Geometry = vterm

    air_vehicle_terminal.Name = "air_vehicle_terminal"
    air_vehicle_terminal.Tab += 46769 -> OrderTerminalDefinition.VehiclePage(
      VehicleTerminalDefinition.flight1Vehicles,
      VehicleTerminalDefinition.trunk
    )
    air_vehicle_terminal.Tab += 4 -> OrderTerminalDefinition.VehicleLoadoutPage(10)
    air_vehicle_terminal.MaxHealth = 500
    air_vehicle_terminal.Damageable = true
    air_vehicle_terminal.Repairable = true
    air_vehicle_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.5f)
    air_vehicle_terminal.RepairIfDestroyed = true
    air_vehicle_terminal.Subtract.Damage1 = 8
    air_vehicle_terminal.Geometry = vterm

    dropship_vehicle_terminal.Name = "dropship_vehicle_terminal"
    dropship_vehicle_terminal.Tab += 46769 -> OrderTerminalDefinition.VehiclePage(
      VehicleTerminalDefinition.flight1Vehicles ++ VehicleTerminalDefinition.flight2Vehicles,
      VehicleTerminalDefinition.trunk
    )
    dropship_vehicle_terminal.Tab += 4 -> OrderTerminalDefinition.VehicleLoadoutPage(10)
    dropship_vehicle_terminal.MaxHealth = 500
    dropship_vehicle_terminal.Damageable = true
    dropship_vehicle_terminal.Repairable = true
    dropship_vehicle_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.5f)
    dropship_vehicle_terminal.RepairIfDestroyed = true
    dropship_vehicle_terminal.Subtract.Damage1 = 8
    dropship_vehicle_terminal.Geometry = vterm

    vehicle_terminal_combined.Name = "vehicle_terminal_combined"
    vehicle_terminal_combined.Tab += 46769 -> OrderTerminalDefinition.VehiclePage(
      VehicleTerminalDefinition.flight1Vehicles ++ VehicleTerminalDefinition.groundVehicles,
      VehicleTerminalDefinition.trunk
    )
    vehicle_terminal_combined.Tab += 4 -> OrderTerminalDefinition.VehicleLoadoutPage(10)
    vehicle_terminal_combined.MaxHealth = 500
    vehicle_terminal_combined.Damageable = true
    vehicle_terminal_combined.Repairable = true
    vehicle_terminal_combined.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.5f)
    vehicle_terminal_combined.RepairIfDestroyed = true
    vehicle_terminal_combined.Subtract.Damage1 = 8
    vehicle_terminal_combined.Geometry = vterm

    vanu_air_vehicle_term.Name = "vanu_air_vehicle_term"
    vanu_air_vehicle_term.Tab += 46769 -> OrderTerminalDefinition.VehiclePage(
      VehicleTerminalDefinition.flight1Vehicles,
      VehicleTerminalDefinition.trunk
    )
    vanu_air_vehicle_term.Tab += 4 -> OrderTerminalDefinition.VehicleLoadoutPage(10)
    vanu_air_vehicle_term.MaxHealth = 500
    vanu_air_vehicle_term.Damageable = true
    vanu_air_vehicle_term.Repairable = true
    vanu_air_vehicle_term.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.5f)
    vanu_air_vehicle_term.RepairIfDestroyed = true
    vanu_air_vehicle_term.Subtract.Damage1 = 8

    vanu_vehicle_term.Name = "vanu_vehicle_term"
    vanu_vehicle_term.Tab += 46769 -> OrderTerminalDefinition.VehiclePage(
      VehicleTerminalDefinition.groundVehicles,
      VehicleTerminalDefinition.trunk
    )
    vanu_vehicle_term.Tab += 4 -> OrderTerminalDefinition.VehicleLoadoutPage(10)
    vanu_vehicle_term.MaxHealth = 500
    vanu_vehicle_term.Damageable = true
    vanu_vehicle_term.Repairable = true
    vanu_vehicle_term.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.5f)
    vanu_vehicle_term.RepairIfDestroyed = true
    vanu_vehicle_term.Subtract.Damage1 = 8

    bfr_terminal.Name = "bfr_terminal"
    bfr_terminal.Tab += 0 -> OrderTerminalDefinition.VehiclePage(
      VehicleTerminalDefinition.bfrVehicles,
      VehicleTerminalDefinition.trunk
    )
    bfr_terminal.Tab += 1 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.bfrAmmunition ++ EquipmentTerminalDefinition.bfrArmWeapons
    ) //inaccessible?
    bfr_terminal.Tab += 2 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.bfrAmmunition ++ EquipmentTerminalDefinition.bfrGunnerWeapons
    ) //inaccessible?
    bfr_terminal.Tab += 3 -> OrderTerminalDefinition.BattleframeSpawnLoadoutPage(
      VehicleTerminalDefinition.bfrVehicles
    )
    bfr_terminal.MaxHealth = 500
    bfr_terminal.Damageable = true
    bfr_terminal.Repairable = true
    bfr_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.5f)
    bfr_terminal.RepairIfDestroyed = true
    bfr_terminal.Subtract.Damage1 = 8
    bfr_terminal.Geometry = GeometryForm.representByCylinder(radius = 0.92185f, height = 2.64693f)

    respawn_tube.Name = "respawn_tube"
    respawn_tube.Delay = 10
    respawn_tube.SpecificPointFunc = SpawnPoint.Tube
    respawn_tube.MaxHealth = 1000
    respawn_tube.Damageable = true
    respawn_tube.DamageableByFriendlyFire = false
    respawn_tube.Repairable = true
    respawn_tube.autoRepair = AutoRepairStats(1.6f, 10000, 2400, 1)
    respawn_tube.RepairIfDestroyed = true
    respawn_tube.Subtract.Damage1 = 8
    respawn_tube.Geometry = GeometryForm.representByCylinder(radius = 0.9336f, height = 2.84375f)

    respawn_tube_sanctuary.Name = "respawn_tube"
    respawn_tube_sanctuary.Delay = 10
    respawn_tube_sanctuary.SpecificPointFunc = SpawnPoint.Default
    respawn_tube_sanctuary.MaxHealth = 1000
    respawn_tube_sanctuary.Damageable = false //true?
    respawn_tube_sanctuary.DamageableByFriendlyFire = false
    respawn_tube_sanctuary.Repairable = true
    respawn_tube_sanctuary.autoRepair = AutoRepairStats(1.6f, 10000, 2400, 1)
    //TODO will need geometry when Damageable = true

    respawn_tube_tower.Name = "respawn_tube_tower"
    respawn_tube_tower.Delay = 10 // Temporary -- Default value is 20
    respawn_tube_tower.SpecificPointFunc = SpawnPoint.Tube
    respawn_tube_tower.MaxHealth = 1000
    respawn_tube_tower.Damageable = true
    respawn_tube_tower.DamageableByFriendlyFire = false
    respawn_tube_tower.Repairable = true
    respawn_tube_tower.autoRepair = AutoRepairStats(1.6f, 10000, 2400, 1)
    respawn_tube_tower.RepairIfDestroyed = true
    respawn_tube_tower.Subtract.Damage1 = 8
    respawn_tube_tower.Geometry = GeometryForm.representByCylinder(radius = 0.9336f, height = 2.84375f)

    teleportpad_terminal.Name = "teleportpad_terminal"
    teleportpad_terminal.Tab += 0 -> OrderTerminalDefinition.EquipmentPage(EquipmentTerminalDefinition.routerTerminal)
    teleportpad_terminal.Damageable = false
    teleportpad_terminal.Repairable = false

    targeting_laser_dispenser.Name = "targeting_laser_dispenser"
    targeting_laser_dispenser.Tab += 0 -> OrderTerminalDefinition.EquipmentPage(EquipmentTerminalDefinition.flailTerminal)
    targeting_laser_dispenser.Damageable = false
    targeting_laser_dispenser.Repairable = false

    medical_terminal.Name = "medical_terminal"
    medical_terminal.Interval = 500
    medical_terminal.HealAmount = 5
    medical_terminal.ArmorAmount = 10
    medical_terminal.UseRadius = 0.75f
    medical_terminal.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.Medical
    medical_terminal.MaxHealth = 500
    medical_terminal.Damageable = true
    medical_terminal.Repairable = true
    medical_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.5f)
    medical_terminal.RepairIfDestroyed = true
    medical_terminal.Geometry = GeometryForm.representByCylinder(radius = 0.711f, height = 1.75f)

    adv_med_terminal.Name = "adv_med_terminal"
    adv_med_terminal.Interval = 500
    adv_med_terminal.HealAmount = 8
    adv_med_terminal.ArmorAmount = 15
    adv_med_terminal.UseRadius = 0.75f
    adv_med_terminal.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.Medical
    adv_med_terminal.MaxHealth = 750
    adv_med_terminal.Damageable = true
    adv_med_terminal.Repairable = true
    adv_med_terminal.autoRepair = AutoRepairStats(1.57894f, 5000, 2400, 0.5f)
    adv_med_terminal.RepairIfDestroyed = true
    adv_med_terminal.Geometry = GeometryForm.representByCylinder(radius = 0.8662125f, height = 3.47f)

    crystals_health_a.Name = "crystals_health_a"
    crystals_health_a.Interval = 500
    crystals_health_a.HealAmount = 4
    crystals_health_a.UseRadius = 5
    crystals_health_a.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.HealthCrystal
    crystals_health_a.Damageable = false
    crystals_health_a.Repairable = false

    crystals_health_b.Name = "crystals_health_b"
    crystals_health_b.Interval = 500
    crystals_health_b.HealAmount = 4
    crystals_health_b.UseRadius = 5
    crystals_health_b.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.HealthCrystal
    crystals_health_b.Damageable = false
    crystals_health_b.Repairable = false

    portable_med_terminal.Name = "portable_med_terminal"
    portable_med_terminal.Interval = 500
    portable_med_terminal.HealAmount = 5
    portable_med_terminal.ArmorAmount = 10
    portable_med_terminal.UseRadius = 3
    portable_med_terminal.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.Medical
    portable_med_terminal.MaxHealth = 500
    portable_med_terminal.Damageable = false //TODO actually true
    portable_med_terminal.Repairable = false
    portable_med_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.5f)

    pad_landing_frame.Name = "pad_landing_frame"
    pad_landing_frame.Interval = 1000
    pad_landing_frame.HealAmount = 60
    pad_landing_frame.UseRadius = 20
    pad_landing_frame.TargetValidation += EffectTarget.Category.Aircraft -> EffectTarget.Validation.PadLanding
    pad_landing_frame.Damageable = false
    pad_landing_frame.Repairable = false

    pad_landing_tower_frame.Name = "pad_landing_tower_frame"
    pad_landing_tower_frame.Interval = 1000
    pad_landing_tower_frame.HealAmount = 60
    pad_landing_tower_frame.UseRadius = 20
    pad_landing_tower_frame.TargetValidation += EffectTarget.Category.Aircraft -> EffectTarget.Validation.PadLanding
    pad_landing_tower_frame.Damageable = false
    pad_landing_tower_frame.Repairable = false

    repair_silo.Name = "repair_silo"
    repair_silo.Interval = 1000
    repair_silo.HealAmount = 60
    repair_silo.UseRadius = 20
    repair_silo.TargetValidation += EffectTarget.Category.Vehicle -> EffectTarget.Validation.RepairSilo
    repair_silo.Damageable = false
    repair_silo.Repairable = false

    recharge_terminal.Name = "recharge_terminal"
    recharge_terminal.Interval = 1000
    recharge_terminal.UseRadius = 20
    recharge_terminal.TargetValidation += EffectTarget.Category.Vehicle -> EffectTarget.Validation.AncientVehicleWeaponRecharge
    recharge_terminal.Damageable = false
    recharge_terminal.Repairable = false

    recharge_terminal_weapon_module.Name = "recharge_terminal_weapon_module"
    recharge_terminal_weapon_module.Interval = 1000
    recharge_terminal_weapon_module.UseRadius = 300
    recharge_terminal_weapon_module.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.AncientWeaponRecharge
    recharge_terminal_weapon_module.Damageable = false
    recharge_terminal_weapon_module.Repairable = false

    mb_pad_creation.Name = "mb_pad_creation"
    mb_pad_creation.Damageable = false
    mb_pad_creation.Repairable = false
    mb_pad_creation.VehicleCreationZOffset = 2.52604f
    mb_pad_creation.killBox = VehicleSpawnPadDefinition.prepareKillBox(
      forwardLimit = 14,
      backLimit = 10,
      sideLimit = 7.5f,
      aboveLimit = 5 //double to 10 when spawning a flying vehicle
    )
    mb_pad_creation.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 99999
      DamageRadiusMin = 14
      DamageRadius = 14.5f
      DamageAtEdge = 0.00002f
      //damage is 99999 at 14m, dropping rapidly to ~1 at 14.5m
    }

    dropship_pad_doors.Name = "dropship_pad_doors"
    dropship_pad_doors.Damageable = false
    dropship_pad_doors.Repairable = false
    dropship_pad_doors.VehicleCreationZOffset = 4.89507f
    dropship_pad_doors.VehicleCreationZOrientOffset = -90f
    dropship_pad_doors.killBox = VehicleSpawnPadDefinition.prepareKillBox(
      forwardLimit = 14,
      backLimit = 14,
      sideLimit = 13.5f,
      aboveLimit = 5 //doubles to 10
    )
    dropship_pad_doors.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 99999
      DamageRadiusMin = 14
      DamageRadius = 14.5f
      DamageAtEdge = 0.00002f
      //damage is 99999 at 14m, dropping rapidly to ~1 at 14.5m
    }

    vanu_vehicle_creation_pad.Name = "vanu_vehicle_creation_pad"
    vanu_vehicle_creation_pad.Damageable = false
    vanu_vehicle_creation_pad.Repairable = false
    vanu_vehicle_creation_pad.killBox = VehicleSpawnPadDefinition.prepareVanuKillBox(
      radius = 8.5f,
      aboveLimit = 5
    )
    vanu_vehicle_creation_pad.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 99999
      DamageRadiusMin = 14
      DamageRadius = 14.5f
      DamageAtEdge = 0.00002f
      //damage is 99999 at 14m, dropping rapidly to ~1 at 14.5m
    }

    bfr_door.Name = "bfr_door"
    bfr_door.Damageable = false
    bfr_door.Repairable = false
    //bfr_door.VehicleCreationZOffset = -4.5f
    bfr_door.VehicleCreationZOrientOffset = 0f //90f
    bfr_door.killBox = VehicleSpawnPadDefinition.prepareBfrShedKillBox(
      radius = 10f,
      aboveLimit = 10f
    )
    bfr_door.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 99999
      DamageRadiusMin = 14 //TODO fix this
      DamageRadius = 14.5f //TODO fix this
      DamageAtEdge = 0.00002f
      //damage is 99999 at 14m, dropping rapidly to ~1 at 14.5m
    }

    pad_create.Name = "pad_create"
    pad_create.Damageable = false
    pad_create.Repairable = false
    //pad_create.killBox = ...
    //pad_create.innateDamage = ...

    pad_creation.Name = "pad_creation"
    pad_creation.Damageable = false
    pad_creation.Repairable = false
    pad_creation.VehicleCreationZOffset = 1.70982f
    //pad_creation.killBox = ...
    //pad_creation.innateDamage = ...

    spawnpoint_vehicle.Name = "spawnpoint_vehicle"
    spawnpoint_vehicle.Damageable = false
    spawnpoint_vehicle.Repairable = false
    //spawnpoint_vehicle.killBox = ...
    //spawnpoint_vehicle.innateDamage = ...

    mb_locker.Name = "mb_locker"
    mb_locker.Damageable = false
    mb_locker.Repairable = false

    lock_external.Name = "lock_external"
    lock_external.Damageable = false
    lock_external.Repairable = false

    door.Name = "door"
    door.Damageable = false
    door.Repairable = false

    door_spawn_mb.Name = "door_spawn_mb"
    door_spawn_mb.Damageable = true
    door_spawn_mb.Repairable = false

    gr_door_mb_orb.Name = "gr_door_mb_orb"
    gr_door_mb_orb.Damageable = false
    gr_door_mb_orb.Repairable = false

    spawn_tube_door.Name = "spawn_tube_door"
    spawn_tube_door.Damageable = true
    spawn_tube_door.Repairable = false

    resource_silo.Name = "resource_silo"
    resource_silo.Damageable = false
    resource_silo.Repairable = false
    resource_silo.MaxNtuCapacitor = 1000

    capture_terminal.Name = "capture_terminal"
    capture_terminal.Damageable = false
    capture_terminal.Repairable = false

    secondary_capture.Name = "secondary_capture"
    secondary_capture.Damageable = false
    secondary_capture.Repairable = false

    vanu_control_console.Name = "vanu_control_console"
    vanu_control_console.Damageable = false
    vanu_control_console.Repairable = false

    lodestar_repair_terminal.Name = "lodestar_repair_terminal"
    lodestar_repair_terminal.Interval = 1000
    lodestar_repair_terminal.HealAmount = 60
    lodestar_repair_terminal.UseRadius = 20
    lodestar_repair_terminal.TargetValidation += EffectTarget.Category.Vehicle -> EffectTarget.Validation.RepairSilo
    lodestar_repair_terminal.Damageable = false
    lodestar_repair_terminal.Repairable = false

    multivehicle_rearm_terminal.Name = "multivehicle_rearm_terminal"
    multivehicle_rearm_terminal.Tab += 3 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.vehicleAmmunition
    )
    multivehicle_rearm_terminal.Tab += 4 -> OrderTerminalDefinition.VehicleLoadoutPage(10)
    multivehicle_rearm_terminal.SellEquipmentByDefault = true //TODO ?
    multivehicle_rearm_terminal.Damageable = false
    multivehicle_rearm_terminal.Repairable = false

    bfr_rearm_terminal.Name = "bfr_rearm_terminal"
    bfr_rearm_terminal.Tab += 1 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.bfrAmmunition ++ EquipmentTerminalDefinition.bfrArmWeapons
    )
    bfr_rearm_terminal.Tab += 2 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.bfrAmmunition ++ EquipmentTerminalDefinition.bfrGunnerWeapons
    )
    bfr_rearm_terminal.Tab += 3 -> OrderTerminalDefinition.VehicleLoadoutPage(15)
    bfr_rearm_terminal.SellEquipmentByDefault = true //TODO ?
    bfr_rearm_terminal.Damageable = false
    bfr_rearm_terminal.Repairable = false

    air_rearm_terminal.Name = "air_rearm_terminal"
    air_rearm_terminal.Tab += 3 -> OrderTerminalDefinition.EquipmentPage(EquipmentTerminalDefinition.vehicleAmmunition)
    air_rearm_terminal.Tab += 4 -> OrderTerminalDefinition.VehicleLoadoutPage(10)
    air_rearm_terminal.SellEquipmentByDefault = true //TODO ?
    air_rearm_terminal.Damageable = false
    air_rearm_terminal.Repairable = false

    ground_rearm_terminal.Name = "ground_rearm_terminal"
    ground_rearm_terminal.Tab += 3 -> OrderTerminalDefinition.EquipmentPage(
      EquipmentTerminalDefinition.vehicleAmmunition
    )
    ground_rearm_terminal.Tab += 4 -> OrderTerminalDefinition.VehicleLoadoutPage(10)
    ground_rearm_terminal.SellEquipmentByDefault = true //TODO ?
    ground_rearm_terminal.Damageable = false
    ground_rearm_terminal.Repairable = false

    manned_turret.Name = "manned_turret"
    manned_turret.MaxHealth = 3600
    manned_turret.Damageable = true
    manned_turret.DamageDisablesAt = 0
    manned_turret.Repairable = true
    manned_turret.autoRepair = AutoRepairStats(1.0909f, 10000, 1600, 0.5f)
    manned_turret.RepairIfDestroyed = true
    manned_turret.WeaponPaths += 1                          -> new mutable.HashMap()
    manned_turret.WeaponPaths(1) += TurretUpgrade.None      -> phalanx_sgl_hevgatcan
    manned_turret.WeaponPaths(1) += TurretUpgrade.AVCombo   -> phalanx_avcombo
    manned_turret.WeaponPaths(1) += TurretUpgrade.FlakCombo -> phalanx_flakcombo
    manned_turret.controlledWeapons(seat = 0, weapon = 1)
    manned_turret.MountPoints += 1                          -> MountInfo(0)
    manned_turret.FactionLocked = true
    manned_turret.ReserveAmmunition = false
    manned_turret.RadiationShielding = 0.5f
    manned_turret.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 300
      DamageRadius = 5
      DamageAtEdge = 0.1f
      Modifiers = ExplodingRadialDegrade
    }
    manned_turret.Geometry = GeometryForm.representByCylinder(radius = 1.2695f, height = 4.042f)

    vanu_sentry_turret.Name = "vanu_sentry_turret"
    vanu_sentry_turret.MaxHealth = 1500
    vanu_sentry_turret.Damageable = true
    vanu_sentry_turret.DamageDisablesAt = 0
    vanu_sentry_turret.Repairable = true
    vanu_sentry_turret.autoRepair = AutoRepairStats(3.27272f, 10000, 1000, 0.5f)
    vanu_sentry_turret.RepairIfDestroyed = true
    vanu_sentry_turret.WeaponPaths += 1                     -> new mutable.HashMap()
    vanu_sentry_turret.WeaponPaths(1) += TurretUpgrade.None -> vanu_sentry_turret_weapon
    vanu_sentry_turret.controlledWeapons(seat = 0, weapon = 1)
    vanu_sentry_turret.MountPoints += 1                     -> MountInfo(0)
    vanu_sentry_turret.MountPoints += 2                     -> MountInfo(0)
    vanu_sentry_turret.FactionLocked = false
    vanu_sentry_turret.ReserveAmmunition = false
    vanu_sentry_turret.Geometry = GeometryForm.representByCylinder(radius = 1.76311f, height = 3.984375f)

    painbox.Name = "painbox"
    painbox.alwaysOn = false
    painbox.sphereOffset = Vector3(0, 0, -0.4f)
    painbox.Damageable = false
    painbox.Repairable = false
    painbox.innateDamage = new DamageWithPosition {
      Damage0 = 2
      DamageRadius = 0
      DamageToHealthOnly = true
    }

    painbox_continuous.Name = "painbox_continuous"
    painbox_continuous.sphereOffset = Vector3(0, 0, -0.4f)
    painbox_continuous.Damageable = false
    painbox_continuous.Repairable = false
    painbox_continuous.innateDamage = new DamageWithPosition {
      Damage0 = 2
      DamageRadius = 0
      DamageToHealthOnly = true
    }

    painbox_door_radius.Name = "painbox_door_radius"
    painbox_door_radius.alwaysOn = false
    painbox_door_radius.sphereOffset = Vector3(0, 0, -0.4f)
    painbox_door_radius.hasNearestDoorDependency = true
    painbox_door_radius.Damageable = false
    painbox_door_radius.Repairable = false
    painbox_door_radius.innateDamage = new DamageWithPosition {
      Damage0 = 2
      DamageRadius = 10f * 0.6928f
      DamageToHealthOnly = true
    }

    painbox_door_radius_continuous.Name = "painbox_door_radius_continuous"
    painbox_door_radius_continuous.sphereOffset = Vector3(0, 0, -0.4f)
    painbox_door_radius_continuous.hasNearestDoorDependency = true
    painbox_door_radius_continuous.Damageable = false
    painbox_door_radius_continuous.Repairable = false
    painbox_door_radius_continuous.innateDamage = new DamageWithPosition {
      Damage0 = 2
      DamageRadius = 10f * 0.6928f
      DamageToHealthOnly = true
    }

    painbox_radius.Name = "painbox_radius"
    painbox_radius.alwaysOn = false
    painbox_radius.sphereOffset = Vector3(0, 0, -0.4f)
    painbox_radius.Damageable = false
    painbox_radius.Repairable = false
    painbox_radius.innateDamage = new DamageWithPosition {
      Damage0 = 2
      DamageRadius = 10f * 0.6928f
      DamageToHealthOnly = true
    }

    painbox_radius_continuous.Name = "painbox_radius_continuous"
    painbox_radius_continuous.Damageable = false
    painbox_radius_continuous.Repairable = false
    painbox_radius_continuous.innateDamage = new DamageWithPosition {
      Damage0 = 2
      DamageRadius = 8.55f
      DamageToHealthOnly = true
    }

    gen_control.Name = "gen_control"
    gen_control.Damageable = false
    gen_control.Repairable = false

    generator.Name = "generator"
    generator.MaxHealth = 4000
    generator.Damageable = true
    generator.DamageableByFriendlyFire = false
    generator.Repairable = true
    generator.autoRepair = AutoRepairStats(0.77775f, 5000, 875, 1)
    generator.RepairDistance = 13.5f
    generator.RepairIfDestroyed = true
    generator.Subtract.Damage1 = 9
    generator.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 99999
      DamageRadiusMin = 15
      DamageRadius = 15.1f
      DamageAtEdge = 0.000011f
      Modifiers = ExplodingRadialDegrade
      //damage is 99999 at 15m, dropping rapidly to ~1 at 15.1m
    }
    generator.Geometry = GeometryForm.representByCylinder(radius = 1.2617f, height = 9.14063f)

    obbasemesh.Name = "obbasemesh"
    obbasemesh.Descriptor = "orbital_shuttle_pad"
    obbasemesh.Damageable = false
    obbasemesh.Repairable = false
  }
}
