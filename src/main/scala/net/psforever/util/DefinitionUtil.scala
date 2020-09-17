package net.psforever.util

import net.psforever.objects.definition.BasicDefinition
import net.psforever.objects.{AmmoBox, GlobalDefinitions, Player, SimpleItem, Tool}
import net.psforever.types.ExoSuitType

import scala.reflect.runtime.universe

// TODO definitions should be in an iterable format
object DefinitionUtil {
  def idToDefinition(id: Int): Any = {
    import net.psforever.objects.GlobalDefinitions._
    id match {
      //ammunition
      case 0    => bullet_105mm
      case 3    => bullet_12mm
      case 6    => bullet_150mm
      case 9    => bullet_15mm
      case 16   => bullet_20mm
      case 19   => bullet_25mm
      case 21   => bullet_35mm
      case 25   => bullet_75mm
      case 28   => bullet_9mm
      case 29   => bullet_9mm_AP
      case 50   => ancient_ammo_combo
      case 51   => ancient_ammo_vehicle
      case 54   => anniversary_ammo
      case 86   => aphelion_immolation_cannon_ammo
      case 89   => aphelion_laser_ammo
      case 97   => aphelion_plasma_rocket_ammo
      case 101  => aphelion_ppa_ammo
      case 106  => aphelion_starfire_ammo
      case 111  => armor_canister
      case 145  => bolt
      case 154  => burster_ammo
      case 180  => colossus_100mm_cannon_ammo
      case 186  => colossus_burster_ammo
      case 191  => colossus_chaingun_ammo
      case 195  => colossus_cluster_bomb_ammo
      case 205  => colossus_tank_cannon_ammo
      case 209  => comet_ammo
      case 265  => dualcycler_ammo
      case 272  => energy_cell
      case 275  => energy_gun_ammo
      case 285  => falcon_ammo
      case 287  => firebird_missile
      case 300  => flamethrower_ammo
      case 307  => flux_cannon_thresher_battery
      case 310  => fluxpod_ammo
      case 327  => frag_cartridge
      case 331  => frag_grenade_ammo
      case 347  => gauss_cannon_ammo
      case 389  => health_canister
      case 391  => heavy_grenade_mortar
      case 393  => heavy_rail_beam_battery
      case 399  => hellfire_ammo
      case 403  => hunter_seeker_missile
      case 413  => jammer_cartridge
      case 417  => jammer_grenade_ammo
      case 426  => lancer_cartridge
      case 434  => liberator_bomb
      case 463  => maelstrom_ammo
      case 540  => melee_ammo
      case 600  => oicw_ammo
      case 630  => pellet_gun_ammo
      case 637  => peregrine_dual_machine_gun_ammo
      case 645  => peregrine_mechhammer_ammo
      case 653  => peregrine_particle_cannon_ammo
      case 656  => peregrine_rocket_pod_ammo
      case 659  => peregrine_sparrow_ammo
      case 664  => phalanx_ammo
      case 674  => phoenix_missile
      case 677  => plasma_cartridge
      case 681  => plasma_grenade_ammo
      case 693  => pounder_ammo
      case 704  => pulse_battery
      case 712  => quasar_ammo
      case 722  => reaver_rocket
      case 734  => rocket
      case 745  => scattercannon_ammo
      case 755  => shotgun_shell
      case 756  => shotgun_shell_AP
      case 762  => six_shooter_ammo
      case 786  => skyguard_flak_cannon_ammo
      case 791  => sparrow_ammo
      case 820  => spitfire_aa_ammo
      case 823  => spitfire_ammo
      case 830  => starfire_ammo
      case 839  => striker_missile_ammo
      case 877  => trek_ammo
      case 922  => upgrade_canister
      case 998  => wasp_gun_ammo
      case 1000 => wasp_rocket_ammo
      case 1004 => winchester_ammo
      //weapons
      case 14   => cannon_dropship_20mm
      case 40   => advanced_missile_launcher_t
      case 55   => anniversary_gun
      case 56   => anniversary_guna
      case 57   => anniversary_gunb
      case 63   => apc_ballgun_l
      case 64   => apc_ballgun_r
      case 69   => apc_weapon_systema
      case 70   => apc_weapon_systemb
      case 72   => apc_weapon_systemc_nc
      case 73   => apc_weapon_systemc_tr
      case 74   => apc_weapon_systemc_vs
      case 76   => apc_weapon_systemd_nc
      case 77   => apc_weapon_systemd_tr
      case 78   => apc_weapon_systemd_vs
      case 119  => aurora_weapon_systema
      case 120  => aurora_weapon_systemb
      case 136  => battlewagon_weapon_systema
      case 137  => battlewagon_weapon_systemb
      case 138  => battlewagon_weapon_systemc
      case 139  => battlewagon_weapon_systemd
      case 140  => beamer
      case 146  => bolt_driver
      case 175  => chainblade
      case 177  => chaingun_p
      case 233  => cycler
      case 262  => dropship_rear_turret
      case 274  => energy_gun
      case 276  => energy_gun_nc
      case 278  => energy_gun_tr
      case 280  => energy_gun_vs
      case 298  => flail_weapon
      case 299  => flamethrower
      case 304  => flechette
      case 306  => flux_cannon_thresher
      case 324  => forceblade
      case 336  => fury_weapon_systema
      case 339  => galaxy_gunship_cannon
      case 340  => galaxy_gunship_gun
      case 342  => galaxy_gunship_tailgun
      case 345  => gauss
      case 371  => grenade_launcher_marauder
      case 394  => heavy_rail_beam_magrider
      case 396  => heavy_sniper
      case 406  => hunterseeker
      case 407  => ilc9
      case 411  => isp
      case 421  => katana
      case 425  => lancer
      case 429  => lasher
      case 433  => liberator_25mm_cannon
      case 435  => liberator_bomb_bay
      case 440  => liberator_weapon_system
      case 445  => lightgunship_weapon_system
      case 448  => lightning_weapon_system
      case 462  => maelstrom
      case 468  => magcutter
      case 534  => mediumtransport_weapon_systemA
      case 535  => mediumtransport_weapon_systemB
      case 556  => mini_chaingun
      case 587  => nchev_falcon
      case 588  => nchev_scattercannon
      case 589  => nchev_sparrow
      case 599  => oicw
      case 628  => particle_beam_magrider
      case 629  => pellet_gun
      case 666  => phalanx_avcombo
      case 668  => phalanx_flakcombo
      case 670  => phalanx_sgl_hevgatcan
      case 673  => phoenix
      case 699  => prowler_weapon_systemA
      case 700  => prowler_weapon_systemB
      case 701  => pulsar
      case 706  => punisher
      case 709  => quadassault_weapon_system
      case 714  => r_shotgun
      case 716  => radiator
      case 730  => repeater
      case 737  => rocklet
      case 740  => rotarychaingun_mosquito
      case 747  => scythe
      case 761  => six_shooter
      case 788  => skyguard_weapon_system
      case 817  => spiker
      case 822  => spitfire_aa_weapon
      case 827  => spitfire_weapon
      case 838  => striker
      case 845  => suppressor
      case 864  => thumper
      case 866  => thunderer_weapon_systema
      case 867  => thunderer_weapon_systemb
      case 888  => trhev_burster
      case 889  => trhev_dualcycler
      case 890  => trhev_pounder
      case 927  => vanguard_weapon_system
      case 968  => vshev_comet
      case 969  => vshev_quasar
      case 970  => vshev_starfire
      case 987  => vulture_bomb_bay
      case 990  => vulture_nose_weapon_system
      case 992  => vulture_tail_cannon
      case 1002 => wasp_weapon_system
      case 1003 => winchester
      case 267  => dynomite
      case 330  => frag_grenade
      case 416  => jammer_grenade
      case 680  => plasma_grenade
      //medkits
      case 536 => medkit
      case 842 => super_armorkit
      case 843 => super_medkit
      case 844 => super_staminakit
      //tools
      case 728 => remote_electronics_kit
      case 876 => trek
      case 531 => medicalapplicator
      case 132 => bank
      case 577 => nano_dispenser
      case 213 => command_detonater
      case 297 => flail_targeting_laser
      //deployables
      case 32  => ace
      case 39  => advanced_ace
      case 148 => boomer
      case 149 => boomer_trigger
      case _   => frag_grenade
    }
  }

