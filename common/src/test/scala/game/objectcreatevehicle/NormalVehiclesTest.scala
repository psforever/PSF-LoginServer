// Copyright (c) 2017 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.objectcreate._
import net.psforever.packet.game.ObjectCreateMessage
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
          data.isInstanceOf[VehicleData] mustEqual true
          val fury = data.asInstanceOf[VehicleData]
          fury.pos.coord mustEqual Vector3(6531.961f, 1872.1406f,24.734375f)
          fury.pos.orient mustEqual Vector3(0, 0, 357.1875f)
          fury.pos.vel.isEmpty mustEqual true
          fury.data.faction mustEqual PlanetSideEmpire.VS
          fury.data.v1 mustEqual true
          fury.data.guid mustEqual PlanetSideGUID(0)
          fury.health mustEqual 255
          //
          fury.inventory.isDefined mustEqual true
          fury.inventory.get.contents.size mustEqual 1
          val mounting = fury.inventory.get.contents.head
          mounting.objectClass mustEqual ObjectClass.fury_weapon_systema
          mounting.guid mustEqual PlanetSideGUID(400)
          mounting.parentSlot mustEqual 1
          mounting.obj.isInstanceOf[WeaponData] mustEqual true
          mounting.obj match {
            case WeaponData(CommonFieldData(wfaction, wbops, walternate, wv1, wv2, wv3, wv4, wv5, wfguid), fmode, ammo, _) =>
              wfaction mustEqual PlanetSideEmpire.NEUTRAL
              wbops mustEqual false
              walternate mustEqual false
              wv1 mustEqual true
              wv2.isEmpty mustEqual true
              wv3 mustEqual false
              wv4.isEmpty mustEqual true
              wv5.isEmpty mustEqual true
              wfguid mustEqual PlanetSideGUID(0)

              fmode mustEqual 0

              ammo.head.objectClass mustEqual ObjectClass.hellfire_ammo
              ammo.head.guid mustEqual PlanetSideGUID(432)
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
            case _ =>
              ko
          }
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
          data.isInstanceOf[VehicleData] mustEqual true
          val lightning = data.asInstanceOf[VehicleData]
          lightning.pos.coord mustEqual Vector3(3674.8438f, 2726.789f, 91.15625f)
          lightning.pos.orient mustEqual Vector3(0, 0, 90)
          lightning.pos.vel.isEmpty mustEqual true
          lightning.data.faction mustEqual PlanetSideEmpire.VS
          lightning.data.v1 mustEqual true
          lightning.data.guid mustEqual PlanetSideGUID(0)
          lightning.health mustEqual 255

          lightning.inventory.isDefined mustEqual true
          lightning.inventory.get.contents.size mustEqual 1
          val mounting = lightning.inventory.get.contents.head
          mounting.objectClass mustEqual ObjectClass.lightning_weapon_system
          mounting.guid mustEqual PlanetSideGUID(91)
          mounting.parentSlot mustEqual 1
          mounting.obj match {
            case WeaponData(CommonFieldData(wfaction, wbops, walternate, wv1, wv2, wv3, wv4, wv5, wfguid), fmode, ammo, _) =>
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
              ammo.head.objectClass mustEqual ObjectClass.bullet_75mm
              ammo.head.guid mustEqual PlanetSideGUID(92)
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
              ammo(1).objectClass mustEqual ObjectClass.bullet_25mm
              ammo(1).guid mustEqual PlanetSideGUID(93)
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

    "decode (medium transport)" in {
      PacketCoding.DecodePacket(string_mediumtransport).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 474L
          cls mustEqual ObjectClass.mediumtransport
          guid mustEqual PlanetSideGUID(387)
          parent.isDefined mustEqual false
          data match {
            case VehicleData(pos, vdata, unk3, health, unk4, _, driveState, unk5, unk6, _, format, Some(InventoryData(inv))) =>
              pos.coord mustEqual Vector3(6531.961f, 1872.1406f, 24.734375f)
              pos.orient mustEqual Vector3.z(357.1875f)

              vdata.faction mustEqual PlanetSideEmpire.NC
              vdata.alternate mustEqual false
              vdata.v1 mustEqual true
              vdata.jammered mustEqual false
              vdata.v5.isEmpty mustEqual true
              vdata.guid mustEqual PlanetSideGUID(0)

              health mustEqual 255
              driveState mustEqual DriveState.State7
              unk3 mustEqual false
              unk4 mustEqual false
              unk5 mustEqual true
              unk6 mustEqual false
              format.isEmpty mustEqual true
              //0
              inv.head.objectClass mustEqual ObjectClass.mediumtransport_weapon_systemA
              inv.head.guid mustEqual PlanetSideGUID(383)
              inv.head.parentSlot mustEqual 5
              inv.head.obj match {
                case WeaponData(CommonFieldData(wfaction, wbops, walternate, wv1, wv2, wv3, wv4, wv5, wfguid), fmode, List(ammo), _) =>
                  wfaction mustEqual PlanetSideEmpire.NEUTRAL
                  wbops mustEqual false
                  walternate mustEqual false
                  wv1 mustEqual true
                  wv2.isEmpty mustEqual true
                  wv3 mustEqual false
                  wv4.isEmpty mustEqual true
                  wv5.isEmpty mustEqual true
                  wfguid mustEqual PlanetSideGUID(0)

                  fmode mustEqual 0

                  ammo.objectClass mustEqual ObjectClass.bullet_20mm
                  ammo.guid mustEqual PlanetSideGUID(420)
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
              //1
              inv(1).objectClass mustEqual ObjectClass.mediumtransport_weapon_systemB
              inv(1).guid mustEqual PlanetSideGUID(556)
              inv(1).parentSlot mustEqual 6
              inv(1).obj match {
                case WeaponData(CommonFieldData(wfaction, wbops, walternate, wv1, wv2, wv3, wv4, wv5, wfguid), fmode, List(ammo), _) =>
                  wfaction mustEqual PlanetSideEmpire.NEUTRAL
                  wbops mustEqual false
                  walternate mustEqual false
                  wv1 mustEqual true
                  wv2.isEmpty mustEqual true
                  wv3 mustEqual false
                  wv4.isEmpty mustEqual true
                  wv5.isEmpty mustEqual true
                  wfguid mustEqual PlanetSideGUID(0)

                  fmode mustEqual 0

                  ammo.objectClass mustEqual ObjectClass.bullet_20mm
                  ammo.guid mustEqual PlanetSideGUID(575)
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
        case _ =>
          ko
      }
    }

    "encode (fury)" in {
      val obj = VehicleData(
        PlacementData(6531.961f, 1872.1406f, 24.734375f, 0f, 0f, 357.1875f),
        CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)),
        false,
        255,
        false, false,
        DriveState.Mobile,
        false, false, false,
        None,
        Some(InventoryData(List(
          InventoryItemData(ObjectClass.fury_weapon_systema, PlanetSideGUID(400), 1,
            WeaponData(
              CommonFieldData(PlanetSideEmpire.NEUTRAL, 2),
              0,
              List(
                InternalSlot(ObjectClass.hellfire_ammo, PlanetSideGUID(432), 0,
                  CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false)
                )
              )
            )
          )
        )))
      )(VehicleFormat.Normal)
      val msg = ObjectCreateMessage(ObjectClass.fury, PlanetSideGUID(413), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_fury
    }

    "encode (lightning)" in {
      val obj = VehicleData(
        PlacementData(3674.8438f, 2726.789f, 91.15625f, 0f, 0f, 90.0f),
        CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)),
        false,
        255,
        false, false,
        DriveState.Mobile,
        false, false, false,
        None,
        Some(InventoryData(List(
          InventoryItemData(ObjectClass.lightning_weapon_system, PlanetSideGUID(91), 1,
            WeaponData(
              CommonFieldData(PlanetSideEmpire.VS, 2),
              0,
              List(
                InternalSlot(ObjectClass.bullet_75mm, PlanetSideGUID(92), 0, CommonFieldData()(false)),
                InternalSlot(ObjectClass.bullet_25mm, PlanetSideGUID(93), 1, CommonFieldData()(false))
              )
            )
          )
        )))
      )(VehicleFormat.Normal)
      val msg = ObjectCreateMessage(ObjectClass.lightning, PlanetSideGUID(90), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_lightning
    }

    "encode (medium transport)" in {
      val obj = VehicleData(
        PlacementData(6531.961f, 1872.1406f, 24.734375f, 0f, 0f, 357.1875f),
        CommonFieldData(PlanetSideEmpire.NC, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)),
        false,
        255,
        false, false,
        DriveState.State7,
        true, false, false,
        None,
        Some(InventoryData(List(
          InventoryItemData(ObjectClass.mediumtransport_weapon_systemA, PlanetSideGUID(383), 5,
            WeaponData(
              CommonFieldData(PlanetSideEmpire.NEUTRAL, 2),
              0,
              List(
                InternalSlot(ObjectClass.bullet_20mm, PlanetSideGUID(420), 0,
                  CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false)
                )
              )
            )
          ),
          InventoryItemData(ObjectClass.mediumtransport_weapon_systemB, PlanetSideGUID(556), 6,
            WeaponData(
              CommonFieldData(PlanetSideEmpire.NEUTRAL, 2),
              0,
              List(
                InternalSlot(ObjectClass.bullet_20mm, PlanetSideGUID(575), 0,
                  CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false)
                )
              )
            )
          )
        )))
      )(VehicleFormat.Normal)
      val msg = ObjectCreateMessage(ObjectClass.mediumtransport, PlanetSideGUID(387), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_mediumtransport
    }
  }
}
