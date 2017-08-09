// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{GamePropertyField, _}
import scodec.bits._

class PropertyOverrideMessageTest extends Specification {
  val string = hex"D5 0B 00 00 00 01 0A E4  0C 02 48 70 75 72 63 68 61 73 65 5F 65 78 65 6D  70 74 5F 76 73 80 92 70 75 72 63 68 61 73 65 5F  65 78 65 6D 70 74 5F 74 72 80 92 70 75 72 63 68  61 73 65 5F 65 78 65 6D 70 74 5F 6E 63 80 11 00  01 14 A4 04 02 1C 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 12 00 01 14 A4 04 02 1C 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 13 00 01 14 A4 04 02 1C  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 14 00 01  14 A4 04 02 1C 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 15 00 01 14 A4 04 02 1C 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 16 00 01 14 A4 04 02 1C 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 1D 00 15 0A  60 04 02 1C 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 54 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 76 00 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 87 00 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 C7 00 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 C8 00 20 10 E0 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 26 20 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 52 20 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 AD  20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 B0 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 B9 20 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 CE 20 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 D6 20 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 2C 40 20 10 E0 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 82 40 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 83 40 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 B9  40 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 CA 40 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 61 60 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 9B 60 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 DA 60 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 1E 00 15 0A 60 04 02  1C 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 54 00  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  76 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 87 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 C7 00 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 C8 00 20 10 E0 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 26 20 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 52 20 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 AD 20 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 B0 20  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  B9 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 CE 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 D6 20 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 2C 40 20 10 E0 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 82 40 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 83 40 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 B9 40 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 CA 40  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  61 60 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 9B 60 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 DA 60 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 1F 00 15 0A 60 04 02 1C 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 54 00 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 76 00 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 87  00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 C7 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 C8 00 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 26 20 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 52 20 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 AD 20 20 10 E0 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 B0 20 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 B9 20 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 CE  20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 D6 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 2C 40 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 82 40 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 83 40 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 B9 40 20 10 E0 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 CA 40 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 61 60 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 9B  60 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 DA 60 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 20 00 15 0A 60 04 02 1C 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 54 00 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 76 00 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 87 00 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 C7 00  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  C8 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 26 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 52 20 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 AD 20 20 10 E0 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 B0 20 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 B9 20 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 CE 20 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 D6 20  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  2C 40 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 82 40 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 83 40 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 B9 40 20 10 E0 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 CA 40 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 61 60 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 9B 60 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 DA 60  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case PropertyOverrideMessage(list) =>
        list.length mustEqual 11
        //
        list.head.unk mustEqual 0
        list.head.list.length mustEqual 1
        list.head.list.head.unk mustEqual 343
        list.head.list.head.list.length mustEqual 3
        list.head.list.head.list.head.field1 mustEqual "purchase_exempt_vs"
        list.head.list.head.list.head.field2 mustEqual ""
        list.head.list.head.list(1).field1 mustEqual "purchase_exempt_tr"
        list.head.list.head.list(1).field2 mustEqual ""
        list.head.list.head.list(2).field1 mustEqual "purchase_exempt_nc"
        list.head.list.head.list(2).field2 mustEqual ""
        //
        list(1).unk mustEqual 17
        list(1).list.length mustEqual 1
        list(1).list.head.unk mustEqual 421
        list(1).list.head.list.length mustEqual 1
        list(1).list.head.list.head.field1 mustEqual "allowed"
        list(1).list.head.list.head.field2 mustEqual "false"
        //
        list(2).unk mustEqual 18
        list(2).list.length mustEqual 1
        list(2).list.head.unk mustEqual 421
        list(2).list.head.list.length mustEqual 1
        list(2).list.head.list.head.field1 mustEqual "allowed"
        list(2).list.head.list.head.field2 mustEqual "false"
        //
        list(3).unk mustEqual 19
        list(3).list.length mustEqual 1
        list(3).list.head.unk mustEqual 421
        list(3).list.head.list.length mustEqual 1
        list(3).list.head.list.head.field1 mustEqual "allowed"
        list(3).list.head.list.head.field2 mustEqual "false"
        //
        list(4).unk mustEqual 20
        list(4).list.length mustEqual 1
        list(4).list.head.unk mustEqual 421
        list(4).list.head.list.length mustEqual 1
        list(4).list.head.list.head.field1 mustEqual "allowed"
        list(4).list.head.list.head.field2 mustEqual "false"
        //
        list(5).unk mustEqual 21
        list(5).list.length mustEqual 1
        list(5).list.head.unk mustEqual 421
        list(5).list.head.list.length mustEqual 1
        list(5).list.head.list.head.field1 mustEqual "allowed"
        list(5).list.head.list.head.field2 mustEqual "false"
        //
        list(6).unk mustEqual 22
        list(6).list.length mustEqual 1
        list(6).list.head.unk mustEqual 421
        list(6).list.head.list.length mustEqual 1
        list(6).list.head.list.head.field1 mustEqual "allowed"
        list(6).list.head.list.head.field2 mustEqual "false"
        //
        list(7).unk mustEqual 29
        list(7).list.length mustEqual 21
        list(7).list.head.unk mustEqual 83
        list(7).list.head.list.length mustEqual 1
        list(7).list.head.list.head.field1 mustEqual "allowed"
        list(7).list.head.list.head.field2 mustEqual "false"
        list(7).list(1).unk mustEqual 84
        list(7).list(1).list.length mustEqual 1
        list(7).list(1).list.head.field1 mustEqual "allowed"
        list(7).list(1).list.head.field2 mustEqual "false"
        list(7).list(2).unk mustEqual 118
        list(7).list(2).list.length mustEqual 1
        list(7).list(2).list.head.field1 mustEqual "allowed"
        list(7).list(2).list.head.field2 mustEqual "false"
        list(7).list(3).unk mustEqual 135
        list(7).list(3).list.length mustEqual 1
        list(7).list(3).list.head.field1 mustEqual "allowed"
        list(7).list(3).list.head.field2 mustEqual "false"
        list(7).list(4).unk mustEqual 199
        list(7).list(4).list.length mustEqual 1
        list(7).list(4).list.head.field1 mustEqual "allowed"
        list(7).list(4).list.head.field2 mustEqual "false"
        list(7).list(5).unk mustEqual 200
        list(7).list(5).list.length mustEqual 1
        list(7).list(5).list.head.field1 mustEqual "allowed"
        list(7).list(5).list.head.field2 mustEqual "false"
        list(7).list(6).unk mustEqual 294
        list(7).list(6).list.length mustEqual 1
        list(7).list(6).list.head.field1 mustEqual "allowed"
        list(7).list(6).list.head.field2 mustEqual "false"
        list(7).list(7).unk mustEqual 338
        list(7).list(7).list.length mustEqual 1
        list(7).list(7).list.head.field1 mustEqual "allowed"
        list(7).list(7).list.head.field2 mustEqual "false"
        list(7).list(8).unk mustEqual 429
        list(7).list(8).list.length mustEqual 1
        list(7).list(8).list.head.field1 mustEqual "allowed"
        list(7).list(8).list.head.field2 mustEqual "false"
        list(7).list(9).unk mustEqual 432
        list(7).list(9).list.length mustEqual 1
        list(7).list(9).list.head.field1 mustEqual "allowed"
        list(7).list(9).list.head.field2 mustEqual "false"
        list(7).list(10).unk mustEqual 441
        list(7).list(10).list.length mustEqual 1
        list(7).list(10).list.head.field1 mustEqual "allowed"
        list(7).list(10).list.head.field2 mustEqual "false"
        list(7).list(11).unk mustEqual 462
        list(7).list(11).list.length mustEqual 1
        list(7).list(11).list.head.field1 mustEqual "allowed"
        list(7).list(11).list.head.field2 mustEqual "false"
        list(7).list(12).unk mustEqual 470
        list(7).list(12).list.length mustEqual 1
        list(7).list(12).list.head.field1 mustEqual "allowed"
        list(7).list(12).list.head.field2 mustEqual "false"
        list(7).list(13).unk mustEqual 556
        list(7).list(13).list.length mustEqual 1
        list(7).list(13).list.head.field1 mustEqual "allowed"
        list(7).list(13).list.head.field2 mustEqual "false"
        list(7).list(14).unk mustEqual 642
        list(7).list(14).list.length mustEqual 1
        list(7).list(14).list.head.field1 mustEqual "allowed"
        list(7).list(14).list.head.field2 mustEqual "false"
        list(7).list(15).unk mustEqual 643
        list(7).list(15).list.length mustEqual 1
        list(7).list(15).list.head.field1 mustEqual "allowed"
        list(7).list(15).list.head.field2 mustEqual "false"
        list(7).list(16).unk mustEqual 697
        list(7).list(16).list.length mustEqual 1
        list(7).list(16).list.head.field1 mustEqual "allowed"
        list(7).list(16).list.head.field2 mustEqual "false"
        list(7).list(17).unk mustEqual 714
        list(7).list(17).list.length mustEqual 1
        list(7).list(17).list.head.field1 mustEqual "allowed"
        list(7).list(17).list.head.field2 mustEqual "false"
        list(7).list(18).unk mustEqual 865
        list(7).list(18).list.length mustEqual 1
        list(7).list(18).list.head.field1 mustEqual "allowed"
        list(7).list(18).list.head.field2 mustEqual "false"
        list(7).list(19).unk mustEqual 923
        list(7).list(19).list.length mustEqual 1
        list(7).list(19).list.head.field1 mustEqual "allowed"
        list(7).list(19).list.head.field2 mustEqual "false"
        list(7).list(20).unk mustEqual 986
        list(7).list(20).list.length mustEqual 1
        list(7).list(20).list.head.field1 mustEqual "allowed"
        list(7).list(20).list.head.field2 mustEqual "false"
        //
        list(8).unk mustEqual 30
        list(8).list.length mustEqual 21
        list(7).list.head.unk mustEqual 83
        list(8).list.head.list.length mustEqual 1
        list(8).list.head.list.head.field1 mustEqual "allowed"
        list(8).list.head.list.head.field2 mustEqual "false"
        list(8).list(1).unk mustEqual 84
        list(8).list(1).list.length mustEqual 1
        list(8).list(1).list.head.field1 mustEqual "allowed"
        list(8).list(1).list.head.field2 mustEqual "false"
        list(8).list(2).unk mustEqual 118
        list(8).list(2).list.length mustEqual 1
        list(8).list(2).list.head.field1 mustEqual "allowed"
        list(8).list(2).list.head.field2 mustEqual "false"
        list(8).list(3).unk mustEqual 135
        list(8).list(3).list.length mustEqual 1
        list(8).list(3).list.head.field1 mustEqual "allowed"
        list(8).list(3).list.head.field2 mustEqual "false"
        list(8).list(4).unk mustEqual 199
        list(8).list(4).list.length mustEqual 1
        list(8).list(4).list.head.field1 mustEqual "allowed"
        list(8).list(4).list.head.field2 mustEqual "false"
        list(8).list(5).unk mustEqual 200
        list(8).list(5).list.length mustEqual 1
        list(8).list(5).list.head.field1 mustEqual "allowed"
        list(8).list(5).list.head.field2 mustEqual "false"
        list(8).list(6).unk mustEqual 294
        list(8).list(6).list.length mustEqual 1
        list(8).list(6).list.head.field1 mustEqual "allowed"
        list(8).list(6).list.head.field2 mustEqual "false"
        list(8).list(7).unk mustEqual 338
        list(8).list(7).list.length mustEqual 1
        list(8).list(7).list.head.field1 mustEqual "allowed"
        list(8).list(7).list.head.field2 mustEqual "false"
        list(8).list(8).unk mustEqual 429
        list(8).list(8).list.length mustEqual 1
        list(8).list(8).list.head.field1 mustEqual "allowed"
        list(8).list(8).list.head.field2 mustEqual "false"
        list(8).list(9).unk mustEqual 432
        list(8).list(9).list.length mustEqual 1
        list(8).list(9).list.head.field1 mustEqual "allowed"
        list(8).list(9).list.head.field2 mustEqual "false"
        list(8).list(10).unk mustEqual 441
        list(8).list(10).list.length mustEqual 1
        list(8).list(10).list.head.field1 mustEqual "allowed"
        list(8).list(10).list.head.field2 mustEqual "false"
        list(8).list(11).unk mustEqual 462
        list(8).list(11).list.length mustEqual 1
        list(8).list(11).list.head.field1 mustEqual "allowed"
        list(8).list(11).list.head.field2 mustEqual "false"
        list(8).list(12).unk mustEqual 470
        list(8).list(12).list.length mustEqual 1
        list(8).list(12).list.head.field1 mustEqual "allowed"
        list(8).list(12).list.head.field2 mustEqual "false"
        list(8).list(13).unk mustEqual 556
        list(8).list(13).list.length mustEqual 1
        list(8).list(13).list.head.field1 mustEqual "allowed"
        list(8).list(13).list.head.field2 mustEqual "false"
        list(8).list(14).unk mustEqual 642
        list(8).list(14).list.length mustEqual 1
        list(8).list(14).list.head.field1 mustEqual "allowed"
        list(8).list(14).list.head.field2 mustEqual "false"
        list(8).list(15).unk mustEqual 643
        list(8).list(15).list.length mustEqual 1
        list(8).list(15).list.head.field1 mustEqual "allowed"
        list(8).list(15).list.head.field2 mustEqual "false"
        list(8).list(16).unk mustEqual 697
        list(8).list(16).list.length mustEqual 1
        list(8).list(16).list.head.field1 mustEqual "allowed"
        list(8).list(16).list.head.field2 mustEqual "false"
        list(8).list(17).unk mustEqual 714
        list(8).list(17).list.length mustEqual 1
        list(8).list(17).list.head.field1 mustEqual "allowed"
        list(8).list(17).list.head.field2 mustEqual "false"
        list(8).list(18).unk mustEqual 865
        list(8).list(18).list.length mustEqual 1
        list(8).list(18).list.head.field1 mustEqual "allowed"
        list(8).list(18).list.head.field2 mustEqual "false"
        list(8).list(19).unk mustEqual 923
        list(8).list(19).list.length mustEqual 1
        list(8).list(19).list.head.field1 mustEqual "allowed"
        list(8).list(19).list.head.field2 mustEqual "false"
        list(8).list(20).unk mustEqual 986
        list(8).list(20).list.length mustEqual 1
        list(8).list(20).list.head.field1 mustEqual "allowed"
        list(8).list(20).list.head.field2 mustEqual "false"
        //
        list(9).unk mustEqual 31
        list(9).list.length mustEqual 21
        list(9).list.head.unk mustEqual 83
        list(9).list.head.list.length mustEqual 1
        list(9).list.head.list.head.field1 mustEqual "allowed"
        list(9).list.head.list.head.field2 mustEqual "false"
        list(9).list(1).unk mustEqual 84
        list(9).list(1).list.length mustEqual 1
        list(9).list(1).list.head.field1 mustEqual "allowed"
        list(9).list(1).list.head.field2 mustEqual "false"
        list(9).list(2).unk mustEqual 118
        list(9).list(2).list.length mustEqual 1
        list(9).list(2).list.head.field1 mustEqual "allowed"
        list(9).list(2).list.head.field2 mustEqual "false"
        list(9).list(3).unk mustEqual 135
        list(9).list(3).list.length mustEqual 1
        list(9).list(3).list.head.field1 mustEqual "allowed"
        list(9).list(3).list.head.field2 mustEqual "false"
        list(9).list(4).unk mustEqual 199
        list(9).list(4).list.length mustEqual 1
        list(9).list(4).list.head.field1 mustEqual "allowed"
        list(9).list(4).list.head.field2 mustEqual "false"
        list(9).list(5).unk mustEqual 200
        list(9).list(5).list.length mustEqual 1
        list(9).list(5).list.head.field1 mustEqual "allowed"
        list(9).list(5).list.head.field2 mustEqual "false"
        list(9).list(6).unk mustEqual 294
        list(9).list(6).list.length mustEqual 1
        list(9).list(6).list.head.field1 mustEqual "allowed"
        list(9).list(6).list.head.field2 mustEqual "false"
        list(9).list(7).unk mustEqual 338
        list(9).list(7).list.length mustEqual 1
        list(9).list(7).list.head.field1 mustEqual "allowed"
        list(9).list(7).list.head.field2 mustEqual "false"
        list(9).list(8).unk mustEqual 429
        list(9).list(8).list.length mustEqual 1
        list(9).list(8).list.head.field1 mustEqual "allowed"
        list(9).list(8).list.head.field2 mustEqual "false"
        list(9).list(9).unk mustEqual 432
        list(9).list(9).list.length mustEqual 1
        list(9).list(9).list.head.field1 mustEqual "allowed"
        list(9).list(9).list.head.field2 mustEqual "false"
        list(9).list(10).unk mustEqual 441
        list(9).list(10).list.length mustEqual 1
        list(9).list(10).list.head.field1 mustEqual "allowed"
        list(9).list(10).list.head.field2 mustEqual "false"
        list(9).list(11).unk mustEqual 462
        list(9).list(11).list.length mustEqual 1
        list(9).list(11).list.head.field1 mustEqual "allowed"
        list(9).list(11).list.head.field2 mustEqual "false"
        list(9).list(12).unk mustEqual 470
        list(9).list(12).list.length mustEqual 1
        list(9).list(12).list.head.field1 mustEqual "allowed"
        list(9).list(12).list.head.field2 mustEqual "false"
        list(9).list(13).unk mustEqual 556
        list(9).list(13).list.length mustEqual 1
        list(9).list(13).list.head.field1 mustEqual "allowed"
        list(9).list(13).list.head.field2 mustEqual "false"
        list(9).list(14).unk mustEqual 642
        list(9).list(14).list.length mustEqual 1
        list(9).list(14).list.head.field1 mustEqual "allowed"
        list(9).list(14).list.head.field2 mustEqual "false"
        list(9).list(15).unk mustEqual 643
        list(9).list(15).list.length mustEqual 1
        list(9).list(15).list.head.field1 mustEqual "allowed"
        list(9).list(15).list.head.field2 mustEqual "false"
        list(9).list(16).unk mustEqual 697
        list(9).list(16).list.length mustEqual 1
        list(9).list(16).list.head.field1 mustEqual "allowed"
        list(9).list(16).list.head.field2 mustEqual "false"
        list(9).list(17).unk mustEqual 714
        list(9).list(17).list.length mustEqual 1
        list(9).list(17).list.head.field1 mustEqual "allowed"
        list(9).list(17).list.head.field2 mustEqual "false"
        list(9).list(18).unk mustEqual 865
        list(9).list(18).list.length mustEqual 1
        list(9).list(18).list.head.field1 mustEqual "allowed"
        list(9).list(18).list.head.field2 mustEqual "false"
        list(9).list(19).unk mustEqual 923
        list(9).list(19).list.length mustEqual 1
        list(9).list(19).list.head.field1 mustEqual "allowed"
        list(9).list(19).list.head.field2 mustEqual "false"
        list(9).list(20).unk mustEqual 986
        list(9).list(20).list.length mustEqual 1
        list(9).list(20).list.head.field1 mustEqual "allowed"
        list(9).list(20).list.head.field2 mustEqual "false"
        //
        list(10).unk mustEqual 32
        list(10).list.length mustEqual 21
        list(10).list.head.unk mustEqual 83
        list(10).list.head.list.length mustEqual 1
        list(10).list.head.list.head.field1 mustEqual "allowed"
        list(10).list.head.list.head.field2 mustEqual "false"
        list(10).list(1).unk mustEqual 84
        list(10).list(1).list.length mustEqual 1
        list(10).list(1).list.head.field1 mustEqual "allowed"
        list(10).list(1).list.head.field2 mustEqual "false"
        list(10).list(2).unk mustEqual 118
        list(10).list(2).list.length mustEqual 1
        list(10).list(2).list.head.field1 mustEqual "allowed"
        list(10).list(2).list.head.field2 mustEqual "false"
        list(10).list(3).unk mustEqual 135
        list(10).list(3).list.length mustEqual 1
        list(10).list(3).list.head.field1 mustEqual "allowed"
        list(10).list(3).list.head.field2 mustEqual "false"
        list(10).list(4).unk mustEqual 199
        list(10).list(4).list.length mustEqual 1
        list(10).list(4).list.head.field1 mustEqual "allowed"
        list(10).list(4).list.head.field2 mustEqual "false"
        list(10).list(5).unk mustEqual 200
        list(10).list(5).list.length mustEqual 1
        list(10).list(5).list.head.field1 mustEqual "allowed"
        list(10).list(5).list.head.field2 mustEqual "false"
        list(10).list(6).unk mustEqual 294
        list(10).list(6).list.length mustEqual 1
        list(10).list(6).list.head.field1 mustEqual "allowed"
        list(10).list(6).list.head.field2 mustEqual "false"
        list(10).list(7).unk mustEqual 338
        list(10).list(7).list.length mustEqual 1
        list(10).list(7).list.head.field1 mustEqual "allowed"
        list(10).list(7).list.head.field2 mustEqual "false"
        list(10).list(8).unk mustEqual 429
        list(10).list(8).list.length mustEqual 1
        list(10).list(8).list.head.field1 mustEqual "allowed"
        list(10).list(8).list.head.field2 mustEqual "false"
        list(10).list(9).unk mustEqual 432
        list(10).list(9).list.length mustEqual 1
        list(10).list(9).list.head.field1 mustEqual "allowed"
        list(10).list(9).list.head.field2 mustEqual "false"
        list(10).list(10).unk mustEqual 441
        list(10).list(10).list.length mustEqual 1
        list(10).list(10).list.head.field1 mustEqual "allowed"
        list(10).list(10).list.head.field2 mustEqual "false"
        list(10).list(11).unk mustEqual 462
        list(10).list(11).list.length mustEqual 1
        list(10).list(11).list.head.field1 mustEqual "allowed"
        list(10).list(11).list.head.field2 mustEqual "false"
        list(10).list(12).unk mustEqual 470
        list(10).list(12).list.length mustEqual 1
        list(10).list(12).list.head.field1 mustEqual "allowed"
        list(10).list(12).list.head.field2 mustEqual "false"
        list(10).list(13).unk mustEqual 556
        list(10).list(13).list.length mustEqual 1
        list(10).list(13).list.head.field1 mustEqual "allowed"
        list(10).list(13).list.head.field2 mustEqual "false"
        list(10).list(14).unk mustEqual 642
        list(10).list(14).list.length mustEqual 1
        list(10).list(14).list.head.field1 mustEqual "allowed"
        list(10).list(14).list.head.field2 mustEqual "false"
        list(10).list(15).unk mustEqual 643
        list(10).list(15).list.length mustEqual 1
        list(10).list(15).list.head.field1 mustEqual "allowed"
        list(10).list(15).list.head.field2 mustEqual "false"
        list(10).list(16).unk mustEqual 697
        list(10).list(16).list.length mustEqual 1
        list(10).list(16).list.head.field1 mustEqual "allowed"
        list(10).list(16).list.head.field2 mustEqual "false"
        list(10).list(17).unk mustEqual 714
        list(10).list(17).list.length mustEqual 1
        list(10).list(17).list.head.field1 mustEqual "allowed"
        list(10).list(17).list.head.field2 mustEqual "false"
        list(10).list(18).unk mustEqual 865
        list(10).list(18).list.length mustEqual 1
        list(10).list(18).list.head.field1 mustEqual "allowed"
        list(10).list(18).list.head.field2 mustEqual "false"
        list(10).list(19).unk mustEqual 923
        list(10).list(19).list.length mustEqual 1
        list(10).list(19).list.head.field1 mustEqual "allowed"
        list(10).list(19).list.head.field2 mustEqual "false"
        list(10).list(20).unk mustEqual 986
        list(10).list(20).list.length mustEqual 1
        list(10).list(20).list.head.field1 mustEqual "allowed"
        list(10).list(20).list.head.field2 mustEqual "false"
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = PropertyOverrideMessage(
      List(
        GameProperty(0, List(
          GamePropertyField(343, List(
            GamePropertyValues("purchase_exempt_vs", ""),
            GamePropertyValues("purchase_exempt_tr", ""),
            GamePropertyValues("purchase_exempt_nc", "")
          ))
        )),
        GameProperty(17, List(
          GamePropertyField(421, List(GamePropertyValues("allowed", "false")))
        )),
        GameProperty(18, List(
          GamePropertyField(421, List(GamePropertyValues("allowed", "false")))
        )),
        GameProperty(19, List(
          GamePropertyField(421, List(GamePropertyValues("allowed", "false")))
        )),
        GameProperty(20, List(
          GamePropertyField(421, List(GamePropertyValues("allowed", "false")))
        )),
        GameProperty(21, List(
          GamePropertyField(421, List(GamePropertyValues("allowed", "false")))
        )),
        GameProperty(22, List(
          GamePropertyField(421, List(GamePropertyValues("allowed", "false")))
        )),
        GameProperty(29, List(
          GamePropertyField(83, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(84, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(118, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(135, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(199, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(200, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(294, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(338, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(429, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(432, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(441, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(462, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(470, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(556, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(642, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(643, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(697, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(714, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(865, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(923, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(986, List(GamePropertyValues("allowed", "false")))
        )),
        GameProperty(30, List(
          GamePropertyField(83, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(84, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(118, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(135, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(199, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(200, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(294, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(338, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(429, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(432, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(441, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(462, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(470, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(556, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(642, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(643, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(697, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(714, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(865, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(923, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(986, List(GamePropertyValues("allowed", "false")))
        )),
        GameProperty(31, List(
          GamePropertyField(83, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(84, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(118, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(135, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(199, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(200, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(294, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(338, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(429, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(432, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(441, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(462, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(470, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(556, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(642, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(643, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(697, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(714, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(865, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(923, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(986, List(GamePropertyValues("allowed", "false")))
        )),
        GameProperty(32, List(
          GamePropertyField(83, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(84, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(118, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(135, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(199, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(200, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(294, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(338, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(429, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(432, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(441, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(462, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(470, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(556, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(642, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(643, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(697, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(714, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(865, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(923, List(GamePropertyValues("allowed", "false"))),
          GamePropertyField(986, List(GamePropertyValues("allowed", "false")))
        ))
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}
