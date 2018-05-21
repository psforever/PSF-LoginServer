// Copyright (c) 2017 PSForever
package net.psforever.objects.loadouts

import net.psforever.objects._
import net.psforever.objects.definition._
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.InventoryItem

import scala.annotation.tailrec

/**
  * The base of all specific kinds of blueprint containers.
  * This previous state can be restored on any appropriate template from which the loadout was copied
  * by reconstructing the items (if permitted).
  * The three fields are the name assigned to the loadout,
  * the visible items that are created (which obey different rules depending on the source),
  * and the concealed items that are created and added to the source's `Inventory`.<br>
  * For example, the `visible_slots` on a `Player`-borne loadout will transform into the form `Array[EquipmentSlot]`;
  * `Vehicle`-originating loadouts transform into the form `Map[Int, Equipment]`.
  * <br>
  * The lists of user-specific loadouts are initialized with `FavoritesMessage` packets.
  * Specific entries are loaded or removed using `FavoritesRequest` packets.
  * @param label the name by which this inventory will be known when displayed in a Favorites list
  * @param visible_slots simplified representation of the `Equipment` that can see "seen" on the target
  * @param inventory simplified representation of the `Equipment` in the target's inventory or trunk
  */
abstract class Loadout(label : String,
                       visible_slots : List[Loadout.SimplifiedEntry],
                       inventory : List[Loadout.SimplifiedEntry])

object Loadout {
  /**
    * Produce the blueprint on a player.
    * @param player the player
    * @param label the name of this loadout
    * @return an `InfantryLoadout` object populated with appropriate information about the current state of the player
    */
  def Create(player : Player, label : String) : Loadout = {
    InfantryLoadout(
      label,
      packageSimplifications(player.Holsters()),
      packageSimplifications(player.Inventory.Items.values.toList),
      player.ExoSuit,
      DetermineSubtype(player)
    )
  }

  /**
    * Produce the blueprint of a vehicle.
    * @param vehicle the vehicle
    * @param label the name of this loadout
    * @return a `VehicleLoadout` object populated with appropriate information about the current state of the vehicle
    */
  def Create(vehicle : Vehicle, label : String) : Loadout = {
    VehicleLoadout(
      label,
      packageSimplifications(vehicle.Weapons.map({ case ((index, weapon)) => InventoryItem(weapon.Equipment.get, index) }).toList),
      packageSimplifications(vehicle.Trunk.Items.values.toList),
      vehicle.Definition
    )
  }

  /**
    * A basic `Trait` connecting all of the `Equipment` blueprints.
    */
  sealed trait Simplification {
    def definition : ObjectDefinition
  }

  /**
    * An entry in the `Loadout`, wrapping around a slot index and what is in the slot index.
    * @param item the `Equipment`
    * @param index the slot number where the `Equipment` is to be stowed
    * @see `InventoryItem`
    */
  final case class SimplifiedEntry(item: Simplification, index: Int)

  /**
    * The simplified form of an `AmmoBox`.
    * @param definition the `AmmoBoxDefinition` that describes this future object
    * @param capacity the amount of ammunition, if any, to initialize;
    *                 if `None`, then the previous `AmmoBoxDefinition` will be referenced for the amount later
    */
  final case class ShorthandAmmoBox(definition : AmmoBoxDefinition, capacity : Int) extends Simplification
  /**
    * The simplified form of a `Tool`.
    * @param definition the `ToolDefinition` that describes this future object
    * @param ammo the blueprints to construct the correct number of ammunition slots in the `Tool`
    */
  final case class ShorthandTool(definition : ToolDefinition, ammo : List[ShorthandAmmoSlot]) extends Simplification
  /**
    * The simplified form of a `Tool` `FireMode`
    * @param ammoIndex the index that points to the type of ammunition this slot currently uses
    * @param ammo a `ShorthandAmmoBox` object to load into that slot
    */
  final case class ShorthandAmmoSlot(ammoIndex : Int, ammo : ShorthandAmmoBox)
  /**
    * The simplified form of a `ConstructionItem`.
    * @param definition the `ConstructionItemDefinition` that describes this future object
    */
  final case class ShorthandConstructionItem(definition : ConstructionItemDefinition) extends Simplification
  /**
    * The simplified form of a `SimpleItem`.
    * @param definition the `SimpleItemDefinition` that describes this future object
    */
  final case class ShorthandSimpleItem(definition : SimpleItemDefinition) extends Simplification
  /**
    * The simplified form of a `Kit`.
    * @param definition the `KitDefinition` that describes this future object
    */
  final case class ShorthandKit(definition : KitDefinition) extends Simplification

