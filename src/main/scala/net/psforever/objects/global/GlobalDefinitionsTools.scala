// Copyright (c) 2024 PSForever
package net.psforever.objects.global

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.avatar.Certification
import net.psforever.objects.ce.DeployedItem
import net.psforever.objects.definition.ConstructionFireMode
import net.psforever.objects.definition.converter._
import net.psforever.objects.equipment._
import net.psforever.objects.inventory.InventoryTile

object GlobalDefinitionsTools {
  import GlobalDefinitions._

  /**
   * Initialize `ToolDefinition` globals.
   */
  def init(): Unit = {
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
    trhev_dualcycler.Descriptor = "trhev_antipersonnel"
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
    trhev_pounder.Descriptor = "trhev_antivehicular"
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
    trhev_burster.Descriptor = "trhev_antiaircraft"
    trhev_burster.Size = EquipmentSize.Max
    trhev_burster.AmmoTypes += burster_ammo
    trhev_burster.ProjectileTypes += burster_projectile
    trhev_burster.FireModes += new FireModeDefinition
    trhev_burster.FireModes.head.AmmoTypeIndices += 0
    trhev_burster.FireModes.head.AmmoSlotIndex = 0
    trhev_burster.FireModes.head.Magazine = 40

    nchev_scattercannon.Name = "nchev_scattercannon"
    nchev_scattercannon.Descriptor = "nchev_antipersonnel"
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
    nchev_falcon.Descriptor = "nchev_antivehicular"
    nchev_falcon.Size = EquipmentSize.Max
    nchev_falcon.AmmoTypes += falcon_ammo
    nchev_falcon.ProjectileTypes += falcon_projectile
    nchev_falcon.FireModes += new FireModeDefinition
    nchev_falcon.FireModes.head.AmmoTypeIndices += 0
    nchev_falcon.FireModes.head.AmmoSlotIndex = 0
    nchev_falcon.FireModes.head.Magazine = 20

    nchev_sparrow.Name = "nchev_sparrow"
    nchev_sparrow.Descriptor = "nchev_antiaircraft"
    nchev_sparrow.Size = EquipmentSize.Max
    nchev_sparrow.AmmoTypes += sparrow_ammo
    nchev_sparrow.ProjectileTypes += sparrow_projectile
    nchev_sparrow.FireModes += new FireModeDefinition
    nchev_sparrow.FireModes.head.AmmoTypeIndices += 0
    nchev_sparrow.FireModes.head.AmmoSlotIndex = 0
    nchev_sparrow.FireModes.head.Magazine = 12

    vshev_quasar.Name = "vshev_quasar"
    vshev_quasar.Descriptor = "vshev_antipersonnel"
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
    vshev_comet.Descriptor = "vshev_antivehicular"
    vshev_comet.Size = EquipmentSize.Max
    vshev_comet.AmmoTypes += comet_ammo
    vshev_comet.ProjectileTypes += comet_projectile
    vshev_comet.FireModes += new FireModeDefinition
    vshev_comet.FireModes.head.AmmoTypeIndices += 0
    vshev_comet.FireModes.head.AmmoSlotIndex = 0
    vshev_comet.FireModes.head.Magazine = 10

    vshev_starfire.Name = "vshev_starfire"
    vshev_starfire.Descriptor = "vshev_antiaircraft"
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
      Item(DeployedItem.tank_traps, Set(Certification.FortificationEngineering))
    }
    advanced_ace.Modes += new ConstructionFireMode {
      Item(DeployedItem.portable_manned_turret, Set(Certification.AssaultEngineering))
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
}
