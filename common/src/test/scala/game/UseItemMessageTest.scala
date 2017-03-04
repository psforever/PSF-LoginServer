// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class UseItemMessageTest extends Specification {
  val string = hex"10 4B00 0000 7401 FFFFFFFF 4001000000000000000000000000058C803600800000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, unk9) =>
        avatar_guid mustEqual PlanetSideGUID(75)
        unk1 mustEqual 0
        object_guid mustEqual PlanetSideGUID(372)
        unk2 mustEqual 0xFFFFFFFFL
        unk3 mustEqual false
        unk4 mustEqual Vector3(5.0f, 0.0f, 0.0f)
        unk5 mustEqual Vector3(0.0f, 0.0f, 0.0f)
        unk6 mustEqual 11
        unk7 mustEqual 25
        unk8 mustEqual 0
        unk9 mustEqual 364
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = UseItemMessage(PlanetSideGUID(75), 0, PlanetSideGUID(372), 0xFFFFFFFFL, false, Vector3(5.0f, 0.0f, 0.0f), Vector3(0.0f, 0.0f, 0.0f), 11, 25, 0, 364)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
