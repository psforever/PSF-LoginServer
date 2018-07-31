// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, Vector3}
import org.specs2.mutable._
import scodec.bits._

class OneMannedFieldTurretDataTest extends Specification {
  val string_orion = hex"17 5E010000 D82640B 92F76 01D65 F611 00 00 5E 4400006304BFC1E4041826E1503900000010104CE704C06400000"

  "OneMannedFieldTurretData" should {
    "decode (orion)" in {
      PacketCoding.DecodePacket(string_orion).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 350
          cls mustEqual ObjectClass.portable_manned_turret_vs
          guid mustEqual PlanetSideGUID(2916)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[OneMannedFieldTurretData] mustEqual true
          val omft = data.get.asInstanceOf[OneMannedFieldTurretData]
          omft.deploy.pos.coord mustEqual Vector3(3567.1406f, 2988.0078f, 71.84375f)
          omft.deploy.pos.orient mustEqual Vector3(0, 0, 185.625f)
          omft.deploy.faction mustEqual PlanetSideEmpire.VS
          omft.deploy.unk1 mustEqual 2
          omft.deploy.owner_guid mustEqual PlanetSideGUID(2502)
          omft.health mustEqual 255
          omft.internals.isDefined mustEqual true
          val internals = omft.internals.get.contents
          internals.head.objectClass mustEqual ObjectClass.energy_gun_vs
          internals.head.guid mustEqual PlanetSideGUID(2615)
          internals.head.parentSlot mustEqual 1
          internals.head.obj.isInstanceOf[WeaponData] mustEqual true
          val wep = internals.head.obj.asInstanceOf[WeaponData]
          wep.unk1 mustEqual 0x6
          wep.unk2 mustEqual 0x8
          wep.fire_mode mustEqual 0
          val ammo = wep.ammo.head
          ammo.objectClass mustEqual ObjectClass.energy_gun_ammo
          ammo.guid mustEqual PlanetSideGUID(2510)
          ammo.parentSlot mustEqual 0
          ammo.obj.isInstanceOf[AmmoBoxData] mustEqual true
          ammo.obj.asInstanceOf[AmmoBoxData].unk mustEqual 8
        case _ =>
          ko
      }
    }

    "encode (orion)" in {
      val obj = OneMannedFieldTurretData(
        SmallDeployableData(
          PlacementData(Vector3(3567.1406f, 2988.0078f, 71.84375f), Vector3(0, 0, 185.625f)),
          PlanetSideEmpire.VS, false, false, 2, false, false, PlanetSideGUID(2502)
        ),
        255,
        InventoryData(List(OneMannedFieldTurretData.orion(PlanetSideGUID(2615), 0x6, 0x8, PlanetSideGUID(2510), 8)))
      )
      val msg = ObjectCreateMessage(ObjectClass.portable_manned_turret_vs, PlanetSideGUID(2916), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_orion
    }

    "avenger" in {
      OneMannedFieldTurretData.avenger(PlanetSideGUID(1), 2, 3, PlanetSideGUID(4), 5) mustEqual
        InternalSlot(ObjectClass.energy_gun_tr, PlanetSideGUID(1), 1,
          WeaponData(2, 3, ObjectClass.energy_gun_ammo, PlanetSideGUID(4), 0,
            AmmoBoxData(5)
          )
        )
    }

    "generic" in {
      OneMannedFieldTurretData.generic(PlanetSideGUID(1), 2, 3, PlanetSideGUID(4), 5) mustEqual
        InternalSlot(ObjectClass.energy_gun, PlanetSideGUID(1), 1,
          WeaponData(2, 3, ObjectClass.energy_gun_ammo, PlanetSideGUID(4), 0,
            AmmoBoxData(5)
          )
        )
    }

    "orion" in {
      OneMannedFieldTurretData.orion(PlanetSideGUID(1), 2, 3, PlanetSideGUID(4), 5) mustEqual
        InternalSlot(ObjectClass.energy_gun_vs, PlanetSideGUID(1), 1,
          WeaponData(2, 3, ObjectClass.energy_gun_ammo, PlanetSideGUID(4), 0,
            AmmoBoxData(5)
          )
        )
    }

    "osprey" in {
      OneMannedFieldTurretData.osprey(PlanetSideGUID(1), 2, 3, PlanetSideGUID(4), 5) mustEqual
        InternalSlot(ObjectClass.energy_gun_nc, PlanetSideGUID(1), 1,
          WeaponData(2, 3, ObjectClass.energy_gun_ammo, PlanetSideGUID(4), 0,
            AmmoBoxData(5)
          )
        )
    }
  }
}
