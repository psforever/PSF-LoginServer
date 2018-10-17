// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects._
import net.psforever.objects.definition._
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.loadouts.Loadout
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.types.ExoSuitType

import scala.annotation.switch

abstract class EquipmentTerminalDefinition(objId : Int) extends TerminalDefinition(objId) {
  Name = "equipment_terminal"

  /**
    * Process a `TransactionType.Sell` action by the user.
    * There is no specific tab associated with this action.
    * It is a common button on the terminal interface window.
    * Additionally, the equipment to be sold ia almost always in the player's `FreeHand` slot.
    * Selling `Equipment` is always permitted.
    * @param player the player
    * @param msg the original packet carrying the request
    * @return an actionable message that explains how to process the request
    */
  override def Sell(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = {
    Terminal.SellEquipment()
  }
}

object EquipmentTerminalDefinition {
  private[this] val log = org.log4s.getLogger("TerminalDefinition")

  /**
    * A `Map` of information for changing exo-suits.
    * key - an identification string sent by the client
    * value - a `Tuple` containing exo-suit specifications
    */
  val suits : Map[String, (ExoSuitType.Value, Int)] = Map(
    "standard_issue_armor" -> (ExoSuitType.Standard, 0),
    "lite_armor" -> (ExoSuitType.Agile, 0),
    "med_armor" -> (ExoSuitType.Reinforced, 0),
    "stealth_armor" -> (ExoSuitType.Infiltration, 0)
  )

  /**
    * A `Map` of information for changing mechanized assault exo-suits.
    * key - an identification string sent by the client
    * value - a `Tuple` containing exo-suit specifications
    */
  val maxSuits : Map[String, (ExoSuitType.Value, Int)] = Map(
    "trhev_antiaircraft" -> (ExoSuitType.MAX, 3),
    "trhev_antipersonnel" -> (ExoSuitType.MAX, 1),
    "trhev_antivehicular" -> (ExoSuitType.MAX, 2),
    "nchev_antiaircraft" -> (ExoSuitType.MAX, 3),
    "nchev_antipersonnel" -> (ExoSuitType.MAX, 1),
    "nchev_antivehicular" -> (ExoSuitType.MAX, 2),
    "vshev_antiaircraft" -> (ExoSuitType.MAX, 3),
    "vshev_antipersonnel" -> (ExoSuitType.MAX, 1),
    "vshev_antivehicular" -> (ExoSuitType.MAX, 2)
  )

  import net.psforever.objects.GlobalDefinitions._
  /**
    * A `Map` of operations for producing the `AmmoBox` `Equipment` for infantry-held weaponry.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  val infantryAmmunition : Map[String, () => Equipment] = Map(
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
  val maxAmmo : Map[String, () => Equipment] = Map(
    "dualcycler_ammo" -> MakeAmmoBox(dualcycler_ammo),
    "pounder_ammo" -> MakeAmmoBox(pounder_ammo),
    "burster_ammo" -> MakeAmmoBox(burster_ammo),
    "scattercannon_ammo" -> MakeAmmoBox(scattercannon_ammo),
    "falcon_ammo" -> MakeAmmoBox(falcon_ammo),
    "sparrow_ammo" -> MakeAmmoBox(sparrow_ammo),
    "quasar_ammo" -> MakeAmmoBox(quasar_ammo),
    "comet_ammo" -> MakeAmmoBox(comet_ammo),
    "starfire_ammo" -> MakeAmmoBox(starfire_ammo)
  )
  /**
    * A `Map` of operations for producing the `AmmoBox` `Equipment` for infantry-held utilities.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  val supportAmmunition : Map[String, () => Equipment] = Map(
    "health_canister" -> MakeAmmoBox(health_canister),
    "armor_canister" -> MakeAmmoBox(armor_canister),
    "upgrade_canister" -> MakeAmmoBox(upgrade_canister)
  )
  /**
    * A `Map` of operations for producing the `AmmoBox` `Equipment` for vehicle-mounted weaponry.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  val vehicleAmmunition : Map[String, () => Equipment] = Map(
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
  val infantryWeapons : Map[String, () => Equipment] = Map(
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
  val supportWeapons : Map[String, () => Equipment] = Map(
    "medkit" -> MakeKit(medkit),
    "super_medkit" -> MakeKit(super_medkit),
    "super_armorkit" -> MakeKit(super_armorkit),
    "super_staminakit" -> MakeKit(super_staminakit),
    "medicalapplicator" -> MakeTool(medicalapplicator),
    "bank" -> MakeTool(bank, armor_canister),
    "nano_dispenser" -> MakeTool(nano_dispenser),
    "ace" -> MakeConstructionItem(ace),
    "advanced_ace" -> MakeConstructionItem(advanced_ace),
    "remote_electronics_kit" -> MakeSimpleItem(remote_electronics_kit),
    "trek" -> MakeTool(trek),
    "command_detonater" -> MakeSimpleItem(command_detonater),
    "flail_targeting_laser" -> MakeSimpleItem(flail_targeting_laser)
  )
  /**
    * A single-element `Map` of the one piece of `Equipment` specific to the Router.
    */
  val routerTerminal : Map[String, () => Equipment] = Map("router_telepad" -> MakeTelepad(router_telepad))

  /**
    * Create a new `Tool` from provided `EquipmentDefinition` objects.
    * @param tdef the `ToolDefinition` object
    * @return a partial function that, when called, creates the piece of `Equipment`
    */
  private def MakeTool(tdef : ToolDefinition)() : Tool = MakeTool(tdef, Nil)

