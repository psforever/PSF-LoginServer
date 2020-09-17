// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class LoadMapMessageTest extends Specification {
  val string = hex"31 85 6D61703130 83 7A3130 0FA0 19000000 F6 F1 60 86 80"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case LoadMapMessage(map_name, nav_map_name, unk1, unk2, weapons_unlocked, unk3) =>
        map_name mustEqual "map10"
        nav_map_name mustEqual "z10"
        unk1 mustEqual 40975
        unk2 mustEqual 25
        weapons_unlocked mustEqual true
        unk3 mustEqual 230810349
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = LoadMapMessage("map10", "z10", 40975, 25, true, 230810349)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}
