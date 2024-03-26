// Copyright (c) 2024 PSForever
package net.psforever.objects.global

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.equipment.EquipmentSize
import net.psforever.objects.inventory.InventoryTile

object GlobalDefinitionsAmmo {
  import GlobalDefinitions._

  /**
   * Initialize `AmmoBoxDefinition` globals.
   */
  final def init(): Unit = {
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
}
