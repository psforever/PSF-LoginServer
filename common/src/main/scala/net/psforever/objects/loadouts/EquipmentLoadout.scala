// Copyright (c) 2017 PSForever
package net.psforever.objects.loadouts

/**
  * The base of all specific kinds of blueprint containers.
  * This previous state can be restored on any appropriate template from which the loadout was copied
  * by reconstructing any items (if warranted and permitted) or restoring any appropriate fields.
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
abstract class EquipmentLoadout(
    label: String,
    visible_slots: List[Loadout.SimplifiedEntry],
    inventory: List[Loadout.SimplifiedEntry]
) extends Loadout(label)
