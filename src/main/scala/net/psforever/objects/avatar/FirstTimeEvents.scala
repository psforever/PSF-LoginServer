// Copyright (c) 2020 PSForever
package net.psforever.objects.avatar

object FirstTimeEvents {
  object TR {
    val InfantryWeapons: Set[String] = Set(
      "used_chainblade",
      "used_repeater",
      "used_cycler",
      "used_mini_chaingun",
      "used_striker",
      "used_anniversary_guna"
    )

    val Vehicles: Set[String] = Set(
      "used_heavy_grenade_launcher",
      "used_apc_tr_weapon",
      "used_15mm_chaingun",
      "used_105mm_cannon",
      "used_colossus_burster",
      "used_colossus_chaingun",
      "used_colossus_cluster_bomb_pod",
      "used_colossus_dual_100mm_cannons",
      "used_colossus_tank_cannon",
      "visited_threemanheavybuggy",
      "visited_battlewagon",
      "visited_apc_tr",
      "visited_prowler",
      "visited_colossus_flight",
      "visited_colossus_gunner"
    )

    val Other: Set[String] = Set(
      "used_trhev_dualcycler",
      "used_trhev_pounder",
      "used_trhev_burster",
      "used_colossus_dual_100mm_cannons",
      "used_colossus_tank_cannon",
      "used_energy_gun_tr",
      "visited_portable_manned_turret_tr"
    )

    val All: Set[String] = InfantryWeapons ++ Vehicles ++ Other
  }

  object NC {
    val InfantryWeapons: Set[String] = Set(
      "used_magcutter",
      "used_isp",
      "used_gauss",
      "used_r_shotgun",
      "used_hunterseeker",
      "used_anniversary_gun"
    )

    val Vehicles: Set[String] = Set(
      "used_firebird",
      "used_gauss_cannon",
      "used_apc_nc_weapon",
      "used_vanguard_weapons",
      "used_peregrine_dual_machine_gun",
      "used_peregrine_dual_rocket_pods",
      "used_peregrine_mechhammer",
      "used_peregrine_particle_cannon",
      "used_peregrine_sparrow",
      "visited_twomanheavybuggy",
      "visited_thunderer",
      "visited_apc_nc",
      "visited_vanguard",
      "visited_peregrine_flight",
      "visited_peregrine_gunner"
    )

    val Other: Set[String] = Set(
      "used_nchev_scattercannon",
      "used_nchev_falcon",
      "used_nchev_sparrow",
      "used_energy_gun_nc",
      "visited_portable_manned_turret_nc"
    )

    val All: Set[String] = InfantryWeapons ++ Vehicles ++ Other
  }

  object VS {
    val InfantryWeapons: Set[String] = Set(
      "used_forceblade",
      "used_beamer",
      "used_pulsar",
      "used_lasher",
      "used_lancer",
      "used_anniversary_gunb"
    )

    val Vehicles: Set[String] = Set(
      "used_fluxpod",
      "used_apc_vs_weapon",
      "used_heavy_rail_beam",
      "used_pulsed_particle_accelerator",
      "used_flux_cannon",
      "used_aphelion_laser",
      "used_aphelion_starfire",
      "used_aphelion_immolation_cannon",
      "used_aphelion_plasma_rocket_pod",
      "used_aphelion_ppa",
      "visited_twomanhoverbuggy",
      "visited_aurora",
      "visited_apc_vs",
      "visited_magrider",
      "visited_aphelion_flight",
      "visited_aphelion_gunner"
    )

    val Other: Set[String] = Set(
      "used_vshev_quasar",
      "used_vshev_comet",
      "used_vshev_starfire",
      "used_energy_gun_vs",
      "visited_portable_manned_turret_vs"
    )

    val All: Set[String] = InfantryWeapons ++ Vehicles ++ Other
  }

  object Standard {
    val InfantryWeapons: Set[String] = Set(
      "used_grenade_plasma",
      "used_grenade_jammer",
      "used_grenade_frag",
      "used_katana",
      "used_ilc9",
      "used_suppressor",
      "used_punisher",
      "used_flechette",
      "used_phoenix",
      "used_thumper",
      "used_rocklet",
      "used_bolt_driver",
      "used_heavy_sniper",
      "used_oicw",
      "used_flamethrower"
    )

