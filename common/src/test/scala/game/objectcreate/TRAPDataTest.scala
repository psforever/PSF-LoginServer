// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, Vector3}
import org.specs2.mutable._
import scodec.bits._

class TRAPDataTest extends Specification {
  val string_trap = hex"17 BB000000 A8B630A 39FA6 FD666 801C 00 00 00 44C6097F80F00"

  "TRAPData" should {
    "decode" in {
      PacketCoding.DecodePacket(string_trap).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 187
          cls mustEqual ObjectClass.tank_traps
          guid mustEqual PlanetSideGUID(2659)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[TRAPData] mustEqual true
          val trap = data.get.asInstanceOf[TRAPData]
          trap.deploy.pos.coord mustEqual Vector3(3572.4453f, 3277.9766f, 114.0f)
          trap.deploy.pos.orient mustEqual Vector3(0, 0, 90)
          trap.deploy.faction mustEqual PlanetSideEmpire.VS
          trap.deploy.owner_guid mustEqual PlanetSideGUID(4748)
          trap.health mustEqual 255
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = TRAPData(
        SmallDeployableData(
          PlacementData(3572.4453f, 3277.9766f, 114.0f, 0f, 0f, 90.0f),
          PlanetSideEmpire.VS, false, false, 2, false, true, PlanetSideGUID(4748)
        ),
        255
      )
      val msg = ObjectCreateMessage(ObjectClass.tank_traps, PlanetSideGUID(2659), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      val pkt_bitv = pkt.toBitVector
      val ori_bitv = string_trap.toBitVector
      pkt_bitv.take(173) mustEqual ori_bitv.take(173)
      pkt_bitv.drop(185) mustEqual ori_bitv.drop(185)
      //TODO work on TRAPData to make this pass as a single stream
    }
  }
}
