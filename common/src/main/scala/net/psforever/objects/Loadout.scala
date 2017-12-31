// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition._
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.InventoryItem
import net.psforever.types.ExoSuitType

import scala.annotation.tailrec

//trait Loadout {
//  def Label : String
//  def VisibleSlots : List[Loadout.SimplifiedEntry]
//  def Inventory : List[Loadout.SimplifiedEntry]
//  def ExoSuit : ExoSuitType.Value
//  def Subtype : Int
//}

/**
  * From a `Player` their current exo-suit and their `Equipment`, retain a set of instructions to reconstruct this arrangement.<br>
  * <br>
  * `Loadout` objects are composed of the following information, as if a blueprint:<br>
  * - the avatar's current exo-suit<br>
  * - the type of specialization, called a "subtype" (mechanized assault exo-suits only)<br>
  * - the contents of the avatar's occupied holster slots<br>
  * - the contents of the avatar's occupied inventory<br>
  * `Equipment` contents of the holsters and of the formal inventory region will be condensed into a simplified form.
  * These are also "blueprints."
  * At its most basic, this simplification will merely comprise the former object's `EquipmentDefinition`.
  * For items that are already simple - `Kit` objects and `SimpleItem` objects - this form will not be too far removed.
  * For more complicated affairs like `Tool` objects and `AmmoBox` objects, only essential information will be retained.<br>
  * <br>
  * The deconstructed blueprint can be applied to any avatar.
  * They are, however, typically tied to unique users and unique characters.
  * For reasons of certifications, however, permissions on that avatar may affect what `Equipment` can be distributed.
  * Even a whole blueprint can be denied if the user lacks the necessary exo-suit certification.
  * A completely new piece of `Equipment` is constructed when the `Loadout` is regurgitated.<br>
  * <br>
  * The fifth tab on an `order_terminal` window is for "Favorite" blueprints for `Loadout` entries.
  * The ten-long list is initialized with `FavoritesMessage` packets.
  * Specific entries are loaded or removed using `FavoritesRequest` packets.
  * @param label the name by which this inventory will be known when displayed in a Favorites list
  * @param visible_slots simplified representation of the `Equipment` that can see "seen" on the target
  * @param inventory simplified representation of the `Equipment` in the target's inventory or trunk
  * @param exosuit na
  * @param subtype na
  */
final case class Loadout(private val label : String,
                         private val visible_slots : List[Loadout.SimplifiedEntry],
                         private val inventory : List[Loadout.SimplifiedEntry],
                         private val exosuit : ExoSuitType.Value,
                         private val subtype : Int) {
  /**
    * The label by which this `Loadout` is called.
    * @return the label
    */
  def Label : String = label

  /**
    * The exo-suit in which the avatar will be dressed.
    * Might be restricted and, thus, restrict the rest of the `Equipment` from being constructed and given.
    * @return the exo-suit
    */
  def ExoSuit : ExoSuitType.Value = exosuit

  /**
    * The mechanized assault exo-suit specialization number that indicates whether the MAX performs:
    * anti-infantry (1),
    * anti-vehicular (2),
    * or anti-air work (3).
    * The major distinction is the type of arm weapons that MAX is equipped.
    * When the blueprint doesn't call for a MAX, the number will be 0.
    * @return the specialization number
    */
  def Subtype : Int = subtype

  /**
    * The `Equipment` in the `Player`'s holster slots when this `Loadout` is created.
    * @return a `List` of the holster item blueprints
    */
  def VisibleSlots : List[Loadout.SimplifiedEntry] = visible_slots

  /**
    * The `Equipment` in the `Player`'s inventory region when this `Loadout` is created.
    * @return a `List` of the inventory item blueprints
    */
  def Inventory : List[Loadout.SimplifiedEntry] = inventory
}

object Loadout {
  def apply(label : String, visible : List[SimplifiedEntry], inventory : List[SimplifiedEntry]) : Loadout = {
    new Loadout(label, visible, inventory, ExoSuitType.Standard, 0)
  }

