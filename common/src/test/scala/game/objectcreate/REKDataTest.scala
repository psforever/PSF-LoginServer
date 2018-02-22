// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import org.specs2.mutable._
import scodec.bits._

class REKDataTest extends Specification {
  val string_rek_held = hex"17 86000000 27086C2350F800800000000000"
  val string_rek_dropped = hex"17 BF000000 EC20311 85219 7AC1A 2D12 00 00 4E 4000000001800"

  "REKData" should {
    "decode (held)" in {
      PacketCoding.DecodePacket(string_rek_held).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 134
          cls mustEqual ObjectClass.remote_electronics_kit
          guid mustEqual PlanetSideGUID(3893)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(4174)
          parent.get.slot mustEqual 0
          data.isDefined mustEqual true
          data.get.isInstanceOf[REKData] mustEqual true
          val rek = data.get.asInstanceOf[REKData]
          rek.unk1 mustEqual 0
          rek.unk2 mustEqual 8
          rek.unk3 mustEqual 0
        case _ =>
          ko
      }
    }

    "decode (dropped)" in {
      PacketCoding.DecodePacket(string_rek_dropped).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 191
          cls mustEqual ObjectClass.remote_electronics_kit
          guid mustEqual PlanetSideGUID(4355)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
          val dropped = data.get.asInstanceOf[DroppedItemData[_]]
          dropped.pos.coord.x mustEqual 4675.039f
          dropped.pos.coord.y mustEqual 5506.953f
          dropped.pos.coord.z mustEqual 72.703125f
          dropped.pos.orient.x mustEqual 0f
          dropped.pos.orient.y mustEqual 0f
          dropped.pos.orient.z mustEqual 230.625f
          dropped.obj.isInstanceOf[REKData] mustEqual true
          val rek = dropped.obj.asInstanceOf[REKData]
          rek.unk1 mustEqual 8
          rek.unk2 mustEqual 0
          rek.unk3 mustEqual 3
        case _ =>
          ko
      }
    }

    "encode (held)" in {
      val obj = REKData(0, 8)
      val msg = ObjectCreateMessage(ObjectClass.remote_electronics_kit, PlanetSideGUID(3893), ObjectCreateMessageParent(PlanetSideGUID(4174), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_rek_held
    }

    "encode (dropped)" in {
      val obj = DroppedItemData(
        PlacementData(4675.039f, 5506.953f, 72.703125f, 0f, 0f, 230.625f),
        REKData(8, 0, 3)
      )
      val msg = ObjectCreateMessage(ObjectClass.remote_electronics_kit, PlanetSideGUID(4355), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_rek_dropped
    }
  }
}
