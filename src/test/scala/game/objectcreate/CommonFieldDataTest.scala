// Copyright (c) 2019 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}
import org.specs2.mutable.Specification
import scodec.bits._

object CommonFieldDataTest extends Specification {
  val string_shotgunshell_dropped = hex"17 A5000000 F9A7D0D 5E269 BED5A F114 0000596000000"
  val string_implant_interface    = hex"17 6C000000 01014C93304818000000"
  val string_order_terminala      = hex"17 A5000000 B2AF30EACF1889F7A3D1200007D2000000"

  "AmmoBoxData" should {
    "decode (shotgun shells, dropped)" in {
      PacketCoding.decodePacket(string_shotgunshell_dropped).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 165
          cls mustEqual ObjectClass.shotgun_shell
          guid mustEqual PlanetSideGUID(3453)
          parent.isDefined mustEqual false
          data.isInstanceOf[DroppedItemData[_]] mustEqual true
          val drop = data.asInstanceOf[DroppedItemData[_]]
          drop.pos.coord.x mustEqual 4684.7344f
          drop.pos.coord.y mustEqual 5547.4844f
          drop.pos.coord.z mustEqual 83.765625f
          drop.pos.orient.x mustEqual 0f
          drop.pos.orient.y mustEqual 0f
          drop.pos.orient.z mustEqual 199.6875f
          drop.obj match {
            case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid) =>
              faction mustEqual PlanetSideEmpire.NEUTRAL
              bops mustEqual false
              alternate mustEqual false
              v1 mustEqual false
              v2.isEmpty mustEqual true
              v3 mustEqual false
              v4.contains(false) mustEqual true
              v5.isEmpty mustEqual true
              fguid mustEqual PlanetSideGUID(0)
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode (shotgun shells, dropped)" in {
      val obj = DroppedItemData(
        PlacementData(4684.7344f, 5547.4844f, 83.765625f, 0f, 0f, 199.6875f),
        CommonFieldData()(false)
      )
      val msg = ObjectCreateMessage(ObjectClass.shotgun_shell, PlanetSideGUID(3453), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector
      pkt mustEqual string_shotgunshell_dropped
    }
  }

  "TerminalData" should {
    "decode (implant interface)" in {
      PacketCoding.decodePacket(string_implant_interface).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 108
          cls mustEqual 0x199
          guid mustEqual PlanetSideGUID(1075)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(514)
          parent.get.slot mustEqual 1
          data.isInstanceOf[CommonFieldData] mustEqual true
        case _ =>
          ko
      }
    }

    "decode (order terminal a)" in {
      PacketCoding.decodePacket(string_order_terminala).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 165
          cls mustEqual ObjectClass.order_terminala
          guid mustEqual PlanetSideGUID(3827)
          parent.isDefined mustEqual false
          data.isInstanceOf[DroppedItemData[_]] mustEqual true
          val drop = data.asInstanceOf[DroppedItemData[_]]
          drop.pos.coord.x mustEqual 4579.3438f
          drop.pos.coord.y mustEqual 5615.0703f
          drop.pos.coord.z mustEqual 72.953125f
          drop.pos.orient.x mustEqual 0f
          drop.pos.orient.y mustEqual 0f
          drop.pos.orient.z mustEqual 98.4375f
          drop.obj.isInstanceOf[CommonFieldData] mustEqual true
          drop.obj.asInstanceOf[CommonFieldData].faction mustEqual PlanetSideEmpire.NC
        case _ =>
          ko
      }
    }

    "encode (implant interface)" in {
      val obj = CommonFieldData(PlanetSideEmpire.VS)(false)
      val msg = ObjectCreateMessage(0x199, PlanetSideGUID(1075), ObjectCreateMessageParent(PlanetSideGUID(514), 1), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector
      pkt mustEqual string_implant_interface
    }

    "encode (order terminal a)" in {
      val obj = DroppedItemData(
        PlacementData(4579.3438f, 5615.0703f, 72.953125f, 0f, 0f, 98.4375f),
        CommonFieldData(PlanetSideEmpire.NC)(false)
      )
      val msg = ObjectCreateMessage(ObjectClass.order_terminala, PlanetSideGUID(3827), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector
      pkt mustEqual string_order_terminala
    }

    "InternalSlot" in {
      TerminalData(
        ObjectClass.order_terminala,
        PlanetSideGUID(1),
        1,
        CommonFieldData(PlanetSideEmpire.NC)(false)
      ) mustEqual
        InternalSlot(ObjectClass.order_terminala, PlanetSideGUID(1), 1, CommonFieldData(PlanetSideEmpire.NC)(false))
    }
  }
}
