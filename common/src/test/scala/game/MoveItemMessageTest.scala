// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class MoveItemMessageTest extends Specification {
  val string = hex"11 4C00 4B00 4B00 0900 0100"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case MoveItemMessage(item_guid, avatar_guid_1, avatar_guid_2, dest, unk1) =>
        item_guid mustEqual PlanetSideGUID(76)
        avatar_guid_1 mustEqual PlanetSideGUID(75)
        avatar_guid_2 mustEqual PlanetSideGUID(75)
        dest mustEqual 9
        unk1 mustEqual 1
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = MoveItemMessage(PlanetSideGUID(76), PlanetSideGUID(75), PlanetSideGUID(75), 9, 1)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
