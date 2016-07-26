// Copyright (c) 2016 PSForever.net to present
import java.net.{InetAddress, InetSocketAddress}

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types._
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
      val packet = hex"18 CF 13 00 00 BC 87 00  0A F0 16 C3 43 A1 30 90 00 02 C0 40 00 08 70 43  00 68 00 6F 00 72 00 64 00 54 00 52 00 82 65 1F  F5 9E 80 80 00 00 00 00 00 3F FF C0 00 00 00 20  00 00 00 20 27 03 FF FF FF FF FF FF FF FF FF FF  FF FF FF FF FF FC CC 10 00 03 20 00 00 00 00 00  00 00 00 00 00 00 00 00 00 01 90 01 90 00 00 00  00 01 00 7E C8 00 C8 00 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 01 C0 00 42 C5 46  86 C7 00 00 02 A0 00 00 12 60 78 70 65 5F 77 61  72 70 5F 67 61 74 65 5F 75 73 61 67 65 92 78 70  65 5F 69 6E 73 74 61 6E 74 5F 61 63 74 69 6F 6E  92 78 70 65 5F 73 61 6E 63 74 75 61 72 79 5F 68  65 6C 70 91 78 70 65 5F 62 61 74 74 6C 65 5F 72  61 6E 6B 5F 32 8E 78 70 65 5F 66 6F 72 6D 5F 73  71 75 61 64 8E 78 70 65 5F 74 68 5F 6E 6F 6E 73  61 6E 63 8B 78 70 65 5F 74 68 5F 61 6D 6D 6F 90  78 70 65 5F 74 68 5F 66 69 72 65 6D 6F 64 65 73  8F 75 73 65 64 5F 63 68 61 69 6E 62 6C 61 64 65  9A 76 69 73 69 74 65 64 5F 62 72 6F 61 64 63 61  73 74 5F 77 61 72 70 67 61 74 65 8E 76 69 73 69  74 65 64 5F 6C 6F 63 6B 65 72 8D 75 73 65 64 5F  70 75 6E 69 73 68 65 72 88 75 73 65 64 5F 72 65  6B 8D 75 73 65 64 5F 72 65 70 65 61 74 65 72 9F  76 69 73 69 74 65 64 5F 64 65 63 6F 6E 73 74 72  75 63 74 69 6F 6E 5F 74 65 72 6D 69 6E 61 6C 8F  75 73 65 64 5F 73 75 70 70 72 65 73 73 6F 72 96  76 69 73 69 74 65 64 5F 6F 72 64 65 72 5F 74 65  72 6D 69 6E 61 6C 85 6D 61 70 31 35 85 6D 61 70  31 34 85 6D 61 70 31 32 85 6D 61 70 30 31 00 00  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00 00 00 00 01 0A 36 13 88  04 00 40 00 00 10 00 04 00 00 4D 6E 40 10 41 00  00 00 40 00 18 08 38 1C C0 20 32 00 00 07 80 15  E1 D0 02 10 20 00 00 08 00 03 01 07 13 A8 04 06  40 00 00 10 03 20 BB 00 42 E4 00 00 01 00 0E 07  70 08 6C 80 00 06 40 01 C0 F0 01 13 90 00 00 C8  00 38 1E 40 23 32 00 00 19 00 07 03 D0 05 0E 40  00 03 20 00 E8 7B 00 A4 C8 00 00 64 00 DA 4F 80  14 E1 00 00 00 40 00 18 08 38 1F 40 20 32 00 00  0A 00 08 "
      val packet2 = hex"18 17 74 00 00 BC 8C 10  90 3B 45 C6 FA 94 00 9F F0 00 00 40 00 08 C0 44  00 69 00 66 00 66 00 45"

      "decode" in {
        PacketCoding.DecodePacket(packet2).require match {
          case obj @ ObjectCreateMessage(len, cls, guid, parent) =>
            len === 29719
            cls === 121
            guid === 2497
            parent === None
          case default =>
            ko
        }
      }

      "encode" in {
        ok
      }
    }
    
    "ChatMsg" should {
      val string_local = hex"12 1A C000 83610062006300"
      val string_tell  = hex"12 20 C180640065006600 83610062006300"
      
      "decode" in {
        PacketCoding.DecodePacket(string_local).require match {
          case ChatMsg(messagetype, unk1, recipient, contents) =>
            messagetype mustEqual ChatMessageType.Local
            unk1 mustEqual true
            recipient mustEqual ""
            contents mustEqual "abc"
          case default =>
            ko
        }
        
        PacketCoding.DecodePacket(string_tell).require match {
          case ChatMsg(messagetype, unk1, recipient, contents) =>
            messagetype mustEqual ChatMessageType.Tell
            unk1 mustEqual true
            recipient mustEqual "def"
            contents mustEqual "abc"
          case default =>
            ko
        }
      }

      "encode" in {
        val msg_local = ChatMsg(ChatMessageType.Local, true, "", "abc")
        val pkt_local = PacketCoding.EncodePacket(msg_local).require.toByteVector

        pkt_local mustEqual string_local
        
        val msg_tell = ChatMsg(ChatMessageType.Tell, true, "def", "abc")
        val pkt_tell = PacketCoding.EncodePacket(msg_tell).require.toByteVector

        pkt_tell mustEqual string_tell
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

    "PlayerStateMessageUpstream" should {
      val string = hex"BD 4B000 E377BA575B616C640A70004014060110007000000"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case PlayerStateMessageUpstream(avatar_guid, pos, vel, unk1, aim_pitch, unk2, unk3, unk4, is_crouching, unk5, unk6, unk7, unk8, unk9) =>
            avatar_guid mustEqual PlanetSideGUID(75)
            pos mustEqual Vector3(3694.1094f, 2735.4531f, 90.84375f)
            vel mustEqual Some(Vector3(4.375f, 2.59375f, 0.0f))
            unk1 mustEqual 10
            aim_pitch mustEqual 3
            unk2 mustEqual 0
            unk3 mustEqual 136
            unk4 mustEqual 0
            is_crouching mustEqual false
            unk5 mustEqual false
            unk6 mustEqual false
            unk7 mustEqual false
            unk8 mustEqual 112
            unk9 mustEqual 0
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
  }
}
