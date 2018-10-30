// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ballistics.Projectiles
import net.psforever.objects.ce.{DeployableCategory, DeployedItem}
import net.psforever.objects.definition._
import net.psforever.objects.definition.converter._
import net.psforever.objects.equipment._
import net.psforever.objects.inventory.InventoryTile
import net.psforever.objects.serverobject.doors.DoorDefinition
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMechDefinition
import net.psforever.objects.serverobject.locks.IFFLockDefinition
import net.psforever.objects.serverobject.mblocker.LockerDefinition
import net.psforever.objects.serverobject.pad.VehicleSpawnPadDefinition
import net.psforever.objects.serverobject.terminals._
import net.psforever.objects.serverobject.tube.SpawnTubeDefinition
import net.psforever.objects.serverobject.resourcesilo.ResourceSiloDefinition
import net.psforever.objects.serverobject.turret.{TurretDefinition, TurretUpgrade}
import net.psforever.objects.vehicles.{DestroyedVehicle, SeatArmorRestriction, UtilityType}
import net.psforever.objects.vital.{DamageType, StandardResolutions}
import net.psforever.types.{CertificationType, PlanetSideEmpire}

import scala.collection.mutable
import scala.concurrent.duration._

object GlobalDefinitions {
  /*
  characters
   */
  val avatar = new AvatarDefinition(121)
  /*
  Implants
   */
  val advanced_regen = ImplantDefinition(0)

  val targeting = ImplantDefinition(1)

  val audio_amplifier = ImplantDefinition(2)

  val darklight_vision = ImplantDefinition(3)

  val melee_booster = ImplantDefinition(4)

  val personal_shield = ImplantDefinition(5)

  val range_magnifier = ImplantDefinition(6)

  val second_wind = ImplantDefinition(7)

  val silent_run = ImplantDefinition(8)

  val surge = ImplantDefinition(9)

  /*
  Projectiles
   */
  val bullet_105mm_projectile = ProjectileDefinition(Projectiles.bullet_105mm_projectile)

  val bullet_12mm_projectile = ProjectileDefinition(Projectiles.bullet_12mm_projectile)

  val bullet_12mm_projectileb = ProjectileDefinition(Projectiles.bullet_12mm_projectileb)

  val bullet_150mm_projectile = ProjectileDefinition(Projectiles.bullet_150mm_projectile)

  val bullet_15mm_apc_projectile = ProjectileDefinition(Projectiles.bullet_15mm_apc_projectile)

  val bullet_15mm_projectile = ProjectileDefinition(Projectiles.bullet_15mm_projectile)

  val bullet_20mm_apc_projectile = ProjectileDefinition(Projectiles.bullet_20mm_apc_projectile)

  val bullet_20mm_projectile = ProjectileDefinition(Projectiles.bullet_20mm_projectile)

  val bullet_25mm_projectile = ProjectileDefinition(Projectiles.bullet_25mm_projectile)

  val bullet_35mm_projectile = ProjectileDefinition(Projectiles.bullet_35mm_projectile)

  val bullet_75mm_apc_projectile = ProjectileDefinition(Projectiles.bullet_75mm_apc_projectile)

  val bullet_75mm_projectile = ProjectileDefinition(Projectiles.bullet_75mm_projectile)

  val bullet_9mm_AP_projectile = ProjectileDefinition(Projectiles.bullet_9mm_AP_projectile)

  val bullet_9mm_projectile = ProjectileDefinition(Projectiles.bullet_9mm_projectile)

  val anniversary_projectilea = ProjectileDefinition(Projectiles.anniversary_projectilea)

  val anniversary_projectileb = ProjectileDefinition(Projectiles.anniversary_projectileb)

  val aphelion_immolation_cannon_projectile = ProjectileDefinition(Projectiles.aphelion_immolation_cannon_projectile)

  val aphelion_laser_projectile = ProjectileDefinition(Projectiles.aphelion_laser_projectile)

  val aphelion_plasma_rocket_projectile = ProjectileDefinition(Projectiles.aphelion_plasma_rocket_projectile)

  val aphelion_ppa_projectile = ProjectileDefinition(Projectiles.aphelion_ppa_projectile)

  val aphelion_starfire_projectile = ProjectileDefinition(Projectiles.aphelion_starfire_projectile)

  val bolt_projectile = ProjectileDefinition(Projectiles.bolt_projectile)

  val burster_projectile = ProjectileDefinition(Projectiles.burster_projectile)

  val chainblade_projectile = ProjectileDefinition(Projectiles.chainblade_projectile)

  val colossus_100mm_projectile = ProjectileDefinition(Projectiles.colossus_100mm_projectile)

  val colossus_burster_projectile = ProjectileDefinition(Projectiles.colossus_burster_projectile)

  val colossus_chaingun_projectile = ProjectileDefinition(Projectiles.colossus_chaingun_projectile)

  val colossus_cluster_bomb_projectile = ProjectileDefinition(Projectiles.colossus_cluster_bomb_projectile)

  val colossus_tank_cannon_projectile = ProjectileDefinition(Projectiles.colossus_tank_cannon_projectile)

  val comet_projectile = ProjectileDefinition(Projectiles.comet_projectile)

  val dualcycler_projectile = ProjectileDefinition(Projectiles.dualcycler_projectile)

  val dynomite_projectile = ProjectileDefinition(Projectiles.dynomite_projectile)

  val energy_cell_projectile = ProjectileDefinition(Projectiles.energy_cell_projectile)

  val energy_gun_nc_projectile = ProjectileDefinition(Projectiles.energy_gun_nc_projectile)

  val energy_gun_tr_projectile = ProjectileDefinition(Projectiles.energy_gun_tr_projectile)

  val energy_gun_vs_projectile = ProjectileDefinition(Projectiles.energy_gun_vs_projectile)

  val enhanced_energy_cell_projectile = ProjectileDefinition(Projectiles.enhanced_energy_cell_projectile)

  val enhanced_quasar_projectile = ProjectileDefinition(Projectiles.enhanced_quasar_projectile)

  val falcon_projectile = ProjectileDefinition(Projectiles.falcon_projectile)

  val firebird_missile_projectile = ProjectileDefinition(Projectiles.firebird_missile_projectile)

  val flail_projectile = ProjectileDefinition(Projectiles.flail_projectile)

  val flamethrower_fireball = ProjectileDefinition(Projectiles.flamethrower_fireball)

  val flamethrower_projectile = ProjectileDefinition(Projectiles.flamethrower_projectile)

  val flux_cannon_apc_projectile = ProjectileDefinition(Projectiles.flux_cannon_apc_projectile)

  val flux_cannon_thresher_projectile = ProjectileDefinition(Projectiles.flux_cannon_thresher_projectile)

  val fluxpod_projectile = ProjectileDefinition(Projectiles.fluxpod_projectile)

  val forceblade_projectile = ProjectileDefinition(Projectiles.forceblade_projectile)

  val frag_cartridge_projectile = ProjectileDefinition(Projectiles.frag_cartridge_projectile)

  val frag_cartridge_projectile_b = ProjectileDefinition(Projectiles.frag_cartridge_projectile_b)

  val frag_grenade_projectile = ProjectileDefinition(Projectiles.frag_grenade_projectile)

  val frag_grenade_projectile_enh = ProjectileDefinition(Projectiles.frag_grenade_projectile_enh)

  val galaxy_gunship_gun_projectile = ProjectileDefinition(Projectiles.galaxy_gunship_gun_projectile)

  val gauss_cannon_projectile = ProjectileDefinition(Projectiles.gauss_cannon_projectile)

  val grenade_projectile = ProjectileDefinition(Projectiles.grenade_projectile)

  val heavy_grenade_projectile = ProjectileDefinition(Projectiles.heavy_grenade_projectile)

  val heavy_rail_beam_projectile = ProjectileDefinition(Projectiles.heavy_rail_beam_projectile)

  val heavy_sniper_projectile = ProjectileDefinition(Projectiles.heavy_sniper_projectile)

  val hellfire_projectile = ProjectileDefinition(Projectiles.hellfire_projectile)

  val hunter_seeker_missile_dumbfire = ProjectileDefinition(Projectiles.hunter_seeker_missile_dumbfire)

  val hunter_seeker_missile_projectile = ProjectileDefinition(Projectiles.hunter_seeker_missile_projectile)

  val jammer_cartridge_projectile = ProjectileDefinition(Projectiles.jammer_cartridge_projectile)

  val jammer_cartridge_projectile_b = ProjectileDefinition(Projectiles.jammer_cartridge_projectile_b)

  val jammer_grenade_projectile = ProjectileDefinition(Projectiles.jammer_grenade_projectile)

  val jammer_grenade_projectile_enh = ProjectileDefinition(Projectiles.jammer_grenade_projectile_enh)

  val katana_projectile = ProjectileDefinition(Projectiles.katana_projectile)

  val katana_projectileb = ProjectileDefinition(Projectiles.katana_projectileb)

  val lancer_projectile = ProjectileDefinition(Projectiles.lancer_projectile)

  val lasher_projectile = ProjectileDefinition(Projectiles.lasher_projectile)

  val lasher_projectile_ap = ProjectileDefinition(Projectiles.lasher_projectile_ap)

  val liberator_bomb_cluster_bomblet_projectile = ProjectileDefinition(Projectiles.liberator_bomb_cluster_bomblet_projectile)

  val liberator_bomb_cluster_projectile = ProjectileDefinition(Projectiles.liberator_bomb_cluster_projectile)

  val liberator_bomb_projectile = ProjectileDefinition(Projectiles.liberator_bomb_projectile)

  val maelstrom_grenade_projectile = ProjectileDefinition(Projectiles.maelstrom_grenade_projectile)

  val maelstrom_grenade_projectile_contact = ProjectileDefinition(Projectiles.maelstrom_grenade_projectile_contact)

  val maelstrom_stream_projectile = ProjectileDefinition(Projectiles.maelstrom_stream_projectile)

  val magcutter_projectile = ProjectileDefinition(Projectiles.magcutter_projectile)

  val melee_ammo_projectile = ProjectileDefinition(Projectiles.melee_ammo_projectile)

  val meteor_common = ProjectileDefinition(Projectiles.meteor_common)

  val meteor_projectile_b_large = ProjectileDefinition(Projectiles.meteor_projectile_b_large)

  val meteor_projectile_b_medium = ProjectileDefinition(Projectiles.meteor_projectile_b_medium)

  val meteor_projectile_b_small = ProjectileDefinition(Projectiles.meteor_projectile_b_small)

  val meteor_projectile_large = ProjectileDefinition(Projectiles.meteor_projectile_large)

  val meteor_projectile_medium = ProjectileDefinition(Projectiles.meteor_projectile_medium)

  val meteor_projectile_small = ProjectileDefinition(Projectiles.meteor_projectile_small)

  val mine_projectile = ProjectileDefinition(Projectiles.mine_projectile)

  val mine_sweeper_projectile = ProjectileDefinition(Projectiles.mine_sweeper_projectile)

  val mine_sweeper_projectile_enh = ProjectileDefinition(Projectiles.mine_sweeper_projectile_enh)

  val oicw_projectile = ProjectileDefinition(Projectiles.oicw_projectile)

  val pellet_gun_projectile = ProjectileDefinition(Projectiles.pellet_gun_projectile)

  val peregrine_dual_machine_gun_projectile = ProjectileDefinition(Projectiles.peregrine_dual_machine_gun_projectile)

  val peregrine_mechhammer_projectile = ProjectileDefinition(Projectiles.peregrine_mechhammer_projectile)

  val peregrine_particle_cannon_projectile = ProjectileDefinition(Projectiles.peregrine_particle_cannon_projectile)

  val peregrine_rocket_pod_projectile = ProjectileDefinition(Projectiles.peregrine_rocket_pod_projectile)

  val peregrine_sparrow_projectile = ProjectileDefinition(Projectiles.peregrine_sparrow_projectile)

  val phalanx_av_projectile = ProjectileDefinition(Projectiles.phalanx_av_projectile)

  val phalanx_flak_projectile = ProjectileDefinition(Projectiles.phalanx_flak_projectile)

  val phalanx_projectile = ProjectileDefinition(Projectiles.phalanx_projectile)

  val phoenix_missile_guided_projectile = ProjectileDefinition(Projectiles.phoenix_missile_guided_projectile)

  val phoenix_missile_projectile = ProjectileDefinition(Projectiles.phoenix_missile_projectile)

  val plasma_cartridge_projectile = ProjectileDefinition(Projectiles.plasma_cartridge_projectile)

  val plasma_cartridge_projectile_b = ProjectileDefinition(Projectiles.plasma_cartridge_projectile_b)

  val plasma_grenade_projectile = ProjectileDefinition(Projectiles.plasma_grenade_projectile)

  val plasma_grenade_projectile_B = ProjectileDefinition(Projectiles.plasma_grenade_projectile_B)

  val pounder_projectile = ProjectileDefinition(Projectiles.pounder_projectile)

  val pounder_projectile_enh = ProjectileDefinition(Projectiles.pounder_projectile_enh)

  val ppa_projectile = ProjectileDefinition(Projectiles.ppa_projectile)

  val pulsar_ap_projectile = ProjectileDefinition(Projectiles.pulsar_ap_projectile)

  val pulsar_projectile = ProjectileDefinition(Projectiles.pulsar_projectile)

  val quasar_projectile = ProjectileDefinition(Projectiles.quasar_projectile)

  val radiator_grenade_projectile = ProjectileDefinition(Projectiles.radiator_grenade_projectile)

  val radiator_sticky_projectile = ProjectileDefinition(Projectiles.radiator_sticky_projectile)

  val reaver_rocket_projectile = ProjectileDefinition(Projectiles.reaver_rocket_projectile)

  val rocket_projectile = ProjectileDefinition(Projectiles.rocket_projectile)

  val rocklet_flak_projectile = ProjectileDefinition(Projectiles.rocklet_flak_projectile)

  val rocklet_jammer_projectile = ProjectileDefinition(Projectiles.rocklet_jammer_projectile)

  val scattercannon_projectile = ProjectileDefinition(Projectiles.scattercannon_projectile)

  val scythe_projectile = ProjectileDefinition(Projectiles.scythe_projectile)

  val scythe_projectile_slave = ProjectileDefinition(Projectiles.scythe_projectile_slave)

  val shotgun_shell_AP_projectile = ProjectileDefinition(Projectiles.shotgun_shell_AP_projectile)

  val shotgun_shell_projectile = ProjectileDefinition(Projectiles.shotgun_shell_projectile)

  val six_shooter_projectile = ProjectileDefinition(Projectiles.six_shooter_projectile)

  val skyguard_flak_cannon_projectile = ProjectileDefinition(Projectiles.skyguard_flak_cannon_projectile)

  val sparrow_projectile = ProjectileDefinition(Projectiles.sparrow_projectile)

  val sparrow_secondary_projectile = ProjectileDefinition(Projectiles.sparrow_secondary_projectile)

  val spiker_projectile = ProjectileDefinition(Projectiles.spiker_projectile)

  val spitfire_aa_ammo_projectile = ProjectileDefinition(Projectiles.spitfire_aa_ammo_projectile)

  val spitfire_ammo_projectile = ProjectileDefinition(Projectiles.spitfire_ammo_projectile)

  val starfire_projectile = ProjectileDefinition(Projectiles.starfire_projectile)

  val striker_missile_projectile = ProjectileDefinition(Projectiles.striker_missile_projectile)

  val striker_missile_targeting_projectile = ProjectileDefinition(Projectiles.striker_missile_targeting_projectile)

