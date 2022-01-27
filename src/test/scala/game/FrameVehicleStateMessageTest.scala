// Copyright (c) 2021 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class FrameVehicleStateMessageTest extends Specification {
  val string = hex"1c b101 087778ad7e62609ddf771beee0647fc6001a0000000000000000"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case FrameVehicleStateMessage(guid, unk1, pos, ang, vel, unk2, unk3, unk4, crouched, airborne, ascending, ftime, u9, uA) =>
        guid mustEqual PlanetSideGUID(433)
        unk1 mustEqual 0
        pos mustEqual Vector3(6518.5234f, 1918.6719f, 16.296875f)
        ang mustEqual Vector3(353.67188f, 6.328125f, 130.42969f)
        vel.contains(Vector3(21.0375f, -17.55f, 27.1125f)) mustEqual true
        unk2 mustEqual false
        unk3 mustEqual 0
        unk4 mustEqual 0
        crouched mustEqual false
        airborne mustEqual false
        ascending mustEqual true
        ftime mustEqual 10
        u9 mustEqual 0
        uA mustEqual 0
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = FrameVehicleStateMessage(
      PlanetSideGUID(433),
      0,
      Vector3(6518.5234f, 1918.6719f, 16.296875f),
      Vector3(353.67188f, 6.328125f, 130.42969f),
      Some(Vector3(21.0375f, -17.55f, 27.1125f)),
      false,
      0,
      0,
      false,
      false,
      true,
      10,
      0,
      0
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}

