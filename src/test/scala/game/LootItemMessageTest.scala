// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class LootItemMessageTest extends Specification {
  val string = hex"6C DD0D 5C14"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case LootItemMessage(item_guid, target_guid) =>
        item_guid mustEqual PlanetSideGUID(3549)
        target_guid mustEqual PlanetSideGUID(5212)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = LootItemMessage(PlanetSideGUID(3549), PlanetSideGUID(5212))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}
