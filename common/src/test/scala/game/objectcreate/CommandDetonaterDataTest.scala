// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import org.specs2.mutable._
import scodec.bits._

class CommandDetonaterDataTest extends Specification {
  val string_detonater_held = hex"17 76000000 1A886A8421080400000000"
  val string_detonater_dropped = hex"17 AF000000 EA8620ED1549B4B6A741500001B000000000"

  "CommandDetonaterData" should {
    "decode (held)" in {
      PacketCoding.DecodePacket(string_detonater_held).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 118
          cls mustEqual ObjectClass.command_detonater
          guid mustEqual PlanetSideGUID(4162)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(4149)
          parent.get.slot mustEqual 0
          data.isDefined mustEqual true
          data.get.isInstanceOf[CommandDetonaterData] mustEqual true
          val cud = data.get.asInstanceOf[CommandDetonaterData]
          cud.unk1 mustEqual 4
          cud.unk2 mustEqual 0
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
          data.isDefined mustEqual true
          data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
          val drop = data.get.asInstanceOf[DroppedItemData[_]]
          drop.pos.coord.x mustEqual 4777.633f
          drop.pos.coord.y mustEqual 5485.4062f
          drop.pos.coord.z mustEqual 85.8125f
          drop.pos.orient.x mustEqual 0f
          drop.pos.orient.y mustEqual 0f
          drop.pos.orient.z mustEqual 14.0625f
          drop.obj.isInstanceOf[CommandDetonaterData] mustEqual true
        case _ =>
          ko
      }
    }

    "encode (held)" in {
      val obj = CommandDetonaterData(4)
      val msg = ObjectCreateMessage(ObjectClass.command_detonater, PlanetSideGUID(4162), ObjectCreateMessageParent(PlanetSideGUID(4149), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_detonater_held
    }

    "encode (dropped)" in {
      val obj = DroppedItemData(
        PlacementData(4777.633f, 5485.4062f, 85.8125f, 0f, 0f, 14.0625f),
        CommandDetonaterData()
      )
      val msg = ObjectCreateMessage(ObjectClass.command_detonater, PlanetSideGUID(3682), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_detonater_dropped
    }
  }
}
