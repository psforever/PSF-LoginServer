// Copyright (c) 2017 PSForever
package game.objectcreatedetailed

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateDetailedMessage, _}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}
import scodec.bits._

class DetailedREKDataTest extends Specification {
  val string_rek = hex"18 97000000 2580 6C2 9F05 81 48000002000080000"

  "DetailedREKData" should {
    "decode" in {
      PacketCoding.DecodePacket(string_rek).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 151
          cls mustEqual ObjectClass.remote_electronics_kit
          guid mustEqual PlanetSideGUID(1439)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(75)
          parent.get.slot mustEqual 1
          data match {
            case DetailedREKData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid), unk) =>
              faction mustEqual PlanetSideEmpire.NC
              bops mustEqual false
              alternate mustEqual false
              v1 mustEqual true
              v2.isEmpty mustEqual true
              v3 mustEqual false
              v4.contains(false) mustEqual true
              v5.isEmpty mustEqual true
              fguid mustEqual PlanetSideGUID(0)
              unk mustEqual 0
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = DetailedREKData(CommonFieldData(PlanetSideEmpire.NC, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)))
      val msg = ObjectCreateDetailedMessage(ObjectClass.remote_electronics_kit, PlanetSideGUID(1439), ObjectCreateMessageParent(PlanetSideGUID(75), 1), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_rek
    }
  }
}
