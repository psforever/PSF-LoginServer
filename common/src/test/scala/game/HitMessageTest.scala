// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class HitMessageTest extends Specification {
  val string_hitgeneric = hex"09 09E9A70200"
  val string_hitobj = hex"09 99292705F4B1FB9514585F08BDD3D454CC5EE80300"

  "decode (generic)" in {
    PacketCoding.DecodePacket(string_hitgeneric).require match {
      case HitMessage(seq_time, projectile_guid, unk1, hit_info, unk2, unk3, unk4) =>
        seq_time mustEqual 777
        projectile_guid mustEqual PlanetSideGUID(40102)
        unk1 mustEqual 0
        hit_info mustEqual None
        unk2 mustEqual true
        unk3 mustEqual false
        unk4 mustEqual None
      case _ =>
        ko
    }
  }

  "decode (object)" in {
    PacketCoding.DecodePacket(string_hitobj).require match {
      case HitMessage(seq_time, projectile_guid, unk1, hit_info, unk2, unk3, unk4) =>
        seq_time mustEqual 153
        projectile_guid mustEqual PlanetSideGUID(40100)
        unk1 mustEqual 0
        hit_info mustEqual Some(HitInfo(Vector3(3672.9766f, 2729.8594f, 92.34375f), Vector3(3679.5156f, 2722.6172f, 92.796875f), Some(PlanetSideGUID(372))))
        unk2 mustEqual true
        unk3 mustEqual false
        unk4 mustEqual None
      case _ =>
        ko
    }
  }

  "encode (generic)" in {
    val msg_hitgeneric = HitMessage(777, PlanetSideGUID(40102), 0, None, true, false, None)
    val pkt_hitgeneric = PacketCoding.EncodePacket(msg_hitgeneric).require.toByteVector
    pkt_hitgeneric mustEqual string_hitgeneric
  }

  "encode (object)" in {
    val msg_hitobj = HitMessage(153, PlanetSideGUID(40100), 0, Some(HitInfo(Vector3(3672.9766f, 2729.8594f, 92.34375f), Vector3(3679.5156f, 2722.6172f, 92.796875f), Some(PlanetSideGUID(372)))), true, false, None)
    val pkt_hitobj = PacketCoding.EncodePacket(msg_hitobj).require.toByteVector

    pkt_hitobj mustEqual string_hitobj
  }
}
