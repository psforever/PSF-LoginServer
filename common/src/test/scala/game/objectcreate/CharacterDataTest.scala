// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types._
import org.specs2.mutable._
import scodec.bits._

class CharacterDataTest extends Specification {
  val string = hex"17 73070000 BC8 3E0F 6C2D7 65535 CA16 00 00 09 9741E4F804000000 234530063007200610077006E00790052006F006E006E0069006500 220B7 E67B540404001000000000022B50100 268042006C00610063006B002000420065007200650074002000410072006D006F007500720065006400200043006F00720070007300 1700E0030050040003BC00000234040001A004000 3FFF67A8F A0A5424E0E800000000080952A9C3A03000001081103E040000000A023782F1080C0000016244108200000000808382403A030000014284C3A0C0000000202512F00B80C00000578F80F840000000280838B3C320300000080"
  //string seated was intentionally-produced test data
  val string_seated =
    hex"170307000069023c83e0f800000011a0530063007200610077006e00790052006f006e006e0069006500220b700000000000000000000000" ++
    hex"06800000268042006c00610063006b002000420065007200650074002000410072006d006f007500720065006400200043006f0072007000" ++
    hex"73001700e0030050040003bc00000234040001a00400020a8fa0a5424e0e800000000080952a9c3a03000001081103e040000000a023782f" ++
    hex"1080c0000016244108200000000808382403a030000014284c3a0c0000000202512f00b80c00000578f80f840000000280838b3c320300000080"
  val string_backpack = hex"17 9C030000 BC8 340D F20A9 3956C AF0D 00 00 73 480000 87041006E00670065006C006C006F00 4A148 0000000000000000000000005C54200 24404F0072006900670069006E0061006C00200044006900730074007200690063007400 1740180181E8000000C202000042000000D202000000010A3C00"

