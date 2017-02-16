// Copyright (c) 2016 PSForever.net to present
import java.net.{InetAddress, InetSocketAddress}

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.packet.game.objectcreate.{InventoryItem, _}
import net.psforever.types._
import scodec.{Attempt, Err}
import scodec.Attempt.Successful
import scodec.bits._

class GamePacketTest extends Specification {

  "PlanetSide game packet" in {

    "ConnectToWorldRequestMessage" should {
      val string = hex"03 8667656D696E69 0000000000000000 00000000 00000000 00000000 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  80 00 00 "

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ConnectToWorldRequestMessage(serverName, token, majorVersion, minorVersion, revision, buildDate, unk) =>
            serverName mustEqual "gemini"
            token mustEqual ""
            majorVersion mustEqual 0
            minorVersion mustEqual 0
            revision mustEqual 0
            buildDate mustEqual ""
            unk mustEqual 0
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ConnectToWorldRequestMessage("gemini", "", 0, 0, 0, "", 0)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ConnectToWorldMessage" should {
      val string = hex"04 8667656D696E69  8C36342E33372E3135382E36393C75"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ConnectToWorldMessage(serverName, serverIp, serverPort) =>
            serverName mustEqual "gemini"
            serverIp mustEqual "64.37.158.69"
            serverPort mustEqual 30012
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ConnectToWorldMessage("gemini", "64.37.158.69", 30012)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "VNLWorldStatusMessage" should {
      // NOTE: the ServerType is encoded as 0x03 here, but the real planetside server will encode it as 0x04
      val string = hex"0597570065006c0063006f006d006500200074006f00200050006c0061006e00650074005300690064006500210020000186" ++
              hex"67656d696e69" ++ hex"0100 03 00 01459e2540 3775" ++ bin"01".toByteVector

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case VNLWorldStatusMessage(message, worlds) =>
            worlds.length mustEqual 1
            message mustEqual "Welcome to PlanetSide! "
            val world = worlds{0}

            world.name mustEqual "gemini"
            world.empireNeed mustEqual PlanetSideEmpire.NC
            world.status mustEqual WorldStatus.Up
            world.serverType mustEqual ServerType.Released

            world.connections.length mustEqual 1
            world.connections{0}.address.getPort mustEqual 30007
            world.connections{0}.address.getAddress.toString mustEqual "/64.37.158.69"
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
          Vector(
            WorldInformation("gemini", WorldStatus.Up, ServerType.Released,
              Vector(
                WorldConnectionInfo(new InetSocketAddress(InetAddress.getByName("64.37.158.69"), 30007))
              ), PlanetSideEmpire.NC
            )
          )
        )
        //0100 04 00 01459e2540377540

        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }

      "encode and decode multiple worlds" in {
        val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
          Vector(
            WorldInformation("PSForever1", WorldStatus.Up, ServerType.Released, Vector(), PlanetSideEmpire.NC),
            WorldInformation("PSForever2", WorldStatus.Down, ServerType.Beta, Vector(), PlanetSideEmpire.TR)
          ))

        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        //println(pkt)

        // TODO: actually test something
        ok
      }
    }

    "PlayerStateMessage" should {
      val string_short = hex"08 A006 DFD17 B5AEB 380B 0F80002990"
      val string_mod = hex"08 A006 DFD17 B5AEB 380B 0F80002985" //slightly modified from above to demonstrate active booleans
      val string_vel = hex"08 A006 4DD47 CDB1B 0C0B A8C1A5000403008014A4"

      "decode (short)" in {
        PacketCoding.DecodePacket(string_short).require match {
          case PlayerStateMessage(guid, pos, vel, facingYaw, facingPitch, facingUpper, unk1, crouching, jumping, unk2, unk3) =>
            guid mustEqual PlanetSideGUID(1696)
            pos.x mustEqual 4003.7422f
            pos.y mustEqual 5981.414f
            pos.z mustEqual 44.875f
            vel.isDefined mustEqual false
            facingYaw mustEqual 31
            facingPitch mustEqual 0
            facingUpper mustEqual 0
            unk1 mustEqual 83
            crouching mustEqual false
            jumping mustEqual false
            unk2 mustEqual false
            unk3 mustEqual false
          case default =>
            ko
        }
      }

      "decode (mod)" in {
        PacketCoding.DecodePacket(string_mod).require match {
          case PlayerStateMessage(guid, pos, vel, facingYaw, facingPitch, facingUpper, unk1, crouching, jumping, unk2, unk3) =>
            guid mustEqual PlanetSideGUID(1696)
            pos.x mustEqual 4003.7422f
            pos.y mustEqual 5981.414f
            pos.z mustEqual 44.875f
            vel.isDefined mustEqual false
            facingYaw mustEqual 31
            facingPitch mustEqual 0
            facingUpper mustEqual 0
            unk1 mustEqual 83
            crouching mustEqual false
            jumping mustEqual true
            unk2 mustEqual false
            unk3 mustEqual true
          case default =>
            ko
        }
      }

      "decode (vel)" in {
        PacketCoding.DecodePacket(string_vel).require match {
          case PlayerStateMessage(guid, pos, vel, facingYaw, facingPitch, facingUpper, unk1, crouching, jumping, unk2, unk3) =>
            guid mustEqual PlanetSideGUID(1696)
            pos.x mustEqual 4008.6016f
            pos.y mustEqual 5987.6016f
            pos.z mustEqual 44.1875f
            vel.isDefined mustEqual true
            vel.get.x mustEqual 2.53125f
            vel.get.y mustEqual 6.5625f
            vel.get.z mustEqual 0.0f
            facingYaw mustEqual 24
            facingPitch mustEqual 4
            facingUpper mustEqual 0
            unk1 mustEqual 165
            crouching mustEqual false
            jumping mustEqual false
            unk2 mustEqual false
            unk3 mustEqual false
          case default =>
            ko
        }
      }

      "encode (short)" in {
        val msg = PlayerStateMessage(
          PlanetSideGUID(1696),
          Vector3(4003.7422f, 5981.414f, 44.875f),
          None,
          31, 0, 0, 83,
          false, false, false, false)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_short
      }

      "encode (mod)" in {
        val msg = PlayerStateMessage(
          PlanetSideGUID(1696),
          Vector3(4003.7422f, 5981.414f, 44.875f),
          None,
          31, 0, 0, 83,
          false, true, false, true)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_mod
      }

      "encode (vel)" in {
        val msg = PlayerStateMessage(
          PlanetSideGUID(1696),
          Vector3(4008.6016f, 5987.6016f, 44.1875f),
          Some(Vector3(2.53125f, 6.5625f, 0f)),
          24, 4, 0, 165,
          false, false, false, false)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_vel
      }
    }

    "ActionResultMessage" should {
      "decode" in {
        PacketCoding.DecodePacket(hex"1f 80").require match {
          case ActionResultMessage(okay, code) =>
            okay === true
            code === None
          case default =>
            ko
        }

        PacketCoding.DecodePacket((hex"1f".bits ++ bin"0" ++ hex"01000000".bits).toByteVector).require match {
          case ActionResultMessage(okay, code) =>
            okay === false
            code === Some(1)
          case default =>
            ko
        }
      }

      "encode" in {
        PacketCoding.EncodePacket(ActionResultMessage(true, None)).require.toByteVector === hex"1f 80"
        PacketCoding.EncodePacket(ActionResultMessage(false, Some(1))).require.toByteVector ===
          (hex"1f".bits ++ bin"0" ++ hex"01000000".bits).toByteVector
      }
    }

    "ObjectCreateMessage" should {
      val packet = hex"18 CF 13 00 00 BC 87 00  0A F0 16 C3 43 A1 30 90 00 02 C0 40 00 08 70 43  00 68 00 6F 00 72 00 64 00 54 00 52 00 82 65 1F  F5 9E 80 80 00 00 00 00 00 3F FF C0 00 00 00 20  00 00 00 20 27 03 FF FF FF FF FF FF FF FF FF FF  FF FF FF FF FF FC CC 10 00 03 20 00 00 00 00 00  00 00 00 00 00 00 00 00 00 01 90 01 90 00 00 00  00 01 00 7E C8 00 C8 00 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 01 C0 00 42 C5 46  86 C7 00 00 02 A0 00 00 12 60 78 70 65 5F 77 61  72 70 5F 67 61 74 65 5F 75 73 61 67 65 92 78 70  65 5F 69 6E 73 74 61 6E 74 5F 61 63 74 69 6F 6E  92 78 70 65 5F 73 61 6E 63 74 75 61 72 79 5F 68  65 6C 70 91 78 70 65 5F 62 61 74 74 6C 65 5F 72  61 6E 6B 5F 32 8E 78 70 65 5F 66 6F 72 6D 5F 73  71 75 61 64 8E 78 70 65 5F 74 68 5F 6E 6F 6E 73  61 6E 63 8B 78 70 65 5F 74 68 5F 61 6D 6D 6F 90  78 70 65 5F 74 68 5F 66 69 72 65 6D 6F 64 65 73  8F 75 73 65 64 5F 63 68 61 69 6E 62 6C 61 64 65  9A 76 69 73 69 74 65 64 5F 62 72 6F 61 64 63 61  73 74 5F 77 61 72 70 67 61 74 65 8E 76 69 73 69  74 65 64 5F 6C 6F 63 6B 65 72 8D 75 73 65 64 5F  70 75 6E 69 73 68 65 72 88 75 73 65 64 5F 72 65  6B 8D 75 73 65 64 5F 72 65 70 65 61 74 65 72 9F  76 69 73 69 74 65 64 5F 64 65 63 6F 6E 73 74 72  75 63 74 69 6F 6E 5F 74 65 72 6D 69 6E 61 6C 8F  75 73 65 64 5F 73 75 70 70 72 65 73 73 6F 72 96  76 69 73 69 74 65 64 5F 6F 72 64 65 72 5F 74 65  72 6D 69 6E 61 6C 85 6D 61 70 31 35 85 6D 61 70  31 34 85 6D 61 70 31 32 85 6D 61 70 30 31 00 00  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 00 01 0A 36 13 88  04 00 40 00 00 10 00 04 00 00 4D 6E 40 10 41 00  00 00 40 00 18 08 38 1C C0 20 32 00 00 07 80 15  E1 D0 02 10 20 00 00 08 00 03 01 07 13 A8 04 06  40 00 00 10 03 20 BB 00 42 E4 00 00 01 00 0E 07  70 08 6C 80 00 06 40 01 C0 F0 01 13 90 00 00 C8  00 38 1E 40 23 32 00 00 19 00 07 03 D0 05 0E 40  00 03 20 00 E8 7B 00 A4 C8 00 00 64 00 DA 4F 80  14 E1 00 00 00 40 00 18 08 38 1F 40 20 32 00 00  0A 00 08 " //fake data?
      val packet2 = hex"18 F8 00 00 00 BC 8C 10 90 3B 45 C6 FA 94 00 9F F0 00 00 40 00 08 C0 44 00 69 00 66 00 66 00 45" //fake data
      //val packet2Rest = packet2.bits.drop(8 + 32 + 1 + 11 + 16)
      var string_inventoryItem = hex"46 04 C0 08 08 80 00 00 20 00 0C 04 10 29 A0 10 19 00 00 04 00 00"
      val string_9mm = hex"18 7C000000 2580 0E0 0005 A1 C8000064000"
      val string_gauss = hex"18 DC000000 2580 2C9 B905 82 480000020000C04 1C00C0B0190000078000"
      val string_punisher = hex"18 27010000 2580 612 a706 82 080000020000c08 1c13a0d01900000780 13a4701a072000000800"
      val string_rek = hex"18 97000000 2580 6C2 9F05 81 48000002000080000"
      val string_testchar = hex"18 570C0000 BC8 4B00 6C2D7 65535 CA16 0 00 01 34 40 00 0970 49006C006C006C004900490049006C006C006C0049006C0049006C006C0049006C006C006C0049006C006C004900 84 52 70 76 1E 80 80 00 00 00 00 00 3FFFC 0 00 00 00 20 00 00 0F F6 A7 03 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FC 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 90 01 90 00 64 00 00 01 00 7E C8 00 C8 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 C0 00 42 C5 46  86 C7 00 00 00 80 00 00 12 40 78 70 65 5F 73 61 6E 63 74 75 61 72 79 5F 68 65 6C 70 90 78 70 65 5F 74 68 5F 66 69 72 65 6D 6F 64 65 73 8B 75 73 65 64 5F 62 65 61 6D 65 72 85 6D 61 70 31 33 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 0A 23 02 60 04 04 40 00 00 10 00 06 02 08 14 D0 08 0C 80 00 02 00 02 6B 4E 00 82 88 00 00 02 00 00 C0 41 C0 9E 01 01 90 00 00 64 00 44 2A 00 10 91 00 00 00 40 00 18 08 38 94 40 20 32 00 00 00 80 19 05 48 02 17 20 00 00 08 00 70 29 80 43 64 00 00 32 00 0E 05 40 08 9C 80 00 06 40 01 C0 AA 01 19 90 00 00 C8 00 3A 15 80 28 72 00 00 19 00 04 0A B8 05 26 40 00 03 20 06 C2 58 00 A7 88 00 00 02 00 00 80 00 00"

      "decode (2)" in {
        //an invalid bit representation will fail to turn into an object
        PacketCoding.DecodePacket(packet2).require match {
          case obj @ ObjectCreateMessage(len, cls, guid, parent, data) =>
            len mustEqual 248
            cls mustEqual 121
            guid mustEqual PlanetSideGUID(2497)
            parent mustEqual None
            data.isDefined mustEqual false
          case default =>
            ko
        }
      }

      "decode (9mm)" in {
        PacketCoding.DecodePacket(string_9mm).require match {
          case obj @ ObjectCreateMessage(len, cls, guid, parent, data) =>
            len mustEqual 124
            cls mustEqual 28
            guid mustEqual PlanetSideGUID(1280)
            parent.isDefined mustEqual true
            parent.get.guid mustEqual PlanetSideGUID(75)
            parent.get.slot mustEqual 33
            data.isDefined mustEqual true
            data.get.asInstanceOf[AmmoBoxData].magazine mustEqual 50
          case default =>
            ko
        }
      }

      "decode (gauss)" in {
        PacketCoding.DecodePacket(string_gauss).require match {
          case obj @ ObjectCreateMessage(len, cls, guid, parent, data) =>
            len mustEqual 220
            cls mustEqual 345
            guid mustEqual PlanetSideGUID(1465)
            parent.isDefined mustEqual true
            parent.get.guid mustEqual PlanetSideGUID(75)
            parent.get.slot mustEqual 2
            data.isDefined mustEqual true
            val obj_wep = data.get.asInstanceOf[WeaponData]
            obj_wep.unk mustEqual 4
            val obj_ammo = obj_wep.ammo
            obj_ammo.objectClass mustEqual 28
            obj_ammo.guid mustEqual PlanetSideGUID(1286)
            obj_ammo.parentSlot mustEqual 0
            obj_ammo.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 30
          case default =>
            ko
        }
      }

      "decode (punisher)" in {
        PacketCoding.DecodePacket(string_punisher).require match {
          case obj @ ObjectCreateMessage(len, cls, guid, parent, data) =>
            len mustEqual 295
            cls mustEqual 706
            guid mustEqual PlanetSideGUID(1703)
            parent.isDefined mustEqual true
            parent.get.guid mustEqual PlanetSideGUID(75)
            parent.get.slot mustEqual 2
            data.isDefined mustEqual true
            val obj_wep = data.get.asInstanceOf[ConcurrentFeedWeaponData]
            obj_wep.unk mustEqual 0
            val obj_ammo = obj_wep.ammo
            obj_ammo.size mustEqual 2
            obj_ammo.head.objectClass mustEqual 28
            obj_ammo.head.guid mustEqual PlanetSideGUID(1693)
            obj_ammo.head.parentSlot mustEqual 0
            obj_ammo.head.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 30
            obj_ammo(1).objectClass mustEqual 413
            obj_ammo(1).guid mustEqual PlanetSideGUID(1564)
            obj_ammo(1).parentSlot mustEqual 1
            obj_ammo(1).obj.asInstanceOf[AmmoBoxData].magazine mustEqual 1
          case _ =>
            ko
          }
        }

      "decode (rek)" in {
        PacketCoding.DecodePacket(string_rek).require match {
          case obj @ ObjectCreateMessage(len, cls, guid, parent, data) =>
            len mustEqual 151
            cls mustEqual 0x2D8
            guid mustEqual PlanetSideGUID(1439)
            parent.isDefined mustEqual true
            parent.get.guid mustEqual PlanetSideGUID(75)
            parent.get.slot mustEqual 1
            data.isDefined mustEqual true
            data.get.asInstanceOf[REKData].unk mustEqual 4
          case _ =>
            ko
        }
      }

      "decode (character)" in {
        PacketCoding.DecodePacket(string_testchar).require match {
          case obj @ ObjectCreateMessage(len, cls, guid, parent, data) =>
            len mustEqual 3159
            cls mustEqual 0x79
            guid mustEqual PlanetSideGUID(75)
            parent.isDefined mustEqual false
            data.isDefined mustEqual true

            val char = data.get.asInstanceOf[CharacterData]
            char.appearance.pos.x mustEqual 3674.8438f
            char.appearance.pos.y mustEqual 2726.789f
            char.appearance.pos.z mustEqual 91.15625f
            char.appearance.objYaw mustEqual 19
            char.appearance.faction mustEqual 2 //vs
            char.appearance.bops mustEqual false
            char.appearance.unk1 mustEqual 4
            char.appearance.name mustEqual "IlllIIIlllIlIllIlllIllI"
            char.appearance.exosuit mustEqual 4 //standard
            char.appearance.sex mustEqual 2 //female
            char.appearance.face1 mustEqual 2
            char.appearance.face2 mustEqual 9
            char.appearance.voice mustEqual 1 //female 1
            char.appearance.unk2 mustEqual 3
            char.appearance.unk3 mustEqual 118
            char.appearance.unk4 mustEqual 30
            char.appearance.unk5 mustEqual 0x8080
            char.appearance.unk6 mustEqual 0xFFFF
            char.appearance.unk7 mustEqual 2
            char.appearance.viewPitch mustEqual 0xFF
            char.appearance.viewYaw mustEqual 0x6A
            char.appearance.unk8 mustEqual 7
            char.appearance.ribbons.upper mustEqual 0xFFFFFFFFL //none
            char.appearance.ribbons.middle mustEqual 0xFFFFFFFFL //none
            char.appearance.ribbons.lower mustEqual 0xFFFFFFFFL //none
            char.appearance.ribbons.tos mustEqual 0xFFFFFFFFL //none
            char.healthMax mustEqual 100
            char.health mustEqual 100
            char.armor mustEqual 50 //standard exosuit value
            char.unk1 mustEqual 1
            char.unk2 mustEqual 7
            char.unk3 mustEqual 7
            char.staminaMax mustEqual 100
            char.stamina mustEqual 100
            char.unk4 mustEqual 28
            char.unk5 mustEqual 4
            char.unk6 mustEqual 44
            char.unk7 mustEqual 84
            char.unk8 mustEqual 104
            char.unk9 mustEqual 1900
            char.firstTimeEvents.size mustEqual 4
            char.firstTimeEvents.head mustEqual "xpe_sanctuary_help"
            char.firstTimeEvents(1) mustEqual "xpe_th_firemodes"
            char.firstTimeEvents(2) mustEqual "used_beamer"
            char.firstTimeEvents(3) mustEqual "map13"
            char.tutorials.size mustEqual 0
            char.inventory.unk1 mustEqual true
            char.inventory.unk2 mustEqual false
            char.inventory.contents.size mustEqual 10
            val inventory = char.inventory.contents
            //0
            inventory.head.item.objectClass mustEqual 0x8C //beamer
            inventory.head.item.guid mustEqual PlanetSideGUID(76)
            inventory.head.item.parentSlot mustEqual 0
            var wep = inventory.head.item.obj.asInstanceOf[WeaponData]
            wep.ammo.objectClass mustEqual 0x110 //plasma
            wep.ammo.guid mustEqual PlanetSideGUID(77)
            wep.ammo.parentSlot mustEqual 0
            wep.ammo.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 16
            //1
            inventory(1).item.objectClass mustEqual 0x34D //suppressor
            inventory(1).item.guid mustEqual PlanetSideGUID(78)
            inventory(1).item.parentSlot mustEqual 2
            wep = inventory(1).item.obj.asInstanceOf[WeaponData]
            wep.ammo.objectClass mustEqual 0x1C //9mm
            wep.ammo.guid mustEqual PlanetSideGUID(79)
            wep.ammo.parentSlot mustEqual 0
            wep.ammo.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 25
            //2
            inventory(2).item.objectClass mustEqual 0x144 //force blade
            inventory(2).item.guid mustEqual PlanetSideGUID(80)
            inventory(2).item.parentSlot mustEqual 4
            wep = inventory(2).item.obj.asInstanceOf[WeaponData]
            wep.ammo.objectClass mustEqual 0x21C //force blade ammo
            wep.ammo.guid mustEqual PlanetSideGUID(81)
            wep.ammo.parentSlot mustEqual 0
            wep.ammo.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 1
            //3
            inventory(3).item.objectClass mustEqual 0x1C8 //thing
            inventory(3).item.guid mustEqual PlanetSideGUID(82)
            inventory(3).item.parentSlot mustEqual 5
            inventory(3).item.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 1
            //4
            inventory(4).item.objectClass mustEqual 0x1C //9mm
            inventory(4).item.guid mustEqual PlanetSideGUID(83)
            inventory(4).item.parentSlot mustEqual 6
            inventory(4).item.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 50
            //5
            inventory(5).item.objectClass mustEqual 0x1C //9mm
            inventory(5).item.guid mustEqual PlanetSideGUID(84)
            inventory(5).item.parentSlot mustEqual 9
            inventory(5).item.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 50
            //6
            inventory(6).item.objectClass mustEqual 0x1C //9mm
            inventory(6).item.guid mustEqual PlanetSideGUID(85)
            inventory(6).item.parentSlot mustEqual 12
            inventory(6).item.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 50
            //7
            inventory(7).item.objectClass mustEqual 0x1D //9mm ap
            inventory(7).item.guid mustEqual PlanetSideGUID(86)
            inventory(7).item.parentSlot mustEqual 33
            inventory(7).item.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 50
            //8
            inventory(8).item.objectClass mustEqual 0x110 //plasma
            inventory(8).item.guid mustEqual PlanetSideGUID(87)
            inventory(8).item.parentSlot mustEqual 36
            inventory(8).item.obj.asInstanceOf[AmmoBoxData].magazine mustEqual 50
            //9
            inventory(9).item.objectClass mustEqual 0x2D8 //rek
            inventory(9).item.guid mustEqual PlanetSideGUID(88)
            inventory(9).item.parentSlot mustEqual 39
          //the rek has data but none worth testing here
          case default =>
            ko
        }
      }

      "encode (2)" in {
        //the lack of an object will fail to turn into a bad bitstream
        val msg = ObjectCreateMessage(0, 121, PlanetSideGUID(2497), None, None)
        PacketCoding.EncodePacket(msg).isFailure mustEqual true
      }

      "encode (9mm)" in {
        val obj = AmmoBoxData(50)
        val msg = ObjectCreateMessage(0, 28, PlanetSideGUID(1280), ObjectCreateMessageParent(PlanetSideGUID(75), 33), obj)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_9mm
      }

      "encode (gauss)" in {
        val obj = WeaponData(4, 28, PlanetSideGUID(1286), 0, AmmoBoxData(30))
        val msg = ObjectCreateMessage(0, 345, PlanetSideGUID(1465), ObjectCreateMessageParent(PlanetSideGUID(75), 2), obj)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_gauss
      }

      "encode (punisher)" in {
        val obj = ConcurrentFeedWeaponData(0, AmmoBoxData(28, PlanetSideGUID(1693), 0, AmmoBoxData(30)) :: AmmoBoxData(413, PlanetSideGUID(1564), 1, AmmoBoxData(1)) :: Nil)
        val msg = ObjectCreateMessage(0, 706, PlanetSideGUID(1703), ObjectCreateMessageParent(PlanetSideGUID(75), 2), obj)
        var pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_punisher
      }

      "encode (rek)" in {
        val obj = REKData(4)
        val msg = ObjectCreateMessage(0, 0x2D8, PlanetSideGUID(1439), ObjectCreateMessageParent(PlanetSideGUID(75), 1), obj)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_rek
      }

      "encode (character)" in {
        val app = CharacterAppearanceData(
          Vector3(3674.8438f, 2726.789f, 91.15625f),
          19,
          2,
          false,
          4,
          "IlllIIIlllIlIllIlllIllI",
          4,
          2,
          2,9,
          1,
          3, 118,30, 0x8080, 0xFFFF, 2,
          255, 106, 7,
          RibbonBars()
        )
        val inv = InventoryItem(0x8C, PlanetSideGUID(76), 0, WeaponData(8, 0x110, PlanetSideGUID(77), 0, AmmoBoxData(16))) ::
          InventoryItem(0x34D, PlanetSideGUID(78), 2, WeaponData(8, 0x1C, PlanetSideGUID(79), 0, AmmoBoxData(25))) ::
          InventoryItem(0x144, PlanetSideGUID(80), 4, WeaponData(8, 0x21C, PlanetSideGUID(81), 0, AmmoBoxData(1))) ::
          InventoryItem(0x1C8, PlanetSideGUID(82), 5, AmmoBoxData(1)) ::
          InventoryItem(0x1C, PlanetSideGUID(83), 6, AmmoBoxData(50)) ::
          InventoryItem(0x1C, PlanetSideGUID(84), 9, AmmoBoxData(50)) ::
          InventoryItem(0x1C, PlanetSideGUID(85), 12, AmmoBoxData(50)) ::
          InventoryItem(0x1D, PlanetSideGUID(86), 33, AmmoBoxData(50)) ::
          InventoryItem(0x110, PlanetSideGUID(87), 36, AmmoBoxData(50)) ::
          InventoryItem(0x2D8, PlanetSideGUID(88), 39, REKData(8)) ::
          Nil
        val obj = CharacterData(
          app,
          100, 100,
          50,
          1, 7, 7,
          100, 100,
          28, 4, 44, 84, 104, 1900,
          "xpe_sanctuary_help" :: "xpe_th_firemodes" :: "used_beamer" :: "map13" :: Nil,
          List.empty,
          InventoryData(
            true, false, false, inv
          )
        )
        val msg = ObjectCreateMessage(0, 0x79, PlanetSideGUID(75), obj)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_testchar
      }
    }

    "ChatMsg" should {
      val string_local = hex"12 1A C000 83610062006300"
      val string_tell  = hex"12 20 C180640065006600 83610062006300"

      "decode" in {
        PacketCoding.DecodePacket(string_local).require match {
          case ChatMsg(messagetype, has_wide_contents, recipient, contents, note_contents) =>
            messagetype mustEqual ChatMessageType.CMT_OPEN
            has_wide_contents mustEqual true
            recipient mustEqual ""
            contents mustEqual "abc"
            note_contents mustEqual None
          case default =>
            ko
        }

        PacketCoding.DecodePacket(string_tell).require match {
          case ChatMsg(messagetype, has_wide_contents, recipient, contents, note_contents) =>
            messagetype mustEqual ChatMessageType.CMT_TELL
            has_wide_contents mustEqual true
            recipient mustEqual "def"
            contents mustEqual "abc"
            note_contents mustEqual None
          case default =>
            ko
        }
      }

      "encode" in {
        val msg_local = ChatMsg(ChatMessageType.CMT_OPEN, true, "", "abc", None)
        val pkt_local = PacketCoding.EncodePacket(msg_local).require.toByteVector

        pkt_local mustEqual string_local

        val msg_tell = ChatMsg(ChatMessageType.CMT_TELL, true, "def", "abc", None)
        val pkt_tell = PacketCoding.EncodePacket(msg_tell).require.toByteVector

        pkt_tell mustEqual string_tell
      }

      "allow and disallow note" in {
        ChatMsg(ChatMessageType.CMT_ARMOR,
          false,
          "DontCare", "DontCare", Some("Should be here")) must throwA[AssertionError]
        ChatMsg(ChatMessageType.CMT_NOTE,
          false,
          "DontCare", "DontCare", None) must throwA[AssertionError]
      }
    }

    "CharacterNoRecordMessage" should {
      val string = hex"13 00400000" //we have no record of this packet, so here's something fake that works

      "deocde" in {
        PacketCoding.DecodePacket(string).require match {
          case CharacterNoRecordMessage(unk) =>
            unk mustEqual 16384
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = CharacterNoRecordMessage(16384)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ChangeFireModeMessage" should {
      val string = hex"46 4C0020"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ChangeFireModeMessage(item_guid, fire_mode) =>
            item_guid mustEqual PlanetSideGUID(76)
            fire_mode mustEqual 1
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ChangeFireModeMessage(PlanetSideGUID(76), 1)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ChangeFireStateMessage_Start" should {
      val string = hex"39 4C00"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ChangeFireStateMessage_Start(item_guid) =>
            item_guid mustEqual PlanetSideGUID(76)
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ChangeFireStateMessage_Start(PlanetSideGUID(76))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ChangeFireStateMessage_Stop" should {
      val string = hex"3A 4C00"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ChangeFireStateMessage_Stop(item_guid) =>
            item_guid mustEqual PlanetSideGUID(76)
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ChangeFireStateMessage_Stop(PlanetSideGUID(76))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "EmoteMsg" should {
      val string = hex"25 4B00 15"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case EmoteMsg(avatar_guid, emote) =>
            avatar_guid mustEqual PlanetSideGUID(75)
            emote mustEqual EmoteType.Thumbsdown
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = EmoteMsg(PlanetSideGUID(75), EmoteType.Thumbsdown)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "CreateShortcutMessage" should {
      val stringMedkit = hex"28 7210 01 00 90 C0 6D65646B6974 80 80"
      val stringMacro = hex"28 4C05 08 00 B1 C0 73686F72746375745F6D6163726F 83 4E00 5400 5500 9B 2F00 7000 6C00 6100 7400 6F00 6F00 6E00 2000 4900 6E00 6300 6F00 6D00 6900 6E00 6700 2000 4E00 5400 5500 2000 7300 7000 6100 6D00 2100"
      val stringRemove = hex"28 4C05 01 00 00"

      "decode (medkit)" in {
        PacketCoding.DecodePacket(stringMedkit).require match {
          case CreateShortcutMessage(player_guid, slot, unk, addShortcut, shortcut) =>
            player_guid mustEqual PlanetSideGUID(4210)
            slot mustEqual 1
            unk mustEqual 0
            addShortcut mustEqual true
            shortcut.isDefined mustEqual true
            shortcut.get.purpose mustEqual 0
            shortcut.get.tile mustEqual "medkit"
            shortcut.get.effect1 mustEqual ""
            shortcut.get.effect2 mustEqual ""
          case _ =>
            ko
        }
      }

      "decode (macro)" in {
        PacketCoding.DecodePacket(stringMacro).require match {
          case CreateShortcutMessage(player_guid, slot, unk, addShortcut, shortcut) =>
            player_guid mustEqual PlanetSideGUID(1356)
            slot mustEqual 8
            unk mustEqual 0
            addShortcut mustEqual true
            shortcut.isDefined mustEqual true
            shortcut.get.purpose mustEqual 1
            shortcut.get.tile mustEqual "shortcut_macro"
            shortcut.get.effect1 mustEqual "NTU"
            shortcut.get.effect2 mustEqual "/platoon Incoming NTU spam!"
          case _ =>
            ko
        }
      }

      "decode (remove)" in {
        PacketCoding.DecodePacket(stringRemove).require match {
          case CreateShortcutMessage(player_guid, slot, unk, addShortcut, shortcut) =>
            player_guid mustEqual PlanetSideGUID(1356)
            slot mustEqual 1
            unk mustEqual 0
            addShortcut mustEqual false
            shortcut.isDefined mustEqual false
          case _ =>
            ko
        }
      }

      "encode (medkit)" in {
        val msg = CreateShortcutMessage(PlanetSideGUID(4210), 1, 0, true, Some(Shortcut(0, "medkit")))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual stringMedkit
      }

      "encode (macro)" in {
        val msg = CreateShortcutMessage(PlanetSideGUID(1356), 8, 0, true, Some(Shortcut(1, "shortcut_macro", "NTU", "/platoon Incoming NTU spam!")))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual stringMacro
      }

      "encode (remove)" in {
        val msg = CreateShortcutMessage(PlanetSideGUID(1356), 1, 0, false)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual stringRemove
      }

      "macro" in {
        val MACRO : Some[Shortcut] = Shortcut.MACRO("NTU", "/platoon Incoming NTU spam!")
        MACRO.get.purpose mustEqual 1
        MACRO.get.tile mustEqual "shortcut_macro"
        MACRO.get.effect1 mustEqual "NTU"
        MACRO.get.effect2 mustEqual "/platoon Incoming NTU spam!"
      }

      "presets" in {
        Shortcut.AUDIO_AMPLIFIER.get.purpose mustEqual 2
        Shortcut.AUDIO_AMPLIFIER.get.tile mustEqual "audio_amplifier"
        Shortcut.DARKLIGHT_VISION.get.purpose mustEqual 2
        Shortcut.DARKLIGHT_VISION.get.tile mustEqual "darklight_vision"
        Shortcut.ENHANCED_TARGETING.get.purpose mustEqual 2
        Shortcut.ENHANCED_TARGETING.get.tile mustEqual "targeting"
        Shortcut.MEDKIT.get.purpose mustEqual 0
        Shortcut.MEDKIT.get.tile mustEqual "medkit"
        Shortcut.MELEE_BOOSTER.get.purpose mustEqual 2
        Shortcut.MELEE_BOOSTER.get.tile mustEqual "melee_booster"
        Shortcut.PERSONAL_SHIELD.get.purpose mustEqual 2
        Shortcut.PERSONAL_SHIELD.get.tile mustEqual "personal_shield"
        Shortcut.RANGE_MAGNIFIER.get.purpose mustEqual 2
        Shortcut.RANGE_MAGNIFIER.get.tile mustEqual "range_magnifier"
        Shortcut.REGENERATION.get.purpose mustEqual 2
        Shortcut.REGENERATION.get.tile mustEqual "advanced_regen"
        Shortcut.SECOND_WIND.get.purpose mustEqual 2
        Shortcut.SECOND_WIND.get.tile mustEqual "second_wind"
        Shortcut.SENSOR_SHIELD.get.purpose mustEqual 2
        Shortcut.SENSOR_SHIELD.get.tile mustEqual "silent_run"
        Shortcut.SURGE.get.purpose mustEqual 2
        Shortcut.SURGE.get.tile mustEqual "surge"
      }
    }

    "ObjectAttachMessage" should {
      val stringToInventory = hex"2A 9F05 D405 86"
      val stringToCursor = hex"2A 9F05 D405 00FA"

      "decode (inventory 1,1)" in {
        PacketCoding.DecodePacket(stringToInventory).require match {
          case ObjectAttachMessage(player_guid, item_guid, index) =>
            player_guid mustEqual PlanetSideGUID(1439)
            item_guid mustEqual PlanetSideGUID(1492)
            index mustEqual 6
          case default =>
            ko
        }
      }

      "decode (cursor)" in {
        PacketCoding.DecodePacket(stringToCursor).require match {
          case ObjectAttachMessage(player_guid, item_guid, index) =>
            player_guid mustEqual PlanetSideGUID(1439)
            item_guid mustEqual PlanetSideGUID(1492)
            index mustEqual 250
          case default =>
            ko
        }
      }

      "encode (inventory 1,1)" in {
        val msg = ObjectAttachMessage(PlanetSideGUID(1439), PlanetSideGUID(1492), 6)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual stringToInventory
      }

      "encode (cursor)" in {
        val msg = ObjectAttachMessage(PlanetSideGUID(1439), PlanetSideGUID(1492), 250)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual stringToCursor
      }
    }

    "ChangeShortcutBankMessage" should {
      val string = hex"29 4B00 20"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ChangeShortcutBankMessage(player_guid, bank) =>
            player_guid mustEqual PlanetSideGUID(75)
            bank mustEqual 2
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ChangeShortcutBankMessage(PlanetSideGUID(75), 2)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "DropItemMessage" should {
      val string = hex"37 4C00"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case DropItemMessage(item_guid) =>
            item_guid mustEqual PlanetSideGUID(76)
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = DropItemMessage(PlanetSideGUID(76))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ReloadMessage" should {
      val string = hex"0D 4C00 7B000000 FFFFFFFF"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ReloadMessage(item_guid, ammo_clip, unk1) =>
            item_guid mustEqual PlanetSideGUID(76)
            ammo_clip mustEqual 123
            unk1 mustEqual -1
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ReloadMessage(PlanetSideGUID(76), 123, -1)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "MountVehicleMsg" should {
      val string = hex"0E E104 6704 06"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case MountVehicleMsg(player_guid, vehicle_guid, entry) =>
            player_guid mustEqual PlanetSideGUID(1249)
            vehicle_guid mustEqual PlanetSideGUID(1127)
            entry mustEqual 6
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = MountVehicleMsg(PlanetSideGUID(1249), PlanetSideGUID(1127), 6)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ObjectHeldMessage" should {
      val string = hex"33 4B00 02 00"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ObjectHeldMessage(avatar_guid, held_holsters, unk1) =>
            avatar_guid mustEqual PlanetSideGUID(75)
            held_holsters mustEqual (1 << 1)
            unk1 mustEqual false
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ObjectHeldMessage(PlanetSideGUID(75), (1 << 1), false)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "AvatarJumpMessage" should {
      val string = hex"35 80"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case AvatarJumpMessage(state) =>
            state mustEqual true
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = AvatarJumpMessage(true)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "RequestDestroyMessage" should {
      val string = hex"2D A49C"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case RequestDestroyMessage(object_guid) =>
            object_guid mustEqual PlanetSideGUID(40100)
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = RequestDestroyMessage(PlanetSideGUID(40100))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ObjectDeleteMessage" should {
      val string = hex"19 4C00 00"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ObjectDeleteMessage(object_guid, unk1) =>
            object_guid mustEqual PlanetSideGUID(76)
            unk1 mustEqual 0
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ObjectDeleteMessage(PlanetSideGUID(76), 0)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "MoveItemMessage" should {
      val string = hex"11 4C00 4B00 4B00 0900 0100"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case MoveItemMessage(item_guid, avatar_guid_1, avatar_guid_2, dest, unk1) =>
            item_guid mustEqual PlanetSideGUID(76)
            avatar_guid_1 mustEqual PlanetSideGUID(75)
            avatar_guid_2 mustEqual PlanetSideGUID(75)
            dest mustEqual 9
            unk1 mustEqual 1
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = MoveItemMessage(PlanetSideGUID(76), PlanetSideGUID(75), PlanetSideGUID(75), 9, 1)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ChangeAmmoMessage" should {
      val string = hex"47 4E00 00000000"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ChangeAmmoMessage(item_guid, unk1) =>
            item_guid mustEqual PlanetSideGUID(78)
            unk1 mustEqual 0
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ChangeAmmoMessage(PlanetSideGUID(78), 0)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "TimeOfDayMessage" should {
      val string = hex"48 00 00 00 47 00 00 20 41"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case TimeOfDayMessage(time, unk) =>
            time mustEqual 1191182336
            unk mustEqual 1092616192
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = TimeOfDayMessage(1191182336)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "PlayerStateMessageUpstream" should {
      val string = hex"BD 4B000 E377BA575B616C640A70004014060110007000000"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case PlayerStateMessageUpstream(avatar_guid, pos, vel, unk1, aim_pitch, unk2, seq_time, unk3, is_crouching, unk4, unk5, is_cloaking, unk6, unk7) =>
            avatar_guid mustEqual PlanetSideGUID(75)
            pos mustEqual Vector3(3694.1094f, 2735.4531f, 90.84375f)
            vel mustEqual Some(Vector3(4.375f, 2.59375f, 0.0f))
            unk1 mustEqual 10
            aim_pitch mustEqual 3
            unk2 mustEqual 0
            seq_time mustEqual 136
            unk3 mustEqual 0
            is_crouching mustEqual false
            unk4 mustEqual false
            unk5 mustEqual false
            is_cloaking mustEqual false
            unk6 mustEqual 112
            unk7 mustEqual 0
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = PlayerStateMessageUpstream(PlanetSideGUID(75), Vector3(3694.1094f, 2735.4531f, 90.84375f), Some(Vector3(4.375f, 2.59375f, 0.0f)), 10, 3, 0, 136, 0, false, false, false, false, 112, 0)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "PlayerStateShiftMessage" should {
      val string_short = hex"BE 68"
      val string_pos = hex"BE 95 A0 89 13 91 B8 B0 BF F0"
      val string_posAndVel = hex"BE AE 01 29 CD 59 B9 40 C0 EA D4 00 0F 86 40"

      "decode (short)" in {
        PacketCoding.DecodePacket(string_short).require match {
          case PlayerStateShiftMessage(state, unk) =>
            state.isDefined mustEqual false
            unk.isDefined mustEqual true
            unk.get mustEqual 5
          case _ =>
            ko
        }
      }

      "decode (pos)" in {
        PacketCoding.DecodePacket(string_pos).require match {
          case PlayerStateShiftMessage(state, unk) =>
            state.isDefined mustEqual true
            state.get.unk mustEqual 1
            state.get.pos.x mustEqual 4624.703f
            state.get.pos.y mustEqual 5922.1484f
            state.get.pos.z mustEqual 46.171875f
            state.get.viewYawLim mustEqual 255
            state.get.vel.isDefined mustEqual false
            unk.isDefined mustEqual false
          case _ =>
            ko
        }
      }

      "decode (pos and vel)" in {
        PacketCoding.DecodePacket(string_posAndVel).require match {
          case PlayerStateShiftMessage(state, unk) =>
            state.isDefined mustEqual true
            state.get.unk mustEqual 2
            state.get.pos.x mustEqual 4645.75f
            state.get.pos.y mustEqual 5811.6016f
            state.get.pos.z mustEqual 50.3125f
            state.get.viewYawLim mustEqual 14
            state.get.vel.isDefined mustEqual true
            state.get.vel.get.x mustEqual 2.8125f
            state.get.vel.get.y mustEqual -8.0f
            state.get.vel.get.z mustEqual 0.375f
            unk.isDefined mustEqual false
          case _ =>
            ko
        }
      }

      "encode (short)" in {
        val msg = PlayerStateShiftMessage(5)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_short
      }

      "encode (pos)" in {
        val msg = PlayerStateShiftMessage(ShiftState(1, Vector3(4624.703f, 5922.1484f, 46.171875f), 255))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_pos
      }

      "encode (pos and vel)" in {
        val msg = PlayerStateShiftMessage(ShiftState(2, Vector3(4645.75f, 5811.6016f, 50.3125f), 14, Vector3(2.8125f, -8.0f, 0.375f)))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_posAndVel
      }
    }

    "UseItemMessage" should {
      val string = hex"10 4B00 0000 7401 FFFFFFFF 4001000000000000000000000000058C803600800000"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case UseItemMessage(avatar_guid, unk1, object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, unk9) =>
            avatar_guid mustEqual PlanetSideGUID(75)
            unk1 mustEqual 0
            object_guid mustEqual PlanetSideGUID(372)
            unk2 mustEqual 0xFFFFFFFFL
            unk3 mustEqual false
            unk4 mustEqual Vector3(5.0f, 0.0f, 0.0f)
            unk5 mustEqual Vector3(0.0f, 0.0f, 0.0f)
            unk6 mustEqual 11
            unk7 mustEqual 25
            unk8 mustEqual 0
            unk9 mustEqual 364
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = UseItemMessage(PlanetSideGUID(75), 0, PlanetSideGUID(372), 0xFFFFFFFFL, false, Vector3(5.0f, 0.0f, 0.0f), Vector3(0.0f, 0.0f, 0.0f), 11, 25, 0, 364)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "GenericObjectStateMsg" should {
      val string = hex"1D 6401 10000000"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case GenericObjectStateMsg(object_guid, state) =>
            object_guid mustEqual PlanetSideGUID(356)
            state mustEqual 16
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = GenericObjectStateMsg(PlanetSideGUID(356), 16)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ItemTransactionMessage" should {
      val string_buy = hex"44 4C03 4000110070756E6973686572000000"
      val string_sell = hex"44 5303 60001000004E00"
      val string_forget = hex"44 BA00 600011006861726173736572000000"

      "decode" in {
        PacketCoding.DecodePacket(string_buy).require match {
          case ItemTransactionMessage(terminal_guid, transaction_type, item_page, item_name, unk1, item_guid) =>
            terminal_guid mustEqual PlanetSideGUID(844)
            transaction_type mustEqual TransactionType.Buy
            item_page mustEqual 0
            item_name mustEqual "punisher"
            unk1 mustEqual 0
            item_guid mustEqual PlanetSideGUID(0)
          case default =>
            ko
        }

        PacketCoding.DecodePacket(string_sell).require match {
          case ItemTransactionMessage(terminal_guid, transaction_type, item_page, item_name, unk1, item_guid) =>
            terminal_guid mustEqual PlanetSideGUID(851)
            transaction_type mustEqual TransactionType.Sell
            item_page mustEqual 0
            item_name mustEqual ""
            unk1 mustEqual 0
            item_guid mustEqual PlanetSideGUID(78)
          case default =>
            ko
        }

        PacketCoding.DecodePacket(string_forget).require match {
          case ItemTransactionMessage(terminal_guid, transaction_type, item_page, item_name, unk1, item_guid) =>
            terminal_guid mustEqual PlanetSideGUID(186)
            transaction_type mustEqual TransactionType.Sell
            item_page mustEqual 0
            item_name mustEqual "harasser"
            unk1 mustEqual 0
            item_guid mustEqual PlanetSideGUID(0)
          case default =>
            ko
        }
      }

      "encode" in {
        val msg_buy = ItemTransactionMessage(PlanetSideGUID(844), TransactionType.Buy, 0, "punisher", 0, PlanetSideGUID(0))
        val pkt_buy = PacketCoding.EncodePacket(msg_buy).require.toByteVector

        pkt_buy mustEqual string_buy

        val msg_sell = ItemTransactionMessage(PlanetSideGUID(851), TransactionType.Sell, 0, "", 0, PlanetSideGUID(78))
        val pkt_sell = PacketCoding.EncodePacket(msg_sell).require.toByteVector

        pkt_sell mustEqual string_sell

        val msg_forget = ItemTransactionMessage(PlanetSideGUID(186), TransactionType.Sell, 0, "harasser", 0, PlanetSideGUID(0))
        val pkt_forget = PacketCoding.EncodePacket(msg_forget).require.toByteVector

        pkt_forget mustEqual string_forget
      }
    }

    "WeaponDelayFireMessage" should {
      val string = hex"88 A3140000"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case WeaponDelayFireMessage(seq_time, weapon_guid) =>
            seq_time mustEqual 163
            weapon_guid mustEqual PlanetSideGUID(80)
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = WeaponDelayFireMessage(163, PlanetSideGUID(80))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "BugReportMessage" should {
      val string = hex"89 03000000 0F000000 8B4465632020322032303039 1 1 0 19 6C511 656B1 7A11 830610062006300 843100320033003400"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case BugReportMessage(major, minor, date, btype, repeat, unk, zone, loc, summary, desc) =>
            major mustEqual 3
            minor mustEqual 15
            date mustEqual "Dec  2 2009"
            btype mustEqual BugType.GAMEPLAY
            repeat mustEqual true
            zone mustEqual 25
            loc.x mustEqual 674.84375f
            loc.y mustEqual 726.78906f
            loc.z mustEqual 69.90625f
            summary mustEqual "abc"
            desc mustEqual "1234"
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = BugReportMessage(3, 15, "Dec  2 2009",
          BugType.GAMEPLAY, true, 0, 25, Vector3(674.84375f, 726.78906f, 69.90625f),
          "abc", "1234")
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "PlayerStasisMessage" should {
      val string = hex"8A 4B 00 80"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case PlayerStasisMessage(player_guid, stasis) =>
            player_guid mustEqual PlanetSideGUID(75)
            stasis mustEqual true
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = PlayerStasisMessage(PlanetSideGUID(75))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ContinentalLockUpdateMessage" should {
      val string = hex"A8 16 00 40"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ContinentalLockUpdateMessage(continent_guid, empire) =>
            continent_guid mustEqual PlanetSideGUID(22)
            empire mustEqual PlanetSideEmpire.NC
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ContinentalLockUpdateMessage(PlanetSideGUID(22), PlanetSideEmpire.NC)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "AvatarGrenadeStateMessage" should {
      val string = hex"A9 DA11 01"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case AvatarGrenadeStateMessage(player_guid, state) =>
            player_guid mustEqual PlanetSideGUID(4570)
            state mustEqual GrenadeState.PRIMED
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = AvatarGrenadeStateMessage(PlanetSideGUID(4570), GrenadeState.PRIMED)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "BroadcastWarpgateUpdateMessage" should {
      val string = hex"D9 0D 00 01 00 20"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case BroadcastWarpgateUpdateMessage(continent_guid, building_guid, state1, state2, state3) =>
            continent_guid mustEqual PlanetSideGUID(13)
            building_guid mustEqual PlanetSideGUID(1)
            state1 mustEqual false
            state2 mustEqual false
            state3 mustEqual true
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = BroadcastWarpgateUpdateMessage(PlanetSideGUID(13), PlanetSideGUID(1), false, false, true)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ZoneLockInfoMesage" should {
      val string = hex"DF 1B 00 40"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ZoneLockInfoMessage(zone, locked, unk) =>
            zone mustEqual PlanetSideGUID(27)
            locked mustEqual false
            unk mustEqual true
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ZoneLockInfoMessage(PlanetSideGUID(27), false, true)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "BattleExperienceMessage" should {
      val string = hex"B4 8A0A E7030000 00"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case BattleExperienceMessage(player_guid, experience, unk) =>
            player_guid mustEqual PlanetSideGUID(2698)
            experience mustEqual 999
            unk mustEqual 0
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = BattleExperienceMessage(PlanetSideGUID(2698), 999, 0)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ZonePopulationUpdateMessage" should {
      val string = hex"B6 0400 9E010000 8A000000 25000000 8A000000 25000000 8A000000 25000000 8A000000 25000000"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ZonePopulationUpdateMessage(continent_guid, zone_queue, tr_queue, tr_pop, nc_queue, nc_pop, vs_queue, vs_pop, bo_queue, bo_pop) =>
            continent_guid mustEqual PlanetSideGUID(4)
            zone_queue mustEqual 414
            tr_queue mustEqual 138
            tr_pop mustEqual 37
            nc_queue mustEqual 138
            nc_pop mustEqual 37
            vs_queue mustEqual 138
            vs_pop mustEqual 37
            bo_queue mustEqual 138
            bo_pop mustEqual 37
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ZonePopulationUpdateMessage(PlanetSideGUID(4), 414, 138, 37, 138, 37, 138, 37, 138, 37)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "DisconnectMessage" should {
      val string = hex"B7 85 46 69 72 73 74 86 53 65 63 6F 6E 64 8E 46 69 72 73 74 20 26 20 73 65 63 6F 6E 64"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case DisconnectMessage(unk1, unk2, unk3) =>
            unk1 mustEqual "First"
            unk2 mustEqual "Second"
            unk3 mustEqual "First & second"
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = DisconnectMessage("First", "Second", "First & second")
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "OrbitalStrikeWaypointMessage" should {
      val string_on = hex"B9 46 0C AA E3 D2 2A 92 00"
      val string_off = hex"B9 46 0C 00"

      "decode (on)" in {
        PacketCoding.DecodePacket(string_on).require match {
          case OrbitalStrikeWaypointMessage(guid, coords) =>
            guid mustEqual PlanetSideGUID(3142)
            coords.isDefined mustEqual true
            coords.get.x mustEqual 5518.664f
            coords.get.y mustEqual 2212.539f
          case default =>
            ko
        }
      }

      "decode (off)" in {
        PacketCoding.DecodePacket(string_off).require match {
          case OrbitalStrikeWaypointMessage(guid, coords) =>
            guid mustEqual PlanetSideGUID(3142)
            coords.isDefined mustEqual false
          case default =>
            ko
        }
      }

      "encode (on)" in {
        val msg = OrbitalStrikeWaypointMessage(PlanetSideGUID(3142), 5518.664f, 2212.539f)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_on
      }

      "encode (off)" in {
        val msg = OrbitalStrikeWaypointMessage(PlanetSideGUID(3142))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_off
      }
    }

    "ExperienceAddedMessage" should {
      val string = hex"B8 04 03"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ExperienceAddedMessage(exp, unk) =>
            exp mustEqual 260 //0x104
            unk mustEqual true
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ExperienceAddedMessage(260)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "WeaponFireMessage" should {
      val string = hex"34 44130029272F0B5DFD4D4EC5C00009BEF78172003FC0"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case WeaponFireMessage(seq_time, weapon_guid, projectile_guid, shot_origin, unk1, unk2, unk3, unk4, unk5, unk6, unk7) =>
            seq_time mustEqual 68
            weapon_guid mustEqual PlanetSideGUID(76)
            projectile_guid mustEqual PlanetSideGUID(40100)
            shot_origin mustEqual Vector3(3675.4688f, 2726.9922f, 92.921875f)
            unk1 mustEqual 0
            unk2 mustEqual 64294
            unk3 mustEqual 1502
            unk4 mustEqual 200
            unk5 mustEqual 255
            unk6 mustEqual 0
            unk7 mustEqual None
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = WeaponFireMessage(68, PlanetSideGUID(76), PlanetSideGUID(40100), Vector3(3675.4688f, 2726.9922f, 92.921875f), 0, 64294, 1502, 200, 255, 0, None)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "HitMessage" should {
      val string_hitgeneric = hex"09 09E9A70200"
      val string_hitobj = hex"09 99292705F4B1FB9514585F08BDD3D454CC5EE80300"

      "decode" in {
        PacketCoding.DecodePacket(string_hitgeneric).require match {
          case HitMessage(seq_time, projectile_guid, unk1, hit_info, unk2, unk3, unk4) =>
            seq_time mustEqual 777
            projectile_guid mustEqual PlanetSideGUID(40102)
            unk1 mustEqual 0
            hit_info mustEqual None
            unk2 mustEqual true
            unk3 mustEqual false
            unk4 mustEqual None
          case default =>
            ko
        }

        PacketCoding.DecodePacket(string_hitobj).require match {
          case HitMessage(seq_time, projectile_guid, unk1, hit_info, unk2, unk3, unk4) =>
            seq_time mustEqual 153
            projectile_guid mustEqual PlanetSideGUID(40100)
            unk1 mustEqual 0
            hit_info mustEqual Some(HitInfo(Vector3(3672.9766f, 2729.8594f, 92.34375f), Vector3(3679.5156f, 2722.6172f, 92.796875f), Some(PlanetSideGUID(372))))
            unk2 mustEqual true
            unk3 mustEqual false
            unk4 mustEqual None
          case default =>
            ko
        }
      }

      "encode" in {
        val msg_hitgeneric = HitMessage(777, PlanetSideGUID(40102), 0, None, true, false, None)
        val pkt_hitgeneric = PacketCoding.EncodePacket(msg_hitgeneric).require.toByteVector

        pkt_hitgeneric mustEqual string_hitgeneric

        val msg_hitobj = HitMessage(153, PlanetSideGUID(40100), 0, Some(HitInfo(Vector3(3672.9766f, 2729.8594f, 92.34375f), Vector3(3679.5156f, 2722.6172f, 92.796875f), Some(PlanetSideGUID(372)))), true, false, None)
        val pkt_hitobj = PacketCoding.EncodePacket(msg_hitobj).require.toByteVector

        pkt_hitobj mustEqual string_hitobj
      }
    }

    "AvatarFirstTimeEventMessage" should {
      val string = hex"69 4b00 c000 01000000 9e 766973697465645f63657274696669636174696f6e5f7465726d696e616c"
    
      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case AvatarFirstTimeEventMessage(avatar_guid, object_guid, unk1, event_name) =>
            avatar_guid mustEqual PlanetSideGUID(75)
            object_guid mustEqual PlanetSideGUID(192)
            unk1 mustEqual 1
            event_name mustEqual "visited_certification_terminal"
          case default =>
            ko
        }
      }
      
      "encode" in {
        val msg = AvatarFirstTimeEventMessage(PlanetSideGUID(75), PlanetSideGUID(192), 1, "visited_certification_terminal")
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }
    
    "FriendsResponse" should {
      val stringOneFriend = hex"73 61 8C 60 4B007500720074004800650063007400690063002D004700 00"
      val stringManyFriends = hex"73 01 AC 48 4100 6E00 6700 6500 6C00 6C00 6F00 2D00 5700 47 00 7400 6800 6500 7000 6800 6100 7400 7400 7000 6800 7200 6F00 6700 6700 46 80 4B00 6900 6D00 7000 6F00 7300 7300 6900 6200 6C00 6500 3100 3200 45 00 5A00 6500 6100 7200 7400 6800 6C00 6900 6E00 6700 46 00 4B00 7500 7200 7400 4800 6500 6300 7400 6900 6300 2D00 4700 00"
      val stringShort = hex"73 81 80"

      "decode (one friend)" in {
        PacketCoding.DecodePacket(stringOneFriend).require match {
          case FriendsResponse(action, unk2, unk3, unk4, list) =>
            action mustEqual 3
            unk2 mustEqual 0
            unk3 mustEqual true
            unk4 mustEqual true
            list.size mustEqual 1
            list.head.name mustEqual "KurtHectic-G"
            list.head.online mustEqual false
          case default =>
            ko
        }
      }

      "decode (multiple friends)" in {
        PacketCoding.DecodePacket(stringManyFriends).require match {
          case FriendsResponse(action, unk2, unk3, unk4, list) =>
            action mustEqual 0
            unk2 mustEqual 0
            unk3 mustEqual true
            unk4 mustEqual true
            list.size mustEqual 5
            list.head.name mustEqual "Angello-W"
            list.head.online mustEqual false
            list(1).name mustEqual "thephattphrogg"
            list(1).online mustEqual false
            list(2).name mustEqual "Kimpossible12"
            list(2).online mustEqual false
            list(3).name mustEqual "Zearthling"
            list(3).online mustEqual false
            list(4).name mustEqual "KurtHectic-G"
            list(4).online mustEqual false
          case default =>
            ko
        }
      }

      "decode (short)" in {
        PacketCoding.DecodePacket(stringShort).require match {
          case FriendsResponse(action, unk2, unk3, unk4, list) =>
            action mustEqual 4
            unk2 mustEqual 0
            unk3 mustEqual true
            unk4 mustEqual true
            list.size mustEqual 0
          case default =>
            ko
        }
      }

      "encode (one friend)" in {
        val msg = FriendsResponse(3, 0, true, true, Friend("KurtHectic-G", false) :: Nil)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual stringOneFriend
      }

      "encode (multiple friends)" in {
        val msg = FriendsResponse(0, 0, true, true, Friend("Angello-W", false) ::
          Friend("thephattphrogg", false) ::
          Friend("Kimpossible12", false) ::
          Friend("Zearthling", false) ::
          Friend("KurtHectic-G", false) :: Nil)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual stringManyFriends
      }

      "encode (short)" in {
        val msg = FriendsResponse(4, 0, true, true)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual stringShort
      }
    }

    "FriendsRequest" should {
      val string = hex"72 3 0A0 46004A0048004E004300"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case FriendsRequest(action, friend) =>
            action mustEqual 1
            friend.length mustEqual 5
            friend mustEqual "FJHNC"
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = FriendsRequest(1, "FJHNC")
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "TrainingZoneMessage" should {
      val string = hex"75 13 000000"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case TrainingZoneMessage(zone, unk) =>
            zone mustEqual PlanetSideGUID(19)
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = TrainingZoneMessage(PlanetSideGUID(19))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "WeaponDryFireMessage" should {
      val string = hex"52 4C00"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case WeaponDryFireMessage(weapon_guid) =>
            weapon_guid mustEqual PlanetSideGUID(76)
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = WeaponDryFireMessage(PlanetSideGUID(76))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "FavoritesMessage" should {
      val stringVehicles = hex"60	5C 84	02 20 5300 6B00 7900 6700 7500 6100 7200 6400"
      val stringInfantry = hex"60 2C 03 82 34 4100 6700 6900 6C00 6500 2000 2800 6200 6100 7300 6900 6300 2900 20"

      "decode (for infantry)" in {
        PacketCoding.DecodePacket(stringInfantry).require match {
          case FavoritesMessage(list, player_guid, line, label, armor) =>
            list mustEqual 0
            player_guid mustEqual PlanetSideGUID(3760)
            line mustEqual 0
            label mustEqual "Agile (basic)"
            armor.isDefined mustEqual true
            armor.get mustEqual 1
          case default =>
            ko
        }
      }

      "encode (for infantry)" in {
        val msg = FavoritesMessage(0, PlanetSideGUID(3760), 0, "Agile (basic)", Option(1))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual stringInfantry
      }

      "decode (for vehicles)" in {
        PacketCoding.DecodePacket(stringVehicles).require match {
          case FavoritesMessage(list, player_guid, line, label, armor) =>
            list mustEqual 1
            player_guid mustEqual PlanetSideGUID(4210)
            line mustEqual 0
            label mustEqual "Skyguard"
            armor.isDefined mustEqual false
          case default =>
            ko
        }
      }

      "encode (for vehicles)" in {
        val msg = FavoritesMessage(1, PlanetSideGUID(4210), 0, "Skyguard")
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual stringVehicles
      }
    }

    "WeaponJammedMessage" should {
      val string = hex"66 4C00"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case WeaponJammedMessage(weapon_guid) =>
            weapon_guid mustEqual PlanetSideGUID(76)
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = WeaponJammedMessage(PlanetSideGUID(76))
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "SetEmpireMessage" should {
      val string = hex"24 02 00 80"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case SetEmpireMessage(object_guid, empire) =>
            object_guid mustEqual PlanetSideGUID(2)
            empire mustEqual PlanetSideEmpire.VS
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = SetEmpireMessage(PlanetSideGUID(2), PlanetSideEmpire.VS)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "LoadMapMessage" should {
      val string = hex"31 85 6D61703130 83 7A3130 0FA0 19000000 F6 F1 60 86 80"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case LoadMapMessage(map_name, nav_map_name, unk1, unk2, weapons_unlocked, unk3) =>
            map_name mustEqual "map10"
            nav_map_name mustEqual "z10"
            unk1 mustEqual 40975
            unk2 mustEqual 25
            weapons_unlocked mustEqual true
            unk3 mustEqual 230810349
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = LoadMapMessage("map10","z10",40975,25,true,230810349)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
        pkt mustEqual string
      }
    }

    "HotSpotInfo" should {
      val string = hex"00 D0 70 08 CA 80 00 00" // note: completing that last byte is required to avoid it being placed at the start of the vector
      "decode" in {
        HotSpotInfo.codec.decode(string.toBitVector) match {
          case Attempt.Successful(decoded) =>
            decoded.value.x mustEqual 4000.0f
            decoded.value.y mustEqual 5400.0f
            decoded.value.scale mustEqual 64.0f
          case _ =>
            ko
        }
      }

      "encode" in {
        val msg = HotSpotInfo(4000.0f, 5400.0f, 64.0f)
        val pkt = HotSpotInfo.codec.encode(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "HotSpotUpdateMessage" should {
      val stringClear = hex"9F 0500 1 00 0"
      val stringOne = hex"9F 0500 1 01 0 00 2E9 00 145 80000 0"
      val stringTwo = hex"9F 0500 5 02 0 00 D07 00 8CA 80000 00 BEA 00 4C4 80000"

      "decode (clear)" in {
        PacketCoding.DecodePacket(stringClear).require match {
          case HotSpotUpdateMessage(continent_guid, unk, spots) =>
            continent_guid mustEqual PlanetSideGUID(5)
            unk mustEqual 1
            spots.size mustEqual 0
          case _ =>
            ko
        }
      }

      "decode (one)" in {
        PacketCoding.DecodePacket(stringOne).require match {
          case HotSpotUpdateMessage(continent_guid, unk, spots) =>
            continent_guid mustEqual PlanetSideGUID(5)
            unk mustEqual 1
            spots.size mustEqual 1
            spots.head.x mustEqual 4700.0f
            spots.head.y mustEqual 2600.0f
            spots.head.scale mustEqual 64.0f
          case _ =>
            ko
        }
      }

      "decode (two)" in {
        PacketCoding.DecodePacket(stringTwo).require match {
          case HotSpotUpdateMessage(continent_guid, unk, spots) =>
            continent_guid mustEqual PlanetSideGUID(5)
            unk mustEqual 5
            spots.size mustEqual 2
            spots.head.x mustEqual 4000.0f
            spots.head.y mustEqual 5400.0f
            spots.head.scale mustEqual 64.0f
            spots(1).x mustEqual 5500.0f
            spots(1).y mustEqual 2200.0f
            spots(1).scale mustEqual 64.0f
          case _ =>
            ko
        }
      }

      "encode (clear)" in {
        val msg = HotSpotUpdateMessage(PlanetSideGUID(5),1)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
        pkt mustEqual stringClear
      }

      "encode (one)" in {
        val msg = HotSpotUpdateMessage(PlanetSideGUID(5),1, HotSpotInfo(4700.0f, 2600.0f, 64.0f)::Nil)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
        pkt mustEqual stringOne
      }

      "encode (two)" in {
        val msg = HotSpotUpdateMessage(PlanetSideGUID(5),5, HotSpotInfo(4000.0f, 5400.0f, 64.0f)::HotSpotInfo(5500.0f, 2200.0f, 64.0f)::Nil)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
        pkt mustEqual stringTwo
      }
    }

    "BuildingInfoUpdateMessage" should {
      val string = hex"a0 04 00 09 00 16 00 00 00 00 80 00 00 00 17 00  00 00 00 00 00 40"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case BuildingInfoUpdateMessage(continent_guid : PlanetSideGUID,
                                         building_guid : PlanetSideGUID,
                                         ntu_level : Int,
                                         is_hacked : Boolean,
                                         empire_hack : PlanetSideEmpire.Value,
                                         hack_time_remaining : Long,
                                         empire_own : PlanetSideEmpire.Value,
                                         unk1 : Long,
                                         unk1x : Option[Additional1],
                                         generator_state : PlanetSideGeneratorState.Value,
                                         spawn_tubes_normal : Boolean,
                                         force_dome_active : Boolean,
                                         lattice_benefit : Int,
                                         unk3 : Int,
                                         unk4 : List[Additional2],
                                         unk5 : Long,
                                         unk6 : Boolean,
                                         unk7 : Int,
                                         unk7x : Option[Additional3],
                                         boost_spawn_pain : Boolean,
                                         boost_generator_pain : Boolean) =>
            continent_guid mustEqual PlanetSideGUID(4)
            building_guid mustEqual PlanetSideGUID(9)
            ntu_level mustEqual 1
            is_hacked mustEqual false
            empire_hack mustEqual PlanetSideEmpire.NEUTRAL
            hack_time_remaining mustEqual 0
            empire_own mustEqual PlanetSideEmpire.NC
            unk1 mustEqual 0
            unk1x mustEqual None
            generator_state mustEqual PlanetSideGeneratorState.Normal
            spawn_tubes_normal mustEqual true
            force_dome_active mustEqual false
            lattice_benefit mustEqual 28
            unk3 mustEqual 0
            unk4.size mustEqual 0
            unk4.isEmpty mustEqual true
            unk5 mustEqual 0
            unk6 mustEqual false
            unk7 mustEqual 8
            unk7x mustEqual None
            boost_spawn_pain mustEqual false
            boost_generator_pain mustEqual false
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = BuildingInfoUpdateMessage(PlanetSideGUID(4),
                                            PlanetSideGUID(9),
                                            1,
                                            false,
                                            PlanetSideEmpire.NEUTRAL,
                                            0,
                                            PlanetSideEmpire.NC,
                                            0,
                                            None,
                                            PlanetSideGeneratorState.Normal,
                                            true,
                                            false,
                                            28,
                                            0,
                                            Nil,
                                            0,
                                            false,
                                            8,
                                            None,
                                            false,
                                            false)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "QuantityUpdateMessage" should {
      val string = hex"3D 5300 7B000000"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case QuantityUpdateMessage(item_guid, quantity) =>
            item_guid mustEqual PlanetSideGUID(83)
            quantity mustEqual 123
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = QuantityUpdateMessage(PlanetSideGUID(83), 123)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ArmorChangedMessage" should {
      val string = hex"3E 11 01 4C"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ArmorChangedMessage(player_guid, armor, subtype) =>
            player_guid mustEqual PlanetSideGUID(273)
            armor mustEqual 2
            subtype mustEqual 3
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = ArmorChangedMessage(PlanetSideGUID(273), 2, 3)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "QuantityDeltaUpdateMessage" should {
      val string = hex"C4 5300 FBFFFFFF"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case QuantityDeltaUpdateMessage(item_guid, quantity) =>
            item_guid mustEqual PlanetSideGUID(83)
            quantity mustEqual -5
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = QuantityDeltaUpdateMessage(PlanetSideGUID(83), -5)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "ZoneInfoMessage" should {
      val string = hex"C6 0C 00 80 00 00 00 00"
      val string_cavern = hex"C6 1B 00 1D F9 F3 00 00"

      "decode (normal)" in {
        PacketCoding.DecodePacket(string).require match {
          case ZoneInfoMessage(zone, empire_status, unk) =>
            zone mustEqual 12
            empire_status mustEqual true
            unk mustEqual 0
          case default =>
            ko
        }
      }

      "decode (cavern)" in {
        PacketCoding.DecodePacket(string_cavern).require match {
          case ZoneInfoMessage(zone, empire_status, unk) =>
            zone mustEqual 27
            empire_status mustEqual false
            unk mustEqual 15135547
          case default =>
            ko
        }
      }

      "encode (normal)" in {
        val msg = ZoneInfoMessage(12, true, 0)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }

      "encode (cavern)" in {
        val msg = ZoneInfoMessage(27, false, 15135547)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string_cavern
      }
    }

    "PingMsg" should  {
      val packet = hex"1a 00000000 b0360000"

      "decode" in {
        PacketCoding.DecodePacket(packet).require match {
          case PingMsg(unk1, unk2) =>
            unk1 === 0
            unk2 === 14000
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = PingMsg(0, 14000)
        PacketCoding.EncodePacket(msg).require.toByteVector === packet
      }
    }
  }

  "SquadDefinitionActionMessage" should {
    //local test data; note that the second field - unk1 - is always blank for now, but that probably changes
    val string_03 = hex"E7 0c 0000c0" //index: 3
    val string_08 = hex"E7 20 000000"
    val string_10 = hex"E7 28 000004" //index: 1
    val string_19 = hex"E7 4c 0000218041002d005400650061006d00" //"A-Team"
    val string_20 = hex"E7 50 0000004000"
    val string_21 = hex"E7 54 000008" //index: 2
    val string_22 = hex"E7 58 000008" //index: 2
    val string_23 = hex"E7 5c 0000061842004c00550046004f005200" //"BLUFOR", index: 1
    val string_24 = hex"E7 60 000006386b0069006c006c002000620061006400200064007500640065007300" //"kill bad dudes", index: 1
    val string_25 = hex"E7 64 000004400000800000" //"Anti-Vehicular" (former), "Infiltration Suit" (latter), index: 1
    val string_26 = hex"E7 68 000000"
    val string_28 = hex"E7 70 000020" //On
    val string_31 = hex"E7 7c 000020" //On
    val string_34a = hex"E7 88 00002180420061006400610073007300000000000000040000" //"Badass", Solsar, Any matching position
    val string_34b = hex"E7 88 00002180420061006400610073007300000000000000080000" //"Badass", Hossin, Any matching position
    val string_34c = hex"E7 88 00002180420061006400610073007300000000000000080080" //"Badass", Hossin, Any position
    val string_34d = hex"E7 88 00002180420061006400610073007300100000200000080100" //"Badass", Hossin, Some("Anti-Vehicular", "Infiltration Suit")
    val string_34e = hex"E7 88 00002180420061006400610073007300100000200000080180" //"Badass", Hossin, All("Anti-Vehicular", "Infiltration Suit")
    val string_35 = hex"E7 8c 000000"
    val string_40 = hex"E7 a0 000004" //index: 1
    val string_41 = hex"E7 a4 000000"

    "decode (03)" in {
      PacketCoding.DecodePacket(string_03).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 3
          unk1 mustEqual 0
          unk2 mustEqual 3
          str.isDefined mustEqual false
          int1.isDefined mustEqual false
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (08)" in {
      PacketCoding.DecodePacket(string_08).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 8
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual false
          int1.isDefined mustEqual false
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (10)" in {
      PacketCoding.DecodePacket(string_10).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 10
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual false
          int1.isDefined mustEqual true
          int1.get mustEqual 1
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (19)" in {
      PacketCoding.DecodePacket(string_19).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 19
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual true
          str.get mustEqual "A-Team"
          int1.isDefined mustEqual false
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (20)" in {
      PacketCoding.DecodePacket(string_20).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 20
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual false
          int1.isDefined mustEqual true
          int1.get mustEqual 1
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (21)" in {
      PacketCoding.DecodePacket(string_21).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 21
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual false
          int1.isDefined mustEqual true
          int1.get mustEqual 2
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (22)" in {
      PacketCoding.DecodePacket(string_22).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 22
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual false
          int1.isDefined mustEqual true
          int1.get mustEqual 2
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (23)" in {
      PacketCoding.DecodePacket(string_23).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 23
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual true
          str.get mustEqual "BLUFOR"
          int1.isDefined mustEqual true
          int1.get mustEqual 1
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (24)" in {
      PacketCoding.DecodePacket(string_24).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 24
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual true
          str.get mustEqual "kill bad dudes"
          int1.isDefined mustEqual true
          int1.get mustEqual 1
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (25)" in {
      PacketCoding.DecodePacket(string_25).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 25
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual false
          int1.isDefined mustEqual true
          int1.get mustEqual 1
          int2.isDefined mustEqual false
          long1.isDefined mustEqual true
          long1.get mustEqual 536870928L
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (26)" in {
      PacketCoding.DecodePacket(string_26).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 26
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual false
          int1.isDefined mustEqual false
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (28)" in {
      PacketCoding.DecodePacket(string_28).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 28
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual false
          int1.isDefined mustEqual false
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual true
          bool.get mustEqual true
        case default =>
          ko
      }
    }

    "decode (31)" in {
      PacketCoding.DecodePacket(string_31).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 31
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual false
          int1.isDefined mustEqual false
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual true
          bool.get mustEqual true
        case default =>
          ko
      }
    }

    "decode (34a)" in {
      PacketCoding.DecodePacket(string_34a).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 34
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual true
          str.get mustEqual "Badass"
          int1.isDefined mustEqual true
          int1.get mustEqual 1
          int2.isDefined mustEqual true
          int2.get mustEqual 0
          long1.isDefined mustEqual true
          long1.get mustEqual 0
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (34b)" in {
      PacketCoding.DecodePacket(string_34b).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 34
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual true
          str.get mustEqual "Badass"
          int1.isDefined mustEqual true
          int1.get mustEqual 2
          int2.isDefined mustEqual true
          int2.get mustEqual 0
          long1.isDefined mustEqual true
          long1.get mustEqual 0
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (34c)" in {
      PacketCoding.DecodePacket(string_34c).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 34
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual true
          str.get mustEqual "Badass"
          int1.isDefined mustEqual true
          int1.get mustEqual 2
          int2.isDefined mustEqual true
          int2.get mustEqual 1
          long1.isDefined mustEqual true
          long1.get mustEqual 0
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (34d)" in {
      PacketCoding.DecodePacket(string_34d).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 34
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual true
          str.get mustEqual "Badass"
          int1.isDefined mustEqual true
          int1.get mustEqual 2
          int2.isDefined mustEqual true
          int2.get mustEqual 2
          long1.isDefined mustEqual true
          long1.get mustEqual 536870928L
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (34e)" in {
      PacketCoding.DecodePacket(string_34e).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 34
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual true
          str.get mustEqual "Badass"
          int1.isDefined mustEqual true
          int1.get mustEqual 2
          int2.isDefined mustEqual true
          int2.get mustEqual 3
          long1.isDefined mustEqual true
          long1.get mustEqual 536870928L
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (35)" in {
      PacketCoding.DecodePacket(string_35).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 35
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual false
          int1.isDefined mustEqual false
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (40)" in {
      PacketCoding.DecodePacket(string_40).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 40
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual false
          int1.isDefined mustEqual true
          int1.get mustEqual 1
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "decode (41)" in {
      PacketCoding.DecodePacket(string_41).require match {
        case SquadDefinitionActionMessage(action, unk1, unk2, str, int1, int2, long1, long2, bool) =>
          action mustEqual 41
          unk1 mustEqual 0
          unk2 mustEqual 0
          str.isDefined mustEqual false
          int1.isDefined mustEqual false
          int2.isDefined mustEqual false
          long1.isDefined mustEqual false
          long2.isDefined mustEqual false
          bool.isDefined mustEqual false
        case default =>
          ko
      }
    }

    "encode (03)" in {
      val msg = SquadDefinitionActionMessage(3, 0, 3, None, None, None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_03
    }

    "encode (08)" in {
      val msg = SquadDefinitionActionMessage(8, 0, 0, None, None, None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_08
    }

    "encode (10)" in {
      val msg = SquadDefinitionActionMessage(10, 0, 0, None, Some(1), None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_10
    }

    "encode (19)" in {
      val msg = SquadDefinitionActionMessage(19, 0, 0, Some("A-Team"), None, None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_19
    }

    "encode (20)" in {
      val msg = SquadDefinitionActionMessage(20, 0, 0, None, Some(1), None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_20
    }

    "encode (21)" in {
      val msg = SquadDefinitionActionMessage(21, 0, 0, None, Some(2), None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_21
    }

    "encode (22)" in {
      val msg = SquadDefinitionActionMessage(22, 0, 0, None, Some(2), None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_22
    }

    "encode (23)" in {
      val msg = SquadDefinitionActionMessage(23, 0, 0, Some("BLUFOR"), Some(1), None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_23
    }

    "encode (24)" in {
      val msg = SquadDefinitionActionMessage(24, 0, 0, Some("kill bad dudes"), Some(1), None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_24
    }

    "encode (25)" in {
      val msg = SquadDefinitionActionMessage(25, 0, 0, None, Some(1), None, Some(536870928L), None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_25
    }

    "encode (26)" in {
      val msg = SquadDefinitionActionMessage(26, 0, 0, None, None, None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_26
    }

    "encode (28)" in {
      val msg = SquadDefinitionActionMessage(28, 0, 0, None, None, None, None, None, Some(true))
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_28
    }

    "encode (31)" in {
      val msg = SquadDefinitionActionMessage(31, 0, 0, None, None, None, None, None, Some(true))
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_31
    }

    "encode (34a)" in {
      val msg = SquadDefinitionActionMessage(34, 0, 0, Some("Badass"), Some(1), Some(0), Some(0L), None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_34a
    }

    "encode (34b)" in {
      val msg = SquadDefinitionActionMessage(34, 0, 0, Some("Badass"), Some(2), Some(0), Some(0L), None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_34b
    }

    "encode (34c)" in {
      val msg = SquadDefinitionActionMessage(34, 0, 0, Some("Badass"), Some(2), Some(1), Some(0L), None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_34c
    }

    "encode (34d)" in {
      val msg = SquadDefinitionActionMessage(34, 0, 0, Some("Badass"), Some(2), Some(2), Some(536870928L), None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_34d
    }

    "encode (34e)" in {
      val msg = SquadDefinitionActionMessage(34, 0, 0, Some("Badass"), Some(2), Some(3), Some(536870928L), None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_34e
    }

    "encode (35)" in {
      val msg = SquadDefinitionActionMessage(35, 0, 0, None, None, None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_35
    }

    "encode (40)" in {
      val msg = SquadDefinitionActionMessage(40, 0, 0, None, Some(1), None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_40
    }

    "encode (41)" in {
      val msg = SquadDefinitionActionMessage(41, 0, 0, None, None, None, None, None, None)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt mustEqual string_41
    }
  }
}
