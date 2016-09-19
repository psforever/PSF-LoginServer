// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Details regarding this shortcut.
  * <br>
  * Purpose:<br>
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
  * `targeting` (enchanced targetting)
  * @param purpose the primary purpose/use of this shortcut
  * @param effect1 for macros, a three letter acronym displayed in the hotbar
  * @param effect2 for macros, the chat message content
  */
final case class Shortcut(purpose : String,
                          effect1 : String = "",
                          effect2 : String = "")

/**
  * Manipulate a quick-use button for the hotbar.
  * <br>
  * na
  * @param player_guid the player
  * @param slot the hotbar slot number (one-indexed)
  * @param unk1 na
  * @param unk2 na
  * @param shortcut optional; details about the shortcut to be created
  */
final case class CreateShortcutMessage(player_guid : PlanetSideGUID,
                                       slot : Int,
                                       unk1 : Int,
                                       unk2 : Int,
                                       shortcut : Option[Shortcut] = None)
  extends PlanetSideGamePacket {
  type Packet = CreateShortcutMessage
  def opcode = GamePacketOpcode.CreateShortcutMessage
  def encode = CreateShortcutMessage.encode(this)
}

object Shortcut extends Marshallable[Shortcut] {
  // Needs to be Marshallable[T] for .as[T] to work its magic on the type of the codec Codec[T]
  implicit val codec : Codec[Shortcut] = (
    ("purpose" | PacketHelpers.encodedStringAligned(5)) ::
      ("effect1" | PacketHelpers.encodedWideString) ::
      ("effect2" | PacketHelpers.encodedWideString)
    ).as[Shortcut]

  // Convenient predefined Shortcuts for the Medkit and Implants
  final val AUDIO_AMPLIFIER : Shortcut = Shortcut("audio_amplifier")
  final val DARKLIGHT_VISION : Shortcut = Shortcut("darklight_vision")
  final val ENHANCED_TARGETING = Shortcut("targeting")
  final val MEDKIT : Shortcut = Shortcut("medkit")
  final val MELEE_BOOSTER : Shortcut = Shortcut("melee_booster")
  final val PERSONAL_SHIELD : Shortcut = Shortcut("personal_shield")
  final val RANGE_MAGNIFIER : Shortcut = Shortcut("range_magnifier")
  final val REGENERATION : Shortcut = Shortcut("advanced_regen")
  final val SECOND_WIND : Shortcut = Shortcut("second_wind")
  final val SENSOR_SHIELD : Shortcut = Shortcut("silent_run")
  final val SURGE : Shortcut = Shortcut("surge")
}

object CreateShortcutMessage extends Marshallable[CreateShortcutMessage] {
  implicit val codec : Codec[CreateShortcutMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("slot" | uint8L) ::
      ("unk1" | uint8L) ::
      (("unk2" | uintL(3)) >>:~ { value =>
        conditional(value > 0, "shortcut" | Shortcut.codec).hlist
      })
    ).as[CreateShortcutMessage]
}
