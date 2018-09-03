// Copyright (c) 2017 PSForever
package game.objectcreatedetailed

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.packet.game.objectcreate._
import org.specs2.mutable._
import scodec.bits._

class DetailedTelepadDataTest extends Specification {
  val string = hex"18 97000000 4f00 f3a e301 80 4a680400000200008"
  val string_short = hex"18 87000000 2a00 f3a 5d01 89 8000000200008"
  //TODO validate the unknown fields before router_guid for testing

  "DetailedTelepadData" should {
    "decode" in {
      PacketCoding.DecodePacket(string).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 151
          cls mustEqual ObjectClass.router_telepad
          guid mustEqual PlanetSideGUID(483)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(414)
          parent.get.slot mustEqual 0
          data.isDefined mustEqual true
          data.get.isInstanceOf[DetailedTelepadData] mustEqual true
          data.get.asInstanceOf[DetailedTelepadData].router_guid mustEqual Some(PlanetSideGUID(564))
        case _ =>
          ko
      }
    }

    "decode (short)" in {
      PacketCoding.DecodePacket(string_short).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 135
          cls mustEqual ObjectClass.router_telepad
          guid mustEqual PlanetSideGUID(349)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(340)
          parent.get.slot mustEqual 9
          data.isDefined mustEqual true
          data.get.isInstanceOf[DetailedTelepadData] mustEqual true
          data.get.asInstanceOf[DetailedTelepadData].router_guid mustEqual None
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = DetailedTelepadData(18, PlanetSideGUID(564))
      val msg = ObjectCreateDetailedMessage(ObjectClass.router_telepad, PlanetSideGUID(483), ObjectCreateMessageParent(PlanetSideGUID(414), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string
    }

    "encode (short)" in {
      val obj = DetailedTelepadData(32)
      val msg = ObjectCreateDetailedMessage(ObjectClass.router_telepad, PlanetSideGUID(349), ObjectCreateMessageParent(PlanetSideGUID(340), 9), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_short
    }
  }
}
