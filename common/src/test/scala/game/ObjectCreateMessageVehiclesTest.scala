// Copyright (c) 2017 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateMessage, _}
import net.psforever.packet.game.objectcreate.{DriveState, _}
import net.psforever.types._
import org.specs2.mutable._
import scodec.bits._

class ObjectCreateMessageVehiclesTest extends Specification {
  val string_fury = hex"17 50010000 A79 9D01 FBC1C 12A83 2F06 00 00 21 4400003FC00101140C800C0E40000004048F3600301900000"
  val string_ant =  hex"17 C2000000 9E0 7C01 6C2D7 65535 CA16 00 00 00 4400003FC000000"
  val string_lightning = hex"17 8b010000 df1 5a00 6c2d7 65535 ca16 00 00 00 4400003fc00101300ad8040c4000000408190b801018000002617402070000000"
  val string_mediumtransport = hex"17 DA010000 8A2 8301 FBC1C 12A83 2F06 00 00 21 2400003FC079020593F80C2E400000040410148030190000017458050D90000001010401F814064000000"
  val string_ams = hex"17 B8010000 970 3D10 002D765535CA16000000 402285BB0037E4100749E1D03000000620D83A0A00000195798741C00000332E40D84800000"
  val string_ams_destroyed = hex"17 8D000000 978 3D10 002D765535CA16000000 0"
  val string_switchblade = hex"17 93010000 A7B A201 FBC1C12A832F06000021 4400003FC00001013AD3180C0E4000000408330DC03019000006620406072000000"
  val string_droppod = hex"17 C1000000 8110B0E00FA9000ACFFFF000000 4400007F83C0900"
  val string_orbital_shuttle_1 = hex"17 82000000 0901B026904838000001FE0700"
  val string_orbital_shuttle_2 = hex"17 C3000000 B02670402F5AA14F88C210000604000007F8FF03C0"

