// Copyright (c) 2017 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types._
import org.specs2.mutable._
import scodec.bits._

class NonstandardVehiclesTest extends Specification {
  val string_droppod = hex"17 C1000000 8110B0E00FA9000ACFFFF000000 4400007F83C0900"
  val string_orbital_shuttle_1 = hex"17 82000000 0901B026904838000001FE0700"
  val string_orbital_shuttle_2 = hex"17 C3000000 B02670402F5AA14F88C210000604000007F8FF03C0"

  "Nonstandard vehicles" should {
    "decode (droppod)" in {
      PacketCoding.DecodePacket(string_droppod).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 193L
          cls mustEqual ObjectClass.droppod
          guid mustEqual PlanetSideGUID(3595)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[DroppodData] mustEqual true
          val droppod = data.get.asInstanceOf[DroppodData]
          droppod.basic.pos.coord.x mustEqual 5108.0f
          droppod.basic.pos.coord.y mustEqual 6164.0f
          droppod.basic.pos.coord.z mustEqual 1023.9844f
          droppod.basic.pos.orient.x mustEqual 0f
          droppod.basic.pos.orient.y mustEqual 0f
          droppod.basic.pos.orient.z mustEqual 90.0f
          droppod.basic.unk mustEqual 2
          droppod.basic.player_guid mustEqual PlanetSideGUID(0)
          droppod.burn mustEqual false
          droppod.health mustEqual 255
        case _ =>
          ko
      }
    }

    "decode (shuttle 1)" in {
      PacketCoding.DecodePacket(string_orbital_shuttle_1).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 130
          cls mustEqual ObjectClass.orbital_shuttle
          guid mustEqual PlanetSideGUID(1129)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(786)
          parent.get.slot mustEqual 3
          data.isDefined mustEqual true
          data.get.isInstanceOf[OrbitalShuttleData] mustEqual true
          data.get.asInstanceOf[OrbitalShuttleData].faction mustEqual PlanetSideEmpire.VS
          data.get.asInstanceOf[OrbitalShuttleData].pos.isDefined mustEqual false
        case _ =>
          ko
      }
    }

    "decode (shuttle 2)" in {
      PacketCoding.DecodePacket(string_orbital_shuttle_2).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 195
          cls mustEqual ObjectClass.orbital_shuttle
          guid mustEqual PlanetSideGUID(1127)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[OrbitalShuttleData] mustEqual true
          val shuttle = data.get.asInstanceOf[OrbitalShuttleData]
          shuttle.faction mustEqual PlanetSideEmpire.VS
          shuttle.pos.isDefined mustEqual true
          shuttle.pos.get.coord.x mustEqual 5610.0156f
          shuttle.pos.get.coord.y mustEqual 4255.258f
          shuttle.pos.get.coord.z mustEqual 134.1875f
          shuttle.pos.get.orient.x mustEqual 0f
          shuttle.pos.get.orient.y mustEqual 0f
          shuttle.pos.get.orient.z mustEqual 180.0f
        case _ =>
          ko
      }
    }

    "encode (droppod)" in {
      val obj = DroppodData(
        CommonFieldData(
          PlacementData(5108.0f, 6164.0f, 1023.9844f, 0f, 0f, 90.0f),
          PlanetSideEmpire.VS,
          2
        )
      )
      val msg = ObjectCreateMessage(ObjectClass.droppod, PlanetSideGUID(3595), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_droppod
    }

    "encode (shuttle 1)" in {
      val obj = OrbitalShuttleData(PlanetSideEmpire.VS)
      val msg = ObjectCreateMessage(ObjectClass.orbital_shuttle, PlanetSideGUID(1129), ObjectCreateMessageParent(PlanetSideGUID(786), 3), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_orbital_shuttle_1
    }

    "encode (shuttle 2)" in {
      val obj = OrbitalShuttleData(PlacementData(5610.0156f, 4255.258f, 134.1875f, 0f, 0f, 180.0f), PlanetSideEmpire.VS)
      val msg = ObjectCreateMessage(ObjectClass.orbital_shuttle, PlanetSideGUID(1127), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_orbital_shuttle_2
    }
  }
}
