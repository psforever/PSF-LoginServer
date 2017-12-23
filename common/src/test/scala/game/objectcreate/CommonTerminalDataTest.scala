// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable._
import scodec.bits._

class CommonTerminalDataTest extends Specification {
  val string_implant_interface = hex"17 6C000000 01014C93304818000000"
  val string_order_terminala = hex"17 A5000000 B2AF30EACF1889F7A3D1200007D2000000"

  "CommonTerminalData" should {
    "decode (implant interface)" in {
      PacketCoding.DecodePacket(string_implant_interface).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 108
          cls mustEqual 0x199
          guid mustEqual PlanetSideGUID(1075)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(514)
          parent.get.slot mustEqual 1
          data.isDefined mustEqual true
          data.get.isInstanceOf[CommonTerminalData] mustEqual true
          data.get.asInstanceOf[CommonTerminalData].faction mustEqual PlanetSideEmpire.VS
        case _ =>
          ko
      }
    }

    "decode (order terminal a)" in {
      PacketCoding.DecodePacket(string_order_terminala).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 165
          cls mustEqual ObjectClass.order_terminala
          guid mustEqual PlanetSideGUID(3827)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
          val drop = data.get.asInstanceOf[DroppedItemData[_]]
          drop.pos.coord.x mustEqual 4579.3438f
          drop.pos.coord.y mustEqual 5615.0703f
          drop.pos.coord.z mustEqual 72.953125f
          drop.pos.orient.x mustEqual 0f
          drop.pos.orient.y mustEqual 0f
          drop.pos.orient.z mustEqual 98.4375f
          drop.obj.isInstanceOf[CommonTerminalData] mustEqual true
          val term = drop.obj.asInstanceOf[CommonTerminalData]
          term.faction mustEqual PlanetSideEmpire.NC
          term.unk mustEqual 0
        case _ =>
          ko
      }
    }

    "encode (implant interface)" in {
      val obj = CommonTerminalData(PlanetSideEmpire.VS)
      val msg = ObjectCreateMessage(0x199, PlanetSideGUID(1075), ObjectCreateMessageParent(PlanetSideGUID(514), 1), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_implant_interface
    }

    "encode (order terminal a)" in {
      val obj = DroppedItemData(
        PlacementData(4579.3438f, 5615.0703f, 72.953125f, 0f, 0f, 98.4375f),
        CommonTerminalData(PlanetSideEmpire.NC)
      )
      val msg = ObjectCreateMessage(ObjectClass.order_terminala, PlanetSideGUID(3827), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_order_terminala
    }

    "InternalSlot" in {
      CommonTerminalData(ObjectClass.order_terminala, PlanetSideGUID(1), 1, CommonTerminalData(PlanetSideEmpire.NC)) mustEqual
        InternalSlot(ObjectClass.order_terminala, PlanetSideGUID(1), 1, CommonTerminalData(PlanetSideEmpire.NC))
    }
  }
}
