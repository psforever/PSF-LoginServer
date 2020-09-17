// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.packet.game.objectcreate.ObjectClass
import scodec.bits._

class PropertyOverrideMessageTest extends Specification {
  val string =
    hex"D5 0B 00 00 00 01 0A E4  0C 02 48 70 75 72 63 68 61 73 65 5F 65 78 65 6D  70 74 5F 76 73 80 92 70 75 72 63 68 61 73 65 5F  65 78 65 6D 70 74 5F 74 72 80 92 70 75 72 63 68  61 73 65 5F 65 78 65 6D 70 74 5F 6E 63 80 11 00  01 14 A4 04 02 1C 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 12 00 01 14 A4 04 02 1C 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 13 00 01 14 A4 04 02 1C  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 14 00 01  14 A4 04 02 1C 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 15 00 01 14 A4 04 02 1C 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 16 00 01 14 A4 04 02 1C 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 1D 00 15 0A  60 04 02 1C 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 54 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 76 00 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 87 00 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 C7 00 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 C8 00 20 10 E0 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 26 20 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 52 20 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 AD  20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 B0 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 B9 20 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 CE 20 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 D6 20 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 2C 40 20 10 E0 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 82 40 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 83 40 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 B9  40 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 CA 40 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 61 60 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 9B 60 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 DA 60 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 1E 00 15 0A 60 04 02  1C 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 54 00  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  76 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 87 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 C7 00 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 C8 00 20 10 E0 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 26 20 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 52 20 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 AD 20 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 B0 20  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  B9 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 CE 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 D6 20 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 2C 40 20 10 E0 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 82 40 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 83 40 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 B9 40 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 CA 40  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  61 60 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 9B 60 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 DA 60 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 1F 00 15 0A 60 04 02 1C 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 54 00 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 76 00 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 87  00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 C7 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 C8 00 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 26 20 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 52 20 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 AD 20 20 10 E0 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 B0 20 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 B9 20 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 CE  20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 D6 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 2C 40 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 82 40 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 83 40 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 B9 40 20 10 E0 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 CA 40 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 61 60 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 9B  60 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 DA 60 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 20 00 15 0A 60 04 02 1C 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 54 00 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 76 00 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 87 00 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 C7 00  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  C8 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 26 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 52 20 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 AD 20 20 10 E0 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 B0 20 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 B9 20 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 CE 20 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 D6 20  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  2C 40 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 82 40 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 83 40 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 B9 40 20 10 E0 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 CA 40 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 61 60 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 9B 60 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 DA 60  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case PropertyOverrideMessage(list) =>
        list.length mustEqual 11
        //
        list.head.zone mustEqual 0
        list.head.list.length mustEqual 1
        list.head.list.head.target mustEqual 343
        list.head.list.head.list.length mustEqual 3
        list.head.list.head.list.head.field1 mustEqual "purchase_exempt_vs"
        list.head.list.head.list.head.field2 mustEqual ""
        list.head.list.head.list(1).field1 mustEqual "purchase_exempt_tr"
        list.head.list.head.list(1).field2 mustEqual ""
        list.head.list.head.list(2).field1 mustEqual "purchase_exempt_nc"
        list.head.list.head.list(2).field2 mustEqual ""
        //
        list(1).zone mustEqual 17
        list(1).list.length mustEqual 1
        list(1).list.head.target mustEqual 421
        list(1).list.head.list.length mustEqual 1
        list(1).list.head.list.head.field1 mustEqual "allowed"
        list(1).list.head.list.head.field2 mustEqual "false"
        //
        list(2).zone mustEqual 18
        list(2).list.length mustEqual 1
        list(2).list.head.target mustEqual 421
        list(2).list.head.list.length mustEqual 1
        list(2).list.head.list.head.field1 mustEqual "allowed"
        list(2).list.head.list.head.field2 mustEqual "false"
        //
        list(3).zone mustEqual 19
        list(3).list.length mustEqual 1
        list(3).list.head.target mustEqual 421
        list(3).list.head.list.length mustEqual 1
        list(3).list.head.list.head.field1 mustEqual "allowed"
        list(3).list.head.list.head.field2 mustEqual "false"
        //
        list(4).zone mustEqual 20
        list(4).list.length mustEqual 1
        list(4).list.head.target mustEqual 421
        list(4).list.head.list.length mustEqual 1
        list(4).list.head.list.head.field1 mustEqual "allowed"
        list(4).list.head.list.head.field2 mustEqual "false"
        //
        list(5).zone mustEqual 21
        list(5).list.length mustEqual 1
        list(5).list.head.target mustEqual 421
        list(5).list.head.list.length mustEqual 1
        list(5).list.head.list.head.field1 mustEqual "allowed"
        list(5).list.head.list.head.field2 mustEqual "false"
        //
        list(6).zone mustEqual 22
        list(6).list.length mustEqual 1
        list(6).list.head.target mustEqual 421
        list(6).list.head.list.length mustEqual 1
        list(6).list.head.list.head.field1 mustEqual "allowed"
        list(6).list.head.list.head.field2 mustEqual "false"
        //
        list(7).zone mustEqual 29
        list(7).list.length mustEqual 21
        list(7).list.head.target mustEqual 83
        list(7).list.head.list.length mustEqual 1
        list(7).list.head.list.head.field1 mustEqual "allowed"
        list(7).list.head.list.head.field2 mustEqual "false"
        list(7).list(1).target mustEqual 84
        list(7).list(1).list.length mustEqual 1
        list(7).list(1).list.head.field1 mustEqual "allowed"
        list(7).list(1).list.head.field2 mustEqual "false"
        list(7).list(2).target mustEqual 118
        list(7).list(2).list.length mustEqual 1
        list(7).list(2).list.head.field1 mustEqual "allowed"
        list(7).list(2).list.head.field2 mustEqual "false"
        list(7).list(3).target mustEqual 135
        list(7).list(3).list.length mustEqual 1
        list(7).list(3).list.head.field1 mustEqual "allowed"
        list(7).list(3).list.head.field2 mustEqual "false"
        list(7).list(4).target mustEqual 199
        list(7).list(4).list.length mustEqual 1
        list(7).list(4).list.head.field1 mustEqual "allowed"
        list(7).list(4).list.head.field2 mustEqual "false"
        list(7).list(5).target mustEqual 200
        list(7).list(5).list.length mustEqual 1
        list(7).list(5).list.head.field1 mustEqual "allowed"
        list(7).list(5).list.head.field2 mustEqual "false"
        list(7).list(6).target mustEqual 294
        list(7).list(6).list.length mustEqual 1
        list(7).list(6).list.head.field1 mustEqual "allowed"
        list(7).list(6).list.head.field2 mustEqual "false"
        list(7).list(7).target mustEqual 338
        list(7).list(7).list.length mustEqual 1
        list(7).list(7).list.head.field1 mustEqual "allowed"
        list(7).list(7).list.head.field2 mustEqual "false"
        list(7).list(8).target mustEqual 429
        list(7).list(8).list.length mustEqual 1
        list(7).list(8).list.head.field1 mustEqual "allowed"
        list(7).list(8).list.head.field2 mustEqual "false"
        list(7).list(9).target mustEqual 432
        list(7).list(9).list.length mustEqual 1
        list(7).list(9).list.head.field1 mustEqual "allowed"
        list(7).list(9).list.head.field2 mustEqual "false"
        list(7).list(10).target mustEqual 441
        list(7).list(10).list.length mustEqual 1
        list(7).list(10).list.head.field1 mustEqual "allowed"
        list(7).list(10).list.head.field2 mustEqual "false"
        list(7).list(11).target mustEqual 462
        list(7).list(11).list.length mustEqual 1
        list(7).list(11).list.head.field1 mustEqual "allowed"
        list(7).list(11).list.head.field2 mustEqual "false"
        list(7).list(12).target mustEqual 470
        list(7).list(12).list.length mustEqual 1
        list(7).list(12).list.head.field1 mustEqual "allowed"
        list(7).list(12).list.head.field2 mustEqual "false"
        list(7).list(13).target mustEqual 556
        list(7).list(13).list.length mustEqual 1
        list(7).list(13).list.head.field1 mustEqual "allowed"
        list(7).list(13).list.head.field2 mustEqual "false"
        list(7).list(14).target mustEqual 642
        list(7).list(14).list.length mustEqual 1
        list(7).list(14).list.head.field1 mustEqual "allowed"
        list(7).list(14).list.head.field2 mustEqual "false"
        list(7).list(15).target mustEqual 643
        list(7).list(15).list.length mustEqual 1
        list(7).list(15).list.head.field1 mustEqual "allowed"
        list(7).list(15).list.head.field2 mustEqual "false"
        list(7).list(16).target mustEqual 697
        list(7).list(16).list.length mustEqual 1
        list(7).list(16).list.head.field1 mustEqual "allowed"
        list(7).list(16).list.head.field2 mustEqual "false"
        list(7).list(17).target mustEqual 714
        list(7).list(17).list.length mustEqual 1
        list(7).list(17).list.head.field1 mustEqual "allowed"
        list(7).list(17).list.head.field2 mustEqual "false"
        list(7).list(18).target mustEqual 865
        list(7).list(18).list.length mustEqual 1
        list(7).list(18).list.head.field1 mustEqual "allowed"
        list(7).list(18).list.head.field2 mustEqual "false"
        list(7).list(19).target mustEqual 923
        list(7).list(19).list.length mustEqual 1
        list(7).list(19).list.head.field1 mustEqual "allowed"
        list(7).list(19).list.head.field2 mustEqual "false"
        list(7).list(20).target mustEqual 986
        list(7).list(20).list.length mustEqual 1
        list(7).list(20).list.head.field1 mustEqual "allowed"
        list(7).list(20).list.head.field2 mustEqual "false"
        //
        list(8).zone mustEqual 30
        list(8).list.length mustEqual 21
        list(8).list.head.target mustEqual 83
        list(8).list.head.list.length mustEqual 1
        list(8).list.head.list.head.field1 mustEqual "allowed"
        list(8).list.head.list.head.field2 mustEqual "false"
        list(8).list(1).target mustEqual 84
        list(8).list(1).list.length mustEqual 1
        list(8).list(1).list.head.field1 mustEqual "allowed"
        list(8).list(1).list.head.field2 mustEqual "false"
        list(8).list(2).target mustEqual 118
        list(8).list(2).list.length mustEqual 1
        list(8).list(2).list.head.field1 mustEqual "allowed"
        list(8).list(2).list.head.field2 mustEqual "false"
        list(8).list(3).target mustEqual 135
        list(8).list(3).list.length mustEqual 1
        list(8).list(3).list.head.field1 mustEqual "allowed"
        list(8).list(3).list.head.field2 mustEqual "false"
        list(8).list(4).target mustEqual 199
        list(8).list(4).list.length mustEqual 1
        list(8).list(4).list.head.field1 mustEqual "allowed"
        list(8).list(4).list.head.field2 mustEqual "false"
        list(8).list(5).target mustEqual 200
        list(8).list(5).list.length mustEqual 1
        list(8).list(5).list.head.field1 mustEqual "allowed"
        list(8).list(5).list.head.field2 mustEqual "false"
        list(8).list(6).target mustEqual 294
        list(8).list(6).list.length mustEqual 1
        list(8).list(6).list.head.field1 mustEqual "allowed"
        list(8).list(6).list.head.field2 mustEqual "false"
        list(8).list(7).target mustEqual 338
        list(8).list(7).list.length mustEqual 1
        list(8).list(7).list.head.field1 mustEqual "allowed"
        list(8).list(7).list.head.field2 mustEqual "false"
        list(8).list(8).target mustEqual 429
        list(8).list(8).list.length mustEqual 1
        list(8).list(8).list.head.field1 mustEqual "allowed"
        list(8).list(8).list.head.field2 mustEqual "false"
        list(8).list(9).target mustEqual 432
        list(8).list(9).list.length mustEqual 1
        list(8).list(9).list.head.field1 mustEqual "allowed"
        list(8).list(9).list.head.field2 mustEqual "false"
        list(8).list(10).target mustEqual 441
        list(8).list(10).list.length mustEqual 1
        list(8).list(10).list.head.field1 mustEqual "allowed"
        list(8).list(10).list.head.field2 mustEqual "false"
        list(8).list(11).target mustEqual 462
        list(8).list(11).list.length mustEqual 1
        list(8).list(11).list.head.field1 mustEqual "allowed"
        list(8).list(11).list.head.field2 mustEqual "false"
        list(8).list(12).target mustEqual 470
        list(8).list(12).list.length mustEqual 1
        list(8).list(12).list.head.field1 mustEqual "allowed"
        list(8).list(12).list.head.field2 mustEqual "false"
        list(8).list(13).target mustEqual 556
        list(8).list(13).list.length mustEqual 1
        list(8).list(13).list.head.field1 mustEqual "allowed"
        list(8).list(13).list.head.field2 mustEqual "false"
        list(8).list(14).target mustEqual 642
        list(8).list(14).list.length mustEqual 1
        list(8).list(14).list.head.field1 mustEqual "allowed"
        list(8).list(14).list.head.field2 mustEqual "false"
        list(8).list(15).target mustEqual 643
        list(8).list(15).list.length mustEqual 1
        list(8).list(15).list.head.field1 mustEqual "allowed"
        list(8).list(15).list.head.field2 mustEqual "false"
        list(8).list(16).target mustEqual 697
        list(8).list(16).list.length mustEqual 1
        list(8).list(16).list.head.field1 mustEqual "allowed"
        list(8).list(16).list.head.field2 mustEqual "false"
        list(8).list(17).target mustEqual 714
        list(8).list(17).list.length mustEqual 1
        list(8).list(17).list.head.field1 mustEqual "allowed"
        list(8).list(17).list.head.field2 mustEqual "false"
        list(8).list(18).target mustEqual 865
        list(8).list(18).list.length mustEqual 1
        list(8).list(18).list.head.field1 mustEqual "allowed"
        list(8).list(18).list.head.field2 mustEqual "false"
        list(8).list(19).target mustEqual 923
        list(8).list(19).list.length mustEqual 1
        list(8).list(19).list.head.field1 mustEqual "allowed"
        list(8).list(19).list.head.field2 mustEqual "false"
        list(8).list(20).target mustEqual 986
        list(8).list(20).list.length mustEqual 1
        list(8).list(20).list.head.field1 mustEqual "allowed"
        list(8).list(20).list.head.field2 mustEqual "false"
        //
        list(9).zone mustEqual 31
        list(9).list.length mustEqual 21
        list(9).list.head.target mustEqual 83
        list(9).list.head.list.length mustEqual 1
        list(9).list.head.list.head.field1 mustEqual "allowed"
        list(9).list.head.list.head.field2 mustEqual "false"
        list(9).list(1).target mustEqual 84
        list(9).list(1).list.length mustEqual 1
        list(9).list(1).list.head.field1 mustEqual "allowed"
        list(9).list(1).list.head.field2 mustEqual "false"
        list(9).list(2).target mustEqual 118
        list(9).list(2).list.length mustEqual 1
        list(9).list(2).list.head.field1 mustEqual "allowed"
        list(9).list(2).list.head.field2 mustEqual "false"
        list(9).list(3).target mustEqual 135
        list(9).list(3).list.length mustEqual 1
        list(9).list(3).list.head.field1 mustEqual "allowed"
        list(9).list(3).list.head.field2 mustEqual "false"
        list(9).list(4).target mustEqual 199
        list(9).list(4).list.length mustEqual 1
        list(9).list(4).list.head.field1 mustEqual "allowed"
        list(9).list(4).list.head.field2 mustEqual "false"
        list(9).list(5).target mustEqual 200
        list(9).list(5).list.length mustEqual 1
        list(9).list(5).list.head.field1 mustEqual "allowed"
        list(9).list(5).list.head.field2 mustEqual "false"
        list(9).list(6).target mustEqual 294
        list(9).list(6).list.length mustEqual 1
        list(9).list(6).list.head.field1 mustEqual "allowed"
        list(9).list(6).list.head.field2 mustEqual "false"
        list(9).list(7).target mustEqual 338
        list(9).list(7).list.length mustEqual 1
        list(9).list(7).list.head.field1 mustEqual "allowed"
        list(9).list(7).list.head.field2 mustEqual "false"
        list(9).list(8).target mustEqual 429
        list(9).list(8).list.length mustEqual 1
        list(9).list(8).list.head.field1 mustEqual "allowed"
        list(9).list(8).list.head.field2 mustEqual "false"
        list(9).list(9).target mustEqual 432
        list(9).list(9).list.length mustEqual 1
        list(9).list(9).list.head.field1 mustEqual "allowed"
        list(9).list(9).list.head.field2 mustEqual "false"
        list(9).list(10).target mustEqual 441
        list(9).list(10).list.length mustEqual 1
        list(9).list(10).list.head.field1 mustEqual "allowed"
        list(9).list(10).list.head.field2 mustEqual "false"
        list(9).list(11).target mustEqual 462
        list(9).list(11).list.length mustEqual 1
        list(9).list(11).list.head.field1 mustEqual "allowed"
        list(9).list(11).list.head.field2 mustEqual "false"
        list(9).list(12).target mustEqual 470
        list(9).list(12).list.length mustEqual 1
        list(9).list(12).list.head.field1 mustEqual "allowed"
        list(9).list(12).list.head.field2 mustEqual "false"
        list(9).list(13).target mustEqual 556
        list(9).list(13).list.length mustEqual 1
        list(9).list(13).list.head.field1 mustEqual "allowed"
        list(9).list(13).list.head.field2 mustEqual "false"
        list(9).list(14).target mustEqual 642
        list(9).list(14).list.length mustEqual 1
        list(9).list(14).list.head.field1 mustEqual "allowed"
        list(9).list(14).list.head.field2 mustEqual "false"
        list(9).list(15).target mustEqual 643
        list(9).list(15).list.length mustEqual 1
        list(9).list(15).list.head.field1 mustEqual "allowed"
        list(9).list(15).list.head.field2 mustEqual "false"
        list(9).list(16).target mustEqual 697
        list(9).list(16).list.length mustEqual 1
        list(9).list(16).list.head.field1 mustEqual "allowed"
        list(9).list(16).list.head.field2 mustEqual "false"
        list(9).list(17).target mustEqual 714
        list(9).list(17).list.length mustEqual 1
        list(9).list(17).list.head.field1 mustEqual "allowed"
        list(9).list(17).list.head.field2 mustEqual "false"
        list(9).list(18).target mustEqual 865
        list(9).list(18).list.length mustEqual 1
        list(9).list(18).list.head.field1 mustEqual "allowed"
        list(9).list(18).list.head.field2 mustEqual "false"
        list(9).list(19).target mustEqual 923
        list(9).list(19).list.length mustEqual 1
        list(9).list(19).list.head.field1 mustEqual "allowed"
        list(9).list(19).list.head.field2 mustEqual "false"
        list(9).list(20).target mustEqual 986
        list(9).list(20).list.length mustEqual 1
        list(9).list(20).list.head.field1 mustEqual "allowed"
        list(9).list(20).list.head.field2 mustEqual "false"
        //
        list(10).zone mustEqual 32
        list(10).list.length mustEqual 21
        list(10).list.head.target mustEqual 83
        list(10).list.head.list.length mustEqual 1
        list(10).list.head.list.head.field1 mustEqual "allowed"
        list(10).list.head.list.head.field2 mustEqual "false"
        list(10).list(1).target mustEqual 84
        list(10).list(1).list.length mustEqual 1
        list(10).list(1).list.head.field1 mustEqual "allowed"
        list(10).list(1).list.head.field2 mustEqual "false"
        list(10).list(2).target mustEqual 118
        list(10).list(2).list.length mustEqual 1
        list(10).list(2).list.head.field1 mustEqual "allowed"
        list(10).list(2).list.head.field2 mustEqual "false"
        list(10).list(3).target mustEqual 135
        list(10).list(3).list.length mustEqual 1
        list(10).list(3).list.head.field1 mustEqual "allowed"
        list(10).list(3).list.head.field2 mustEqual "false"
        list(10).list(4).target mustEqual 199
        list(10).list(4).list.length mustEqual 1
        list(10).list(4).list.head.field1 mustEqual "allowed"
        list(10).list(4).list.head.field2 mustEqual "false"
        list(10).list(5).target mustEqual 200
        list(10).list(5).list.length mustEqual 1
        list(10).list(5).list.head.field1 mustEqual "allowed"
        list(10).list(5).list.head.field2 mustEqual "false"
        list(10).list(6).target mustEqual 294
        list(10).list(6).list.length mustEqual 1
        list(10).list(6).list.head.field1 mustEqual "allowed"
        list(10).list(6).list.head.field2 mustEqual "false"
        list(10).list(7).target mustEqual 338
        list(10).list(7).list.length mustEqual 1
        list(10).list(7).list.head.field1 mustEqual "allowed"
        list(10).list(7).list.head.field2 mustEqual "false"
        list(10).list(8).target mustEqual 429
        list(10).list(8).list.length mustEqual 1
        list(10).list(8).list.head.field1 mustEqual "allowed"
        list(10).list(8).list.head.field2 mustEqual "false"
        list(10).list(9).target mustEqual 432
        list(10).list(9).list.length mustEqual 1
        list(10).list(9).list.head.field1 mustEqual "allowed"
        list(10).list(9).list.head.field2 mustEqual "false"
        list(10).list(10).target mustEqual 441
        list(10).list(10).list.length mustEqual 1
        list(10).list(10).list.head.field1 mustEqual "allowed"
        list(10).list(10).list.head.field2 mustEqual "false"
        list(10).list(11).target mustEqual 462
        list(10).list(11).list.length mustEqual 1
        list(10).list(11).list.head.field1 mustEqual "allowed"
        list(10).list(11).list.head.field2 mustEqual "false"
        list(10).list(12).target mustEqual 470
        list(10).list(12).list.length mustEqual 1
        list(10).list(12).list.head.field1 mustEqual "allowed"
        list(10).list(12).list.head.field2 mustEqual "false"
        list(10).list(13).target mustEqual 556
        list(10).list(13).list.length mustEqual 1
        list(10).list(13).list.head.field1 mustEqual "allowed"
        list(10).list(13).list.head.field2 mustEqual "false"
        list(10).list(14).target mustEqual 642
        list(10).list(14).list.length mustEqual 1
        list(10).list(14).list.head.field1 mustEqual "allowed"
        list(10).list(14).list.head.field2 mustEqual "false"
        list(10).list(15).target mustEqual 643
        list(10).list(15).list.length mustEqual 1
        list(10).list(15).list.head.field1 mustEqual "allowed"
        list(10).list(15).list.head.field2 mustEqual "false"
        list(10).list(16).target mustEqual 697
        list(10).list(16).list.length mustEqual 1
        list(10).list(16).list.head.field1 mustEqual "allowed"
        list(10).list(16).list.head.field2 mustEqual "false"
        list(10).list(17).target mustEqual 714
        list(10).list(17).list.length mustEqual 1
        list(10).list(17).list.head.field1 mustEqual "allowed"
        list(10).list(17).list.head.field2 mustEqual "false"
        list(10).list(18).target mustEqual 865
        list(10).list(18).list.length mustEqual 1
        list(10).list(18).list.head.field1 mustEqual "allowed"
        list(10).list(18).list.head.field2 mustEqual "false"
        list(10).list(19).target mustEqual 923
        list(10).list(19).list.length mustEqual 1
        list(10).list(19).list.head.field1 mustEqual "allowed"
        list(10).list(19).list.head.field2 mustEqual "false"
        list(10).list(20).target mustEqual 986
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
        GamePropertyScope(
          0,
          GamePropertyTarget(
            GamePropertyTarget.game_properties,
            List(
              "purchase_exempt_vs" -> "",
              "purchase_exempt_tr" -> "",
              "purchase_exempt_nc" -> ""
            )
          )
        ),
        GamePropertyScope(17, GamePropertyTarget(ObjectClass.katana, "allowed" -> "false")),
        GamePropertyScope(18, GamePropertyTarget(ObjectClass.katana, "allowed" -> "false")),
        GamePropertyScope(19, GamePropertyTarget(ObjectClass.katana, "allowed" -> "false")),
        GamePropertyScope(20, GamePropertyTarget(ObjectClass.katana, "allowed" -> "false")),
        GamePropertyScope(21, GamePropertyTarget(ObjectClass.katana, "allowed" -> "false")),
        GamePropertyScope(22, GamePropertyTarget(ObjectClass.katana, "allowed" -> "false")),
        GamePropertyScope(
          29,
          List(
            GamePropertyTarget(ObjectClass.aphelion_flight, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.aphelion_gunner, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.aurora, "allowed"           -> "false"),
            GamePropertyTarget(ObjectClass.battlewagon, "allowed"      -> "false"),
            GamePropertyTarget(ObjectClass.colossus_flight, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.colossus_gunner, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.flail, "allowed"            -> "false"),
            GamePropertyTarget(ObjectClass.galaxy_gunship, "allowed"   -> "false"),
            GamePropertyTarget(ObjectClass.lasher, "allowed"           -> "false"),
            GamePropertyTarget(ObjectClass.liberator, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.lightgunship, "allowed"     -> "false"),
            GamePropertyTarget(ObjectClass.maelstrom, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.magrider, "allowed"         -> "false"),
            GamePropertyTarget(ObjectClass.mini_chaingun, "allowed"    -> "false"),
            GamePropertyTarget(ObjectClass.peregrine_flight, "allowed" -> "false"),
            GamePropertyTarget(ObjectClass.peregrine_gunner, "allowed" -> "false"),
            GamePropertyTarget(ObjectClass.prowler, "allowed"          -> "false"),
            GamePropertyTarget(ObjectClass.r_shotgun, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.thunderer, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.vanguard, "allowed"         -> "false"),
            GamePropertyTarget(ObjectClass.vulture, "allowed"          -> "false")
          )
        ),
        GamePropertyScope(
          30,
          List(
            GamePropertyTarget(ObjectClass.aphelion_flight, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.aphelion_gunner, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.aurora, "allowed"           -> "false"),
            GamePropertyTarget(ObjectClass.battlewagon, "allowed"      -> "false"),
            GamePropertyTarget(ObjectClass.colossus_flight, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.colossus_gunner, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.flail, "allowed"            -> "false"),
            GamePropertyTarget(ObjectClass.galaxy_gunship, "allowed"   -> "false"),
            GamePropertyTarget(ObjectClass.lasher, "allowed"           -> "false"),
            GamePropertyTarget(ObjectClass.liberator, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.lightgunship, "allowed"     -> "false"),
            GamePropertyTarget(ObjectClass.maelstrom, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.magrider, "allowed"         -> "false"),
            GamePropertyTarget(ObjectClass.mini_chaingun, "allowed"    -> "false"),
            GamePropertyTarget(ObjectClass.peregrine_flight, "allowed" -> "false"),
            GamePropertyTarget(ObjectClass.peregrine_gunner, "allowed" -> "false"),
            GamePropertyTarget(ObjectClass.prowler, "allowed"          -> "false"),
            GamePropertyTarget(ObjectClass.r_shotgun, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.thunderer, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.vanguard, "allowed"         -> "false"),
            GamePropertyTarget(ObjectClass.vulture, "allowed"          -> "false")
          )
        ),
        GamePropertyScope(
          31,
          List(
            GamePropertyTarget(ObjectClass.aphelion_flight, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.aphelion_gunner, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.aurora, "allowed"           -> "false"),
            GamePropertyTarget(ObjectClass.battlewagon, "allowed"      -> "false"),
            GamePropertyTarget(ObjectClass.colossus_flight, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.colossus_gunner, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.flail, "allowed"            -> "false"),
            GamePropertyTarget(ObjectClass.galaxy_gunship, "allowed"   -> "false"),
            GamePropertyTarget(ObjectClass.lasher, "allowed"           -> "false"),
            GamePropertyTarget(ObjectClass.liberator, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.lightgunship, "allowed"     -> "false"),
            GamePropertyTarget(ObjectClass.maelstrom, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.magrider, "allowed"         -> "false"),
            GamePropertyTarget(ObjectClass.mini_chaingun, "allowed"    -> "false"),
            GamePropertyTarget(ObjectClass.peregrine_flight, "allowed" -> "false"),
            GamePropertyTarget(ObjectClass.peregrine_gunner, "allowed" -> "false"),
            GamePropertyTarget(ObjectClass.prowler, "allowed"          -> "false"),
            GamePropertyTarget(ObjectClass.r_shotgun, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.thunderer, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.vanguard, "allowed"         -> "false"),
            GamePropertyTarget(ObjectClass.vulture, "allowed"          -> "false")
          )
        ),
        GamePropertyScope(
          32,
          List(
            GamePropertyTarget(ObjectClass.aphelion_flight, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.aphelion_gunner, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.aurora, "allowed"           -> "false"),
            GamePropertyTarget(ObjectClass.battlewagon, "allowed"      -> "false"),
            GamePropertyTarget(ObjectClass.colossus_flight, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.colossus_gunner, "allowed"  -> "false"),
            GamePropertyTarget(ObjectClass.flail, "allowed"            -> "false"),
            GamePropertyTarget(ObjectClass.galaxy_gunship, "allowed"   -> "false"),
            GamePropertyTarget(ObjectClass.lasher, "allowed"           -> "false"),
            GamePropertyTarget(ObjectClass.liberator, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.lightgunship, "allowed"     -> "false"),
            GamePropertyTarget(ObjectClass.maelstrom, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.magrider, "allowed"         -> "false"),
            GamePropertyTarget(ObjectClass.mini_chaingun, "allowed"    -> "false"),
            GamePropertyTarget(ObjectClass.peregrine_flight, "allowed" -> "false"),
            GamePropertyTarget(ObjectClass.peregrine_gunner, "allowed" -> "false"),
            GamePropertyTarget(ObjectClass.prowler, "allowed"          -> "false"),
            GamePropertyTarget(ObjectClass.r_shotgun, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.thunderer, "allowed"        -> "false"),
            GamePropertyTarget(ObjectClass.vanguard, "allowed"         -> "false"),
            GamePropertyTarget(ObjectClass.vulture, "allowed"          -> "false")
          )
        )
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}
