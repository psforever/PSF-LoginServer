// Copyright (c) 2017 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types._
import org.specs2.mutable._
import scodec.bits._

class MountedVehiclesTest extends Specification {
  val string_mosquito_seated =
    hex"17c70700009e2d410d8ed818f1a4017047f7ffbc6390ffbe01801cff00003c08791801d00000002340530063007200610077006e00790052" ++
      hex"006f006e006e0069006500020b7e67b540404001000000000022b50100268042006c00610063006b00200042006500720065007400200041" ++
      hex"0072006d006f007500720065006400200043006f00720070007300170040030050040003bc00000234040001a00400027a7a0809a6910800" ++
      hex"00000008090a6403603000001082202e040000000202378ae0e80c00000162710b82000000008083837032030000015e2583210000000020" ++
      hex"20e21c0c80c000007722120e81c0000000808063483603000000"

  "decode (Scrawny Ronnie's mosquito)" in {
    PacketCoding.DecodePacket(string_mosquito_seated).require match {
      case ObjectCreateMessage(len, cls, guid, parent, data) =>
        len mustEqual 1991
        cls mustEqual ObjectClass.mosquito
        guid mustEqual PlanetSideGUID(4308)
        parent mustEqual None
        data match {
          case Some(vdata : VehicleData) =>
            vdata.basic.pos.coord mustEqual Vector3(4571.6875f, 5602.1875f, 93)
            vdata.basic.pos.orient mustEqual Vector3(11.25f, 2.8125f, 92.8125f)
            vdata.basic.pos.vel mustEqual Some(Vector3(31.71875f, 8.875f, -0.03125f))
            vdata.basic.faction mustEqual PlanetSideEmpire.TR
            vdata.basic.bops mustEqual false
            vdata.basic.destroyed mustEqual false
            vdata.basic.jammered mustEqual false
            vdata.basic.player_guid mustEqual PlanetSideGUID(1888)
            vdata.unk1 mustEqual 0
            vdata.health mustEqual 255
            vdata.unk2 mustEqual false
            vdata.no_mount_points mustEqual false
            vdata.driveState mustEqual DriveState.Mobile
            vdata.unk3 mustEqual false
            vdata.unk5 mustEqual false
            vdata.cloak mustEqual false
            vdata.unk4 mustEqual Some(VariantVehicleData(7))
            vdata.inventory match {
              case Some(InventoryData(list)) =>
                list.head.objectClass mustEqual ObjectClass.avatar
                list.head.guid mustEqual PlanetSideGUID(3776)
                list.head.parentSlot mustEqual 0
                list.head.obj match {
                  case PlayerData(pos, app, char, Some(InventoryData(inv)), hand) =>
                    pos mustEqual None
                    app.app.name mustEqual "ScrawnyRonnie"
                    app.app.faction mustEqual PlanetSideEmpire.TR
                    app.app.sex mustEqual CharacterGender.Male
                    app.app.head mustEqual 5
                    app.app.voice mustEqual 5
                    app.voice2 mustEqual 3
                    app.black_ops mustEqual false
                    app.lfs mustEqual false
                    app.outfit_name mustEqual "Black Beret Armoured Corps"
                    app.outfit_logo mustEqual 23
                    app.facingPitch mustEqual 354.375f
                    app.facingYawUpper mustEqual 0.0f
                    app.altModelBit mustEqual None
                    app.charging_pose mustEqual false
                    app.on_zipline mustEqual false
                    app.backpack mustEqual false
                    app.ribbons.upper mustEqual MeritCommendation.MarkovVeteran
                    app.ribbons.middle mustEqual MeritCommendation.HeavyInfantry4
                    app.ribbons.lower mustEqual MeritCommendation.TankBuster7
                    app.ribbons.tos mustEqual MeritCommendation.SixYearTR
                    char.health mustEqual 100
                    char.armor mustEqual 0
                    char.uniform_upgrade mustEqual UniformStyle.ThirdUpgrade
                    char.command_rank mustEqual 5
                    char.implant_effects mustEqual None
                    char.cosmetics mustEqual Some(Cosmetics(true, true, true, true, false))
                    inv.size mustEqual 4
                    inv.head.objectClass mustEqual ObjectClass.medicalapplicator
                    inv.head.parentSlot mustEqual 0
                    inv(1).objectClass mustEqual ObjectClass.bank
                    inv(1).parentSlot mustEqual 1
                    inv(2).objectClass mustEqual ObjectClass.mini_chaingun
                    inv(2).parentSlot mustEqual 2
                    inv(3).objectClass mustEqual ObjectClass.chainblade
                    inv(3).parentSlot mustEqual 4
                    hand mustEqual DrawnSlot.None
                  case _ =>
                    ko
                }
                list(1).objectClass mustEqual ObjectClass.rotarychaingun_mosquito
                list(1).parentSlot mustEqual 1
              case None =>
                ko
            }
          case _ =>
            ko
        }
      case _ =>
        ko
    }
  }

