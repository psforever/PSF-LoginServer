// Copyright (c) 2021 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.objectcreate._
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import org.specs2.mutable._
import scodec.bits._

class BattleframeRoboticsTest extends Specification {
  val string_aphelion_gunner = hex"17 80 02 00 00 AA 0B F0 15 EB 1C FE C3 30 90 40 00 00 E4 40 00 0F FF F0 00 00 F2 08 18 CC 13 C0 60 B2 00 00 00 10 11 94 2A 00 C0 64 00 00 1A 04 D8 0C 1E 40 00 00 02 02 32 85 60 18 0C 80 00 03 10 99 01 84 C8 00 00 00 40 46 10 CE 03 01 90 00 00"
  val string_aphelion_flight = hex"17 f6010000 a98 8901 5eb1c fec33 0904 00 00 0e4 40000ffff0000002040866102030390000000808ca1cc0603200000d0140060b20000001011943c00c06400000"

  "Battle Frame Robotics" should {
    "decode (aphelion)" in {
      PacketCoding.decodePacket(string_aphelion_gunner).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 640L
          cls mustEqual ObjectClass.aphelion_gunner
          guid mustEqual PlanetSideGUID(447)
          parent.isDefined mustEqual false
          data match {
            case BattleFrameRoboticsData(pos, vdata, health, shields, unk1, unk2, no_mount_points, drive_state, proper_anim, unk3, show_shield, unk4, Some(inv)) =>
              pos.coord mustEqual Vector3(6498.7344f, 1927.9844f, 16.140625f)
              pos.orient mustEqual Vector3.z(50.625f)
              pos.vel.isEmpty mustEqual true

              vdata match {
                case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, vguid) =>
                  faction mustEqual PlanetSideEmpire.VS
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual true
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.isEmpty mustEqual true
                  v5.isEmpty mustEqual true
                  vguid mustEqual PlanetSideGUID(0)
                case _ =>
                  ko
              }

              health mustEqual 255
              shields mustEqual 255
              unk1 mustEqual 0
              unk2 mustEqual false
              no_mount_points mustEqual false
              drive_state mustEqual 60
              proper_anim mustEqual true
              unk3 mustEqual 0
              show_shield mustEqual false
              unk4.isEmpty mustEqual true

              inv match {
                case InventoryData(list) =>
                  list.head.objectClass mustEqual ObjectClass.aphelion_ppa_left
                  list.head.guid mustEqual PlanetSideGUID(335)
                  list.head.parentSlot mustEqual 2
                  list.head.obj match {
                    case WeaponData(wdata, fire_mode, ammo, unk) =>
                      wdata match {
                        case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, wguid) =>
                          faction mustEqual PlanetSideEmpire.NEUTRAL
                          bops mustEqual false
                          alternate mustEqual false
                          v1 mustEqual true
                          v2.isEmpty mustEqual true
                          v3 mustEqual false
                          v4.contains(false) mustEqual true
                          v5.isEmpty mustEqual true
                          wguid mustEqual PlanetSideGUID(0)
                        case _ =>
                          ko
                      }
                      fire_mode mustEqual 0
                      ammo.head.objectClass mustEqual ObjectClass.aphelion_ppa_ammo
                      ammo.head.guid mustEqual PlanetSideGUID(340)
                      ammo.head.parentSlot mustEqual 0
                      unk mustEqual false
                    case _ =>
                      ko
                  }

                  list(1).objectClass mustEqual ObjectClass.aphelion_ppa_right
                  list(1).guid mustEqual PlanetSideGUID(411)
                  list(1).parentSlot mustEqual 3
                  list(1).obj match {
                    case WeaponData(wdata, fire_mode, ammo, unk) =>
                      wdata match {
                        case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, wguid) =>
                          faction mustEqual PlanetSideEmpire.NEUTRAL
                          bops mustEqual false
                          alternate mustEqual false
                          v1 mustEqual true
                          v2.isEmpty mustEqual true
                          v3 mustEqual false
                          v4.contains(false) mustEqual true
                          v5.isEmpty mustEqual true
                          wguid mustEqual PlanetSideGUID(0)
                        case _ =>
                          ko
                      }
                      fire_mode mustEqual 0
                      ammo.head.objectClass mustEqual ObjectClass.aphelion_ppa_ammo
                      ammo.head.guid mustEqual PlanetSideGUID(342)
                      ammo.head.parentSlot mustEqual 0
                      unk mustEqual false
                    case _ =>
                      ko
                  }

                  list(2).objectClass mustEqual ObjectClass.aphelion_plasma_rocket_pod
                  list(2).guid mustEqual PlanetSideGUID(409)
                  list(2).parentSlot mustEqual 4
                  list(2).obj match {
                    case WeaponData(wdata, fire_mode, ammo, unk) =>
                      wdata match {
                        case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, wguid) =>
                          faction mustEqual PlanetSideEmpire.NEUTRAL
                          bops mustEqual false
                          alternate mustEqual false
                          v1 mustEqual true
                          v2.isEmpty mustEqual true
                          v3 mustEqual false
                          v4.contains(false) mustEqual true
                          v5.isEmpty mustEqual true
                          wguid mustEqual PlanetSideGUID(0)
                        case _ =>
                          ko
                      }
                      fire_mode mustEqual 0
                      ammo.head.objectClass mustEqual ObjectClass.aphelion_plasma_rocket_ammo
                      ammo.head.guid mustEqual PlanetSideGUID(359)
                      ammo.head.parentSlot mustEqual 0
                      unk mustEqual false
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

    "decode (eclipse)" in {
      PacketCoding.decodePacket(string_aphelion_flight).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 502L
          cls mustEqual ObjectClass.aphelion_flight
          guid mustEqual PlanetSideGUID(393)
          parent.isDefined mustEqual false
          data match {
            case BattleFrameRoboticsData(pos, vdata, health, shields, unk1, unk2, no_mount_points, drive_state, proper_anim, unk3, show_shield, unk4, Some(inv)) =>
              pos.coord mustEqual Vector3(6498.7344f, 1927.9844f, 16.140625f)
              pos.orient mustEqual Vector3.z(50.625f)
              pos.vel.isEmpty mustEqual true

              vdata match {
                case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, vguid) =>
                  faction mustEqual PlanetSideEmpire.VS
                  bops mustEqual false
                  alternate mustEqual false
                  v1 mustEqual true
                  v2.isEmpty mustEqual true
                  v3 mustEqual false
                  v4.isEmpty mustEqual true
                  v5.isEmpty mustEqual true
                  vguid mustEqual PlanetSideGUID(0)
                case _ =>
                  ko
              }

              health mustEqual 255
              shields mustEqual 255
              unk1 mustEqual 0
              unk2 mustEqual false
              no_mount_points mustEqual false
              drive_state mustEqual 0
              proper_anim mustEqual true
              unk3 mustEqual 0
              show_shield mustEqual false
              unk4.contains(false) mustEqual true

              inv match {
                case InventoryData(list) =>
                  list.head.objectClass mustEqual ObjectClass.aphelion_ppa_left
                  list.head.guid mustEqual PlanetSideGUID(385)
                  list.head.parentSlot mustEqual 1
                  list.head.obj match {
                    case WeaponData(wdata, fire_mode, ammo, unk) =>
                      wdata match {
                        case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, wguid) =>
                          faction mustEqual PlanetSideEmpire.NEUTRAL
                          bops mustEqual false
                          alternate mustEqual false
                          v1 mustEqual true
                          v2.isEmpty mustEqual true
                          v3 mustEqual false
                          v4.contains(false) mustEqual true
                          v5.isEmpty mustEqual true
                          wguid mustEqual PlanetSideGUID(0)
                        case _ =>
                          ko
                      }
                      fire_mode mustEqual 0
                      ammo.head.objectClass mustEqual ObjectClass.aphelion_ppa_ammo
                      ammo.head.guid mustEqual PlanetSideGUID(371)
                      ammo.head.parentSlot mustEqual 0
                      unk mustEqual false
                    case _ =>
                      ko
                  }

                  list(1).objectClass mustEqual ObjectClass.aphelion_ppa_right
                  list(1).guid mustEqual PlanetSideGUID(336)
                  list(1).parentSlot mustEqual 2
                  list(1).obj match {
                    case WeaponData(wdata, fire_mode, ammo, unk) =>
                      wdata match {
                        case CommonFieldData(faction, bops, alternate, v1, v2, v3, v4, v5, wguid) =>
                          faction mustEqual PlanetSideEmpire.NEUTRAL
                          bops mustEqual false
                          alternate mustEqual false
                          v1 mustEqual true
                          v2.isEmpty mustEqual true
                          v3 mustEqual false
                          v4.contains(false) mustEqual true
                          v5.isEmpty mustEqual true
                          wguid mustEqual PlanetSideGUID(0)
                        case _ =>
                          ko
                      }
                      fire_mode mustEqual 0
                      ammo.head.objectClass mustEqual ObjectClass.aphelion_ppa_ammo
                      ammo.head.guid mustEqual PlanetSideGUID(376)
                      ammo.head.parentSlot mustEqual 0
                      unk mustEqual false
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

    "encode (aphelion)" in {
      val obj = BattleFrameRoboticsData(
        PlacementData(6498.7344f, 1927.9844f, 16.140625f, 0, 0, 50.625f),
        CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, None, None, PlanetSideGUID(0)),
        255,
        255,
        0,
        false,
        false,
        60,
        true,
        0,
        false,
        None,
        Some(InventoryData(List(
          InventoryItemData(ObjectClass.aphelion_ppa_left, PlanetSideGUID(335), 2,
            WeaponData(
              CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)),
              0,
              List(
                InternalSlot(ObjectClass.aphelion_ppa_ammo, PlanetSideGUID(340), 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
              )
            )
          ),
          InventoryItemData(ObjectClass.aphelion_ppa_right, PlanetSideGUID(411), 3,
            WeaponData(
              CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)),
              0,
              List(
                InternalSlot(ObjectClass.aphelion_ppa_ammo, PlanetSideGUID(342), 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
              )
            )
          ),
          InventoryItemData(ObjectClass.aphelion_plasma_rocket_pod, PlanetSideGUID(409), 4,
            WeaponData(
              CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)),
              0,
              List(
                InternalSlot(ObjectClass.aphelion_plasma_rocket_ammo, PlanetSideGUID(359), 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
              )
            )
          )
        )))
      )
      val msg = ObjectCreateMessage(ObjectClass.aphelion_gunner, PlanetSideGUID(447), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_aphelion_gunner
    }

    "encode (eclipse)" in {
      val obj = BattleFrameRoboticsData(
        PlacementData(6498.7344f, 1927.9844f, 16.140625f, 0, 0, 50.625f),
        CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, None, None, PlanetSideGUID(0)),
        255,
        255,
        0,
        false,
        false,
        0,
        true,
        0,
        false,
        Some(false),
        Some(InventoryData(List(
          InventoryItemData(ObjectClass.aphelion_ppa_left, PlanetSideGUID(385), 1,
            WeaponData(
              CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)),
              0,
              List(
                InternalSlot(ObjectClass.aphelion_ppa_ammo, PlanetSideGUID(371), 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
              )
            )
          ),
          InventoryItemData(ObjectClass.aphelion_ppa_right, PlanetSideGUID(336), 2,
            WeaponData(
              CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)),
              0,
              List(
                InternalSlot(ObjectClass.aphelion_ppa_ammo, PlanetSideGUID(376), 0, CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false))
              )
            )
          )
        )))
      )
      val msg = ObjectCreateMessage(ObjectClass.aphelion_flight, PlanetSideGUID(393), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_aphelion_flight
    }
  }
}

