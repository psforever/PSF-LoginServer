// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import org.specs2.mutable._
import scodec.bits._

class WeaponDataTest extends Specification {
  val string_lasher_held = hex"17 BB000000 1688569D90B83 880000008082077036032000000"
  val string_punisher_held = hex"17 F6000000 0A06612331083 88000000810381383E03200003793287C0E400000"
  val string_lasher_dropped = hex"17 F4000000 D69020C 99299 85D0A 5F10 00 00 20 400000004041038819018000000"
  val string_punisher_dropped = hex"17 2F010000 E12A20B 915A9 28C9A 1412 00 00 33 200000004081C1901B01800001BCB5C2E07000000"

  "WeaponData" should {
    "decode (lasher, held)" in {
      PacketCoding.DecodePacket(string_lasher_held).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 187
          cls mustEqual ObjectClass.lasher
          guid mustEqual PlanetSideGUID(3033)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(4141)
          parent.get.slot mustEqual 3
          data.isDefined mustEqual true
          data.get.isInstanceOf[WeaponData] mustEqual true
          val wep = data.get.asInstanceOf[WeaponData]
          wep.unk1 mustEqual 4
          wep.unk2 mustEqual 8
          wep.fire_mode mustEqual 0
          wep.ammo.head.objectClass mustEqual ObjectClass.energy_cell
          wep.ammo.head.guid mustEqual PlanetSideGUID(3548)
          wep.ammo.head.parentSlot mustEqual 0
          wep.ammo.head.obj.isInstanceOf[AmmoBoxData] mustEqual true
          val ammo = wep.ammo.head.obj.asInstanceOf[AmmoBoxData]
          ammo.unk mustEqual 8
        case _ =>
          ko
      }
    }

