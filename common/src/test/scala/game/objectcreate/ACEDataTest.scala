// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import org.specs2.mutable._
import scodec.bits._

class ACEDataTest extends Specification {
  val string_ace_held = hex"17 76000000 0406900650C80480000000"
  val string_ace_dropped = hex"17 AF000000 90024113B329C5D5A2D1200005B440000000"

  "ACEData" should {
    "decode (held)" in {
      PacketCoding.DecodePacket(string_ace_held).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 118
          cls mustEqual ObjectClass.ace
          guid mustEqual PlanetSideGUID(3173)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(3336)
          parent.get.slot mustEqual 0
          data.isDefined mustEqual true
          data.get.isInstanceOf[ACEData] mustEqual true
          val ace = data.get.asInstanceOf[ACEData]
          ace.unk1 mustEqual 4
          ace.unk2 mustEqual 8
          ace.unk3 mustEqual 0
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
          data.isDefined mustEqual true
          data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
          val drop = data.get.asInstanceOf[DroppedItemData[_]]
          drop.pos.coord.x mustEqual 4708.461f
          drop.pos.coord.y mustEqual 5547.539f
          drop.pos.coord.z mustEqual 72.703125f
          drop.pos.orient.x mustEqual 0f
          drop.pos.orient.y mustEqual 0f
          drop.pos.orient.z mustEqual 194.0625f
          drop.obj.isInstanceOf[ACEData] mustEqual true
          val ace = drop.obj.asInstanceOf[ACEData]
          ace.unk1 mustEqual 8
          ace.unk2 mustEqual 8
        case _ =>
          ko
      }
    }

    "encode (held)" in {
      val obj = ACEData(4, 8)
      val msg = ObjectCreateMessage(ObjectClass.ace, PlanetSideGUID(3173), ObjectCreateMessageParent(PlanetSideGUID(3336), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_ace_held
    }

    "encode (dropped)" in {
      val obj = DroppedItemData(
        PlacementData(4708.461f, 5547.539f, 72.703125f, 0f, 0f, 194.0625f),
        ACEData(8, 8)
      )
      val msg = ObjectCreateMessage(ObjectClass.ace, PlanetSideGUID(4388), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_ace_dropped
    }
  }
}
