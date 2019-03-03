// Copyright (c) 2017 PSForever
package game.objectcreatedetailed

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateDetailedMessage, _}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideEmpire
import scodec.bits._

class DetailedCommandDetonaterDataTest extends Specification {
  val string_detonater = hex"18 87000000 6506 EA8 7420 80 8000000200008"

  "DetailedCommandDetonaterData" should {
    "decode" in {
      PacketCoding.DecodePacket(string_detonater).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 135
          cls mustEqual ObjectClass.command_detonater
          guid mustEqual PlanetSideGUID(8308)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(3530)
          parent.get.slot mustEqual 0
          data match {
            case DetailedCommandDetonaterData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid)) =>
              faction mustEqual PlanetSideEmpire.VS
              bops mustEqual false
              alternate mustEqual false
              v1 mustEqual false
              v2.isEmpty mustEqual true
              v3 mustEqual false
              v4.isEmpty mustEqual true
              v5.isEmpty mustEqual true
              fguid mustEqual PlanetSideGUID(0)
            case _ =>
              ko
          }

          data.isInstanceOf[DetailedCommandDetonaterData] mustEqual true
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = DetailedCommandDetonaterData(CommonFieldData(PlanetSideEmpire.VS, false, false, false, None, false, None, None, PlanetSideGUID(0)))
      val msg = ObjectCreateDetailedMessage(ObjectClass.command_detonater, PlanetSideGUID(8308), ObjectCreateMessageParent(PlanetSideGUID(3530), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_detonater
    }
  }
}
