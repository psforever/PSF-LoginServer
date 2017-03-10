// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ChangeShortcutBankMessageTest extends Specification {
  val string = hex"29 4B00 20"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ChangeShortcutBankMessage(player_guid, bank) =>
        player_guid mustEqual PlanetSideGUID(75)
        bank mustEqual 2
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ChangeShortcutBankMessage(PlanetSideGUID(75), 2)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
