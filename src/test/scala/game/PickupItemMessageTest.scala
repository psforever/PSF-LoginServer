// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class PickupItemMessageTest extends Specification {
  val string = hex"36 5600 4B00 00 0000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case PickupItemMessage(item_guid, player_guid, unk1, unk2) =>
        item_guid mustEqual PlanetSideGUID(86)
        player_guid mustEqual PlanetSideGUID(75)
        unk1 mustEqual 0
        unk2 mustEqual 0
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = PickupItemMessage(PlanetSideGUID(86), PlanetSideGUID(75), 0, 0)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
