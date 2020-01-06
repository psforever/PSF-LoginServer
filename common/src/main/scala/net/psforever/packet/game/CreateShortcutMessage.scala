// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Details regarding this shortcut.<br>
  * <br>
  * The parameters `purpose` and `tile` are closely related.
  * These two fields are consistent for all shortcuts of the same type.
  * `purpose` indicates the purpose of the shortcut.
  * `tile` is related to what kind of graphic is displayed in this shortcut's slot on the hotbar based on its purpose.
  * The parameters `effect1` and `effect2` are exclusive to text macro shortcuts and are defaulted to empty `String`s.<br>
  * <br>
  * The `shortcut_macro` setting displays a word bubble superimposed by the (first three letters of) `effect1` text.<br>
  * Implants and the medkit should have self-explanatory graphics.
  * <br>
  * Purpose:<br>
  * `0 - Medkit`<br>
  * `1 - Macro`<br>
  * `2 - Implant`<br>
  * <br>
  * Tile:<br>
  * `advanced_regen` (regeneration)<br>
  * `audio_amplifier`<br>
  * `darklight_vision`<br>
  * `medkit`<br>
  * `melee_booster`<br>
  * `personal_shield`<br>
  * `range_magnifier`<br>
  * `second_wind`<br>
  * `shortcut_macro`<br>
  * `silent_run` (sensor shield)<br>
  * `surge`<br>
  * `targeting` (enhanced targetting)<br>
  * <br>
  * Exploration:<br>
  * What is `purpose` when 3?
  * @param purpose the primary use of this shortcut
  * @param tile the visual element of the shortcut
  * @param effect1 for macros, a three letter acronym displayed in the hotbar
  * @param effect2 for macros, the chat message content
  */
final case class Shortcut(purpose : Int,
                          tile : String,
                          effect1 : String = "",
                          effect2 : String = "")

/**
  * Facilitate a quick-use button for the hotbar.<br>
  * <br>
  * The hotbar is the eight quick-use slots along the bottom center of the HUD.
  * Each of these slots is the application of a medkit, or use of an implant, or repetition of a text macro.
  * There are actually sixty-four of these slots, eight bound to the Function keys depending on which set is selected.<br>
  * <br>
  * When `addShortcut` is `true`, the provided `Shortcut` will be defined and attached to the respective hotbar slot indicated by `slot`.
  * If it is `false`, the given `slot` will be unbound.
  * Nothing happens if the `slot` selection is invalid.<br>
  * <br>
  * This packet coordinates the shortcut both as an upstream and as a downstream packet, leaning heavily towards the latter.
  * An interesting application is that, if the user does not already have a medkit or a medkit shortcut;
  * but, if he places a medkit in his inventory, the shortcut will be automatically added to his hotbar.
  * This, in turn, dispatches a packet informing the server.
  * The prior functionality will rarely be appreciated, however, as players rarely never have their medkit shortcut unbound.
  * @param player_guid the player
  * @param slot the hotbar slot number (one-indexed)
  * @param unk na; always zero?
  * @param addShortcut true, if we are adding a shortcut; false, if we are removing any current shortcut
  * @param shortcut optional; details about the shortcut to be created
  * @see ChangeShortcutBankMessage
  */
final case class CreateShortcutMessage(player_guid : PlanetSideGUID,
                                       slot : Int,
                                       unk : Int,
                                       addShortcut : Boolean,
                                       shortcut : Option[Shortcut] = None)
  extends PlanetSideGamePacket {
  type Packet = CreateShortcutMessage
  def opcode = GamePacketOpcode.CreateShortcutMessage
  def encode = CreateShortcutMessage.encode(this)
}

object Shortcut extends Marshallable[Shortcut] {
  // Convenient predefined Shortcuts for the Medkit and Implants
  /**
    * Preset for the Audio Amplifier implant. */
  final val AUDIO_AMPLIFIER : Some[Shortcut] = Some(Shortcut(2, "audio_amplifier"))
  /**
    * Preset for the Darklight Vision implant. */
  final val DARKLIGHT_VISION : Some[Shortcut] = Some(Shortcut(2, "darklight_vision"))
  /**
    * Preset for the Enhanced Targeting implant. */
  final val ENHANCED_TARGETING : Some[Shortcut] = Some(Shortcut(2, "targeting"))
  /**
    * Preset for the medkit quick-use option. */
  final val MEDKIT : Some[Shortcut] = Some(Shortcut(0, "medkit"))
  /**
    * Preset for the Melee Booster implant. */
  final val MELEE_BOOSTER : Some[Shortcut] = Some(Shortcut(2, "melee_booster"))
  /**
    * Preset for the Personal Shield implant. */
  final val PERSONAL_SHIELD : Some[Shortcut] = Some(Shortcut(2, "personal_shield"))
  /**
    * Preset for the Range Magnifier implant. */
  final val RANGE_MAGNIFIER : Some[Shortcut] = Some(Shortcut(2, "range_magnifier"))
  /**
    * Preset for the Regeneration implant. */
  final val REGENERATION : Some[Shortcut] = Some(Shortcut(2, "advanced_regen"))
  /**
    * Preset for the Second Wind implant. */
  final val SECOND_WIND : Some[Shortcut] = Some(Shortcut(2, "second_wind"))
  /**
    * Preset for the Sensor Shield implant. */
  final val SENSOR_SHIELD : Some[Shortcut] = Some(Shortcut(2, "silent_run"))
  /**
    * Preset for the Surge implant. */
  final val SURGE : Some[Shortcut] = Some(Shortcut(2, "surge"))
  /**
    * Converter for text macro parameters that acts like a preset.
    * @param effect1 a three letter acronym displayed in the hotbar
    * @param effect2 the chat message content
    * @return `Some` shortcut that represents a voice macro command
    */
  def MACRO(effect1 : String, effect2 : String) : Some[Shortcut] = Some(Shortcut(1, "shortcut_macro", effect1, effect2))

  implicit val codec : Codec[Shortcut] = (
    ("purpose" | uint2L) ::
      ("tile" | PacketHelpers.encodedStringAligned(5)) ::
      ("effect1" | PacketHelpers.encodedWideString) ::
      ("effect2" | PacketHelpers.encodedWideString)
    ).as[Shortcut]
}

object CreateShortcutMessage extends Marshallable[CreateShortcutMessage] {
  implicit val codec : Codec[CreateShortcutMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("slot" | uint8L) ::
      ("unk" | uint8L) ::
      (("addShortcut" | bool) >>:~ { value =>
        conditional(value, "shortcut" | Shortcut.codec).hlist
      })
    ).as[CreateShortcutMessage]
}
