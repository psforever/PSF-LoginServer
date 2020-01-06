// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class ActionCancelMessageTest extends Specification {
  val string = hex"22 201ee01a10"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case ActionCancelMessage(player_guid, object_guid, unk) =>
        player_guid mustEqual PlanetSideGUID(7712)
        object_guid mustEqual PlanetSideGUID(6880)
        unk mustEqual 1
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = ActionCancelMessage(PlanetSideGUID(7712), PlanetSideGUID(6880), 1)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
