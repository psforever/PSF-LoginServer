// Copyright (c) 2017 PSForever
package net.psforever.objects.loadouts

import net.psforever.objects.avatar.Certification
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
  *
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
final case class InfantryLoadout(
    label: String,
    visible_slots: List[Loadout.SimplifiedEntry],
    inventory: List[Loadout.SimplifiedEntry],
    exosuit: ExoSuitType.Value,
    subtype: Int
) extends EquipmentLoadout(label, visible_slots, inventory)

object InfantryLoadout {
  import net.psforever.objects.Player
  import net.psforever.objects.GlobalDefinitions
  import net.psforever.objects.equipment.Equipment

  /**
    * The sub-type of the player's uniform.
    * Applicable to mechanized assault units, mainly.
    * The subtype is reported as a number but indicates the specialization - anti-infantry, ani-vehicular, anti-air - of the suit
    * as indicated by the arm weapon(s).
    * @param player the player
    * @return the numeric subtype
    */
  def DetermineSubtype(player: Player): Int = {
    DetermineSubtypeA(player.ExoSuit, player.Slot(0).Equipment)
  }

  /**
    * The sub-type of the player's uniform.
    * Applicable to mechanized assault units, mainly.
    * The subtype is reported as a number but indicates the specialization - anti-infantry, ani-vehicular, anti-air - of the suit
    * as indicated by the arm weapon(s).
    * @param suit the player's uniform;
    *             the target is for MAX armors
    * @param weapon any weapon the player may have it his "first pistol slot;"
    *               to a MAX, that is its "primary weapon slot"
    * @return the numeric subtype
    */
  def DetermineSubtypeA(suit: ExoSuitType.Value, weapon: Option[Equipment]): Int = {
    if (suit == ExoSuitType.MAX) {
      weapon match {
        case Some(item) =>
          item.Definition match {
            case GlobalDefinitions.trhev_burster | GlobalDefinitions.nchev_sparrow | GlobalDefinitions.vshev_starfire =>
              1
            case GlobalDefinitions.trhev_dualcycler | GlobalDefinitions.nchev_scattercannon |
                GlobalDefinitions.vshev_quasar =>
              2
            case GlobalDefinitions.trhev_pounder | GlobalDefinitions.nchev_falcon | GlobalDefinitions.vshev_comet =>
              3
            case _ =>
              0
          }
        case None =>
          0
      }
    } else {
      0
    }
  }

  /**
    * The sub-type of the player's uniform, as used in `FavoritesMessage`.<br>
    * <br>
    * The values for `Standard`, `Infiltration`, and the generic `MAX` are not perfectly known.
    * The latter-most exo-suit option is presumed.
    * @param suit the player's uniform
    * @param subtype the mechanized assault exo-suit subtype as determined by their arm weapons
    * @return the numeric subtype
    */
  def DetermineSubtypeB(suit: ExoSuitType.Value, subtype: Int): Int = {
    suit match {
      case ExoSuitType.Standard     => 0
      case ExoSuitType.Agile        => 1
      case ExoSuitType.Reinforced   => 2
      case ExoSuitType.MAX          => 3 + subtype // 4, 5, 6
      case ExoSuitType.Infiltration => 7
    }
  }

  /**
    * Assuming the exo-suit is a mechanized assault type,
    * use the subtype to determine what certifications would be valid for permitted access to that specific exo-suit.
    * The "C" does not stand for "certification."
    *
    * @see `CertificationType`
    * @param subtype the numeric subtype
    * @return a `Set` of all certifications that would grant access to the mechanized assault exo-suit subtype
    */
  def DetermineSubtypeC(subtype: Int): Set[Certification] =
    subtype match {
      case 1 => Set(Certification.AAMAX, Certification.UniMAX)
      case 2 => Set(Certification.AIMAX, Certification.UniMAX)
      case 3 => Set(Certification.AVMAX, Certification.UniMAX)
      case _ => Set.empty[Certification]
    }
}
