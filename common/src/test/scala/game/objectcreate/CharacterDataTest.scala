// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types._
import org.specs2.mutable._
import scodec.bits._

class CharacterDataTest extends Specification {
  val string_character = hex"17 73070000 BC8 3E0F 6C2D7 65535 CA16 00 00 09 9741E4F804000000 234530063007200610077006E00790052006F006E006E0069006500 220B7 E67B540404001000000000022B50100 268042006C00610063006B002000420065007200650074002000410072006D006F007500720065006400200043006F00720070007300 1700E0030050040003BC00000234040001A004000 3FFF67A8F A0A5424E0E800000000080952A9C3A03000001081103E040000000A023782F1080C0000016244108200000000808382403A030000014284C3A0C0000000202512F00B80C00000578F80F840000000280838B3C320300000080"
  val string_character_backpack = hex"17 9C030000 BC8 340D F20A9 3956C AF0D 00 00 73 480000 87041006E00670065006C006C006F00 4A148 0000000000000000000000005C54200 24404F0072006900670069006E0061006C00200044006900730074007200690063007400 1740180181E8000000C202000042000000D202000000010A3C00"

  "CharacterData" should {
    "decode" in {
      PacketCoding.DecodePacket(string_character).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 1907
          cls mustEqual ObjectClass.avatar
          guid mustEqual PlanetSideGUID(3902)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[CharacterData] mustEqual true
          val pc = data.get.asInstanceOf[CharacterData]
          pc.appearance.pos.coord.x mustEqual 3674.8438f
          pc.appearance.pos.coord.y mustEqual 2726.789f
          pc.appearance.pos.coord.z mustEqual 91.15625f
          pc.appearance.pos.orient.x mustEqual 0f
          pc.appearance.pos.orient.y mustEqual 0f
          pc.appearance.pos.orient.z mustEqual 64.6875f
          pc.appearance.pos.vel.isDefined mustEqual true
          pc.appearance.pos.vel.get.x mustEqual 1.4375f
          pc.appearance.pos.vel.get.y mustEqual -0.4375f
          pc.appearance.pos.vel.get.z mustEqual 0f
          pc.appearance.basic_appearance.name mustEqual "ScrawnyRonnie"
          pc.appearance.basic_appearance.faction mustEqual PlanetSideEmpire.TR
          pc.appearance.basic_appearance.sex mustEqual CharacterGender.Male
          pc.appearance.basic_appearance.head mustEqual 5
          pc.appearance.basic_appearance.voice mustEqual 5
          pc.appearance.voice2 mustEqual 3
          pc.appearance.black_ops mustEqual false
          pc.appearance.jammered mustEqual false
          pc.appearance.exosuit mustEqual ExoSuitType.Reinforced
          pc.appearance.outfit_name mustEqual "Black Beret Armoured Corps"
          pc.appearance.outfit_logo mustEqual 23
          pc.appearance.facingPitch mustEqual 340.3125f
          pc.appearance.facingYawUpper mustEqual 0
          pc.appearance.lfs mustEqual false
          pc.appearance.grenade_state mustEqual GrenadeState.None
          pc.appearance.is_cloaking mustEqual false
          pc.appearance.charging_pose mustEqual false
          pc.appearance.on_zipline mustEqual false
          pc.appearance.ribbons.upper mustEqual MeritCommendation.MarkovVeteran
          pc.appearance.ribbons.middle mustEqual MeritCommendation.HeavyInfantry4
          pc.appearance.ribbons.lower mustEqual MeritCommendation.TankBuster7
          pc.appearance.ribbons.tos mustEqual MeritCommendation.SixYearTR
          pc.health mustEqual 255
          pc.armor mustEqual 253
          pc.uniform_upgrade mustEqual UniformStyle.ThirdUpgrade
          pc.command_rank mustEqual 5
          pc.implant_effects.isDefined mustEqual true
          pc.implant_effects.get mustEqual ImplantEffects.NoEffects
          pc.cosmetics.isDefined mustEqual true
          pc.cosmetics.get.no_helmet mustEqual true
          pc.cosmetics.get.beret mustEqual true
          pc.cosmetics.get.sunglasses mustEqual true
          pc.cosmetics.get.earpiece mustEqual true
          pc.cosmetics.get.brimmed_cap mustEqual false
          //short test of inventory items
          pc.inventory.isDefined mustEqual true
          val contents = pc.inventory.get.contents
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
          pc.drawn_slot mustEqual DrawnSlot.Rifle1
        case _ =>
          ko
      }
    }

