// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import org.specs2.mutable._
import scodec.bits._

class TRAPDataTest extends Specification {
  val string_trap = hex"17 BB000000 A8B630A 39FA6 FD666 801C 00 00 00 44C6097F80F00"

  "TRAPData" should {
    "decode" in {
      PacketCoding.decodePacket(string_trap).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 187
          cls mustEqual ObjectClass.tank_traps
          guid mustEqual PlanetSideGUID(2659)
          parent.isDefined mustEqual false
          data match {
            case TRAPData(CommonFieldDataWithPlacement(pos, deploy), health) =>
              pos.coord mustEqual Vector3(3572.4453f, 3277.9766f, 114.0f)
              pos.orient mustEqual Vector3.z(90)
              deploy.faction mustEqual PlanetSideEmpire.VS
              deploy.bops mustEqual false
              deploy.alternate mustEqual false
              deploy.v1 mustEqual true
              deploy.v2.isEmpty mustEqual true
              deploy.jammered mustEqual false
              deploy.v4.contains(true) mustEqual true
              deploy.v5.isEmpty mustEqual true
              deploy.guid mustEqual PlanetSideGUID(4748)
              health mustEqual 255
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = TRAPData(
        CommonFieldDataWithPlacement(
          PlacementData(Vector3(3572.4453f, 3277.9766f, 114.0f), Vector3.z(90)),
          CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, Some(true), None, PlanetSideGUID(4748))
        ),
        255
      )
      val msg      = ObjectCreateMessage(ObjectClass.tank_traps, PlanetSideGUID(2659), obj)
      val pkt      = PacketCoding.encodePacket(msg).require.toByteVector
      val pkt_bitv = pkt.toBitVector
      val ori_bitv = string_trap.toBitVector
      pkt_bitv.take(173) mustEqual ori_bitv.take(173)
      pkt_bitv.drop(185) mustEqual ori_bitv.drop(185)
      //TODO work on TRAPData to make this pass as a single stream
    }
  }
}
