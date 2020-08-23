// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class ChangeAmmoMessageTest extends Specification {
  val string = hex"47 4E00 00000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ChangeAmmoMessage(item_guid, unk1) =>
        item_guid mustEqual PlanetSideGUID(78)
        unk1 mustEqual 0
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ChangeAmmoMessage(PlanetSideGUID(78), 0)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
