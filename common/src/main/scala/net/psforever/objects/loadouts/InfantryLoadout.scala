// Copyright (c) 2017 PSForever
package net.psforever.objects.loadouts

import net.psforever.types.ExoSuitType

/**
  * A blueprint of a player's uniform, their holster items, and their inventory items, saved in a specific state.
  * This previous state can be restored on any given player template
  * by reconstructing the items (if permitted) and re-assigning the uniform (if available).<br>
  * <br>
  * The fifth tab on an `order_terminal` window is occupied by the list of "Favorite" `Loadout` blueprints.
  * The ten-long list is initialized with `FavoritesMessage` packets assigned to the "Infantry" list.
  * Specific entries are added or removed using `FavoritesRequest` packets,
  * re-established using other conventional game packets.
  * @param label the name by which this inventory will be known when displayed in a Favorites list;
  *              field gets inherited
  * @param visible_slots simplified representation of the `Equipment` that can see "seen" on the target;
  *                      field gets inherited
  * @param inventory simplified representation of the `Equipment` in the target's inventory or trunk;
  *                  field gets inherited
  * @param exosuit the exo-suit in which the avatar will be dressed;
  *                may be restricted
  * @param subtype the mechanized assault exo-suit specialization number that indicates whether the MAX performs:
  *                anti-infantry (1), anti-vehicular (2), or anti-air work (3);
  *                the default value is 0
  */
final case class InfantryLoadout(label : String,
                                 visible_slots : List[Loadout.SimplifiedEntry],
                                 inventory : List[Loadout.SimplifiedEntry],
                                 exosuit : ExoSuitType.Value,
                                 subtype : Int) extends Loadout(label, visible_slots, inventory)
