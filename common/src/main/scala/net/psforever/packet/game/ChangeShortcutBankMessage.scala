// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Switch the set of shortcuts displayed on the HUD's hotbar.<br>
  * <br>
  * The hotbar contains eight slots for user shortcuts - medkits, implants, and text macros.
  * Next to the first slot are up and down arrow buttons with a number.
  * By progressing through the options available from the arrows, eight sets of eight shortcut slots are revealed.
  * Which set is visible determines the effect of the activating the respective of eight binding keys (the Function keys) for the hotbar.
  * Each set is called a "bank."
  * @param player_guid the player
  * @param bank the shortcut bank (zero-indexed);
  *             0-7 are the valid banks
  */
final case class ChangeShortcutBankMessage(player_guid : PlanetSideGUID,
                                  bank : Int)
  extends PlanetSideGamePacket {
  type Packet = ChangeShortcutBankMessage
  def opcode = GamePacketOpcode.ChangeShortcutBankMessage
  def encode = ChangeShortcutBankMessage.encode(this)
}

object ChangeShortcutBankMessage extends Marshallable[ChangeShortcutBankMessage] {
  implicit val codec : Codec[ChangeShortcutBankMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("bank" | uintL(4))
    ).as[ChangeShortcutBankMessage]
}
