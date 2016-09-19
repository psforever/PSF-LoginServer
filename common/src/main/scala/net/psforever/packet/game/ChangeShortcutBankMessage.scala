// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Switch the reference group of shortcuts on the HUD's hotbar.<br>
  * <br>
  * The hotbar contains eight slots for user shortcuts - medkits, implants, and text macros.
  * Next to the first slot are up and down arrow buttons with a number.
  * By progressing through the options available from the arrows, eight sets of eight shortcut slots are revealed.
  * Which set is visible determines the effect of the activating the respective of eight binding keys for the hotbar.
  * Each set is called a "bank."<br>
  * <br>
  * When shortcuts are manipulated, the bank acts as a reference point to the set and moves that set of eight shortcuts onto the HUD.
  * Adding a shortcut to the first slot when viewing the second bank is the same as added a shortcut to the ninth slot when viewing the first bank.
  * Obviously, there is no ninth slot.
  * The slot value merely wraps back around into the next bank.
  * The `bank` value can also wrap around through the first set, so requesting bank 8 (`80`) is the equivalent of requesting bank 1 (`00`).
  * @param player_guid the player
  * @param bank the shortcut bank (zero-indexed)
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
