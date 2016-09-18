// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Create a quick-use button for the hotbar.<br>
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
  * `targeting`
  * @param player_guid the player
  * @param slot the hotbar slot number (one-indexed)
  * @param unk1 na
  * @param unk2 na
  * @param purpose the primary purpose/use of this shortcut
  * @param effect1 for macros, a three letter acronym displayed in the hotbar
  * @param effect2 for macros, the chat message content
  */
//TODO Failed to parse game packet 0x28: purpose: cannot acquire 15 bits from a vector that contains 4 bits -- encountered when attempting to move shortcuts -- look into this
final case class CreateShortcutMessage(player_guid : PlanetSideGUID,
                                       slot : Int,
                                       unk1 : Int,
                                       unk2 : Int,
                                       purpose : String,
                                       effect1 : String = "",
                                       effect2 : String = "")
  extends PlanetSideGamePacket {
  type Packet = CreateShortcutMessage
  def opcode = GamePacketOpcode.CreateShortcutMessage
  def encode = CreateShortcutMessage.encode(this)
}

object CreateShortcutMessage extends Marshallable[CreateShortcutMessage] {
  implicit val codec : Codec[CreateShortcutMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("slot" | uint8L) ::
      ("unk1" | uint8L) ::
      ("unk2" | uintL(3)) ::
      ("purpose" | PacketHelpers.encodedStringAligned(5)) ::
      ("effect1" | PacketHelpers.encodedWideString) ::
      ("effect2" | PacketHelpers.encodedWideString)
    ).as[CreateShortcutMessage]
}
