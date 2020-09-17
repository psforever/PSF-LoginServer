// Copyright (c) 2017 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate._
import net.psforever.types._
import org.specs2.mutable._
import scodec.bits._

class VariantVehiclesTest extends Specification {
  val string_switchblade =
    hex"17 93010000 A7B A201 FBC1C12A832F06000021 4400003FC00001013AD3180C0E4000000408330DC03019000006620406072000000"

  "Variant vehicles" should {
    "decode (switchblade)" in {
      PacketCoding.decodePacket(string_switchblade).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 403L
          cls mustEqual ObjectClass.switchblade
          guid mustEqual PlanetSideGUID(418)
          parent.isDefined mustEqual false
          data.isInstanceOf[VehicleData] mustEqual true
          val switchblade = data.asInstanceOf[VehicleData]
          switchblade.pos.coord.x mustEqual 6531.961f
          switchblade.pos.coord.y mustEqual 1872.1406f
          switchblade.pos.coord.z mustEqual 24.734375f
          switchblade.pos.orient.x mustEqual 0f
          switchblade.pos.orient.y mustEqual 0f
          switchblade.pos.orient.z mustEqual 357.1875f
          switchblade.data.faction mustEqual PlanetSideEmpire.VS
          switchblade.data.v1 mustEqual true
          switchblade.health mustEqual 255
          switchblade.driveState mustEqual DriveState.Mobile
          switchblade.inventory.isDefined mustEqual true
          switchblade.inventory.get.contents.size mustEqual 1
          //0
          val weapon = switchblade.inventory.get.contents.head
          weapon.objectClass mustEqual ObjectClass.scythe
          weapon.guid mustEqual PlanetSideGUID(355)
          weapon.parentSlot mustEqual 1
          weapon.obj match {
            case WeaponData(
                  CommonFieldData(wfaction, wbops, walternate, wv1, wv2, wv3, wv4, wv5, wfguid),
                  fmode,
                  ammo,
                  _
                ) =>
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

              //ammo-0
              ammo.head.objectClass mustEqual ObjectClass.ancient_ammo_vehicle
              ammo.head.guid mustEqual PlanetSideGUID(366)
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
              //ammo-1
              ammo(1).objectClass mustEqual ObjectClass.ancient_ammo_vehicle
              ammo(1).guid mustEqual PlanetSideGUID(385)
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

    "encode (switchblade)" in {
      val obj = VehicleData(
        PlacementData(6531.961f, 1872.1406f, 24.734375f, 0f, 0f, 357.1875f),
        CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)),
        false,
        255,
        false,
        false,
        DriveState.Mobile,
        false,
        false,
        false,
        Some(VariantVehicleData(0)),
        Some(
          InventoryData(
            List(
              InventoryItemData(
                ObjectClass.scythe,
                PlanetSideGUID(355),
                1,
                WeaponData(
                  CommonFieldData(
                    PlanetSideEmpire.NEUTRAL,
                    false,
                    false,
                    true,
                    None,
                    false,
                    None,
                    None,
                    PlanetSideGUID(0)
                  ),
                  0,
                  List(
                    InternalSlot(
                      ObjectClass.ancient_ammo_vehicle,
                      PlanetSideGUID(366),
                      0,
                      CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false)
                    ),
                    InternalSlot(
                      ObjectClass.ancient_ammo_vehicle,
                      PlanetSideGUID(385),
                      1,
                      CommonFieldData(PlanetSideEmpire.NEUTRAL, 2)(false)
                    )
                  )
                )
              )
            )
          )
        )
      )(VehicleFormat.Variant)
      val msg = ObjectCreateMessage(ObjectClass.switchblade, PlanetSideGUID(418), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_switchblade
    }
  }
}
