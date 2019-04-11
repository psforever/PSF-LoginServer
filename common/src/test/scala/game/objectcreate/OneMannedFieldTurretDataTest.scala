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
          data match {
            case OneMannedFieldTurretData(CommonFieldDataWithPlacement(pos, deploy), health, Some(InventoryData(inv))) =>
              pos.coord mustEqual Vector3(3567.1406f, 2988.0078f, 71.84375f)
              pos.orient mustEqual Vector3.z(185.625f)
              deploy.faction mustEqual PlanetSideEmpire.VS
              deploy.bops mustEqual false
              deploy.alternate mustEqual false
              deploy.v1 mustEqual true
              deploy.v2.isEmpty mustEqual true
              deploy.v3 mustEqual false
              deploy.v4.contains(false) mustEqual true
              deploy.v5.isEmpty mustEqual true
              deploy.guid mustEqual PlanetSideGUID(2502)

              health mustEqual 255

              inv.head.objectClass mustEqual ObjectClass.energy_gun_vs
              inv.head.guid mustEqual PlanetSideGUID(2615)
              inv.head.parentSlot mustEqual 1
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

                  ammo.objectClass mustEqual ObjectClass.energy_gun_ammo
                  ammo.guid mustEqual PlanetSideGUID(2510)
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

    "encode (orion)" in {
      val obj = OneMannedFieldTurretData(
        CommonFieldDataWithPlacement(
          PlacementData(Vector3(3567.1406f, 2988.0078f, 71.84375f), Vector3.z(185.625f)),
          CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, Some(false), None, PlanetSideGUID(2502))
        ),
        255,
        InventoryData(List(
          InternalSlot(ObjectClass.energy_gun_vs, PlanetSideGUID(2615), 1,
            WeaponData(
              CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0)),
              0,
              List(InternalSlot(ObjectClass.energy_gun_ammo, PlanetSideGUID(2510), 0,
                CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)))
              )
            )
          )
        ))
      )
      val msg = ObjectCreateMessage(ObjectClass.portable_manned_turret_vs, PlanetSideGUID(2916), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_orion
    }
  }
}
