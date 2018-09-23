// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate.{SmallDeployableData, _}
import net.psforever.types.{PlanetSideEmpire, Vector3}
import org.specs2.mutable._
import scodec.bits._

class SmallTurretDataTest extends Specification {
  val string_spitfire_short = hex"17 BB000000 9D37010 E4F08 6AFCA 0312 00 7F 42 2C1F0F0000F00"
  val string_spitfire = hex"17 4F010000 9D3A910 D1D78 AE3FC 9111 00 00 69 4488107F80F2021DBF80B80C80000008086EDB83A03200000"

  "SmallTurretData" should {
    "decode (spitfire, short)" in {
      PacketCoding.DecodePacket(string_spitfire_short).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 187
          cls mustEqual ObjectClass.spitfire_turret
          guid mustEqual PlanetSideGUID(4208)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[SmallTurretData] mustEqual true
          val turret = data.get.asInstanceOf[SmallTurretData]
          turret.deploy.pos.coord mustEqual Vector3(4577.7812f, 5624.828f, 72.046875f)
          turret.deploy.pos.orient mustEqual Vector3(0, 2.8125f, 264.375f)
          turret.deploy.faction mustEqual PlanetSideEmpire.NC
          turret.deploy.destroyed mustEqual true
          turret.deploy.unk1 mustEqual 2
          turret.deploy.owner_guid mustEqual PlanetSideGUID(7742)
          turret.health mustEqual 0
          turret.internals.isDefined mustEqual false
        case _ =>
          ko
      }
    }

    "decode (spitfire)" in {
      PacketCoding.DecodePacket(string_spitfire).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 335
          cls mustEqual ObjectClass.spitfire_turret
          guid mustEqual PlanetSideGUID(4265)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[SmallTurretData] mustEqual true
          val turret = data.get.asInstanceOf[SmallTurretData]
          turret.deploy.pos.coord mustEqual Vector3(4527.633f, 6271.3594f, 70.265625f)
          turret.deploy.pos.orient mustEqual Vector3(0, 0, 154.6875f)
          turret.deploy.faction mustEqual PlanetSideEmpire.VS
          turret.deploy.unk1 mustEqual 2
          turret.deploy.owner_guid mustEqual PlanetSideGUID(8208)
          turret.health mustEqual 255
          turret.internals.isDefined mustEqual true
          val internals = turret.internals.get.contents
          internals.head.objectClass mustEqual ObjectClass.spitfire_weapon
          internals.head.guid mustEqual PlanetSideGUID(3064)
          internals.head.parentSlot mustEqual 0
          internals.head.obj.isInstanceOf[WeaponData] mustEqual true
          val wep = internals.head.obj.asInstanceOf[WeaponData]
          wep.unk1 mustEqual 0x6
          wep.unk2 mustEqual 0x8
          wep.fire_mode mustEqual 0
          val ammo = wep.ammo.head
          ammo.objectClass mustEqual ObjectClass.spitfire_ammo
          ammo.guid mustEqual PlanetSideGUID(3694)
          ammo.parentSlot mustEqual 0
          ammo.obj.isInstanceOf[AmmoBoxData] mustEqual true
          ammo.obj.asInstanceOf[AmmoBoxData].unk mustEqual 8
        case _ =>
          ko
      }
    }

    "encode (spitfire, short)" in {
      val obj = SmallTurretData(
        SmallDeployableData(
          PlacementData(Vector3(4577.7812f, 5624.828f, 72.046875f), Vector3(0, 2.8125f, 264.375f)),
          PlanetSideEmpire.NC, false, true, 2, false, false, PlanetSideGUID(7742)
        ),
        255 //sets to 0
      )
      val msg = ObjectCreateMessage(ObjectClass.spitfire_turret, PlanetSideGUID(4208), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_spitfire_short
    }

    "encode (spitfire)" in {
      val obj = SmallTurretData(
        SmallDeployableData(
          PlacementData(Vector3(4527.633f, 6271.3594f, 70.265625f), Vector3(0, 0, 154.6875f)),
          PlanetSideEmpire.VS, false, false, 2, false, true, PlanetSideGUID(8208)
        ),
        255,
        InventoryData(List(SmallTurretData.spitfire(PlanetSideGUID(3064), 0x6, 0x8, PlanetSideGUID(3694), 8)))
      )
      val msg = ObjectCreateMessage(ObjectClass.spitfire_turret, PlanetSideGUID(4265), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_spitfire
    }
  }
}