  /**
    * The sub-type of the player's uniform.
    * Applicable to mechanized assault units, mainly.
    * The subtype is reported as a number but indicates the specialization - anti-infantry, ani-vehicular, anti-air - of the suit
    * as indicated by the arm weapon(s).
    * @param player the player
    * @return the numeric subtype
    */
  def DetermineSubtype(player : Player) : Int = {
    InfantryLoadout.DetermineSubtype(player)
  }

  /**
    * The sub-type of the vehicle.
    * Vehicle's have no subtype.
    * @param vehicle the vehicle
    * @return the numeric subtype, always 0
    */
  def DetermineSubtype(vehicle : Vehicle) : Int = 0

  /**
    * Overloaded entry point for constructing simplified blueprints from holster slot equipment.
    * @param equipment the holster slots
    * @return a `List` of simplified `Equipment`
    */
  protected def packageSimplifications(equipment : Array[EquipmentSlot]) : List[SimplifiedEntry] = {
    recursiveHolsterSimplifications(equipment.iterator)
  }

  /**
    * Overloaded entry point for constructing simplified blueprints from inventory region equipment.
    * @param equipment the enumerated contents of the inventory
    * @return a `List` of simplified `Equipment`
    */
  protected def packageSimplifications(equipment : List[InventoryItem]) : List[SimplifiedEntry] = {
    equipment.map(entry => { SimplifiedEntry(buildSimplification(entry.obj), entry.start) })
  }


  /**
    * Traverse a `Player`'s holsters and transform occupied slots into simplified blueprints for the contents of that slot.
    * The holsters are fixed positions and can be unoccupied.
    * Only occupied holsters are transformed into blueprints.
    * The `index` field is necessary as the `Iterator` for the holsters lacks self-knowledge about slot position.
    * @param iter an `Iterator`
    * @param index the starting index;
    *              defaults to 0 and increments automatically
    * @param list an updating `List` of simplified `Equipment` blueprints;
    *             empty, by default
    * @return a `List` of simplified `Equipment` blueprints
    */
  @tailrec private def recursiveHolsterSimplifications(iter : Iterator[EquipmentSlot], index : Int = 0, list : List[SimplifiedEntry] = Nil) : List[SimplifiedEntry] = {
    if(!iter.hasNext) {
      list
    }
    else {
      val entry = iter.next
      entry.Equipment match {
        case Some(obj) =>
          recursiveHolsterSimplifications(iter, index + 1, list :+ SimplifiedEntry(buildSimplification(obj), index))
        case None =>
          recursiveHolsterSimplifications(iter, index + 1, list)
      }
    }
  }

  /**
    * Ammunition slots are internal connection points where `AmmoBox` units represent the characteristics of a magazine.
    * Their simplification process has a layer of complexity that ensures that the content of the slot
    * matches the type of content that should be in the slot.
    * If it does not, it extracts information about the slot from the `EquipmentDefinition` and sets the blueprints.
    * @param iter an `Iterator`
    * @param list an updating `List` of simplified ammo slot blueprints;
    *             empty, by default
    * @return a `List` of simplified ammo slot blueprints
    * @see `Tool.FireModeSlot`
    */
  @tailrec private def recursiveFireModeSimplications(iter : Iterator[Tool.FireModeSlot], list : List[ShorthandAmmoSlot] = Nil) : List[ShorthandAmmoSlot] = {
    if(!iter.hasNext) {
      list
    }
    else {
      val entry = iter.next
      val fmodeSimp = if(entry.Box.AmmoType == entry.AmmoType) {
        ShorthandAmmoSlot(
          entry.AmmoTypeIndex,
          ShorthandAmmoBox(entry.Box.Definition, entry.Box.Capacity)
        )
      }
      else {
        ShorthandAmmoSlot(
          entry.AmmoTypeIndex,
          ShorthandAmmoBox(entry.Tool.AmmoTypes(entry.Definition.AmmoTypeIndices.head), 1)
        )
      }
      recursiveFireModeSimplications(iter, list :+ fmodeSimp)
    }
  }

  /**
    * Accept a piece of `Equipment` and transform it into a simplified blueprint.
    * @param obj the `Equipment`
    * @return the simplified blueprint
    */
  private def buildSimplification(obj : Equipment) : Simplification = {
    obj match {
      case obj : Tool =>
        val flist = recursiveFireModeSimplications(obj.AmmoSlots.iterator)
        ShorthandTool(obj.Definition, flist)
      case obj : AmmoBox =>
        ShorthandAmmoBox(obj.Definition, obj.Capacity)
      case obj : ConstructionItem =>
        ShorthandConstructionItem(obj.Definition)
      case obj : SimpleItem =>
        ShorthandSimpleItem(obj.Definition)
      case obj : Kit =>
        ShorthandKit(obj.Definition)
    }
  }
}
