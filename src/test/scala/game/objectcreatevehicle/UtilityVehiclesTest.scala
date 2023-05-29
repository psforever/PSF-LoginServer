// Copyright (c) 2017 PSForever
package game.objectcreatevehicle

import net.psforever.packet._
import net.psforever.packet.game.objectcreate.{CharacterAppearanceA, _}
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.types._
import org.specs2.mutable._
import scodec.bits._

class UtilityVehiclesTest extends Specification {
  val string_ant = hex"17 C2000000 9E0 7C01 6C2D7 65535 CA16 00 00 00 4400003FC000000"
  val string_ams =
    hex"17 B8010000 970 3D10 002D765535CA16000000 402285BB0037E4100749E1D03000000620D83A0A00000195798741C00000332E40D84800000"
  val string_ams_seated =
    hex"17 ec060000 970 fe0f 6C2D765535CA16000013 f9c1f2f80c000 1e18ff0000 105 1e4078640000000 8c50004c0041006d0069006e00670079007500650054005200 04217c859e808000000000000000250342002 2c02a002a002a002a0050004c0041002a002a002a002a00 010027e3007c000003940000016c0400023c040002285a086c2f00c80000000000300210288740800000004046f17423018000002c4d6190400000001010704a86406000002bc770842000000004041c5f21d01800000e0 75821902000000 623e8420800000 1950588c1800000 332ea0f840000000"

  "Utility vehicles" should {
    "decode (ant)" in {
      PacketCoding.decodePacket(string_ant).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 194L
          cls mustEqual ObjectClass.ant
          guid mustEqual PlanetSideGUID(380)
          parent.isDefined mustEqual false
          data.isInstanceOf[VehicleData] mustEqual true
          val ant = data.asInstanceOf[VehicleData]
          ant.pos.coord mustEqual Vector3(3674.8438f, 2726.789f, 91.15625f)
          ant.pos.orient mustEqual Vector3(0, 0, 90)
          ant.data.faction mustEqual PlanetSideEmpire.VS
          ant.data.alternate mustEqual false
          ant.data.v1 mustEqual true
          ant.data.jammered mustEqual false
          ant.data.v5.isEmpty mustEqual true
          ant.data.guid mustEqual PlanetSideGUID(0)
          ant.driveState mustEqual DriveState.Mobile
          ant.health mustEqual 255
          ant.cloak mustEqual false
          ant.unk3 mustEqual false
          ant.unk4 mustEqual false
          ant.unk5 mustEqual false
          ant.unk6 mustEqual false
        case _ =>
          ko
      }
    }

