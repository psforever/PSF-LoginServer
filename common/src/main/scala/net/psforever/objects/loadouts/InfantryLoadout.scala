// Copyright (c) 2017 PSForever
package net.psforever.objects.loadouts

import net.psforever.types.ExoSuitType

final case class InfantryLoadout(label : String,
                                 visible_slots : List[Loadout.SimplifiedEntry],
                                 inventory : List[Loadout.SimplifiedEntry],
                                 exosuit : ExoSuitType.Value,
                                 subtype : Int) extends Loadout(label, visible_slots, inventory) {
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
}
