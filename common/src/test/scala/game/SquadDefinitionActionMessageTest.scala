// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class SquadDefinitionActionMessageTest extends Specification {
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
      case _ =>
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