  "encode (Scrawny Ronnie's mosquito)" in {
    val app : (Int)=>CharacterAppearanceData = CharacterAppearanceData(
      BasicCharacterData("ScrawnyRonnie", PlanetSideEmpire.TR, CharacterGender.Male, 5, 5),
      3,
      false, false,
      ExoSuitType.Agile,
      "Black Beret Armoured Corps",
      23,
      false,
      354.375f, 0.0f,
      false,
      GrenadeState.None, false, false, false,
      RibbonBars(
        MeritCommendation.MarkovVeteran,
        MeritCommendation.HeavyInfantry4,
        MeritCommendation.TankBuster7,
        MeritCommendation.SixYearTR
      )
    )
    val char : (Boolean,Boolean)=>CharacterData = CharacterData(
      100, 0,
      UniformStyle.ThirdUpgrade,
      0,
      5,
      None,
      Some(Cosmetics(true, true, true, true, false))
    )
    val inv : InventoryData = InventoryData(
      List(
        InternalSlot(ObjectClass.medicalapplicator, PlanetSideGUID(4201), 0,
          WeaponData(0, 0, 0, List(InternalSlot(ObjectClass.health_canister, PlanetSideGUID(3472), 0, AmmoBoxData(0))))
        ),
        InternalSlot(ObjectClass.bank, PlanetSideGUID(2952), 1,
          WeaponData(0, 0, 0, List(InternalSlot(ObjectClass.armor_canister, PlanetSideGUID(3758), 0, AmmoBoxData(0))))
        ),
        InternalSlot(ObjectClass.mini_chaingun, PlanetSideGUID(2929), 2,
          WeaponData(0, 0, 0, List(InternalSlot(ObjectClass.bullet_9mm, PlanetSideGUID(3292), 0, AmmoBoxData(0))))
        ),
        InternalSlot(ObjectClass.chainblade, PlanetSideGUID(3222), 4,
          WeaponData(0, 0, 0, List(InternalSlot(ObjectClass.melee_ammo, PlanetSideGUID(3100), 0, AmmoBoxData(0))))
        )
      )
    )
    val player = VehicleData.PlayerData(app, char, inv, DrawnSlot.None, VehicleData.InitialStreamLengthToSeatEntries(true, VehicleFormat.Variant))
    val obj = VehicleData(
      CommonFieldData(
        PlacementData(
          Vector3(4571.6875f, 5602.1875f, 93),
          Vector3(11.25f, 2.8125f, 92.8125f),
          Some(Vector3(31.71875f, 8.875f, -0.03125f))
        ),
        PlanetSideEmpire.TR,
        false, false, 0, false,
        PlanetSideGUID(1888)
      ),
      0, 255,
      false, false,
      DriveState.Mobile,
      false, false, false,
      Some(VariantVehicleData(7)),
      Some(
        InventoryData(
          List(
            InternalSlot(ObjectClass.avatar, PlanetSideGUID(3776), 0, player),
            InternalSlot(ObjectClass.rotarychaingun_mosquito, PlanetSideGUID(3602), 1,
              WeaponData(6, 0, 0, List(InternalSlot(ObjectClass.bullet_12mm, PlanetSideGUID(3538), 0, AmmoBoxData(0))))
            )
          )
        )
      )
    )(VehicleFormat.Variant)
    val msg = ObjectCreateMessage(ObjectClass.mosquito, PlanetSideGUID(4308), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    val pkt_bitv = pkt.toBitVector
    val ori_bitv = string_mosquito_seated.toBitVector
    pkt_bitv.take(555) mustEqual ori_bitv.take(555) //skip 126
    pkt_bitv.drop(681).take(512) mustEqual ori_bitv.drop(681).take(512) //renew
    pkt_bitv.drop(1193).take(88) mustEqual ori_bitv.drop(1193).take(88) //skip 3
    pkt_bitv.drop(1284).take(512) mustEqual ori_bitv.drop(1284).take(512) //renew
    pkt_bitv.drop(1796) mustEqual ori_bitv.drop(1796)
    //TODO work on CharacterData to make this pass as a single stream
  }
}