  /** Apply default loadout to given player */
  def applyDefaultLoadout(player: Player): Unit = {
    val faction = player.Faction
    player.ExoSuit = ExoSuitType.Standard
    player.Slot(0).Equipment = Tool(GlobalDefinitions.StandardPistol(faction))
    player.Slot(2).Equipment = Tool(GlobalDefinitions.suppressor)
    player.Slot(4).Equipment = Tool(GlobalDefinitions.StandardMelee(faction))
    player.Slot(6).Equipment = AmmoBox(GlobalDefinitions.bullet_9mm)
    player.Slot(9).Equipment = AmmoBox(GlobalDefinitions.bullet_9mm)
    player.Slot(12).Equipment = AmmoBox(GlobalDefinitions.bullet_9mm)
    player.Slot(33).Equipment = AmmoBox(GlobalDefinitions.bullet_9mm_AP)
    player.Slot(36).Equipment = AmmoBox(GlobalDefinitions.StandardPistolAmmo(faction))
    player.Slot(39).Equipment = SimpleItem(GlobalDefinitions.remote_electronics_kit)
    player.Inventory.Items.foreach(_.obj.Faction = faction)
  }

  /*
  def fromStringImpl(c: blackbox.Context)(name: c.Expr[String]): c.Tree = {
    import c.universe._

    q"""
      ${name} match {
        case ..${GlobalDefinitions.getClass.getFields
      .map { f =>
        cq"""${f.getName} => net.psforever.objects.GlobalDefinitions.generator"""
      }}
      }
    """
  }

  def fromString(name: String): BasicDefinition = macro fromStringImpl

   */

  private val runtimeMirror  = universe.runtimeMirror(getClass.getClassLoader)
  private val instanceMirror = runtimeMirror.reflect(GlobalDefinitions)

  /** Returns definition for given string */
  // This is slow and ugly, the macro implementation from above would be better
  // But macros cannot be called from the project they're defined in, and moving this to another project is not easy
  // Making GlobalDefinitions iterable (map etc) should be the preferred solution
  def fromString(name: String): BasicDefinition = {
    universe.typeOf[GlobalDefinitions.type].decl(universe.TermName(name))
    val method = universe.typeOf[GlobalDefinitions.type].member(universe.TermName(name)).asMethod
    instanceMirror.reflectMethod(method).apply().asInstanceOf[BasicDefinition]
  }
}
