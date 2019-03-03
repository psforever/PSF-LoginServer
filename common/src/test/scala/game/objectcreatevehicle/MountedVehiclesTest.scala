// Copyright (c) 2017 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.objectcreate._
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
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
        parent.isEmpty mustEqual true
        data match {
          case vdata : VehicleData =>
            vdata.pos.coord mustEqual Vector3(4571.6875f, 5602.1875f, 93)
            vdata.pos.orient mustEqual Vector3(11.25f, 2.8125f, 92.8125f)
            vdata.pos.vel.contains(Vector3(31.71875f, 8.875f, -0.03125f)) mustEqual true
            vdata.data.faction mustEqual PlanetSideEmpire.TR
            vdata.data.bops mustEqual false
            vdata.data.alternate mustEqual false
            vdata.data.v1 mustEqual false
            vdata.data.v3 mustEqual false
            vdata.data.v5.isEmpty mustEqual true
            vdata.data.guid mustEqual PlanetSideGUID(3776)
            vdata.health mustEqual 255
            vdata.no_mount_points mustEqual false
            vdata.driveState mustEqual DriveState.Mobile
            vdata.cloak mustEqual false
            vdata.unk3 mustEqual false
            vdata.unk4 mustEqual false
            vdata.unk5 mustEqual false
            vdata.unk6 mustEqual false
            vdata.vehicle_format_data.contains(VariantVehicleData(7)) mustEqual true
            vdata.inventory match {
              case Some(InventoryData(list)) =>
                list.head.objectClass mustEqual ObjectClass.avatar
                list.head.guid mustEqual PlanetSideGUID(3776)
                list.head.parentSlot mustEqual 0
                list.head.obj match {
                  case PlayerData(None, app, char, Some(InventoryData(inv)), DrawnSlot.None) =>
                    app match {
                      case CharacterAppearanceData(a, b, ribbons) =>
                        a.app mustEqual BasicCharacterData("ScrawnyRonnie", PlanetSideEmpire.TR, CharacterGender.Male, 5, CharacterVoice.Voice5)
                        a.data.bops mustEqual false
                        a.data.v1 mustEqual false
                        a.data.v2.isEmpty mustEqual true
                        a.data.v3 mustEqual false
                        a.data.v4.isEmpty mustEqual true
                        a.data.v5.isEmpty mustEqual true
                        a.exosuit mustEqual ExoSuitType.Agile
                        a.unk5 mustEqual 0
                        a.unk6 mustEqual 30777081L
                        a.unk7 mustEqual 1
                        a.unk8 mustEqual 4
                        a.unk9 mustEqual 0
                        a.unkA mustEqual 0

                        b.outfit_name mustEqual "Black Beret Armoured Corps"
                        b.outfit_logo mustEqual 23
                        b.backpack mustEqual false
                        b.facingPitch mustEqual 348.75f
                        b.facingYawUpper mustEqual 0
                        b.lfs mustEqual false
                        b.grenade_state mustEqual GrenadeState.None
                        b.is_cloaking mustEqual false
                        b.charging_pose mustEqual false
                        b.on_zipline.isEmpty mustEqual true
                        b.unk0 mustEqual 316554L
                        b.unk1 mustEqual false
                        b.unk2 mustEqual false
                        b.unk3 mustEqual false
                        b.unk4 mustEqual false
                        b.unk5 mustEqual false
                        b.unk6 mustEqual false
                        b.unk7 mustEqual false

                      case _ =>
                        ko
                    }

                    char match {
                      case CharacterData(health, armor, uniform, unk, cr, implants, cosmetics) =>
                        health mustEqual 100
                        armor mustEqual 0
                        uniform mustEqual UniformStyle.ThirdUpgrade
                        unk mustEqual 7
                        cr mustEqual 5
                        implants mustEqual Nil
                        cosmetics.contains(Cosmetics(true, true, true, true, false)) mustEqual true
                      case _ =>
                        ko
                    }
                    //briefly ...
                    inv.size mustEqual 4
                    inv.head.objectClass mustEqual ObjectClass.medicalapplicator
                    inv.head.parentSlot mustEqual 0
                    inv(1).objectClass mustEqual ObjectClass.bank
                    inv(1).parentSlot mustEqual 1
                    inv(2).objectClass mustEqual ObjectClass.mini_chaingun
                    inv(2).parentSlot mustEqual 2
                    inv(3).objectClass mustEqual ObjectClass.chainblade
                    inv(3).parentSlot mustEqual 4
                  case _ =>
                    ko
                }
                //back to mosquito inventory
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
    val a : Int=>CharacterAppearanceA = CharacterAppearanceA(
      BasicCharacterData(
        "ScrawnyRonnie",
        PlanetSideEmpire.TR,
        CharacterGender.Male,
        5,
        CharacterVoice.Voice5
      ),
      CommonFieldData(
        PlanetSideEmpire.TR,
        false,
        false,
        false,
        None,
        false,
        None,
        None,
        PlanetSideGUID(0)
      ),
      ExoSuitType.Agile,
      0,
      30777081L,
      1,
      4,
      0,
      0
    )
    val b : (Boolean,Int)=>CharacterAppearanceB = CharacterAppearanceB(
      316554L,
      "Black Beret Armoured Corps",
      23,
      false,
      false,
      false,
      false,
      false,
      348.75f, 0,
      false,
      GrenadeState.None,
      false,
      false,
      false,
      false,
      false,
      None
    )

    val app : Int=>CharacterAppearanceData = CharacterAppearanceData(
      a, b,
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
      7,
      5,
      Nil,
      Some(Cosmetics(true, true, true, true, false))
    )
    val inv : InventoryData = InventoryData(
      List(
        InternalSlot(ObjectClass.medicalapplicator, PlanetSideGUID(4201), 0,
          WeaponData(CommonFieldData(PlanetSideEmpire.TR), 0, List(InternalSlot(ObjectClass.health_canister, PlanetSideGUID(3472), 0, CommonFieldData()(false))))
        ),
        InternalSlot(ObjectClass.bank, PlanetSideGUID(2952), 1,
          WeaponData(CommonFieldData(PlanetSideEmpire.TR), 0, List(InternalSlot(ObjectClass.armor_canister, PlanetSideGUID(3758), 0, CommonFieldData()(false))))
        ),
        InternalSlot(ObjectClass.mini_chaingun, PlanetSideGUID(2929), 2,
          WeaponData(CommonFieldData(PlanetSideEmpire.TR), 0, List(InternalSlot(ObjectClass.bullet_9mm, PlanetSideGUID(3292), 0, CommonFieldData()(false))))
        ),
        InternalSlot(ObjectClass.chainblade, PlanetSideGUID(3222), 4,
          WeaponData(CommonFieldData(PlanetSideEmpire.TR), 0, List(InternalSlot(ObjectClass.melee_ammo, PlanetSideGUID(3100), 0, CommonFieldData()(false))))
        )
      )
    )
    val player = VehicleData.PlayerData(app, char, inv, DrawnSlot.None, VehicleData.InitialStreamLengthToSeatEntries(true, VehicleFormat.Variant))
    val obj = VehicleData(
      PlacementData(
        Vector3(4571.6875f, 5602.1875f, 93),
        Vector3(11.25f, 2.8125f, 92.8125f),
        Some(Vector3(31.71875f, 8.875f, -0.03125f))
      ),
      CommonFieldData(PlanetSideEmpire.TR, false, false, false, None, false, Some(false), None, PlanetSideGUID(3776)),
      false,
      255,
      false, false,
      DriveState.Mobile,
      false, false, false,
      Some(VariantVehicleData(7)),
      Some(
        InventoryData(
          List(
            InternalSlot(ObjectClass.avatar, PlanetSideGUID(3776), 0, player),
            InternalSlot(ObjectClass.rotarychaingun_mosquito, PlanetSideGUID(3602), 1,
              WeaponData(CommonFieldData(), 0, List(InternalSlot(ObjectClass.bullet_12mm, PlanetSideGUID(3538), 0, CommonFieldData()(false))))
            )
          )
        )
      )
    )(VehicleFormat.Variant)
    val msg = ObjectCreateMessage(ObjectClass.mosquito, PlanetSideGUID(4308), obj)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string_mosquito_seated
  }
}