    "decode (ams)" in {
      PacketCoding.decodePacket(string_ams).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 440L
          cls mustEqual ObjectClass.ams
          guid mustEqual PlanetSideGUID(4157)
          parent.isDefined mustEqual false
          data.isInstanceOf[VehicleData] mustEqual true
          val ams = data.asInstanceOf[VehicleData]
          ams.pos.coord mustEqual Vector3(3674, 2726.789f, 91.15625f)
          ams.pos.orient mustEqual Vector3(0, 0, 90)
          ams.pos.vel mustEqual None
          ams.data.faction mustEqual PlanetSideEmpire.VS
          ams.data.alternate mustEqual false
          ams.data.v1 mustEqual false
          ams.data.jammered mustEqual false
          ams.data.v5.isEmpty mustEqual true
          ams.data.guid mustEqual PlanetSideGUID(2885)
          ams.driveState mustEqual DriveState.Deployed
          ams.vehicle_format_data mustEqual Some(UtilityVehicleData(60))
          ams.health mustEqual 236
          ams.cloak mustEqual true
          ams.unk3 mustEqual false
          ams.unk4 mustEqual false
          ams.unk5 mustEqual false
          ams.unk6 mustEqual true

          ams.inventory.isDefined mustEqual true
          val inv = ams.inventory.get.contents
          inv.head.objectClass mustEqual ObjectClass.matrix_terminalc
          inv.head.guid mustEqual PlanetSideGUID(3663)
          inv.head.parentSlot mustEqual 1
          inv.head.obj.isInstanceOf[CommonFieldData] mustEqual true
          inv(1).objectClass mustEqual ObjectClass.ams_respawn_tube
          inv(1).guid mustEqual PlanetSideGUID(3638)
          inv(1).parentSlot mustEqual 2
          inv(1).obj.isInstanceOf[CommonFieldData] mustEqual true
          inv(2).objectClass mustEqual ObjectClass.order_terminala
          inv(2).guid mustEqual PlanetSideGUID(3827)
          inv(2).parentSlot mustEqual 3
          inv(2).obj.isInstanceOf[CommonFieldData] mustEqual true
          inv(3).objectClass mustEqual ObjectClass.order_terminalb
          inv(3).guid mustEqual PlanetSideGUID(3556)
          inv(3).parentSlot mustEqual 4
          inv(3).obj.isInstanceOf[CommonFieldData] mustEqual true
        case _ =>
          ko
      }
    }

    "decode (ams, seated)" in {
      PacketCoding.decodePacket(string_ams_seated).require match {
        case ObjectCreateMessage(len, cls, guid, None, data) =>
          len mustEqual 1772
          cls mustEqual ObjectClass.ams
          guid mustEqual PlanetSideGUID(4094)
          data match {
            case ams: VehicleData =>
              ams.pos.coord mustEqual Vector3(3674.8438f, 2726.789f, 91.15625f)
              ams.pos.orient mustEqual Vector3(0f, 0f, 36.5625f)
              ams.pos.vel.contains(Vector3(27.3375f, -0.78749996f, 0.1125f)) mustEqual true //contains does not work
              ams.data match {
                case data: CommonFieldData =>
                  data.faction mustEqual PlanetSideEmpire.TR
                  data.bops mustEqual false
                  data.alternate mustEqual false
                  data.v1 mustEqual false
                  data.v2.isEmpty mustEqual true
                  data.jammered mustEqual false
                  data.v4.contains(false) mustEqual true
                  data.v5.isEmpty mustEqual true
                  data.guid mustEqual ValidPlanetSideGUID(3087)
                case _ =>
                  ko
              }
              ams.unk3 mustEqual false
              ams.health mustEqual 255
              ams.unk4 mustEqual false
              ams.no_mount_points mustEqual false
              ams.driveState mustEqual DriveState.Mobile
              ams.unk5 mustEqual false
              ams.unk6 mustEqual false
              ams.cloak mustEqual false
              ams.vehicle_format_data.contains(UtilityVehicleData(0)) mustEqual true
              val inv = ams.inventory match {
                case Some(inv: InventoryData) => inv.contents
                case _ => Nil
              }
              //0
              val inv0 = inv.head
              inv0.objectClass mustEqual ObjectClass.avatar
              inv0.guid mustEqual PlanetSideGUID(3087)
              inv0.parentSlot mustEqual 0
              inv0.obj match {
                case PlayerData(None, CharacterAppearanceData(a, b, r), char, Some(InventoryData(pinv)), DrawnSlot.None) =>
                  a.app.name mustEqual "PLAmingyueTR"
                  a.app.faction mustEqual PlanetSideEmpire.TR
                  a.app.sex mustEqual CharacterSex.Female
                  a.app.voice mustEqual CharacterVoice.Voice5
                  a.app.head mustEqual 16
                  a.data match {
                    case data: CommonFieldData =>
                      data.faction mustEqual PlanetSideEmpire.TR
                      data.bops mustEqual false
                      data.alternate mustEqual false
                      data.v1 mustEqual false
                      data.v2.isEmpty mustEqual true
                      data.jammered mustEqual false
                      data.v4.isEmpty mustEqual true
                      data.v5.isEmpty mustEqual true
                      data.guid mustEqual ValidPlanetSideGUID(0)
                    case _ =>
                      ko
                  }
                  a.char_id mustEqual 41555698L
                  a.exosuit mustEqual ExoSuitType.Agile
                  a.unk5 mustEqual 0
                  a.unk7 mustEqual 0
                  a.unk8 mustEqual 0
                  a.unk9 mustEqual 0
                  a.unkA mustEqual 0
                  b.outfit_id mustEqual 527764
                  b.outfit_name mustEqual "****PLA****"
                  b.outfit_logo mustEqual 1
                  b.lfs mustEqual false
                  b.backpack mustEqual false
                  b.charging_pose mustEqual false
                  b.grenade_state mustEqual GrenadeState.None
                  b.on_zipline.isEmpty mustEqual true
                  b.facingPitch mustEqual -5.625f
                  b.facingYawUpper mustEqual 5.625f
                  b.unk1 mustEqual false
                  b.unk2 mustEqual false
                  b.unk3 mustEqual false
                  b.unk4 mustEqual false
                  b.unk5 mustEqual false
                  b.unk6 mustEqual false
                  b.unk7 mustEqual false
                  r mustEqual RibbonBars(
                    MeritCommendation.AMSSupport2,
                    MeritCommendation.HeavyAssault1,
                    MeritCommendation.ScavengerTR1,
                    MeritCommendation.ThreeYearTR
                  )
                  char.health mustEqual 100
                  char.armor mustEqual 0
                  char.uniform_upgrade mustEqual UniformStyle.ThirdUpgrade
                  char.command_rank mustEqual 4
                  char.implant_effects mustEqual Nil
                  char.unk mustEqual 2
                  char.cosmetics.contains(Set(Cosmetic.Earpiece, Cosmetic.Sunglasses, Cosmetic.NoHelmet)) mustEqual true
                  pinv mustEqual List(
                    InternalSlot(728, ValidPlanetSideGUID(3312), 0, REKData(CommonFieldData(PlanetSideEmpire.TR,false,false,false,None,false,Some(false),None,ValidPlanetSideGUID(0)),3,0)),
                    InternalSlot(132, ValidPlanetSideGUID(3665), 1, WeaponData(CommonFieldData(PlanetSideEmpire.TR,false,false,false,None,false,None,None,ValidPlanetSideGUID(0)),0,List(
                      InternalSlot(111,ValidPlanetSideGUID(4538),0,CommonFieldData(PlanetSideEmpire.NEUTRAL,false,false,false,None,false,Some(false),None,ValidPlanetSideGUID(0)))),false)
                    ),
                    InternalSlot(556, ValidPlanetSideGUID(3179), 2, WeaponData(CommonFieldData(PlanetSideEmpire.TR,false,false,false,None,false,None,None,ValidPlanetSideGUID(0)),0,List(
                      InternalSlot(28,ValidPlanetSideGUID(3221),0,CommonFieldData(PlanetSideEmpire.NEUTRAL,false,false,false,None,false,Some(false),None,ValidPlanetSideGUID(0)))),false)
                    ),
                    InternalSlot(175,ValidPlanetSideGUID(4334),4,WeaponData(CommonFieldData(PlanetSideEmpire.TR,false,false,false,None,false,None,None,ValidPlanetSideGUID(0)),0,List(InternalSlot(540,ValidPlanetSideGUID(3833),0,CommonFieldData(PlanetSideEmpire.NEUTRAL,false,false,false,None,false,Some(false),None,ValidPlanetSideGUID(0)))),false))
                  )
                case _ =>
                  ko
              }
              //1
              val inv1 = inv(1)
              inv1.objectClass mustEqual ObjectClass.matrix_terminalc
              inv1.guid mustEqual PlanetSideGUID(3265)
              inv1.parentSlot mustEqual 1
              inv1.obj.isInstanceOf[CommonFieldData] mustEqual true
              //2
              val inv2 = inv(2)
              inv2.objectClass mustEqual ObjectClass.ams_respawn_tube
              inv2.guid mustEqual PlanetSideGUID(4346)
              inv2.parentSlot mustEqual 2
              inv2.obj.isInstanceOf[CommonFieldData] mustEqual true
              //3
              val inv3 = inv(3)
              inv3.objectClass mustEqual ObjectClass.order_terminala
              inv3.guid mustEqual PlanetSideGUID(4363)
              inv3.parentSlot mustEqual 3
              inv3.obj.isInstanceOf[CommonFieldData] mustEqual true
              //4
              val inv4 = inv(4)
              inv4.objectClass mustEqual ObjectClass.order_terminalb
              inv4.guid mustEqual PlanetSideGUID(4074)
              inv4.parentSlot mustEqual 4
              inv4.obj.isInstanceOf[CommonFieldData] mustEqual true
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode (ant)" in {
      val obj = VehicleData(
        PlacementData(3674.8438f, 2726.789f, 91.15625f, 0f, 0f, 90.0f),
        CommonFieldData(PlanetSideEmpire.VS, false, false, true, None, false, Some(false), None, PlanetSideGUID(0)),
        false,
        255,
        false,
        false,
        DriveState.Mobile,
        false,
        false,
        false,
        Some(UtilityVehicleData(0)),
        None
      )(VehicleFormat.Utility)
      val msg = ObjectCreateMessage(ObjectClass.ant, PlanetSideGUID(380), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_ant
    }

    "encode (ams)" in {
      val obj = VehicleData(
        PlacementData(3674.0f, 2726.789f, 91.15625f, 0f, 0f, 90.0f),
        CommonFieldData(PlanetSideEmpire.VS, false, false, false, None, false, Some(false), None, PlanetSideGUID(2885)),
        false,
        236,
        false,
        false,
        DriveState.Deployed,
        false,
        true,
        true,
        Some(UtilityVehicleData(60)), //what does this mean?
        Some(
          InventoryData(
            List(
              InternalSlot(
                ObjectClass.matrix_terminalc,
                PlanetSideGUID(3663),
                1,
                CommonFieldData(PlanetSideEmpire.VS)(false)
              ),
              InternalSlot(
                ObjectClass.ams_respawn_tube,
                PlanetSideGUID(3638),
                2,
                CommonFieldData(PlanetSideEmpire.VS)(false)
              ),
              InternalSlot(
                ObjectClass.order_terminala,
                PlanetSideGUID(3827),
                3,
                CommonFieldData(PlanetSideEmpire.VS)(false)
              ),
              InternalSlot(
                ObjectClass.order_terminalb,
                PlanetSideGUID(3556),
                4,
                CommonFieldData(PlanetSideEmpire.VS)(false)
              )
            )
          )
        )
      )(VehicleFormat.Utility)
      val msg = ObjectCreateMessage(ObjectClass.ams, PlanetSideGUID(4157), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_ams
    }

    "encode (ams, seated)" in {
      val obj = VehicleData(
        PlacementData(
          Vector3(3674.8438f, 2726.789f, 91.15625f),
          Vector3(0f, 0f, 36.5625f),
          Some(Vector3(27.3375f, -0.78749996f, 0.1125f))
        ),
        CommonFieldData(PlanetSideEmpire.TR, false, false, false, None, false, Some(false), None, PlanetSideGUID(3087)),
        unk3 = false,
        health = 255,
        unk4 = false,
        no_mount_points = false,
        DriveState.Mobile,
        unk5 = false,
        unk6 = false,
        cloak = false,
        Some(UtilityVehicleData(0)),
        Some(InventoryData(
          List(
            InternalSlot(
              ObjectClass.avatar, PlanetSideGUID(3087), 0, {
                val a: Int => CharacterAppearanceA = CharacterAppearanceA(
                  BasicCharacterData(
                    "PLAmingyueTR",
                    PlanetSideEmpire.TR,
                    CharacterSex.Female,
                    head = 16,
                    CharacterVoice.Voice5
                  ),
                  CommonFieldData(PlanetSideEmpire.TR, false, false, false, None, false, None, None, PlanetSideGUID(0)),
                  ExoSuitType.Agile,
                  unk5 = 0,
                  char_id = 41555698L,
                  unk7 = 0,
                  unk8 = 0,
                  unk9 = 0,
                  unkA = 0
                )
                val b: (Boolean, Int) => CharacterAppearanceB = CharacterAppearanceB(
                  outfit_id = 527764L,
                  outfit_name = "****PLA****",
                  outfit_logo = 1,
                  unk1 = false,
                  backpack = false,
                  unk2 = false,
                  unk3 = false,
                  unk4 = false,
                  facingPitch = -5.625f,
                  facingYawUpper = 5.625f,
                  lfs = false,
                  GrenadeState.None,
                  is_cloaking = false,
                  unk5 = false,
                  unk6 = false,
                  charging_pose = false,
                  unk7 = false,
                  on_zipline = None
                )
                val app: Int => CharacterAppearanceData = CharacterAppearanceData(
                  a,
                  b,
                  RibbonBars(
                    MeritCommendation.AMSSupport2,
                    MeritCommendation.HeavyAssault1,
                    MeritCommendation.ScavengerTR1,
                    MeritCommendation.ThreeYearTR
                  )
                )
                val char: (Boolean, Boolean) => CharacterData = CharacterData(
                  health = 100,
                  armor = 0,
                  UniformStyle.ThirdUpgrade,
                  unk = 2,
                  command_rank = 4,
                  implant_effects = List(),
                  Some(Set(Cosmetic.Earpiece, Cosmetic.Sunglasses, Cosmetic.NoHelmet))
                )
                val inv = InventoryData(
                  List(
                    InternalSlot(728, ValidPlanetSideGUID(3312), 0, REKData(CommonFieldData(PlanetSideEmpire.TR,false,false,false,None,false,Some(false),None,ValidPlanetSideGUID(0)),3,0)),
                    InternalSlot(132, ValidPlanetSideGUID(3665), 1, WeaponData(CommonFieldData(PlanetSideEmpire.TR,false,false,false,None,false,None,None,ValidPlanetSideGUID(0)),0,List(
                      InternalSlot(111, ValidPlanetSideGUID(4538), 0, CommonFieldData(PlanetSideEmpire.NEUTRAL,false,false,false,None,false,Some(false),None,ValidPlanetSideGUID(0)))),false)
                    ),
                    InternalSlot(556, ValidPlanetSideGUID(3179), 2, WeaponData(CommonFieldData(PlanetSideEmpire.TR,false,false,false,None,false,None,None,ValidPlanetSideGUID(0)),0,List(
                      InternalSlot(28, ValidPlanetSideGUID(3221), 0, CommonFieldData(PlanetSideEmpire.NEUTRAL,false,false,false,None,false,Some(false),None,ValidPlanetSideGUID(0)))),false)
                    ),
                    InternalSlot(175, ValidPlanetSideGUID(4334), 4, WeaponData(CommonFieldData(PlanetSideEmpire.TR,false,false,false,None,false,None,None,ValidPlanetSideGUID(0)),0,List(InternalSlot(540,ValidPlanetSideGUID(3833),0,CommonFieldData(PlanetSideEmpire.NEUTRAL,false,false,false,None,false,Some(false),None,ValidPlanetSideGUID(0)))),false))
                  )
                )
                MountableInventory.PlayerData(
                  app,
                  char,
                  inv,
                  DrawnSlot.None,
                  MountableInventory.InitialStreamLengthToSeatEntries(hasVelocity=true, VehicleFormat.Utility)
                )
              }
            ),
            InternalSlot(
              ObjectClass.matrix_terminalc, PlanetSideGUID(3265), 1, CommonFieldData(PlanetSideEmpire.TR)(flag = false)
            ),
            InternalSlot(
              ObjectClass.ams_respawn_tube, PlanetSideGUID(4346), 2, CommonFieldData(PlanetSideEmpire.TR)(flag = false)
            ),
            InternalSlot(
              ObjectClass.order_terminala, PlanetSideGUID(4363), 3, CommonFieldData(PlanetSideEmpire.TR)(flag = false)
            ),
            InternalSlot(
              ObjectClass.order_terminalb, PlanetSideGUID(4074), 4, CommonFieldData(PlanetSideEmpire.TR)(flag = false)
            )
          )
        ))
      )(VehicleFormat.Utility)
      val msg = ObjectCreateMessage(ObjectClass.ams, PlanetSideGUID(4094), obj)
      val pkt = PacketCoding.encodePacket(msg).require.toByteVector

      pkt mustEqual string_ams_seated
    }
  }
}