  "decode (fury)" in {
    PacketCoding.DecodePacket(string_fury).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 336
        cls mustEqual ObjectClass.fury
        guid mustEqual PlanetSideGUID(413)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[VehicleData] mustEqual true
        val fury = data.get.asInstanceOf[VehicleData]
        fury.basic.pos.coord.x mustEqual 6531.961f
        fury.basic.pos.coord.y mustEqual 1872.1406f
        fury.basic.pos.coord.z mustEqual 24.734375f
        fury.basic.pos.roll mustEqual 0
        fury.basic.pos.pitch mustEqual 0
        fury.basic.pos.yaw mustEqual 33
        fury.basic.pos.vel.isDefined mustEqual false
        fury.basic.faction mustEqual PlanetSideEmpire.VS
        fury.basic.unk mustEqual 4
        fury.basic.player_guid mustEqual PlanetSideGUID(0)
        fury.health mustEqual 255
        //
        fury.mountings.isDefined mustEqual true
        fury.mountings.get.size mustEqual 1
        val mounting = fury.mountings.get.head
        mounting.objectClass mustEqual ObjectClass.fury_weapon_systema
        mounting.guid mustEqual PlanetSideGUID(400)
        mounting.parentSlot mustEqual 1
        mounting.obj.isInstanceOf[WeaponData] mustEqual true
        val weapon = mounting.obj.asInstanceOf[WeaponData]
        weapon.unk1 mustEqual 0x6
        weapon.unk2 mustEqual 0x8
        weapon.fire_mode mustEqual 0
        weapon.ammo.size mustEqual 1
        val ammo = weapon.ammo.head
        ammo.objectClass mustEqual ObjectClass.hellfire_ammo
        ammo.guid mustEqual PlanetSideGUID(432)
        ammo.parentSlot mustEqual 0
        ammo.obj.isInstanceOf[AmmoBoxData] mustEqual true
        ammo.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0x8
      case _ =>
        ko
    }
  }

  "decode (ant)" in {
    PacketCoding.DecodePacket(string_ant).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 194L
        cls mustEqual ObjectClass.ant
        guid mustEqual PlanetSideGUID(380)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[ANTData] mustEqual true
        val ant = data.get.asInstanceOf[ANTData]
        ant.basic.pos.coord.x mustEqual 3674.8438f
        ant.basic.pos.coord.y mustEqual 2726.789f
        ant.basic.pos.coord.z mustEqual 91.15625f
        ant.basic.pos.roll mustEqual 0
        ant.basic.pos.pitch mustEqual 0
        ant.basic.pos.yaw mustEqual 0
        ant.basic.faction mustEqual PlanetSideEmpire.VS
        ant.basic.unk mustEqual 4
        ant.basic.player_guid mustEqual PlanetSideGUID(0)
        ant.health mustEqual 255
        ant.driveState mustEqual DriveState.Mobile
      case _ =>
        ko
    }
  }

  "decode (lightning)" in {
    PacketCoding.DecodePacket(string_lightning).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 395L
        cls mustEqual ObjectClass.lightning
        guid mustEqual PlanetSideGUID(90)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[VehicleData] mustEqual true
        val lightning = data.get.asInstanceOf[VehicleData]
        lightning.basic.pos.coord.x mustEqual 3674.8438f
        lightning.basic.pos.coord.y mustEqual 2726.789f
        lightning.basic.pos.coord.z mustEqual 91.15625f
        lightning.basic.pos.roll mustEqual 0
        lightning.basic.pos.pitch mustEqual 0
        lightning.basic.pos.yaw mustEqual 0
        lightning.basic.faction mustEqual PlanetSideEmpire.VS
        lightning.basic.unk mustEqual 4
        lightning.basic.player_guid mustEqual PlanetSideGUID(0)
        lightning.health mustEqual 255
        lightning.mountings.isDefined mustEqual true
        lightning.mountings.get.size mustEqual 1
        val mounting = lightning.mountings.get.head
        mounting.objectClass mustEqual ObjectClass.lightning_weapon_system
        mounting.guid mustEqual PlanetSideGUID(91)
        mounting.parentSlot mustEqual 1
        mounting.obj.isInstanceOf[WeaponData] mustEqual true
        val weapon = mounting.obj.asInstanceOf[WeaponData]
        weapon.unk1 mustEqual 0x4
        weapon.unk2 mustEqual 0x8
        weapon.fire_mode mustEqual 0
        weapon.ammo.size mustEqual 2
        //0
        var ammo = weapon.ammo.head
        ammo.objectClass mustEqual ObjectClass.bullet_75mm
        ammo.guid mustEqual PlanetSideGUID(92)
        ammo.parentSlot mustEqual 0
        ammo.obj.isInstanceOf[AmmoBoxData] mustEqual true
        ammo.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0x0
        //1
        ammo = weapon.ammo(1)
        ammo.objectClass mustEqual ObjectClass.bullet_25mm
        ammo.guid mustEqual PlanetSideGUID(93)
        ammo.parentSlot mustEqual 1
        ammo.obj.isInstanceOf[AmmoBoxData] mustEqual true
        ammo.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0x0
      case _ =>
        ko
    }
  }

  "decode (medium transport)" in {
    PacketCoding.DecodePacket(string_mediumtransport).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 474L
        cls mustEqual ObjectClass.mediumtransport
        guid mustEqual PlanetSideGUID(387)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[VehicleData] mustEqual true
        val deliverer = data.get.asInstanceOf[VehicleData]
        deliverer.basic.pos.coord.x mustEqual 6531.961f
        deliverer.basic.pos.coord.y mustEqual 1872.1406f
        deliverer.basic.pos.coord.z mustEqual 24.734375f
        deliverer.basic.pos.roll mustEqual 0
        deliverer.basic.pos.pitch mustEqual 0
        deliverer.basic.pos.yaw mustEqual 33
        deliverer.basic.faction mustEqual PlanetSideEmpire.NC
        deliverer.basic.unk mustEqual 4
        deliverer.basic.player_guid mustEqual PlanetSideGUID(0)
        deliverer.unk1 mustEqual 0
        deliverer.health mustEqual 255
        deliverer.unk2 mustEqual 0
        deliverer.driveState mustEqual DriveState.State7
        deliverer.unk4 mustEqual true
        deliverer.unk5 mustEqual 0
        deliverer.mountings.isDefined mustEqual true
        deliverer.mountings.get.size mustEqual 2
        //0
        var mounting = deliverer.mountings.get.head
        mounting.objectClass mustEqual ObjectClass.mediumtransport_weapon_systemA
        mounting.guid mustEqual PlanetSideGUID(383)
        mounting.parentSlot mustEqual 5
        mounting.obj.isInstanceOf[WeaponData] mustEqual true
        var weapon = mounting.obj.asInstanceOf[WeaponData]
        weapon.unk1 mustEqual 0x6
        weapon.unk2 mustEqual 0x8
        weapon.fire_mode mustEqual 0
        weapon.ammo.size mustEqual 1
        var ammo = weapon.ammo.head
        ammo.objectClass mustEqual ObjectClass.bullet_20mm
        ammo.guid mustEqual PlanetSideGUID(420)
        ammo.parentSlot mustEqual 0
        ammo.obj.isInstanceOf[AmmoBoxData] mustEqual true
        ammo.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0x8
        //1
        mounting = deliverer.mountings.get(1)
        mounting.objectClass mustEqual ObjectClass.mediumtransport_weapon_systemB
        mounting.guid mustEqual PlanetSideGUID(556)
        mounting.parentSlot mustEqual 6
        mounting.obj.isInstanceOf[WeaponData] mustEqual true
        weapon = mounting.obj.asInstanceOf[WeaponData]
        weapon.unk1 mustEqual 0x6
        weapon.unk2 mustEqual 0x8
        weapon.fire_mode mustEqual 0
        weapon.ammo.size mustEqual 1
        ammo = weapon.ammo.head
        ammo.objectClass mustEqual ObjectClass.bullet_20mm
        ammo.guid mustEqual PlanetSideGUID(575)
        ammo.parentSlot mustEqual 0
        ammo.obj.isInstanceOf[AmmoBoxData] mustEqual true
        ammo.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0x8
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
        data.get.isInstanceOf[AMSData] mustEqual true
        val ams = data.get.asInstanceOf[AMSData]
        ams.basic.pos.coord.x mustEqual 3674.0f
        ams.basic.pos.coord.y mustEqual 2726.789f
        ams.basic.pos.coord.z mustEqual 91.15625f
        ams.basic.pos.roll mustEqual 0
        ams.basic.pos.pitch mustEqual 0
        ams.basic.pos.yaw mustEqual 0
        ams.basic.faction mustEqual PlanetSideEmpire.VS
        ams.basic.unk mustEqual 0
        ams.basic.player_guid mustEqual PlanetSideGUID(34082)
        ams.unk1 mustEqual 2
        ams.health mustEqual 236
        ams.unk2 mustEqual 0
        ams.driveState mustEqual DriveState.Deployed
        ams.matrix_guid mustEqual PlanetSideGUID(3663)
        ams.respawn_guid mustEqual PlanetSideGUID(3638)
        ams.term_a_guid mustEqual PlanetSideGUID(3827)
        ams.term_b_guid mustEqual PlanetSideGUID(3556)
      case _ =>
        ko
    }
  }

  "decode (ams, destroyed)" in {
    PacketCoding.DecodePacket(string_ams_destroyed).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 141L
        cls mustEqual ObjectClass.ams_destroyed
        guid mustEqual PlanetSideGUID(4157)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[DestroyedVehicleData] mustEqual true
        val dams = data.get.asInstanceOf[DestroyedVehicleData]
        dams.pos.coord.x mustEqual 3674.0f
        dams.pos.coord.y mustEqual 2726.789f
        dams.pos.coord.z mustEqual 91.15625f
        dams.pos.roll mustEqual 0
        dams.pos.pitch mustEqual 0
        dams.pos.yaw mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (switchblade)" in {
    PacketCoding.DecodePacket(string_switchblade).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 403L
        cls mustEqual ObjectClass.switchblade
        guid mustEqual PlanetSideGUID(418)
        parent.isDefined mustEqual false
        data.isDefined mustEqual true
        data.get.isInstanceOf[Vehicle2Data] mustEqual true
        val switchblade = data.get.asInstanceOf[Vehicle2Data]
        switchblade.basic.pos.coord.x mustEqual 6531.961f
        switchblade.basic.pos.coord.y mustEqual 1872.1406f
        switchblade.basic.pos.coord.z mustEqual 24.734375f
        switchblade.basic.pos.roll mustEqual 0
        switchblade.basic.pos.pitch mustEqual 0
        switchblade.basic.pos.yaw mustEqual 33
        switchblade.basic.faction mustEqual PlanetSideEmpire.VS
        switchblade.basic.unk mustEqual 4
        switchblade.health mustEqual 255
        switchblade.driveState mustEqual DriveState.Mobile
        switchblade.mountings.isDefined mustEqual true
        switchblade.mountings.get.size mustEqual 1
        //0
        val weapon = switchblade.mountings.get.head
        weapon.objectClass mustEqual ObjectClass.scythe
        weapon.guid mustEqual PlanetSideGUID(355)
        weapon.parentSlot mustEqual 1
        weapon.obj.asInstanceOf[WeaponData].unk1 mustEqual 0x6
        weapon.obj.asInstanceOf[WeaponData].unk2 mustEqual 0x8
        weapon.obj.asInstanceOf[WeaponData].ammo.size mustEqual 2
        //ammo-0
        var ammo = weapon.obj.asInstanceOf[WeaponData].ammo.head
        ammo.objectClass mustEqual ObjectClass.ancient_ammo_vehicle
        ammo.guid mustEqual PlanetSideGUID(366)
        ammo.parentSlot mustEqual 0
        ammo.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0x8
        //ammo-1
        ammo = weapon.obj.asInstanceOf[WeaponData].ammo(1)
        ammo.objectClass mustEqual ObjectClass.ancient_ammo_vehicle
        ammo.guid mustEqual PlanetSideGUID(385)
        ammo.parentSlot mustEqual 1
        ammo.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0x8
      case _ =>
        ko
    }
  }

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
        droppod.basic.pos.roll mustEqual 0
        droppod.basic.pos.pitch mustEqual 0
        droppod.basic.pos.yaw mustEqual 0
        droppod.basic.unk mustEqual 4
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
        shuttle.pos.get.roll mustEqual 0
        shuttle.pos.get.pitch mustEqual 0
        shuttle.pos.get.yaw mustEqual 96
      case _ =>
        ko
    }
  }

  "encode (fury)" in {
    val obj = VehicleData(
      CommonFieldData(
        PlacementData(6531.961f, 1872.1406f, 24.734375f, 0, 0, 33),
        PlanetSideEmpire.VS, 4
      ),
      255,
      MountItem(ObjectClass.fury_weapon_systema, PlanetSideGUID(400), 1,
        WeaponData(0x6, 0x8, 0, ObjectClass.hellfire_ammo, PlanetSideGUID(432), 0, AmmoBoxData(0x8))
      )
    )
    val msg = ObjectCreateMessage(ObjectClass.fury, PlanetSideGUID(413), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_fury
  }

  "encode (ant)" in {
    val obj = ANTData(
      CommonFieldData(
        PlacementData(3674.8438f, 2726.789f, 91.15625f),
        PlanetSideEmpire.VS, 4
      ),
      255,
      DriveState.Mobile
    )
    val msg = ObjectCreateMessage(ObjectClass.ant, PlanetSideGUID(380), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_ant
  }

  "encode (lightning)" in {
    val obj = VehicleData(
      CommonFieldData(
        PlacementData(3674.8438f, 2726.789f, 91.15625f),
        PlanetSideEmpire.VS, 4
      ),
      255,
      MountItem(ObjectClass.lightning_weapon_system, PlanetSideGUID(91), 1,
        WeaponData(4, 8, 0, ObjectClass.bullet_75mm, PlanetSideGUID(92), 0, AmmoBoxData(), ObjectClass.bullet_25mm, PlanetSideGUID(93), 1, AmmoBoxData())
      )
    )
    val msg = ObjectCreateMessage(ObjectClass.lightning, PlanetSideGUID(90), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_lightning
  }

  "encode (deliverer)" in {
    val obj = VehicleData(
      CommonFieldData(
        PlacementData(6531.961f, 1872.1406f, 24.734375f, 0, 0, 33),
        PlanetSideEmpire.NC, 4
      ),
      0,
      255,
      0,
      DriveState.State7,
      true,
      0,
      Some(
        MountItem(
          ObjectClass.mediumtransport_weapon_systemA, PlanetSideGUID(383), 5,
          WeaponData(6, 8, ObjectClass.bullet_20mm, PlanetSideGUID(420), 0, AmmoBoxData(8))
        ) ::
          MountItem(
            ObjectClass.mediumtransport_weapon_systemB, PlanetSideGUID(556), 6,
            WeaponData(6, 8, ObjectClass.bullet_20mm, PlanetSideGUID(575), 0, AmmoBoxData(8))
          ) ::
          Nil
        )
      )(2)
    val msg = ObjectCreateMessage(ObjectClass.mediumtransport, PlanetSideGUID(387), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_mediumtransport
  }

  "encode (ams)" in {
    val obj = AMSData(
      CommonFieldData(PlacementData(3674.0f, 2726.789f, 91.15625f, 0, 0, 0),
        PlanetSideEmpire.VS, 0,
        PlanetSideGUID(34082)
      ),
      2,
      236,
      0,
      DriveState.Deployed,
      63,
      PlanetSideGUID(3663),
      PlanetSideGUID(3638),
      PlanetSideGUID(3827),
      PlanetSideGUID(3556)
    )
    val msg = ObjectCreateMessage(ObjectClass.ams, PlanetSideGUID(4157), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_ams
  }

  "encode (ams, destroyed)" in {
    val obj = DestroyedVehicleData(PlacementData(3674.0f, 2726.789f, 91.15625f))
    val msg = ObjectCreateMessage(ObjectClass.ams_destroyed, PlanetSideGUID(4157), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_ams_destroyed
  }

  "encode (switchblade(" in {
    val obj = Vehicle2Data(
      CommonFieldData(PlacementData(6531.961f, 1872.1406f, 24.734375f ,0 ,0 ,33),
        PlanetSideEmpire.VS, 4
      ),
      255,
      DriveState.Mobile,
      MountItem(ObjectClass.scythe, PlanetSideGUID(355), 1,
        WeaponData(0x6, 0x8, 0, ObjectClass.ancient_ammo_vehicle, PlanetSideGUID(366), 0, AmmoBoxData(0x8), ObjectClass.ancient_ammo_vehicle, PlanetSideGUID(385), 1, AmmoBoxData(0x8))
      )
    )
    val msg = ObjectCreateMessage(ObjectClass.switchblade, PlanetSideGUID(418), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_switchblade
  }

  "encode (droppod)" in {
    val obj = DroppodData(
      CommonFieldData(
        PlacementData(5108.0f, 6164.0f, 1023.9844f),
        PlanetSideEmpire.VS, 4
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
    val obj = OrbitalShuttleData(PlacementData(5610.0156f, 4255.258f, 134.1875f, 0, 0, 96), PlanetSideEmpire.VS)
    val msg = ObjectCreateMessage(ObjectClass.orbital_shuttle, PlanetSideGUID(1127), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_orbital_shuttle_2
  }
}