  /**
    * Create a new `Tool` from provided `EquipmentDefinition` objects.
    * @param tdef the `ToolDefinition` object
    * @param adef an `AmmoBoxDefinition` object
    * @return a partial function that, when called, creates the piece of `Equipment`
    */
  private def MakeTool(tdef : ToolDefinition, adef : AmmoBoxDefinition)() : Tool = MakeTool(tdef, List(adef))

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
  private def MakeTool(tdef : ToolDefinition, adefs : List[AmmoBoxDefinition])() : Tool = {
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
  private def MakeAmmoBox(adef : AmmoBoxDefinition, capacity : Option[Int] = None)() : AmmoBox = {
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
  private def MakeKit(kdef : KitDefinition)() : Kit = Kit(kdef)

  /**
    * Create a new `BoomerTrigger`, a unique kind of `SimpleItem`.
    * @param sdef the `SimpleItemDefinition` object;
    *             actually ignored, but retained for function definition consistency
    * @return a curried function that, when called, creates the piece of `Equipment`
    * @see `GlobalDefinitions`
    */
  private def MakeTriggerItem(sdef : SimpleItemDefinition)() : SimpleItem = new BoomerTrigger

  /**
    * Create a new `SimpleItem` from provided `EquipmentDefinition` objects.
    * @param sdef the `SimpleItemDefinition` object
    * @return a curried function that, when called, creates the piece of `Equipment`
    * @see `GlobalDefinitions`
    */
  private def MakeSimpleItem(sdef : SimpleItemDefinition)() : SimpleItem = SimpleItem(sdef)

  /**
    * Create a new `ConstructionItem` from provided `EquipmentDefinition` objects.
    * @param cdef the `ConstructionItemDefinition` object
    * @return a curried function that, when called, creates the piece of `Equipment`
    * @see `GlobalDefinitions`
    */
  private def MakeConstructionItem(cdef : ConstructionItemDefinition)() : ConstructionItem = ConstructionItem(cdef)

  /**
    * na
    * @param cdef na
    * @return na
    */
  private def MakeTelepad(cdef : ConstructionItemDefinition)() : Telepad = Telepad(cdef)

  /**
    * Accept a simplified blueprint for some piece of `Equipment` and create an actual piece of `Equipment` based on it.
    * Used specifically for the reconstruction of `Equipment` via an `Loadout`.
    * @param entry the simplified blueprint
    * @return some `Equipment` object
    * @see `TerminalDefinition.MakeTool`<br>
    *       `TerminalDefinition.MakeAmmoBox`<br>
    *       `TerminalDefinition.MakeSimpleItem`<br>
    *       `TerminalDefinition.MakeConstructionItem`<br>
    *       `TerminalDefinition.MakeKit`
    */
  def BuildSimplifiedPattern(entry : Loadout.Simplification) : Equipment = {
    import net.psforever.objects.loadouts.Loadout._
    entry match {
      case obj : ShorthandTool =>
        val ammo : List[AmmoBoxDefinition] = obj.ammo.map(fmode => { fmode.ammo.definition })
        val tool = Tool(obj.definition)
        //makes Tools where an ammo slot may have one of its alternate ammo types
        (0 until tool.MaxAmmoSlot).foreach(index => {
          val slot = tool.AmmoSlots(index)
          slot.AmmoTypeIndex += obj.ammo(index).ammoIndex
          slot.Box = MakeAmmoBox(ammo(index), Some(obj.ammo(index).ammo.capacity))
        })
        tool

      case obj : ShorthandAmmoBox =>
        MakeAmmoBox(obj.definition, Some(obj.capacity))

      case obj : ShorthandConstructionItem =>
        MakeConstructionItem(obj.definition)

      case obj : ShorthandTriggerItem =>
        MakeTriggerItem(obj.definition)

      case obj : ShorthandSimpleItem =>
        MakeSimpleItem(obj.definition)

      case obj : ShorthandKit =>
        MakeKit(obj.definition)
    }
  }

  /**
    * Process a `TransactionType.Buy` action by the user.
    * Either attempt to purchase equipment or attempt to switch directly to a different exo-suit.
    * @param page0Stock the `Equipment` items and `AmmoBox` items available on the first tab
    * @param page2Stock the `Equipment` items and `AmmoBox` items available on the third tab
    * @param exosuits the exo-suit types (and subtypes) available on the second tab
    * @param player the player
    * @param msg the original packet carrying the request
    * @return an actionable message that explains how to process the request
    */
  def Buy(page0Stock : Map[String, ()=>Equipment],
          page2Stock : Map[String, ()=>Equipment],
          exosuits : Map[String, (ExoSuitType.Value, Int)])
         (player : Player, msg : ItemTransactionMessage): Terminal.Exchange = {
    (msg.item_page : @switch) match {
      case 0 => //Weapon tab
        page0Stock.get(msg.item_name) match {
          case Some(item) =>
            Terminal.BuyEquipment(item())
          case None =>
            Terminal.NoDeal()
        }
      case 2 => //Support tab
        page2Stock.get(msg.item_name) match {
          case Some(item) =>
            Terminal.BuyEquipment(item())
          case None =>
            Terminal.NoDeal()
        }
      case 3 => //Vehicle tab
        vehicleAmmunition.get(msg.item_name) match {
          case Some(item) =>
            Terminal.BuyEquipment(item())
          case None =>
            Terminal.NoDeal()
        }
      case 1 => //Armor tab
        exosuits.get(msg.item_name) match {
          case Some((suit, subtype)) =>
            Terminal.BuyExosuit(suit, subtype)
          case None =>
            maxAmmo.get(msg.item_name) match {
              case Some(item) =>
                Terminal.BuyEquipment(item())
              case None =>
                Terminal.NoDeal()
            }
        }
      case _ =>
        Terminal.NoDeal()
    }
  }
}