    val Vehicles: Set[String] = Set(
      "used_armor_siphon",
      "used_ntu_siphon",
      "used_ballgun",
      "used_skyguard_weapons",
      "used_reaver_weapons",
      "used_lightning_weapons",
      "used_wasp_weapon_system",
      "used_20mm_cannon",
      "used_25mm_cannon",
      "used_35mm_cannon",
      "used_35mm_rotarychaingun",
      "used_75mm_cannon",
      "used_rotarychaingun",
      "used_vulture_bombardier",
      "used_vulture_nose_cannon",
      "used_vulture_tail_cannon",
      "used_liberator_bombardier",
      "visited_ams",
      "visited_ant",
      "visited_quadassault",
      "visited_fury",
      "visited_quadstealth",
      "visited_two_man_assault_buggy",
      "visited_skyguard",
      "visited_mediumtransport",
      "visited_apc",
      "visited_lightning",
      "visited_mosquito",
      "visited_lightgunship",
      "visited_wasp",
      "visited_liberator",
      "visited_vulture",
      "visited_dropship",
      "visited_galaxy_gunship",
      "visited_phantasm",
      "visited_lodestar"
    )

    val Facilities: Set[String] = Set(
      "visited_broadcast_warpgate",
      "visited_warpgate_small",
      "visited_respawn_terminal",
      "visited_deconstruction_terminal",
      "visited_capture_terminal",
      "visited_secondary_capture",
      "visited_LLU_socket",
      "visited_resource_silo",
      "visited_med_terminal",
      "visited_adv_med_terminal",
      "visited_repair_silo",
      "visited_order_terminal",
      "visited_certification_terminal",
      "visited_implant_terminal",
      "visited_locker",
      "visited_ground_vehicle_terminal",
      "visited_bfr_terminal",
      "visited_air_vehicle_terminal",
      "visited_galaxy_terminal",
      "visited_generator",
      "visited_generator_terminal",
      "visited_wall_turret",
      "used_phalanx",
      "used_phalanx_avcombo",
      "used_phalanx_flakcombo",
      "visited_external_door_lock"
    )

    val Other: Set[String] = Set(
      "used_command_uplink",
      "used_med_app",
      "used_nano_dispenser",
      "used_bank",
      "used_ace",
      "used_advanced_ace",
      "used_rek",
      "used_trek",
      "used_laze_pointer",
      "used_telepad",
      "visited_motion_sensor",
      "visited_sensor_shield",
      "visited_spitfire_turret",
      "visited_spitfire_cloaked",
      "visited_spitfire_aa",
      "visited_shield_generator",
      "visited_tank_traps"
    )

    val All: Set[String] = InfantryWeapons ++ Vehicles ++ Facilities ++ Other
  }

  object Cavern {
    val InfantryWeapons: Set[String] = Set(
      "used_spiker",
      "used_radiator",
      "used_maelstrom"
    )

    val Vehicles: Set[String] = Set(
      "used_scythe",
      "used_flail_weapon",
      "visited_switchblade",
      "visited_flail",
      "visited_router"
    )

    val Facilities: Set[String] = Set(
      "used_ancient_turret_weapon",
      "visited_vanu_control_console",
      "visited_ancient_air_vehicle_terminal",
      "visited_ancient_equipment_terminal",
      "visited_ancient_ground_vehicle_terminal",
      "visited_health_crystal",
      "visited_repair_crystal",
      "visited_vehicle_crystal",
      "visited_damage_crystal",
      "visited_energy_crystal"
    )

    val Other: Set[String] = Set(
      "visited_vanu_module"
    )

    val All: Set[String] = InfantryWeapons ++ Vehicles ++ Facilities ++ Other
  }

  val Maps: Set[String] = Set(
    "map01",
    "map02",
    "map03",
    "map04",
    "map05",
    "map06",
    "map07",
    "map08",
    "map09",
    "map10",
    "map11",
    "map12",
    "map13",
    "map14",
    "map15",
    "map16",
    "ugd01",
    "ugd02",
    "ugd03",
    "ugd04",
    "ugd05",
    "ugd06",
    "map96",
    "map97",
    "map98",
    "map99"
  )

  val Monoliths: Set[String] = Set(
    "visited_monolith_amerish",
    "visited_monolith_ceryshen",
    "visited_monolith_cyssor",
    "visited_monolith_esamir",
    "visited_monolith_forseral",
    "visited_monolith_hossin",
    "visited_monolith_ishundar",
    "visited_monolith_searhus",
    "visited_monolith_solsar"
  )

  val Gingerman: Set[String] = Set(
    "visited_gingerman_atar",
    "visited_gingerman_dahaka",
    "visited_gingerman_hvar",
    "visited_gingerman_izha",
    "visited_gingerman_jamshid",
    "visited_gingerman_mithra",
    "visited_gingerman_rashnu",
    "visited_gingerman_sraosha",
    "visited_gingerman_yazata",
    "visited_gingerman_zal"
  )

