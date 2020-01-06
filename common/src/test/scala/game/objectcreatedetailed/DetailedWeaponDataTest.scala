// Copyright (c) 2017 PSForever
package game.objectcreatedetailed

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateDetailedMessage, _}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}
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
          data match {
            case DetailedWeaponData(cdata, fmode, ammo, _) =>
              cdata match {
                case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid) =>
                  faction mustEqual PlanetSideEmpire.NC
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual true
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.isEmpty mustEqual true
                  v5.isEmpty mustEqual true
                  fguid mustEqual PlanetSideGUID(0)
                case _ =>
                  ko
              }

              fmode mustEqual 0

              ammo.size mustEqual 1
              ammo.head.objectClass mustEqual 28
              ammo.head.guid mustEqual PlanetSideGUID(1286)
              ammo.head.parentSlot mustEqual 0
              ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 30
            case _ =>
              ko
          }
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
          data match {
            case DetailedWeaponData(cdata, fmode, ammo, _) =>
              cdata match {
                case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid) =>
                  faction mustEqual PlanetSideEmpire.TR
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual true
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.isEmpty mustEqual true
                  v5.isEmpty mustEqual true
                  fguid mustEqual PlanetSideGUID(0)
                case _ =>
                  ko
              }

              fmode mustEqual 0

              ammo.size mustEqual 2
              ammo.head.objectClass mustEqual ObjectClass.bullet_9mm
              ammo.head.guid mustEqual PlanetSideGUID(1693)
              ammo.head.parentSlot mustEqual 0
              ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 30
              ammo(1).objectClass mustEqual ObjectClass.jammer_cartridge
              ammo(1).guid mustEqual PlanetSideGUID(1564)
              ammo(1).parentSlot mustEqual 1
              ammo(1).obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 1
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode (gauss)" in {
      val obj = DetailedWeaponData(
        CommonFieldData(PlanetSideEmpire.NC, false, false, true, None, false, None, None, PlanetSideGUID(0)),
        0,
        List(InternalSlot(ObjectClass.bullet_9mm, PlanetSideGUID(1286), 0, DetailedAmmoBoxData(8, 30)))
      )
      val msg = ObjectCreateDetailedMessage(ObjectClass.gauss, PlanetSideGUID(1465), ObjectCreateMessageParent(PlanetSideGUID(75), 2), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_gauss
    }

    "encode (punisher)" in {
      val obj = DetailedWeaponData(
        CommonFieldData(
          PlanetSideEmpire.TR,
          bops = false,
          alternate = false,
          true,
          None,
          false,
          None,
          None,
          PlanetSideGUID(0)
        ),
        0,
        List(
          DetailedAmmoBoxData(ObjectClass.bullet_9mm, PlanetSideGUID(1693), 0, DetailedAmmoBoxData(8, 30)),
          DetailedAmmoBoxData(ObjectClass.jammer_cartridge, PlanetSideGUID(1564), 1, DetailedAmmoBoxData(8, 1))
        )
      )
      val msg = ObjectCreateDetailedMessage(ObjectClass.punisher, PlanetSideGUID(1703), ObjectCreateMessageParent(PlanetSideGUID(75), 2), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_punisher
    }
  }
}
