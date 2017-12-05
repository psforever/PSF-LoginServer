// Copyright (c) 2017 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types._
import org.specs2.mutable._
import scodec.bits._

class NormalVehiclesTest extends Specification {
  val string_fury = hex"17 50010000 A79 9D01 FBC1C 12A83 2F06 00 00 21 4400003FC00101140C800C0E40000004048F3600301900000"
  val string_lightning = hex"17 8b010000 df1 5a00 6c2d7 65535 ca16 00 00 00 4400003fc00101300ad8040c4000000408190b801018000002617402070000000"
  val string_mediumtransport = hex"17 DA010000 8A2 8301 FBC1C 12A83 2F06 00 00 21 2400003FC079020593F80C2E400000040410148030190000017458050D90000001010401F814064000000"

  "Normal vehicles" should {
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
          fury.basic.pos.orient.x mustEqual 0f
          fury.basic.pos.orient.y mustEqual 0f
          fury.basic.pos.orient.z mustEqual 357.1875f
          fury.basic.pos.vel.isDefined mustEqual false
          fury.basic.faction mustEqual PlanetSideEmpire.VS
          fury.basic.unk mustEqual 2
          fury.basic.player_guid mustEqual PlanetSideGUID(0)
          fury.health mustEqual 255
          //
          fury.inventory.isDefined mustEqual true
          fury.inventory.get.contents.size mustEqual 1
          val mounting = fury.inventory.get.contents.head
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
          lightning.basic.pos.orient.x mustEqual 0f
          lightning.basic.pos.orient.y mustEqual 0f
          lightning.basic.pos.orient.z mustEqual 90.0f
          lightning.basic.faction mustEqual PlanetSideEmpire.VS
          lightning.basic.unk mustEqual 2
          lightning.basic.player_guid mustEqual PlanetSideGUID(0)
          lightning.health mustEqual 255
          lightning.inventory.isDefined mustEqual true
          lightning.inventory.get.contents.size mustEqual 1
          val mounting = lightning.inventory.get.contents.head
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
          deliverer.basic.pos.orient.x mustEqual 0f
          deliverer.basic.pos.orient.y mustEqual 0f
          deliverer.basic.pos.orient.z mustEqual 357.1875f
          deliverer.basic.faction mustEqual PlanetSideEmpire.NC
          deliverer.basic.unk mustEqual 2
          deliverer.basic.player_guid mustEqual PlanetSideGUID(0)
          deliverer.unk1 mustEqual 0
          deliverer.health mustEqual 255
          deliverer.unk2 mustEqual false
          deliverer.driveState mustEqual DriveState.State7
          deliverer.unk3 mustEqual true
          deliverer.unk4 mustEqual None
          deliverer.unk5 mustEqual false
          deliverer.inventory.isDefined mustEqual true
          deliverer.inventory.get.contents.size mustEqual 2
          //0
          var mounting = deliverer.inventory.get.contents.head
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
          mounting = deliverer.inventory.get.contents(1)
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

    "encode (fury)" in {
      val obj = VehicleData(
        CommonFieldData(
          PlacementData(6531.961f, 1872.1406f, 24.734375f, 0f, 0f, 357.1875f),
          PlanetSideEmpire.VS, 2
        ),
        0,
        255,
        false, false,
        DriveState.Mobile,
        false, false, false,
        None,
        Some(InventoryData(
          InventoryItemData(ObjectClass.fury_weapon_systema, PlanetSideGUID(400), 1,
            WeaponData(0x6, 0x8, 0, ObjectClass.hellfire_ammo, PlanetSideGUID(432), 0, AmmoBoxData(0x8))
          ) :: Nil
        ))
      )(VehicleFormat.Normal)
      val msg = ObjectCreateMessage(ObjectClass.fury, PlanetSideGUID(413), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_fury
    }

    "encode (lightning)" in {
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
        None,
        Some(InventoryData(
          InventoryItemData(ObjectClass.lightning_weapon_system, PlanetSideGUID(91), 1,
            WeaponData(4, 8, 0, ObjectClass.bullet_75mm, PlanetSideGUID(92), 0, AmmoBoxData(), ObjectClass.bullet_25mm, PlanetSideGUID(93), 1, AmmoBoxData())
          ) :: Nil
        ))
      )(VehicleFormat.Normal)
      val msg = ObjectCreateMessage(ObjectClass.lightning, PlanetSideGUID(90), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_lightning
    }

    "encode (medium transport)" in {
      val obj = VehicleData(
        CommonFieldData(
          PlacementData(6531.961f, 1872.1406f, 24.734375f, 0f, 0f, 357.1875f),
          PlanetSideEmpire.NC, 2
        ),
        0,
        255,
        false, false,
        DriveState.State7,
        true, false, false,
        None,
        Some(InventoryData(
          InventoryItemData(ObjectClass.mediumtransport_weapon_systemA, PlanetSideGUID(383), 5,
            WeaponData(6, 8, ObjectClass.bullet_20mm, PlanetSideGUID(420), 0, AmmoBoxData(8))
          ) ::
            InventoryItemData(ObjectClass.mediumtransport_weapon_systemB, PlanetSideGUID(556), 6,
              WeaponData(6, 8, ObjectClass.bullet_20mm, PlanetSideGUID(575), 0, AmmoBoxData(8))
            ) :: Nil
        ))
      )(VehicleFormat.Normal)
      val msg = ObjectCreateMessage(ObjectClass.mediumtransport, PlanetSideGUID(387), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_mediumtransport
    }
  }
}