  val trek_projectile = ProjectileDefinition(Projectiles.trek_projectile)

  val vanu_sentry_turret_projectile = ProjectileDefinition(Projectiles.vanu_sentry_turret_projectile)

  val vulture_bomb_projectile = ProjectileDefinition(Projectiles.vulture_bomb_projectile)

  val vulture_nose_bullet_projectile = ProjectileDefinition(Projectiles.vulture_nose_bullet_projectile)

  val vulture_tail_bullet_projectile = ProjectileDefinition(Projectiles.vulture_tail_bullet_projectile)

  val wasp_gun_projectile = ProjectileDefinition(Projectiles.wasp_gun_projectile)

  val wasp_rocket_projectile = ProjectileDefinition(Projectiles.wasp_rocket_projectile)

  val winchester_projectile = ProjectileDefinition(Projectiles.winchester_projectile)
  init_projectile()

  /*
  Equipment (locker_container, kits, ammunition, weapons)
   */
  import net.psforever.packet.game.objectcreate.ObjectClass
  val locker_container = new EquipmentDefinition(456) {
    Name = "locker_container"
    Size = EquipmentSize.Inventory
    Packet = new LockerContainerConverter()
  }

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

  val hunterseeker = ToolDefinition(ObjectClass.hunterseeker) //phoenix

  val lancer = ToolDefinition(ObjectClass.lancer)

  val rocklet = ToolDefinition(ObjectClass.rocklet)

  val thumper = ToolDefinition(ObjectClass.thumper)

  val radiator = ToolDefinition(ObjectClass.radiator)

  val heavy_sniper = ToolDefinition(ObjectClass.heavy_sniper) //hsr

  val bolt_driver = ToolDefinition(ObjectClass.bolt_driver)

  val oicw = ToolDefinition(ObjectClass.oicw) //scorpion

  val flamethrower = ToolDefinition(ObjectClass.flamethrower)

  val trhev_dualcycler = new ToolDefinition(ObjectClass.trhev_dualcycler) {
    override def NextFireModeIndex(index : Int) : Int = index
  }

  val trhev_pounder = new ToolDefinition(ObjectClass.trhev_pounder) {
    override def NextFireModeIndex(index : Int) : Int = {
      //TODO other modes
      if(index == 0 || index == 3) {
        if(index == 0) {
          3 //3-second fuse
        }
        else {
          0 //explode on contact
        }
      }
      else if(index == 1 || index == 4) {
        if(index == 1) {
          4 //3-second fuse, anchored
        }
        else {
          1 //explode on contact, anchored
        }
      }
      else {
        index
      }
    }
  }

  val trhev_burster = new ToolDefinition(ObjectClass.trhev_burster) {
    override def NextFireModeIndex(index : Int) : Int = index
  }

  val nchev_scattercannon = ToolDefinition(ObjectClass.nchev_scattercannon)

  val nchev_falcon = ToolDefinition(ObjectClass.nchev_falcon)

  val nchev_sparrow = ToolDefinition(ObjectClass.nchev_sparrow)

  val vshev_quasar = ToolDefinition(ObjectClass.vshev_quasar)

  val vshev_comet = ToolDefinition(ObjectClass.vshev_comet)

  val vshev_starfire = ToolDefinition(ObjectClass.vshev_starfire)

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

  val wasp_weapon_system = ToolDefinition(ObjectClass.wasp_weapon_system)

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

  val spitfire_weapon = ToolDefinition(ObjectClass.spitfire_weapon)

  val spitfire_aa_weapon = ToolDefinition(ObjectClass.spitfire_aa_weapon)

  val energy_gun = ToolDefinition(ObjectClass.energy_gun)

  val energy_gun_nc = ToolDefinition(ObjectClass.energy_gun_nc)

  val energy_gun_tr = ToolDefinition(ObjectClass.energy_gun_tr)

  val energy_gun_vs = ToolDefinition(ObjectClass.energy_gun_vs)
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

  val apc_tr = VehicleDefinition(ObjectClass.apc_tr)

  val apc_nc = VehicleDefinition(ObjectClass.apc_nc)

  val apc_vs = VehicleDefinition(ObjectClass.apc_vs)

  val lightning = VehicleDefinition(ObjectClass.lightning)

  val prowler = VehicleDefinition(ObjectClass.prowler)

  val vanguard = VehicleDefinition(ObjectClass.vanguard)

  val magrider = VehicleDefinition(ObjectClass.magrider)

  val ant = VehicleDefinition(ObjectClass.ant)

  val ams = VehicleDefinition(ObjectClass.ams)

  val router = VehicleDefinition(ObjectClass.router)

  val switchblade = VehicleDefinition(ObjectClass.switchblade)

  val flail = VehicleDefinition(ObjectClass.flail)

  val mosquito = VehicleDefinition(ObjectClass.mosquito)

  val lightgunship = VehicleDefinition(ObjectClass.lightgunship)

  val wasp = VehicleDefinition(ObjectClass.wasp)

  val liberator = VehicleDefinition(ObjectClass.liberator)

  val vulture = VehicleDefinition(ObjectClass.vulture)

  val dropship = VehicleDefinition(ObjectClass.dropship)

  val galaxy_gunship = VehicleDefinition(ObjectClass.galaxy_gunship)

  val lodestar = VehicleDefinition(ObjectClass.lodestar)

  val phantasm = VehicleDefinition(ObjectClass.phantasm)
  init_vehicles()

  /*
  combat engineering deployables
   */
  val boomer = DeployableDefinition(DeployedItem.boomer)

  val he_mine = DeployableDefinition(DeployedItem.he_mine)

  val jammer_mine = DeployableDefinition(DeployedItem.jammer_mine)

  val spitfire_turret = TurretDeployableDefinition(DeployedItem.spitfire_turret)

  val spitfire_cloaked = TurretDeployableDefinition(DeployedItem.spitfire_cloaked)

  val spitfire_aa = TurretDeployableDefinition(DeployedItem.spitfire_aa)

  val motionalarmsensor = DeployableDefinition(DeployedItem.motionalarmsensor)

  val sensor_shield = DeployableDefinition(DeployedItem.sensor_shield)

  val tank_traps = DeployableDefinition(DeployedItem.tank_traps)

  val portable_manned_turret = TurretDeployableDefinition(DeployedItem.portable_manned_turret)

  val portable_manned_turret_nc = TurretDeployableDefinition(DeployedItem.portable_manned_turret_nc)

  val portable_manned_turret_tr = TurretDeployableDefinition(DeployedItem.portable_manned_turret_tr)

  val portable_manned_turret_vs = TurretDeployableDefinition(DeployedItem.portable_manned_turret_vs)

  val deployable_shield_generator = new ShieldGeneratorDefinition

  val router_telepad_deployable = DeployableDefinition(DeployedItem.router_telepad_deployable)

  val internal_router_telepad_deployable = DeployableDefinition(DeployedItem.router_telepad_deployable)
  init_deployables()

  /*
  Miscellaneous
   */
  val order_terminal = new OrderTerminalDefinition

  val ams_respawn_tube = new SpawnTubeDefinition(49)

  val matrix_terminalc = new MatrixTerminalDefinition(519)

  val order_terminala = new OrderTerminalABDefinition(613)

  val order_terminalb = new OrderTerminalABDefinition(614)

  val cert_terminal = new CertTerminalDefinition

  val implant_terminal_mech = new ImplantTerminalMechDefinition

  val implant_terminal_interface = new ImplantTerminalInterfaceDefinition

  val ground_vehicle_terminal = new GroundVehicleTerminalDefinition

  val air_vehicle_terminal = new AirVehicleTerminalDefinition

  val dropship_vehicle_terminal = new DropshipVehicleTerminalDefinition

  val vehicle_terminal_combined = new VehicleTerminalCombinedDefinition

  val spawn_terminal = new MatrixTerminalDefinition(812)

  val respawn_tube = new SpawnTubeDefinition(732)

  val respawn_tube_tower = new SpawnTubeDefinition(733)

  val teleportpad_terminal = new TeleportPadTerminalDefinition

  val adv_med_terminal = new MedicalTerminalDefinition(38)

  val crystals_health_a = new MedicalTerminalDefinition(225)

  val crystals_health_b = new MedicalTerminalDefinition(226)

  val medical_terminal = new MedicalTerminalDefinition(529)

  val pad_landing = new RepairRearmSiloDefinition(719)

  val repair_silo = new RepairRearmSiloDefinition(729)

  val spawn_pad = new VehicleSpawnPadDefinition

  val mb_locker = new LockerDefinition

  val lock_external = new IFFLockDefinition

  val door = new DoorDefinition

  val resource_silo = new ResourceSiloDefinition

  val capture_terminal = new CaptureTerminalDefinition(158) // Base CC

  val secondary_capture = new CaptureTerminalDefinition(751) // Tower CC

  val manned_turret = new TurretDefinition(480) {
    Name = "manned_turret"
    MaxHealth = 3600
    Weapons += 1 -> new mutable.HashMap()
    Weapons(1) += TurretUpgrade.None -> phalanx_sgl_hevgatcan
    Weapons(1) += TurretUpgrade.AVCombo -> phalanx_avcombo
    Weapons(1) += TurretUpgrade.FlakCombo -> phalanx_flakcombo
    MountPoints += 1 -> 0
    FactionLocked = true
    ReserveAmmunition = false
  }

