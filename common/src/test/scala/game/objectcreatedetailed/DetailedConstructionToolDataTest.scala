// Copyright (c) 2019 PSForever
package game.objectcreatedetailed

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable._
import scodec.bits._

class DetailedConstructionToolDataTest extends Specification {
  val string_ace = hex"18 87000000 1006 100 C70B 80 8800000200008"
  val string_boomer_trigger = hex"18 87000000 6304CA8760B 80 C800000200008"
  val string_telepad = hex"18 97000000 4f00 f3a e301 80 4a680400000200008"
  val string_telepad_short = hex"18 87000000 2a00 f3a 5d01 89 8000000200008"

  "ACE (detailed)" should {
    "decode" in {
      PacketCoding.DecodePacket(string_ace).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 135
          cls mustEqual ObjectClass.ace
          guid mustEqual PlanetSideGUID(3015)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(3104)
          parent.get.slot mustEqual 0
          data match {
            case DetailedConstructionToolData(cdata, mode) =>
              cdata.faction mustEqual PlanetSideEmpire.VS
              cdata.bops mustEqual false
              cdata.alternate mustEqual false
              cdata.v1 mustEqual true
              cdata.v2.isEmpty mustEqual true
              cdata.jammered mustEqual false
              cdata.v4.isEmpty mustEqual true
              cdata.v5.isEmpty mustEqual true
              cdata.guid mustEqual PlanetSideGUID(0)
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = DetailedConstructionToolData(
        CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, None, None, PlanetSideGUID(0))
      )
      val msg = ObjectCreateDetailedMessage(ObjectClass.ace, PlanetSideGUID(3015), ObjectCreateMessageParent(PlanetSideGUID(3104), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_ace
    }
  }

  "Boomer Trigger (detailed)" should {
    "decode" in {
      PacketCoding.DecodePacket(string_boomer_trigger).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 135
          cls mustEqual ObjectClass.boomer_trigger
          guid mustEqual PlanetSideGUID(2934)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(2502)
          parent.get.slot mustEqual 0
          data match {
            case DetailedConstructionToolData(cdata, mode) =>
              cdata.faction mustEqual PlanetSideEmpire.NEUTRAL
              cdata.bops mustEqual false
              cdata.alternate mustEqual false
              cdata.v1 mustEqual true
              cdata.v2.isEmpty mustEqual true
              cdata.jammered mustEqual false
              cdata.v4.isEmpty mustEqual true
              cdata.v5.isEmpty mustEqual true
              cdata.guid mustEqual PlanetSideGUID(0)
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = DetailedConstructionToolData(
        CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0))
      )
      val msg = ObjectCreateDetailedMessage(ObjectClass.boomer_trigger, PlanetSideGUID(2934), ObjectCreateMessageParent(PlanetSideGUID(2502), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_boomer_trigger
    }
  }

  "Telepad (detailed)" should {
    "decode" in {
      PacketCoding.DecodePacket(string_telepad).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 151
          cls mustEqual ObjectClass.router_telepad
          guid mustEqual PlanetSideGUID(483)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(414)
          parent.get.slot mustEqual 0
          data match {
            case DetailedConstructionToolData(cdata, mode) =>
              cdata.faction mustEqual PlanetSideEmpire.NC
              cdata.bops mustEqual false
              cdata.alternate mustEqual false
              cdata.v1 mustEqual true
              cdata.v2.isEmpty mustEqual true
              cdata.jammered mustEqual false
              cdata.v4.isEmpty mustEqual true
              cdata.v5.contains(564) mustEqual true
              cdata.guid mustEqual PlanetSideGUID(0)
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "decode (short)" in {
      PacketCoding.DecodePacket(string_telepad_short).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 135
          cls mustEqual ObjectClass.router_telepad
          guid mustEqual PlanetSideGUID(349)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(340)
          parent.get.slot mustEqual 9
          data match {
            case DetailedConstructionToolData(cdata, mode) =>
              cdata.faction mustEqual PlanetSideEmpire.VS
              cdata.bops mustEqual false
              cdata.alternate mustEqual false
              cdata.v1 mustEqual false
              cdata.v2.isEmpty mustEqual true
              cdata.jammered mustEqual false
              cdata.v4.isEmpty mustEqual true
              cdata.v5.isEmpty mustEqual true
              cdata.guid mustEqual PlanetSideGUID(0)
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = DetailedConstructionToolData(
        CommonFieldData(PlanetSideEmpire.NC, false, false, true, None, false, None, Some(564), PlanetSideGUID(0))
      )
      val msg = ObjectCreateDetailedMessage(ObjectClass.router_telepad, PlanetSideGUID(483), ObjectCreateMessageParent(PlanetSideGUID(414), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_telepad
    }

    "encode (short)" in {
      val obj = DetailedConstructionToolData(CommonFieldData(PlanetSideEmpire.VS))
      val msg = ObjectCreateDetailedMessage(ObjectClass.router_telepad, PlanetSideGUID(349), ObjectCreateMessageParent(PlanetSideGUID(340), 9), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_telepad_short
    }
  }
}
