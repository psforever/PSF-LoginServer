// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import org.specs2.mutable._
import scodec.bits._

class HandheldDataTest extends Specification {
  val string_ace_held = hex"17 76000000 0406900650C80480000000"
  val string_ace_dropped = hex"17 AF000000 90024113B329C5D5A2D1200005B440000000"

  "ACE" should {
    "decode (held)" in {
      PacketCoding.DecodePacket(string_ace_held).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 118
          cls mustEqual ObjectClass.ace
          guid mustEqual PlanetSideGUID(3173)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(3336)
          parent.get.slot mustEqual 0
          data match {
            case HandheldData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid), mode, unk) =>
              faction mustEqual PlanetSideEmpire.NC
              bops mustEqual false
              alternate mustEqual false
              v1 mustEqual true
              v2.isEmpty mustEqual true
              v3 mustEqual false
              v4.isEmpty mustEqual true
              v5.isEmpty mustEqual true
              fguid mustEqual PlanetSideGUID(0)
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "decode (dropped)" in {
      PacketCoding.DecodePacket(string_ace_dropped).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 175
          cls mustEqual ObjectClass.ace
          guid mustEqual PlanetSideGUID(4388)
          parent.isDefined mustEqual false
          data.isInstanceOf[DroppedItemData[_]] mustEqual true
          data match {
            case DroppedItemData(pos, HandheldData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid), mode, unk)) =>
              pos.coord mustEqual Vector3(4708.461f, 5547.539f, 72.703125f)
              pos.orient mustEqual Vector3.z(194.0625f)

              faction mustEqual PlanetSideEmpire.VS
              bops mustEqual false
              alternate mustEqual false
              v1 mustEqual true
              v2.isEmpty mustEqual true
              v3 mustEqual false
              v4.isEmpty mustEqual true
              v5.isEmpty mustEqual true
              fguid mustEqual PlanetSideGUID(0)
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode (held)" in {
      val obj = HandheldData(CommonFieldData(PlanetSideEmpire.NC, false, false, true, None, false, None, None, PlanetSideGUID(0)))
      val msg = ObjectCreateMessage(ObjectClass.ace, PlanetSideGUID(3173), ObjectCreateMessageParent(PlanetSideGUID(3336), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_ace_held
    }

    "encode (dropped)" in {
      val obj = DroppedItemData(
        PlacementData(4708.461f, 5547.539f, 72.703125f, 0f, 0f, 194.0625f),
        HandheldData(CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, None, None, PlanetSideGUID(0)))
      )
      val msg = ObjectCreateMessage(ObjectClass.ace, PlanetSideGUID(4388), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_ace_dropped
    }
  }

  val string_telepad = hex"17 86000000 5700 f3a a201 80 0302020000000"

  "Telepad" should {
    "decode" in {
      PacketCoding.DecodePacket(string_telepad).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 134
          cls mustEqual ObjectClass.router_telepad
          guid mustEqual PlanetSideGUID(418)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(430)
          parent.get.slot mustEqual 0
          data match {
            case HandheldData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid), mode, unk) =>
              faction mustEqual PlanetSideEmpire.TR
              bops mustEqual false
              alternate mustEqual false
              v1 mustEqual false
              v2.isEmpty mustEqual true
              v3 mustEqual false
              v4.isEmpty mustEqual true
              v5.contains(385) mustEqual true
              fguid mustEqual PlanetSideGUID(0)
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = HandheldData(CommonFieldData(PlanetSideEmpire.TR, false, false, false, None, false, None, Some(385), PlanetSideGUID(0)))
      val msg = ObjectCreateMessage(ObjectClass.router_telepad, PlanetSideGUID(418), ObjectCreateMessageParent(PlanetSideGUID(430), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_telepad
    }
  }

  val string_boomertrigger = hex"17 76000000 58084A8100E80C00000000" //reconstructed from an inventory entry

  "Boomer Trigger" should {
    "decode" in {
      PacketCoding.DecodePacket(string_boomertrigger).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 118
          cls mustEqual ObjectClass.boomer_trigger
          guid mustEqual PlanetSideGUID(3600)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(4272)
          parent.get.slot mustEqual 0
          data match {
            case HandheldData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid), mode, unk) =>
              faction mustEqual PlanetSideEmpire.NEUTRAL
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
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = HandheldData(CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, false, None, false, None, None, PlanetSideGUID(0)))
      val msg = ObjectCreateMessage(ObjectClass.boomer_trigger, PlanetSideGUID(3600), ObjectCreateMessageParent(PlanetSideGUID(4272), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_boomertrigger
    }
  }

  val string_detonater_held = hex"17 76000000 1A886A8421080400000000"
  val string_detonater_dropped = hex"17 AF000000 EA8620ED1549B4B6A741500001B000000000"

  "Command Detonater" should {
    "decode (held)" in {
      PacketCoding.DecodePacket(string_detonater_held).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 118
          cls mustEqual ObjectClass.command_detonater
          guid mustEqual PlanetSideGUID(4162)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(4149)
          parent.get.slot mustEqual 0
          data match {
            case HandheldData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid), mode, unk) =>
              faction mustEqual PlanetSideEmpire.NC
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
        case _ =>
          ko
      }
    }

    "decode (dropped)" in {
      PacketCoding.DecodePacket(string_detonater_dropped).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 175
          cls mustEqual ObjectClass.command_detonater
          guid mustEqual PlanetSideGUID(3682)
          parent.isDefined mustEqual false
          data match {
            case DroppedItemData(pos, HandheldData(CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid), mode, unk)) =>
              pos.coord mustEqual Vector3(4777.633f, 5485.4062f, 85.8125f)
              pos.orient mustEqual Vector3.z(14.0625f)

              faction mustEqual PlanetSideEmpire.TR
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
        case _ =>
          ko
      }
    }

    "encode (held)" in {
      val obj = HandheldData(CommonFieldData(PlanetSideEmpire.NC, false, false, false, None, false, None, None, PlanetSideGUID(0)))
      val msg = ObjectCreateMessage(ObjectClass.command_detonater, PlanetSideGUID(4162), ObjectCreateMessageParent(PlanetSideGUID(4149), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_detonater_held
    }

    "encode (dropped)" in {
      val obj = DroppedItemData(
        PlacementData(4777.633f, 5485.4062f, 85.8125f, 0f, 0f, 14.0625f),
        HandheldData(CommonFieldData(PlanetSideEmpire.TR, false, false, false, None, false, None, None, PlanetSideGUID(0)))
      )
      val msg = ObjectCreateMessage(ObjectClass.command_detonater, PlanetSideGUID(3682), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_detonater_dropped
    }
  }
}