  def Create(player : Player, label : String) : Loadout = {
    new Loadout(
      label,
      packageSimplifications(player.Holsters()),
      packageSimplifications(player.Inventory.Items.values.toList),
      player.ExoSuit,
      DetermineSubtype(player)
    )
  }

  def Create(vehicle : Vehicle, label : String) : Loadout = {
    Loadout(
      label,
      packageSimplifications(vehicle.Weapons.map({ case ((index, weapon)) => InventoryItem(weapon.Equipment.get, index) }).toList),
      packageSimplifications(vehicle.Trunk.Items.values.toList)
    )
  }

  /**
    * A basic `Trait` connecting all of the `Equipment` blueprints.
    */
  sealed trait Simplification

  /**
    * An entry in the `Loadout`, wrapping around a slot index and what is in the slot index.
    * @param item the `Equipment`
    * @param index the slot number where the `Equipment` is to be stowed
    * @see `InventoryItem`
    */
  final case class SimplifiedEntry(item: Simplification, index: Int)

  /**
    * The simplified form of an `AmmoBox`.
    * @param adef the `AmmoBoxDefinition` that describes this future object
    * @param capacity the amount of ammunition, if any, to initialize;
    *                 if `None`, then the previous `AmmoBoxDefinition` will be referenced for the amount later
    */
  final case class ShorthandAmmoBox(adef : AmmoBoxDefinition, capacity : Int) extends Simplification
  /**
    * The simplified form of a `Tool`.
    * @param tdef the `ToolDefinition` that describes this future object
    * @param ammo the blueprints to construct the correct number of ammunition slots in the `Tool`
    */
  final case class ShorthandTool(tdef : ToolDefinition, ammo : List[ShorthandAmmoSlot]) extends Simplification
  /**
    * The simplified form of a `Tool` `FireMode`
    * @param ammoIndex the index that points to the type of ammunition this slot currently uses
    * @param ammo a `ShorthandAmmoBox` object to load into that slot
    */
  final case class ShorthandAmmoSlot(ammoIndex : Int, ammo : ShorthandAmmoBox)
  /**
    * The simplified form of a `ConstructionItem`.
    * @param cdef the `ConstructionItemDefinition` that describes this future object
    */
  final case class ShorthandConstructionItem(cdef : ConstructionItemDefinition) extends Simplification
  /**
    * The simplified form of a `SimpleItem`.
    * @param sdef the `SimpleItemDefinition` that describes this future object
    */
  final case class ShorthandSimpleItem(sdef : SimpleItemDefinition) extends Simplification
  /**
    * The simplified form of a `Kit`.
    * @param kdef the `KitDefinition` that describes this future object
    */
  final case class ShorthandKit(kdef : KitDefinition) extends Simplification

  def DetermineSubtype(player : Player) : Int = {
    if(player.ExoSuit == ExoSuitType.MAX) {
      player.Slot(0).Equipment match {
        case Some(item) =>
          item.Definition match {
            case GlobalDefinitions.trhev_dualcycler | GlobalDefinitions.nchev_scattercannon | GlobalDefinitions.vshev_quasar =>
              1
            case GlobalDefinitions.trhev_pounder | GlobalDefinitions.nchev_falcon | GlobalDefinitions.vshev_comet =>
              2
            case GlobalDefinitions.trhev_burster | GlobalDefinitions.nchev_sparrow | GlobalDefinitions.vshev_starfire =>
              3
            case _ =>
              0
          }
        case None =>
          0
      }
    }
    else {
      0
    }
  }

  /**
    * Overloaded entry point for constructing simplified blueprints from holster slot equipment.
    * @param equipment the holster slots
    * @return a `List` of simplified `Equipment`
    */
  private def packageSimplifications(equipment : Array[EquipmentSlot]) : List[SimplifiedEntry] = {
    recursiveHolsterSimplifications(equipment.iterator)
  }

  /**
    * Overloaded entry point for constructing simplified blueprints from inventory region equipment.
    * @param equipment the enumerated contents of the inventory
    * @return a `List` of simplified `Equipment`
    */
  private def packageSimplifications(equipment : List[InventoryItem]) : List[SimplifiedEntry] = {
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
