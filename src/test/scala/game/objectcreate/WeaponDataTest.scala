// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}
import org.specs2.mutable._
import scodec.bits._

class WeaponDataTest extends Specification {
  val string_lasher_held    = hex"17 BB000000 1688569D90B83 880000008082077036032000000"
  val string_punisher_held  = hex"17 F6000000 0A06612331083 88000000810381383E03200003793287C0E400000"
  val string_lasher_dropped = hex"17 F4000000 D69020C 99299 85D0A 5F10 00 00 20 400000004041038819018000000"
  val string_punisher_dropped =
    hex"17 2F010000 E12A20B 915A9 28C9A 1412 00 00 33 200000004081C1901B01800001BCB5C2E07000000"

  "WeaponData" should {
    "decode (lasher, held)" in {
      PacketCoding.decodePacket(string_lasher_held).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 187
          cls mustEqual ObjectClass.lasher
          guid mustEqual PlanetSideGUID(3033)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(4141)
          parent.get.slot mustEqual 3
          data match {
            case WeaponData(
                  CommonFieldData(wfaction, wbops, walternate, wv1, wv2, wv3, wv4, wv5, wfguid),
                  fmode,
                  List(ammo),
                  _
                ) =>
              wfaction mustEqual PlanetSideEmpire.VS
              wbops mustEqual false
              walternate mustEqual false
              wv1 mustEqual true
              wv2.isEmpty mustEqual true
              wv3 mustEqual false
              wv4.isEmpty mustEqual true
              wv5.isEmpty mustEqual true
              wfguid mustEqual PlanetSideGUID(0)

              fmode mustEqual 0

              ammo.objectClass mustEqual ObjectClass.energy_cell
              ammo.guid mustEqual PlanetSideGUID(3548)
              ammo.parentSlot mustEqual 0
              ammo.obj match {
                case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid) =>
                  faction mustEqual PlanetSideEmpire.NEUTRAL
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual true
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.contains(false) mustEqual true
                  v5.isEmpty mustEqual true
                  fguid mustEqual PlanetSideGUID(0)
                case _ =>
                  ko
              }
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "decode (punisher, held)" in {
      PacketCoding.decodePacket(string_punisher_held).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 246
          cls mustEqual ObjectClass.punisher
          guid mustEqual PlanetSideGUID(4147)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(3092)
          parent.get.slot mustEqual 3
          data match {
            case WeaponData(
                  CommonFieldData(wfaction, wbops, walternate, wv1, wv2, wv3, wv4, wv5, wfguid),
                  fmode,
                  ammo,
                  _
                ) =>
              wfaction mustEqual PlanetSideEmpire.VS
              wbops mustEqual false
              walternate mustEqual false
              wv1 mustEqual true
              wv2.isEmpty mustEqual true
              wv3 mustEqual false
              wv4.isEmpty mustEqual true
              wv5.isEmpty mustEqual true
              wfguid mustEqual PlanetSideGUID(0)

              fmode mustEqual 0
              //0
              ammo.head.objectClass mustEqual ObjectClass.bullet_9mm
              ammo.head.guid mustEqual PlanetSideGUID(3918)
              ammo.head.parentSlot mustEqual 0
              ammo.head.obj match {
                case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid) =>
                  faction mustEqual PlanetSideEmpire.NEUTRAL
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual true
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.contains(false) mustEqual true
                  v5.isEmpty mustEqual true
                  fguid mustEqual PlanetSideGUID(0)
                case _ =>
                  ko
              }
              //1
              //1
              ammo(1).objectClass mustEqual ObjectClass.rocket
              ammo(1).guid mustEqual PlanetSideGUID(3941)
              ammo(1).parentSlot mustEqual 1
              ammo(1).obj match {
                case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid) =>
                  faction mustEqual PlanetSideEmpire.NEUTRAL
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual true
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.contains(false) mustEqual true
                  v5.isEmpty mustEqual true
                  fguid mustEqual PlanetSideGUID(0)
                case _ =>
                  ko
              }
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "decode (lasher, dropped)" in {
      PacketCoding.decodePacket(string_lasher_dropped).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 244
          cls mustEqual ObjectClass.lasher
          guid mustEqual PlanetSideGUID(3074)
          parent.isDefined mustEqual false
          data.isInstanceOf[DroppedItemData[_]] mustEqual true
          val drop = data.asInstanceOf[DroppedItemData[_]]
          drop.pos.coord.x mustEqual 4691.1953f
          drop.pos.coord.y mustEqual 5537.039f
          drop.pos.coord.z mustEqual 65.484375f
          drop.pos.orient.x mustEqual 0f
          drop.pos.orient.y mustEqual 0f
          drop.pos.orient.z mustEqual 0f
          drop.obj match {
            case WeaponData(
                  CommonFieldData(wfaction, wbops, walternate, wv1, wv2, wv3, wv4, wv5, wfguid),
                  fmode,
                  List(ammo),
                  _
                ) =>
              wfaction mustEqual PlanetSideEmpire.VS
              wbops mustEqual false
              walternate mustEqual false
              wv1 mustEqual false
              wv2.isEmpty mustEqual true
              wv3 mustEqual false
              wv4.isEmpty mustEqual true
              wv5.isEmpty mustEqual true
              wfguid mustEqual PlanetSideGUID(0)

              fmode mustEqual 0

              ammo.objectClass mustEqual ObjectClass.energy_cell
              ammo.guid mustEqual PlanetSideGUID(3268)
              ammo.parentSlot mustEqual 0
              ammo.obj match {
                case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid) =>
                  faction mustEqual PlanetSideEmpire.NEUTRAL
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual false
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.contains(false) mustEqual true
                  v5.isEmpty mustEqual true
                  fguid mustEqual PlanetSideGUID(0)
                case _ =>
                  ko
              }
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "decode (punisher, dropped)" in {
      PacketCoding.decodePacket(string_punisher_dropped).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 303
          cls mustEqual ObjectClass.punisher
          guid mustEqual PlanetSideGUID(2978)
          parent.isDefined mustEqual false
          data.isInstanceOf[DroppedItemData[_]] mustEqual true
          val drop = data.asInstanceOf[DroppedItemData[_]]
          drop.pos.coord.x mustEqual 4789.133f
          drop.pos.coord.y mustEqual 5522.3125f
          drop.pos.coord.z mustEqual 72.3125f
          drop.pos.orient.x mustEqual 0f
          drop.pos.orient.y mustEqual 0f
          drop.pos.orient.z mustEqual 306.5625f
          drop.obj match {
            case WeaponData(
                  CommonFieldData(wfaction, wbops, walternate, wv1, wv2, wv3, wv4, wv5, wfguid),
                  fmode,
                  ammo,
                  _
                ) =>
              wfaction mustEqual PlanetSideEmpire.NC
              wbops mustEqual false
              walternate mustEqual false
              wv1 mustEqual false
              wv2.isEmpty mustEqual true
              wv3 mustEqual false
              wv4.isEmpty mustEqual true
              wv5.isEmpty mustEqual true
              wfguid mustEqual PlanetSideGUID(0)

              fmode mustEqual 0
              //0
              ammo.head.objectClass mustEqual ObjectClass.bullet_9mm
              ammo.head.guid mustEqual PlanetSideGUID(3528)
              ammo.head.parentSlot mustEqual 0
              ammo.head.obj match {
                case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid) =>
                  faction mustEqual PlanetSideEmpire.NEUTRAL
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual false
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.contains(false) mustEqual true
                  v5.isEmpty mustEqual true
                  fguid mustEqual PlanetSideGUID(0)
                case _ =>
                  ko
              }
              //1
              //1
              ammo(1).objectClass mustEqual ObjectClass.rocket
              ammo(1).guid mustEqual PlanetSideGUID(3031)
              ammo(1).parentSlot mustEqual 1
              ammo(1).obj match {
                case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, fguid) =>
                  faction mustEqual PlanetSideEmpire.NEUTRAL
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual false
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.contains(false) mustEqual true
                  v5.isEmpty mustEqual true
                  fguid mustEqual PlanetSideGUID(0)
                case _ =>
                  ko
              }
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode (lasher, held)" in {
      val obj = WeaponData(
        CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, None, None, PlanetSideGUID(0)),
        0,
        List(
          InternalSlot(
            ObjectClass.energy_cell,
            PlanetSideGUID(3548),
            0,
            CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false)
          )
        )
      )
      val msg = ObjectCreateMessage(
        ObjectClass.lasher,
        PlanetSideGUID(3033),
        ObjectCreateMessageParent(PlanetSideGUID(4141), 3),
        obj
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector
      pkt mustEqual string_lasher_held
    }

    "encode (punisher, held)" in {
      val obj =
        WeaponData(
          CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, None, None, PlanetSideGUID(0)),
          0,
          List(
            AmmoBoxData(
              ObjectClass.bullet_9mm,
              PlanetSideGUID(3918),
              0,
              CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false)
            ),
            AmmoBoxData(
              ObjectClass.rocket,
              PlanetSideGUID(3941),
              1,
              CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false)
            )
          )
        )
      val msg = ObjectCreateMessage(
        ObjectClass.punisher,
        PlanetSideGUID(4147),
        ObjectCreateMessageParent(PlanetSideGUID(3092), 3),
        obj
      )
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector
      pkt mustEqual string_punisher_held
    }

    "encode (lasher, dropped)" in {
      val obj = DroppedItemData(
        PlacementData(4691.1953f, 5537.039f, 65.484375f, 0.0f, 0.0f, 0.0f),
        WeaponData(
          CommonFieldData(PlanetSideEmpire.VS, false, false, false, None, false, None, None, PlanetSideGUID(0)),
          0,
          List(InternalSlot(ObjectClass.energy_cell, PlanetSideGUID(3268), 0, CommonFieldData()(false)))
        )
      )
      val msg = ObjectCreateMessage(ObjectClass.lasher, PlanetSideGUID(3074), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector
      pkt mustEqual string_lasher_dropped
    }

    "encode (punisher, dropped)" in {
      val obj = DroppedItemData(
        PlacementData(4789.133f, 5522.3125f, 72.3125f, 0f, 0f, 306.5625f),
        WeaponData(
          CommonFieldData(PlanetSideEmpire.NC, false, false, false, None, false, None, None, PlanetSideGUID(0)),
          0,
          List(
            AmmoBoxData(ObjectClass.bullet_9mm, PlanetSideGUID(3528), 0, CommonFieldData()(false)),
            AmmoBoxData(ObjectClass.rocket, PlanetSideGUID(3031), 1, CommonFieldData()(false))
          )
        )
      )
      val msg = ObjectCreateMessage(ObjectClass.punisher, PlanetSideGUID(2978), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector
      pkt mustEqual string_punisher_dropped
    }
  }
}
