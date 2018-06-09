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
            case Some(PlayerData(Some(pos), basic, char, inv, hand)) =>
              pos.coord mustEqual Vector3(3674.8438f, 2726.789f, 91.15625f)
              pos.orient mustEqual Vector3(0f, 0f, 64.6875f)
              pos.vel.isDefined mustEqual true
              pos.vel.get mustEqual Vector3(1.4375f, -0.4375f, 0f)

              basic.app.name mustEqual "ScrawnyRonnie"
              basic.app.faction mustEqual PlanetSideEmpire.TR
              basic.app.sex mustEqual CharacterGender.Male
              basic.app.head mustEqual 5
              basic.app.voice mustEqual CharacterVoice.Voice5
              basic.voice2 mustEqual 3
              basic.black_ops mustEqual false
              basic.jammered mustEqual false
              basic.exosuit mustEqual ExoSuitType.Reinforced
              basic.outfit_name mustEqual "Black Beret Armoured Corps"
              basic.outfit_logo mustEqual 23
              basic.facingPitch mustEqual 340.3125f
              basic.facingYawUpper mustEqual 0
              basic.lfs mustEqual false
              basic.grenade_state mustEqual GrenadeState.None
              basic.is_cloaking mustEqual false
              basic.charging_pose mustEqual false
              basic.on_zipline mustEqual false
              basic.ribbons.upper mustEqual MeritCommendation.MarkovVeteran
              basic.ribbons.middle mustEqual MeritCommendation.HeavyInfantry4
              basic.ribbons.lower mustEqual MeritCommendation.TankBuster7
              basic.ribbons.tos mustEqual MeritCommendation.SixYearTR

              char.health mustEqual 255
              char.armor mustEqual 253
              char.uniform_upgrade mustEqual UniformStyle.ThirdUpgrade
              char.command_rank mustEqual 5
              char.implant_effects.isDefined mustEqual true
              char.implant_effects.get mustEqual ImplantEffects.NoEffects
              char.cosmetics.isDefined mustEqual true
              char.cosmetics.get.no_helmet mustEqual true
              char.cosmetics.get.beret mustEqual true
              char.cosmetics.get.sunglasses mustEqual true
              char.cosmetics.get.earpiece mustEqual true
              char.cosmetics.get.brimmed_cap mustEqual false
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

    "decode (seated)" in {
      PacketCoding.DecodePacket(string_seated).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 1795
          cls mustEqual ObjectClass.avatar
          guid mustEqual PlanetSideGUID(3902)
          parent mustEqual Some(ObjectCreateMessageParent(PlanetSideGUID(1234), 0))
          data match {
            case Some(PlayerData(None, basic, char, inv, hand)) =>
              basic.app.name mustEqual "ScrawnyRonnie"
              basic.app.faction mustEqual PlanetSideEmpire.TR
              basic.app.sex mustEqual CharacterGender.Male
              basic.app.head mustEqual 5
              basic.app.voice mustEqual CharacterVoice.Voice5
              basic.voice2 mustEqual 3
              basic.black_ops mustEqual false
              basic.jammered mustEqual false
              basic.exosuit mustEqual ExoSuitType.Reinforced
              basic.outfit_name mustEqual "Black Beret Armoured Corps"
              basic.outfit_logo mustEqual 23
              basic.facingPitch mustEqual 340.3125f
              basic.facingYawUpper mustEqual 0
              basic.lfs mustEqual false
              basic.grenade_state mustEqual GrenadeState.None
              basic.is_cloaking mustEqual false
              basic.charging_pose mustEqual false
              basic.on_zipline mustEqual false
              basic.ribbons.upper mustEqual MeritCommendation.MarkovVeteran
              basic.ribbons.middle mustEqual MeritCommendation.HeavyInfantry4
              basic.ribbons.lower mustEqual MeritCommendation.TankBuster7
              basic.ribbons.tos mustEqual MeritCommendation.SixYearTR
              //etc..
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
            case Some(PlayerData(Some(pos), basic, char, None, hand)) =>
              pos.coord mustEqual Vector3(4629.8906f, 6316.4453f, 54.734375f)
              pos.orient mustEqual Vector3(0, 0, 126.5625f)
              pos.vel.isDefined mustEqual false

              basic.app.name mustEqual "Angello"
              basic.app.faction mustEqual PlanetSideEmpire.VS
              basic.app.sex mustEqual CharacterGender.Male
              basic.app.head mustEqual 10
              basic.app.voice mustEqual CharacterVoice.Voice2
              basic.voice2 mustEqual 0
              basic.black_ops mustEqual false
              basic.jammered mustEqual false
              basic.exosuit mustEqual ExoSuitType.MAX
              basic.outfit_name mustEqual "Original District"
              basic.outfit_logo mustEqual 23
              basic.facingPitch mustEqual 0
              basic.facingYawUpper mustEqual 180.0f
              basic.lfs mustEqual false
              basic.grenade_state mustEqual GrenadeState.None
              basic.is_cloaking mustEqual false
              basic.charging_pose mustEqual false
              basic.on_zipline mustEqual false
              basic.ribbons.upper mustEqual MeritCommendation.Jacking2
              basic.ribbons.middle mustEqual MeritCommendation.ScavengerVS1
              basic.ribbons.lower mustEqual MeritCommendation.AMSSupport4
              basic.ribbons.tos mustEqual MeritCommendation.SixYearVS

              char.health mustEqual 0
              char.armor mustEqual 0
              char.uniform_upgrade mustEqual UniformStyle.ThirdUpgrade
              char.command_rank mustEqual 2
              char.implant_effects.isDefined mustEqual false
              char.cosmetics.isDefined mustEqual true
              char.cosmetics.get.no_helmet mustEqual true
              char.cosmetics.get.beret mustEqual true
              char.cosmetics.get.sunglasses mustEqual true
              char.cosmetics.get.earpiece mustEqual true
              char.cosmetics.get.brimmed_cap mustEqual false

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
      val app : (Int)=>CharacterAppearanceData = CharacterAppearanceData(
        BasicCharacterData(
          "ScrawnyRonnie",
          PlanetSideEmpire.TR,
          CharacterGender.Male,
          5,
          CharacterVoice.Voice5
        ),
        3,
        false,
        false,
        ExoSuitType.Reinforced,
        "Black Beret Armoured Corps",
        23,
        false,
        340.3125f, 0f,
        false,
        GrenadeState.None,
        false, false, false,
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
        Some(ImplantEffects.NoEffects),
        Some(Cosmetics(true, true, true, true, false))
      )
      val inv = InventoryData(
        InventoryItemData(ObjectClass.plasma_grenade, PlanetSideGUID(3662), 0, WeaponData(0, 0, ObjectClass.plasma_grenade_ammo, PlanetSideGUID(3751), 0, AmmoBoxData())) ::
          InventoryItemData(ObjectClass.bank, PlanetSideGUID(3908), 1, WeaponData(0, 0, 1, ObjectClass.armor_canister, PlanetSideGUID(4143), 0, AmmoBoxData())) ::
          InventoryItemData(ObjectClass.mini_chaingun, PlanetSideGUID(4164), 2, WeaponData(0, 0, ObjectClass.bullet_9mm, PlanetSideGUID(3728), 0, AmmoBoxData())) ::
          InventoryItemData(ObjectClass.phoenix, PlanetSideGUID(3603), 3, WeaponData(0, 0, ObjectClass.phoenix_missile, PlanetSideGUID(3056), 0, AmmoBoxData())) ::
          InventoryItemData(ObjectClass.chainblade, PlanetSideGUID(4088), 4, WeaponData(0, 0, 1, ObjectClass.melee_ammo, PlanetSideGUID(3279), 0, AmmoBoxData())) ::
          Nil
      )
      val obj = PlayerData(pos, app, char, inv, DrawnSlot.Rifle1)

      val msg = ObjectCreateMessage(ObjectClass.avatar, PlanetSideGUID(3902), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      val pkt_bitv = pkt.toBitVector
      val ori_bitv = string.toBitVector
      pkt_bitv.take(452) mustEqual ori_bitv.take(452) //skip 126
      pkt_bitv.drop(578).take(438) mustEqual ori_bitv.drop(578).take(438) //skip 2
      pkt_bitv.drop(1018).take(17) mustEqual ori_bitv.drop(1018).take(17) //skip 11
      pkt_bitv.drop(1046).take(147) mustEqual ori_bitv.drop(1046).take(147) //skip 3
      pkt_bitv.drop(1196) mustEqual ori_bitv.drop(1196)
      //TODO work on CharacterData to make this pass as a single stream
    }

    "encode (seated)" in {
      val app : (Int)=>CharacterAppearanceData = CharacterAppearanceData(
        BasicCharacterData(
          "ScrawnyRonnie",
          PlanetSideEmpire.TR,
          CharacterGender.Male,
          5,
          CharacterVoice.Voice5
        ),
        3,
        false,
        false,
        ExoSuitType.Reinforced,
        "Black Beret Armoured Corps",
        23,
        false,
        340.3125f, 0f,
        false,
        GrenadeState.None,
        false, false, false,
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
        Some(ImplantEffects.NoEffects),
        Some(Cosmetics(true, true, true, true, false))
      )
      val inv = InventoryData(
        InventoryItemData(ObjectClass.plasma_grenade, PlanetSideGUID(3662), 0, WeaponData(0, 0, ObjectClass.plasma_grenade_ammo, PlanetSideGUID(3751), 0, AmmoBoxData())) ::
          InventoryItemData(ObjectClass.bank, PlanetSideGUID(3908), 1, WeaponData(0, 0, 1, ObjectClass.armor_canister, PlanetSideGUID(4143), 0, AmmoBoxData())) ::
          InventoryItemData(ObjectClass.mini_chaingun, PlanetSideGUID(4164), 2, WeaponData(0, 0, ObjectClass.bullet_9mm, PlanetSideGUID(3728), 0, AmmoBoxData())) ::
          InventoryItemData(ObjectClass.phoenix, PlanetSideGUID(3603), 3, WeaponData(0, 0, ObjectClass.phoenix_missile, PlanetSideGUID(3056), 0, AmmoBoxData())) ::
          InventoryItemData(ObjectClass.chainblade, PlanetSideGUID(4088), 4, WeaponData(0, 0, 1, ObjectClass.melee_ammo, PlanetSideGUID(3279), 0, AmmoBoxData())) ::
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
      val app : (Int)=>CharacterAppearanceData = CharacterAppearanceData(
        BasicCharacterData(
          "Angello",
          PlanetSideEmpire.VS,
          CharacterGender.Male,
          10,
          CharacterVoice.Voice2
        ),
        0,
        false,
        false,
        ExoSuitType.MAX,
        "Original District",
        23,
        true, //backpack
        0f, 180.0f,
        false,
        GrenadeState.None,
        false, false, false,
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
        2,
        None,
        Some(Cosmetics(true, true, true, true, false))
      )
      val obj = PlayerData(pos, app, char, DrawnSlot.Pistol1)

      val msg = ObjectCreateMessage(ObjectClass.avatar, PlanetSideGUID(3380), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      val pkt_bitv = pkt.toBitVector
      val ori_bitv = string_backpack.toBitVector
      pkt_bitv.take(300) mustEqual ori_bitv.take(300) //skip 2
      pkt_bitv.drop(302).take(14) mustEqual ori_bitv.drop(302).take(14) //skip 126
      pkt_bitv.drop(442).take(305) mustEqual ori_bitv.drop(442).take(305) //skip 1
      pkt_bitv.drop(748).take(9) mustEqual ori_bitv.drop(748).take(9) // skip 2
      pkt_bitv.drop(759).take(157) mustEqual ori_bitv.drop(759).take(157) //skip 1
      pkt_bitv.drop(917) mustEqual ori_bitv.drop(917)
      //TODO work on CharacterData to make this pass as a single stream
    }
  }
}
