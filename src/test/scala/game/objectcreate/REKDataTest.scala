// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import org.specs2.mutable._
import scodec.bits._

class REKDataTest extends Specification {
  val string_rek_held    = hex"17 86000000 27086C2350F800800000000000"
  val string_rek_dropped = hex"17 BF000000 EC20311 85219 7AC1A 2D12 00 00 4E 4000000001800"

  "REKData" should {
    "decode (held)" in {
      PacketCoding.decodePacket(string_rek_held).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 134
          cls mustEqual ObjectClass.remote_electronics_kit
          guid mustEqual PlanetSideGUID(3893)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(4174)
          parent.get.slot mustEqual 0
          data match {
            case REKData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid), unk1, unk2) =>
              faction mustEqual PlanetSideEmpire.TR
              bops mustEqual false
              alternate mustEqual false
              v1 mustEqual true
              v2.isEmpty mustEqual true
              v3 mustEqual false
              v4.contains(false) mustEqual true
              v5.isEmpty mustEqual true
              fguid mustEqual PlanetSideGUID(0)
              unk1 mustEqual 0
              unk2 mustEqual 0
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "decode (dropped)" in {
      PacketCoding.decodePacket(string_rek_dropped).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 191
          cls mustEqual ObjectClass.remote_electronics_kit
          guid mustEqual PlanetSideGUID(4355)
          parent.isDefined mustEqual false
          data match {
            case DroppedItemData(
                  pos,
                  REKData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid), unk1, unk2)
                ) =>
              pos.coord mustEqual Vector3(4675.039f, 5506.953f, 72.703125f)
              pos.orient mustEqual Vector3.z(230.625f)

              faction mustEqual PlanetSideEmpire.VS
              bops mustEqual false
              alternate mustEqual false
              v1 mustEqual false
              v2.isEmpty mustEqual true
              v3 mustEqual false
              v4.contains(false) mustEqual true
              v5.isEmpty mustEqual true
              fguid mustEqual PlanetSideGUID(0)

              unk1 mustEqual 3
              unk2 mustEqual 0
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode (held)" in {
      val obj = REKData(
        CommonFieldData(PlanetSideEmpire.TR, false, false, true, None, false, Some(false), None, PlanetSideGUID(0))
      )
      val msg = ObjectCreateMessage(
        ObjectClass.remote_electronics_kit,
        PlanetSideGUID(3893),
        ObjectCreateMessageParent(PlanetSideGUID(4174), 0),
        obj
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector
      pkt mustEqual string_rek_held
    }

    "encode (dropped)" in {
      val obj = DroppedItemData(
        PlacementData(4675.039f, 5506.953f, 72.703125f, 0f, 0f, 230.625f),
        REKData(
          CommonFieldData(PlanetSideEmpire.VS, false, false, false, None, false, Some(false), None, PlanetSideGUID(0)),
          3,
          0
        )
      )
      val msg = ObjectCreateMessage(ObjectClass.remote_electronics_kit, PlanetSideGUID(4355), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector
      pkt mustEqual string_rek_dropped
    }
  }
}
