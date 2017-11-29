// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects._
import net.psforever.objects.definition._
import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.types.ExoSuitType

/**
  * The basic definition for any `Terminal`.
  * @param objectId the object's identifier number
  */
abstract class TerminalDefinition(objectId : Int) extends ObjectDefinition(objectId) {
  private[this] val log = org.log4s.getLogger("TerminalDefinition")
  Name = "terminal"

  /**
    * The unimplemented functionality for this `Terminal`'s `TransactionType.Buy` and `TransactionType.Learn` activity.
    */
  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange

  /**
    * The unimplemented functionality for this `Terminal`'s `TransactionType.Sell` activity.
    */
  def Sell(player: Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()

  /**
    * The unimplemented functionality for this `Terminal`'s `TransactionType.InfantryLoadout` activity.
    */
  def Loadout(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()

  /**
    * A `Map` of information for changing exo-suits.
    * key - an identification string sent by the client
    * value - a `Tuple` containing exo-suit specifications
    */
  protected val suits : Map[String, (ExoSuitType.Value, Int)] = Map(
    "standard_issue_armor" -> (ExoSuitType.Standard, 0),
    "lite_armor" -> (ExoSuitType.Agile, 0),
    "med_armor" -> (ExoSuitType.Reinforced, 0)
    //TODO max and infiltration suit
  )

  import net.psforever.objects.GlobalDefinitions._
  /**
    * A `Map` of operations for producing the `AmmoBox` `Equipment` for infantry-held weaponry.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  protected val infantryAmmunition : Map[String, ()=>Equipment] = Map(
    "9mmbullet" -> MakeAmmoBox(bullet_9mm),
    "9mmbullet_AP" -> MakeAmmoBox(bullet_9mm_AP),
    "shotgun_shell" -> MakeAmmoBox(shotgun_shell),
    "shotgun_shell_AP" -> MakeAmmoBox(shotgun_shell_AP),
    "energy_cell" -> MakeAmmoBox(energy_cell),
    "anniversary_ammo" -> MakeAmmoBox(anniversary_ammo), //10mm multi-phase
    "rocket" -> MakeAmmoBox(rocket),
    "frag_cartridge" -> MakeAmmoBox(frag_cartridge),
    "jammer_cartridge" -> MakeAmmoBox(jammer_cartridge),
    "plasma_cartridge" -> MakeAmmoBox(plasma_cartridge),
    "ancient_ammo_combo" -> MakeAmmoBox(ancient_ammo_combo),
    "maelstrom_ammo" -> MakeAmmoBox(maelstrom_ammo),
    "striker_missile_ammo" -> MakeAmmoBox(striker_missile_ammo),
    "hunter_seeker_missile" -> MakeAmmoBox(hunter_seeker_missile), //phoenix missile
    "lancer_cartridge" -> MakeAmmoBox(lancer_cartridge),
    "bolt" -> MakeAmmoBox(bolt),
    "oicw_ammo" -> MakeAmmoBox(oicw_ammo), //scorpion missile
    "flamethrower_ammo" -> MakeAmmoBox(flamethrower_ammo)
  )

  /**
    * A `Map` of operations for producing the `AmmoBox` `Equipment` for infantry-held utilities.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  protected val supportAmmunition : Map[String, ()=>Equipment] = Map(
    "health_canister" -> MakeAmmoBox(health_canister),
    "armor_canister" -> MakeAmmoBox(armor_canister),
    "upgrade_canister" -> MakeAmmoBox(upgrade_canister)
  )

  /**
    * A `Map` of operations for producing the `AmmoBox` `Equipment` for vehicle-mounted weaponry.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  protected val vehicleAmmunition : Map[String, ()=>Equipment] = Map(
    "35mmbullet" -> MakeAmmoBox(bullet_35mm),
    "hellfire_ammo" -> MakeAmmoBox(hellfire_ammo),
    "liberator_bomb" -> MakeAmmoBox(liberator_bomb),
    "25mmbullet" -> MakeAmmoBox(bullet_25mm),
    "75mmbullet" -> MakeAmmoBox(bullet_75mm),
    "heavy_grenade_mortar" -> MakeAmmoBox(heavy_grenade_mortar),
    "reaver_rocket" -> MakeAmmoBox(reaver_rocket),
    "20mmbullet" -> MakeAmmoBox(bullet_20mm),
    "12mmbullet" -> MakeAmmoBox(bullet_12mm),
    "wasp_rocket_ammo" -> MakeAmmoBox(wasp_rocket_ammo),
    "wasp_gun_ammo" -> MakeAmmoBox(wasp_gun_ammo),
    "aphelion_laser_ammo" -> MakeAmmoBox(aphelion_laser_ammo),
    "aphelion_immolation_cannon_ammo" -> MakeAmmoBox(aphelion_immolation_cannon_ammo),
    "aphelion_plasma_rocket_ammo" -> MakeAmmoBox(aphelion_plasma_rocket_ammo),
    "aphelion_ppa_ammo" -> MakeAmmoBox(aphelion_ppa_ammo),
    "aphelion_starfire_ammo" -> MakeAmmoBox(aphelion_starfire_ammo),
    "skyguard_flak_cannon_ammo" -> MakeAmmoBox(skyguard_flak_cannon_ammo),
    "flux_cannon_thresher_battery" -> MakeAmmoBox(flux_cannon_thresher_battery),
    "fluxpod_ammo" -> MakeAmmoBox(fluxpod_ammo),
    "pulse_battery" -> MakeAmmoBox(pulse_battery),
    "heavy_rail_beam_battery" -> MakeAmmoBox(heavy_rail_beam_battery),
    "15mmbullet" -> MakeAmmoBox(bullet_15mm),
    "colossus_100mm_cannon_ammo" -> MakeAmmoBox(colossus_100mm_cannon_ammo),
    "colossus_burster_ammo" -> MakeAmmoBox(colossus_burster_ammo),
    "colossus_cluster_bomb_ammo" -> MakeAmmoBox(colossus_cluster_bomb_ammo),
    "colossus_chaingun_ammo" -> MakeAmmoBox(colossus_chaingun_ammo),
    "colossus_tank_cannon_ammo" -> MakeAmmoBox(colossus_tank_cannon_ammo),
    "105mmbullet" -> MakeAmmoBox(bullet_105mm),
    "gauss_cannon_ammo" -> MakeAmmoBox(gauss_cannon_ammo),
    "peregrine_dual_machine_gun_ammo" -> MakeAmmoBox(peregrine_dual_machine_gun_ammo),
    "peregrine_mechhammer_ammo" -> MakeAmmoBox(peregrine_mechhammer_ammo),
    "peregrine_particle_cannon_ammo" -> MakeAmmoBox(peregrine_particle_cannon_ammo),
    "peregrine_rocket_pod_ammo" -> MakeAmmoBox(peregrine_rocket_pod_ammo),
    "peregrine_sparrow_ammo" -> MakeAmmoBox(peregrine_sparrow_ammo),
    "150mmbullet" -> MakeAmmoBox(bullet_150mm)
  )

  /**
    * A `Map` of operations for producing the `Tool` `Equipment` for infantry weapons.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  protected val infantryWeapons : Map[String, ()=>Equipment] = Map(
    "ilc9" -> MakeTool(ilc9),
    "repeater" -> MakeTool(repeater),
    "isp" -> MakeTool(isp), //amp
    "beamer" -> MakeTool(beamer),
    "suppressor" -> MakeTool(suppressor),
    "anniversary_guna" -> MakeTool(anniversary_guna), //tr stinger
    "anniversary_gun" -> MakeTool(anniversary_gun), //nc spear
    "anniversary_gunb" -> MakeTool(anniversary_gunb), //vs eraser
    "cycler" -> MakeTool(cycler),
    "gauss" -> MakeTool(gauss),
    "pulsar" -> MakeTool(pulsar),
    "punisher" -> MakeTool(punisher),
    "flechette" -> MakeTool(flechette),
    "spiker" -> MakeTool(spiker),
    "frag_grenade" -> MakeTool(frag_grenade),
    "jammer_grenade" -> MakeTool(jammer_grenade),
    "plasma_grenade" -> MakeTool(plasma_grenade),
    "katana" -> MakeTool(katana),
    "chainblade" -> MakeTool(chainblade),
    "magcutter" -> MakeTool(magcutter),
    "forceblade" -> MakeTool(forceblade),
    "mini_chaingun" -> MakeTool(mini_chaingun),
    "r_shotgun" -> MakeTool(r_shotgun), //jackhammer
    "lasher" -> MakeTool(lasher),
    "maelstrom" -> MakeTool(maelstrom),
    "striker" -> MakeTool(striker),
    "hunterseeker" -> MakeTool(hunterseeker), //phoenix
    "lancer" -> MakeTool(lancer),
    "phoenix" -> MakeTool(phoenix), //decimator
    "rocklet" -> MakeTool(rocklet),
    "thumper" -> MakeTool(thumper),
    "radiator" -> MakeTool(radiator),
    "heavy_sniper" -> MakeTool(heavy_sniper), //hsr
    "bolt_driver" -> MakeTool(bolt_driver),
    "oicw" -> MakeTool(oicw), //scorpion
    "flamethrower" -> MakeTool(flamethrower)
  )

  /**
    * A `Map` of operations for producing the `Tool` `Equipment` for utilities.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  protected val supportWeapons : Map[String, ()=>Equipment] = Map(
    "medkit" -> MakeKit(medkit),
    "super_medkit" -> MakeKit(super_medkit),
    "super_armorkit" -> MakeKit(super_armorkit),
    "super_staminakit" -> MakeKit(super_staminakit),
    "medicalapplicator" -> MakeTool(medicalapplicator),
    "bank" -> MakeTool(bank, armor_canister),
    "nano_dispenser" -> MakeTool(nano_dispenser),
    //TODO "ace" -> MakeConstructionItem(ace),
    //TODO "advanced_ace" -> MakeConstructionItem(advanced_ace),
    "remote_electronics_kit" -> MakeSimpleItem(remote_electronics_kit),
    "trek" -> MakeTool(trek),
    "command_detonater" -> MakeSimpleItem(command_detonater),
    "flail_targeting_laser" -> MakeSimpleItem(flail_targeting_laser)
  )

  /**
    * A `Map` of operations for producing a ground-based `Vehicle`.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  protected val groundVehicles : Map[String, ()=>Vehicle] = Map(
    "quadassault" -> MakeVehicle(quadassault),
    "fury" -> MakeVehicle(fury),
    "quadstealth" -> MakeVehicle(quadstealth),
    "ant" -> MakeVehicle(ant),
    "ams" -> MakeVehicle(ams),
    "mediumtransport" -> MakeVehicle(mediumtransport),
    "two_man_assault_buggy" -> MakeVehicle(two_man_assault_buggy),
    "skyguard" -> MakeVehicle(skyguard),
    "lightning" -> MakeVehicle(lightning),
    "threemanheavybuggy" -> MakeVehicle(threemanheavybuggy),
    "battlewagon" -> MakeVehicle(battlewagon),
    "apc_tr" -> MakeVehicle(apc_tr),
    "prowler" -> MakeVehicle(prowler),
    "twomanheavybuggy" -> MakeVehicle(twomanheavybuggy),
    "thunderer" -> MakeVehicle(thunderer),
    "apc_nc" -> MakeVehicle(apc_nc),
    "vanguard" -> MakeVehicle(vanguard),
    "twomanhoverbuggy" -> MakeVehicle(twomanhoverbuggy),
    "aurora" -> MakeVehicle(aurora),
    "apc_vs" -> MakeVehicle(apc_vs),
    "magrider" -> MakeVehicle(magrider),
    "flail" -> MakeVehicle(flail),
    "switchblade" -> MakeVehicle(switchblade),
    "router" -> MakeVehicle(router)
  )

  /**
    * A `Map` of operations for producing most flight-based `Vehicle`.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  protected val flight1Vehicles : Map[String, ()=>Vehicle] = Map(
    "mosquito" -> MakeVehicle(mosquito),
    "lightgunship" -> MakeVehicle(lightgunship),
    "wasp" -> MakeVehicle(wasp),
    "phantasm" -> MakeVehicle(phantasm),
    "vulture" -> MakeVehicle(vulture),
    "liberator" -> MakeVehicle(liberator)
  )

  /**
    * A `Map` of operations for producing a flight-based `Vehicle` specific to the dropship terminal.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  protected val flight2Vehicles : Map[String, ()=>Vehicle] = Map(
    "dropship" -> MakeVehicle(dropship),
    "galaxy_gunship" -> MakeVehicle(galaxy_gunship),
    "lodestar" -> MakeVehicle(lodestar)
  )

  /**
    * A `Map` of operations for producing a ground-based `Vehicle` specific to the bfr terminal.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  protected val bfrVehicles : Map[String, ()=>Vehicle] = Map(
//    "colossus_gunner" -> (()=>Unit),
//    "colossus_flight" -> (()=>Unit),
//    "peregrine_gunner" -> (()=>Unit),
//    "peregrine_flight" -> (()=>Unit),
//    "aphelion_gunner" -> (()=>Unit),
//    "aphelion_flight" -> (()=>Unit)
  )

  /**
    * Create a new `Tool` from provided `EquipmentDefinition` objects.
    * @param tdef the `ToolDefinition` object
    * @return a partial function that, when called, creates the piece of `Equipment`
    */
  protected def MakeTool(tdef : ToolDefinition)() : Tool = MakeTool(tdef, Nil)

  /**
    * Create a new `Tool` from provided `EquipmentDefinition` objects.
    * @param tdef the `ToolDefinition` object
    * @param adef an `AmmoBoxDefinition` object
    * @return a partial function that, when called, creates the piece of `Equipment`
    */
  protected def MakeTool(tdef : ToolDefinition, adef : AmmoBoxDefinition)() : Tool = MakeTool(tdef, List(adef))

  /**
    * Create a new `Tool` from provided `EquipmentDefinition` objects.
    * Only use this function to create default `Tools` with the default parameters.
    * For example, loadouts can retain `Tool` information that utilizes alternate, valid ammunition types;
    * and, this method function will not construct a complete object if provided that information.
    * @param tdef the `ToolDefinition` object
    * @param adefs a `List` of `AmmoBoxDefinition` objects
    * @return a curried function that, when called, creates the piece of `Equipment`
    * @see `GlobalDefinitions`
    * @see `OrderTerminalDefinition.BuildSimplifiedPattern`
    */
  protected def MakeTool(tdef : ToolDefinition, adefs : List[AmmoBoxDefinition])() : Tool = {
    val obj = Tool(tdef)
    adefs match {
      case _ :: _ =>
        LoadAmmunitionIntoWeapon(obj, adefs)
      case Nil => ; //as-is
    }
    obj
  }

  /**
    * Given a weapon, and custom ammunition profiles, attempt to load those boxes of ammunition into the weapon.<br>
    * <br>
    * This is a customization function that should normally go unused.
    * All of the information necessary to generate a `Tool` from a `Terminal` request should be available on the `ToolDefinition` object.
    * The ammunition information, regardless of "customization,"  must satisfy the type limits of the original definition.
    * As thus, to introduce very strange ammunition to a give `Tool`,
    * either the definition must be modified or a different definition must be used.
    * The custom ammunition is organized into order of ammunition slots based on the `FireModeDefinition` objects.
    * That is:
    * the first custom element is processed by the first ammunition slot;
    * the second custom element is processed by the second ammunition slot; and, so forth.
    * @param weapon the `Tool` object
    * @param adefs a sequential `List` of ammunition to be loaded into weapon
    * @see `AmmoBoxDefinition`
    * @see `FireModeDefinition`
    */
  private def LoadAmmunitionIntoWeapon(weapon : Tool, adefs : List[AmmoBoxDefinition]) : Unit = {
    val definition = weapon.Definition
    (0 until math.min(weapon.MaxAmmoSlot, adefs.length)).foreach(index => {
      val ammoSlot = weapon.AmmoSlots(index)
      adefs.lift(index) match {
        case Some(aType) =>
          ammoSlot.AllAmmoTypes.indexOf(aType.AmmoType) match {
            case -1 =>
              log.warn(s"terminal plans do not match definition: can not feed ${aType.AmmoType} ammunition into Tool (${definition.ObjectId} @ ammo $index)")
            case n =>
              ammoSlot.AmmoTypeIndex = n
              ammoSlot.Box = MakeAmmoBox(aType, Some(definition.FireModes(index).Magazine)) //make new internal magazine, full
          }
        case None => ;
      }
    })
  }

  /**
    * Create a new `AmmoBox` from provided `EquipmentDefinition` objects.
    * @param adef the `AmmoBoxDefinition` object
    * @param capacity optional number of rounds in this `AmmoBox`, deviating from the `EquipmentDefinition`;
    *                 necessary for constructing the magazine (`AmmoSlot`) of `Tool`s
    * @return a curried function that, when called, creates the piece of `Equipment`
    * @see `GlobalDefinitions`
    */
  protected def MakeAmmoBox(adef : AmmoBoxDefinition, capacity : Option[Int] = None)() : AmmoBox = {
    capacity match {
      case Some(cap) =>
        AmmoBox(adef, cap)
      case None =>
        AmmoBox(adef)
    }
  }

  /**
    * Create a new `Kit` from provided `EquipmentDefinition` objects.
    * @param kdef the `KitDefinition` object
    * @return a curried function that, when called, creates the piece of `Equipment`
    * @see `GlobalDefinitions`
    */
  protected def MakeKit(kdef : KitDefinition)() : Kit = Kit(kdef)

  /**
    * Create a new `SimpleItem` from provided `EquipmentDefinition` objects.
    * @param sdef the `SimpleItemDefinition` object
    * @return a curried function that, when called, creates the piece of `Equipment`
    * @see `GlobalDefinitions`
    */
  protected def MakeSimpleItem(sdef : SimpleItemDefinition)() : SimpleItem = SimpleItem(sdef)

  /**
    * Create a new `ConstructionItem` from provided `EquipmentDefinition` objects.
    * @param cdef the `ConstructionItemDefinition` object
    * @return a curried function that, when called, creates the piece of `Equipment`
    * @see `GlobalDefinitions`
    */
  protected def MakeConstructionItem(cdef : ConstructionItemDefinition)() : ConstructionItem = ConstructionItem(cdef)

  /**
    * Create a new `Vehicle` from provided `VehicleDefinition` objects.
    * @param vdef the `VehicleDefinition` object
    * @return a curried function that, when called, creates the `Vehicle`
    * @see `GlobalDefinitions`
    */
  protected def MakeVehicle(vdef : VehicleDefinition)() : Vehicle = Vehicle(vdef)
}