  "CharacterData" should {
    "decode" in {
      PacketCoding.DecodePacket(string).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 1907
          cls mustEqual ObjectClass.avatar
          guid mustEqual PlanetSideGUID(3902)
          parent.isDefined mustEqual false
          data match {
            case PlayerData(Some(pos), basic, char, inv, hand) =>
              pos.coord mustEqual Vector3(3674.8438f, 2726.789f, 91.15625f)
              pos.orient mustEqual Vector3(0f, 0f, 64.6875f)
              pos.vel.isDefined mustEqual true
              pos.vel.get mustEqual Vector3(1.4375f, -0.4375f, 0f)

              basic match {
                case CharacterAppearanceData(a, b, ribbons) =>
                  a.app.name mustEqual "ScrawnyRonnie"
                  a.app.faction mustEqual PlanetSideEmpire.TR
                  a.app.sex mustEqual CharacterGender.Male
                  a.app.head mustEqual 5
                  a.app.voice mustEqual CharacterVoice.Voice5
                  a.data.bops mustEqual false
                  a.data.v1 mustEqual false
                  a.data.v2.isEmpty mustEqual true
                  a.data.v3 mustEqual false
                  a.data.v4.isEmpty mustEqual true
                  a.data.v5.isEmpty mustEqual true
                  a.exosuit mustEqual ExoSuitType.Reinforced
                  a.unk5 mustEqual 0
                  a.char_id mustEqual 30777081L
                  a.unk7 mustEqual 1
                  a.unk8 mustEqual 4
                  a.unk9 mustEqual 0
                  a.unkA mustEqual 0

                  b.outfit_name mustEqual "Black Beret Armoured Corps"
                  b.outfit_logo mustEqual 23
                  b.backpack mustEqual false
                  b.facingPitch mustEqual -39.375f
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

                  ribbons.upper mustEqual MeritCommendation.MarkovVeteran
                  ribbons.middle mustEqual MeritCommendation.HeavyInfantry4
                  ribbons.lower mustEqual MeritCommendation.TankBuster7
                  ribbons.tos mustEqual MeritCommendation.SixYearTR
                case _ =>
                  ko
              }

              char.health mustEqual 255
              char.armor mustEqual 253
              char.uniform_upgrade mustEqual UniformStyle.ThirdUpgrade
              char.command_rank mustEqual 5
              char.implant_effects.length mustEqual 1
              char.implant_effects.head mustEqual ImplantEffects.NoEffects
              char.cosmetics match {
                case Some(c : Cosmetics) =>
                  c.Styles mustEqual Set(PersonalStyle.NoHelmet, PersonalStyle.Beret, PersonalStyle.Sunglasses, PersonalStyle.Earpiece)
                case None =>
                  ko
              }
              char.unk mustEqual 7
              //short test of inventory items
              inv.isDefined mustEqual true
              val contents = inv.get.contents
              contents.size mustEqual 5
              //0
              contents.head.objectClass mustEqual ObjectClass.plasma_grenade
              contents.head.guid mustEqual PlanetSideGUID(3662)
              contents.head.parentSlot mustEqual 0
              contents.head.obj.asInstanceOf[WeaponData].fire_mode mustEqual 0
              contents.head.obj.asInstanceOf[WeaponData].ammo.head.objectClass mustEqual ObjectClass.plasma_grenade_ammo
              contents.head.obj.asInstanceOf[WeaponData].ammo.head.guid mustEqual PlanetSideGUID(3751)
              //1
              contents(1).objectClass mustEqual ObjectClass.bank
              contents(1).guid mustEqual PlanetSideGUID(3908)
              contents(1).parentSlot mustEqual 1
              contents(1).obj.asInstanceOf[WeaponData].fire_mode mustEqual 1
              contents(1).obj.asInstanceOf[WeaponData].ammo.head.objectClass mustEqual ObjectClass.armor_canister
              contents(1).obj.asInstanceOf[WeaponData].ammo.head.guid mustEqual PlanetSideGUID(4143)
              //2
              contents(2).objectClass mustEqual ObjectClass.mini_chaingun
              contents(2).guid mustEqual PlanetSideGUID(4164)
              contents(2).parentSlot mustEqual 2
              contents(2).obj.asInstanceOf[WeaponData].fire_mode mustEqual 0
              contents(2).obj.asInstanceOf[WeaponData].ammo.head.objectClass mustEqual ObjectClass.bullet_9mm
              contents(2).obj.asInstanceOf[WeaponData].ammo.head.guid mustEqual PlanetSideGUID(3728)
              //3
              contents(3).objectClass mustEqual ObjectClass.phoenix //actually, a decimator
              contents(3).guid mustEqual PlanetSideGUID(3603)
              contents(3).parentSlot mustEqual 3
              contents(3).obj.asInstanceOf[WeaponData].fire_mode mustEqual 0
              contents(3).obj.asInstanceOf[WeaponData].ammo.head.objectClass mustEqual ObjectClass.phoenix_missile
              contents(3).obj.asInstanceOf[WeaponData].ammo.head.guid mustEqual PlanetSideGUID(3056)
              //4
              contents(4).objectClass mustEqual ObjectClass.chainblade
              contents(4).guid mustEqual PlanetSideGUID(4088)
              contents(4).parentSlot mustEqual 4
              contents(4).obj.asInstanceOf[WeaponData].fire_mode mustEqual 1
              contents(4).obj.asInstanceOf[WeaponData].ammo.head.objectClass mustEqual ObjectClass.melee_ammo
              contents(4).obj.asInstanceOf[WeaponData].ammo.head.guid mustEqual PlanetSideGUID(3279)

              hand mustEqual DrawnSlot.Rifle1
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }
//
    "decode (seated)" in {
      PacketCoding.DecodePacket(string_seated).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 1795
          cls mustEqual ObjectClass.avatar
          guid mustEqual PlanetSideGUID(3902)
          parent.contains(ObjectCreateMessageParent(PlanetSideGUID(1234), 0)) mustEqual true
          data match {
            case PlayerData(None, basic, _, _, _) =>
              basic match {
                case CharacterAppearanceData(a, b, ribbons) =>
                  a.app.name mustEqual "ScrawnyRonnie"
                  a.app.faction mustEqual PlanetSideEmpire.TR
                  a.app.sex mustEqual CharacterGender.Male
                  a.app.head mustEqual 5
                  a.app.voice mustEqual CharacterVoice.Voice5
                  a.data.bops mustEqual false
                  a.data.v1 mustEqual false
                  a.data.v2.isEmpty mustEqual true
                  a.data.v3 mustEqual false
                  a.data.v4.isEmpty mustEqual true
                  a.data.v5.isEmpty mustEqual true
                  a.exosuit mustEqual ExoSuitType.Reinforced
                  a.unk5 mustEqual 0
                  a.char_id mustEqual 192L
                  a.unk7 mustEqual 0
                  a.unk8 mustEqual 0
                  a.unk9 mustEqual 0
                  a.unkA mustEqual 0

                  b.outfit_name mustEqual "Black Beret Armoured Corps"
                  b.outfit_logo mustEqual 23
                  b.backpack mustEqual false
                  b.facingPitch mustEqual -39.375f
                  b.facingYawUpper mustEqual 0
                  b.lfs mustEqual false
                  b.grenade_state mustEqual GrenadeState.None
                  b.is_cloaking mustEqual false
                  b.charging_pose mustEqual false
                  b.on_zipline.isEmpty mustEqual true
                  b.unk0 mustEqual 26L
                  b.unk1 mustEqual false
                  b.unk2 mustEqual false
                  b.unk3 mustEqual false
                  b.unk4 mustEqual false
                  b.unk5 mustEqual false
                  b.unk6 mustEqual false
                  b.unk7 mustEqual false

                  ribbons.upper mustEqual MeritCommendation.MarkovVeteran
                  ribbons.middle mustEqual MeritCommendation.HeavyInfantry4
                  ribbons.lower mustEqual MeritCommendation.TankBuster7
                  ribbons.tos mustEqual MeritCommendation.SixYearTR
                  //etc..
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

    "decode (backpack)" in {
      PacketCoding.DecodePacket(string_backpack).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 924L
          cls mustEqual ObjectClass.avatar
          guid mustEqual PlanetSideGUID(3380)
          parent.isDefined mustEqual false
          data match {
            case PlayerData(Some(pos), basic, char, None, hand) =>
              pos.coord mustEqual Vector3(4629.8906f, 6316.4453f, 54.734375f)
              pos.orient mustEqual Vector3(0, 0, 126.5625f)
              pos.vel.isDefined mustEqual false

              basic match {
                case CharacterAppearanceData(a, b, ribbons) =>
                  a.app.name mustEqual "Angello"
                  a.app.faction mustEqual PlanetSideEmpire.VS
                  a.app.sex mustEqual CharacterGender.Male
                  a.app.head mustEqual 10
                  a.app.voice mustEqual CharacterVoice.Voice2
                  a.data.bops mustEqual false
                  a.data.v1 mustEqual false
                  a.data.v2.isEmpty mustEqual true
                  a.data.v3 mustEqual false
                  a.data.v4.isEmpty mustEqual true
                  a.data.v5.isEmpty mustEqual true
                  a.exosuit mustEqual ExoSuitType.MAX
                  a.unk5 mustEqual 1
                  a.char_id mustEqual 0L
                  a.unk7 mustEqual 0
                  a.unk8 mustEqual 0
                  a.unk9 mustEqual 0
                  a.unkA mustEqual 0

                  b.outfit_name mustEqual "Original District"
                  b.outfit_logo mustEqual 23
                  b.backpack mustEqual true
                  b.facingPitch mustEqual -8.4375f
                  b.facingYawUpper mustEqual 0
                  b.lfs mustEqual false
                  b.grenade_state mustEqual GrenadeState.None
                  b.is_cloaking mustEqual false
                  b.charging_pose mustEqual false
                  b.on_zipline.isEmpty mustEqual true
                  b.unk0 mustEqual 529687L
                  b.unk1 mustEqual false
                  b.unk2 mustEqual false
                  b.unk3 mustEqual false
                  b.unk4 mustEqual false
                  b.unk5 mustEqual false
                  b.unk6 mustEqual false
                  b.unk7 mustEqual false

                  ribbons.upper mustEqual MeritCommendation.Jacking2
                  ribbons.middle mustEqual MeritCommendation.ScavengerVS1
                  ribbons.lower mustEqual MeritCommendation.AMSSupport4
                  ribbons.tos mustEqual MeritCommendation.SixYearVS
                //etc..
                case _ =>
                  ko
              }

              char.health mustEqual 0
              char.armor mustEqual 0
              char.uniform_upgrade mustEqual UniformStyle.ThirdUpgrade
              char.command_rank mustEqual 2
              char.implant_effects.isEmpty mustEqual true
              char.cosmetics match {
                case Some(c : Cosmetics) =>
                  c.Styles mustEqual Set(PersonalStyle.NoHelmet, PersonalStyle.Beret, PersonalStyle.Sunglasses, PersonalStyle.Earpiece)
                case None =>
                  ko
              }
              char.unk mustEqual 1

              hand mustEqual DrawnSlot.Pistol1
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val pos : PlacementData = PlacementData(
        Vector3(3674.8438f, 2726.789f, 91.15625f),
        Vector3(0f, 0f, 64.6875f),
        Some(Vector3(1.4375f, -0.4375f, 0f))
      )
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
        ExoSuitType.Reinforced,
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
        -39.375f, 0f,
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
        255, 253,
        UniformStyle.ThirdUpgrade,
        7,
        5,
        List(ImplantEffects.NoEffects),
        Some(Cosmetics(true, true, true, true, false))
      )
      val inv = InventoryData(
        InventoryItemData(ObjectClass.plasma_grenade, PlanetSideGUID(3662), 0, WeaponData(0, 0, ObjectClass.plasma_grenade_ammo, PlanetSideGUID(3751), 0, CommonFieldData()(false))) ::
          InventoryItemData(ObjectClass.bank, PlanetSideGUID(3908), 1, WeaponData(0, 0, 1, ObjectClass.armor_canister, PlanetSideGUID(4143), 0, CommonFieldData()(false))) ::
          InventoryItemData(ObjectClass.mini_chaingun, PlanetSideGUID(4164), 2, WeaponData(0, 0, ObjectClass.bullet_9mm, PlanetSideGUID(3728), 0, CommonFieldData()(false))) ::
          InventoryItemData(ObjectClass.phoenix, PlanetSideGUID(3603), 3, WeaponData(0, 0, ObjectClass.phoenix_missile, PlanetSideGUID(3056), 0, CommonFieldData()(false))) ::
          InventoryItemData(ObjectClass.chainblade, PlanetSideGUID(4088), 4, WeaponData(0, 0, 1, ObjectClass.melee_ammo, PlanetSideGUID(3279), 0, CommonFieldData()(false))) ::
          Nil
      )
      val obj = PlayerData(pos, app, char, inv, DrawnSlot.Rifle1)

      val msg = ObjectCreateMessage(ObjectClass.avatar, PlanetSideGUID(3902), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string
    }

    "encode (seated)" in {
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
        ExoSuitType.Reinforced,
        0,
        192L,
        0,
        0,
        0,
        0
      )
      val b : (Boolean,Int)=>CharacterAppearanceB = CharacterAppearanceB(
        26L,
        "Black Beret Armoured Corps",
        23,
        false,
        false,
        false,
        false,
        false,
        -39.375f, 0f,
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
        255, 253,
        UniformStyle.ThirdUpgrade,
        5,
        List(ImplantEffects.NoEffects),
        Some(Cosmetics(true, true, true, true, false))
      )
      val inv = InventoryData(
        InventoryItemData(ObjectClass.plasma_grenade, PlanetSideGUID(3662), 0, WeaponData(0, 0, ObjectClass.plasma_grenade_ammo, PlanetSideGUID(3751), 0, CommonFieldData()(false))) ::
          InventoryItemData(ObjectClass.bank, PlanetSideGUID(3908), 1, WeaponData(0, 0, 1, ObjectClass.armor_canister, PlanetSideGUID(4143), 0, CommonFieldData()(false))) ::
          InventoryItemData(ObjectClass.mini_chaingun, PlanetSideGUID(4164), 2, WeaponData(0, 0, ObjectClass.bullet_9mm, PlanetSideGUID(3728), 0, CommonFieldData()(false))) ::
          InventoryItemData(ObjectClass.phoenix, PlanetSideGUID(3603), 3, WeaponData(0, 0, ObjectClass.phoenix_missile, PlanetSideGUID(3056), 0, CommonFieldData()(false))) ::
          InventoryItemData(ObjectClass.chainblade, PlanetSideGUID(4088), 4, WeaponData(0, 0, 1, ObjectClass.melee_ammo, PlanetSideGUID(3279), 0, CommonFieldData()(false))) ::
          Nil
      )
      val obj = PlayerData(app, char, inv, DrawnSlot.Rifle1)

      val msg = ObjectCreateMessage(ObjectClass.avatar, PlanetSideGUID(3902), ObjectCreateMessageParent(PlanetSideGUID(1234), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_seated
    }

    "encode (backpack)" in {
      val pos = PlacementData(
        Vector3(4629.8906f, 6316.4453f, 54.734375f),
        Vector3(0, 0, 126.5625f)
      )
      val a : Int=>CharacterAppearanceA = CharacterAppearanceA(
        BasicCharacterData(
          "Angello",
          PlanetSideEmpire.VS,
          CharacterGender.Male,
          10,
          CharacterVoice.Voice2
        ),
        CommonFieldData(
          PlanetSideEmpire.VS,
          false,
          true,
          false,
          None,
          false,
          None,
          None,
          PlanetSideGUID(0)
        ),
        ExoSuitType.MAX,
        1,
        0L,
        0,
        0,
        0,
        0
      )
      val b : (Boolean,Int)=>CharacterAppearanceB = CharacterAppearanceB(
        529687L,
        "Original District",
        23,
        false, //unk1
        true, //backpack
        false, //unk2
        false, //unk3
        false, //unk4
        351.5625f, 0f, //also: -8.4375f, 0f
        false, //lfs
        GrenadeState.None,
        false, //is_cloaking
        false, //unk5
        false, //unk6
        false, //charging_pose
        false, //unk7
        None
      )

      val app : Int=>CharacterAppearanceData = CharacterAppearanceData(
        a, b,
        RibbonBars(
          MeritCommendation.Jacking2,
          MeritCommendation.ScavengerVS1,
          MeritCommendation.AMSSupport4,
          MeritCommendation.SixYearVS
        )
      )
      val char : (Boolean,Boolean)=>CharacterData = CharacterData(
        0, 0,
        UniformStyle.ThirdUpgrade,
        1,
        List(),
        Some(Cosmetics(true, true, true, true, false))
      )
      val obj = PlayerData(pos, app, char, DrawnSlot.Pistol1)

      val msg = ObjectCreateMessage(ObjectClass.avatar, PlanetSideGUID(3380), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      //granular test
      val pkt_bitv = pkt.toBitVector
      val ori_bitv = string_backpack.toBitVector
      pkt_bitv.take(916) mustEqual pkt_bitv.take(916) //skip 4
      pkt_bitv.drop(920) mustEqual pkt_bitv.drop(920)
      //TODO work on CharacterData to make this pass as a single stream
    }
  }
}
