// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects._
import net.psforever.objects.definition._
import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.types.ExoSuitType

/**
  * The definition for any `Terminal`.
  * @param objectId the object's identifier number
  */
abstract class TerminalDefinition(objectId : Int) extends ObjectDefinition(objectId) {
  Name = "terminal"

  /**
    * The unimplemented functionality for this `Terminal`'s `TransactionType.Buy` and `TransactionType.Learn` activity.
    */
  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange

  /**
    * The unimplemented functionality for this `Terminal`'s `TransactionType.Sell` activity.
    */
  def Sell(player: Player, msg : ItemTransactionMessage) : Terminal.Exchange

  /**
    * The unimplemented functionality for this `Terminal`'s `TransactionType.InfantryLoadout` activity.
    */
  def Loadout(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange

  /**
    * A `Map` of information for changing exo-suits.
    * key - an identification string sent by the client
    * value - a `Tuple` containing exo-suit specifications
    */
  protected val suits : Map[String, (ExoSuitType.Value, Int)] = Map(
    "standard_issue_armor" -> (ExoSuitType.Standard, 0),
    "lite_armor" -> (ExoSuitType.Agile, 0),
    "med_armor" -> (ExoSuitType.Reinforced, 0),
    "stealth_armor" -> (ExoSuitType.Infiltration, 0)
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
    "ilc9" -> MakeTool(ilc9, bullet_9mm),
    "repeater" -> MakeTool(repeater, bullet_9mm),
    "isp" -> MakeTool(isp, shotgun_shell), //amp
    "beamer" -> MakeTool(beamer, energy_cell),
    "suppressor" -> MakeTool(suppressor, bullet_9mm),
    "anniversary_guna" -> MakeTool(anniversary_guna, anniversary_ammo), //tr stinger
    "anniversary_gun" -> MakeTool(anniversary_gun, anniversary_ammo), //nc spear
    "anniversary_gunb" -> MakeTool(anniversary_gunb, anniversary_ammo), //vs eraser
    "cycler" -> MakeTool(cycler, bullet_9mm),
    "gauss" -> MakeTool(gauss, bullet_9mm),
    "pulsar" -> MakeTool(pulsar, energy_cell),
    "punisher" -> MakeTool(punisher, List(bullet_9mm, rocket)),
    "flechette" -> MakeTool(flechette, shotgun_shell),
    "spiker" -> MakeTool(spiker, ancient_ammo_combo),
    "frag_grenade" -> MakeTool(frag_grenade, frag_grenade_ammo),
    "jammer_grenade" -> MakeTool(jammer_grenade, jammer_grenade_ammo),
    "plasma_grenade" -> MakeTool(plasma_grenade, plasma_grenade_ammo),
    "katana" -> MakeTool(katana, melee_ammo),
    "chainblade" -> MakeTool(chainblade, melee_ammo),
    "magcutter" -> MakeTool(magcutter, melee_ammo),
    "forceblade" -> MakeTool(forceblade, melee_ammo),
    "mini_chaingun" -> MakeTool(mini_chaingun, bullet_9mm),
    "r_shotgun" -> MakeTool(r_shotgun, shotgun_shell), //jackhammer
    "lasher" -> MakeTool(lasher, energy_cell),
    "maelstrom" -> MakeTool(maelstrom, maelstrom_ammo),
    "striker" -> MakeTool(striker, striker_missile_ammo),
    "hunterseeker" -> MakeTool(hunterseeker, hunter_seeker_missile), //phoenix
    "lancer" -> MakeTool(lancer, lancer_cartridge),
    "phoenix" -> MakeTool(phoenix, phoenix_missile), //decimator
    "rocklet" -> MakeTool(rocklet, rocket),
    "thumper" -> MakeTool(thumper, frag_cartridge),
    "radiator" -> MakeTool(radiator, ancient_ammo_combo),
    "heavy_sniper" -> MakeTool(heavy_sniper, bolt), //hsr
    "bolt_driver" -> MakeTool(bolt_driver, bolt),
    "oicw" -> MakeTool(oicw, oicw_ammo), //scorpion
    "flamethrower" -> MakeTool(flamethrower, flamethrower_ammo)
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
    "medicalapplicator" -> MakeTool(medicalapplicator, health_canister),
    "bank" -> MakeTool(bank, armor_canister),
    "nano_dispenser" -> MakeTool(nano_dispenser, armor_canister),
    //TODO "ace" -> MakeConstructionItem(ace),
    //TODO "advanced_ace" -> MakeConstructionItem(advanced_ace),
    "remote_electronics_kit" -> MakeSimpleItem(remote_electronics_kit),
    "trek" -> MakeTool(trek, trek_ammo),
    "command_detonater" -> MakeSimpleItem(command_detonater),
    "flail_targeting_laser" -> MakeSimpleItem(flail_targeting_laser)
  )

  /**
    * Create a new `Tool` from provided `EquipmentDefinition` objects.
    * @param tdef the `ToolDefinition` objects
    * @param adef an `AmmoBoxDefinition` object
    * @return a partial function that, when called, creates the piece of `Equipment`
    */
  protected def MakeTool(tdef : ToolDefinition, adef : AmmoBoxDefinition)() : Tool = MakeTool(tdef, List(adef))

  /**
    * Create a new `Tool` from provided `EquipmentDefinition` objects.
    * Only use this function to create default `Tools` with the default parameters.
    * For example, loadouts can retain `Tool` information that utilizes alternate, valid ammunition types;
    * and, this method function will not construct a complete object if provided that information.
    * @param tdef the `ToolDefinition` objects
    * @param adefs a `List` of `AmmoBoxDefinition` objects
    * @return a curried function that, when called, creates the piece of `Equipment`
    * @see `GlobalDefinitions`
    * @see `OrderTerminalDefinition.BuildSimplifiedPattern`
    */
  protected def MakeTool(tdef : ToolDefinition, adefs : List[AmmoBoxDefinition])() : Tool =  {
    val obj = Tool(tdef)
    (0 until obj.MaxAmmoSlot).foreach(index => {
      val aType = adefs(index)
      val ammo = MakeAmmoBox(aType, Some(obj.Definition.FireModes(index).Magazine)) //make internal magazine, full
      (obj.AmmoSlots(index).Box = ammo) match {
        case Some(_) => ; //this means it worked
        case None =>
          org.log4s.getLogger("TerminalDefinition").warn(s"plans do not match definition: trying to feed ${ammo.AmmoType} ammunition into Tool (${obj.Definition.ObjectId} @ $index)")
      }
    })
    obj
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
    val obj = AmmoBox(adef)
    if(capacity.isDefined) {
      obj.Capacity = capacity.get
    }
    obj
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
}
