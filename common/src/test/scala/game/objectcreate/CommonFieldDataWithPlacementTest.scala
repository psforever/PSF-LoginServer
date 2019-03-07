// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, Vector3}
import org.specs2.mutable._
import scodec.bits._

class CommonFieldDataWithPlacementTest extends Specification {
  val string_boomer = hex"17 A5000000 CA0000F1630938D5A8F1400003F0031100"

  "Boomer" should {
    "decode" in {
      PacketCoding.DecodePacket(string_boomer).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 165
          cls mustEqual ObjectClass.boomer
          guid mustEqual PlanetSideGUID(3840)
          parent.isDefined mustEqual false
          data match {
            case CommonFieldDataWithPlacement(pos, com) =>
              pos.coord mustEqual Vector3(4704.172f, 5546.4375f, 82.234375f)
              pos.orient mustEqual Vector3.z(272.8125f)
              com match {
                case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid) =>
                  faction mustEqual PlanetSideEmpire.TR
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual false
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.contains(false) mustEqual true
                  v5.isEmpty mustEqual true
                  fguid mustEqual PlanetSideGUID(8290)
                case _ =>
                  ko
              }
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = CommonFieldDataWithPlacement(
        PlacementData(Vector3(4704.172f, 5546.4375f, 82.234375f), Vector3.z(272.8125f)),
        CommonFieldData(PlanetSideEmpire.TR, false, false, false, None, false, Some(false), None, PlanetSideGUID(8290))
      )
      val msg = ObjectCreateMessage(ObjectClass.boomer, PlanetSideGUID(3840), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_boomer
    }
  }
}
