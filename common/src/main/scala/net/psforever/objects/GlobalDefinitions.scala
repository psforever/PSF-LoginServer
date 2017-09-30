// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition._
import net.psforever.objects.definition.converter.{CommandDetonaterConverter, LockerContainerConverter, REKConverter}
import net.psforever.objects.doors.{DoorDefinition, IFFLockDefinition}
import net.psforever.objects.equipment.CItem.DeployedItem
import net.psforever.objects.equipment._
import net.psforever.objects.inventory.InventoryTile
<<<<<<< c5ae9e477ccade5759eac2e9526ba898d0e8f16b
import net.psforever.objects.terminals.{CertTerminalDefinition, OrderTerminalDefinition}
import net.psforever.packet.game.objectcreate.ObjectClass
=======
import net.psforever.objects.terminals.OrderTerminalDefinition
>>>>>>> automated doors, IFF locks, and bases thus that only permissible doors can be opened by players of correct faction alignment; Base is just a prototype example, hastily created for this functionality; LocalService will eventually be used for doors messages (and other things)
import net.psforever.types.PlanetSideEmpire

object GlobalDefinitions {
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
  def AntiVehicular(faction : PlanetSideEmpire.Value) : ToolDefinition = {
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
    * Useful for determining if some item has to be dropped during an activity like `InfantryLoadout` switching.
    * @param edef the `EquipmentDefinition` of the item
    * @return the faction alignment, or `Neutral`
    */
  def isFactionWeapon(edef : EquipmentDefinition) : PlanetSideEmpire.Value = {
    edef match {
      case `chainblade` | `repeater` | `anniversary_guna` | `cycler` | `mini_chaingun` | `striker` =>
        PlanetSideEmpire.TR
      case `magcutter` | `isp` | `anniversary_gun` | `gauss` | `r_shotgun` | `hunterseeker` =>
        PlanetSideEmpire.NC
      case `forceblade` | `beamer` | `anniversary_gunb` | `pulsar` | `lasher` | `lancer` =>
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
      case `chainblade` | `repeater` | `anniversary_guna` | `cycler` | `mini_chaingun` | `striker` | `striker_missile_ammo` =>
        PlanetSideEmpire.TR
      case `magcutter` | `isp` | `anniversary_gun` | `gauss` | `r_shotgun` | `hunterseeker` | `hunter_seeker_missile` =>
        PlanetSideEmpire.NC
      case `forceblade` | `beamer` | `anniversary_gunb` | `pulsar` | `lasher` | `lancer` | `energy_cell` | `lancer_cartridge` =>
        PlanetSideEmpire.VS
      case _ =>
        PlanetSideEmpire.NEUTRAL
    }
  }

  /**
    * Using the definition for a piece of `Equipment` determine whether it is a "cavern weapon."
    * Useful for determining if some item has to be dropped during an activity like `InfantryLoadout` switching.
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

  /*
  Implants
   */
  val
  advanced_regen = ImplantDefinition(0)

  val
  targeting = ImplantDefinition(1)

  val
  audio_amplifier = ImplantDefinition(2)

  val
  darklight_vision = ImplantDefinition(3)

  val
  melee_booster = ImplantDefinition(4)

  val
  personal_shield = ImplantDefinition(5)

  val
  range_magnifier = ImplantDefinition(6)

  val
  second_wind = ImplantDefinition(7)

  val
  silent_run = ImplantDefinition(8)

  val
  surge = ImplantDefinition(9)

  /*
  Equipment (locker_container, kits, ammunition, weapons)
   */
  import net.psforever.packet.game.objectcreate.ObjectClass
  val
  locker_container = new EquipmentDefinition(456) {
    Name = "locker container"
    Size = EquipmentSize.Inventory
    Packet = new LockerContainerConverter()
  }

  val
  medkit = KitDefinition(Kits.medkit)

  val
  super_medkit = KitDefinition(Kits.super_medkit)

  val
  super_armorkit = KitDefinition(Kits.super_armorkit)

  val
  super_staminakit = KitDefinition(Kits.super_staminakit) //super stimpak

  val
  melee_ammo = AmmoBoxDefinition(Ammo.melee_ammo)

  val
  frag_grenade_ammo = AmmoBoxDefinition(Ammo.frag_grenade_ammo)

  val
  plasma_grenade_ammo = AmmoBoxDefinition(Ammo.plasma_grenade_ammo)

  val
  jammer_grenade_ammo = AmmoBoxDefinition(Ammo.jammer_grenade_ammo)

  val
  bullet_9mm = AmmoBoxDefinition(Ammo.bullet_9mm)
  bullet_9mm.Capacity = 50
  bullet_9mm.Tile = InventoryTile.Tile33

  val
  bullet_9mm_AP = AmmoBoxDefinition(Ammo.bullet_9mm_AP)
  bullet_9mm_AP.Capacity = 50
  bullet_9mm_AP.Tile = InventoryTile.Tile33

  val
  shotgun_shell = AmmoBoxDefinition(Ammo.shotgun_shell)
  shotgun_shell.Capacity = 32
  shotgun_shell.Tile = InventoryTile.Tile33

  val
  shotgun_shell_AP = AmmoBoxDefinition(Ammo.shotgun_shell_AP)
  shotgun_shell_AP.Capacity = 32
  shotgun_shell_AP.Tile = InventoryTile.Tile33

  val
  energy_cell = AmmoBoxDefinition(Ammo.energy_cell)
  energy_cell.Capacity = 50
  energy_cell.Tile = InventoryTile.Tile33

  val
  anniversary_ammo = AmmoBoxDefinition(Ammo.anniversary_ammo) //10mm multi-phase
  anniversary_ammo.Capacity = 30
  anniversary_ammo.Tile = InventoryTile.Tile33

  val
  ancient_ammo_combo = AmmoBoxDefinition(Ammo.ancient_ammo_combo)
  ancient_ammo_combo.Capacity = 30
  ancient_ammo_combo.Tile = InventoryTile.Tile33

  val
  maelstrom_ammo = AmmoBoxDefinition(Ammo.maelstrom_ammo)
  maelstrom_ammo.Capacity = 50
  maelstrom_ammo.Tile = InventoryTile.Tile33

  val
  phoenix_missile = AmmoBoxDefinition(Ammo.phoenix_missile) //decimator missile

  val
  striker_missile_ammo = AmmoBoxDefinition(Ammo.striker_missile_ammo)
  striker_missile_ammo.Capacity = 15
  striker_missile_ammo.Tile = InventoryTile.Tile44

  val
  hunter_seeker_missile = AmmoBoxDefinition(Ammo.hunter_seeker_missile) //phoenix missile
  hunter_seeker_missile.Capacity = 9
  hunter_seeker_missile.Tile = InventoryTile.Tile44

  val
  lancer_cartridge = AmmoBoxDefinition(Ammo.lancer_cartridge)
  lancer_cartridge.Capacity = 18
  lancer_cartridge.Tile = InventoryTile.Tile44

  val
  rocket = AmmoBoxDefinition(Ammo.rocket)
  rocket.Capacity = 15
  rocket.Tile = InventoryTile.Tile33

  val
  frag_cartridge = AmmoBoxDefinition(Ammo.frag_cartridge)
  frag_cartridge.Capacity = 12
  frag_cartridge.Tile = InventoryTile.Tile33

  val
  plasma_cartridge = AmmoBoxDefinition(Ammo.plasma_cartridge)
  plasma_cartridge.Capacity = 12
  plasma_cartridge.Tile = InventoryTile.Tile33

  val
  jammer_cartridge = AmmoBoxDefinition(Ammo.jammer_cartridge)
  jammer_cartridge.Capacity = 12
  jammer_cartridge.Tile = InventoryTile.Tile33

  val
  bolt = AmmoBoxDefinition(Ammo.bolt)
  bolt.Capacity = 10
  bolt.Tile = InventoryTile.Tile33

  val
  oicw_ammo = AmmoBoxDefinition(Ammo.oicw_ammo) //scorpion missile
  oicw_ammo.Capacity = 10
  oicw_ammo.Tile = InventoryTile.Tile44

  val
  flamethrower_ammo = AmmoBoxDefinition(Ammo.flamethrower_ammo)
  flamethrower_ammo.Capacity = 100
  flamethrower_ammo.Tile = InventoryTile.Tile44

  val
  health_canister = AmmoBoxDefinition(Ammo.health_canister)
  health_canister.Capacity = 100
  health_canister.Tile = InventoryTile.Tile33

  val
  armor_canister = AmmoBoxDefinition(Ammo.armor_canister)
  armor_canister.Capacity = 100
  armor_canister.Tile = InventoryTile.Tile33

  val
  upgrade_canister = AmmoBoxDefinition(Ammo.upgrade_canister)
  upgrade_canister.Capacity = 100
  upgrade_canister.Tile = InventoryTile.Tile33

  val
  trek_ammo = AmmoBoxDefinition(Ammo.trek_ammo)
//
  val
  bullet_35mm = AmmoBoxDefinition(Ammo.bullet_35mm) //liberator nosegun
  bullet_35mm.Capacity = 100
  bullet_35mm.Tile = InventoryTile.Tile44

  val
  aphelion_laser_ammo = AmmoBoxDefinition(Ammo.aphelion_laser_ammo)
  aphelion_laser_ammo.Capacity = 165
  aphelion_laser_ammo.Tile = InventoryTile.Tile44

  val
  aphelion_immolation_cannon_ammo = AmmoBoxDefinition(Ammo.aphelion_immolation_cannon_ammo)
  aphelion_immolation_cannon_ammo.Capacity = 100
  aphelion_immolation_cannon_ammo.Tile = InventoryTile.Tile55

  val
  aphelion_plasma_rocket_ammo = AmmoBoxDefinition(Ammo.aphelion_plasma_rocket_ammo)
  aphelion_plasma_rocket_ammo.Capacity = 195
  aphelion_plasma_rocket_ammo.Tile = InventoryTile.Tile55

  val
  aphelion_ppa_ammo = AmmoBoxDefinition(Ammo.aphelion_ppa_ammo)
  aphelion_ppa_ammo.Capacity = 110
  aphelion_ppa_ammo.Tile = InventoryTile.Tile44

  val
  aphelion_starfire_ammo = AmmoBoxDefinition(Ammo.aphelion_starfire_ammo)
  aphelion_starfire_ammo.Capacity = 132
  aphelion_starfire_ammo.Tile = InventoryTile.Tile44

  val
  skyguard_flak_cannon_ammo = AmmoBoxDefinition(Ammo.skyguard_flak_cannon_ammo)
  skyguard_flak_cannon_ammo.Capacity = 200
  skyguard_flak_cannon_ammo.Tile = InventoryTile.Tile44

  val
  flux_cannon_thresher_battery = AmmoBoxDefinition(Ammo.flux_cannon_thresher_battery)
  flux_cannon_thresher_battery.Capacity = 150
  flux_cannon_thresher_battery.Tile = InventoryTile.Tile44

  val
  fluxpod_ammo = AmmoBoxDefinition(Ammo.fluxpod_ammo)
  fluxpod_ammo.Capacity = 80
  fluxpod_ammo.Tile = InventoryTile.Tile44

  val
  hellfire_ammo = AmmoBoxDefinition(Ammo.hellfire_ammo)
  hellfire_ammo.Capacity = 24
  hellfire_ammo.Tile = InventoryTile.Tile44

  val
  liberator_bomb = AmmoBoxDefinition(Ammo.liberator_bomb)
  liberator_bomb.Capacity = 20
  liberator_bomb.Tile = InventoryTile.Tile44

  val
  bullet_25mm = AmmoBoxDefinition(Ammo.bullet_25mm) //liberator tailgun
  bullet_25mm.Capacity = 150
  bullet_25mm.Tile = InventoryTile.Tile44

  val
  bullet_75mm = AmmoBoxDefinition(Ammo.bullet_75mm) //lightning shell
  bullet_75mm.Capacity = 100
  bullet_75mm.Tile = InventoryTile.Tile44

  val
  heavy_grenade_mortar = AmmoBoxDefinition(Ammo.heavy_grenade_mortar) //marauder and gal gunship
  heavy_grenade_mortar.Capacity = 100
  heavy_grenade_mortar.Tile = InventoryTile.Tile44

  val
  pulse_battery = AmmoBoxDefinition(Ammo.pulse_battery)
  pulse_battery.Capacity = 100
  pulse_battery.Tile = InventoryTile.Tile44

  val
  heavy_rail_beam_battery = AmmoBoxDefinition(Ammo.heavy_rail_beam_battery)
  heavy_rail_beam_battery.Capacity = 100
  heavy_rail_beam_battery.Tile = InventoryTile.Tile44

  val
  reaver_rocket = AmmoBoxDefinition(Ammo.reaver_rocket)
  reaver_rocket.Capacity = 12
  reaver_rocket.Tile = InventoryTile.Tile44

  val
  bullet_20mm = AmmoBoxDefinition(Ammo.bullet_20mm) //reaver nosegun
  bullet_20mm.Capacity = 200
  bullet_20mm.Tile = InventoryTile.Tile44

  val
  bullet_12mm = AmmoBoxDefinition(Ammo.bullet_12mm) //common
  bullet_12mm.Capacity = 200
  bullet_12mm.Tile = InventoryTile.Tile44

  val
  wasp_rocket_ammo = AmmoBoxDefinition(Ammo.wasp_rocket_ammo)
  wasp_rocket_ammo.Capacity = 6
  wasp_rocket_ammo.Tile = InventoryTile.Tile44

  val
  wasp_gun_ammo = AmmoBoxDefinition(Ammo.wasp_gun_ammo) //wasp nosegun
  wasp_gun_ammo.Capacity = 150
  wasp_gun_ammo.Tile = InventoryTile.Tile44

  val
  bullet_15mm = AmmoBoxDefinition(Ammo.bullet_15mm)
  bullet_15mm.Capacity = 360
  bullet_15mm.Tile = InventoryTile.Tile44

  val
  colossus_100mm_cannon_ammo = AmmoBoxDefinition(Ammo.colossus_100mm_cannon_ammo)
  colossus_100mm_cannon_ammo.Capacity = 90
  colossus_100mm_cannon_ammo.Tile = InventoryTile.Tile55

  val
  colossus_burster_ammo = AmmoBoxDefinition(Ammo.colossus_burster_ammo)
  colossus_burster_ammo.Capacity = 235
  colossus_burster_ammo.Tile = InventoryTile.Tile44

  val
  colossus_cluster_bomb_ammo = AmmoBoxDefinition(Ammo.colossus_cluster_bomb_ammo) //colossus mortar launcher shells
  colossus_cluster_bomb_ammo.Capacity = 150
  colossus_cluster_bomb_ammo.Tile = InventoryTile.Tile55

  val
  colossus_chaingun_ammo = AmmoBoxDefinition(Ammo.colossus_chaingun_ammo)
  colossus_chaingun_ammo.Capacity = 600
  colossus_chaingun_ammo.Tile = InventoryTile.Tile44

  val
  colossus_tank_cannon_ammo = AmmoBoxDefinition(Ammo.colossus_tank_cannon_ammo)
  colossus_tank_cannon_ammo.Capacity = 110
  colossus_tank_cannon_ammo.Tile = InventoryTile.Tile44

  val
  bullet_105mm = AmmoBoxDefinition(Ammo.bullet_105mm) //prowler 100mm cannon shell
  bullet_105mm.Capacity = 100
  bullet_105mm.Tile = InventoryTile.Tile44

  val
  gauss_cannon_ammo = AmmoBoxDefinition(Ammo.gauss_cannon_ammo)
  gauss_cannon_ammo.Capacity = 15
  gauss_cannon_ammo.Tile = InventoryTile.Tile44

  val
  peregrine_dual_machine_gun_ammo = AmmoBoxDefinition(Ammo.peregrine_dual_machine_gun_ammo)
  peregrine_dual_machine_gun_ammo.Capacity = 240
  peregrine_dual_machine_gun_ammo.Tile = InventoryTile.Tile44

  val
  peregrine_mechhammer_ammo = AmmoBoxDefinition(Ammo.peregrine_mechhammer_ammo)
  peregrine_mechhammer_ammo.Capacity = 30
  peregrine_mechhammer_ammo.Tile = InventoryTile.Tile44

  val
  peregrine_particle_cannon_ammo = AmmoBoxDefinition(Ammo.peregrine_particle_cannon_ammo)
  peregrine_particle_cannon_ammo.Capacity = 40
  peregrine_particle_cannon_ammo.Tile = InventoryTile.Tile55

  val
  peregrine_rocket_pod_ammo = AmmoBoxDefinition(Ammo.peregrine_rocket_pod_ammo)
  peregrine_rocket_pod_ammo.Capacity = 275
  peregrine_rocket_pod_ammo.Tile = InventoryTile.Tile55

  val
  peregrine_sparrow_ammo = AmmoBoxDefinition(Ammo.peregrine_sparrow_ammo)
  peregrine_sparrow_ammo.Capacity = 150
  peregrine_sparrow_ammo.Tile = InventoryTile.Tile44

  val
  bullet_150mm = AmmoBoxDefinition(Ammo.bullet_150mm)
  bullet_150mm.Capacity = 50
  bullet_150mm.Tile = InventoryTile.Tile44

  val
  chainblade = ToolDefinition(ObjectClass.chainblade)
  chainblade.Size = EquipmentSize.Melee
  chainblade.AmmoTypes += Ammo.melee_ammo
  chainblade.FireModes += new FireModeDefinition
  chainblade.FireModes.head.AmmoTypeIndices += 0
  chainblade.FireModes.head.AmmoSlotIndex = 0
  chainblade.FireModes.head.Magazine = 1
  chainblade.FireModes += new FireModeDefinition
  chainblade.FireModes(1).AmmoTypeIndices += 0
  chainblade.FireModes(1).AmmoSlotIndex = 0
  chainblade.FireModes(1).Magazine = 1

  val
  magcutter = ToolDefinition(ObjectClass.magcutter)
  magcutter.Size = EquipmentSize.Melee
  magcutter.AmmoTypes += Ammo.melee_ammo
  magcutter.FireModes += new FireModeDefinition
  magcutter.FireModes.head.AmmoTypeIndices += 0
  magcutter.FireModes.head.AmmoSlotIndex = 0
  magcutter.FireModes.head.Magazine = 1
  magcutter.FireModes += new FireModeDefinition
  magcutter.FireModes(1).AmmoTypeIndices += 0
  magcutter.FireModes(1).AmmoSlotIndex = 0
  magcutter.FireModes(1).Magazine = 1

  val
  forceblade = ToolDefinition(ObjectClass.forceblade)
  forceblade.Size = EquipmentSize.Melee
  forceblade.AmmoTypes += Ammo.melee_ammo
  forceblade.FireModes += new FireModeDefinition
  forceblade.FireModes.head.AmmoTypeIndices += 0
  forceblade.FireModes.head.AmmoSlotIndex = 0
  forceblade.FireModes.head.Magazine = 1
  forceblade.FireModes.head.Chamber = 0
  forceblade.FireModes += new FireModeDefinition
  forceblade.FireModes(1).AmmoTypeIndices += 0
  forceblade.FireModes(1).AmmoSlotIndex = 0
  forceblade.FireModes(1).Magazine = 1
  forceblade.FireModes(1).Chamber = 0

  val
  katana = ToolDefinition(ObjectClass.katana)
  katana.Size = EquipmentSize.Melee
  katana.AmmoTypes += Ammo.melee_ammo
  katana.FireModes += new FireModeDefinition
  katana.FireModes.head.AmmoTypeIndices += 0
  katana.FireModes.head.AmmoSlotIndex = 0
  katana.FireModes.head.Magazine = 1
  katana.FireModes.head.Chamber = 0
  katana.FireModes += new FireModeDefinition
  katana.FireModes(1).AmmoTypeIndices += 0
  katana.FireModes(1).AmmoSlotIndex = 0
  katana.FireModes(1).Magazine = 1
  katana.FireModes(1).Chamber = 0

  val
  frag_grenade = ToolDefinition(ObjectClass.frag_grenade)
  frag_grenade.Size = EquipmentSize.Pistol
  frag_grenade.AmmoTypes += Ammo.frag_grenade_ammo
  frag_grenade.FireModes += new FireModeDefinition
  frag_grenade.FireModes.head.AmmoTypeIndices += 0
  frag_grenade.FireModes.head.AmmoSlotIndex = 0
  frag_grenade.FireModes.head.Magazine = 3
  frag_grenade.FireModes += new FireModeDefinition
  frag_grenade.FireModes(1).AmmoTypeIndices += 0
  frag_grenade.FireModes(1).AmmoSlotIndex = 0
  frag_grenade.FireModes(1).Magazine = 3
  frag_grenade.Tile = InventoryTile.Tile22

  val
  plasma_grenade = ToolDefinition(ObjectClass.plasma_grenade)
  plasma_grenade.Size = EquipmentSize.Pistol
  plasma_grenade.AmmoTypes += Ammo.plasma_grenade_ammo
  plasma_grenade.FireModes += new FireModeDefinition
  plasma_grenade.FireModes.head.AmmoTypeIndices += 0
  plasma_grenade.FireModes.head.AmmoSlotIndex = 0
  plasma_grenade.FireModes.head.Magazine = 3
  plasma_grenade.FireModes += new FireModeDefinition
  plasma_grenade.FireModes(1).AmmoTypeIndices += 0
  plasma_grenade.FireModes(1).AmmoSlotIndex = 0
  plasma_grenade.FireModes(1).Magazine = 3
  plasma_grenade.Tile = InventoryTile.Tile22

  val
  jammer_grenade = ToolDefinition(ObjectClass.jammer_grenade)
  jammer_grenade.Size = EquipmentSize.Pistol
  jammer_grenade.AmmoTypes += Ammo.jammer_grenade_ammo
  jammer_grenade.FireModes += new FireModeDefinition
  jammer_grenade.FireModes.head.AmmoTypeIndices += 0
  jammer_grenade.FireModes.head.AmmoSlotIndex = 0
  jammer_grenade.FireModes.head.Magazine = 3
  jammer_grenade.FireModes += new FireModeDefinition
  jammer_grenade.FireModes(1).AmmoTypeIndices += 0
  jammer_grenade.FireModes(1).AmmoSlotIndex = 0
  jammer_grenade.FireModes(1).Magazine = 3
  jammer_grenade.Tile = InventoryTile.Tile22

  val
  repeater = ToolDefinition(ObjectClass.repeater)
  repeater.Size = EquipmentSize.Pistol
  repeater.AmmoTypes += Ammo.bullet_9mm
  repeater.AmmoTypes += Ammo.bullet_9mm_AP
  repeater.FireModes += new FireModeDefinition
  repeater.FireModes.head.AmmoTypeIndices += 0
  repeater.FireModes.head.AmmoTypeIndices += 1
  repeater.FireModes.head.AmmoSlotIndex = 0
  repeater.FireModes.head.Magazine = 20
  repeater.Tile = InventoryTile.Tile33

  val
  isp = ToolDefinition(ObjectClass.isp) //mag-scatter
  isp.Size = EquipmentSize.Pistol
  isp.AmmoTypes += Ammo.shotgun_shell
  isp.AmmoTypes += Ammo.shotgun_shell_AP
  isp.FireModes += new FireModeDefinition
  isp.FireModes.head.AmmoTypeIndices += 0
  isp.FireModes.head.AmmoTypeIndices += 1
  isp.FireModes.head.AmmoSlotIndex = 0
  isp.FireModes.head.Magazine = 8
  isp.Tile = InventoryTile.Tile33

  val
  beamer = ToolDefinition(ObjectClass.beamer)
  beamer.Size = EquipmentSize.Pistol
  beamer.AmmoTypes += Ammo.energy_cell
  beamer.FireModes += new FireModeDefinition
  beamer.FireModes.head.AmmoTypeIndices += 0
  beamer.FireModes.head.AmmoSlotIndex = 0
  beamer.FireModes.head.Magazine = 16
  beamer.FireModes += new FireModeDefinition
  beamer.FireModes(1).AmmoTypeIndices += 0
  beamer.FireModes(1).AmmoSlotIndex = 0
  beamer.FireModes(1).Magazine = 16
  beamer.Tile = InventoryTile.Tile33

  val
  ilc9 = ToolDefinition(ObjectClass.ilc9) //amp
  ilc9.Size = EquipmentSize.Pistol
  ilc9.AmmoTypes += Ammo.bullet_9mm
  ilc9.AmmoTypes += Ammo.bullet_9mm_AP
  ilc9.FireModes += new FireModeDefinition
  ilc9.FireModes.head.AmmoTypeIndices += 0
  ilc9.FireModes.head.AmmoTypeIndices += 1
  ilc9.FireModes.head.AmmoSlotIndex = 0
  ilc9.FireModes.head.Magazine = 30
  ilc9.Tile = InventoryTile.Tile33

  val
  suppressor = ToolDefinition(ObjectClass.suppressor)
  suppressor.Size = EquipmentSize.Rifle
  suppressor.AmmoTypes += Ammo.bullet_9mm
  suppressor.AmmoTypes += Ammo.bullet_9mm_AP
  suppressor.FireModes += new FireModeDefinition
  suppressor.FireModes.head.AmmoTypeIndices += 0
  suppressor.FireModes.head.AmmoTypeIndices += 1
  suppressor.FireModes.head.AmmoSlotIndex = 0
  suppressor.FireModes.head.Magazine = 25
  suppressor.Tile = InventoryTile.Tile63

  val
  punisher = ToolDefinition(ObjectClass.punisher)
  punisher.Size = EquipmentSize.Rifle
  punisher.AmmoTypes += Ammo.bullet_9mm
  punisher.AmmoTypes += Ammo.bullet_9mm_AP
  punisher.AmmoTypes += Ammo.rocket
  punisher.AmmoTypes += Ammo.frag_cartridge
  punisher.AmmoTypes += Ammo.jammer_cartridge
  punisher.AmmoTypes += Ammo.plasma_cartridge
  punisher.FireModes += new FireModeDefinition
  punisher.FireModes.head.AmmoTypeIndices += 0
  punisher.FireModes.head.AmmoTypeIndices += 1
  punisher.FireModes.head.AmmoSlotIndex = 0
  punisher.FireModes.head.Magazine = 30
  punisher.FireModes += new FireModeDefinition
  punisher.FireModes(1).AmmoTypeIndices += 2
  punisher.FireModes(1).AmmoTypeIndices += 3
  punisher.FireModes(1).AmmoTypeIndices += 4
  punisher.FireModes(1).AmmoTypeIndices += 5
  punisher.FireModes(1).AmmoSlotIndex = 1
  punisher.FireModes(1).Magazine = 1
  punisher.Tile = InventoryTile.Tile63

  val
  flechette = ToolDefinition(ObjectClass.flechette) //sweeper
  flechette.Size = EquipmentSize.Rifle
  flechette.AmmoTypes += Ammo.shotgun_shell
  flechette.AmmoTypes += Ammo.shotgun_shell_AP
  flechette.FireModes += new FireModeDefinition
  flechette.FireModes.head.AmmoTypeIndices += 0
  flechette.FireModes.head.AmmoTypeIndices += 1
  flechette.FireModes.head.AmmoSlotIndex = 0
  flechette.FireModes.head.Magazine = 12 //12 shells * 8 pellets = 96
  flechette.Tile = InventoryTile.Tile63

  val
  cycler = ToolDefinition(ObjectClass.cycler)
  cycler.Size = EquipmentSize.Rifle
  cycler.AmmoTypes += Ammo.bullet_9mm
  cycler.AmmoTypes += Ammo.bullet_9mm_AP
  cycler.FireModes += new FireModeDefinition
  cycler.FireModes.head.AmmoTypeIndices += 0
  cycler.FireModes.head.AmmoTypeIndices += 1
  cycler.FireModes.head.AmmoSlotIndex = 0
  cycler.FireModes.head.Magazine = 50
  cycler.Tile = InventoryTile.Tile63

  val
  gauss = ToolDefinition(ObjectClass.gauss)
  gauss.Size = EquipmentSize.Rifle
  gauss.AmmoTypes += Ammo.bullet_9mm
  gauss.AmmoTypes += Ammo.bullet_9mm_AP
  gauss.FireModes += new FireModeDefinition
  gauss.FireModes.head.AmmoTypeIndices += 0
  gauss.FireModes.head.AmmoTypeIndices += 1
  gauss.FireModes.head.AmmoSlotIndex = 0
  gauss.FireModes.head.Magazine = 30
  gauss.Tile = InventoryTile.Tile63

  val
  pulsar = ToolDefinition(ObjectClass.pulsar)
  pulsar.Size = EquipmentSize.Rifle
  pulsar.AmmoTypes += Ammo.energy_cell
  pulsar.FireModes += new FireModeDefinition
  pulsar.FireModes.head.AmmoTypeIndices += 0
  pulsar.FireModes.head.AmmoSlotIndex = 0
  pulsar.FireModes.head.Magazine = 40
  pulsar.FireModes += new FireModeDefinition
  pulsar.FireModes(1).AmmoTypeIndices += 0
  pulsar.FireModes(1).AmmoSlotIndex = 0
  pulsar.FireModes(1).Magazine = 40
  pulsar.Tile = InventoryTile.Tile63

  val
  anniversary_guna = ToolDefinition(ObjectClass.anniversary_guna) //tr stinger
  anniversary_guna.Size = EquipmentSize.Pistol
  anniversary_guna.AmmoTypes += Ammo.anniversary_ammo
  anniversary_guna.FireModes += new FireModeDefinition
  anniversary_guna.FireModes.head.AmmoTypeIndices += 0
  anniversary_guna.FireModes.head.AmmoSlotIndex = 0
  anniversary_guna.FireModes.head.Magazine = 6
  anniversary_guna.FireModes += new FireModeDefinition
  anniversary_guna.FireModes(1).AmmoTypeIndices += 0
  anniversary_guna.FireModes(1).AmmoSlotIndex = 0
  anniversary_guna.FireModes(1).Magazine = 6
  anniversary_guna.FireModes(1).Chamber = 6
  anniversary_guna.Tile = InventoryTile.Tile33

  val
  anniversary_gun = ToolDefinition(ObjectClass.anniversary_gun) //nc spear
  anniversary_gun.Size = EquipmentSize.Pistol
  anniversary_gun.AmmoTypes += Ammo.anniversary_ammo
  anniversary_gun.FireModes += new FireModeDefinition
  anniversary_gun.FireModes.head.AmmoTypeIndices += 0
  anniversary_gun.FireModes.head.AmmoSlotIndex = 0
  anniversary_gun.FireModes.head.Magazine = 6
  anniversary_gun.FireModes += new FireModeDefinition
  anniversary_gun.FireModes(1).AmmoTypeIndices += 0
  anniversary_gun.FireModes(1).AmmoSlotIndex = 0
  anniversary_gun.FireModes(1).Magazine = 6
  anniversary_gun.FireModes(1).Chamber = 6
  anniversary_gun.Tile = InventoryTile.Tile33

  val
  anniversary_gunb = ToolDefinition(ObjectClass.anniversary_gunb) //vs eraser
  anniversary_gunb.Size = EquipmentSize.Pistol
  anniversary_gunb.AmmoTypes += Ammo.anniversary_ammo
  anniversary_gunb.FireModes += new FireModeDefinition
  anniversary_gunb.FireModes.head.AmmoTypeIndices += 0
  anniversary_gunb.FireModes.head.AmmoSlotIndex = 0
  anniversary_gunb.FireModes.head.Magazine = 6
  anniversary_gunb.FireModes += new FireModeDefinition
  anniversary_gunb.FireModes(1).AmmoTypeIndices += 0
  anniversary_gunb.FireModes(1).AmmoSlotIndex = 0
  anniversary_gunb.FireModes(1).Magazine = 6
  anniversary_gunb.FireModes(1).Chamber = 6
  anniversary_gunb.Tile = InventoryTile.Tile33

  val
  spiker = ToolDefinition(ObjectClass.spiker)
  spiker.Size = EquipmentSize.Pistol
  spiker.AmmoTypes += Ammo.ancient_ammo_combo
  spiker.FireModes += new FireModeDefinition
  spiker.FireModes.head.AmmoTypeIndices += 0
  spiker.FireModes.head.AmmoSlotIndex = 0
  spiker.FireModes.head.Magazine = 25
  spiker.Tile = InventoryTile.Tile33

  val
  mini_chaingun = ToolDefinition(ObjectClass.mini_chaingun)
  mini_chaingun.Size = EquipmentSize.Rifle
  mini_chaingun.AmmoTypes += Ammo.bullet_9mm
  mini_chaingun.AmmoTypes += Ammo.bullet_9mm_AP
  mini_chaingun.FireModes += new FireModeDefinition
  mini_chaingun.FireModes.head.AmmoTypeIndices += 0
  mini_chaingun.FireModes.head.AmmoTypeIndices += 1
  mini_chaingun.FireModes.head.AmmoSlotIndex = 0
  mini_chaingun.FireModes.head.Magazine = 100
  mini_chaingun.Tile = InventoryTile.Tile93

  val
  r_shotgun = ToolDefinition(ObjectClass.r_shotgun) //jackhammer
  r_shotgun.Size = EquipmentSize.Rifle
  r_shotgun.AmmoTypes += Ammo.shotgun_shell
  r_shotgun.AmmoTypes += Ammo.shotgun_shell_AP
  r_shotgun.FireModes += new FireModeDefinition
  r_shotgun.FireModes.head.AmmoTypeIndices += 0
  r_shotgun.FireModes.head.AmmoTypeIndices += 1
  r_shotgun.FireModes.head.AmmoSlotIndex = 0
  r_shotgun.FireModes.head.Magazine = 16 //16 shells * 8 pellets = 128
  r_shotgun.FireModes += new FireModeDefinition
  r_shotgun.FireModes(1).AmmoTypeIndices += 0
  r_shotgun.FireModes(1).AmmoTypeIndices += 1
  r_shotgun.FireModes(1).AmmoSlotIndex = 0
  r_shotgun.FireModes(1).Magazine = 16 //16 shells * 8 pellets = 128
  r_shotgun.FireModes(1).Chamber = 3
  r_shotgun.Tile = InventoryTile.Tile93

  val
  lasher = ToolDefinition(ObjectClass.lasher)
  lasher.Size = EquipmentSize.Rifle
  lasher.AmmoTypes += Ammo.energy_cell
  lasher.FireModes += new FireModeDefinition
  lasher.FireModes.head.AmmoTypeIndices += 0
  lasher.FireModes.head.AmmoSlotIndex = 0
  lasher.FireModes.head.Magazine = 35
  lasher.FireModes += new FireModeDefinition
  lasher.FireModes(1).AmmoTypeIndices += 0
  lasher.FireModes(1).AmmoSlotIndex = 0
  lasher.FireModes(1).Magazine = 35
  lasher.Tile = InventoryTile.Tile93

  val
  maelstrom = ToolDefinition(ObjectClass.maelstrom)
  maelstrom.Size = EquipmentSize.Rifle
  maelstrom.AmmoTypes += Ammo.maelstrom_ammo
  maelstrom.FireModes += new FireModeDefinition
  maelstrom.FireModes.head.AmmoTypeIndices += 0
  maelstrom.FireModes.head.AmmoSlotIndex = 0
  maelstrom.FireModes.head.Magazine = 150
  maelstrom.FireModes += new FireModeDefinition
  maelstrom.FireModes(1).AmmoTypeIndices += 0
  maelstrom.FireModes(1).AmmoSlotIndex = 0
  maelstrom.FireModes(1).Magazine = 150
  maelstrom.FireModes += new FireModeDefinition
  maelstrom.FireModes(2).AmmoTypeIndices += 0
  maelstrom.FireModes(2).AmmoSlotIndex = 0
  maelstrom.FireModes(2).Magazine = 150
  maelstrom.Tile = InventoryTile.Tile93

  val
  phoenix = ToolDefinition(ObjectClass.phoenix) //decimator
  phoenix.Size = EquipmentSize.Rifle
  phoenix.AmmoTypes += Ammo.phoenix_missile
  phoenix.FireModes += new FireModeDefinition
  phoenix.FireModes.head.AmmoTypeIndices += 0
  phoenix.FireModes.head.AmmoSlotIndex = 0
  phoenix.FireModes.head.Magazine = 3
  phoenix.FireModes += new FireModeDefinition
  phoenix.FireModes(1).AmmoTypeIndices += 0
  phoenix.FireModes(1).AmmoSlotIndex = 0
  phoenix.FireModes(1).Magazine = 3
  phoenix.Tile = InventoryTile.Tile93

  val
  striker = ToolDefinition(ObjectClass.striker)
  striker.Size = EquipmentSize.Rifle
  striker.AmmoTypes += Ammo.striker_missile_ammo
  striker.FireModes += new FireModeDefinition
  striker.FireModes.head.AmmoTypeIndices += 0
  striker.FireModes.head.AmmoSlotIndex = 0
  striker.FireModes.head.Magazine = 5
  striker.FireModes += new FireModeDefinition
  striker.FireModes(1).AmmoTypeIndices += 0
  striker.FireModes(1).AmmoSlotIndex = 0
  striker.FireModes(1).Magazine = 5
  striker.Tile = InventoryTile.Tile93

  val
  hunterseeker = ToolDefinition(ObjectClass.hunterseeker) //phoenix
  hunterseeker.Size = EquipmentSize.Rifle
  hunterseeker.AmmoTypes += Ammo.hunter_seeker_missile
  hunterseeker.FireModes += new FireModeDefinition
  hunterseeker.FireModes.head.AmmoTypeIndices += 0
  hunterseeker.FireModes.head.AmmoSlotIndex = 0
  hunterseeker.FireModes.head.Magazine = 1
  hunterseeker.FireModes += new FireModeDefinition
  hunterseeker.FireModes(1).AmmoTypeIndices += 0
  hunterseeker.FireModes(1).AmmoSlotIndex = 0
  hunterseeker.FireModes(1).Magazine = 1
  hunterseeker.Tile = InventoryTile.Tile93

  val
  lancer = ToolDefinition(ObjectClass.lancer)
  lancer.Size = EquipmentSize.Rifle
  lancer.AmmoTypes += Ammo.lancer_cartridge
  lancer.FireModes += new FireModeDefinition
  lancer.FireModes.head.AmmoTypeIndices += 0
  lancer.FireModes.head.AmmoSlotIndex = 0
  lancer.FireModes.head.Magazine = 6
  lancer.Tile = InventoryTile.Tile93

  val
  rocklet = ToolDefinition(ObjectClass.rocklet)
  rocklet.Size = EquipmentSize.Rifle
  rocklet.AmmoTypes += Ammo.rocket
  rocklet.AmmoTypes += Ammo.frag_cartridge
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
  rocklet.FireModes(1).Chamber = 6
  rocklet.Tile = InventoryTile.Tile63

  val
  thumper = ToolDefinition(ObjectClass.thumper)
  thumper.Size = EquipmentSize.Rifle
  thumper.AmmoTypes += Ammo.frag_cartridge
  thumper.AmmoTypes += Ammo.plasma_cartridge
  thumper.AmmoTypes += Ammo.jammer_cartridge
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

  val
  radiator = ToolDefinition(ObjectClass.radiator)
  radiator.Size = EquipmentSize.Rifle
  radiator.AmmoTypes += Ammo.ancient_ammo_combo
  radiator.FireModes += new FireModeDefinition
  radiator.FireModes.head.AmmoTypeIndices += 0
  radiator.FireModes.head.AmmoSlotIndex = 0
  radiator.FireModes.head.Magazine = 25
  radiator.FireModes += new FireModeDefinition
  radiator.FireModes(1).AmmoTypeIndices += 0
  radiator.FireModes(1).AmmoSlotIndex = 0
  radiator.FireModes(1).Magazine = 25
  radiator.Tile = InventoryTile.Tile63

  val
  heavy_sniper = ToolDefinition(ObjectClass.heavy_sniper) //hsr
  heavy_sniper.Size = EquipmentSize.Rifle
  heavy_sniper.AmmoTypes += Ammo.bolt
  heavy_sniper.FireModes += new FireModeDefinition
  heavy_sniper.FireModes.head.AmmoTypeIndices += 0
  heavy_sniper.FireModes.head.AmmoSlotIndex = 0
  heavy_sniper.FireModes.head.Magazine = 10
  heavy_sniper.Tile = InventoryTile.Tile93

  val
  bolt_driver = ToolDefinition(ObjectClass.bolt_driver)
  bolt_driver.Size = EquipmentSize.Rifle
  bolt_driver.AmmoTypes += Ammo.bolt
  bolt_driver.FireModes += new FireModeDefinition
  bolt_driver.FireModes.head.AmmoTypeIndices += 0
  bolt_driver.FireModes.head.AmmoSlotIndex = 0
  bolt_driver.FireModes.head.Magazine = 1
  bolt_driver.Tile = InventoryTile.Tile93

  val
  oicw = ToolDefinition(ObjectClass.oicw) //scorpion
  oicw.Size = EquipmentSize.Rifle
  oicw.AmmoTypes += Ammo.oicw_ammo
  oicw.FireModes += new FireModeDefinition
  oicw.FireModes.head.AmmoTypeIndices += 0
  oicw.FireModes.head.AmmoSlotIndex = 0
  oicw.FireModes.head.Magazine = 1
  oicw.FireModes += new FireModeDefinition
  oicw.FireModes(1).AmmoTypeIndices += 0
  oicw.FireModes(1).AmmoSlotIndex = 0
  oicw.FireModes(1).Magazine = 1
  oicw.Tile = InventoryTile.Tile93

  val
  flamethrower = ToolDefinition(ObjectClass.flamethrower)
  flamethrower.Size = EquipmentSize.Rifle
  flamethrower.AmmoTypes += Ammo.flamethrower_ammo
  flamethrower.FireModes += new FireModeDefinition
  flamethrower.FireModes.head.AmmoTypeIndices += 0
  flamethrower.FireModes.head.AmmoSlotIndex = 0
  flamethrower.FireModes.head.Magazine = 100
  flamethrower.FireModes.head.Chamber = 5
  flamethrower.FireModes += new FireModeDefinition
  flamethrower.FireModes(1).AmmoTypeIndices += 0
  flamethrower.FireModes(1).AmmoSlotIndex = 0
  flamethrower.FireModes(1).Magazine = 100
  flamethrower.FireModes(1).Chamber = 50
  flamethrower.Tile = InventoryTile.Tile63

  val
  medicalapplicator = ToolDefinition(ObjectClass.medicalapplicator)
  medicalapplicator.Size = EquipmentSize.Pistol
  medicalapplicator.AmmoTypes += Ammo.health_canister
  medicalapplicator.FireModes += new FireModeDefinition
  medicalapplicator.FireModes.head.AmmoTypeIndices += 0
  medicalapplicator.FireModes.head.AmmoSlotIndex = 0
  medicalapplicator.FireModes.head.Magazine = 100
  medicalapplicator.FireModes += new FireModeDefinition
  medicalapplicator.FireModes(1).AmmoTypeIndices += 0
  medicalapplicator.FireModes(1).AmmoSlotIndex = 0
  medicalapplicator.FireModes(1).Magazine = 100
  medicalapplicator.Tile = InventoryTile.Tile33

  val
  nano_dispenser = ToolDefinition(ObjectClass.nano_dispenser)
  nano_dispenser.Size = EquipmentSize.Rifle
  nano_dispenser.AmmoTypes += Ammo.armor_canister
  nano_dispenser.AmmoTypes += Ammo.upgrade_canister
  nano_dispenser.FireModes += new FireModeDefinition
  nano_dispenser.FireModes.head.AmmoTypeIndices += 0
  nano_dispenser.FireModes.head.AmmoTypeIndices += 1
  nano_dispenser.FireModes.head.AmmoSlotIndex = 0
  nano_dispenser.FireModes.head.Magazine = 100
  nano_dispenser.Tile = InventoryTile.Tile63

  val
  bank = ToolDefinition(ObjectClass.bank)
  bank.Size = EquipmentSize.Pistol
  bank.AmmoTypes += Ammo.armor_canister
  bank.FireModes += new FireModeDefinition
  bank.FireModes.head.AmmoTypeIndices += 0
  bank.FireModes.head.AmmoSlotIndex = 0
  bank.FireModes.head.Magazine = 100
  bank.FireModes += new FireModeDefinition
  bank.FireModes(1).AmmoTypeIndices += 0
  bank.FireModes(1).AmmoSlotIndex = 0
  bank.FireModes(1).Magazine = 100
  bank.Tile = InventoryTile.Tile33

  val
  remote_electronics_kit = SimpleItemDefinition(SItem.remote_electronics_kit)
  remote_electronics_kit.Packet = new REKConverter
  remote_electronics_kit.Tile = InventoryTile.Tile33

  val
  trek = ToolDefinition(ObjectClass.trek)
  trek.Size = EquipmentSize.Pistol
  trek.AmmoTypes += Ammo.trek_ammo
  trek.FireModes += new FireModeDefinition
  trek.FireModes.head.AmmoTypeIndices += 0
  trek.FireModes.head.AmmoSlotIndex = 0
  trek.FireModes.head.Magazine = 4
  trek.FireModes += new FireModeDefinition
  trek.FireModes(1).AmmoTypeIndices += 0
  trek.FireModes(1).AmmoSlotIndex = 0
  trek.FireModes(1).Magazine = 0
  trek.Tile = InventoryTile.Tile33

  val
  flail_targeting_laser = SimpleItemDefinition(SItem.flail_targeting_laser)
  flail_targeting_laser.Packet = new CommandDetonaterConverter
  flail_targeting_laser.Tile = InventoryTile.Tile33

  val
  command_detonater = SimpleItemDefinition(SItem.command_detonater)
  command_detonater.Packet = new CommandDetonaterConverter
  command_detonater.Tile = InventoryTile.Tile33

  val
  ace = ConstructionItemDefinition(CItem.Unit.ace)
  ace.Modes += DeployedItem.boomer
  ace.Modes += DeployedItem.he_mine
  ace.Modes += DeployedItem.jammer_mine
  ace.Modes += DeployedItem.spitfire_turret
  ace.Modes += DeployedItem.spitfire_cloaked
  ace.Modes += DeployedItem.spitfire_aa
  ace.Modes += DeployedItem.motionalarmsensor
  ace.Modes += DeployedItem.sensor_shield
  ace.Tile = InventoryTile.Tile33

  val
  advanced_ace = ConstructionItemDefinition(CItem.Unit.advanced_ace)
  advanced_ace.Modes += DeployedItem.tank_traps
  advanced_ace.Modes += DeployedItem.portable_manned_turret
  advanced_ace.Modes += DeployedItem.deployable_shield_generator
  advanced_ace.Tile = InventoryTile.Tile63

  val
  fury_weapon_systema = ToolDefinition(ObjectClass.fury_weapon_systema)
  fury_weapon_systema.Size = EquipmentSize.VehicleWeapon
  fury_weapon_systema.AmmoTypes += Ammo.hellfire_ammo
  fury_weapon_systema.FireModes += new FireModeDefinition
  fury_weapon_systema.FireModes.head.AmmoTypeIndices += 0
  fury_weapon_systema.FireModes.head.AmmoSlotIndex = 0
  fury_weapon_systema.FireModes.head.Magazine = 2

  val
  fury = VehicleDefinition(ObjectClass.fury)
  fury.Seats += 0 -> new SeatDefinition()
  fury.Seats(0).Bailable = true
  fury.Seats(0).ControlledWeapon = Some(1)
  fury.MountPoints += 0 -> 0
  fury.MountPoints += 2 -> 0
  fury.Weapons += 1 -> fury_weapon_systema
  fury.TrunkSize = InventoryTile(11, 11)
  fury.TrunkOffset = 30

  val
  order_terminal = new OrderTerminalDefinition
  val
  cert_terminal = new CertTerminalDefinition

  val
  external_lock = new IFFLockDefinition
  val
  door = new DoorDefinition
}
