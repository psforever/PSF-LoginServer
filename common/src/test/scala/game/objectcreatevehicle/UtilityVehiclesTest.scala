// Copyright (c) 2017 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.objectcreate._
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.types._
import org.specs2.mutable._
import scodec.bits._

class UtilityVehiclesTest extends Specification {
  val string_ant =  hex"17 C2000000 9E0 7C01 6C2D7 65535 CA16 00 00 00 4400003FC000000"
  val string_ams = hex"17 B8010000 970 3D10 002D765535CA16000000 402285BB0037E4100749E1D03000000620D83A0A00000195798741C00000332E40D84800000"
//  val string_ams_seated =
//    hex"17ec060000970fe0f030898abda28127f007ff9c1f2f80c0001e18ff00001051e40786400000008c50004c0041006d0069006e006700790075006500540052007c00000304217c859e8080000000000000002503420022c02a002a002a002a0050004c0041002a002a002a002a00010027e300940000016c0400023c040002285a086c2f00c80000000000300210288740800000004046f17423018000002c4d6190400000001010704a86406000002bc770842000000004041c5f21d01800000e075821902000000623e84208000001950588c1800000332ea0f840000000"

  "Utility vehicles" should {
    "decode (ant)" in {
      PacketCoding.DecodePacket(string_ant).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 194L
          cls mustEqual ObjectClass.ant
          guid mustEqual PlanetSideGUID(380)
          parent.isDefined mustEqual false
          data.isInstanceOf[VehicleData] mustEqual true
          val ant = data.asInstanceOf[VehicleData]
          ant.pos.coord mustEqual Vector3(3674.8438f, 2726.789f, 91.15625f)
          ant.pos.orient mustEqual Vector3(0, 0, 90)
          ant.data.faction mustEqual PlanetSideEmpire.VS
          ant.data.alternate mustEqual false
          ant.data.v1 mustEqual true
          ant.data.jammered mustEqual false
          ant.data.v5.isEmpty mustEqual true
          ant.data.guid mustEqual PlanetSideGUID(0)
          ant.driveState mustEqual DriveState.Mobile
          ant.health mustEqual 255
          ant.cloak mustEqual false
          ant.unk3 mustEqual false
          ant.unk4 mustEqual false
          ant.unk5 mustEqual false
          ant.unk6 mustEqual false
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
          data.isInstanceOf[VehicleData] mustEqual true
          val ams = data.asInstanceOf[VehicleData]
          ams.pos.coord mustEqual Vector3(3674, 2726.789f, 91.15625f)
          ams.pos.orient mustEqual Vector3(0, 0, 90)
          ams.pos.vel mustEqual None
          ams.data.faction mustEqual PlanetSideEmpire.VS
          ams.data.alternate mustEqual false
          ams.data.v1 mustEqual false
          ams.data.jammered mustEqual false
          ams.data.v5.isEmpty mustEqual true
          ams.data.guid mustEqual PlanetSideGUID(2885)
          ams.driveState mustEqual DriveState.Deployed
          ams.vehicle_format_data mustEqual Some(UtilityVehicleData(60))
          ams.health mustEqual 236
          ams.cloak mustEqual true
          ams.unk3 mustEqual false
          ams.unk4 mustEqual false
          ams.unk5 mustEqual false
          ams.unk6 mustEqual true

          ams.inventory.isDefined mustEqual true
          val inv = ams.inventory.get.contents
          inv.head.objectClass mustEqual ObjectClass.matrix_terminalc
          inv.head.guid mustEqual PlanetSideGUID(3663)
          inv.head.parentSlot mustEqual 1
          inv.head.obj.isInstanceOf[CommonFieldData] mustEqual true
          inv(1).objectClass mustEqual ObjectClass.ams_respawn_tube
          inv(1).guid mustEqual PlanetSideGUID(3638)
          inv(1).parentSlot mustEqual 2
          inv(1).obj.isInstanceOf[CommonFieldData] mustEqual true
          inv(2).objectClass mustEqual ObjectClass.order_terminala
          inv(2).guid mustEqual PlanetSideGUID(3827)
          inv(2).parentSlot mustEqual 3
          inv(2).obj.isInstanceOf[CommonFieldData] mustEqual true
          inv(3).objectClass mustEqual ObjectClass.order_terminalb
          inv(3).guid mustEqual PlanetSideGUID(3556)
          inv(3).parentSlot mustEqual 4
          inv(3).obj.isInstanceOf[CommonFieldData] mustEqual true
        case _ =>
          ko
      }
    }

    "encode (ant)" in {
      val obj = VehicleData(
        PlacementData(3674.8438f, 2726.789f, 91.15625f, 0f, 0f, 90.0f),
        CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)),
        false,
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
        PlacementData(3674.0f, 2726.789f, 91.15625f, 0f, 0f, 90.0f),
        CommonFieldData(PlanetSideEmpire.VS, false, false, false, None, false, Some(false), None, PlanetSideGUID(2885)),
        false,
        236,
        false, false,
        DriveState.Deployed,
        false, true, true,
        Some(UtilityVehicleData(60)), //what does this mean?
        Some(InventoryData(List(
          InternalSlot(ObjectClass.matrix_terminalc, PlanetSideGUID(3663), 1, CommonFieldData(PlanetSideEmpire.VS)(false)),
          InternalSlot(ObjectClass.ams_respawn_tube, PlanetSideGUID(3638), 2, CommonFieldData(PlanetSideEmpire.VS)(false)),
          InternalSlot(ObjectClass.order_terminala, PlanetSideGUID(3827), 3, CommonFieldData(PlanetSideEmpire.VS)(false)),
          InternalSlot(ObjectClass.order_terminalb, PlanetSideGUID(3556), 4, CommonFieldData(PlanetSideEmpire.VS)(false))
        )))
      )(VehicleFormat.Utility)
      val msg = ObjectCreateMessage(ObjectClass.ams, PlanetSideGUID(4157), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_ams
    }
  }
}
