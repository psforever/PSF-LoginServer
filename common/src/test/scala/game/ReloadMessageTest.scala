// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ReloadMessageTest extends Specification {
  val string = hex"0D 4C00 7B000000 FFFFFFFF"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ReloadMessage(item_guid, ammo_clip, unk1) =>
        item_guid mustEqual PlanetSideGUID(76)
        ammo_clip mustEqual 123
        unk1 mustEqual -1
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ReloadMessage(PlanetSideGUID(76), 123, -1)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
