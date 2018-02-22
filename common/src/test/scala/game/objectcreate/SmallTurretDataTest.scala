// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.PlanetSideEmpire
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
          turret.deploy.pos.coord.x mustEqual 4577.7812f
          turret.deploy.pos.coord.y mustEqual 5624.828f
          turret.deploy.pos.coord.z mustEqual 72.046875f
          turret.deploy.pos.orient.x mustEqual 0f
          turret.deploy.pos.orient.y mustEqual 2.8125f
          turret.deploy.pos.orient.z mustEqual 264.375f
          turret.deploy.faction mustEqual PlanetSideEmpire.NC
          turret.deploy.destroyed mustEqual true
          turret.deploy.unk mustEqual 2
          turret.deploy.player_guid mustEqual PlanetSideGUID(3871)
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
          turret.deploy.pos.coord.x mustEqual 4527.633f
          turret.deploy.pos.coord.y mustEqual 6271.3594f
          turret.deploy.pos.coord.z mustEqual 70.265625f
          turret.deploy.pos.orient.x mustEqual 0f
          turret.deploy.pos.orient.y mustEqual 0f
          turret.deploy.pos.orient.z mustEqual 154.6875f
          turret.deploy.faction mustEqual PlanetSideEmpire.VS
          turret.deploy.unk mustEqual 2
          turret.deploy.player_guid mustEqual PlanetSideGUID(4232)
          turret.health mustEqual 255
          turret.internals.isDefined mustEqual true
          val internals = turret.internals.get
          internals.objectClass mustEqual ObjectClass.spitfire_weapon
          internals.guid mustEqual PlanetSideGUID(3064)
          internals.parentSlot mustEqual 0
          internals.obj.isInstanceOf[WeaponData] mustEqual true
          val wep = internals.obj.asInstanceOf[WeaponData]
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
        CommonFieldData(
          PlacementData(4577.7812f, 5624.828f, 72.046875f, 0f, 2.8125f, 264.375f),
          PlanetSideEmpire.NC, true, 2, PlanetSideGUID(3871)
        ),
        255 //sets to 0
      )
      val msg = ObjectCreateMessage(ObjectClass.spitfire_turret, PlanetSideGUID(4208), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      val pkt_bitv = pkt.toBitVector
      val ori_bitv = string_spitfire_short.toBitVector
      pkt_bitv.take(173) mustEqual ori_bitv.take(173)
      pkt_bitv.drop(185) mustEqual ori_bitv.drop(185)
      //TODO work on SmallTurretData to make this pass as a single stream
    }

    "encode (spitfire)" in {
      val obj = SmallTurretData(
        CommonFieldData(
          PlacementData(4527.633f, 6271.3594f, 70.265625f, 0f, 0f, 154.6875f),
          PlanetSideEmpire.VS, 2, PlanetSideGUID(4232)
        ),
        255,
        SmallTurretData.spitfire(PlanetSideGUID(3064), 0x6, 0x8, PlanetSideGUID(3694), 8)
      )
      val msg = ObjectCreateMessage(ObjectClass.spitfire_turret, PlanetSideGUID(4265), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      val pkt_bitv = pkt.toBitVector
      val ori_bitv = string_spitfire.toBitVector
      pkt_bitv.take(173) mustEqual ori_bitv.take(173)
      pkt_bitv.drop(185) mustEqual ori_bitv.drop(185)
      //TODO work on SmallTurretData to make this pass as a single stream
    }
  }
}