  val Sled: Set[String] = Set(
    "visited_sled01",
    "visited_sled02",
    "visited_sled04",
    "visited_sled05",
    "visited_sled06",
    "visited_sled07",
    "visited_sled08",
    "visited_sled09"
  )

  val Snowman: Set[String] = Set(
    "visited_snowman_amerish",
    "visited_snowman_ceryshen",
    "visited_snowman_cyssor",
    "visited_snowman_esamir",
    "visited_snowman_forseral",
    "visited_snowman_hossin",
    "visited_snowman_ishundar",
    "visited_snowman_searhus",
    "visited_snowman_solsar"
  )

  val Charlie: Set[String] = Set(
    "visited_charlie01",
    "visited_charlie02",
    "visited_charlie03",
    "visited_charlie04",
    "visited_charlie05",
    "visited_charlie06",
    "visited_charlie07",
    "visited_charlie08",
    "visited_charlie09"
  )

  val BattleRanks: Set[String] = Set(
    "xpe_battle_rank_1",
    "xpe_battle_rank_2",
    "xpe_battle_rank_3",
    "xpe_battle_rank_4",
    "xpe_battle_rank_5",
    "xpe_battle_rank_6",
    "xpe_battle_rank_7",
    "xpe_battle_rank_8",
    "xpe_battle_rank_9",
    "xpe_battle_rank_10",
    "xpe_battle_rank_11",
    "xpe_battle_rank_12",
    "xpe_battle_rank_13",
    "xpe_battle_rank_14",
    "xpe_battle_rank_15",
    "xpe_battle_rank_16",
    "xpe_battle_rank_17",
    "xpe_battle_rank_18",
    "xpe_battle_rank_19",
    "xpe_battle_rank_20",
    "xpe_battle_rank_21",
    "xpe_battle_rank_22",
    "xpe_battle_rank_23",
    "xpe_battle_rank_24",
    "xpe_battle_rank_25",
    "xpe_battle_rank_26",
    "xpe_battle_rank_27",
    "xpe_battle_rank_28",
    "xpe_battle_rank_29",
    "xpe_battle_rank_30",
    "xpe_battle_rank_31",
    "xpe_battle_rank_32",
    "xpe_battle_rank_33",
    "xpe_battle_rank_34",
    "xpe_battle_rank_35",
    "xpe_battle_rank_36",
    "xpe_battle_rank_37",
    "xpe_battle_rank_38",
    "xpe_battle_rank_39",
    "xpe_battle_rank_40"
  )

  val CommandRanks: Set[String] = Set(
    "xpe_command_rank_1",
    "xpe_command_rank_2",
    "xpe_command_rank_3",
    "xpe_command_rank_4",
    "xpe_command_rank_5"
  )

  val Training: Set[String] = Set(
    "training_welcome",
    "training_map",
    "training_hart",
    "training_warpgates",
    "training_weapons01",
    "training_armors",
    "training_healing",
    "training_certifications",
    "training_inventory",
    "training_vehicles",
    "training_implants"
  )

  val OldTraining: Set[String] = Set(
    "training_start_tr",
    "training_start_nc",
    "training_start_vs"
  )

  val Generic: Set[String] = Set(
    "xpe_overhead_map",
    "xpe_mail_alert",
    "xpe_join_platoon",
    "xpe_form_platoon",
    "xpe_join_outfit",
    "xpe_form_outfit",
    "xpe_join_squad",
    "xpe_form_squad",
    "xpe_blackops",
    "xpe_instant_action",
    "xpe_orbital_shuttle",
    "xpe_drop_pod",
    "xpe_sanctuary_help",
    "xpe_bind_facility",
    "xpe_warp_gate",
    "xpe_warp_gate_usage",
    "xpe_bind_ams",
    "xpe_th_nonsanc",
    "xpe_th_ammo",
    "xpe_th_firemodes",
    "xpe_th_cloak",
    "xpe_th_max",
    "xpe_th_ant",
    "xpe_th_ams",
    "xpe_th_ground",
    "xpe_th_ground_p",
    "xpe_th_air",
    "xpe_th_air_p",
    "xpe_th_afterburner",
    "xpe_th_hover",
    "xpe_th_switchblade",
    "xpe_th_router",
    "xpe_th_flail",
    "xpe_th_bfr"
  )
}
