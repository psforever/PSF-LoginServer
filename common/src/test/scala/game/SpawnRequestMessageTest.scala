// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.SpawnGroup
import scodec.bits._

class SpawnRequestMessageTest extends Specification {
  val string = hex"4a000007000000000000000200"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case SpawnRequestMessage(unk1,unk2,unk3,unk4,unk5) =>
        unk1 mustEqual 0
        unk2 mustEqual SpawnGroup.Facility
        unk3 mustEqual 0
        unk4 mustEqual 0
        unk5 mustEqual 2
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = SpawnRequestMessage(0, SpawnGroup.Facility, 0, 0, 2)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
