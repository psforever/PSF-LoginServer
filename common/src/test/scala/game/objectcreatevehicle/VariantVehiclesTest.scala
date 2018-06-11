// Copyright (c) 2017 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types._
import org.specs2.mutable._
import scodec.bits._

class VariantVehiclesTest extends Specification {
  val string_switchblade = hex"17 93010000 A7B A201 FBC1C12A832F06000021 4400003FC00001013AD3180C0E4000000408330DC03019000006620406072000000"

  "Variant vehicles" should {
    "decode (switchblade)" in {
      PacketCoding.DecodePacket(string_switchblade).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 403L
          cls mustEqual ObjectClass.switchblade
          guid mustEqual PlanetSideGUID(418)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[VehicleData] mustEqual true
          val switchblade = data.get.asInstanceOf[VehicleData]
          switchblade.pos.coord.x mustEqual 6531.961f
          switchblade.pos.coord.y mustEqual 1872.1406f
          switchblade.pos.coord.z mustEqual 24.734375f
          switchblade.pos.orient.x mustEqual 0f
          switchblade.pos.orient.y mustEqual 0f
          switchblade.pos.orient.z mustEqual 357.1875f
          switchblade.faction mustEqual PlanetSideEmpire.VS
          switchblade.unk1 mustEqual 2
          switchblade.health mustEqual 255
          switchblade.driveState mustEqual DriveState.Mobile
          switchblade.inventory.isDefined mustEqual true
          switchblade.inventory.get.contents.size mustEqual 1
          //0
          val weapon = switchblade.inventory.get.contents.head
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

    "encode (switchblade)" in {
      val obj = VehicleData(
        PlacementData(6531.961f, 1872.1406f, 24.734375f, 0f, 0f, 357.1875f),
        PlanetSideEmpire.VS,
        false, false,
        2,
        false, false,
        PlanetSideGUID(0),
        false,
        255,
        false, false,
        DriveState.Mobile,
        false, false, false,
        Some(VariantVehicleData(0)),
        Some(InventoryData(
          InventoryItemData(ObjectClass.scythe, PlanetSideGUID(355), 1,
            WeaponData(0x6, 0x8, 0, ObjectClass.ancient_ammo_vehicle, PlanetSideGUID(366), 0, AmmoBoxData(0x8), ObjectClass.ancient_ammo_vehicle, PlanetSideGUID(385), 1, AmmoBoxData(0x8))
          ) :: Nil
        ))
      )(VehicleFormat.Variant)
      val msg = ObjectCreateMessage(ObjectClass.switchblade, PlanetSideGUID(418), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_switchblade
    }
  }
}
