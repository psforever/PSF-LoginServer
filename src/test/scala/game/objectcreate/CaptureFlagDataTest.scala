// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.objects.Default
import net.psforever.packet.PacketCoding
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import org.specs2.mutable._
import scodec.bits._

class CaptureFlagDataTest extends Specification {
  val string_captureflag =
    hex"17 E5000000 CE8EA10 04A47 B818A FE0E 00 00 0F 24000015000400160B09000" //LLU for Qumu on Amerish, going to Verica

  "CaptureFlagData" in {
    "decode" in {
      PacketCoding.decodePacket(string_captureflag).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 229
          cls mustEqual ObjectClass.capture_flag
          guid mustEqual PlanetSideGUID(4330)
          parent.isDefined mustEqual false
          data match {
            case CaptureFlagData(CommonFieldDataWithPlacement(pos, vdata), ownerGuid, targetGuid, time) =>
              pos.coord mustEqual Vector3(3912.0312f, 5169.4375f, 59.96875f)
              pos.orient mustEqual Vector3(0f, 0f, 47.8125f)
              pos.vel.isEmpty mustEqual true
              vdata.faction mustEqual PlanetSideEmpire.NC
              vdata.bops mustEqual false
              vdata.alternate mustEqual false
              vdata.v1 mustEqual true
              vdata.v2.isEmpty mustEqual true
              vdata.jammered mustEqual false
              vdata.v4.isEmpty mustEqual true
              vdata.v5.isEmpty mustEqual true
              vdata.guid mustEqual Default.GUID0
              ownerGuid mustEqual 21
              targetGuid mustEqual 4
              time mustEqual 592662
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = CaptureFlagData(
        CommonFieldDataWithPlacement(
          PlacementData(3912.0312f, 5169.4375f, 59.96875f, 0f, 0f, 47.8125f),
          CommonFieldData(PlanetSideEmpire.NC, false, false, true, None, false, None, None, Default.GUID0)
        ),
        21,
        4,
        592662
      )
      val msg = ObjectCreateMessage(ObjectClass.capture_flag, PlanetSideGUID(4330), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector
      pkt mustEqual string_captureflag
    }
  }
}
