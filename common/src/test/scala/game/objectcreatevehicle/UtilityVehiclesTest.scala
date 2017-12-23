// Copyright (c) 2017 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types._
import org.specs2.mutable._
import scodec.bits._

class UtilityVehiclesTest extends Specification {
  val string_ant =  hex"17 C2000000 9E0 7C01 6C2D7 65535 CA16 00 00 00 4400003FC000000"
  val string_ams = hex"17 B8010000 970 3D10 002D765535CA16000000 402285BB0037E4100749E1D03000000620D83A0A00000195798741C00000332E40D84800000"

  "Utility vehicles" should {
    "decode (ant)" in {
      PacketCoding.DecodePacket(string_ant).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 194L
          cls mustEqual ObjectClass.ant
          guid mustEqual PlanetSideGUID(380)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[VehicleData] mustEqual true
          val ant = data.get.asInstanceOf[VehicleData]
          ant.basic.pos.coord.x mustEqual 3674.8438f
          ant.basic.pos.coord.y mustEqual 2726.789f
          ant.basic.pos.coord.z mustEqual 91.15625f
          ant.basic.pos.orient.x mustEqual 0f
          ant.basic.pos.orient.y mustEqual 0f
          ant.basic.pos.orient.z mustEqual 90.0f
          ant.basic.faction mustEqual PlanetSideEmpire.VS
          ant.basic.unk mustEqual 2
          ant.basic.player_guid mustEqual PlanetSideGUID(0)
          ant.health mustEqual 255
          ant.driveState mustEqual DriveState.Mobile
        case _ =>
          ko
      }
    }

    "decode (ams)" in {
      PacketCoding.DecodePacket(string_ams).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 440L
          cls mustEqual ObjectClass.ams
          guid mustEqual PlanetSideGUID(4157)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[VehicleData] mustEqual true
          val ams = data.get.asInstanceOf[VehicleData]
          ams.basic.pos.coord.x mustEqual 3674.0f
          ams.basic.pos.coord.y mustEqual 2726.789f
          ams.basic.pos.coord.z mustEqual 91.15625f
          ams.basic.pos.orient.x mustEqual 0f
          ams.basic.pos.orient.y mustEqual 0f
          ams.basic.pos.orient.z mustEqual 90.0f
          ams.basic.faction mustEqual PlanetSideEmpire.VS
          ams.basic.unk mustEqual 0
          ams.basic.player_guid mustEqual PlanetSideGUID(34082)
          ams.unk1 mustEqual 2
          ams.health mustEqual 236
          ams.unk2 mustEqual false
          ams.driveState mustEqual DriveState.Deployed

          ams.inventory.isDefined mustEqual true
          val inv = ams.inventory.get.contents
          inv.head.objectClass mustEqual ObjectClass.matrix_terminalc
          inv.head.guid mustEqual PlanetSideGUID(3663)
          inv.head.parentSlot mustEqual 1
          inv.head.obj.isInstanceOf[CommonTerminalData] mustEqual true
          inv(1).objectClass mustEqual ObjectClass.ams_respawn_tube
          inv(1).guid mustEqual PlanetSideGUID(3638)
          inv(1).parentSlot mustEqual 2
          inv(1).obj.isInstanceOf[CommonTerminalData] mustEqual true
          inv(2).objectClass mustEqual ObjectClass.order_terminala
          inv(2).guid mustEqual PlanetSideGUID(3827)
          inv(2).parentSlot mustEqual 3
          inv(2).obj.isInstanceOf[CommonTerminalData] mustEqual true
          inv(3).objectClass mustEqual ObjectClass.order_terminalb
          inv(3).guid mustEqual PlanetSideGUID(3556)
          inv(3).parentSlot mustEqual 4
          inv(3).obj.isInstanceOf[CommonTerminalData] mustEqual true
        case _ =>
          ko
      }
    }

    "encode (ant)" in {
      val obj = VehicleData(
        CommonFieldData(
          PlacementData(3674.8438f, 2726.789f, 91.15625f, 0f, 0f, 90.0f),
          PlanetSideEmpire.VS, 2
        ),
        0,
        255,
        false, false,
        DriveState.Mobile,
        false, false, false,
        Some(UtilityVehicleData(0)),
        None
      )(VehicleFormat.Utility)
      val msg = ObjectCreateMessage(ObjectClass.ant, PlanetSideGUID(380), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_ant
    }

    "encode (ams)" in {
      val obj =  VehicleData(
        CommonFieldData(
          PlacementData(3674.0f, 2726.789f, 91.15625f, 0f, 0f, 90.0f),
          PlanetSideEmpire.VS, 0,
          PlanetSideGUID(34082)
        ),
        2,
        236,
        false, false,
        DriveState.Deployed,
        false, true, true,
        Some(UtilityVehicleData(60)), //what does this mean?
        Some(InventoryData(List(
          InternalSlot(ObjectClass.matrix_terminalc, PlanetSideGUID(3663), 1, CommonTerminalData(PlanetSideEmpire.VS)),
          InternalSlot(ObjectClass.ams_respawn_tube, PlanetSideGUID(3638), 2, CommonTerminalData(PlanetSideEmpire.VS)),
          InternalSlot(ObjectClass.order_terminala, PlanetSideGUID(3827), 3, CommonTerminalData(PlanetSideEmpire.VS)),
          InternalSlot(ObjectClass.order_terminalb, PlanetSideGUID(3556), 4, CommonTerminalData(PlanetSideEmpire.VS))
        )))
      )(VehicleFormat.Utility)
      val msg = ObjectCreateMessage(ObjectClass.ams, PlanetSideGUID(4157), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_ams
    }
  }
}
