// Copyright (c) 2016 PSForever.net to present

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.control._
import scodec.Attempt.Successful
import scodec.bits._
import scodec.codecs.uint16

class ControlPacketTest extends Specification {

  "PlanetSide control packet" in {
    "ControlSync" should {
      val string = hex"0007 5268 0000004D 00000052 0000004D 0000007C 0000004D 0000000000000276 0000000000000275"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ControlSync(a, b, c, d, e, f, g, h) =>
            a mustEqual 21096

            b mustEqual 0x4d
            c mustEqual 0x52
            d mustEqual 0x4d
            e mustEqual 0x7c
            f mustEqual 0x4d

            g mustEqual 0x276
            h mustEqual 0x275
          case default =>
            true mustEqual false
        }
      }

      "encode" in {
        val encoded = PacketCoding.EncodePacket(ControlSync(21096, 0x4d, 0x52, 0x4d, 0x7c, 0x4d, 0x276, 0x275)).require

        encoded.toByteVector mustEqual string
      }
    }

    "ControlSyncResp" should {
      val string = hex"0008 5268 21392D92 0000000000000276 0000000000000275 0000000000000275 0000000000000276"

      "decode" in {
        PacketCoding.DecodePacket(string).require match {
          case ControlSyncResp(a, b, c, d, e, f) =>
            a mustEqual 21096

            b mustEqual 0x21392D92
            c mustEqual 0x276
            d mustEqual 0x275
            e mustEqual 0x275
            f mustEqual 0x276
          case default =>
            true mustEqual false
        }
      }

      "encode" in {
        val encoded = PacketCoding.EncodePacket(ControlSyncResp(21096, 0x21392D92, 0x276, 0x275, 0x275, 0x276)).require

        encoded.toByteVector mustEqual string
      }
    }

    "SlottedMetaPacket" should {
      val string = hex"00 09 00 00 00194302484C36563130433F" ++
        hex"4C6835316369774A0000000018FABE0C" ++
        hex"00000000000000000000000001000000" ++
        hex"020000006B7BD8288C6469666671756F" ++
        hex"7469656E740000000000440597570065" ++
        hex"006C0063006F006D006500200074006F" ++
        hex"00200050006C0061006E006500740053" ++
        hex"0069006400650021002000018667656D" ++
        hex"696E690100040001459E2540377540"

      def createMetaPacket(slot : Int, subslot : Int, rest : ByteVector) = hex"00" ++
        ControlPacketOpcode.codec.encode(
          ControlPacketOpcode(ControlPacketOpcode.SlottedMetaPacket0.id + slot)
        ).require.toByteVector ++ uint16.encode(subslot).require.toByteVector ++ rest

      "decode as the base slot and subslot" in {
        PacketCoding.DecodePacket(string).require match {
          case SlottedMetaPacket(slot, subslot, rest) =>
            slot mustEqual 0
            subslot mustEqual 0
            rest mustEqual string.drop(4)
          case default =>
            true mustEqual false
        }
      }

      "decode as an arbitrary slot and subslot" in {
        val maxSlots = ControlPacketOpcode.SlottedMetaPacket7.id - ControlPacketOpcode.SlottedMetaPacket0.id

        // create all possible SlottedMetaPackets
        for(i <- 0 until maxSlots) {
          val subslot = 12323
          val pkt = createMetaPacket(i, subslot, ByteVector.empty)

          PacketCoding.DecodePacket(pkt).require match {
            case SlottedMetaPacket(slot, subslotDecoded, rest) =>

              // XXX: there isn't a simple solution to Slot0 and Slot4 be aliases of each other structurally
              // This is probably best left to higher layers
              //slot mustEqual i % 4 // this is seen at 0x00A3FBFA
              slot mustEqual i
              subslotDecoded mustEqual subslot
              rest mustEqual ByteVector.empty // empty in this case
            case default =>
              true mustEqual false
          }
        }

        true
      }

      "encode" in {
        val encoded = PacketCoding.EncodePacket(SlottedMetaPacket(0, 0x1000, ByteVector.empty)).require
        val encoded2 = PacketCoding.EncodePacket(SlottedMetaPacket(3, 0xffff, hex"414243")).require
        val encoded3 = PacketCoding.EncodePacket(SlottedMetaPacket(7, 0, hex"00")).require

        encoded.toByteVector mustEqual createMetaPacket(0, 0x1000, ByteVector.empty)
        encoded2.toByteVector mustEqual createMetaPacket(3, 0xffff, hex"414243")
        encoded3.toByteVector mustEqual createMetaPacket(7, 0, hex"00")

        PacketCoding.EncodePacket(SlottedMetaPacket(8, 0, hex"00")).require must throwA[AssertionError]
        PacketCoding.EncodePacket(SlottedMetaPacket(-1, 0, hex"00")).require must throwA[AssertionError]
        PacketCoding.EncodePacket(SlottedMetaPacket(0, 0x10000, hex"00")).require must throwA[IllegalArgumentException]
      }
    }
  }
}