    "decode (backpack)" in {
      PacketCoding.DecodePacket(string_character_backpack).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 924L
          cls mustEqual ObjectClass.avatar
          guid mustEqual PlanetSideGUID(3380)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[CharacterData] mustEqual true
          val pc = data.get.asInstanceOf[CharacterData]
          pc.appearance.pos.coord.x mustEqual 4629.8906f
          pc.appearance.pos.coord.y mustEqual 6316.4453f
          pc.appearance.pos.coord.z mustEqual 54.734375f
          pc.appearance.pos.orient.x mustEqual 0f
          pc.appearance.pos.orient.y mustEqual 0f
          pc.appearance.pos.orient.z mustEqual 126.5625f
          pc.appearance.pos.vel.isDefined mustEqual false
          pc.appearance.basic_appearance.name mustEqual "Angello"
          pc.appearance.basic_appearance.faction mustEqual PlanetSideEmpire.VS
          pc.appearance.basic_appearance.sex mustEqual CharacterGender.Male
          pc.appearance.basic_appearance.head mustEqual 10
          pc.appearance.basic_appearance.voice mustEqual 2
          pc.appearance.voice2 mustEqual 0
          pc.appearance.black_ops mustEqual false
          pc.appearance.jammered mustEqual false
          pc.appearance.exosuit mustEqual ExoSuitType.MAX
          pc.appearance.outfit_name mustEqual "Original District"
          pc.appearance.outfit_logo mustEqual 23
          pc.appearance.facingPitch mustEqual 0
          pc.appearance.facingYawUpper mustEqual 180.0f
          pc.appearance.lfs mustEqual false
          pc.appearance.grenade_state mustEqual GrenadeState.None
          pc.appearance.is_cloaking mustEqual false
          pc.appearance.charging_pose mustEqual false
          pc.appearance.on_zipline mustEqual false
          pc.appearance.ribbons.upper mustEqual MeritCommendation.Jacking2
          pc.appearance.ribbons.middle mustEqual MeritCommendation.ScavengerVS1
          pc.appearance.ribbons.lower mustEqual MeritCommendation.AMSSupport4
          pc.appearance.ribbons.tos mustEqual MeritCommendation.SixYearVS
          pc.health mustEqual 0
          pc.armor mustEqual 0
          pc.uniform_upgrade mustEqual UniformStyle.ThirdUpgrade
          pc.command_rank mustEqual 2
          pc.implant_effects.isDefined mustEqual false
          pc.cosmetics.isDefined mustEqual true
          pc.cosmetics.get.no_helmet mustEqual true
          pc.cosmetics.get.beret mustEqual true
          pc.cosmetics.get.sunglasses mustEqual true
          pc.cosmetics.get.earpiece mustEqual true
          pc.cosmetics.get.brimmed_cap mustEqual false
          pc.inventory.isDefined mustEqual false
          pc.drawn_slot mustEqual DrawnSlot.Pistol1
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = CharacterData(
        CharacterAppearanceData(
          PlacementData(
            Vector3(3674.8438f, 2726.789f, 91.15625f),
            Vector3(0f, 0f, 64.6875f),
            Some(Vector3(1.4375f, -0.4375f, 0f))
          ),
          BasicCharacterData(
            "ScrawnyRonnie",
            PlanetSideEmpire.TR,
            CharacterGender.Male,
            5,
            5
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
        ),
        255, 253,
        UniformStyle.ThirdUpgrade,
        5,
        Some(ImplantEffects.NoEffects),
        Some(Cosmetics(true, true, true, true, false)),
        InventoryData(
          InventoryItemData(ObjectClass.plasma_grenade, PlanetSideGUID(3662), 0, WeaponData(0, 0, ObjectClass.plasma_grenade_ammo, PlanetSideGUID(3751), 0, AmmoBoxData())) ::
            InventoryItemData(ObjectClass.bank, PlanetSideGUID(3908), 1, WeaponData(0, 0, 1, ObjectClass.armor_canister, PlanetSideGUID(4143), 0, AmmoBoxData())) ::
            InventoryItemData(ObjectClass.mini_chaingun, PlanetSideGUID(4164), 2, WeaponData(0, 0, ObjectClass.bullet_9mm, PlanetSideGUID(3728), 0, AmmoBoxData())) ::
            InventoryItemData(ObjectClass.phoenix, PlanetSideGUID(3603), 3, WeaponData(0, 0, ObjectClass.phoenix_missile, PlanetSideGUID(3056), 0, AmmoBoxData())) ::
            InventoryItemData(ObjectClass.chainblade, PlanetSideGUID(4088), 4, WeaponData(0, 0, 1, ObjectClass.melee_ammo, PlanetSideGUID(3279), 0, AmmoBoxData())) ::
            Nil
        ),
        DrawnSlot.Rifle1
      )
      val msg = ObjectCreateMessage(ObjectClass.avatar, PlanetSideGUID(3902), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      val pkt_bitv = pkt.toBitVector
      val ori_bitv = string_character.toBitVector
      pkt_bitv.take(452) mustEqual ori_bitv.take(452) //skip 126
      pkt_bitv.drop(578).take(438) mustEqual ori_bitv.drop(578).take(438) //skip 2
      pkt_bitv.drop(1018).take(17) mustEqual ori_bitv.drop(1018).take(17) //skip 11
      pkt_bitv.drop(1046).take(147) mustEqual ori_bitv.drop(1046).take(147) //skip 3
      pkt_bitv.drop(1196) mustEqual ori_bitv.drop(1196)
      //TODO work on CharacterData to make this pass as a single stream
    }

    "encode (backpack)" in {
      val obj = CharacterData(
        CharacterAppearanceData(
          PlacementData(4629.8906f, 6316.4453f, 54.734375f, 0f, 0f, 126.5625f),
          BasicCharacterData(
            "Angello",
            PlanetSideEmpire.VS,
            CharacterGender.Male,
            10,
            2
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
        ),
        0, 0,
        UniformStyle.ThirdUpgrade,
        2,
        None,
        Some(Cosmetics(true, true, true, true, false)),
        None,
        DrawnSlot.Pistol1
      )
      val msg = ObjectCreateMessage(ObjectClass.avatar, PlanetSideGUID(3380), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      val pkt_bitv = pkt.toBitVector
      val ori_bitv = string_character_backpack.toBitVector
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
