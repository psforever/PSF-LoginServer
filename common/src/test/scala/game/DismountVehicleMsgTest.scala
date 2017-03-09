// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class DismountVehicleMsgTest extends Specification {
  val string = hex"0F C609 00"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case DismountVehicleMsg(player_guid, unk1, unk2) =>
        player_guid mustEqual PlanetSideGUID(2502)
        unk1 mustEqual 0
        unk2 mustEqual false
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DismountVehicleMsg(PlanetSideGUID(2502), 0, false)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