  /**
    * Given a faction, provide the standard assault melee weapon.
    * @param faction the faction
    * @return the `ToolDefinition` for the melee weapon
    */
  def StandardMelee(faction : PlanetSideEmpire.Value) : ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR => chainblade
      case PlanetSideEmpire.NC => magcutter
      case PlanetSideEmpire.VS => forceblade
      case PlanetSideEmpire.NEUTRAL => chainblade //do NOT hand out the katana
    }
  }

  /**
    * Given a faction, provide the satndard assault pistol.
    * @param faction the faction
    * @return the `ToolDefinition` for the pistol
    */
  def StandardPistol(faction : PlanetSideEmpire.Value) : ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR => repeater
      case PlanetSideEmpire.NC => isp
      case PlanetSideEmpire.VS => beamer
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
  def StandardPistolAmmo(faction : PlanetSideEmpire.Value) : AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR => bullet_9mm
      case PlanetSideEmpire.NC => shotgun_shell
      case PlanetSideEmpire.VS => energy_cell
      case PlanetSideEmpire.NEUTRAL => bullet_9mm
    }
  }

  /**
    * For a given faction, provide the medium assault pistol.
    * The medium assault pistols all use the same ammunition so there is no point for a separate selection function.
    * @param faction the faction
    * @return the `ToolDefinition` for the pistol
    */
  def MediumPistol(faction : PlanetSideEmpire.Value) : ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR => anniversary_guna
      case PlanetSideEmpire.NC => anniversary_gun
      case PlanetSideEmpire.VS => anniversary_gunb
      case PlanetSideEmpire.NEUTRAL => ilc9 //do not hand out the spiker
    }
  }

  /**
    * For a given faction, provide the medium assault rifle.
    * For `Neutral` or `Black Ops`, just return a Suppressor.
    * @param faction the faction
    * @return the `ToolDefinition` for the rifle
    */
  def MediumRifle(faction : PlanetSideEmpire.Value) : ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR => cycler
      case PlanetSideEmpire.NC => gauss
      case PlanetSideEmpire.VS => pulsar
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
  def MediumRifleAmmo(faction : PlanetSideEmpire.Value) : AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR => bullet_9mm
      case PlanetSideEmpire.NC => bullet_9mm
      case PlanetSideEmpire.VS => energy_cell
      case PlanetSideEmpire.NEUTRAL => bullet_9mm
    }
  }

  /**
    * For a given faction, provide the heavy assault rifle.
    * For `Neutral` or `Black Ops`, just return a Suppressor.
    * @param faction the faction
    * @return the `ToolDefinition` for the rifle
    */
  def HeavyRifle(faction : PlanetSideEmpire.Value) : ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR => mini_chaingun
      case PlanetSideEmpire.NC => r_shotgun
      case PlanetSideEmpire.VS => lasher
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
  def HeavyRifleAmmo(faction : PlanetSideEmpire.Value) : AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR => bullet_9mm
      case PlanetSideEmpire.NC => shotgun_shell
      case PlanetSideEmpire.VS => energy_cell
      case PlanetSideEmpire.NEUTRAL => bullet_9mm
    }
  }

  /**
    * For a given faction, provide the anti-vehicular launcher.
    * @param faction the faction
    * @return the `ToolDefinition` for the launcher
    */
  def AntiVehicularLauncher(faction : PlanetSideEmpire.Value) : ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR => striker
      case PlanetSideEmpire.NC => hunterseeker
      case PlanetSideEmpire.VS => lancer
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
  def AntiVehicularAmmo(faction : PlanetSideEmpire.Value) : AmmoBoxDefinition = {
    faction match {
      case PlanetSideEmpire.TR => striker_missile_ammo
      case PlanetSideEmpire.NC => hunter_seeker_missile
      case PlanetSideEmpire.VS => lancer_cartridge
      case PlanetSideEmpire.NEUTRAL => phoenix_missile //careful - does not exist as an AmmoBox normally
    }
  }

  def MAXArms(subtype : Int, faction : PlanetSideEmpire.Value) : ToolDefinition = {
    if(subtype == 1) {
      AIMAX(faction)
    }
    else if(subtype == 2) {
      AVMAX(faction)
    }
    else if(subtype == 3) {
      AAMAX(faction)
    }
    else {
      suppressor //there are no common pool MAX arms
    }
  }

  def isMaxArms(tdef : ToolDefinition) : Boolean = {
    tdef match {
      case `trhev_dualcycler` | `nchev_scattercannon` | `vshev_quasar`
           | `trhev_pounder` | `nchev_falcon` | `vshev_comet`
           | `trhev_burster` | `nchev_sparrow` | `vshev_starfire` =>
        true
      case _ =>
        false
    }
  }

  def AIMAX(faction : PlanetSideEmpire.Value) : ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR => trhev_dualcycler
      case PlanetSideEmpire.NC => nchev_scattercannon
      case PlanetSideEmpire.VS => vshev_quasar
      case PlanetSideEmpire.NEUTRAL => suppressor //there are no common pool MAX arms
    }
  }

  def AVMAX(faction : PlanetSideEmpire.Value) : ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR => trhev_pounder
      case PlanetSideEmpire.NC => nchev_falcon
      case PlanetSideEmpire.VS => vshev_comet
      case PlanetSideEmpire.NEUTRAL => suppressor //there are no common pool MAX arms
    }
  }

  def AAMAX(faction : PlanetSideEmpire.Value) : ToolDefinition = {
    faction match {
      case PlanetSideEmpire.TR => trhev_burster
      case PlanetSideEmpire.NC => nchev_sparrow
      case PlanetSideEmpire.VS => vshev_starfire
      case PlanetSideEmpire.NEUTRAL => suppressor //there are no common pool MAX arms
    }
  }

  def PortableMannedTurret(faction :PlanetSideEmpire.Value) : TurretDeployableDefinition = {
    faction match {
      case PlanetSideEmpire.TR => portable_manned_turret_tr
      case PlanetSideEmpire.NC => portable_manned_turret_nc
      case PlanetSideEmpire.VS => portable_manned_turret_vs
      case PlanetSideEmpire.NEUTRAL => portable_manned_turret
    }
  }

  /**
    * Using the definition for a piece of `Equipment` determine if it is a grenade-type weapon.
    * Only the normal grenades count; the grenade packs are excluded.
    * @param edef the `EquipmentDefinition` of the item
    * @return `true`, if it is a grenade-type weapon; `false`, otherwise
    */
  def isGrenade(edef : EquipmentDefinition) : Boolean = {
    edef match {
      case `frag_grenade` | `jammer_grenade` | `plasma_grenade` =>
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
  def isGrenadePack(edef : EquipmentDefinition) : Boolean = {
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
  def isFactionWeapon(edef : EquipmentDefinition) : PlanetSideEmpire.Value = {
    edef match {
      case `chainblade` | `repeater` | `anniversary_guna` | `cycler` | `mini_chaingun` | `striker` | `trhev_dualcycler` | `trhev_pounder` | `trhev_burster` =>
        PlanetSideEmpire.TR
      case `magcutter` | `isp` | `anniversary_gun` | `gauss` | `r_shotgun` | `hunterseeker` | `nchev_scattercannon` | `nchev_falcon` | `nchev_sparrow` =>
        PlanetSideEmpire.NC
      case `forceblade` | `beamer` | `anniversary_gunb` | `pulsar` | `lasher` | `lancer` | `vshev_quasar` | `vshev_comet` | `vshev_starfire` =>
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
  def isFactionEquipment(edef : EquipmentDefinition) : PlanetSideEmpire.Value = {
    edef match {
      case `chainblade` | `repeater` | `anniversary_guna` | `cycler` | `mini_chaingun` | `striker` | `striker_missile_ammo` | `trhev_dualcycler` | `trhev_pounder` | `trhev_burster` | `dualcycler_ammo` | `pounder_ammo` | `burster_ammo` =>
        PlanetSideEmpire.TR
      case `magcutter` | `isp` | `anniversary_gun` | `gauss` | `r_shotgun` | `hunterseeker` | `hunter_seeker_missile` | `nchev_scattercannon` | `nchev_falcon` | `nchev_sparrow` | `scattercannon_ammo` | `falcon_ammo` | `sparrow_ammo` =>
        PlanetSideEmpire.NC
      case `forceblade` | `beamer` | `anniversary_gunb` | `pulsar` | `lasher` | `lancer` | `energy_cell` | `lancer_cartridge` | `vshev_quasar` | `vshev_comet` | `vshev_starfire` | `quasar_ammo` | `comet_ammo` | `starfire_ammo` =>
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
  def isCavernWeapon(edef : EquipmentDefinition) : Boolean = {
    edef match {
      case `spiker` | `maelstrom` | `radiator` => true
      case _ => false
    }
  }

  /**
    * Using the definition for a piece of `Equipment` determine whether it is "cavern equipment."
    * @param edef the `EquipmentDefinition` of the item
    * @return `true`, if it is; otherwise, `false`
    */
  def isCavernEquipment(edef : EquipmentDefinition) : Boolean = {
    edef match {
      case `spiker` | `maelstrom` | `radiator` | `ancient_ammo_combo` | `maelstrom_ammo` => true
      case _ => false
    }
  }

  /**
    *  Using the definition for a piece of `Equipment` determine whether it is "special."
    * "Special equipment" is any non-standard `Equipment` that, while it can be obtained from a `Terminal`, has artificial prerequisites.
    * For example, the Kits are unlocked as rewards for holiday events and require possessing a specific `MeritCommendation`.
    * @param edef the `EquipmentDefinition` of the item
    * @return `true`, if it is; otherwise, `false`
    */
  def isSpecialEquipment(edef : EquipmentDefinition) : Boolean = {
    edef match {
      case `super_medkit` | `super_armorkit` | `super_staminakit` | `katana` =>
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
  def isFlightVehicle(vdef : VehicleDefinition) : Boolean = {
    vdef match {
      case `mosquito` | `lightgunship` | `wasp` | `liberator` | `vulture` | `phantasm` | `lodestar` | `dropship` | `galaxy_gunship` =>
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
  def isHoverVehicle(vdef : VehicleDefinition) : Boolean = {
    vdef match {
      case `twomanhoverbuggy` | `magrider` | `router` | `flail` =>
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
  def canStationaryRotate(vdef : VehicleDefinition) : Boolean = {
    if(isFlightVehicle(vdef) || isHoverVehicle(vdef)) {
      true
    }
    else {
      vdef match {
        case `lightning` | `prowler` | `vanguard` =>
          true
        case _ =>
          false
      }
    }
  }

  /**
    * Initialize `KitDefinition` globals.
    */
  private def init_kit() : Unit = {
    medkit.Name = "medkit"

    super_medkit.Name = "super_medkit"

    super_armorkit.Name = "super_armorkit"

    super_staminakit.Name = "super_staminakit"
  }

  /**
    * Initialize `AmmoBoxDefinition` globals.
    */
  private def init_ammo() : Unit = {
    melee_ammo.Name = "melee_ammo"
    melee_ammo.Size = EquipmentSize.Blocked

    frag_grenade_ammo.Name = "frag_grenade_ammo"
    frag_grenade_ammo.Size = EquipmentSize.Blocked

    jammer_grenade_ammo.Name = "jammer_grenade_ammo"
    jammer_grenade_ammo.Size = EquipmentSize.Blocked

    plasma_grenade_ammo.Name = "plasma_grenade_ammo"
    plasma_grenade_ammo.Size = EquipmentSize.Blocked

    bullet_9mm.Name = "bullet_9mm"
    bullet_9mm.Capacity = 50
    bullet_9mm.Tile = InventoryTile.Tile33

    bullet_9mm_AP.Name="bullet_9mm_AP"
    bullet_9mm_AP.Capacity = 50
    bullet_9mm_AP.Tile = InventoryTile.Tile33

    shotgun_shell.Name ="shotgun_shell"
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
    armor_canister.Tile = InventoryTile.Tile23

    upgrade_canister.Name = "upgrade_canister"
    upgrade_canister.Capacity = 1
    upgrade_canister.Tile = InventoryTile.Tile23

    trek_ammo.Name = "trek_ammo"
    trek_ammo.Size = EquipmentSize.Blocked

    bullet_35mm.Name = "bullet_35mm"
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

    bullet_25mm.Name = "bullet_25mm"
    bullet_25mm.Capacity = 150
    bullet_25mm.Tile = InventoryTile.Tile44

    bullet_75mm.Name = "bullet_75mm"
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

    bullet_20mm.Name = "bullet_20mm"
    bullet_20mm.Capacity = 200
    bullet_20mm.Tile = InventoryTile.Tile44

    bullet_12mm.Name = "bullet_12mm"
    bullet_12mm.Capacity = 300
    bullet_12mm.Tile = InventoryTile.Tile44

    wasp_rocket_ammo.Name = "wasp_rocket_ammo"
    wasp_rocket_ammo.Capacity = 6
    wasp_rocket_ammo.Tile = InventoryTile.Tile44

    wasp_gun_ammo.Name = "wasp_gun_ammo"
    wasp_gun_ammo.Capacity = 150
    wasp_gun_ammo.Tile = InventoryTile.Tile44

    bullet_15mm.Name = "bullet_15mm"
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

    bullet_105mm.Name = "bullet_105mm"
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

    bullet_150mm.Name = "bullet_150mm"
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
  }

  /**
    * Initialize `ProjectileDefinition` globals.
    */
  private def init_projectile() : Unit = {
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

    bullet_12mm_projectile.Name= "12mmbullet_projectile"
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

    bullet_75mm_projectile.Name = "75mmbullet_projectile"
    bullet_75mm_projectile.Damage0 = 75
    bullet_75mm_projectile.Damage1 = 125
    bullet_75mm_projectile.DamageAtEdge = 0.1f
    bullet_75mm_projectile.DamageRadius = 5f
    bullet_75mm_projectile.ProjectileDamageType = DamageType.Splash
    bullet_75mm_projectile.InitialVelocity = 100
    bullet_75mm_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(bullet_75mm_projectile)

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
    ProjectileDefinition.CalculateDerivedFields(anniversary_projectileb)

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

    aphelion_plasma_rocket_projectile.Name = "aphelion_plasma_rocket_projectile"
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
    aphelion_plasma_rocket_projectile.InitialVelocity = 75
    aphelion_plasma_rocket_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(aphelion_plasma_rocket_projectile)

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
    ProjectileDefinition.CalculateDerivedFields(aphelion_starfire_projectile)

    bolt_projectile.Name = "bolt_projectile"
    bolt_projectile.Damage0 = 100
    bolt_projectile.Damage1 = 50
    bolt_projectile.Damage2 = 50
    bolt_projectile.Damage3 = 50
    bolt_projectile.Damage4 = 75
    bolt_projectile.ProjectileDamageType = DamageType.Splash
    bolt_projectile.InitialVelocity = 500
    bolt_projectile.Lifespan = 1.0f
    ProjectileDefinition.CalculateDerivedFields(bolt_projectile)

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

    chainblade_projectile.Name = "chainblade_projectile"
    // TODO for later, maybe : set_resource_parent chainblade_projectile game_objects melee_ammo_projectile
    chainblade_projectile.Damage0 = 50
    chainblade_projectile.Damage1 = 0
    chainblade_projectile.ProjectileDamageType = DamageType.Direct
    chainblade_projectile.InitialVelocity = 100
    chainblade_projectile.Lifespan = .02f
    ProjectileDefinition.CalculateDerivedFields(chainblade_projectile)

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
    comet_projectile.InitialVelocity = 80
    comet_projectile.Lifespan = 3.1f
    ProjectileDefinition.CalculateDerivedFields(comet_projectile)

    dualcycler_projectile.Name = "dualcycler_projectile"
    dualcycler_projectile.Damage0 = 18
    dualcycler_projectile.Damage1 = 10
    dualcycler_projectile.ProjectileDamageType = DamageType.Direct
    dualcycler_projectile.DegradeDelay = .025f
    dualcycler_projectile.DegradeMultiplier = .5f
    dualcycler_projectile.InitialVelocity = 500
    dualcycler_projectile.Lifespan = 0.5f
    ProjectileDefinition.CalculateDerivedFields(dualcycler_projectile)

    dynomite_projectile.Name = "dynomite_projectile"
    // TODO for later, maybe : set_resource_parent dynomite_projectile game_objects frag_grenade_projectile_enh
    dynomite_projectile.Damage0 = 75
    dynomite_projectile.Damage1 = 175
    dynomite_projectile.DamageAtEdge = 0.1f
    dynomite_projectile.DamageRadius = 10f
    dynomite_projectile.ProjectileDamageType = DamageType.Splash
    dynomite_projectile.InitialVelocity = 30
    dynomite_projectile.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(dynomite_projectile)

    energy_cell_projectile.Name = "energy_cell_projectile"
    energy_cell_projectile.Damage0 = 18
    energy_cell_projectile.Damage1 = 10
    energy_cell_projectile.ProjectileDamageType = DamageType.Direct
    energy_cell_projectile.DegradeDelay = 0.05f
    energy_cell_projectile.DegradeMultiplier = 0.4f
    energy_cell_projectile.InitialVelocity = 500
    energy_cell_projectile.Lifespan = .4f
    energy_cell_projectile.UseDamage1Subtract = true
    ProjectileDefinition.CalculateDerivedFields(energy_cell_projectile)

    energy_gun_nc_projectile.Name = "energy_gun_nc_projectile"
    energy_gun_nc_projectile.Damage0 = 10
    energy_gun_nc_projectile.Damage1 = 13
    energy_gun_nc_projectile.ProjectileDamageType = DamageType.Direct
    energy_gun_nc_projectile.InitialVelocity = 500
    energy_gun_nc_projectile.Lifespan = 0.5f
    ProjectileDefinition.CalculateDerivedFields(energy_gun_nc_projectile)

    energy_gun_tr_projectile.Name = "energy_gun_tr_projectile"
    energy_gun_tr_projectile.Damage0 = 14
    energy_gun_tr_projectile.Damage1 = 18
    energy_gun_tr_projectile.ProjectileDamageType = DamageType.Direct
    energy_gun_tr_projectile.DegradeDelay = .025f
    energy_gun_tr_projectile.DegradeMultiplier = .5f
    energy_gun_tr_projectile.InitialVelocity = 500
    energy_gun_tr_projectile.Lifespan = 0.5f
    ProjectileDefinition.CalculateDerivedFields(energy_gun_tr_projectile)

    energy_gun_vs_projectile.Name = "energy_gun_vs_projectile"
    energy_gun_vs_projectile.Damage0 = 25
    energy_gun_vs_projectile.Damage1 = 35
    energy_gun_vs_projectile.ProjectileDamageType = DamageType.Direct
    energy_gun_vs_projectile.DegradeDelay = 0.045f
    energy_gun_vs_projectile.DegradeMultiplier = 0.5f
    energy_gun_vs_projectile.InitialVelocity = 500
    energy_gun_vs_projectile.Lifespan = .5f
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
    flail_projectile.DegradeMultiplier = 5f
    flail_projectile.InitialVelocity = 75
    flail_projectile.Lifespan = 40f
    ProjectileDefinition.CalculateDerivedFields(flail_projectile)

    flamethrower_fireball.Name = "flamethrower_fireball"
    flamethrower_fireball.Damage0 = 30
    flamethrower_fireball.Damage1 = 0
    flamethrower_fireball.Damage2 = 0
    flamethrower_fireball.Damage3 = 20
    flamethrower_fireball.Damage4 = 0
    flamethrower_fireball.DamageAtEdge = 0.15f
    flamethrower_fireball.DamageRadius = 5f
    flamethrower_fireball.ProjectileDamageType = DamageType.Aggravated
    flamethrower_fireball.InitialVelocity = 15
    flamethrower_fireball.Lifespan = 1.2f
    ProjectileDefinition.CalculateDerivedFields(flamethrower_fireball)

    flamethrower_projectile.Name = "flamethrower_projectile"
    flamethrower_projectile.Damage0 = 10
    flamethrower_projectile.Damage1 = 0
    flamethrower_projectile.Damage2 = 0
    flamethrower_projectile.Damage3 = 4
    flamethrower_projectile.Damage4 = 0
    flamethrower_projectile.Acceleration = -5
    flamethrower_projectile.AccelerationUntil = 2f
    flamethrower_projectile.ProjectileDamageType = DamageType.Aggravated
    flamethrower_projectile.DegradeDelay = 1.0f
    flamethrower_projectile.DegradeMultiplier = 0.5f
    flamethrower_projectile.InitialVelocity = 10
    flamethrower_projectile.Lifespan = 2.0f
    ProjectileDefinition.CalculateDerivedFields(flamethrower_projectile)

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

    forceblade_projectile.Name = "forceblade_projectile"
    // TODO for later, maybe : set_resource_parent forceblade_projectile game_objects melee_ammo_projectile
    forceblade_projectile.Damage0 = 50
    forceblade_projectile.Damage1 = 0
    forceblade_projectile.ProjectileDamageType = DamageType.Direct
    forceblade_projectile.InitialVelocity = 100
    forceblade_projectile.Lifespan = .02f
    ProjectileDefinition.CalculateDerivedFields(forceblade_projectile)

    frag_cartridge_projectile.Name = "frag_cartridge_projectile"
    // TODO for later, maybe : set_resource_parent frag_cartridge_projectile game_objects frag_grenade_projectile
    frag_cartridge_projectile.Damage0 = 75
    frag_cartridge_projectile.Damage1 = 100
    frag_cartridge_projectile.DamageAtEdge = 0.1f
    frag_cartridge_projectile.DamageRadius = 7f
    frag_cartridge_projectile.ProjectileDamageType = DamageType.Splash
    frag_cartridge_projectile.InitialVelocity = 30
    frag_cartridge_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(frag_cartridge_projectile)

    frag_cartridge_projectile_b.Name = "frag_cartridge_projectile_b"
    // TODO for later, maybe : set_resource_parent frag_cartridge_projectile_b game_objects frag_grenade_projectile_enh
    frag_cartridge_projectile_b.Damage0 = 75
    frag_cartridge_projectile_b.Damage1 = 100
    frag_cartridge_projectile_b.DamageAtEdge = 0.1f
    frag_cartridge_projectile_b.DamageRadius = 5f
    frag_cartridge_projectile_b.ProjectileDamageType = DamageType.Splash
    frag_cartridge_projectile_b.InitialVelocity = 30
    frag_cartridge_projectile_b.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(frag_cartridge_projectile_b)

    frag_grenade_projectile.Name = "frag_grenade_projectile"
    frag_grenade_projectile.Damage0 = 75
    frag_grenade_projectile.Damage1 = 100
    frag_grenade_projectile.DamageAtEdge = 0.1f
    frag_grenade_projectile.DamageRadius = 7f
    frag_grenade_projectile.ProjectileDamageType = DamageType.Splash
    frag_grenade_projectile.InitialVelocity = 30
    frag_grenade_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(frag_grenade_projectile)

    frag_grenade_projectile_enh.Name = "frag_grenade_projectile_enh"
    // TODO for later, maybe : set_resource_parent frag_grenade_projectile_enh game_objects frag_grenade_projectile
    frag_grenade_projectile_enh.Damage0 = 75
    frag_grenade_projectile_enh.Damage1 = 100
    frag_grenade_projectile_enh.DamageAtEdge = 0.1f
    frag_grenade_projectile_enh.DamageRadius = 7f
    frag_grenade_projectile_enh.ProjectileDamageType = DamageType.Splash
    frag_grenade_projectile_enh.InitialVelocity = 30
    frag_grenade_projectile_enh.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(frag_grenade_projectile_enh)

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

    grenade_projectile.Name = "grenade_projectile"
    grenade_projectile.Damage0 = 50
    grenade_projectile.DamageAtEdge = 0.2f
    grenade_projectile.DamageRadius = 100f
    grenade_projectile.ProjectileDamageType = DamageType.Splash
    grenade_projectile.InitialVelocity = 15
    grenade_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(grenade_projectile)

    heavy_grenade_projectile.Name = "heavy_grenade_projectile"
    heavy_grenade_projectile.Damage0 = 50
    heavy_grenade_projectile.Damage1 = 82
    heavy_grenade_projectile.Damage2 = 82
    heavy_grenade_projectile.Damage3 = 75
    heavy_grenade_projectile.Damage4 = 66
    heavy_grenade_projectile.DamageAtEdge = 0.1f
    heavy_grenade_projectile.DamageRadius = 5f
    heavy_grenade_projectile.ProjectileDamageType = DamageType.Splash
    heavy_grenade_projectile.InitialVelocity = 75
    heavy_grenade_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(heavy_grenade_projectile)

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

    heavy_sniper_projectile.Name = "heavy_sniper_projectile"
    heavy_sniper_projectile.Damage0 = 55
    heavy_sniper_projectile.Damage1 = 28
    heavy_sniper_projectile.Damage2 = 28
    heavy_sniper_projectile.Damage3 = 28
    heavy_sniper_projectile.Damage4 = 42
    heavy_sniper_projectile.ProjectileDamageType = DamageType.Splash
    heavy_sniper_projectile.InitialVelocity = 500
    heavy_sniper_projectile.Lifespan = 1.0f
    ProjectileDefinition.CalculateDerivedFields(heavy_sniper_projectile)

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
    ProjectileDefinition.CalculateDerivedFields(hunter_seeker_missile_projectile)

    jammer_cartridge_projectile.Name = "jammer_cartridge_projectile"
    // TODO for later, maybe : set_resource_parent jammer_cartridge_projectile game_objects jammer_grenade_projectile
    jammer_cartridge_projectile.Damage0 = 0
    jammer_cartridge_projectile.Damage1 = 0
    jammer_cartridge_projectile.DamageAtEdge = 1.0f
    jammer_cartridge_projectile.DamageRadius = 10f
    jammer_cartridge_projectile.ProjectileDamageType = DamageType.Splash
    jammer_cartridge_projectile.InitialVelocity = 30
    jammer_cartridge_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(jammer_cartridge_projectile)

    jammer_cartridge_projectile_b.Name = "jammer_cartridge_projectile_b"
    // TODO for later, maybe : set_resource_parent jammer_cartridge_projectile_b game_objects jammer_grenade_projectile_enh
    jammer_cartridge_projectile_b.Damage0 = 0
    jammer_cartridge_projectile_b.Damage1 = 0
    jammer_cartridge_projectile_b.DamageAtEdge = 1.0f
    jammer_cartridge_projectile_b.DamageRadius = 10f
    jammer_cartridge_projectile_b.ProjectileDamageType = DamageType.Splash
    jammer_cartridge_projectile_b.InitialVelocity = 30
    jammer_cartridge_projectile_b.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(jammer_cartridge_projectile_b)

    jammer_grenade_projectile.Name = "jammer_grenade_projectile"
    jammer_grenade_projectile.Damage0 = 0
    jammer_grenade_projectile.Damage1 = 0
    jammer_grenade_projectile.DamageAtEdge = 1.0f
    jammer_grenade_projectile.DamageRadius = 10f
    jammer_grenade_projectile.ProjectileDamageType = DamageType.Splash
    jammer_grenade_projectile.InitialVelocity = 30
    jammer_grenade_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(jammer_grenade_projectile)

    jammer_grenade_projectile_enh.Name = "jammer_grenade_projectile_enh"
    // TODO for later, maybe : set_resource_parent jammer_grenade_projectile_enh game_objects jammer_grenade_projectile
    jammer_grenade_projectile_enh.Damage0 = 0
    jammer_grenade_projectile_enh.Damage1 = 0
    jammer_grenade_projectile_enh.DamageAtEdge = 1.0f
    jammer_grenade_projectile_enh.DamageRadius = 10f
    jammer_grenade_projectile_enh.ProjectileDamageType = DamageType.Splash
    jammer_grenade_projectile_enh.InitialVelocity = 30
    jammer_grenade_projectile_enh.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(jammer_grenade_projectile_enh)

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
    lasher_projectile.Lifespan = 0.75f
    ProjectileDefinition.CalculateDerivedFields(lasher_projectile)

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
    lasher_projectile_ap.Lifespan = 0.75f
    ProjectileDefinition.CalculateDerivedFields(lasher_projectile_ap)

    liberator_bomb_cluster_bomblet_projectile.Name = "liberator_bomb_cluster_bomblet_projectile"
    liberator_bomb_cluster_bomblet_projectile.Damage0 = 75
    liberator_bomb_cluster_bomblet_projectile.Damage1 = 100
    liberator_bomb_cluster_bomblet_projectile.DamageAtEdge = 0.25f
    liberator_bomb_cluster_bomblet_projectile.DamageRadius = 3f
    liberator_bomb_cluster_bomblet_projectile.ProjectileDamageType = DamageType.Splash
    liberator_bomb_cluster_bomblet_projectile.InitialVelocity = 0
    liberator_bomb_cluster_bomblet_projectile.Lifespan = 30f
    ProjectileDefinition.CalculateDerivedFields(liberator_bomb_cluster_bomblet_projectile)

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

    maelstrom_grenade_projectile.Name = "maelstrom_grenade_projectile"
    maelstrom_grenade_projectile.Damage0 = 32
    maelstrom_grenade_projectile.Damage1 = 60
    maelstrom_grenade_projectile.DamageRadius = 20.0f
    maelstrom_grenade_projectile.ProjectileDamageType = DamageType.Direct
    maelstrom_grenade_projectile.InitialVelocity = 30
    maelstrom_grenade_projectile.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(maelstrom_grenade_projectile)

    maelstrom_grenade_projectile_contact.Name = "maelstrom_grenade_projectile_contact"
    // TODO for later, maybe : set_resource_parent maelstrom_grenade_projectile_contact game_objects maelstrom_grenade_projectile
    maelstrom_grenade_projectile_contact.Damage0 = 32
    maelstrom_grenade_projectile_contact.Damage1 = 60
    maelstrom_grenade_projectile_contact.DamageRadius = 20.0f
    maelstrom_grenade_projectile_contact.ProjectileDamageType = DamageType.Direct
    maelstrom_grenade_projectile_contact.InitialVelocity = 30
    maelstrom_grenade_projectile_contact.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(maelstrom_grenade_projectile_contact)

    maelstrom_stream_projectile.Name = "maelstrom_stream_projectile"
    maelstrom_stream_projectile.Damage0 = 15
    maelstrom_stream_projectile.Damage1 = 6
    maelstrom_stream_projectile.ProjectileDamageType = DamageType.Direct
    maelstrom_stream_projectile.DegradeDelay = .075f
    maelstrom_stream_projectile.DegradeMultiplier = 0.5f
    maelstrom_stream_projectile.InitialVelocity = 200
    maelstrom_stream_projectile.Lifespan = 0.2f
    ProjectileDefinition.CalculateDerivedFields(maelstrom_stream_projectile)

    magcutter_projectile.Name = "magcutter_projectile"
    // TODO for later, maybe : set_resource_parent magcutter_projectile game_objects melee_ammo_projectile
    magcutter_projectile.Damage0 = 50
    magcutter_projectile.Damage1 = 0
    magcutter_projectile.ProjectileDamageType = DamageType.Direct
    magcutter_projectile.InitialVelocity = 100
    magcutter_projectile.Lifespan = .02f
    ProjectileDefinition.CalculateDerivedFields(magcutter_projectile)

    melee_ammo_projectile.Name = "melee_ammo_projectile"
    melee_ammo_projectile.Damage0 = 25
    melee_ammo_projectile.Damage1 = 0
    melee_ammo_projectile.ProjectileDamageType = DamageType.Direct
    melee_ammo_projectile.InitialVelocity = 100
    melee_ammo_projectile.Lifespan = .02f
    ProjectileDefinition.CalculateDerivedFields(melee_ammo_projectile)

    meteor_common.Name = "meteor_common"
    meteor_common.DamageAtEdge = .1f
    meteor_common.ProjectileDamageType = DamageType.Splash
    meteor_common.InitialVelocity = 0
    meteor_common.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_common)

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

    mine_projectile.Name = "mine_projectile"
    mine_projectile.Lifespan = 0.01f
    mine_projectile.InitialVelocity = 300
    ProjectileDefinition.CalculateDerivedFields(mine_projectile)

    mine_sweeper_projectile.Name = "mine_sweeper_projectile"
    mine_sweeper_projectile.Damage0 = 0
    mine_sweeper_projectile.Damage1 = 0
    mine_sweeper_projectile.DamageAtEdge = .33f
    mine_sweeper_projectile.DamageRadius = 25f
    mine_sweeper_projectile.ProjectileDamageType = DamageType.Splash
    mine_sweeper_projectile.InitialVelocity = 30
    mine_sweeper_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(mine_sweeper_projectile)

    mine_sweeper_projectile_enh.Name = "mine_sweeper_projectile_enh"
    mine_sweeper_projectile_enh.Damage0 = 0
    mine_sweeper_projectile_enh.Damage1 = 0
    mine_sweeper_projectile_enh.DamageAtEdge = 0.33f
    mine_sweeper_projectile_enh.DamageRadius = 25f
    mine_sweeper_projectile_enh.InitialVelocity = 30
    mine_sweeper_projectile_enh.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(mine_sweeper_projectile_enh)

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
    ProjectileDefinition.CalculateDerivedFields(oicw_projectile)

    pellet_gun_projectile.Name = "pellet_gun_projectile"
    // TODO for later, maybe : set_resource_parent pellet_gun_projectile game_objects shotgun_shell_projectile
    pellet_gun_projectile.Damage0 = 12
    pellet_gun_projectile.Damage1 = 8
    pellet_gun_projectile.ProjectileDamageType = DamageType.Direct
    pellet_gun_projectile.InitialVelocity = 400
    pellet_gun_projectile.Lifespan = 0.1875f
    pellet_gun_projectile.UseDamage1Subtract = false
    ProjectileDefinition.CalculateDerivedFields(pellet_gun_projectile)

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
    peregrine_particle_cannon_projectile.InitialVelocity = 500
    peregrine_particle_cannon_projectile.Lifespan = .6f
    ProjectileDefinition.CalculateDerivedFields(peregrine_particle_cannon_projectile)

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
    ProjectileDefinition.CalculateDerivedFields(peregrine_sparrow_projectile)

    phalanx_av_projectile.Name = "phalanx_av_projectile"
    phalanx_av_projectile.Damage0 = 60
    phalanx_av_projectile.Damage1 = 140
    phalanx_av_projectile.DamageAtEdge = 0.1f
    phalanx_av_projectile.DamageRadius = 5f
    phalanx_av_projectile.ProjectileDamageType = DamageType.Splash
    phalanx_av_projectile.InitialVelocity = 100
    phalanx_av_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(phalanx_av_projectile)

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
    ProjectileDefinition.CalculateDerivedFields(phoenix_missile_guided_projectile)

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

    plasma_cartridge_projectile.Name = "plasma_cartridge_projectile"
    // TODO for later, maybe : set_resource_parent plasma_cartridge_projectile game_objects plasma_grenade_projectile
    plasma_cartridge_projectile.Damage0 = 20
    plasma_cartridge_projectile.Damage1 = 15
    plasma_cartridge_projectile.DamageAtEdge = 0.2f
    plasma_cartridge_projectile.DamageRadius = 7f
    plasma_cartridge_projectile.ProjectileDamageType = DamageType.Aggravated
    plasma_cartridge_projectile.InitialVelocity = 30
    plasma_cartridge_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(plasma_cartridge_projectile)

    plasma_cartridge_projectile_b.Name = "plasma_cartridge_projectile_b"
    // TODO for later, maybe : set_resource_parent plasma_cartridge_projectile_b game_objects plasma_grenade_projectile_B
    plasma_cartridge_projectile_b.Damage0 = 20
    plasma_cartridge_projectile_b.Damage1 = 15
    plasma_cartridge_projectile_b.DamageAtEdge = 0.2f
    plasma_cartridge_projectile_b.DamageRadius = 7f
    plasma_cartridge_projectile_b.ProjectileDamageType = DamageType.Aggravated
    plasma_cartridge_projectile_b.InitialVelocity = 30
    plasma_cartridge_projectile_b.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(plasma_cartridge_projectile_b)

    plasma_grenade_projectile.Name = "plasma_grenade_projectile"
    plasma_grenade_projectile.Damage0 = 40
    plasma_grenade_projectile.Damage1 = 30
    plasma_grenade_projectile.DamageAtEdge = 0.1f
    plasma_grenade_projectile.DamageRadius = 7f
    plasma_grenade_projectile.ProjectileDamageType = DamageType.Aggravated
    plasma_grenade_projectile.InitialVelocity = 30
    plasma_grenade_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(plasma_grenade_projectile)

    plasma_grenade_projectile_B.Name = "plasma_grenade_projectile_B"
    // TODO for later, maybe : set_resource_parent plasma_grenade_projectile_B game_objects plasma_grenade_projectile
    plasma_grenade_projectile_B.Damage0 = 40
    plasma_grenade_projectile_B.Damage1 = 30
    plasma_grenade_projectile_B.DamageAtEdge = 0.1f
    plasma_grenade_projectile_B.DamageRadius = 7f
    plasma_grenade_projectile_B.ProjectileDamageType = DamageType.Aggravated
    plasma_grenade_projectile_B.InitialVelocity = 30
    plasma_grenade_projectile_B.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(plasma_grenade_projectile_B)

    pounder_projectile.Name = "pounder_projectile"
    pounder_projectile.Damage0 = 31
    pounder_projectile.Damage1 = 120
    pounder_projectile.Damage2 = 120
    pounder_projectile.Damage3 = 75
    pounder_projectile.Damage4 = 132
    pounder_projectile.DamageAtEdge = 0.1f
    pounder_projectile.DamageRadius = 1f
    pounder_projectile.ProjectileDamageType = DamageType.Splash
    pounder_projectile.InitialVelocity = 120
    pounder_projectile.Lifespan = 2.5f
    ProjectileDefinition.CalculateDerivedFields(pounder_projectile)

    pounder_projectile_enh.Name = "pounder_projectile_enh"
    // TODO for later, maybe : set_resource_parent pounder_projectile_enh game_objects pounder_projectile
    pounder_projectile_enh.Damage0 = 31
    pounder_projectile_enh.Damage1 = 120
    pounder_projectile_enh.Damage2 = 120
    pounder_projectile_enh.Damage3 = 75
    pounder_projectile_enh.Damage4 = 132
    pounder_projectile_enh.DamageAtEdge = 0.1f
    pounder_projectile_enh.DamageRadius = 1f
    pounder_projectile_enh.ProjectileDamageType = DamageType.Splash
    pounder_projectile_enh.InitialVelocity = 120
    pounder_projectile_enh.Lifespan = 3.2f
    ProjectileDefinition.CalculateDerivedFields(pounder_projectile_enh)

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
    ProjectileDefinition.CalculateDerivedFields(pulsar_projectile)

    quasar_projectile.Name = "quasar_projectile"
    quasar_projectile.Damage0 = 18
    quasar_projectile.Damage1 = 8
    quasar_projectile.ProjectileDamageType = DamageType.Direct
    quasar_projectile.DegradeDelay = 0.045f
    quasar_projectile.DegradeMultiplier = 0.5f
    quasar_projectile.InitialVelocity = 500
    quasar_projectile.Lifespan = .4f
    ProjectileDefinition.CalculateDerivedFields(quasar_projectile)

    radiator_grenade_projectile.Name = "radiator_grenade_projectile" // Todo : Radiator damages ?
    radiator_grenade_projectile.ProjectileDamageType = DamageType.Direct
    radiator_grenade_projectile.InitialVelocity = 30
    radiator_grenade_projectile.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(radiator_grenade_projectile)

    radiator_sticky_projectile.Name = "radiator_sticky_projectile"
    // TODO for later, maybe : set_resource_parent radiator_sticky_projectile game_objects radiator_grenade_projectile
    radiator_sticky_projectile.ProjectileDamageType = DamageType.Direct
    radiator_sticky_projectile.InitialVelocity = 30
    radiator_sticky_projectile.Lifespan = 4f
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

    scattercannon_projectile.Name = "scattercannon_projectile"
    scattercannon_projectile.Damage0 = 11
    scattercannon_projectile.Damage1 = 5
    scattercannon_projectile.ProjectileDamageType = DamageType.Direct
    scattercannon_projectile.InitialVelocity = 400
    scattercannon_projectile.Lifespan = 0.25f
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
    ProjectileDefinition.CalculateDerivedFields(shotgun_shell_AP_projectile)

    shotgun_shell_projectile.Name = "shotgun_shell_projectile"
    shotgun_shell_projectile.Damage0 = 12
    shotgun_shell_projectile.Damage1 = 5
    shotgun_shell_projectile.ProjectileDamageType = DamageType.Direct
    shotgun_shell_projectile.InitialVelocity = 400
    shotgun_shell_projectile.Lifespan = 0.25f
    shotgun_shell_projectile.UseDamage1Subtract = true
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
    ProjectileDefinition.CalculateDerivedFields(sparrow_projectile)

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
    ProjectileDefinition.CalculateDerivedFields(sparrow_secondary_projectile)

    spiker_projectile.Name = "spiker_projectile"
    //    spiker_projectile.Damage0 = 75
    spiker_projectile.Damage0 = 20
    //    spiker_projectile.Damage0_min = 20
    //    spiker_projectile.Damage1 = 75
    spiker_projectile.Damage1 = 20
    //    spiker_projectile.Damage1_min = 20
    spiker_projectile.DamageAtEdge = 0.1f
    spiker_projectile.DamageRadius = 5f
    spiker_projectile.DamageRadius = 1f
    spiker_projectile.ProjectileDamageType = DamageType.Splash
    spiker_projectile.InitialVelocity = 40
    spiker_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(spiker_projectile)

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

    spitfire_ammo_projectile.Name = "spitfire_ammo_projectile"
    spitfire_ammo_projectile.Damage0 = 15
    spitfire_ammo_projectile.Damage1 = 10
    spitfire_ammo_projectile.ProjectileDamageType = DamageType.Direct
    spitfire_ammo_projectile.DegradeDelay = .01f
    spitfire_ammo_projectile.DegradeMultiplier = 0.5f
    spitfire_ammo_projectile.InitialVelocity = 100
    spitfire_ammo_projectile.Lifespan = .5f
    ProjectileDefinition.CalculateDerivedFields(spitfire_ammo_projectile)

    starfire_projectile.Name = "starfire_projectile"
    starfire_projectile.Damage0 = 16
    starfire_projectile.Damage1 = 20
    starfire_projectile.Damage2 = 58
    starfire_projectile.Acceleration = 12
    starfire_projectile.AccelerationUntil = 5f
    starfire_projectile.ProjectileDamageType = DamageType.Aggravated
    starfire_projectile.InitialVelocity = 45
    starfire_projectile.Lifespan = 7.8f
    ProjectileDefinition.CalculateDerivedFields(starfire_projectile)

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
    ProjectileDefinition.CalculateDerivedFields(striker_missile_targeting_projectile)

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
    ProjectileDefinition.CalculateDerivedFields(trek_projectile)

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
    ProjectileDefinition.CalculateDerivedFields(wasp_rocket_projectile)

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
    * Initialize `ToolDefinition` globals.
    */
  private def init_tools() : Unit = {
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
    repeater.FireModes.head.Modifiers.Damage0 = 2
    repeater.FireModes.head.Modifiers.Damage1 = -3
    repeater.FireModes.head.Modifiers.Damage2 = -3
    repeater.FireModes.head.Modifiers.Damage3 = -3
    repeater.FireModes.head.Modifiers.Damage4 = -3
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
    isp.FireModes.head.Magazine = 8
    isp.FireModes.head.Chamber = 6 //8 shells x 6 pellets = 48
    isp.FireModes.head.Modifiers.Damage0 = 1
    isp.FireModes.head.Modifiers.Damage2 = 1
    isp.FireModes.head.Modifiers.Damage3 = 1
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
    beamer.FireModes.head.Modifiers.Damage0 = 4
    beamer.FireModes.head.Modifiers.Damage1 = -1
    beamer.FireModes.head.Modifiers.Damage2 = -1
    beamer.FireModes.head.Modifiers.Damage3 = -1
    beamer.FireModes.head.Modifiers.Damage4 = -1
    beamer.FireModes += new FireModeDefinition
    beamer.FireModes(1).AmmoTypeIndices += 0
    beamer.FireModes(1).ProjectileTypeIndices += 1
    beamer.FireModes(1).AmmoSlotIndex = 0
    beamer.FireModes(1).Magazine = 16
    beamer.FireModes(1).Modifiers.Damage0 = -3
    beamer.FireModes(1).Modifiers.Damage1 = -3
    beamer.FireModes(1).Modifiers.Damage2 = -3
    beamer.FireModes(1).Modifiers.Damage3 = -3
    beamer.FireModes(1).Modifiers.Damage4 = -3
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
    ilc9.FireModes.head.Modifiers.Damage1 = -3
    ilc9.FireModes.head.Modifiers.Damage4 = -3
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
    suppressor.FireModes.head.Modifiers.Damage0 = -1
    suppressor.FireModes.head.Modifiers.Damage1 = -1
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
    punisher.FireModes.head.Modifiers.Damage0 = 1
    punisher.FireModes.head.Modifiers.Damage3 = 1
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
    gauss.FireModes.head.Modifiers.Damage0 = 2
    gauss.FireModes.head.Modifiers.Damage3 = 2
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
    spiker.FireModes += new FireModeDefinition
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
    r_shotgun.FireModes.head.Modifiers.Damage0 = 1
    r_shotgun.FireModes += new PelletFireModeDefinition
    r_shotgun.FireModes(1).AmmoTypeIndices += 0
    r_shotgun.FireModes(1).AmmoTypeIndices += 1
    r_shotgun.FireModes(1).AmmoSlotIndex = 0
    r_shotgun.FireModes(1).Magazine = 16
    r_shotgun.FireModes(1).Chamber = 8 //16 shells * 8 pellets = 128
    r_shotgun.FireModes(1).Modifiers.Damage0 = -3
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
    maelstrom.FireModes += new FireModeDefinition
    maelstrom.FireModes(2).AmmoTypeIndices += 0
    maelstrom.FireModes(2).ProjectileTypeIndices += 2
    maelstrom.FireModes(2).AmmoSlotIndex = 0
    maelstrom.FireModes(2).Magazine = 150
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
    flamethrower.FireModes(1).Rounds = 50
    flamethrower.Tile = InventoryTile.Tile63

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
    nchev_scattercannon.FireModes.head.Chamber = 10
    nchev_scattercannon.FireModes += new PelletFireModeDefinition
    nchev_scattercannon.FireModes(1).AmmoTypeIndices += 0
    nchev_scattercannon.FireModes(1).AmmoSlotIndex = 0
    nchev_scattercannon.FireModes(1).Magazine = 40
    nchev_scattercannon.FireModes(1).Chamber = 10
    nchev_scattercannon.FireModes += new PelletFireModeDefinition
    nchev_scattercannon.FireModes(2).AmmoTypeIndices += 0
    nchev_scattercannon.FireModes(2).AmmoSlotIndex = 0
    nchev_scattercannon.FireModes(2).Magazine = 40
    nchev_scattercannon.FireModes(2).Chamber = 10

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
    nano_dispenser.FireModes += new FireModeDefinition
    nano_dispenser.FireModes.head.AmmoTypeIndices += 0
    nano_dispenser.FireModes.head.AmmoTypeIndices += 1
    nano_dispenser.FireModes.head.AmmoSlotIndex = 0
    nano_dispenser.FireModes.head.Magazine = 100
    nano_dispenser.FireModes.head.CustomMagazine = Ammo.upgrade_canister -> 1
    nano_dispenser.FireModes.head.Modifiers.Damage1 = 20
    nano_dispenser.FireModes.head.Modifiers.Damage4 = 20
    nano_dispenser.Tile = InventoryTile.Tile63

    bank.Name = "bank"
    bank.Size = EquipmentSize.Pistol
    bank.AmmoTypes += armor_canister
    bank.FireModes += new FireModeDefinition
    bank.FireModes.head.AmmoTypeIndices += 0
    bank.FireModes.head.AmmoSlotIndex = 0
    bank.FireModes.head.Magazine = 100
    bank.FireModes += new FireModeDefinition
    bank.FireModes(1).AmmoTypeIndices += 0
    bank.FireModes(1).AmmoSlotIndex = 0
    bank.FireModes(1).Magazine = 100
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
    ace.Modes += new ConstructionFireMode
    ace.Modes.head.Item(DeployedItem.boomer -> Set(CertificationType.CombatEngineering))
    ace.Modes += new ConstructionFireMode
    ace.Modes(1).Item(DeployedItem.he_mine -> Set(CertificationType.CombatEngineering))
    ace.Modes(1).Item(DeployedItem.jammer_mine -> Set(CertificationType.AssaultEngineering))
    ace.Modes += new ConstructionFireMode
    ace.Modes(2).Item(DeployedItem.spitfire_turret -> Set(CertificationType.CombatEngineering))
    ace.Modes(2).Item(DeployedItem.spitfire_cloaked -> Set(CertificationType.FortificationEngineering))
    ace.Modes(2).Item(DeployedItem.spitfire_aa -> Set(CertificationType.FortificationEngineering))
    ace.Modes += new ConstructionFireMode
    ace.Modes(3).Item(DeployedItem.motionalarmsensor -> Set(CertificationType.CombatEngineering))
    ace.Modes(3).Item(DeployedItem.sensor_shield -> Set(CertificationType.AdvancedHacking, CertificationType.CombatEngineering))
    ace.Tile = InventoryTile.Tile33

    advanced_ace.Name = "advanced_ace"
    advanced_ace.Size = EquipmentSize.Rifle
    advanced_ace.Modes += new ConstructionFireMode
    advanced_ace.Modes.head.Item(DeployedItem.tank_traps -> Set(CertificationType.FortificationEngineering))
    advanced_ace.Modes += new ConstructionFireMode
    advanced_ace.Modes(1).Item(DeployedItem.portable_manned_turret -> Set(CertificationType.AssaultEngineering))
    advanced_ace.Modes += new ConstructionFireMode
    advanced_ace.Modes(2).Item(DeployedItem.deployable_shield_generator -> Set(CertificationType.AssaultEngineering))
    advanced_ace.Tile = InventoryTile.Tile93

    router_telepad.Name = "router_telepad"
    router_telepad.Size = EquipmentSize.Pistol
    router_telepad.Modes += new ConstructionFireMode
    router_telepad.Modes.head.Item(DeployedItem.router_telepad_deployable -> Set(CertificationType.GroundSupport))
    router_telepad.Tile = InventoryTile.Tile33
    router_telepad.Packet = new TelepadConverter

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
    scythe.FireModes(1).AmmoSlotIndex = 1 //note: the scythe has two magazines using a single pool; however, it can not ammo-switch or mode-switch
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
    vanguard_weapon_system.ProjectileTypes += bullet_105mm_projectile
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
    galaxy_gunship_cannon.FireModes.head.Modifiers.Damage1 = 50
    galaxy_gunship_cannon.FireModes.head.Modifiers.Damage2 = 50
    galaxy_gunship_cannon.FireModes.head.Modifiers.Damage3 = 10
    galaxy_gunship_cannon.FireModes.head.Modifiers.Damage4 = 50

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
    energy_gun_nc.FireModes.head.Chamber = 9

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
  }

  /**
    * Initialize `VehicleDefinition` globals.
    */
  private def init_vehicles() : Unit = {
    fury.Name = "fury"
    fury.MaxHealth = 650
    fury.MaxShields = 130 + 1
    fury.Seats += 0 -> new SeatDefinition()
    fury.Seats(0).Bailable = true
    fury.Seats(0).ControlledWeapon = 1
    fury.Weapons += 1 -> fury_weapon_systema
    fury.MountPoints += 1 -> 0
    fury.MountPoints += 2 -> 0
    fury.TrunkSize = InventoryTile.Tile1111
    fury.TrunkOffset = 30
    fury.AutoPilotSpeeds = (24, 10)
    fury.DestroyedModel = Some(DestroyedVehicle.QuadAssault)

    quadassault.Name = "quadassault"
    quadassault.MaxHealth = 650
    quadassault.MaxShields = 130 + 1
    quadassault.Seats += 0 -> new SeatDefinition()
    quadassault.Seats(0).Bailable = true
    quadassault.Seats(0).ControlledWeapon = 1
    quadassault.Weapons += 1 -> quadassault_weapon_system
    quadassault.MountPoints += 1 -> 0
    quadassault.MountPoints += 2 -> 0
    quadassault.TrunkSize = InventoryTile.Tile1111
    quadassault.TrunkOffset = 30
    quadassault.AutoPilotSpeeds = (24, 10)
    quadassault.DestroyedModel = Some(DestroyedVehicle.QuadAssault)

    quadstealth.Name = "quadstealth"
    quadstealth.MaxHealth = 650
    quadstealth.MaxShields = 130 + 1
    quadstealth.CanCloak = true
    quadstealth.Seats += 0 -> new SeatDefinition()
    quadstealth.Seats(0).Bailable = true
    quadstealth.CanCloak = true
    quadstealth.MountPoints += 1 -> 0
    quadstealth.MountPoints += 2 -> 0
    quadstealth.TrunkSize = InventoryTile.Tile1111
    quadstealth.TrunkOffset = 30
    quadstealth.AutoPilotSpeeds = (24, 10)
    quadstealth.DestroyedModel = Some(DestroyedVehicle.QuadStealth)

    two_man_assault_buggy.Name = "two_man_assault_buggy"
    two_man_assault_buggy.MaxHealth = 1250
    two_man_assault_buggy.MaxShields = 250 + 1
    two_man_assault_buggy.Seats += 0 -> new SeatDefinition()
    two_man_assault_buggy.Seats(0).Bailable = true
    two_man_assault_buggy.Seats += 1 -> new SeatDefinition()
    two_man_assault_buggy.Seats(1).Bailable = true
    two_man_assault_buggy.Seats(1).ControlledWeapon = 2
    two_man_assault_buggy.Weapons += 2 -> chaingun_p
    two_man_assault_buggy.MountPoints += 1 -> 0
    two_man_assault_buggy.MountPoints += 2 -> 1
    two_man_assault_buggy.TrunkSize = InventoryTile.Tile1511
    two_man_assault_buggy.TrunkOffset = 30
    two_man_assault_buggy.AutoPilotSpeeds = (22, 8)
    two_man_assault_buggy.DestroyedModel = Some(DestroyedVehicle.TwoManAssaultBuggy)

    skyguard.Name = "skyguard"
    skyguard.MaxHealth = 1000
    skyguard.MaxShields = 200 + 1
    skyguard.Seats += 0 -> new SeatDefinition()
    skyguard.Seats(0).Bailable = true
    skyguard.Seats += 1 -> new SeatDefinition()
    skyguard.Seats(1).Bailable = true
    skyguard.Seats(1).ControlledWeapon = 2
    skyguard.Weapons += 2 -> skyguard_weapon_system
    skyguard.MountPoints += 1 -> 0
    skyguard.MountPoints += 2 -> 0
    skyguard.MountPoints += 3 -> 1
    skyguard.TrunkSize = InventoryTile.Tile1511
    skyguard.TrunkOffset = 30
    skyguard.AutoPilotSpeeds = (22, 8)
    skyguard.DestroyedModel = Some(DestroyedVehicle.Skyguard)

    threemanheavybuggy.Name = "threemanheavybuggy"
    threemanheavybuggy.MaxHealth = 1700
    threemanheavybuggy.MaxShields = 340 + 1
    threemanheavybuggy.Seats += 0 -> new SeatDefinition()
    threemanheavybuggy.Seats(0).Bailable = true
    threemanheavybuggy.Seats += 1 -> new SeatDefinition()
    threemanheavybuggy.Seats(1).Bailable = true
    threemanheavybuggy.Seats(1).ControlledWeapon = 3
    threemanheavybuggy.Seats += 2 -> new SeatDefinition()
    threemanheavybuggy.Seats(2).Bailable = true
    threemanheavybuggy.Seats(2).ControlledWeapon = 4
    threemanheavybuggy.Weapons += 3 -> chaingun_p
    threemanheavybuggy.Weapons += 4 -> grenade_launcher_marauder
    threemanheavybuggy.MountPoints += 1 -> 0
    threemanheavybuggy.MountPoints += 2 -> 1
    threemanheavybuggy.MountPoints += 3 -> 2
    threemanheavybuggy.TrunkSize = InventoryTile.Tile1511
    threemanheavybuggy.TrunkOffset = 30
    threemanheavybuggy.AutoPilotSpeeds = (22, 8)
    threemanheavybuggy.DestroyedModel = Some(DestroyedVehicle.ThreeManHeavyBuggy)

    twomanheavybuggy.Name = "twomanheavybuggy"
    twomanheavybuggy.MaxHealth = 1800
    twomanheavybuggy.MaxShields = 360 + 1
    twomanheavybuggy.Seats += 0 -> new SeatDefinition()
    twomanheavybuggy.Seats(0).Bailable = true
    twomanheavybuggy.Seats += 1 -> new SeatDefinition()
    twomanheavybuggy.Seats(1).Bailable = true
    twomanheavybuggy.Seats(1).ControlledWeapon = 2
    twomanheavybuggy.Weapons += 2 -> advanced_missile_launcher_t
    twomanheavybuggy.MountPoints += 1 -> 0
    twomanheavybuggy.MountPoints += 2 -> 1
    twomanheavybuggy.TrunkSize = InventoryTile.Tile1511
    twomanheavybuggy.TrunkOffset = 30
    twomanheavybuggy.AutoPilotSpeeds = (22, 8)
    twomanheavybuggy.DestroyedModel = Some(DestroyedVehicle.TwoManHeavyBuggy)

    twomanhoverbuggy.Name = "twomanhoverbuggy"
    twomanhoverbuggy.MaxHealth = 1600
    twomanhoverbuggy.MaxShields = 320 + 1
    twomanhoverbuggy.Seats += 0 -> new SeatDefinition()
    twomanhoverbuggy.Seats(0).Bailable = true
    twomanhoverbuggy.Seats += 1 -> new SeatDefinition()
    twomanhoverbuggy.Seats(1).Bailable = true
    twomanhoverbuggy.Seats(1).ControlledWeapon = 2
    twomanhoverbuggy.Weapons += 2 -> flux_cannon_thresher
    twomanhoverbuggy.MountPoints += 1 -> 0
    twomanhoverbuggy.MountPoints += 2 -> 1
    twomanhoverbuggy.TrunkSize = InventoryTile.Tile1511
    twomanhoverbuggy.TrunkOffset = 30
    twomanhoverbuggy.AutoPilotSpeeds = (22, 10)
    twomanhoverbuggy.DestroyedModel = Some(DestroyedVehicle.TwoManHoverBuggy)

    mediumtransport.Name = "mediumtransport"
    mediumtransport.MaxHealth = 2500
    mediumtransport.MaxShields = 500 + 1
    mediumtransport.Seats += 0 -> new SeatDefinition()
    mediumtransport.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    mediumtransport.Seats += 1 -> new SeatDefinition()
    mediumtransport.Seats(1).ControlledWeapon = 5
    mediumtransport.Seats += 2 -> new SeatDefinition()
    mediumtransport.Seats(2).ControlledWeapon = 6
    mediumtransport.Seats += 3 -> new SeatDefinition()
    mediumtransport.Seats += 4 -> new SeatDefinition()
    mediumtransport.Weapons += 5 -> mediumtransport_weapon_systemA
    mediumtransport.Weapons += 6 -> mediumtransport_weapon_systemB
    mediumtransport.MountPoints += 1 -> 0
    mediumtransport.MountPoints += 2 -> 1
    mediumtransport.MountPoints += 3 -> 2
    mediumtransport.MountPoints += 4 -> 3
    mediumtransport.MountPoints += 5 -> 4
    mediumtransport.TrunkSize = InventoryTile.Tile1515
    mediumtransport.TrunkOffset = 30
    mediumtransport.AutoPilotSpeeds = (18, 6)
    mediumtransport.DestroyedModel = Some(DestroyedVehicle.MediumTransport)

    battlewagon.Name = "battlewagon"
    battlewagon.MaxHealth = 2500
    battlewagon.MaxShields = 500 + 1
    battlewagon.Seats += 0 -> new SeatDefinition()
    battlewagon.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    battlewagon.Seats += 1 -> new SeatDefinition()
    battlewagon.Seats(1).ControlledWeapon = 5
    battlewagon.Seats += 2 -> new SeatDefinition()
    battlewagon.Seats(2).ControlledWeapon = 6
    battlewagon.Seats += 3 -> new SeatDefinition()
    battlewagon.Seats(3).ControlledWeapon = 7
    battlewagon.Seats += 4 -> new SeatDefinition()
    battlewagon.Seats(4).ControlledWeapon = 8
    battlewagon.Weapons += 5 -> battlewagon_weapon_systema
    battlewagon.Weapons += 6 -> battlewagon_weapon_systemb
    battlewagon.Weapons += 7 -> battlewagon_weapon_systemc
    battlewagon.Weapons += 8 -> battlewagon_weapon_systemd
    battlewagon.MountPoints += 1 -> 0
    battlewagon.MountPoints += 2 -> 1
    battlewagon.MountPoints += 3 -> 2
    battlewagon.MountPoints += 4 -> 3
    battlewagon.MountPoints += 5 -> 4
    battlewagon.TrunkSize = InventoryTile.Tile1515
    battlewagon.TrunkOffset = 30
    battlewagon.AutoPilotSpeeds = (18, 6)
    battlewagon.DestroyedModel = Some(DestroyedVehicle.MediumTransport)

    thunderer.Name = "thunderer"
    thunderer.MaxHealth = 2500
    thunderer.MaxShields = 500 + 1
    thunderer.Seats += 0 -> new SeatDefinition()
    thunderer.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    thunderer.Seats += 1 -> new SeatDefinition()
    thunderer.Seats(1).ControlledWeapon = 5
    thunderer.Seats += 2 -> new SeatDefinition()
    thunderer.Seats(2).ControlledWeapon = 6
    thunderer.Seats += 3 -> new SeatDefinition()
    thunderer.Seats += 4 -> new SeatDefinition()
    thunderer.Weapons += 5 -> thunderer_weapon_systema
    thunderer.Weapons += 6 -> thunderer_weapon_systemb
    thunderer.MountPoints += 1 -> 0
    thunderer.MountPoints += 2 -> 1
    thunderer.MountPoints += 3 -> 2
    thunderer.MountPoints += 4 -> 3
    thunderer.MountPoints += 5 -> 4
    thunderer.TrunkSize = InventoryTile.Tile1515
    thunderer.TrunkOffset = 30
    thunderer.AutoPilotSpeeds = (18, 6)
    thunderer.DestroyedModel = Some(DestroyedVehicle.MediumTransport)

    aurora.Name = "aurora"
    aurora.MaxHealth = 2500
    aurora.MaxShields = 500 + 1
    aurora.Seats += 0 -> new SeatDefinition()
    aurora.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    aurora.Seats += 1 -> new SeatDefinition()
    aurora.Seats(1).ControlledWeapon = 5
    aurora.Seats += 2 -> new SeatDefinition()
    aurora.Seats(2).ControlledWeapon = 6
    aurora.Seats += 3 -> new SeatDefinition()
    aurora.Seats += 4 -> new SeatDefinition()
    aurora.Weapons += 5 -> aurora_weapon_systema
    aurora.Weapons += 6 -> aurora_weapon_systemb
    aurora.MountPoints += 1 -> 0
    aurora.MountPoints += 2 -> 1
    aurora.MountPoints += 3 -> 2
    aurora.MountPoints += 4 -> 3
    aurora.MountPoints += 5 -> 4
    aurora.TrunkSize = InventoryTile.Tile1515
    aurora.TrunkOffset = 30
    aurora.AutoPilotSpeeds = (18, 6)
    aurora.DestroyedModel = Some(DestroyedVehicle.MediumTransport)

    apc_tr.Name = "apc_tr"
    apc_tr.MaxHealth = 6000
    apc_tr.MaxShields = 1200 + 1
    apc_tr.Seats += 0 -> new SeatDefinition()
    apc_tr.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    apc_tr.Seats += 1 -> new SeatDefinition()
    apc_tr.Seats(1).ControlledWeapon = 11
    apc_tr.Seats += 2 -> new SeatDefinition()
    apc_tr.Seats(2).ControlledWeapon = 12
    apc_tr.Seats += 3 -> new SeatDefinition()
    apc_tr.Seats += 4 -> new SeatDefinition()
    apc_tr.Seats += 5 -> new SeatDefinition()
    apc_tr.Seats(5).ControlledWeapon = 15
    apc_tr.Seats += 6 -> new SeatDefinition()
    apc_tr.Seats(6).ControlledWeapon = 16
    apc_tr.Seats += 7 -> new SeatDefinition()
    apc_tr.Seats(7).ControlledWeapon = 13
    apc_tr.Seats += 8 -> new SeatDefinition()
    apc_tr.Seats(8).ControlledWeapon = 14
    apc_tr.Seats += 9 -> new SeatDefinition()
    apc_tr.Seats(9).ArmorRestriction = SeatArmorRestriction.MaxOnly
    apc_tr.Seats += 10 -> new SeatDefinition()
    apc_tr.Seats(10).ArmorRestriction = SeatArmorRestriction.MaxOnly
    apc_tr.Weapons += 11 -> apc_weapon_systemc_tr
    apc_tr.Weapons += 12 -> apc_weapon_systemb
    apc_tr.Weapons += 13 -> apc_weapon_systema
    apc_tr.Weapons += 14 -> apc_weapon_systemd_tr
    apc_tr.Weapons += 15 -> apc_ballgun_r
    apc_tr.Weapons += 16 -> apc_ballgun_l
    apc_tr.MountPoints += 1 -> 0
    apc_tr.MountPoints += 2 -> 0
    apc_tr.MountPoints += 3 -> 1
    apc_tr.MountPoints += 4 -> 2
    apc_tr.MountPoints += 5 -> 3
    apc_tr.MountPoints += 6 -> 4
    apc_tr.MountPoints += 7 -> 5
    apc_tr.MountPoints += 8 -> 6
    apc_tr.MountPoints += 9 -> 7
    apc_tr.MountPoints += 10 -> 8
    apc_tr.MountPoints += 11 -> 9
    apc_tr.MountPoints += 12 -> 10
    apc_tr.TrunkSize = InventoryTile.Tile2016
    apc_tr.TrunkOffset = 30
    apc_tr.AutoPilotSpeeds = (16, 6)
    apc_tr.DestroyedModel = Some(DestroyedVehicle.Apc)

    apc_nc.Name = "apc_nc"
    apc_nc.MaxHealth = 6000
    apc_nc.MaxShields = 1200 + 1
    apc_nc.Seats += 0 -> new SeatDefinition()
    apc_nc.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    apc_nc.Seats += 1 -> new SeatDefinition()
    apc_nc.Seats(1).ControlledWeapon = 11
    apc_nc.Seats += 2 -> new SeatDefinition()
    apc_nc.Seats(2).ControlledWeapon = 12
    apc_nc.Seats += 3 -> new SeatDefinition()
    apc_nc.Seats += 4 -> new SeatDefinition()
    apc_nc.Seats += 5 -> new SeatDefinition()
    apc_nc.Seats(5).ControlledWeapon = 15
    apc_nc.Seats += 6 -> new SeatDefinition()
    apc_nc.Seats(6).ControlledWeapon = 16
    apc_nc.Seats += 7 -> new SeatDefinition()
    apc_nc.Seats(7).ControlledWeapon = 13
    apc_nc.Seats += 8 -> new SeatDefinition()
    apc_nc.Seats(8).ControlledWeapon = 14
    apc_nc.Seats += 9 -> new SeatDefinition()
    apc_nc.Seats(9).ArmorRestriction = SeatArmorRestriction.MaxOnly
    apc_nc.Seats += 10 -> new SeatDefinition()
    apc_nc.Seats(10).ArmorRestriction = SeatArmorRestriction.MaxOnly
    apc_nc.Weapons += 11 -> apc_weapon_systemc_nc
    apc_nc.Weapons += 12 -> apc_weapon_systemb
    apc_nc.Weapons += 13 -> apc_weapon_systema
    apc_nc.Weapons += 14 -> apc_weapon_systemd_nc
    apc_nc.Weapons += 15 -> apc_ballgun_r
    apc_nc.Weapons += 16 -> apc_ballgun_l
    apc_nc.MountPoints += 1 -> 0
    apc_nc.MountPoints += 2 -> 0
    apc_nc.MountPoints += 3 -> 1
    apc_nc.MountPoints += 4 -> 2
    apc_nc.MountPoints += 5 -> 3
    apc_nc.MountPoints += 6 -> 4
    apc_nc.MountPoints += 7 -> 5
    apc_nc.MountPoints += 8 -> 6
    apc_nc.MountPoints += 9 -> 7
    apc_nc.MountPoints += 10 -> 8
    apc_nc.MountPoints += 11 -> 9
    apc_nc.MountPoints += 12 -> 10
    apc_nc.TrunkSize = InventoryTile.Tile2016
    apc_nc.TrunkOffset = 30
    apc_nc.AutoPilotSpeeds = (16, 6)
    apc_nc.DestroyedModel = Some(DestroyedVehicle.Apc)

    apc_vs.Name = "apc_vs"
    apc_vs.MaxHealth = 6000
    apc_vs.MaxShields = 1200 + 1
    apc_vs.Seats += 0 -> new SeatDefinition()
    apc_vs.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    apc_vs.Seats += 1 -> new SeatDefinition()
    apc_vs.Seats(1).ControlledWeapon = 11
    apc_vs.Seats += 2 -> new SeatDefinition()
    apc_vs.Seats(2).ControlledWeapon = 12
    apc_vs.Seats += 3 -> new SeatDefinition()
    apc_vs.Seats += 4 -> new SeatDefinition()
    apc_vs.Seats += 5 -> new SeatDefinition()
    apc_vs.Seats(5).ControlledWeapon = 15
    apc_vs.Seats += 6 -> new SeatDefinition()
    apc_vs.Seats(6).ControlledWeapon = 16
    apc_vs.Seats += 7 -> new SeatDefinition()
    apc_vs.Seats(7).ControlledWeapon = 13
    apc_vs.Seats += 8 -> new SeatDefinition()
    apc_vs.Seats(8).ControlledWeapon = 14
    apc_vs.Seats += 9 -> new SeatDefinition()
    apc_vs.Seats(9).ArmorRestriction = SeatArmorRestriction.MaxOnly
    apc_vs.Seats += 10 -> new SeatDefinition()
    apc_vs.Seats(10).ArmorRestriction = SeatArmorRestriction.MaxOnly
    apc_vs.Weapons += 11 -> apc_weapon_systemc_vs
    apc_vs.Weapons += 12 -> apc_weapon_systemb
    apc_vs.Weapons += 13 -> apc_weapon_systema
    apc_vs.Weapons += 14 -> apc_weapon_systemd_vs
    apc_vs.Weapons += 15 -> apc_ballgun_r
    apc_vs.Weapons += 16 -> apc_ballgun_l
    apc_vs.MountPoints += 1 -> 0
    apc_vs.MountPoints += 2 -> 0
    apc_vs.MountPoints += 3 -> 1
    apc_vs.MountPoints += 4 -> 2
    apc_vs.MountPoints += 5 -> 3
    apc_vs.MountPoints += 6 -> 4
    apc_vs.MountPoints += 7 -> 5
    apc_vs.MountPoints += 8 -> 6
    apc_vs.MountPoints += 9 -> 7
    apc_vs.MountPoints += 10 -> 8
    apc_vs.MountPoints += 11 -> 9
    apc_vs.MountPoints += 12 -> 10
    apc_vs.TrunkSize = InventoryTile.Tile2016
    apc_vs.TrunkOffset = 30
    apc_vs.AutoPilotSpeeds = (16, 6)
    apc_vs.DestroyedModel = Some(DestroyedVehicle.Apc)

    lightning.Name = "lightning"
    lightning.MaxHealth = 2000
    lightning.MaxShields = 400 + 1
    lightning.Seats += 0 -> new SeatDefinition()
    lightning.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    lightning.Seats(0).ControlledWeapon = 1
    lightning.Weapons += 1 -> lightning_weapon_system
    lightning.MountPoints += 1 -> 0
    lightning.MountPoints += 2 -> 0
    lightning.TrunkSize = InventoryTile.Tile1511
    lightning.TrunkOffset = 30
    lightning.AutoPilotSpeeds = (20, 8)
    lightning.DestroyedModel = Some(DestroyedVehicle.Lightning)

    prowler.Name = "prowler"
    prowler.MaxHealth = 4800
    prowler.MaxShields = 960 + 1
    prowler.Seats += 0 -> new SeatDefinition()
    prowler.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    prowler.Seats += 1 -> new SeatDefinition()
    prowler.Seats(1).ControlledWeapon = 3
    prowler.Seats += 2 -> new SeatDefinition()
    prowler.Seats(2).ControlledWeapon = 4
    prowler.Weapons += 3 -> prowler_weapon_systemA
    prowler.Weapons += 4 -> prowler_weapon_systemB
    prowler.MountPoints += 1 -> 0
    prowler.MountPoints += 2 -> 1
    prowler.MountPoints += 3 -> 2
    prowler.TrunkSize = InventoryTile.Tile1511
    prowler.TrunkOffset = 30
    prowler.AutoPilotSpeeds = (14, 6)
    prowler.DestroyedModel = Some(DestroyedVehicle.Prowler)

    vanguard.Name = "vanguard"
    vanguard.MaxHealth = 5400
    vanguard.MaxShields = 1080 + 1
    vanguard.Seats += 0 -> new SeatDefinition()
    vanguard.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    vanguard.Seats += 1 -> new SeatDefinition()
    vanguard.Seats(1).ControlledWeapon = 2
    vanguard.Weapons += 2 -> vanguard_weapon_system
    vanguard.MountPoints += 1 -> 0
    vanguard.MountPoints += 2 -> 1
    vanguard.TrunkSize = InventoryTile.Tile1511
    vanguard.TrunkOffset = 30
    vanguard.AutoPilotSpeeds = (16, 6)
    vanguard.DestroyedModel = Some(DestroyedVehicle.Vanguard)

    magrider.Name = "magrider"
    magrider.MaxHealth = 4200
    magrider.MaxShields = 840 + 1
    magrider.Seats += 0 -> new SeatDefinition()
    magrider.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    magrider.Seats(0).ControlledWeapon = 2
    magrider.Seats += 1 -> new SeatDefinition()
    magrider.Seats(1).ControlledWeapon = 3
    magrider.Weapons += 2 -> particle_beam_magrider
    magrider.Weapons += 3 -> heavy_rail_beam_magrider
    magrider.MountPoints += 1 -> 0
    magrider.MountPoints += 2 -> 1
    magrider.TrunkSize = InventoryTile.Tile1511
    magrider.TrunkOffset = 30
    magrider.AutoPilotSpeeds = (18, 6)
    magrider.DestroyedModel = Some(DestroyedVehicle.Magrider)

    val utilityConverter = new UtilityVehicleConverter
    ant.Name = "ant"
    ant.MaxHealth = 2000
    ant.MaxShields = 400 + 1
    ant.Seats += 0 -> new SeatDefinition()
    ant.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    ant.MountPoints += 1 -> 0
    ant.MountPoints += 2 -> 0
    ant.Deployment = true
    ant.DeployTime = 1500
    ant.UndeployTime = 1500
    ant.AutoPilotSpeeds = (18, 6)
    ant.MaximumCapacitor = 1500
    ant.Packet = utilityConverter
    ant.DestroyedModel = Some(DestroyedVehicle.Ant)

    ams.Name = "ams"
    ams.MaxHealth = 3000
    ams.MaxShields = 600 + 1
    ams.Seats += 0 -> new SeatDefinition()
    ams.Seats(0).ArmorRestriction = SeatArmorRestriction.NoReinforcedOrMax
    ams.MountPoints += 1 -> 0
    ams.MountPoints += 2 -> 0
    ams.Utilities += 1 -> UtilityType.matrix_terminalc
    ams.Utilities += 2 -> UtilityType.ams_respawn_tube
    ams.Utilities += 3 -> UtilityType.order_terminala
    ams.Utilities += 4 -> UtilityType.order_terminalb
    ams.Deployment = true
    ams.DeployTime = 2000
    ams.UndeployTime = 2000
    ams.DeconstructionTime = Some(20 minutes)
    ams.AutoPilotSpeeds = (18, 6)
    ams.Packet = utilityConverter
    ams.DestroyedModel = Some(DestroyedVehicle.Ams)

    val variantConverter = new VariantVehicleConverter
    router.Name = "router"
    router.MaxHealth = 4000
    router.MaxShields = 800 + 1
    router.Seats += 0 -> new SeatDefinition()
    router.MountPoints += 1 -> 0
    router.Utilities += 1 -> UtilityType.teleportpad_terminal
    router.Utilities += 2 -> UtilityType.internal_router_telepad_deployable
    router.TrunkSize = InventoryTile.Tile1511
    router.TrunkOffset = 30
    router.Deployment = true
    router.DeployTime = 2000
    router.UndeployTime = 2000
    router.DeconstructionTime = Duration(20, "minutes")
    router.AutoPilotSpeeds = (16, 6)
    router.Packet = variantConverter
    router.DestroyedModel = Some(DestroyedVehicle.Router)

    switchblade.Name = "switchblade"
    switchblade.MaxHealth = 1750
    switchblade.MaxShields = 350 + 1
    switchblade.Seats += 0 -> new SeatDefinition()
    switchblade.Seats(0).ControlledWeapon = 1
    switchblade.Weapons += 1 -> scythe
    switchblade.MountPoints += 1 -> 0
    switchblade.MountPoints += 2 -> 0
    switchblade.TrunkSize = InventoryTile.Tile1511
    switchblade.TrunkOffset = 30
    switchblade.Deployment = true
    switchblade.DeployTime = 2000
    switchblade.UndeployTime = 2000
    switchblade.AutoPilotSpeeds = (22, 8)
    switchblade.Packet = variantConverter
    switchblade.DestroyedModel = Some(DestroyedVehicle.Switchblade)

    flail.Name = "flail"
    flail.MaxHealth = 2400
    flail.MaxShields = 480 + 1
    flail.Seats += 0 -> new SeatDefinition()
    flail.Seats(0).ControlledWeapon = 1
    flail.Weapons += 1 -> flail_weapon
    flail.MountPoints += 1 -> 0
    flail.TrunkSize = InventoryTile.Tile1511
    flail.TrunkOffset = 30
    flail.Deployment = true
    flail.DeployTime = 2000
    flail.UndeployTime = 2000
    flail.AutoPilotSpeeds = (14, 6)
    flail.Packet = variantConverter
    flail.DestroyedModel = Some(DestroyedVehicle.Flail)

    mosquito.Name = "mosquito"
    mosquito.MaxHealth = 665
    mosquito.MaxShields = 133 + 1
    mosquito.Seats += 0 -> new SeatDefinition()
    mosquito.Seats(0).Bailable = true
    mosquito.Seats(0).ControlledWeapon = 1
    mosquito.Weapons += 1 -> rotarychaingun_mosquito
    mosquito.MountPoints += 1 -> 0
    mosquito.MountPoints += 2 -> 0
    mosquito.TrunkSize = InventoryTile.Tile1111
    mosquito.TrunkOffset = 30
    mosquito.AutoPilotSpeeds = (0, 6)
    mosquito.Packet = variantConverter
    mosquito.DestroyedModel = Some(DestroyedVehicle.Mosquito)

    lightgunship.Name = "lightgunship"
    lightgunship.MaxHealth = 1000
    lightgunship.MaxShields = 200 + 1
    lightgunship.Seats += 0 -> new SeatDefinition()
    lightgunship.Seats(0).Bailable = true
    lightgunship.Seats(0).ControlledWeapon = 1
    lightgunship.Weapons += 1 -> lightgunship_weapon_system
    lightgunship.MountPoints += 1 -> 0
    lightgunship.MountPoints += 2 -> 0
    lightgunship.TrunkSize = InventoryTile.Tile1511
    lightgunship.TrunkOffset = 30
    lightgunship.AutoPilotSpeeds = (0, 4)
    lightgunship.Packet = variantConverter
    lightgunship.DestroyedModel = Some(DestroyedVehicle.LightGunship)

    wasp.Name = "wasp"
    wasp.MaxHealth = 515
    wasp.MaxShields = 103 + 1
    wasp.Seats += 0 -> new SeatDefinition()
    wasp.Seats(0).Bailable = true
    wasp.Seats(0).ControlledWeapon = 1
    wasp.Weapons += 1 -> wasp_weapon_system
    wasp.MountPoints += 1 -> 0
    wasp.MountPoints += 2 -> 0
    wasp.TrunkSize = InventoryTile.Tile1111
    wasp.TrunkOffset = 30
    wasp.AutoPilotSpeeds = (0, 6)
    wasp.Packet = variantConverter
    wasp.DestroyedModel = Some(DestroyedVehicle.Mosquito) //set_resource_parent wasp game_objects mosquito

    liberator.Name = "liberator"
    liberator.MaxHealth = 2500
    liberator.MaxShields = 500 + 1
    liberator.Seats += 0 -> new SeatDefinition()
    liberator.Seats(0).ControlledWeapon = 3
    liberator.Seats += 1 -> new SeatDefinition()
    liberator.Seats(1).ControlledWeapon = 4
    liberator.Seats += 2 -> new SeatDefinition()
    liberator.Seats(2).ControlledWeapon = 5
    liberator.Weapons += 3 -> liberator_weapon_system
    liberator.Weapons += 4 -> liberator_bomb_bay
    liberator.Weapons += 5 -> liberator_25mm_cannon
    liberator.MountPoints += 1 -> 0
    liberator.MountPoints += 2 -> 1
    liberator.MountPoints += 3 -> 1
    liberator.MountPoints += 4 -> 2
    liberator.TrunkSize = InventoryTile.Tile1515
    liberator.TrunkOffset = 30
    liberator.AutoPilotSpeeds = (0, 4)
    liberator.Packet = variantConverter
    liberator.DestroyedModel = Some(DestroyedVehicle.Liberator)

    vulture.Name = "vulture"
    vulture.MaxHealth = 2500
    vulture.MaxShields = 500 + 1
    vulture.Seats += 0 -> new SeatDefinition()
    vulture.Seats(0).ControlledWeapon = 3
    vulture.Seats += 1 -> new SeatDefinition()
    vulture.Seats(1).ControlledWeapon = 4
    vulture.Seats += 2 -> new SeatDefinition()
    vulture.Seats(2).ControlledWeapon = 5
    vulture.Weapons += 3 -> vulture_nose_weapon_system
    vulture.Weapons += 4 -> vulture_bomb_bay
    vulture.Weapons += 5 -> vulture_tail_cannon
    vulture.MountPoints += 1 -> 0
    vulture.MountPoints += 2 -> 1
    vulture.MountPoints += 3 -> 1
    vulture.MountPoints += 4 -> 2
    vulture.TrunkSize = InventoryTile.Tile1611
    vulture.TrunkOffset = 30
    vulture.AutoPilotSpeeds = (0, 4)
    vulture.Packet = variantConverter
    vulture.DestroyedModel = Some(DestroyedVehicle.Liberator) //add_property vulture destroyedphysics liberator_destroyed

    dropship.Name = "dropship"
    dropship.MaxHealth = 5000
    dropship.MaxShields = 1000 + 1
    dropship.Seats += 0 -> new SeatDefinition()
    dropship.Seats += 1 -> new SeatDefinition()
    dropship.Seats(1).Bailable = true
    dropship.Seats(1).ControlledWeapon = 12
    dropship.Seats += 2 -> new SeatDefinition()
    dropship.Seats(2).Bailable = true
    dropship.Seats(2).ControlledWeapon = 13
    dropship.Seats += 3 -> new SeatDefinition()
    dropship.Seats(3).Bailable = true
    dropship.Seats += 4 -> new SeatDefinition()
    dropship.Seats(4).Bailable = true
    dropship.Seats += 5 -> new SeatDefinition()
    dropship.Seats(5).Bailable = true
    dropship.Seats += 6 -> new SeatDefinition()
    dropship.Seats(6).Bailable = true
    dropship.Seats += 7 -> new SeatDefinition()
    dropship.Seats(7).Bailable = true
    dropship.Seats += 8 -> new SeatDefinition()
    dropship.Seats(8).Bailable = true
    dropship.Seats += 9 -> new SeatDefinition()
    dropship.Seats(9).Bailable = true
    dropship.Seats(9).ArmorRestriction = SeatArmorRestriction.MaxOnly
    dropship.Seats += 10 -> new SeatDefinition()
    dropship.Seats(10).Bailable = true
    dropship.Seats(10).ArmorRestriction = SeatArmorRestriction.MaxOnly
    dropship.Seats += 11 -> new SeatDefinition()
    dropship.Seats(11).Bailable = true
    dropship.Seats(11).ControlledWeapon = 14
    dropship.Weapons += 12 -> cannon_dropship_20mm
    dropship.Weapons += 13 -> cannon_dropship_20mm
    dropship.Weapons += 14 -> dropship_rear_turret
    dropship.Cargo += 15 -> new CargoDefinition()
    dropship.MountPoints += 1 -> 0
    dropship.MountPoints += 2 -> 11
    dropship.MountPoints += 3 -> 1
    dropship.MountPoints += 4 -> 2
    dropship.MountPoints += 5 -> 3
    dropship.MountPoints += 6 -> 4
    dropship.MountPoints += 7 -> 5
    dropship.MountPoints += 8 -> 6
    dropship.MountPoints += 9 -> 7
    dropship.MountPoints += 10 -> 8
    dropship.MountPoints += 11 -> 9
    dropship.MountPoints += 12 -> 10
    dropship.MountPoints += 13 -> 15
    dropship.TrunkSize = InventoryTile.Tile1612
    dropship.TrunkOffset = 30
    dropship.AutoPilotSpeeds = (0, 4)
    dropship.Packet = variantConverter
    dropship.DestroyedModel = Some(DestroyedVehicle.Dropship)

    galaxy_gunship.Name = "galaxy_gunship"
    galaxy_gunship.MaxHealth = 6000
    galaxy_gunship.MaxShields = 1200 + 1
    galaxy_gunship.Seats += 0 -> new SeatDefinition()
    galaxy_gunship.Seats += 1 -> new SeatDefinition()
    galaxy_gunship.Seats(1).ControlledWeapon = 6
    galaxy_gunship.Seats += 2 -> new SeatDefinition()
    galaxy_gunship.Seats(2).ControlledWeapon = 7
    galaxy_gunship.Seats += 3 -> new SeatDefinition()
    galaxy_gunship.Seats(3).ControlledWeapon = 8
    galaxy_gunship.Seats += 4 -> new SeatDefinition()
    galaxy_gunship.Seats(4).ControlledWeapon = 9
    galaxy_gunship.Seats += 5 -> new SeatDefinition()
    galaxy_gunship.Seats(5).ControlledWeapon = 10
    galaxy_gunship.Weapons += 6 -> galaxy_gunship_cannon
    galaxy_gunship.Weapons += 7 -> galaxy_gunship_cannon
    galaxy_gunship.Weapons += 8 -> galaxy_gunship_tailgun
    galaxy_gunship.Weapons += 9 -> galaxy_gunship_gun
    galaxy_gunship.Weapons += 10 -> galaxy_gunship_gun
    galaxy_gunship.MountPoints += 1 -> 0
    galaxy_gunship.MountPoints += 2 -> 3
    galaxy_gunship.MountPoints += 3 -> 1
    galaxy_gunship.MountPoints += 4 -> 2
    galaxy_gunship.MountPoints += 5 -> 4
    galaxy_gunship.MountPoints += 6 -> 5
    galaxy_gunship.TrunkSize = InventoryTile.Tile1816
    galaxy_gunship.TrunkOffset = 30
    galaxy_gunship.AutoPilotSpeeds = (0, 4)
    galaxy_gunship.Packet = variantConverter
    galaxy_gunship.DestroyedModel = Some(DestroyedVehicle.Dropship) //the adb calls out a galaxy_gunship_destroyed but no such asset exists

    lodestar.Name = "lodestar"
    lodestar.MaxHealth = 5000
    lodestar.MaxShields = 1000 + 1
    lodestar.Seats += 0 -> new SeatDefinition()
    lodestar.MountPoints += 1 -> 0
    lodestar.MountPoints += 2 -> 1
    lodestar.Cargo += 1 -> new CargoDefinition()
    lodestar.TrunkSize = InventoryTile.Tile1612
    lodestar.TrunkOffset = 30
    lodestar.AutoPilotSpeeds = (0, 4)
    lodestar.Packet = variantConverter
    lodestar.DestroyedModel = Some(DestroyedVehicle.Lodestar)

    phantasm.Name = "phantasm"
    phantasm.MaxHealth = 2500
    phantasm.MaxShields = 500 + 1
    phantasm.CanCloak = true
    phantasm.Seats += 0 -> new SeatDefinition()
    phantasm.Seats += 1 -> new SeatDefinition()
    phantasm.Seats(1).Bailable = true
    phantasm.Seats += 2 -> new SeatDefinition()
    phantasm.Seats(2).Bailable = true
    phantasm.Seats += 3 -> new SeatDefinition()
    phantasm.Seats(3).Bailable = true
    phantasm.Seats += 4 -> new SeatDefinition()
    phantasm.Seats(4).Bailable = true
    phantasm.MountPoints += 1 -> 0
    phantasm.MountPoints += 2 -> 1
    phantasm.MountPoints += 3 -> 2
    phantasm.MountPoints += 4 -> 3
    phantasm.MountPoints += 5 -> 4
    phantasm.TrunkSize = InventoryTile.Tile1107
    phantasm.TrunkOffset = 30
    phantasm.AutoPilotSpeeds = (0, 6)
    phantasm.Packet = variantConverter
    phantasm.DestroyedModel = None //the adb calls out a phantasm_destroyed but no such asset exists
  }

  /**
    * Initialize `Deployable` globals.
    */
  private def init_deployables() : Unit = {
    boomer.Name = "boomer"
    boomer.Descriptor = "Boomers"
    boomer.MaxHealth = 100
    boomer.DeployCategory = DeployableCategory.Boomers
    boomer.DeployTime = Duration.create(1000, "ms")
    boomer.Model = StandardResolutions.SimpleDeployables

    he_mine.Name = "he_mine"
    he_mine.Descriptor = "Mines"
    he_mine.MaxHealth = 100
    he_mine.DeployCategory = DeployableCategory.Mines
    he_mine.DeployTime = Duration.create(1000, "ms")
    he_mine.Model = StandardResolutions.SimpleDeployables

    jammer_mine.Name = "jammer_mine"
    jammer_mine.Descriptor = "JammerMines"
    jammer_mine.MaxHealth = 100
    jammer_mine.DeployCategory = DeployableCategory.Mines
    jammer_mine.DeployTime = Duration.create(1000, "ms")
    jammer_mine.Model = StandardResolutions.SimpleDeployables

    spitfire_turret.Name = "spitfire_turret"
    spitfire_turret.Descriptor= "Spitfires"
    spitfire_turret.MaxHealth = 100
    spitfire_turret.Weapons += 1 -> new mutable.HashMap()
    spitfire_turret.Weapons(1) += TurretUpgrade.None -> spitfire_weapon
    spitfire_turret.ReserveAmmunition = false
    spitfire_turret.DeployCategory = DeployableCategory.SmallTurrets
    spitfire_turret.DeployTime = Duration.create(5000, "ms")
    spitfire_turret.Model = StandardResolutions.ComplexDeployables

    spitfire_cloaked.Name = "spitfire_cloaked"
    spitfire_cloaked.Descriptor= "CloakingSpitfires"
    spitfire_cloaked.MaxHealth = 100
    spitfire_cloaked.Weapons += 1 -> new mutable.HashMap()
    spitfire_cloaked.Weapons(1) += TurretUpgrade.None -> spitfire_weapon
    spitfire_cloaked.ReserveAmmunition = false
    spitfire_cloaked.DeployCategory = DeployableCategory.SmallTurrets
    spitfire_cloaked.DeployTime = Duration.create(5000, "ms")
    spitfire_cloaked.Model = StandardResolutions.ComplexDeployables

    spitfire_aa.Name = "spitfire_aa"
    spitfire_aa.Descriptor= "FlakSpitfires"
    spitfire_aa.MaxHealth = 100
    spitfire_aa.Weapons += 1 -> new mutable.HashMap()
    spitfire_aa.Weapons(1) += TurretUpgrade.None -> spitfire_aa_weapon
    spitfire_aa.ReserveAmmunition = false
    spitfire_aa.DeployCategory = DeployableCategory.SmallTurrets
    spitfire_aa.DeployTime = Duration.create(5000, "ms")
    spitfire_aa.Model = StandardResolutions.ComplexDeployables

    motionalarmsensor.Name = "motionalarmsensor"
    motionalarmsensor.Descriptor = "MotionSensors"
    motionalarmsensor.MaxHealth = 100
    motionalarmsensor.DeployCategory = DeployableCategory.Sensors
    motionalarmsensor.DeployTime = Duration.create(1000, "ms")
    motionalarmsensor.Model = StandardResolutions.SimpleDeployables

    sensor_shield.Name = "sensor_shield"
    sensor_shield.Descriptor = "SensorShields"
    sensor_shield.MaxHealth = 100
    sensor_shield.DeployCategory = DeployableCategory.Sensors
    sensor_shield.DeployTime = Duration.create(5000, "ms")
    sensor_shield.Model = StandardResolutions.SimpleDeployables

    tank_traps.Name = "tank_traps"
    tank_traps.Descriptor = "TankTraps"
    tank_traps.MaxHealth = 5000
    tank_traps.Packet = new TRAPConverter
    tank_traps.DeployCategory = DeployableCategory.TankTraps
    tank_traps.DeployTime = Duration.create(6000, "ms")
    tank_traps.Model = StandardResolutions.SimpleDeployables

    val fieldTurretConverter = new FieldTurretConverter
    portable_manned_turret.Name = "portable_manned_turret"
    portable_manned_turret.Descriptor = "FieldTurrets"
    portable_manned_turret.MaxHealth = 1000
    portable_manned_turret.MountPoints += 1 -> 0
    portable_manned_turret.MountPoints += 2 -> 0
    portable_manned_turret.Weapons += 1 -> new mutable.HashMap()
    portable_manned_turret.Weapons(1) += TurretUpgrade.None -> energy_gun
    portable_manned_turret.ReserveAmmunition = true
    portable_manned_turret.FactionLocked = true
    portable_manned_turret.Packet = fieldTurretConverter
    portable_manned_turret.DeployCategory = DeployableCategory.FieldTurrets
    portable_manned_turret.DeployTime = Duration.create(6000, "ms")
    portable_manned_turret.Model = StandardResolutions.ComplexDeployables

    portable_manned_turret_nc.Name = "portable_manned_turret_nc"
    portable_manned_turret_nc.Descriptor = "FieldTurrets"
    portable_manned_turret_nc.MaxHealth = 1000
    portable_manned_turret_nc.MountPoints += 1 -> 0
    portable_manned_turret_nc.MountPoints += 2 -> 0
    portable_manned_turret_nc.Weapons += 1 -> new mutable.HashMap()
    portable_manned_turret_nc.Weapons(1) += TurretUpgrade.None -> energy_gun_nc
    portable_manned_turret_nc.ReserveAmmunition = true
    portable_manned_turret_nc.FactionLocked = true
    portable_manned_turret_nc.Packet = fieldTurretConverter
    portable_manned_turret_nc.DeployCategory = DeployableCategory.FieldTurrets
    portable_manned_turret_nc.DeployTime = Duration.create(6000, "ms")
    portable_manned_turret_nc.Model = StandardResolutions.ComplexDeployables

    portable_manned_turret_tr.Name = "portable_manned_turret_tr"
    portable_manned_turret_tr.Descriptor = "FieldTurrets"
    portable_manned_turret_tr.MaxHealth = 1000
    portable_manned_turret_tr.MountPoints += 1 -> 0
    portable_manned_turret_tr.MountPoints += 2 -> 0
    portable_manned_turret_tr.Weapons += 1 -> new mutable.HashMap()
    portable_manned_turret_tr.Weapons(1) += TurretUpgrade.None -> energy_gun_tr
    portable_manned_turret_tr.ReserveAmmunition = true
    portable_manned_turret_tr.FactionLocked = true
    portable_manned_turret_tr.Packet = fieldTurretConverter
    portable_manned_turret_tr.DeployCategory = DeployableCategory.FieldTurrets
    portable_manned_turret_tr.DeployTime = Duration.create(6000, "ms")
    portable_manned_turret_tr.Model = StandardResolutions.ComplexDeployables

    portable_manned_turret_vs.Name = "portable_manned_turret_vs"
    portable_manned_turret_vs.Descriptor = "FieldTurrets"
    portable_manned_turret_vs.MaxHealth = 1000
    portable_manned_turret_vs.MountPoints += 1 -> 0
    portable_manned_turret_vs.MountPoints += 2 -> 0
    portable_manned_turret_vs.Weapons += 1 -> new mutable.HashMap()
    portable_manned_turret_vs.Weapons(1) += TurretUpgrade.None -> energy_gun_vs
    portable_manned_turret_vs.ReserveAmmunition = true
    portable_manned_turret_vs.FactionLocked = true
    portable_manned_turret_vs.Packet = fieldTurretConverter
    portable_manned_turret_vs.DeployCategory = DeployableCategory.FieldTurrets
    portable_manned_turret_vs.DeployTime = Duration.create(6000, "ms")
    portable_manned_turret_vs.Model = StandardResolutions.ComplexDeployables

    deployable_shield_generator.Name = "deployable_shield_generator"
    deployable_shield_generator.Descriptor = "ShieldGenerators"
    deployable_shield_generator.MaxHealth = 1700
    deployable_shield_generator.DeployTime = Duration.create(6000, "ms")
    deployable_shield_generator.Model = StandardResolutions.ComplexDeployables

    router_telepad_deployable.Name = "router_telepad_deployable"
    router_telepad_deployable.MaxHealth = 100
    router_telepad_deployable.DeployTime = Duration.create(1, "ms")
    router_telepad_deployable.Packet = new TelepadDeployableConverter
    router_telepad_deployable.Model = StandardResolutions.SimpleDeployables

    internal_router_telepad_deployable.Name = "router_telepad_deployable"
    internal_router_telepad_deployable.MaxHealth = 1
    internal_router_telepad_deployable.DeployTime = Duration.create(1, "ms")
    internal_router_telepad_deployable.Packet = new InternalTelepadDeployableConverter
  }
}
