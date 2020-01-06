// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class VehicleStateMessageTest extends Specification {
  val string = hex"1b 9d010d85aecaa6b8c2dfdfefffc020008006000078"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case VehicleStateMessage(guid, unk1, pos, ang, vel, unk2, unk3, unk4, wheel, unk5, unk6) =>
        guid mustEqual PlanetSideGUID(413)
        unk1 mustEqual 0
        pos.x mustEqual 3674.8438f
        pos.y mustEqual 2726.789f
        pos.z mustEqual 91.09375f
        ang.x mustEqual 359.29688f
        ang.y mustEqual 1.0546875f
        ang.z mustEqual 90.35156f
        vel.isDefined mustEqual true
        vel.get.x mustEqual 0.0f
        vel.get.y mustEqual 0.0f
        vel.get.z mustEqual 0.03125f
        unk2.isDefined mustEqual false
        unk3 mustEqual 0
        unk4 mustEqual 0
        wheel mustEqual 15
        unk5 mustEqual false
        unk6 mustEqual false
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = VehicleStateMessage(
      PlanetSideGUID(413),
      0,
      Vector3(3674.8438f, 2726.789f, 91.09375f),
      Vector3(359.29688f, 1.0546875f, 90.35156f),
      Some(Vector3(0.0f, 0.0f, 0.03125f)),
      None,
      0, 0, 15,
      false, false
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
