// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class AvatarDeadStateMessageTest extends Specification {
  val string = hex"ad3c1260801c12608009f99861fb0741e040000010"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case AvatarDeadStateMessage(unk1,unk2,unk3,pos,unk4,unk5) =>
        unk1 mustEqual 1
        unk2 mustEqual 300000
        unk3 mustEqual 300000
        pos mustEqual Vector3(6552.617f,4602.375f,60.90625f)
        unk4 mustEqual 2
        unk5 mustEqual true
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = AvatarDeadStateMessage(1, 300000, 300000, Vector3(6552.617f,4602.375f,60.90625f), 2, true)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
