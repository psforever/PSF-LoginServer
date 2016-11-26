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
      val packet2 = hex"18 F8 00 00 00 BC 8C 10 90 3B 45 C6 FA 94 00 9F F0 00 00 40 00 08 C0 44 00 69 00 66 00 66 00 45" //faked data?
      val packet2Rest = packet2.bits.drop(8 + 32 + 1 + 11 + 16)

      "decode (2)" in {
        PacketCoding.DecodePacket(packet2).require match {
          case obj @ ObjectCreateMessage(len, cls, guid, parent, rest) =>
            len mustEqual 248
            cls mustEqual 121
            guid mustEqual PlanetSideGUID(2497)
            parent mustEqual None
            rest mustEqual packet2Rest
          case default =>
            ko
        }
      }

      "encode (2)" in {
        val msg = ObjectCreateMessage(0, 121, PlanetSideGUID(2497), None, packet2Rest)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual packet2
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
          case TimeOfDayMessage(unk1, time, unk2, unk3, unk4, unk5) =>
            unk1 mustEqual 0
            time mustEqual 4653056
            unk2 mustEqual 0
            unk3 mustEqual 0
            unk4 mustEqual 32
            unk5 mustEqual 65
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = TimeOfDayMessage(0, 4653056, 0, 0, 32, 65)
        val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

        pkt mustEqual string
      }
    }

    "PlayerStateMessageUpstream" should {
      val string = hex"BD 4B000 E377BA575B616C640A70004014060110007000000"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case PlayerStateMessageUpstream(avatar_guid, pos, vel, unk1, aim_pitch, unk2, seq_time, unk3, is_crouching, unk4, unk5, unk6, unk7, unk8) =>
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
            unk6 mustEqual false
            unk7 mustEqual 112
            unk8 mustEqual 0
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

    "BroadcastWarpgateUpdateMessage" should {
      val string = hex"D9 0D 00 01 00 20"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case BroadcastWarpgateUpdateMessage(continent_guid, building_guid, state) =>
            continent_guid mustEqual PlanetSideGUID(13)
            building_guid mustEqual PlanetSideGUID(1)
            state mustEqual 32
          case default =>
            ko
        }
      }

      "encode" in {
        val msg = BroadcastWarpgateUpdateMessage(PlanetSideGUID(13), PlanetSideGUID(1), 32)
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
                                         generator_state : PlanetSideGeneratorState.Value,
                                         spawn_tubes_normal : Boolean,
                                         force_dome_active : Boolean,
                                         lattice_benefit : Int,
                                         unk3 : Int,
                                         unk4 : Int,
                                         unk5 : Long,
                                         unk6 : Boolean,
                                         unk7 : Int,
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
            generator_state mustEqual PlanetSideGeneratorState.Normal
            spawn_tubes_normal mustEqual true
            force_dome_active mustEqual false
            lattice_benefit mustEqual 28
            unk3 mustEqual 0
            unk4 mustEqual 0
            unk5 mustEqual 0
            unk6 mustEqual false
            unk7 mustEqual 8
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
                                            PlanetSideGeneratorState.Normal,
                                            true,
                                            false,
                                            28,
                                            0,
                                            0,
                                            0,
                                            false,
                                            8,
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
}
