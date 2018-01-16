// Copyright (c) 2017 PSForever
package game.objectcreatedetailed

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateDetailedMessage, _}
import net.psforever.packet.game.objectcreate._
import scodec.bits._

class DetailedWeaponDataTest extends Specification {
  val string_gauss = hex"18 DC000000 2580 2C9 B905 82 480000020000C04 1C00C0B0190000078000"
  val string_punisher = hex"18 27010000 2580 612 a706 82 080000020000c08 1c13a0d01900000780 13a4701a072000000800"

  "DetailedWeaponData" should {
    "decode (gauss)" in {
      PacketCoding.DecodePacket(string_gauss).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 220
          cls mustEqual ObjectClass.gauss
          guid mustEqual PlanetSideGUID(1465)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(75)
          parent.get.slot mustEqual 2
          data.isDefined mustEqual true
          val obj_wep = data.get.asInstanceOf[DetailedWeaponData]
          obj_wep.unk1 mustEqual 2
          obj_wep.unk2 mustEqual 8
          val obj_ammo = obj_wep.ammo
          obj_ammo.head.objectClass mustEqual 28
          obj_ammo.head.guid mustEqual PlanetSideGUID(1286)
          obj_ammo.head.parentSlot mustEqual 0
          obj_ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 30
        case _ =>
          ko
      }
    }

    "decode (punisher)" in {
      PacketCoding.DecodePacket(string_punisher).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 295
          cls mustEqual ObjectClass.punisher
          guid mustEqual PlanetSideGUID(1703)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(75)
          parent.get.slot mustEqual 2
          data.isDefined mustEqual true
          val obj_wep = data.get.asInstanceOf[DetailedWeaponData]
          obj_wep.unk1 mustEqual 0
          obj_wep.unk2 mustEqual 8
          val obj_ammo = obj_wep.ammo
          obj_ammo.size mustEqual 2
          obj_ammo.head.objectClass mustEqual ObjectClass.bullet_9mm
          obj_ammo.head.guid mustEqual PlanetSideGUID(1693)
          obj_ammo.head.parentSlot mustEqual 0
          obj_ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 30
          obj_ammo(1).objectClass mustEqual ObjectClass.jammer_cartridge
          obj_ammo(1).guid mustEqual PlanetSideGUID(1564)
          obj_ammo(1).parentSlot mustEqual 1
          obj_ammo(1).obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 1
        case _ =>
          ko
      }
    }

    "encode (gauss)" in {
      val obj = DetailedWeaponData(2, 8, ObjectClass.bullet_9mm, PlanetSideGUID(1286), 0, DetailedAmmoBoxData(8, 30))
      val msg = ObjectCreateDetailedMessage(ObjectClass.gauss, PlanetSideGUID(1465), ObjectCreateMessageParent(PlanetSideGUID(75), 2), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_gauss
    }

    "encode (punisher)" in {
      val obj = DetailedWeaponData(0, 8, 0,
        DetailedAmmoBoxData(ObjectClass.bullet_9mm, PlanetSideGUID(1693), 0, DetailedAmmoBoxData(8, 30)) ::
          DetailedAmmoBoxData(ObjectClass.jammer_cartridge, PlanetSideGUID(1564), 1, DetailedAmmoBoxData(8, 1)) ::
          Nil
      )(2)
      val msg = ObjectCreateDetailedMessage(ObjectClass.punisher, PlanetSideGUID(1703), ObjectCreateMessageParent(PlanetSideGUID(75), 2), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_punisher
    }
  }
}