    "decode (punisher, held)" in {
      PacketCoding.DecodePacket(string_punisher_held).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 246
          cls mustEqual ObjectClass.punisher
          guid mustEqual PlanetSideGUID(4147)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(3092)
          parent.get.slot mustEqual 3
          data.isDefined mustEqual true
          data.get.isInstanceOf[WeaponData] mustEqual true
          val wep = data.get.asInstanceOf[WeaponData]
          wep.unk1 mustEqual 4
          wep.unk2 mustEqual 8
          wep.fire_mode mustEqual 0
          val ammo = wep.ammo
          ammo.size mustEqual 2
          //0
          ammo.head.objectClass mustEqual ObjectClass.bullet_9mm
          ammo.head.guid mustEqual PlanetSideGUID(3918)
          ammo.head.parentSlot mustEqual 0
          ammo.head.obj.isInstanceOf[AmmoBoxData] mustEqual true
          ammo.head.obj.asInstanceOf[AmmoBoxData].unk mustEqual 8
          //1
          ammo(1).objectClass mustEqual ObjectClass.rocket
          ammo(1).guid mustEqual PlanetSideGUID(3941)
          ammo(1).parentSlot mustEqual 1
          ammo(1).obj.isInstanceOf[AmmoBoxData] mustEqual true
          ammo(1).obj.asInstanceOf[AmmoBoxData].unk mustEqual 8
        case _ =>
          ko
      }
    }

    "decode (lasher, dropped)" in {
      PacketCoding.DecodePacket(string_lasher_dropped).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 244
          cls mustEqual ObjectClass.lasher
          guid mustEqual PlanetSideGUID(3074)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
          val drop = data.get.asInstanceOf[DroppedItemData[_]]
          drop.pos.coord.x mustEqual 4691.1953f
          drop.pos.coord.y mustEqual 5537.039f
          drop.pos.coord.z mustEqual 65.484375f
          drop.pos.orient.x mustEqual 0f
          drop.pos.orient.y mustEqual 0f
          drop.pos.orient.z mustEqual 0f
          drop.obj.isInstanceOf[WeaponData] mustEqual true
          val wep = drop.obj.asInstanceOf[WeaponData]
          wep.unk1 mustEqual 4
          wep.unk2 mustEqual 0
          wep.fire_mode mustEqual 0
          wep.ammo.head.objectClass mustEqual ObjectClass.energy_cell
          wep.ammo.head.guid mustEqual PlanetSideGUID(3268)
          wep.ammo.head.parentSlot mustEqual 0
          wep.ammo.head.obj.isInstanceOf[AmmoBoxData] mustEqual true
          val ammo = wep.ammo.head.obj.asInstanceOf[AmmoBoxData]
          ammo.unk mustEqual 0
        case _ =>
          ko
      }
    }

    "decode (punisher, dropped)" in {
      PacketCoding.DecodePacket(string_punisher_dropped).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 303
          cls mustEqual ObjectClass.punisher
          guid mustEqual PlanetSideGUID(2978)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[DroppedItemData[_]] mustEqual true
          val drop = data.get.asInstanceOf[DroppedItemData[_]]
          drop.pos.coord.x mustEqual 4789.133f
          drop.pos.coord.y mustEqual 5522.3125f
          drop.pos.coord.z mustEqual 72.3125f
          drop.pos.orient.x mustEqual 0f
          drop.pos.orient.y mustEqual 0f
          drop.pos.orient.z mustEqual 306.5625f
          drop.obj.isInstanceOf[WeaponData] mustEqual true
          val wep = drop.obj.asInstanceOf[WeaponData]
          wep.unk1 mustEqual 2
          wep.unk2 mustEqual 0
          wep.fire_mode mustEqual 0
          val ammo = wep.ammo
          ammo.size mustEqual 2
          //0
          ammo.head.objectClass mustEqual ObjectClass.bullet_9mm
          ammo.head.guid mustEqual PlanetSideGUID(3528)
          ammo.head.parentSlot mustEqual 0
          ammo.head.obj.isInstanceOf[AmmoBoxData] mustEqual true
          ammo.head.obj.asInstanceOf[AmmoBoxData].unk mustEqual 0
          //1
          ammo(1).objectClass mustEqual ObjectClass.rocket
          ammo(1).guid mustEqual PlanetSideGUID(3031)
          ammo(1).parentSlot mustEqual 1
          ammo(1).obj.isInstanceOf[AmmoBoxData] mustEqual true
          ammo(1).obj.asInstanceOf[AmmoBoxData].unk mustEqual 0
        case _ =>
          ko
      }
    }

    "encode (lasher, held)" in {
      val obj = WeaponData(4, 8, ObjectClass.energy_cell, PlanetSideGUID(3548), 0, AmmoBoxData(8))
      val msg = ObjectCreateMessage(ObjectClass.lasher, PlanetSideGUID(3033), ObjectCreateMessageParent(PlanetSideGUID(4141), 3), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_lasher_held
    }

    "encode (punisher, held)" in {
      val obj =
        WeaponData(4, 8, 0,
          AmmoBoxData(ObjectClass.bullet_9mm, PlanetSideGUID(3918), 0, AmmoBoxData(8)) ::
            AmmoBoxData(ObjectClass.rocket, PlanetSideGUID(3941), 1, AmmoBoxData(8)) ::
            Nil
        )(2)
      val msg = ObjectCreateMessage(ObjectClass.punisher, PlanetSideGUID(4147), ObjectCreateMessageParent(PlanetSideGUID(3092), 3), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_punisher_held
    }

    "encode (lasher, dropped)" in {
      val obj = DroppedItemData(
        PlacementData(4691.1953f, 5537.039f, 65.484375f, 0.0f, 0.0f, 0.0f),
        WeaponData(4, 0, ObjectClass.energy_cell, PlanetSideGUID(3268), 0, AmmoBoxData())
      )
      val msg = ObjectCreateMessage(ObjectClass.lasher, PlanetSideGUID(3074), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_lasher_dropped
    }

    "encode (punisher, dropped)" in {
      val obj = DroppedItemData(
        PlacementData(4789.133f, 5522.3125f, 72.3125f, 0f, 0f, 306.5625f),
        WeaponData(2, 0, 0,
          AmmoBoxData(ObjectClass.bullet_9mm, PlanetSideGUID(3528), 0, AmmoBoxData()) ::
            AmmoBoxData(ObjectClass.rocket, PlanetSideGUID(3031), 1, AmmoBoxData()) ::
            Nil
        )(2)
      )
      val msg = ObjectCreateMessage(ObjectClass.punisher, PlanetSideGUID(2978), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_punisher_dropped
    }
  }
}
