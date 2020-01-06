// Copyright (c) 2017 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.objectcreate._
import net.psforever.packet.game.ObjectCreateMessage
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
          data match {
            case DroppodData(basic, health, burn, unk) =>
              basic.pos.coord mustEqual Vector3(5108.0f, 6164.0f, 1023.9844f)
              basic.pos.orient mustEqual Vector3.z(90.0f)

              basic.data.faction mustEqual PlanetSideEmpire.VS
              basic.data.bops mustEqual false
              basic.data.alternate mustEqual false
              basic.data.v1 mustEqual true
              basic.data.v2.isDefined mustEqual false
              basic.data.jammered mustEqual false
              basic.data.v5.isDefined mustEqual false
              basic.data.guid mustEqual PlanetSideGUID(0)

              burn mustEqual false
              health mustEqual 255
            case _ =>
              ko
          }
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
          data.isInstanceOf[OrbitalShuttleData] mustEqual true
          data.asInstanceOf[OrbitalShuttleData].faction mustEqual PlanetSideEmpire.VS
          data.asInstanceOf[OrbitalShuttleData].pos.isDefined mustEqual false
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
          data.isInstanceOf[OrbitalShuttleData] mustEqual true
          val shuttle = data.asInstanceOf[OrbitalShuttleData]
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
        CommonFieldDataWithPlacement(
          PlacementData(5108.0f, 6164.0f, 1023.9844f, 0f, 0f, 90.0f),
          CommonFieldData(PlanetSideEmpire.VS, 2)
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
