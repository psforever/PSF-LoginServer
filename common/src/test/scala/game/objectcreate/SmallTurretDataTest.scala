// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
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
          data match {
            case SmallTurretData(CommonFieldDataWithPlacement(pos, deploy), health, None) =>
              pos.coord mustEqual Vector3(4577.7812f, 5624.828f, 72.046875f)
              pos.orient mustEqual Vector3(0, 2.8125f, 264.375f)

              deploy.faction mustEqual PlanetSideEmpire.NC
              deploy.bops mustEqual false
              deploy.alternate mustEqual true
              deploy.v1 mustEqual true
              deploy.v2.isEmpty mustEqual true
              deploy.jammered mustEqual false
              deploy.v4.contains(false) mustEqual true
              deploy.v5.isEmpty mustEqual true
              deploy.guid mustEqual PlanetSideGUID(7742)

              health mustEqual 0
            case _ =>
              ko
          }
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
          data match {
            case SmallTurretData(CommonFieldDataWithPlacement(pos, deploy), health, Some(InventoryData(inv))) =>
              pos.coord mustEqual Vector3(4527.633f, 6271.3594f, 70.265625f)
              pos.orient mustEqual Vector3.z(154.6875f)

              deploy.faction mustEqual PlanetSideEmpire.VS
              deploy.bops mustEqual false
              deploy.alternate mustEqual false
              deploy.v1 mustEqual true
              deploy.v2.isEmpty mustEqual true
              deploy.jammered mustEqual false
              deploy.v4.contains(true) mustEqual true
              deploy.v5.isEmpty mustEqual true
              deploy.guid mustEqual PlanetSideGUID(8208)

              health mustEqual 255

              inv.head.objectClass mustEqual ObjectClass.spitfire_weapon
              inv.head.guid mustEqual PlanetSideGUID(3064)
              inv.head.parentSlot mustEqual 0
              inv.head.obj.isInstanceOf[WeaponData] mustEqual true
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

                  ammo.objectClass mustEqual ObjectClass.spitfire_ammo
                  ammo.guid mustEqual PlanetSideGUID(3694)
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

    "encode (spitfire, short)" in {
      val obj = SmallTurretData(
        CommonFieldDataWithPlacement(
          PlacementData(Vector3(4577.7812f, 5624.828f, 72.046875f), Vector3(0, 2.8125f, 264.375f)),
          CommonFieldData(PlanetSideEmpire.NC, false, true, true, None, false, Some(false), None, PlanetSideGUID(7742))
        ),
        0
      )
      val msg = ObjectCreateMessage(ObjectClass.spitfire_turret, PlanetSideGUID(4208), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_spitfire_short
    }

    "encode (spitfire)" in {
      val obj = SmallTurretData(
        CommonFieldDataWithPlacement(
          PlacementData(Vector3(4527.633f, 6271.3594f, 70.265625f), Vector3(0, 0, 154.6875f)),
          CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, Some(true), None, PlanetSideGUID(8208))
        ),
        255,
        InventoryData(List(
          InternalSlot(ObjectClass.spitfire_weapon, PlanetSideGUID(3064), 0,
            WeaponData(
              CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0)),
              0,
              List(InternalSlot(ObjectClass.spitfire_ammo, PlanetSideGUID(3694), 0,
                CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, Some(false), None, PlanetSideGUID(0))
              ))
            )
          )
          //SmallTurretData.spitfire(PlanetSideGUID(3064), 0x6, 0x8, PlanetSideGUID(3694), 8)
        ))
      )
      val msg = ObjectCreateMessage(ObjectClass.spitfire_turret, PlanetSideGUID(4265), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_spitfire
    }
  }
}
