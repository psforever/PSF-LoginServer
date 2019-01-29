// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable._
import scodec.bits._

class CaptureFlagDataTest extends Specification {
  val string_captureflag = hex"17 E5000000 CE8EA10 04A47 B818A FE0E 00 00 0F 24000015000400160B09000" //LLU for Qumu on Amerish

  "CaptureFlagData" in {
    "decode" in {
      PacketCoding.DecodePacket(string_captureflag).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 229
          cls mustEqual ObjectClass.capture_flag
          guid mustEqual PlanetSideGUID(4330)
          parent.isDefined mustEqual false
          data.isInstanceOf[CaptureFlagData] mustEqual true
          val flag = data.asInstanceOf[CaptureFlagData]
          flag.pos.coord.x mustEqual 3912.0312f
          flag.pos.coord.y mustEqual 5169.4375f
          flag.pos.coord.z mustEqual 59.96875f
          flag.pos.orient.x mustEqual 0f
          flag.pos.orient.y mustEqual 0f
          flag.pos.orient.z mustEqual 47.8125f
          flag.faction mustEqual PlanetSideEmpire.NC
          flag.unk1 mustEqual 21
          flag.unk2 mustEqual 4
          flag.unk3 mustEqual 2838
          flag.unk4 mustEqual 9
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = CaptureFlagData(PlacementData(3912.0312f, 5169.4375f, 59.96875f, 0f, 0f, 47.8125f), PlanetSideEmpire.NC, 21, 4, 2838, 9)
      val msg = ObjectCreateMessage(ObjectClass.capture_flag, PlanetSideGUID(4330), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_captureflag
    }
  }
}
